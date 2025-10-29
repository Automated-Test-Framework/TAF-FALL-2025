export interface TestRun {
  id: string; suiteId: string; suiteName: string;
  status: 'QUEUED'|'RUNNING'|'PASSED'|'FAILED';
  found: number; passed: number; failed: number; skipped: number;
  createdAt: string; startedAt?: string; endedAt?: string;
  reportIndexPath?: string; logs?: string[];
}
