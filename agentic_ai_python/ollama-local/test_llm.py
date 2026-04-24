# ============================================================
# Local LLM Setup using Ollama + LangChain (NO API COST)
# ============================================================

# ----------------------------
# Install Ollama:
# ----------------------------
# 1. Download OllamaSetup.exe and install
# 2. Run models (optional test):
#       ollama run tinyllama
#       ollama run tinyllama:1.1b
#       ollama run phi3:mini
#       ollama run phi3

# ----------------------------
# Install dependencies:
# ----------------------------
# pip install langchain
# pip install -U langchain-ollama

# ----------------------------
# Start Ollama server:
# ----------------------------
# Run in terminal (NOT in Python):
#   ollama serve
#
# This starts server at:
#   http://localhost:11434

# NOTE:
# Use ONE approach:
# ✔ ollama serve  (recommended for apps)
# ✔ ollama run <model> (manual testing)
# ❌ Don't run both together unnecessarily

# ----------------------------
# Model options:
# ----------------------------
# tinyllama           → best for learning (lightweight ✅)
# tinyllama:1.1b      → ultra light (lowest usage ✅)
# phi3:mini           → better quality (medium ⚠️)
# phi3                → high quality (heavy ❌)

# ============================================================
# Python Code Starts Here
# ============================================================

from langchain_ollama import OllamaLLM

# Initialize lightweight local model
llm = OllamaLLM(model="tinyllama")

# Send prompt to LLM
response = llm.invoke("Say a one line joke")

# Print response safely
if response:
    print("Response:", response.strip())
else:
    print("No response received")