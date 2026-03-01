package net.projectsync.agentic_ai.controller;

import net.projectsync.agentic_ai.model.AgentRequest;
import net.projectsync.agentic_ai.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * REST controller — the entry point for all HTTP requests.
 *
 * Listens at: POST /agent/execute
 *
 * Responsibilities:
 *  - Receive the HTTP request
 *  - Assign a sessionId if one wasn't provided
 *  - Delegate all business logic to AgentService
 *  - Return the result to the caller
 *
 * The controller intentionally has no business logic —
 * it only handles HTTP concerns.
 */
@RestController
@RequestMapping("/agent")
public class AgentController {

    // AgentService is injected by Spring via constructor injection
    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * Accepts a goal, runs the AI agent, and returns the result.
     *
     * @param request  JSON body containing goal and optional sessionId
     * @return         The AI-generated result as plain text
     */
    @PostMapping("/execute")
    public ResponseEntity<String> execute(@RequestBody AgentRequest request) {

        // Use the provided sessionId, or generate a new random one.
        // This ensures every request is tracked to a unique session.
        String sessionId = request.getSessionId() != null
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        // Hand off to the service layer — controller's job is done here
        String result = agentService.executeGoal(sessionId, request.getGoal());

        // Wrap the result in a 200 OK HTTP response
        return ResponseEntity.ok(result);
    }
}

/*
POST /agent/execute
  {
    "goal": "Write a blog post about water"
  }
  URL: http://localhost:8080/agent/execute
  Header: Content-Type=application/json

         │
         ▼

  AgentController
  ├── assigns sessionId (random UUID if not provided)
  └── calls agentService.executeGoal(sessionId, goal)

         │
         ▼

  AgentService
  ├── STEP 1: sends planning prompt → GeminiClient → Gemini API
  │           saves plan to MemoryStore["PLAN"]
  └── STEP 2: sends execution prompt → GeminiClient → Gemini API
              saves result to MemoryStore["RESULT"]
              returns result

         │
         ▼

  HTTP 200 OK — final text returned to user
*/