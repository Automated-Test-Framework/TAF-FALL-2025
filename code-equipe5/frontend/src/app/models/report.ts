export interface Report {
  run: { runId: string; generatedAt: string; };
  stats: { total: number; passed: number; failed: number; skipped: number; durationMs: number; };
  tests: TestItem[];
}

export type TestStatus = 'passed' | 'failed' | 'skipped' | string;

export interface TestItem {
  id: number;
  tool: string;         // 'jmeter' | 'selenium' | 'gatling' | ...
  feature: string;      // 'General' si non fourni
  scenario: string;     // = nom du test
  status: TestStatus;
  durationMs: number;   // en ms (0 si inconnu)

  executedBy?: string;
  executedAt?: string;
}
