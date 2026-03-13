client<llm> TestGenerator {
  provider openai
  options {
    model "gpt-4o-mini"
    api_key env.OPENAI_API_KEY
    temperature 0.1
  }
}