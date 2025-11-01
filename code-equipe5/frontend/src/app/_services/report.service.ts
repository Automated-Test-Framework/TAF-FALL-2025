import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Report } from '../models/report';

@Injectable({ providedIn: 'root' })
export class ReportService {
  // adapte si tu as un fichier environment.*
  private base = 'http://localhost:8083/api/test';

  constructor(private http: HttpClient) {}

  getReport(): Observable<Report> {
    return this.http.get<Report>(`${this.base}/report`).pipe(
      map(rep => ({
        ...rep,
        tests: rep.tests.map(t => ({
          ...t,
          tool: (t.tool || 'unknown').toLowerCase(),
          feature: t.feature || 'General',
          scenario: t.scenario || 'N/A',
          status: (t.status || 'skipped').toLowerCase(),
          durationMs: t.durationMs ?? 0
        }))
      }))
    );
  }
}
