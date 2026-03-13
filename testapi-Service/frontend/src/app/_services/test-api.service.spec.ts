/* tslint:disable:no-unused-variable */

import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestApiService } from './test-api.service';
import { testModel2 } from '../models/testmodel2';

describe('Service: TestApi', () => {
  let service: TestApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TestApiService]
    });
    service = TestBed.inject(TestApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should add a test to the list', () => {
    const test: testModel2 = {
      id: 0, method: 'GET', apiUrl: 'https://example.com',
      headers: {}, expectedHeaders: {}, statusCode: 200,
    };
    service.addTestOnList(test);

    // Flush the POST request to backend
    const req = httpMock.expectOne(r => r.method === 'POST' && r.url.includes('/definitions'));
    req.flush({ id: 'mongo1' });

    let tests: testModel2[] = [];
    service.tests$.subscribe(t => tests = t);

    expect(tests.length).toBe(1);
    expect(tests[0].apiUrl).toBe('https://example.com');
    expect(tests[0].mongoId).toBe('mongo1');
  });

  it('should delete a test from the list', () => {
    service.addTestOnList({
      id: 0, method: 'GET', apiUrl: 'https://a.com',
      headers: {}, expectedHeaders: {},
    });
    httpMock.expectOne(r => r.method === 'POST').flush({ id: 'mA' });

    service.addTestOnList({
      id: 0, method: 'POST', apiUrl: 'https://b.com',
      headers: {}, expectedHeaders: {},
    });
    httpMock.expectOne(r => r.method === 'POST').flush({ id: 'mB' });

    service.deleteTest(1); // delete first test (id=1)

    // Flush DELETE request
    const delReq = httpMock.expectOne(r => r.method === 'DELETE');
    delReq.flush({});

    let tests: testModel2[] = [];
    service.tests$.subscribe(t => tests = t);

    expect(tests.length).toBe(1);
    expect(tests[0].apiUrl).toBe('https://b.com');
    expect(tests[0].id).toBe(1); // renumbered after deletion
  });

  it('should get a test by id', () => {
    service.addTestOnList({
      id: 0, method: 'PUT', apiUrl: 'https://c.com',
      headers: {}, expectedHeaders: {},
    });
    httpMock.expectOne(r => r.method === 'POST').flush({ id: 'm1' });

    const found = service.getTest(1);
    expect(found).toBeDefined();
    expect(found!.method).toBe('PUT');
  });

  it('should return undefined for non-existent test id', () => {
    const found = service.getTest(999);
    expect(found).toBeUndefined();
  });

  it('should clear all tests', () => {
    service.addTestOnList({
      id: 0, method: 'GET', apiUrl: 'https://a.com',
      headers: {}, expectedHeaders: {},
    });
    httpMock.expectOne(r => r.method === 'POST').flush({ id: 'm1' });

    service.addTestOnList({
      id: 0, method: 'POST', apiUrl: 'https://b.com',
      headers: {}, expectedHeaders: {},
    });
    httpMock.expectOne(r => r.method === 'POST').flush({ id: 'm2' });

    service.clearTests();

    let tests: testModel2[] = [];
    service.tests$.subscribe(t => tests = t);

    expect(tests.length).toBe(0);
  });

  it('should update an existing test by id', () => {
    service.addTestOnList({
      id: 0, method: 'GET', apiUrl: 'https://old.com',
      headers: {}, expectedHeaders: {}, statusCode: 200,
    });
    httpMock.expectOne(r => r.method === 'POST').flush({ id: 'mOld' });

    service.updateTest({
      id: 1, method: 'POST', apiUrl: 'https://new.com',
      headers: { 'Content-Type': 'application/json' }, expectedHeaders: {}, statusCode: 201,
    });

    // Flush the PUT request
    const putReq = httpMock.expectOne(r => r.method === 'PUT');
    putReq.flush({});

    let tests: testModel2[] = [];
    service.tests$.subscribe(t => tests = t);

    expect(tests.length).toBe(1);
    expect(tests[0].method).toBe('POST');
    expect(tests[0].apiUrl).toBe('https://new.com');
    expect(tests[0].statusCode).toBe(201);
    expect(tests[0].responseStatus).toBeUndefined();
  });

  it('should not modify list when updating non-existent test', () => {
    service.addTestOnList({
      id: 0, method: 'GET', apiUrl: 'https://a.com',
      headers: {}, expectedHeaders: {},
    });
    httpMock.expectOne(r => r.method === 'POST').flush({ id: 'm1' });

    service.updateTest({
      id: 999, method: 'DELETE', apiUrl: 'https://gone.com',
      headers: {}, expectedHeaders: {},
    });

    let tests: testModel2[] = [];
    service.tests$.subscribe(t => tests = t);

    expect(tests.length).toBe(1);
    expect(tests[0].method).toBe('GET');
  });

  it('should load definitions from backend', () => {
    service.loadDefinitions().subscribe();

    const req = httpMock.expectOne(r => r.method === 'GET' && r.url.includes('/definitions'));
    req.flush([
      { id: 'abc', method: 'GET', apiUrl: 'https://loaded.com', headers: {}, expectedHeaders: {}, statusCode: 200 },
      { id: 'def', method: 'POST', apiUrl: 'https://loaded2.com', headers: {}, expectedHeaders: {}, statusCode: 201 },
    ]);

    let tests: testModel2[] = [];
    service.tests$.subscribe(t => tests = t);

    expect(tests.length).toBe(2);
    expect(tests[0].mongoId).toBe('abc');
    expect(tests[0].apiUrl).toBe('https://loaded.com');
    expect(tests[1].mongoId).toBe('def');
  });
});
