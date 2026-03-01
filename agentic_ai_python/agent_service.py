# agent_service.py

from gemini_client import GeminiClient
from memory_store import MemoryStore

class AgentService:
    def __init__(self, gemini_client: GeminiClient, memory: MemoryStore):
        self._gemini = gemini_client
        self._memory = memory

    def execute_goal(self, session_id: str, goal: str) -> str:

        # -------- STEP 1: PLANNING --------
        planning_prompt = f"""
                        You are an AI planner.
                        Break the goal into a small number of clear steps.
                        Do NOT perform the steps.
                        Return only a numbered list.
                        
                        Goal:
                        {goal}
                        """
        plan = self._gemini.generate(planning_prompt)
        self._memory.add(session_id, "PLAN", plan)

        # -------- STEP 2: EXECUTION --------
        execution_prompt = f"""
                        You are an AI executor.
                        Follow the plan to complete the goal.
                        Do not create a new plan.
                        Return only the final output.
                        
                        Goal:
                        {goal}
                        
                        Plan:
                        {plan}
                        """
        result = self._gemini.generate(execution_prompt)
        self._memory.add(session_id, "RESULT", result)

        return result