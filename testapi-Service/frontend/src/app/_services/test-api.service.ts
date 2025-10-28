import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {BehaviorSubject,  Observable, Subject, forkJoin, throwError} from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {testModel} from "../models/test-model";
import {testModel2} from "../models/testmodel2";
import {TestResponseModel} from "../models/testResponseModel";

@Injectable({
  providedIn: 'root'
})

export class TestApiService {
  REST_API: string = `${environment.apiUrl}/team2/api`;
  constructor(private http: HttpClient) { }

  executeTests(dataTests: testModel2[]): Observable<TestResponseModel[]> {
    return forkJoin(
      dataTests.map(test => {
        const sanitizedTest = {
          ...test,
          headers: typeof test.headers === 'object' ? test.headers : JSON.parse(test.headers || '{}')
        };

        console.log('Payload envoyé:', sanitizedTest); // Vérifie ce qui est envoyé

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
