package net.projectsync.agentic_ai.model;

/**
 * Represents the incoming HTTP request body.
 *
 * When the user sends a POST to /agent/execute, Spring automatically
 * converts the JSON body into this object.
 *
 * Example JSON:
 * {
 *   "goal": "Write a blog post about water",
 *   "sessionId": "abc-123"   ← optional, generated if missing
 * }
 */
public class AgentRequest {

    /** What the user wants the agent to accomplish. */
    private String goal;

    /**
     * Tracks which user/conversation this request belongs to.
     * If not provided, the controller will generate a random one.
     */
    private String sessionId;

    // Default constructor required by Jackson for JSON deserialization
    public AgentRequest() {}

    public AgentRequest(String goal, String sessionId) {
        this.goal = goal;
        this.sessionId = sessionId;
    }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}