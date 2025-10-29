export interface TestCaseResult {
  id: string; runId: string; name: string;
  statusCode: number; passed: boolean; error?: string|null; durationMs: number;
}
