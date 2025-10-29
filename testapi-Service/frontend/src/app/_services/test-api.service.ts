import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {BehaviorSubject,  Observable, Subject, forkJoin, throwError} from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {testModel} from "../models/test-model";
import {testModel2} from "../models/testmodel2";
import {TestResponseModel} from "../models/testResponseModel";
import {CreateSuiteReq} from "../models/CreateSuiteReq";
import {CreateSuiteRes} from "../models/CreateSuiteRes";
import {CreateRunReq} from "../models/CreateRunReq";
import {CreateRunRes} from "../models/CreateRunRes";
import {TestRun} from "../models/TestRun";
import {TestCaseResult} from "../models/TestCaseResult";

@Injectable({
  providedIn: 'root'
})

export class TestApiService {
  REST_API: string = `${environment.apiUrl}/team2/api`;
  constructor(private http: HttpClient) { }




  createSuite(body: CreateSuiteReq): Observable<CreateSuiteRes> {
    return this.http.post<CreateSuiteRes>(`${this.REST_API}/testapi/suites`, body);
  }

  getAllSuite(): Observable<CreateSuiteReq[]> {
    return this.http.get<CreateSuiteReq[]>(`${this.REST_API}/testapi/suites`);
  }

  createRun(body: CreateRunReq): Observable<CreateRunRes> {
    return this.http.post<CreateRunRes>(`${this.REST_API}/testapi/runs`, body);
  }

  getRun(runId: string): Observable<TestRun> {
    return this.http.get<TestRun>(`${this.REST_API}/testapi/runs?runId=${runId}`);
  }

  getCases(runId: string): Observable<TestCaseResult[]> {
    return this.http.get<TestCaseResult[]>(`${this.REST_API}/testapi/runs/cases?runId=${runId}`);
  }

  /** Le backend renvoie du HTML (string). */
  getReportHtml(runId: string): Observable<string> {
    const headers = new HttpHeaders({ Accept: 'text/html' });
    return this.http.get(`${this.REST_API}/testapi/runs/report?runId=${runId}`, { headers, responseType: 'text' });
  }





  executeTests(dataTests: testModel2[]): Observable<TestResponseModel[]> {
    return forkJoin(
      dataTests.map(test => {
        const sanitizedTest = {
          ...test,
          headers: typeof test.headers === 'object' ? test.headers : JSON.parse(test.headers || '{}')
        };

        console.log('Payload envoyé:', sanitizedTest); // Vérifie ce qui est envoyé

        return this.http.post<TestResponseModel>(
          `${this.REST_API}/microservice/testapi/checkApi`,
          sanitizedTest,
          { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
        ).pipe(
          catchError(this.handleError)
        );
      })
    );
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

  //ajouter un test a la liste
  addTestOnList(newTest: testModel2){
    newTest.id= this.listTests.length+1;
    this.listTests.push(newTest);
    this.testsSubject.next([...this.listTests]);

  }

// delete a test from the liste when user confirm the remove
  deleteTest(id: number){
    let indiceASupprimer = id-1;
    this.listTests.splice(indiceASupprimer, 1);
    this.testsSubject.next([...this.listTests]);

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
      } else {
        console.error(`Aucun test trouvé à l'index ${index}`);
      }
    });

    // Met à jour la liste des tests pour rafraîchir l'affichage
    this.testsSubject.next([...this.listTests]);
  }


}
