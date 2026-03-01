# models.py

from pydantic import BaseModel
from typing import Optional

# Equivalent to AgentRequest.java
# Pydantic automatically validates and parses the incoming JSON
class AgentRequest(BaseModel):
    goal: str
    session_id: Optional[str] = None  # generated if not provided