# 5 Cas de tests sur : frontend\src\app\_services\

## Fichier #1: `frontend\src\app\_services\auth.service.spec.ts`
## Test de la méthode login :

1. Vérifier que la méthode `login` envoie une requête POST à l'URL correcte avec les bonnes données.
2. Vérifier que la méthode `login` retourne une Observable avec les données de réponse.

## Test de la méthode register :

1. Vérifier que la méthode `register` envoie une requête POST à l'URL correcte avec les bonnes données.
2. Vérifier que la méthode `register` retourne une Observable avec les données de réponse.

# Code pour `auth.service.spec.ts`
```ts
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './user.service';
import { environment } from '../../environments/environment';

/**
 * Suite de tests pour le service AuthService.
 */
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  /**
   * Configuration du module de test avant chaque test.
   * Cette méthode est exécutée une seule fois avant tous les tests.
   * Elle configure le module de test avec les déclarations et les fournisseurs nécessaires.
   */
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  /**
   * Nettoyage après chaque test.
   * Cette méthode est exécutée après chaque test individuel.
   * Elle s'assure qu'il n'y a pas de requêtes HTTP en attente.
   */
  afterEach(() => {
    httpMock.verify();
  });

  /**
   * Test pour vérifier que la méthode login envoie une requête POST à l'URL correcte avec les bonnes données.
   * Ce test simule un appel à la méthode login et vérifie que la requête HTTP est envoyée avec les bonnes données.
   */
  it('devrait envoyer une requête POST correcte lors de l\'appel de login', () => {
    const mockResponse = { token: 'fake-jwt-token' };
    const username = 'testuser';
    const password = 'testpassword';

    service.login(username, password).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/auth/signin`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username, password });
    req.flush(mockResponse);
  });

  /**
   * Test pour vérifier que la méthode register envoie une requête POST à l'URL correcte avec les bonnes données.
   * Ce test simule un appel à la méthode register et vérifie que la requête HTTP est envoyée avec les bonnes données.
   */
  it('devrait envoyer une requête POST correcte lors de l\'appel de register', () => {
    const mockResponse = { message: 'User registered successfully!' };
    const fullName = 'John Doe';
    const username = 'testuser';
    const email = 'testuser@example.ca';
    const password = 'testpassword';

    service.register(fullName, username, email, password).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/auth/signup`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ fullName, username, email, password });
    req.flush(mockResponse);
  });
});
```

# Explications

## Test de la méthode login :

- **Requête POST** : Vérifie que la méthode `login` envoie une requête POST à l'URL correcte (`${environment.apiUrl}/api/auth/signin`) avec les bonnes données (`{ username, password }`).
- **Observable** : Vérifie que la méthode `login` retourne une Observable avec les données de réponse (`{ token: 'fake-jwt-token' }`).

## Test de la méthode register :

- **Requête POST** : Vérifie que la méthode `register` envoie une requête POST à l'URL correcte (`${environment.apiUrl}/api/auth/signup`) avec les bonnes données (`{ fullName, username, email, password }`).
- **Observable** : Vérifie que la méthode `register` retourne une Observable avec les données de réponse (`{ message: 'User registered successfully!' }`).

# Configuration des tests :

- **HttpClientTestingModule** : Utilisé pour tester les requêtes HTTP sans effectuer de vraies requêtes réseau.
- **HttpTestingController** : Utilisé pour vérifier et contrôler les requêtes HTTP effectuées par le service.
- **Nettoyage après chaque test** : Vérifie qu'il n'y a pas de requêtes HTTP en attente après chaque test.

==========================================
## Fichier #2: `frontend\src\app\_services\performance-test-api.service.spec.ts`

# Plan de test

## Test de la méthode `sendGatlingRequest`
- Vérifier que la méthode `sendGatlingRequest` envoie une requête POST à l'URL correcte avec les bonnes données.
- Vérifier que la méthode `sendGatlingRequest` retourne une Observable avec les données de réponse.

## Test de la méthode `sendHttpJMeterRequest`
- Vérifier que la méthode `sendHttpJMeterRequest` envoie une requête POST à l'URL correcte avec les bonnes données.
- Vérifier que la méthode `sendHttpJMeterRequest` retourne une Observable avec les données de réponse.

## Test de la méthode `sendFtpJMeterRequest`
- Vérifier que la méthode `sendFtpJMeterRequest` envoie une requête POST à l'URL correcte avec les bonnes données.
- Vérifier que la méthode `sendFtpJMeterRequest` retourne une Observable avec les données de réponse.

```ts
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PerformanceTestApiService } from './performance-test-api.service';
import { environment } from '../../environments/environment';
import { GatlingRequest } from '../performance-test-api/gatling-api/gatling-request';
import { JMeterHttpRequest } from '../performance-test-api/jmeter-api/jmeter-http-request';
import { JMeterFTPRequest } from '../performance-test-api/jmeter-api/jmeter-ftp-request';

/**
 * Suite de tests pour le service PerformanceTestApiService.
 */
describe('PerformanceTestApiService', () => {
  let service: PerformanceTestApiService;
  let httpMock: HttpTestingController;

  /**
   * Configuration du module de test avant chaque test.
   * Cette méthode est exécutée une seule fois avant tous les tests.
   * Elle configure le module de test avec les déclarations et les fournisseurs nécessaires.
   */
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PerformanceTestApiService]
    });

    service = TestBed.inject(PerformanceTestApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  /**
   * Nettoyage après chaque test.
   * Cette méthode est exécutée après chaque test individuel.
   * Elle s'assure qu'il n'y a pas de requêtes HTTP en attente.
   */
  afterEach(() => {
    httpMock.verify();
  });

  /**
   * Test pour vérifier que la méthode sendGatlingRequest envoie une requête POST à l'URL correcte avec les bonnes données.
   * Ce test simule un appel à la méthode sendGatlingRequest et vérifie que la requête HTTP est envoyée avec les bonnes données.
   */
  it('devrait envoyer une requête POST correcte lors de l\'appel de sendGatlingRequest', () => {
    const mockResponse = { success: true };
    const request: GatlingRequest = {
      testBaseUrl: '',
      testScenarioName: '',
      testRequestName: '',
      testUri: '',
      testRequestBody: '',
      testMethodType: '',
      testUsersNumber: 0
    };

    service.sendGatlingRequest(request).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/performance/gatling/runSimulation`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  /**
   * Test pour vérifier que la méthode sendHttpJMeterRequest envoie une requête POST à l'URL correcte avec les bonnes données.
   * Ce test simule un appel à la méthode sendHttpJMeterRequest et vérifie que la requête HTTP est envoyée avec les bonnes données.
   */
  it('devrait envoyer une requête POST correcte lors de l\'appel de sendHttpJMeterRequest', () => {
    const mockResponse = { success: true };
    const request: JMeterHttpRequest = {
      nbThreads: '',
      rampTime: '',
      duration: '',
      domain: '',
      port: '',
      protocol: '',
      path: '',
      method: '',
      loop: '',
      data: ''
    };

    service.sendHttpJMeterRequest(request).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/performance/jmeter/http`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  /**
   * Test pour vérifier que la méthode sendFtpJMeterRequest envoie une requête POST à l'URL correcte avec les bonnes données.
   * Ce test simule un appel à la méthode sendFtpJMeterRequest et vérifie que la requête HTTP est envoyée avec les bonnes données.
   */
  it('devrait envoyer une requête POST correcte lors de l\'appel de sendFtpJMeterRequest', () => {
    const mockResponse = { success: true };
    const request: JMeterFTPRequest = {
      nbThreads: '',
      rampTime: '',
      duration: '',
      domain: '',
      port: '',
      method: '',
      remotefile: '',
      localfile: '',
      username: '',
      password: '',
      loop: ''
    };

    service.sendFtpJMeterRequest(request).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/performance/jmeter/ftp`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });
});
```


# Explications

## Test de la méthode `sendGatlingRequest`
- **Requête POST** : Vérifie que la méthode `sendGatlingRequest` envoie une requête POST à l'URL correcte (`${environment.apiUrl}/api/performance/gatling/runSimulation`) avec les bonnes données (`request`).
- **Observable** : Vérifie que la méthode `sendGatlingRequest` retourne une Observable avec les données de réponse (`{ success: true }`).

## Test de la méthode `sendHttpJMeterRequest`
- **Requête POST** : Vérifie que la méthode `sendHttpJMeterRequest` envoie une requête POST à l'URL correcte (`${environment.apiUrl}/api/performance/jmeter/http`) avec les bonnes données (`request`).
- **Observable** : Vérifie que la méthode `sendHttpJMeterRequest` retourne une Observable avec les données de réponse (`{ success: true }`).

## Test de la méthode `sendFtpJMeterRequest`
- **Requête POST** : Vérifie que la méthode `sendFtpJMeterRequest` envoie une requête POST à l'URL correcte (`${environment.apiUrl}/api/performance/jmeter/ftp`) avec les bonnes données (`request`).
- **Observable** : Vérifie que la méthode `sendFtpJMeterRequest` retourne une Observable avec les données de réponse (`{ success: true }`).

## Configuration des tests
- **HttpClientTestingModule** : Utilisé pour tester les requêtes HTTP sans effectuer de vraies requêtes réseau.
- **HttpTestingController** : Utilisé pour vérifier et contrôler les requêtes HTTP effectuées par le service.
- **Nettoyage après chaque test** : Vérifie qu'il n'y a pas de requêtes HTTP en attente après chaque test.

==========================================
## Fichier #3: `frontend\src\app\_services\test-api.service.spec.ts`


# Code pour `test-api.service.spec.ts`

# Plan de test

## Test de la méthode `executeTests`
- Vérifier que la méthode `executeTests` envoie une requête POST correcte pour chaque test dans `dataTests`.
- Vérifier que la méthode `executeTests` retourne la réponse attendue.

## Test de la méthode `addTestOnList`
- Vérifier que la méthode `addTestOnList` ajoute un nouveau test à la liste.
- Vérifier que le BehaviorSubject est mis à jour après l'ajout d'un test.

## Test de la méthode `deleteTest`
- Vérifier que la méthode `deleteTest` supprime le test correspondant à l'ID donné.
- Vérifier que le BehaviorSubject est mis à jour après la suppression d'un test.
- Vérifier que la méthode `deleteTest` ne fait rien pour un ID inexistant.

## Test de la méthode `getTest`
- Vérifier que la méthode `getTest` retourne le test correspondant à l'ID donné.
- Vérifier que la méthode `getTest` retourne `undefined` pour un ID inexistant.

## Test de la méthode `updateTestsStatusExecution`
- Vérifier que la méthode `updateTestsStatusExecution` met à jour le statut des tests en fonction des réponses.
- Vérifier que le BehaviorSubject est mis à jour après la mise à jour des statuts des tests.

```ts
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestApiService } from './test-api.service';
import { environment } from '../../environments/environment';
import { testModel2 } from '../models/testmodel2';
import { TestResponseModel } from '../models/testResponseModel';

