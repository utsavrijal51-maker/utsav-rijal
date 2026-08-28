from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime

class StudentBase(BaseModel):
    student_id: str
    name: str
    roll_no: str
    class_id: str

class StudentCreate(StudentBase):
    pass

class FaceEmbeddingData(BaseModel):
    vector: List[float]

class StudentRegisterRequest(StudentBase):
    embeddings: List[List[float]]

class StudentResponse(StudentBase):
    id: int
    created_at: datetime

    class Config:
        from_attributes = True

class AttendanceRecognizeRequest(BaseModel):
    session_id: str
    embedding: List[float]
    liveness_verified: bool = True

class AttendanceLogResponse(BaseModel):
    id: int
    student_id: str
    student_name: Optional[str] = None
    session_id: str
    status: str
    confidence_score: float
    liveness_verified: bool
    timestamp: datetime

    class Config:
        from_attributes = True

class ManualOverrideRequest(BaseModel):
    log_id: int
    new_status: str

class ClassSessionCreate(BaseModel):
    session_id: str
    course_name: str
    class_id: str
    cooldown_minutes: int = 30
    distance_threshold: float = 0.55
