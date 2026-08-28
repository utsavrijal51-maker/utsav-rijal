import numpy as np
from typing import List, Tuple, Optional

def compute_euclidean_distance(vector_a: List[float], vector_b: List[float]) -> float:
    a = np.array(vector_a)
    b = np.array(vector_b)
    return float(np.linalg.norm(a - b))

def compute_cosine_similarity(vector_a: List[float], vector_b: List[float]) -> float:
    a = np.array(vector_a)
    b = np.array(vector_b)
    norm_a = np.linalg.norm(a)
    norm_b = np.linalg.norm(b)
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return float(np.dot(a, b) / (norm_a * norm_b))

def eye_aspect_ratio(eye_landmarks) -> float:
    """Computes Eye Aspect Ratio (EAR) for blink detection anti-spoofing."""
    if len(eye_landmarks) < 6:
        return 0.3
    p1, p2, p3, p4, p5, p6 = eye_landmarks
    v1 = np.linalg.norm(np.array(p2) - np.array(p6))
    v2 = np.linalg.norm(np.array(p3) - np.array(p5))
    h = np.linalg.norm(np.array(p1) - np.array(p4))
    if h == 0:
        return 0.0
    ear = (v1 + v2) / (2.0 * h)
    return float(ear)

def match_face(query_embedding: List[float], known_embeddings: List[Tuple[str, List[float]]], threshold: float = 0.55) -> Optional[Tuple[str, float]]:
    best_student_id = None
    min_distance = float('inf')

    for student_id, embedding in known_embeddings:
        dist = compute_euclidean_distance(query_embedding, embedding)
        if dist < min_distance:
            min_distance = dist
            best_student_id = student_id

    if best_student_id and min_distance < threshold:
        confidence = max(0.0, min(1.0, 1.0 - (min_distance / threshold)))
        return best_student_id, confidence
    return None
