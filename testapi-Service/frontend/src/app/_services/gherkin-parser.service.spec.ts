import { TestBed } from '@angular/core/testing';
import { GherkinParserService } from './gherkin-parser.service';
import { TestModel2 } from '../models/testmodel2';

describe('GherkinParserService', () => {
  let service: GherkinParserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GherkinParserService]
    });
    service = TestBed.inject(GherkinParserService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ── parse() ───────────────────────────────────────────────

  describe('parse()', () => {

    it('should parse a simple GET scenario', () => {
      const gherkin = `Feature: Test
  Scenario: Simple GET
    Given the API method is "GET"
    And the API URL is "https://example.com/api"
    And the expected status code is 200
    When I execute the test
    Then the test should pass`;

      const result = service.parse(gherkin);

      expect(result.errors.length).toBe(0);
      expect(result.tests.length).toBe(1);
      expect(result.tests[0].method).toBe('GET');
      expect(result.tests[0].apiUrl).toBe('https://example.com/api');
      expect(result.tests[0].statusCode).toBe(200);
    });

    it('should parse multiple scenarios', () => {
      const gherkin = `Feature: Multi
  Scenario: First
    Given the API method is "GET"
    And the API URL is "https://example.com/1"

  Scenario: Second
    Given the API method is "POST"
    And the API URL is "https://example.com/2"`;

      const result = service.parse(gherkin);

      expect(result.errors.length).toBe(0);
      expect(result.tests.length).toBe(2);
      expect(result.tests[0].method).toBe('GET');
      expect(result.tests[1].method).toBe('POST');
    });

    it('should parse headers', () => {
      const gherkin = `Feature: Headers
  Scenario: With headers
    Given the API method is "GET"
    And the API URL is "https://example.com"
    And the header "Accept" is "application/json"
    And the header "X-Custom" is "value123"`;

      const result = service.parse(gherkin);

      expect(result.tests[0].headers['Accept']).toBe('application/json');
      expect(result.tests[0].headers['X-Custom']).toBe('value123');
    });

    it('should parse expected headers', () => {
      const gherkin = `Feature: Expected Headers
  Scenario: With expected headers
    Given the API method is "GET"
    And the API URL is "https://example.com"
    And the expected header "Content-Type" is "application/json"`;

      const result = service.parse(gherkin);

      expect(result.tests[0].expectedHeaders['Content-Type']).toBe('application/json');
    });

    it('should parse input body', () => {
      const gherkin = `Feature: Input
  Scenario: POST with body
    Given the API method is "POST"
    And the API URL is "https://example.com"
    And the input is '{"title": "foo"}'`;

      const result = service.parse(gherkin);

      expect(result.tests[0].input).toBe('{"title": "foo"}');
    });

    it('should parse expected output', () => {
      const gherkin = `Feature: Output
  Scenario: Expected output
    Given the API method is "GET"
    And the API URL is "https://example.com"
    And the expected output is '{"id": 1}'`;

      const result = service.parse(gherkin);

      expect(result.tests[0].expectedOutput).toBe('{"id": 1}');
    });

    it('should parse response time', () => {
      const gherkin = `Feature: ResponseTime
  Scenario: With response time
    Given the API method is "GET"
    And the API URL is "https://example.com"
    And the response time is 5000`;

      const result = service.parse(gherkin);

      expect(result.tests[0].responseTime).toBe(5000);
    });

    it('should report error for missing API URL', () => {
      const gherkin = `Feature: NoURL
  Scenario: Missing URL
    Given the API method is "GET"
    And the expected status code is 200`;

      const result = service.parse(gherkin);

      expect(result.tests.length).toBe(1);
      expect(result.errors.length).toBeGreaterThan(0);
      expect(result.errors[0]).toContain('missing API URL');
    });

    it('should report error for unrecognized steps', () => {
      const gherkin = `Feature: Unknown
  Scenario: Bad step
    Given the API method is "GET"
    And the API URL is "https://example.com"
    And something completely unknown`;

      const result = service.parse(gherkin);

      expect(result.errors.length).toBeGreaterThan(0);
      expect(result.errors[0]).toContain('unrecognized step');
    });

    it('should return error when no Feature keyword is present', () => {
      const gherkin = `Scenario: Orphan
    Given the API method is "GET"`;

      const result = service.parse(gherkin);

      // Native parser detects missing Feature keyword
      expect(result.errors.length).toBeGreaterThan(0);
    });

    it('should return empty tests for empty string', () => {
      const result = service.parse('');

      expect(result.tests.length).toBe(0);
      expect(result.errors.length).toBeGreaterThan(0);
    });

    it('should parse a full scenario with all fields', () => {
      const gherkin = `Feature: Full
  Scenario: Complete test
    Given the API method is "POST"
    And the API URL is "https://api.example.com/items"
    And the expected status code is 201
    And the header "Content-Type" is "application/json"
    And the header "Authorization" is "Bearer token123"
    And the input is '{"name": "item1"}'
    And the expected output is '{"id": 1, "name": "item1"}'
    And the expected header "Location" is "/items/1"
    And the response time is 3000
    When I execute the test
    Then the test should pass`;

      const result = service.parse(gherkin);

      expect(result.errors.length).toBe(0);
      const test = result.tests[0];
      expect(test.method).toBe('POST');
      expect(test.apiUrl).toBe('https://api.example.com/items');
      expect(test.statusCode).toBe(201);
      expect(test.headers['Content-Type']).toBe('application/json');
      expect(test.headers['Authorization']).toBe('Bearer token123');
      expect(test.input).toBe('{"name": "item1"}');
      expect(test.expectedOutput).toBe('{"id": 1, "name": "item1"}');
      expect(test.expectedHeaders['Location']).toBe('/items/1');
      expect(test.responseTime).toBe(3000);
    });

    it('should accept "the test should fail" step without error', () => {
      const gherkin = `Feature: Fail
  Scenario: Failure expected
    Given the API method is "GET"
    And the API URL is "https://example.com/404"
    When I execute the test
    Then the test should fail`;

      const result = service.parse(gherkin);

      expect(result.errors.length).toBe(0);
      expect(result.tests.length).toBe(1);
    });

    it('should assign sequential IDs to scenarios', () => {
      const gherkin = `Feature: IDs
  Scenario: A
    Given the API method is "GET"
    And the API URL is "https://example.com/a"

  Scenario: B
    Given the API method is "POST"
    And the API URL is "https://example.com/b"

  Scenario: C
    Given the API method is "DELETE"
    And the API URL is "https://example.com/c"`;

      const result = service.parse(gherkin);

      expect(result.tests[0].id).toBe(1);
      expect(result.tests[1].id).toBe(2);
      expect(result.tests[2].id).toBe(3);
    });

    it('should handle method case insensitively (uppercased)', () => {
      const gherkin = `Feature: Case
  Scenario: Lowercase method
    Given the API method is "post"
    And the API URL is "https://example.com"`;

      const result = service.parse(gherkin);

      expect(result.tests[0].method).toBe('POST');
    });
  });

  // ── toGherkin() ───────────────────────────────────────────

  describe('toGherkin()', () => {

    it('should convert a simple test to Gherkin', () => {
      const tests: TestModel2[] = [{
        id: 1,
        method: 'GET',
        apiUrl: 'https://example.com/api',
        headers: {},
        expectedHeaders: {},
        statusCode: 200,
      }];

      const gherkin = service.toGherkin(tests);

      expect(gherkin).toContain('Feature: API Tests');
      expect(gherkin).toContain('Scenario: Test GET https://example.com/api');
      expect(gherkin).toContain('the API method is "GET"');
      expect(gherkin).toContain('the API URL is "https://example.com/api"');
      expect(gherkin).toContain('the expected status code is 200');
    });

    it('should include headers in output', () => {
      const tests: TestModel2[] = [{
        id: 1,
        method: 'GET',
        apiUrl: 'https://example.com',
        headers: { 'Accept': 'application/json' },
        expectedHeaders: {},
      }];

      const gherkin = service.toGherkin(tests);

      expect(gherkin).toContain('the header "Accept" is "application/json"');
    });

    it('should include expected headers', () => {
      const tests: TestModel2[] = [{
        id: 1,
        method: 'GET',
        apiUrl: 'https://example.com',
        headers: {},
        expectedHeaders: { 'Content-Type': 'text/html' },
      }];

      const gherkin = service.toGherkin(tests);

      expect(gherkin).toContain('the expected header "Content-Type" is "text/html"');
    });

    it('should include input body', () => {
      const tests: TestModel2[] = [{
        id: 1,
        method: 'POST',
        apiUrl: 'https://example.com',
        headers: {},
        expectedHeaders: {},
        input: '{"key": "val"}',
      }];

      const gherkin = service.toGherkin(tests);

      expect(gherkin).toContain("the input is '{\"key\": \"val\"}'");
    });

    it('should include expected output', () => {
      const tests: TestModel2[] = [{
        id: 1,
        method: 'GET',
        apiUrl: 'https://example.com',
        headers: {},
        expectedHeaders: {},
        expectedOutput: '{"id": 1}',
      }];

      const gherkin = service.toGherkin(tests);

      expect(gherkin).toContain("the expected output is '{\"id\": 1}'");
    });

    it('should include response time', () => {
      const tests: TestModel2[] = [{
        id: 1,
        method: 'GET',
        apiUrl: 'https://example.com',
        headers: {},
        expectedHeaders: {},
        responseTime: 2000,
      }];

      const gherkin = service.toGherkin(tests);

      expect(gherkin).toContain('the response time is 2000');
    });

    it('should produce multiple scenarios for multiple tests', () => {
      const tests: TestModel2[] = [
        { id: 1, method: 'GET', apiUrl: 'https://a.com', headers: {}, expectedHeaders: {} },
        { id: 2, method: 'POST', apiUrl: 'https://b.com', headers: {}, expectedHeaders: {} },
      ];

      const gherkin = service.toGherkin(tests);

      expect((gherkin.match(/Scenario:/g) || []).length).toBe(2);
    });

    it('should always include When/Then steps', () => {
      const tests: TestModel2[] = [{
        id: 1,
        method: 'GET',
        apiUrl: 'https://example.com',
        headers: {},
        expectedHeaders: {},
      }];

      const gherkin = service.toGherkin(tests);

      expect(gherkin).toContain('When I execute the test');
      expect(gherkin).toContain('Then the test should pass');
    });
  });

  // ── Round-trip ────────────────────────────────────────────

  describe('round-trip (toGherkin → parse)', () => {

    it('should produce equivalent tests after round-trip', () => {
      const original: TestModel2[] = [{
        id: 1,
        method: 'PUT',
        apiUrl: 'https://example.com/items/1',
        headers: { 'Content-Type': 'application/json' },
        expectedHeaders: { 'X-Request-Id': 'abc' },
        statusCode: 200,
        input: '{"name": "updated"}',
        expectedOutput: '{"name": "updated"}',
        responseTime: 1500,
      }];

      const gherkin = service.toGherkin(original);
      const parsed = service.parse(gherkin);

      expect(parsed.errors.length).toBe(0);
      expect(parsed.tests.length).toBe(1);
      const t = parsed.tests[0];
      expect(t.method).toBe('PUT');
      expect(t.apiUrl).toBe('https://example.com/items/1');
      expect(t.statusCode).toBe(200);
      expect(t.headers['Content-Type']).toBe('application/json');
      expect(t.input).toBe('{"name": "updated"}');
      expect(t.expectedOutput).toBe('{"name": "updated"}');
      expect(t.expectedHeaders['X-Request-Id']).toBe('abc');
      expect(t.responseTime).toBe(1500);
    });
  });

  // ── getSampleTemplate() ───────────────────────────────────

  describe('getSampleTemplate()', () => {

    it('should return a non-empty string', () => {
      const template = service.getSampleTemplate();
      expect(template.length).toBeGreaterThan(0);
    });

    it('should contain Feature keyword', () => {
      const template = service.getSampleTemplate();
      expect(template).toContain('Feature:');
    });

    it('should be parseable without errors', () => {
      const template = service.getSampleTemplate();
      const result = service.parse(template);
      expect(result.errors.length).toBe(0);
      expect(result.tests.length).toBeGreaterThan(0);
    });
  });
});
