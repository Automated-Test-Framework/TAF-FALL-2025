import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {BehaviorSubject,  Observable, Subject, forkJoin, merge, of, throwError} from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {testModel} from "../models/test-model";
import {testModel2} from "../models/testmodel2";
import {TestResponseModel} from "../models/testResponseModel";

@Injectable({
  providedIn: 'root'
})

export class TestApiService {
  REST_API: string = `${environment.apiUrl}/team2/api`;
  private DEFINITIONS_API: string = `${this.REST_API}/testapi/definitions`;
  constructor(private http: HttpClient) { }

  executeTests(dataTests: testModel2[]): Observable<TestResponseModel[]> {
    return forkJoin(
      dataTests.map(test => {
        const sanitizedTest = {
          ...test,
          headers: typeof test.headers === 'object' ? test.headers : JSON.parse(test.headers || '{}')
        };

        console.log('Payload envoyé:', sanitizedTest);

        return this.http.post<TestResponseModel>(
          `${this.REST_API}/testapi/checkApi`,
          sanitizedTest,
          { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
        ).pipe(
          catchError(this.handleError)
        );
      })
    );
  }

  /**
   * Execute tests progressively — emits each result as it arrives.
   * Returns Observable of {index, result} so the UI can update one row at a time.
   */
  executeTestsProgressive(dataTests: testModel2[]): Observable<{index: number, result: TestResponseModel}> {
    const requests = dataTests.map((test, index) => {
      const sanitizedTest = {
        ...test,
        headers: typeof test.headers === 'object' ? test.headers : JSON.parse(test.headers || '{}')
      };
      return this.http.post<TestResponseModel>(
        `${this.REST_API}/testapi/checkApi`,
        sanitizedTest,
        { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
      ).pipe(
        map(result => ({ index, result })),
        catchError(() => of({ index, result: { id: 0, stutsCode: -1, fieldAnswer: null, answer: false, statusCode: -1, output: '', messages: ['❌ Erreur réseau'], actualResponseTime: -1 } as TestResponseModel }))
      );
    });
    return merge(...requests);
  }



  private handleError(error: HttpErrorResponse) {
    console.error('Erreur détectée:', error);
    console.error('Réponse du serveur:', error.error);
    return throwError(() => new Error(error.error?.message || 'Une erreur est survenue lors de l\'exécution des tests.'));
  }



//to refresh automatically the tests's  list
  private testsSubject: BehaviorSubject<testModel2[]> = new BehaviorSubject<testModel2[]>([]);
  tests$ : Observable<testModel2[]> = this.testsSubject.asObservable();
  listTests : testModel2 []=[];

  // ── MongoDB persistence ─────────────────────────

  /** Load all saved test definitions from backend */
  loadDefinitions(): Observable<any[]> {
    return this.http.get<any[]>(this.DEFINITIONS_API).pipe(
      tap(defs => {
        this.listTests = defs.map((d, index) => this.fromBackend(d, index + 1));
        this.testsSubject.next([...this.listTests]);
      }),
      catchError(err => {
        console.error('Failed to load definitions from backend:', err);
        return throwError(() => err);
      })
    );
  }

  /** Convert backend ApiTestDefinition to frontend testModel2 */
  private fromBackend(d: any, localId: number): testModel2 {
    return {
      id: localId,
      mongoId: d.id,
      method: d.method || 'GET',
      apiUrl: d.apiUrl || '',
      headers: d.headers || {},
      expectedHeaders: d.expectedHeaders || {},
      statusCode: d.statusCode,
      responseTime: d.responseTime,
      input: d.input,
      expectedOutput: d.expectedOutput,
    };
  }

  /** Convert frontend testModel2 to backend payload */
  private toBackend(test: testModel2): any {
    return {
      method: test.method,
      apiUrl: test.apiUrl,
      headers: test.headers,
      expectedHeaders: test.expectedHeaders,
      input: test.input || '',
      expectedOutput: test.expectedOutput || '',
      statusCode: test.statusCode,
      responseTime: test.responseTime,
    };
  }

  //ajouter un test a la liste
  addTestOnList(newTest: testModel2){
    newTest.id= this.listTests.length+1;
    this.listTests.push(newTest);
    this.testsSubject.next([...this.listTests]);

    // Persist to backend
    const index = this.listTests.length - 1;
    this.http.post<any>(this.DEFINITIONS_API, this.toBackend(newTest)).subscribe({
      next: saved => {
        // Update by index in case the reference was replaced by updateTest
        if (this.listTests[index]) {
          this.listTests[index].mongoId = saved.id;
        }
      },
      error: err => console.error('Failed to save definition:', err)
    });
  }

  // Mettre a jour un test existant dans la liste
  updateTest(updatedTest: testModel2): void {
    const index = this.listTests.findIndex(t => t.id === updatedTest.id);
    if (index !== -1) {
      const mongoId = this.listTests[index].mongoId;
      updatedTest.mongoId = mongoId;
      // Reset result fields
      updatedTest.responseStatus = undefined;
      updatedTest.messages = [];
      this.listTests[index] = updatedTest;
      this.testsSubject.next([...this.listTests]);

      // Persist to backend
      if (mongoId) {
        this.http.put<any>(`${this.DEFINITIONS_API}/${mongoId}`, this.toBackend(updatedTest)).subscribe({
          error: err => console.error('Failed to update definition:', err)
        });
      }
    }
  }

// delete a test from the liste when user confirm the remove
  deleteTest(id: number){
    const index = this.listTests.findIndex(t => t.id === id);
    if (index === -1) return;
    const mongoId = this.listTests[index].mongoId;

    this.listTests.splice(index, 1);
    // Renumber IDs sequentially after deletion
    this.listTests.forEach((t, i) => t.id = i + 1);
    this.testsSubject.next([...this.listTests]);

    // Delete from backend
    if (mongoId) {
      this.http.delete(`${this.DEFINITIONS_API}/${mongoId}`).subscribe({
        error: err => console.error('Failed to delete definition:', err)
      });
    }
  }

  // get test information to show it to the user, so he can conform that he wants delete the right test on the list
  getTest(id: number) {
    const rowTest = this.listTests.find(row => row.id === id);
    return rowTest;
  }

  // Update the status of test executions using index
  updateTestsStatusExecution(listTestsResponses: TestResponseModel[]) {
    // Vérifie que le nombre de réponses correspond au nombre de tests
    if (listTestsResponses.length !== this.listTests.length) {
      console.error('Le nombre de réponses ne correspond pas au nombre de tests.');
      return;
    }

    // Parcours chaque réponse et met à jour le test correspondant
    listTestsResponses.forEach((response, index) => {
      if (this.listTests[index]) { // Vérifie si le test existe à cet index
        this.listTests[index].responseStatus = response.answer;
        this.listTests[index].messages = response.messages || []; // Mise à jour des erreurs
        this.listTests[index].actualResponseTime = response.actualResponseTime;
      } else {
        console.error(`Aucun test trouvé à l'index ${index}`);
      }
    });

    // Met à jour la liste des tests pour rafraîchir l'affichage
    this.testsSubject.next([...this.listTests]);
  }

  /** Update a single test result by index (for progressive display) */
  updateSingleTestResult(index: number, response: TestResponseModel): void {
    if (this.listTests[index]) {
      this.listTests[index].responseStatus = response.answer;
      this.listTests[index].messages = response.messages || [];
      this.listTests[index].actualResponseTime = response.actualResponseTime;
      this.listTests[index].pending = false;
      this.testsSubject.next([...this.listTests]);
    }
  }

  /** Clear result fields for all tests (set to pending) */
  clearTestResults(): void {
    this.listTests.forEach(t => {
      t.responseStatus = undefined;
      t.messages = [];
      t.actualResponseTime = undefined;
      t.pending = true;
    });
    this.testsSubject.next([...this.listTests]);
  }

  /** Clear all tests from the list */
  clearTests(): void {
    this.listTests = [];
    this.testsSubject.next([]);
  }

}
