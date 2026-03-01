# main.py

from dotenv import load_dotenv
load_dotenv()  # ← add this

import uvicorn
from fastapi import FastAPI
from gemini_client import GeminiClient
from memory_store import MemoryStore
from agent_service import AgentService
from agent_controller import make_router

app = FastAPI()

gemini_client = GeminiClient()
memory = MemoryStore()
agent_service = AgentService(gemini_client, memory)

app.include_router(make_router(agent_service))

if __name__ == '__main__':
    uvicorn.run("main:app", host="localhost", port=8000, reload=True)

# http://localhost:8000/docs