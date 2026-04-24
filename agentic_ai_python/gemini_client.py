import time
from google import genai
import os


class GeminiClient:
    def __init__(self):
        api_key = os.getenv("GEMINI_API_KEY")
        if not api_key:
            raise RuntimeError("GEMINI_API_KEY not found in environment")

        self._client = genai.Client(api_key=api_key)

    def generate(self, prompt: str) -> str:
        max_retries = 5
        delay = 2

        for attempt in range(max_retries):
            try:
                response = self._client.models.generate_content(
                    model="gemini-2.5-flash-lite",  # gemini-2.5-flash
                    contents=prompt
                )
                return response.text.strip()

            except Exception as ex:
                error_msg = str(ex)

                is_retryable = any(keyword in error_msg for keyword in [
                    "503",
                    "UNAVAILABLE",
                    "429",
                    "RESOURCE_EXHAUSTED",
                    "timeout"
                ])

                if attempt < max_retries - 1 and is_retryable:
                    print(f"Retry {attempt + 1}/{max_retries} after {delay}s...")
                    time.sleep(delay)
                    delay *= 2
                    continue

                raise RuntimeError(f"Gemini API call failed: {ex}") from ex