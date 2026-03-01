package net.projectsync.agentic_ai.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for per-session data.
 *
 * Think of it as a notebook with labelled sections:
 *
 *   sessionId "abc-123"
 *     ├── "PLAN"   → "1. Research the topic\n2. Write intro..."
 *     └── "RESULT" → "Here is the finished blog post..."
 *
 *   sessionId "xyz-456"
 *     ├── "PLAN"   → "1. Outline the steps..."
 *     └── "RESULT" → "The final output..."
 *
 * Why ConcurrentHashMap?
 * This service is a singleton shared across all requests.
 * If two users send requests at the same time, both threads
 * will write to this map simultaneously. ConcurrentHashMap
 * is thread-safe — a regular HashMap is not and would corrupt data.
 *
 * Limitation: data is lost when the application restarts.
 * For production, replace this with Redis or a database.
 */
@Service
public class MemoryStore {

    // Outer map: sessionId → inner map
    // Inner map: key (e.g. "PLAN") → value
    private final ConcurrentHashMap<String, Map<String, String>> sessions =
            new ConcurrentHashMap<>();

    /**
     * Stores a value under a key for a given session.
     * Creates the session entry if it doesn't exist yet.
     *
     * @param sessionId  The session this data belongs to
     * @param key        What kind of data this is (e.g. "PLAN", "RESULT")
     * @param value      The data to store
     */
    public void add(String sessionId, String key, String value) {
        // computeIfAbsent: create the inner map for this session if it doesn't exist,
        // then put the key-value pair into it
        sessions.computeIfAbsent(sessionId, id -> new ConcurrentHashMap<>())
                .put(key, value);
    }

    /**
     * Retrieves a single value for a session.
     *
     * @param sessionId  The session to look up
     * @param key        The key to retrieve (e.g. "PLAN")
     * @return           The stored value, or null if not found
     */
    public String get(String sessionId, String key) {
        return sessions.getOrDefault(sessionId, Map.of()).get(key);
    }

    /**
     * Returns everything stored for a session.
     * Useful for debugging or building a conversation history view.
     *
     * @param sessionId  The session to retrieve
     * @return           All key-value pairs for that session (empty map if none)
     */
    public Map<String, String> getAll(String sessionId) {
        return sessions.getOrDefault(sessionId, Map.of());
    }

    /**
     * Removes all data for a session.
     * Call this when a user is done to free up memory.
     *
     * @param sessionId  The session to clear
     */
    public void clear(String sessionId) {
        sessions.remove(sessionId);
    }
}

/*
// MemoryStore is a singleton with a shared HashMap
// Right now every user's goals, plans, and results are stored in the same map with the same keys ("PLAN", "RESULT"). Concurrent requests will overwrite each other
@Service
public class MemoryStore {

    private final Map<String, String> memory = new HashMap<>();

    public void add(String key, String value) {
        memory.put(key, value);
    }

    public String get(String key) {
        return memory.get(key);
    }
}
*/