/**
 * Suite de tests pour le service TestApiService.
 */
describe('TestApiService', () => {
  let service: TestApiService;
  let httpMock: HttpTestingController;

  /**
   * Configuration du module de test avant chaque test.
   * Cette méthode est exécutée une seule fois avant tous les tests.
   * Elle configure le module de test avec les déclarations et les fournisseurs nécessaires.
   */
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TestApiService]
    });

    service = TestBed.inject(TestApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  /**
   * Nettoyage après chaque test.
   * Cette méthode est exécutée après chaque test individuel.
   * Elle s'assure qu'il n'y a pas de requêtes HTTP en attente.
   */
  /**
   * Suite de tests pour le service TestApiService.
   */
  describe('TestApiService', () => {
    let service: TestApiService;
    let httpMock: HttpTestingController;

    /**
     * Configuration du module de test avant chaque test.
     * Cette méthode est exécutée une seule fois avant tous les tests.
     * Elle configure le module de test avec les déclarations et les fournisseurs nécessaires.
     */
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [TestApiService]
      });

      service = TestBed.inject(TestApiService);
      httpMock = TestBed.inject(HttpTestingController);
    });

    /**
     * Nettoyage après chaque test.
     * Cette méthode est exécutée après chaque test individuel.
     * Elle s'assure qu'il n'y a pas de requêtes HTTP en attente.
     */
  afterEach(() => {
    httpMock.verify();
  });

  /**
   * Test pour vérifier que la méthode executeTests envoie une requête POST pour chaque test dans dataTests.
   * Ce test simule un appel à la méthode executeTests et vérifie que les requêtes HTTP sont envoyées avec les bonnes données.
   */
  it('devrait envoyer une requête POST correcte pour chaque test lors de l\'appel de executeTests', () => {
    const mockResponse: TestResponseModel[] = [{
      answer: true,
      id: 0,
      stutsCode: 0,
      output: '',
      fieldAnswer: null,
      messages: []
    }];
      const dataTests: testModel2[] = [{ id: 1, method: 'GET', apiUrl: '/api/test', headers: {}, expectedHeaders: {} }];

    service.executeTests(dataTests).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/testapi/checkApi`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dataTests[0]);
    req.flush(mockResponse);
  });

  /**
   * Test pour vérifier que la méthode addTestOnList ajoute un nouveau test à la liste.
   * Ce test simule un appel à la méthode addTestOnList et vérifie que le test est ajouté à la liste et que le BehaviorSubject est mis à jour.
   */
    it('devrait ajouter un nouveau test à la liste lors de l\'appel de addTestOnList', () => {
      const newTest: testModel2 = { id: 0, method: 'POST', apiUrl: '/api/new', headers: {}, expectedHeaders: {} };

      service.addTestOnList(newTest);

      expect(service.listTests.length).toBe(1);
      expect(service.listTests[0].method).toBe('POST');
      service.tests$.subscribe(tests => {
        expect(tests.length).toBe(1);
        expect(tests[0].method).toBe('POST');
      });
    });

    /**
     * Test pour vérifier que la méthode deleteTest supprime le test correspondant à l'ID donné.
     * Ce test simule un appel à la méthode deleteTest et vérifie que le test est supprimé de la liste et que le BehaviorSubject est mis à jour.
     */
    it('devrait supprimer le test correspondant à l\'ID donné lors de l\'appel de deleteTest', () => {
      const testToDelete: testModel2 = { id: 1, method: 'DELETE', apiUrl: '/api/delete', headers: {}, expectedHeaders: {} };
      service.addTestOnList(testToDelete);

      service.deleteTest(1);

      expect(service.listTests.length).toBe(0);
      service.tests$.subscribe(tests => {
        expect(tests.length).toBe(0);
      });
    });

    /**
     * Test pour vérifier que la méthode getTest retourne le test correspondant à l'ID donné.
     * Ce test simule un appel à la méthode getTest et vérifie que le test correspondant est retourné.
     */
    it('devrait retourner le test correspondant à l\'ID donné lors de l\'appel de getTest', () => {
      const testToGet: testModel2 = { id: 1, method: 'GET', apiUrl: '/api/get', headers: {}, expectedHeaders: {} };
      service.addTestOnList(testToGet);

      const result = service.getTest(1);

      expect(result).toEqual(testToGet);
    });

    /**
     * Test pour vérifier que la méthode updateTestsStatusExecution met à jour le statut des tests en fonction des réponses.
     * Ce test simule un appel à la méthode updateTestsStatusExecution et vérifie que les statuts des tests sont mis à jour et que le BehaviorSubject est mis à jour.
     */
    it('devrait mettre à jour le statut des tests en fonction des réponses lors de l\'appel de updateTestsStatusExecution', () => {
      const testToUpdate: testModel2 = { id: 1, method: 'PUT', apiUrl: '/api/update', headers: {}, expectedHeaders: {}, responseStatus: false };
      service.addTestOnList(testToUpdate);

      const responses: TestResponseModel[] = [{ answer: true, id: 1, stutsCode: 200, output: '', fieldAnswer: null, messages: [] }];
      service.updateTestsStatusExecution(responses);

      expect(service.listTests[0].responseStatus).toBe(true);
      service.tests$.subscribe(tests => {
        expect(tests[0].responseStatus).toBe(true);
      });
    });

    /**
     * Test pour vérifier que la méthode getTest retourne undefined pour un ID inexistant.
     * Ce test simule un appel à la méthode getTest avec un ID qui n'existe pas et vérifie que la méthode retourne undefined.
     */
    it('devrait retourner undefined pour un ID inexistant lors de l\'appel de getTest', () => {
      const result = service.getTest(999);

      expect(result).toBeUndefined();
    });

    /**
     * Test pour vérifier que la méthode deleteTest ne fait rien pour un ID inexistant.
     * Ce test simule un appel à la méthode deleteTest avec un ID qui n'existe pas et vérifie que la liste des tests reste inchangée.
     */
    it('devrait ne rien faire pour un ID inexistant lors de l\'appel de deleteTest', () => {
      const initialTest: testModel2 = { id: 1, method: 'PATCH', apiUrl: '/api/patch', headers: {}, expectedHeaders: {} };
      service.addTestOnList(initialTest);

      service.deleteTest(999);

      expect(service.listTests.length).toBe(1);
      expect(service.listTests[0].id).toBe(1);
    });
  });
  it('devrait ajouter un nouveau test à la liste lors de l\'appel de addTestOnList', () => {
    const newTest: testModel2 = {
      id: 0, name: 'New Test',
      method: '',
      apiUrl: '',
      headers: {},
      expectedHeaders: {}
    };

    service.addTestOnList(newTest);

    expect(service.listTests.length).toBe(1);
    expect(service.listTests[0].name).toBe('New Test');
    service.tests$.subscribe(tests => {
      expect(tests.length).toBe(1);
      expect(tests[0].name).toBe('New Test');
    });
  });

  /**
   * Test pour vérifier que la méthode deleteTest supprime le test correspondant à l'ID donné.
   * Ce test simule un appel à la méthode deleteTest et vérifie que le test est supprimé de la liste et que le BehaviorSubject est mis à jour.
   */
  it('devrait supprimer le test correspondant à l\'ID donné lors de l\'appel de deleteTest', () => {
    const testToDelete: testModel2 = {
      id: 1, name: 'Test to Delete',
      method: '',
      apiUrl: '',
      headers: {},
      expectedHeaders: {}
    };
    service.addTestOnList(testToDelete);

    service.deleteTest(1);

    expect(service.listTests.length).toBe(0);
    service.tests$.subscribe(tests => {
      expect(tests.length).toBe(0);
    });
  });

  /**
   * Test pour vérifier que la méthode getTest retourne le test correspondant à l'ID donné.
   * Ce test simule un appel à la méthode getTest et vérifie que le test correspondant est retourné.
   */
  it('devrait retourner le test correspondant à l\'ID donné lors de l\'appel de getTest', () => {
    const testToGet: testModel2 = {
      id: 1, name: 'Test to Get',
      method: '',
      apiUrl: '',
      headers: {},
      expectedHeaders: {}
    };
    service.addTestOnList(testToGet);

    const result = service.getTest(1);

    expect(result).toEqual(testToGet);
  });

  /**
   * Test pour vérifier que la méthode updateTestsStatusExecution met à jour le statut des tests en fonction des réponses.
   * Ce test simule un appel à la méthode updateTestsStatusExecution et vérifie que les statuts des tests sont mis à jour et que le BehaviorSubject est mis à jour.
   */
  it('devrait mettre à jour le statut des tests en fonction des réponses lors de l\'appel de updateTestsStatusExecution', () => {
    const testToUpdate: testModel2 = {
      id: 1, name: 'Test to Update', responseStatus: false,
      method: '',
      apiUrl: '',
      headers: {},
      expectedHeaders: {}
    };
    service.addTestOnList(testToUpdate);

    const responses: TestResponseModel[] = [{
      answer: true,
      id: 0,
      stutsCode: 0,
      output: '',
      fieldAnswer: null,
      messages: []
    }];
    service.updateTestsStatusExecution(responses);

    expect(service.listTests[0].responseStatus).toBe(true);
    service.tests$.subscribe(tests => {
      return expect(tests[0].responseStatus).toBe(true);
    });
  });
});

```

# Explications

## Importations
Le code commence par importer les modules et services nécessaires pour les tests :
- `TestBed` de `@angular/core/testing` pour configurer et créer le composant à tester.
- `HttpClientTestingModule` et `HttpTestingController` de `@angular/common/http/testing` pour tester les requêtes HTTP.
- `TestApiService` qui est le service à tester.
- `environment` pour accéder à l'URL de l'API.
- `testModel2` et `TestResponseModel` pour les modèles de données utilisés dans les tests.

## Suite de tests pour le service `TestApiService`
La suite de tests est définie à l'aide de `describe`, qui regroupe plusieurs tests pour le service `TestApiService`.

### Variables globales
- `service`: Instance du service à tester.
- `httpMock`: Instance de `HttpTestingController` pour vérifier et contrôler les requêtes HTTP.

### Configuration du module de test
La `beforeEach` configure le module de test :
- Importe `HttpClientTestingModule` pour tester les requêtes HTTP.
- Fournit le service `TestApiService`.
- Injecte le service et `HttpTestingController` dans les variables `service` et `httpMock`.

### Vérification des requêtes HTTP
La `afterEach` vérifie qu'il n'y a pas de requêtes HTTP en attente après chaque test en appelant `httpMock.verify()`.

### Test de la méthode `executeTests`
Le test `it('devrait envoyer une requête POST correcte pour chaque test lors de l'appel de executeTests')` vérifie que la méthode `executeTests` envoie une requête POST correcte pour chaque test dans `dataTests` et retourne la réponse attendue :
- Définit une réponse simulée `mockResponse`.
- Définit des données de test `dataTests`.
- Appelle la méthode `executeTests` du service.
- Vérifie que la réponse est égale à la réponse simulée.
- Vérifie que la requête HTTP est envoyée à l'URL correcte avec la méthode POST.
- Envoie la réponse simulée avec `req.flush(mockResponse)`.

### Test de la méthode `addTestOnList`
Le test `it('devrait ajouter un nouveau test à la liste lors de l'appel de addTestOnList')` vérifie que la méthode `addTestOnList` ajoute un nouveau test à la liste et que le BehaviorSubject est mis à jour :
- Définit un nouveau test `newTest`.
- Appelle la méthode `addTestOnList` du service.
- Vérifie que le test est ajouté à la liste.
- Vérifie que le BehaviorSubject est mis à jour.

### Test de la méthode `deleteTest`
Le test `it('devrait supprimer le test correspondant à l'ID donné lors de l'appel de deleteTest')` vérifie que la méthode `deleteTest` supprime le test correspondant à l'ID donné et que le BehaviorSubject est mis à jour :
- Définit un test à supprimer `testToDelete`.
- Appelle la méthode `deleteTest` du service.
- Vérifie que le test est supprimé de la liste.
- Vérifie que le BehaviorSubject est mis à jour.

Le test `it('devrait ne rien faire pour un ID inexistant lors de l'appel de deleteTest')` vérifie que la méthode `deleteTest` ne fait rien pour un ID inexistant :
- Définit un test initial `initialTest`.
- Appelle la méthode `deleteTest` avec un ID inexistant.
- Vérifie que la liste des tests reste inchangée.

### Test de la méthode `getTest`
Le test `it('devrait retourner le test correspondant à l'ID donné lors de l'appel de getTest')` vérifie que la méthode `getTest` retourne le test correspondant à l'ID donné :
- Définit un test à récupérer `testToGet`.
- Appelle la méthode `getTest` du service.
- Vérifie que le test retourné est égal au test défini.

Le test `it('devrait retourner undefined pour un ID inexistant lors de l'appel de getTest')` vérifie que la méthode `getTest` retourne `undefined` pour un ID inexistant :
- Appelle la méthode `getTest` avec un ID inexistant.
- Vérifie que la méthode retourne `undefined`.

### Test de la méthode `updateTestsStatusExecution`
Le test `it('devrait mettre à jour le statut des tests en fonction des réponses lors de l'appel de updateTestsStatusExecution')` vérifie que la méthode `updateTestsStatusExecution` met à jour le statut des tests en fonction des réponses et que le BehaviorSubject est mis à jour :
- Définit un test à mettre à jour `testToUpdate`.
- Définit des réponses simulées `responses`.
- Appelle la méthode `updateTestsStatusExecution` du service.
- Vérifie que le statut du test est mis à jour.
- Vérifie que le BehaviorSubject est mis à jour.

==========================================
## Fichier #4: `frontend\src\app\_services\token-storage.service.spec.ts`
# Plan de test

## Test de la méthode `signOut`
- Vérifier que la méthode `signOut` vide le sessionStorage.

## Test de la méthode `saveToken`
- Vérifier que la méthode `saveToken` enregistre le token dans le sessionStorage.
- Vérifier que la méthode `saveToken` remplace un token existant dans le sessionStorage.

## Test de la méthode `getToken`
- Vérifier que la méthode `getToken` retourne le token enregistré dans le sessionStorage.
- Vérifier que la méthode `getToken` retourne null si aucun token n'est enregistré.

## Test de la méthode `saveUser`
- Vérifier que la méthode `saveUser` enregistre l'utilisateur dans le sessionStorage.
- Vérifier que la méthode `saveUser` remplace un utilisateur existant dans le sessionStorage.

## Test de la méthode `getUser`
- Vérifier que la méthode `getUser` retourne l'utilisateur enregistré dans le sessionStorage.
- Vérifier que la méthode `getUser` retourne un objet vide si aucun utilisateur n'est enregistré.

# Code `token-storage.service.spec.ts`
```ts
import { TestBed } from '@angular/core/testing';
import { TokenStorageService } from './token-storage.service';

/**
 * Suite de tests pour le service TokenStorageService.
 */
describe('TokenStorageService', () => {
  let service: TokenStorageService;

  /**
   * Configuration du module de test avant chaque test.
   * Cette méthode est exécutée une seule fois avant tous les tests.
   * Elle configure le module de test avec les déclarations et les fournisseurs nécessaires.
   */
  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenStorageService);
  });

  /**
   * Nettoyage après chaque test.
   * Cette méthode est exécutée après chaque test individuel.
   * Elle s'assure que le `sessionStorage` est vidé.
   */
  afterEach(() => {
    window.sessionStorage.clear();
  });

  /**
   * Test pour vérifier que la méthode signOut vide le sessionStorage.
   * Ce test simule un appel à la méthode signOut et vérifie que le sessionStorage est vidé.
   */
  it('devrait vider le sessionStorage lors de l\'appel de signOut', () => {
    window.sessionStorage.setItem('test-key', 'test-value');
    service.signOut();
    expect(window.sessionStorage.getItem('test-key')).toBeNull();
  });

  /**
   * Test pour vérifier que la méthode saveToken enregistre le token dans le sessionStorage.
   * Ce test simule un appel à la méthode saveToken et vérifie que le token est enregistré dans le sessionStorage.
   */
  it('devrait enregistrer le token dans le sessionStorage lors de l\'appel de saveToken', () => {
    const token = 'test-token';
    service.saveToken(token);
    expect(window.sessionStorage.getItem('user-token')).toBe(token);
  });

  /**
   * Test pour vérifier que la méthode saveToken remplace un token existant dans le sessionStorage.
   * Ce test simule un appel à la méthode saveToken avec un token existant et vérifie que le token est remplacé dans le sessionStorage.
   */
  it('devrait remplacer un token existant dans le sessionStorage lors de l\'appel de saveToken', () => {
    const oldToken = 'old-token';
    const newToken = 'new-token';
    service.saveToken(oldToken);
    service.saveToken(newToken);
    expect(window.sessionStorage.getItem('user-token')).toBe(newToken);
  });

  /**
   * Test pour vérifier que la méthode getToken retourne le token enregistré dans le sessionStorage.
   * Ce test simule un appel à la méthode getToken et vérifie que le token enregistré est retourné.
   */
  it('devrait retourner le token enregistré dans le sessionStorage lors de l\'appel de getToken', () => {
    const token = 'test-token';
    window.sessionStorage.setItem('user-token', token);
    expect(service.getToken()).toBe(token);
  });

  /**
   * Test pour vérifier que la méthode getToken retourne null si aucun token n'est enregistré.
   * Ce test simule un appel à la méthode getToken sans token enregistré et vérifie que null est retourné.
   */
  it('devrait retourner null si aucun token n\'est enregistré lors de l\'appel de getToken', () => {
    expect(service.getToken()).toBeNull();
  });

  /**
   * Test pour vérifier que la méthode saveUser enregistre l'utilisateur dans le sessionStorage.
   * Ce test simule un appel à la méthode saveUser et vérifie que l'utilisateur est enregistré dans le sessionStorage.
   */
  it('devrait enregistrer l\'utilisateur dans le sessionStorage lors de l\'appel de saveUser', () => {
    const user = { username: 'test-user' };
    service.saveUser(user);
    expect(window.sessionStorage.getItem('user-user')).toBe(JSON.stringify(user));
  });

  /**
   * Test pour vérifier que la méthode saveUser remplace un utilisateur existant dans le sessionStorage.
   * Ce test simule un appel à la méthode saveUser avec un utilisateur existant et vérifie que l'utilisateur est remplacé dans le sessionStorage.
   */
  it('devrait remplacer un utilisateur existant dans le sessionStorage lors de l\'appel de saveUser', () => {
    const oldUser = { username: 'old-user' };
    const newUser = { username: 'new-user' };
    service.saveUser(oldUser);
    service.saveUser(newUser);
    expect(window.sessionStorage.getItem('user-user')).toBe(JSON.stringify(newUser));
  });

  /**
   * Test pour vérifier que la méthode getUser retourne l'utilisateur enregistré dans le sessionStorage.
   * Ce test simule un appel à la méthode getUser et vérifie que l'utilisateur enregistré est retourné.
   */
  it('devrait retourner l\'utilisateur enregistré dans le sessionStorage lors de l\'appel de getUser', () => {
    const user = { username: 'test-user' };
    window.sessionStorage.setItem('user-user', JSON.stringify(user));
    expect(service.getUser()).toEqual(user);
  });

  /**
   * Test pour vérifier que la méthode getUser retourne un objet vide si aucun utilisateur n'est enregistré.
   * Ce test simule un appel à la méthode getUser sans utilisateur enregistré et vérifie qu'un objet vide est retourné.
   */
  it('devrait retourner un objet vide si aucun utilisateur n\'est enregistré lors de l\'appel de getUser', () => {
    expect(service.getUser()).toEqual({});
  });
});

```

# Explications

## Test de la méthode `signOut`
- **Vider le sessionStorage** : Vérifie que la méthode `signOut` vide le sessionStorage en appelant `window.sessionStorage.clear()`.

## Test de la méthode `saveToken`
- **Enregistrer le token** : Vérifie que la méthode `saveToken` enregistre le token dans le sessionStorage en appelant `window.sessionStorage.setItem(TOKEN_KEY, token)`.
- **Remplacer le token existant** : Vérifie que la méthode `saveToken` remplace un token existant dans le sessionStorage.

## Test de la méthode `getToken`
- **Retourner le token enregistré** : Vérifie que la méthode `getToken` retourne le token enregistré dans le sessionStorage en appelant `window.sessionStorage.getItem(TOKEN_KEY)`.
- **Retourner null si aucun token n'est enregistré** : Vérifie que la méthode `getToken` retourne null si aucun token n'est enregistré.

## Test de la méthode `saveUser`
- **Enregistrer l'utilisateur** : Vérifie que la méthode `saveUser` enregistre l'utilisateur dans le sessionStorage en appelant `window.sessionStorage.setItem(USER_KEY, JSON.stringify(user))`.
- **Remplacer l'utilisateur existant** : Vérifie que la méthode `saveUser` remplace un utilisateur existant dans le sessionStorage.

## Test de la méthode `getUser`
- **Retourner l'utilisateur enregistré** : Vérifie que la méthode `getUser` retourne l'utilisateur enregistré dans le sessionStorage en appelant `window.sessionStorage.getItem(USER_KEY)` et en le parsant avec `JSON.parse`.
- **Retourner un objet vide si aucun utilisateur n'est enregistré** : Vérifie que la méthode `getUser` retourne un objet vide si aucun utilisateur n'est enregistré.
==========================================
## Fichier #5: `frontend\src\app\_services\user.service.spec.ts`

# Plan de test

## Test de la méthode `getPublicContent`
- Vérifier que la méthode `getPublicContent` envoie une requête GET à l'URL correcte.
- Vérifier que la méthode `getPublicContent` retourne la réponse attendue.

## Test de la méthode `getUserBoard`
- Vérifier que la méthode `getUserBoard` envoie une requête GET à l'URL correcte.
- Vérifier que la méthode `getUserBoard` retourne la réponse attendue.

## Test de la méthode `getAdminBoard`
- Vérifier que la méthode `getAdminBoard` envoie une requête GET à l'URL correcte.
- Vérifier que la méthode `getAdminBoard` retourne la réponse attendue.

# Code pour `user.service.spec.ts`
```ts
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from './user.service';
import { environment } from '../../environments/environment';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  /**
   * Test pour vérifier que la méthode getPublicContent envoie une requête GET à l'URL correcte.
   * Ce test simule un appel à la méthode getPublicContent et vérifie que la requête HTTP est envoyée avec les bonnes données.
   */
  it('devrait envoyer une requête GET correcte pour getPublicContent', () => {
    const mockResponse = 'Contenu public';

    service.getPublicContent().subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/test/all`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  /**
   * Test pour vérifier que la méthode getUserBoard envoie une requête GET à l'URL correcte.
   * Ce test simule un appel à la méthode getUserBoard et vérifie que la requête HTTP est envoyée avec les bonnes données.
   */
  it('devrait envoyer une requête GET correcte pour getUserBoard', () => {
    const mockResponse = 'Contenu utilisateur';

    service.getUserBoard().subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/test/user`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  /**
   * Test pour vérifier que la méthode getAdminBoard envoie une requête GET à l'URL correcte.
   * Ce test simule un appel à la méthode getAdminBoard et vérifie que la requête HTTP est envoyée avec les bonnes données.
   */
  it('devrait envoyer une requête GET correcte pour getAdminBoard', () => {
    const mockResponse = 'Contenu administrateur';

    service.getAdminBoard().subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/test/admin`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });
});

