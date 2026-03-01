# agent_controller.py

from fastapi import APIRouter
import uuid
from models import AgentRequest
from agent_service import AgentService

# Equivalent to AgentController.java
router = APIRouter(prefix="/agent")

# Dependency instances (see main.py for wiring)
def make_router(agent_service: AgentService) -> APIRouter:

    # Move router INSIDE the function — created fresh each time
    router = APIRouter(prefix="/agent")

    @router.post("/execute")
    def execute_goal(request: AgentRequest) -> str:  # rename from "execute" to "execute_goal"
        session_id = request.session_id or str(uuid.uuid4())
        return agent_service.execute_goal(session_id, request.goal)

    return router