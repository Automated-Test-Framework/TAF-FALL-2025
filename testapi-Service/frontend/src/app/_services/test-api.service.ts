import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, forkJoin, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { testModel } from '../models/test-model';
import { testModel2 } from '../models/testmodel2';
import { TestResponseModel } from '../models/testResponseModel';

@Injectable({
  providedIn: 'root'
})
export class TestApiService {
  // === Base URL (goes through Angular proxy) ===
  private static readonly BASE_URL = '/api';

  // === Hardcoded dev Bearer token (temporary for local testing) ===
  private static readonly BEARER_TOKEN =
     'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqYW5lIiwiZXhwIjoxNzYxMTc5NzA1LCJpYXQiOjE3NjExNzYxMDV9.-X4RwkV1Rkc1Zt-Enys1qLJKubM11XtPA0n39WZSjGwmsAhyarV0eyduth30DR7rR3N0CbuMYTQUElcGTNCLDA'
  // === Standard JSON + Auth headers ===
  private static readonly JSON_HEADERS = new HttpHeaders({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${TestApiService.BEARER_TOKEN}`
  });

  // === Endpoint (no duplicated /api) ===
  private readonly checkApiUrl = `${TestApiService.BASE_URL}/testapi/checkApi`;

  constructor(private http: HttpClient) {}

  // === Execute all tests concurrently ===
  executeTests(dataTests: testModel2[]): Observable<TestResponseModel[]> {
    return forkJoin(
      dataTests.map(test => {
        const sanitizedTest = {
          ...test,
          headers: this.safeParseHeaders(test.headers)
        };

        console.log('Payload envoyé:', sanitizedTest);
        console.log('Headers HTTP envoyés:', TestApiService.JSON_HEADERS.keys());

        return this.http.post<TestResponseModel>(
          this.checkApiUrl,
          sanitizedTest,
          { headers: TestApiService.JSON_HEADERS }
        ).pipe(catchError(this.handleError));
      })
    );
  }

  // === Safe header parsing ===
  private safeParseHeaders(value: unknown): Record<string, string> {
    // Already an object
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      return value as Record<string, string>;
    }

    // Empty or null
    if (value == null || value === '') {
      return {};
    }

    // Try JSON string
    if (typeof value === 'string') {
      try {
        const parsed = JSON.parse(value);
        if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
          return parsed as Record<string, string>;
        }
      } catch {
        // ignore JSON parsing errors
      }

      // Try "Key: Value" pairs (newline or ';' separated)
      const obj: Record<string, string> = {};
      const lines = value.split(/\r?\n|;/).map(l => l.trim()).filter(Boolean);
      for (const line of lines) {
        const idx = line.indexOf(':');
        if (idx > -1) {
          const key = line.slice(0, idx).trim();
          const val = line.slice(idx + 1).trim();
          if (key) obj[key] = val;
        }
      }

      // If at least one header parsed, return it
      if (Object.keys(obj).length > 0) {
        return obj;
      }
    }

    // Fallback: preserve original
    return { raw: String(value) };
  }

  // === Error handling ===
  private handleError(error: HttpErrorResponse) {
    console.error('Erreur détectée:', error);
    console.error('Réponse du serveur:', error.error);
    return throwError(() =>
      new Error(error.error?.message || 'Une erreur est survenue lors de l\'exécution des tests.')
    );
  }

  // === Local list management ===
  private testsSubject: BehaviorSubject<testModel2[]> = new BehaviorSubject<testModel2[]>([]);
  tests$: Observable<testModel2[]> = this.testsSubject.asObservable();
  listTests: testModel2[] = [];

  addTestOnList(newTest: testModel2) {
    newTest.id = this.listTests.length + 1;
    this.listTests.push(newTest);
    this.testsSubject.next([...this.listTests]);
  }

  deleteTest(id: number) {
    const indiceASupprimer = id - 1;
    this.listTests.splice(indiceASupprimer, 1);
    this.testsSubject.next([...this.listTests]);
  }

  getTest(id: number) {
    return this.listTests.find(row => row.id === id);
  }

  updateTestsStatusExecution(listTestsResponses: TestResponseModel[]) {
    if (listTestsResponses.length !== this.listTests.length) {
      console.error('Le nombre de réponses ne correspond pas au nombre de tests.');
      return;
    }

    listTestsResponses.forEach((response, index) => {
      if (this.listTests[index]) {
        this.listTests[index].responseStatus = response.answer;
        this.listTests[index].messages = response.messages || [];
      } else {
        console.error(`Aucun test trouvé à l'index ${index}`);
      }
    });

    this.testsSubject.next([...this.listTests]);
  }
}
