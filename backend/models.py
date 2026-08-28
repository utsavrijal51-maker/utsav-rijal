from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Boolean, JSON
from sqlalchemy.orm import relationship
from datetime import datetime
from database import Base

class Student(Base):
    __tablename__ = "students"

    id = Column(Integer, primary_key=True, index=True)
    student_id = Column(String, unique=True, index=True, nullable=False)
    name = Column(String, nullable=False)
    roll_no = Column(String, nullable=False)
    class_id = Column(String, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    embeddings = relationship("FaceEmbedding", back_populates="student", cascade="all, delete-orphan")
    attendance_logs = relationship("AttendanceLog", back_populates="student")

class FaceEmbedding(Base):
    __tablename__ = "face_embeddings"

    id = Column(Integer, primary_key=True, index=True)
    student_id = Column(String, ForeignKey("students.student_id"), nullable=False)
    embedding_vector = Column(JSON, nullable=False)  # List of floats (128-d / 512-d)
    created_at = Column(DateTime, default=datetime.utcnow)

    student = relationship("Student", back_populates="embeddings")

class ClassSession(Base):
    __tablename__ = "class_sessions"

    id = Column(Integer, primary_key=True, index=True)
    session_id = Column(String, unique=True, index=True, nullable=False)
    course_name = Column(String, nullable=False)
    class_id = Column(String, nullable=False)
    cooldown_minutes = Column(Integer, default=30)
    distance_threshold = Column(Float, default=0.55)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)

class AttendanceLog(Base):
    __tablename__ = "attendance_logs"

    id = Column(Integer, primary_key=True, index=True)
    student_id = Column(String, ForeignKey("students.student_id"), nullable=False)
    session_id = Column(String, nullable=False)
    status = Column(String, default="Present") # Present, Late, Absent
    confidence_score = Column(Float, nullable=False)
    liveness_verified = Column(Boolean, default=True)
    timestamp = Column(DateTime, default=datetime.utcnow)

    student = relationship("Student", back_populates="attendance_logs")
