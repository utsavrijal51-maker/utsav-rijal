# Automated Student Attendance System - Backend & Computer Vision Engine

This is the FastAPI backend for the Automated Student Attendance System featuring facial embedding matching, Liveness Anti-Spoofing checks, Cooldown verification, and attendance CSV reports.

## Setup & Running Locally

### 1. Prerequisites
- Python 3.9+ installed
- `pip` package manager

### 2. Installation
```bash
# Navigate to backend directory
cd backend

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt
```

### 3. Run FastAPI Server
```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### 4. API Documentation
Once running, open your browser and navigate to:
- Interactive Swagger UI: `http://localhost:8000/docs`
- ReDoc Docs: `http://localhost:8000/redoc`

### 5. API Endpoints Overview
- `POST /register`: Register a student with name, roll_no, class_id, and 128-d face embedding vectors.
- `POST /recognize`: Real-time frame face embedding check against stored embeddings with threshold & 30-min cooldown logic.
- `GET /attendance/today`: Retrieve real-time attendance logs for today.
- `POST /attendance/override`: Admin manual attendance override (Present/Late/Absent).
- `GET /export`: Download attendance logs report as a CSV spreadsheet.