```

# Explications

## Importations
Le code commence par importer les modules et services nécessaires pour les tests :
- `TestBed` de `@angular/core/testing` pour configurer et créer le composant à tester.
- `HttpClientTestingModule` et `HttpTestingController` de `@angular/common/http/testing` pour tester les requêtes HTTP.
- `UserService` qui est le service à tester.
- `environment` pour accéder à l'URL de l'API.

## Suite de tests pour le service `UserService`
La suite de tests est définie à l'aide de `describe`, qui regroupe plusieurs tests pour le service `UserService`.

### Variables globales
- `service`: Instance du service à tester.
- `httpMock`: Instance de `HttpTestingController` pour vérifier et contrôler les requêtes HTTP.

### Configuration du module de test
La `beforeEach` configure le module de test :
- Importe `HttpClientTestingModule` pour tester les requêtes HTTP.
- Fournit le service `UserService`.
- Injecte le service et `HttpTestingController` dans les variables `service` et `httpMock`.

### Vérification des requêtes HTTP
La `afterEach` vérifie qu'il n'y a pas de requêtes HTTP en attente après chaque test en appelant `httpMock.verify()`.

### Test de la méthode `getPublicContent`
Le test `it('devrait envoyer une requête GET correcte pour getPublicContent')` vérifie que la méthode `getPublicContent` envoie une requête GET à l'URL correcte et retourne la réponse attendue :
- Définit une réponse simulée `mockResponse`.
- Appelle la méthode `getPublicContent` du service.
- Vérifie que la réponse est égale à la réponse simulée.
- Vérifie que la requête HTTP est envoyée à l'URL correcte avec la méthode GET.
- Envoie la réponse simulée avec `req.flush(mockResponse)`.

### Test de la méthode `getUserBoard`
Le test `it('devrait envoyer une requête GET correcte pour getUserBoard')` vérifie que la méthode `getUserBoard` envoie une requête GET à l'URL correcte et retourne la réponse attendue :
- Définit une réponse simulée `mockResponse`.
- Appelle la méthode `getUserBoard` du service.
- Vérifie que la réponse est égale à la réponse simulée.
- Vérifie que la requête HTTP est envoyée à l'URL correcte avec la méthode GET.
- Envoie la réponse simulée avec `req.flush(mockResponse)`.

### Test de la méthode `getAdminBoard`
Le test `it('devrait envoyer une requête GET correcte pour getAdminBoard')` vérifie que la méthode `getAdminBoard` envoie une requête GET à l'URL correcte et retourne la réponse attendue :
- Définit une réponse simulée `mockResponse`.
- Appelle la méthode `getAdminBoard` du service.
- Vérifie que la réponse est égale à la réponse simulée.
- Vérifie que la requête HTTP est envoyée à l'URL correcte avec la méthode GET.
- Envoie la réponse simulée avec `req.flush(mockResponse)`.
