// Types utilisés par le board admin

export interface RunCard {
  projectKey?: string | null;
  runId?: string | null;
  status?: 'passed' | 'failed' | string | null;
  total?: number;
  passed?: number;
  failed?: number;
  createdAt?: string | null; // ISO
}

export interface PassratePoint {
  day: string;   // 'YYYY-MM-DD'
  total: number;
  passed: number;
}

export interface CaseItem {
  runId?: string;
  suite?: string;
  type?: string;   // api | ui | performance | ...
  tool?: string;   // restAssured | selenium | gatling | ...
  name?: string;
  status?: 'passed' | 'failed' | string;
  executedAt?: string; // ISO
}

export interface CaseSearchResponse {
  page: number;
  size: number;
  total: number;
  items: CaseItem[];
}

export interface RunDetail {
  runId?: string;
  projectKey?: string;
  status?: string;
  createdAt?: string | null;
  stats?: {
    total?: number;
    passed?: number;
    failed?: number;
    durationMs?: number;
  };
  cases?: CaseItem[];
}

/** % de succès par outil sur une fenêtre (days) */
export interface ToolRate {
  tool: string;          // 'gatling' | 'selenium' | 'restAssured' | 'other'
  total: number;
  passed: number;
  failed: number;
}

/** Statistiques empilées par type (api/ui/performance/...) */
export interface TypeStat {
  type: string;          // 'api' | 'ui' | 'performance' | ...
  total: number;
  passed: number;
  failed: number;
}

/** Un nom + un compteur (top fails, flaky, etc.) */
export interface NamedCount {
  name: string;
  count: number;
  tool?: string;
}



export interface TypeRate {
  type: string | null;
  passed: number;
  total: number;
}
