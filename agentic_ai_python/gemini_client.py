# gemini_client.py

from google import genai
import os

class GeminiClient:
    def __init__(self):
        api_key = os.getenv("GEMINI_API_KEY")
        if not api_key:
            raise RuntimeError("❌ GEMINI_API_KEY not found in environment")

        print(f"API Key loaded: {api_key[:8]}...")
        self._client = genai.Client(api_key=api_key)

    def generate(self, prompt: str) -> str:
        try:
            response = self._client.models.generate_content(
                model="gemini-2.5-flash",   # ✅ free-tier friendly
                contents=prompt
            )
            return response.text.strip()
        except Exception as ex:
            raise RuntimeError(f"Gemini API call failed: {ex}") from ex