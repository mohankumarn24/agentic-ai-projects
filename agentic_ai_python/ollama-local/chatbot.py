# ============================================================
# Simple Chatbot using LangChain + Ollama (Beginner Friendly)
# ============================================================

# Import Ollama LLM wrapper from LangChain community package
from langchain_ollama import OllamaLLM

# Initialize lightweight local model
llm = OllamaLLM(model="tinyllama")

# Print startup message
print("🤖 Chatbot started! Type 'exit' to quit.\n")

# Start infinite loop for continuous conversation
while True:
    # Get user input and clean it
    user_input = input("You: ").strip()

    # Exit condition
    if user_input.lower() in ["exit", "quit"]:
        print("Bot: Goodbye!")
        break

    # Skip empty input
    if not user_input:
        continue

    # Create a structured prompt to guide the model
    # This improves response quality (important for small models)
    # f""" ... """
    #  """ → multi-line string
    #  f → allows variables inside {}
    prompt = f"""
             You are a helpful assistant.
             Answer ONLY the question in 1 short sentence.
             Do not ask questions back.
             Do not change the topic.
             
             Question: {user_input}
             """

    try:
        # Send prompt to local LLM and get response
        response = llm.invoke(prompt)

        # Print cleaned response (strip removes extra spaces/newlines)
        print("Bot:", response.strip())

    except Exception as e:
        print("Bot: Error occurred:", str(e))
