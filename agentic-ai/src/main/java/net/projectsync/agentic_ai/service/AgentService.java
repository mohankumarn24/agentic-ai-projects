package net.projectsync.agentic_ai.service;

import org.springframework.stereotype.Service;

/**
 * Core agent logic — the "brain" of the application.
 *
 * This service implements a two-step agentic loop:
 *   1. PLAN  — Ask Gemini to break the goal into numbered steps
 *   2. EXECUTE — Ask Gemini to carry out that plan
 *
 * Both results are stored in MemoryStore so they can be retrieved later
 * (e.g. for debugging or multi-turn conversations).
 *
 * Note:
 *  - This makes 2 API calls to Gemini per request.
 *  - With the free tier limit of ~10 RPM, this means you can handle roughly 5 goals per minute.
 */
@Service
public class AgentService {

    private final GeminiClient geminiClient;
    private final MemoryStore memory;

    // Both dependencies are injected by Spring automatically
    public AgentService(GeminiClient geminiClient, MemoryStore memory) {
        this.geminiClient = geminiClient;
        this.memory = memory;
    }

    /**
     * Runs the two-step agent loop for a given goal.
     *
     * @param sessionId  Unique ID for this user's session
     * @param goal       What the user wants to achieve
     * @return           The final AI-generated output
     */
    public String executeGoal(String sessionId, String goal) {

        // Step 1: Planning (Decide what to do -> Think first. Don’t act yet)
        // Ask Gemini to think about HOW to achieve the goal.
        // Keeping this separate from execution gives better results —
        // the model can focus purely on decomposing the problem first.
        String planningPrompt = """
                You are an AI planner.
                Break the following goal into clear, ordered steps.
                Do NOT execute the steps.
                Return ONLY a numbered list.
                
                Goal:
                %s
                """.formatted(goal);
        String plan = geminiClient.generate(planningPrompt);
        /*
            -----
            Input:
            -----
            You are an AI planner.
            Break the following goal into clear, ordered steps.
            Do NOT execute the steps.
            Return ONLY a numbered list.

            Goal:
            Write a short blog post about the benefits of drinking water

            -------
            Output:
            -------
            1.  Brainstorm and select 3-5 key benefits of drinking water to feature.
            2.  Create an outline for the blog post, including a title, introduction, main body paragraphs (one for each benefit), and a conclusion.
            3.  Draft a compelling and catchy title for the blog post.
            4.  Write an engaging introduction that highlights the general importance of hydration.
            5.  Develop individual body paragraphs, each explaining one of the selected benefits of drinking water.
            6.  Compose a concise conclusion that summarizes the benefits and encourages readers to drink more water.
            7.  Review the entire blog post for clarity, conciseness, grammar, spelling, and punctuation.
            8.  Ensure the post maintains an appropriate tone and length for a "short blog post."
         */

        // Save the plan so it can be inspected or reused later
        memory.add(sessionId, "PLAN", plan);

        // Step 2: Execution (Actually do it -> Now follow the plan and produce the answer)
        // Now ask Gemini to carry out the plan.
        // We include BOTH the original goal AND the plan so the model
        // has full context — not just the plan in isolation.
        String executionPrompt = """
                You are an AI executor.
                Follow the plan strictly and produce the final output.
                Do NOT create a new plan.
                Do NOT explain the plan.
                Return ONLY the final result.
                
                Original goal:
                %s
                
                Plan:
                %s
                """.formatted(goal, plan);
        String result = geminiClient.generate(executionPrompt);
        /*
            -----
            Input:
            -----
            You are an AI executor.
            Follow the plan strictly and produce the final output.
            Do NOT create a new plan.
            Do NOT explain the plan.
            Return ONLY the final result.

            Original goal:
            Write a short blog post about the benefits of drinking water

            Plan:
            1.  Brainstorm and select 3-5 key benefits of drinking water to feature.
            2.  Create an outline for the blog post, including a title, introduction, main body paragraphs (one for each benefit), and a conclusion.
            3.  Draft a compelling and catchy title for the blog post.
            4.  Write an engaging introduction that highlights the general importance of hydration.
            5.  Develop individual body paragraphs, each explaining one of the selected benefits of drinking water.
            6.  Compose a concise conclusion that summarizes the benefits and encourages readers to drink more water.
            7.  Review the entire blog post for clarity, conciseness, grammar, spelling, and punctuation.
            8.  Ensure the post maintains an appropriate tone and length for a "short blog post."


            -------
            Output:
            -------
            ## Unlock Your Best Self: The Undeniable Power of Water
            We often overlook the simplest things that have the biggest impact on our health. One such powerhouse is something readily available and absolutely essential: water. It's not just for quenching thirst; proper hydration is fundamental to nearly every bodily function, influencing everything from our mood to our metabolism.
            Feeling sluggish? Dehydration is a common culprit. Even mild dehydration can drain your energy levels and make you feel tired. Drinking enough water helps your body transport nutrients and oxygen efficiently, giving you a natural energy boost throughout the day without relying on caffeine.
            Want glowing skin? Look no further than your water glass. Water helps maintain your skin's elasticity, moisture, and overall health. It flushes out toxins that can contribute to breakouts and leaves your skin looking plumper and more radiant. Think of it as your internal moisturizer.
            For a smoothly running digestive system, water is key. It helps break down food, allowing nutrients to be absorbed effectively, and softens stool, preventing constipation. Staying hydrated keeps your digestive tract moving, making you feel lighter and more comfortable.
            Ever feel foggy-brained? Your brain is mostly water, and even slight dehydration can impair concentration, memory, and overall cognitive performance. Staying well-hydrated ensures your brain cells get the oxygen and nutrients they need to function optimally, keeping your mind sharp and focused.
            From boosting your energy and enhancing your skin to aiding digestion and sharpening your mind, the benefits of drinking enough water are profound and far-reaching. Make a conscious effort to reach for that glass of H2O throughout your day. Your body will thank you!
         */

        // Save the final result
        memory.add(sessionId, "RESULT", result);

        return result;
    }
}
