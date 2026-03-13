/* tslint:disable:no-unused-variable */

import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestApiService } from './test-api.service';
import { testModel2 } from '../models/testmodel2';

describe('Service: TestApi', () => {
  let service: TestApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TestApiService]
    });
    service = TestBed.inject(TestApiService);
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

    let tests: testModel2[] = [];
    service.tests$.subscribe(t => tests = t);

    expect(tests.length).toBe(1);
    expect(tests[0].apiUrl).toBe('https://example.com');
  });

  it('should delete a test from the list', () => {
    service.addTestOnList({
      id: 0, method: 'GET', apiUrl: 'https://a.com',
      headers: {}, expectedHeaders: {},
    });
    service.addTestOnList({
      id: 0, method: 'POST', apiUrl: 'https://b.com',
      headers: {}, expectedHeaders: {},
    });

    service.deleteTest(1); // delete first test (id=1)

    let tests: testModel2[] = [];
    service.tests$.subscribe(t => tests = t);

    expect(tests.length).toBe(1);
  });

  it('should get a test by id', () => {
    service.addTestOnList({
      id: 0, method: 'PUT', apiUrl: 'https://c.com',
      headers: {}, expectedHeaders: {},
    });

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
    service.addTestOnList({
      id: 0, method: 'POST', apiUrl: 'https://b.com',
      headers: {}, expectedHeaders: {},
    });

    service.clearTests();

    let tests: testModel2[] = [];
    service.tests$.subscribe(t => tests = t);

    expect(tests.length).toBe(0);
  });
});
