from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse, FileResponse
from sqlalchemy.orm import Session
from datetime import datetime, timedelta
import io
import pandas as pd
from typing import List

from database import engine, Base, get_db
import models, schemas
from facial_engine import match_face, compute_euclidean_distance

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Automated Student Attendance API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def read_root():
    return {"message": "Automated Student Attendance Facial Recognition Server Running"}

@app.post("/register", response_model=schemas.StudentResponse, status_code=status.HTTP_201_CREATED)
def register_student(data: schemas.StudentRegisterRequest, db: Session = Depends(get_db)):
    existing = db.query(models.Student).filter(models.Student.student_id == data.student_id).first()
    if existing:
        raise HTTPException(status_code=400, detail="Student ID already registered")

    student = models.Student(
        student_id=data.student_id,
        name=data.name,
        roll_no=data.roll_no,
        class_id=data.class_id
    )
    db.add(student)
    db.commit()
    db.refresh(student)

    for vec in data.embeddings:
        emb = models.FaceEmbedding(
            student_id=student.student_id,
            embedding_vector=vec
        )
        db.add(emb)

    db.commit()
    return student

@app.post("/recognize")
def recognize_and_mark_attendance(req: schemas.AttendanceRecognizeRequest, db: Session = Depends(get_db)):
    if not req.liveness_verified:
        raise HTTPException(status_code=400, detail="Liveness verification failed. Anti-spoofing triggered.")

    session = db.query(models.ClassSession).filter(models.ClassSession.session_id == req.session_id).first()
    threshold = session.distance_threshold if session else 0.55
    cooldown_mins = session.cooldown_minutes if session else 30

    embeddings = db.query(models.FaceEmbedding).all()
    known = [(e.student_id, e.embedding_vector) for e in embeddings]

    result = match_face(req.embedding, known, threshold=threshold)
    if not result:
        return {"matched": False, "message": "No matching student identified above threshold"}

    matched_student_id, confidence = result

    # Check cooldown logic
    cooldown_cutoff = datetime.utcnow() - timedelta(minutes=cooldown_mins)
    recent_log = db.query(models.AttendanceLog).filter(
        models.AttendanceLog.student_id == matched_student_id,
        models.AttendanceLog.session_id == req.session_id,
        models.AttendanceLog.timestamp >= cooldown_cutoff
    ).first()

    if recent_log:
        return {
            "matched": True,
            "student_id": matched_student_id,
            "status": "COOLDOWN_ACTIVE",
            "message": f"Attendance already logged within the last {cooldown_mins} mins.",
            "log_id": recent_log.id
        }

    # Log attendance
    new_log = models.AttendanceLog(
        student_id=matched_student_id,
        session_id=req.session_id,
        status="Present",
        confidence_score=round(confidence, 4),
        liveness_verified=True
    )
    db.add(new_log)
    db.commit()
    db.refresh(new_log)

    student = db.query(models.Student).filter(models.Student.student_id == matched_student_id).first()

    return {
        "matched": True,
        "student_id": matched_student_id,
        "student_name": student.name if student else "Unknown",
        "status": "Present",
        "confidence_score": confidence,
        "timestamp": new_log.timestamp
    }

@app.get("/attendance/today", response_model=List[schemas.AttendanceLogResponse])
def get_today_attendance(session_id: str = None, db: Session = Depends(get_db)):
    today_start = datetime.utcnow().replace(hour=0, minute=0, second=0, microsecond=0)
    query = db.query(models.AttendanceLog).filter(models.AttendanceLog.timestamp >= today_start)
    if session_id:
        query = query.filter(models.AttendanceLog.session_id == session_id)
    
    logs = query.all()
    results = []
    for log in logs:
        student = db.query(models.Student).filter(models.Student.student_id == log.student_id).first()
        item = schemas.AttendanceLogResponse.from_orm(log)
        item.student_name = student.name if student else "N/A"
        results.append(item)
    return results

@app.post("/attendance/override")
def override_attendance(req: schemas.ManualOverrideRequest, db: Session = Depends(get_db)):
    log = db.query(models.AttendanceLog).filter(models.AttendanceLog.id == req.log_id).first()
    if not log:
        raise HTTPException(status_code=404, detail="Attendance log record not found")
    log.status = req.new_status
    db.commit()
    return {"success": True, "log_id": log.id, "new_status": req.new_status}

@app.get("/export")
def export_attendance_csv(session_id: str = None, db: Session = Depends(get_db)):
    query = db.query(models.AttendanceLog)
    if session_id:
        query = query.filter(models.AttendanceLog.session_id == session_id)
    logs = query.all()

    data = []
    for log in logs:
        student = db.query(models.Student).filter(models.Student.student_id == log.student_id).first()
        data.append({
            "Log ID": log.id,
            "Student ID": log.student_id,
            "Student Name": student.name if student else "Unknown",
            "Roll No": student.roll_no if student else "Unknown",
            "Class ID": student.class_id if student else "Unknown",
            "Session ID": log.session_id,
            "Status": log.status,
            "Confidence": log.confidence_score,
            "Liveness Verified": log.liveness_verified,
            "Timestamp": log.timestamp.strftime("%Y-%m-%d %H:%M:%S")
        })

    df = pd.DataFrame(data)
    stream = io.StringIO()
    df.to_csv(stream, index=False)
    
    response = StreamingResponse(iter([stream.getvalue()]), media_type="text/csv")
    response.headers["Content-Disposition"] = "attachment; filename=attendance_report.csv"
    return response

@app.post("/classes")
def create_class_session(session_data: schemas.ClassSessionCreate, db: Session = Depends(get_db)):
    existing = db.query(models.ClassSession).filter(models.ClassSession.session_id == session_data.session_id).first()
    if existing:
        raise HTTPException(status_code=400, detail="Session ID already exists")
    
    cs = models.ClassSession(
        session_id=session_data.session_id,
        course_name=session_data.course_name,
        class_id=session_data.class_id,
        cooldown_minutes=session_data.cooldown_minutes,
        distance_threshold=session_data.distance_threshold
    )
    db.add(cs)
    db.commit()
    return {"success": True, "session_id": cs.session_id}
