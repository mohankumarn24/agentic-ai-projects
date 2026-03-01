# memory_store.py

# Equivalent to MemoryStore.java
# Simple in-memory store: sessionId -> {key -> value}

class MemoryStore:
    def __init__(self):
        # Python dicts are thread-safe for simple get/set operations
        self._sessions: dict[str, dict[str, str]] = {}

    def add(self, session_id: str, key: str, value: str):
        if session_id not in self._sessions:
            self._sessions[session_id] = {}
        self._sessions[session_id][key] = value

    def get(self, session_id: str, key: str) -> str | None:
        return self._sessions.get(session_id, {}).get(key)

    def get_all(self, session_id: str) -> dict[str, str]:
        return self._sessions.get(session_id, {})

    def clear(self, session_id: str):
        self._sessions.pop(session_id, None)