class JMeterHttpPlan {
  protocol string @description("HTTP or HTTPS")
  method string @description("GET, POST, PUT, DELETE, PATCH")
  domain string @description("Target API host/domain")
  path string @description("Path beginning with /")
  port string @description("Optional port")
  nbThreads string @description("Number of users/threads")
  rampTime string @description("Ramp-up duration in seconds")
  duration string @description("Test duration in seconds")
  loop string @description("Loop count")
  data string @description("Optional JSON body as string")
}

function GenerateJMeterPlan(prompt: string) -> JMeterHttpPlan {
  client TestGenerator
  prompt #"
You are generating a JMeter HTTP test plan from a user prompt.

Rules:
- Return ONLY the structured object.
- protocol must be HTTP or HTTPS.
- method must be one of GET, POST, PUT, DELETE, PATCH.
- path must start with '/'.
- nbThreads, rampTime, duration, loop must be numeric strings.
- if domain is missing, use "api.example.com".
- if path is missing, use "/".
- if method implies a body and JSON body exists in the prompt, set `data`.
- if no body, set `data` to empty string.

User prompt:
{{ prompt }}
"#
}