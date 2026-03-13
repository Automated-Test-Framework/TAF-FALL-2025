import { Injectable } from '@angular/core';
import { TestModel2 } from '../models/testmodel2';

/**
 * Lightweight, browser-safe Gherkin parser — no @cucumber dependencies.
 *
 * Supported step patterns:
 *   Given the API method is "GET"
 *   Given/And the API URL is "https://..."
 *   Given/And the expected status code is 200
 *   Given/And the header "Key" is "Value"
 *   Given/And the expected header "Key" is "Value"
 *   Given/And the input is '{"key": "value"}'
 *   Given/And the expected output is '{"key": "value"}'
 *   Given/And the response time is 5000
 */
@Injectable({ providedIn: 'root' })
export class GherkinParserService {

  /** Gherkin step keywords (prefix stripped before parsing). */
  private static readonly STEP_KW = /^\s*(?:Given|When|Then|And|But|\*)\s+/i;

  /** Matches a Feature line. */
  private static readonly FEATURE_RE = /^\s*Feature:\s*(.*)/i;

  /** Matches a Scenario / Scenario Outline / Example line. */
  private static readonly SCENARIO_RE =
    /^\s*(?:Scenario Outline|Scenario Template|Scenario|Example):\s*(.*)/i;

  /**
   * Parse Gherkin text and return an array of testModel2.
   * Each Scenario becomes one test.
   */
  parse(gherkinText: string): { tests: TestModel2[]; errors: string[] } {
    const errors: string[] = [];
    const tests: TestModel2[] = [];

    try {
      const lines = gherkinText.split(/\r?\n/);

      let foundFeature = false;
      let currentScenarioName: string | null = null;
      let currentTest: TestModel2 | null = null;
      let testId = 1;

      for (const raw of lines) {
        const line = raw.trim();

        // Skip blank lines and comments
        if (line === '' || line.startsWith('#')) continue;

        // Feature line
        const featureMatch = line.match(GherkinParserService.FEATURE_RE);
        if (featureMatch) {
          foundFeature = true;
          continue;
        }

        // Scenario line — flush previous scenario and start a new one
        const scenarioMatch = line.match(GherkinParserService.SCENARIO_RE);
        if (scenarioMatch) {
          if (currentTest) {
            this.finalizeTest(currentTest, currentScenarioName!, errors);
            tests.push(currentTest);
          }
          currentScenarioName = scenarioMatch[1].trim();
          currentTest = {
            id: testId++,
            method: 'GET',
            apiUrl: '',
            headers: {},
            expectedHeaders: {},
            statusCode: undefined,
            input: undefined,
            expectedOutput: undefined,
            responseTime: undefined,
            responseStatus: undefined,
            messages: []
          };
          continue;
        }

        // Step line
        const stepMatch = line.match(GherkinParserService.STEP_KW);
        if (stepMatch && currentTest) {
          const text = line.replace(GherkinParserService.STEP_KW, '').trim();
          this.parseStep(text, currentTest, errors, currentScenarioName || '');
          continue;
        }
      }

      // Flush the last scenario
      if (currentTest) {
        this.finalizeTest(currentTest, currentScenarioName!, errors);
        tests.push(currentTest);
      }

      if (!foundFeature) {
        errors.push('No Feature found in the Gherkin text.');
      }
    } catch (e: any) {
      errors.push('Gherkin syntax error: ' + (e.message || String(e)));
    }

    return { tests, errors };
  }

  /** Validate a completed test and push warnings. */
  private finalizeTest(test: TestModel2, scenarioName: string, errors: string[]): void {
    if (!test.apiUrl) {
      errors.push(`Scenario "${scenarioName}": missing API URL (use: And the API URL is "...")`);
    }
  }

  /**
   * Convert testModel2[] back to Gherkin text.
   */
  toGherkin(tests: TestModel2[]): string {
    const lines: string[] = ['Feature: API Tests', ''];

    for (const test of tests) {
      const scenarioName = `Test ${test.method} ${test.apiUrl}`;
      lines.push(`  Scenario: ${scenarioName}`);
      lines.push(`    Given the API method is "${test.method}"`);
      lines.push(`    And the API URL is "${test.apiUrl}"`);

      if (test.statusCode !== undefined && test.statusCode !== null) {
        lines.push(`    And the expected status code is ${test.statusCode}`);
      }

      if (test.headers && Object.keys(test.headers).length > 0) {
        for (const [key, value] of Object.entries(test.headers)) {
          lines.push(`    And the header "${key}" is "${value}"`);
        }
      }

      if (test.input != null) {
        lines.push(`    And the input is '${test.input}'`);
      }

      if (test.expectedOutput != null) {
        lines.push(`    And the expected output is '${test.expectedOutput}'`);
      }

      if (test.expectedHeaders && Object.keys(test.expectedHeaders).length > 0) {
        for (const [key, value] of Object.entries(test.expectedHeaders)) {
          lines.push(`    And the expected header "${key}" is "${value}"`);
        }
      }

      if (test.responseTime !== undefined && test.responseTime !== null) {
        lines.push(`    And the response time is ${test.responseTime}`);
      }

      lines.push(`    When I execute the test`);
      lines.push(`    Then the test should pass`);
      lines.push('');
    }

    return lines.join('\n');
  }

  /**
   * Generate a sample Gherkin template for guidance.
   */
  getSampleTemplate(): string {
    return `Feature: API Tests

  Scenario: Verify GET endpoint returns correct data
    Given the API method is "GET"
    And the API URL is "https://jsonplaceholder.typicode.com/posts/1"
    And the expected status code is 200
    And the header "Accept" is "application/json"
    When I execute the test
    Then the test should pass

  Scenario: Create a new post via POST
    Given the API method is "POST"
    And the API URL is "https://jsonplaceholder.typicode.com/posts"
    And the expected status code is 201
    And the header "Content-Type" is "application/json"
    And the input is '{"title": "foo", "body": "bar", "userId": 1}'
    When I execute the test
    Then the test should pass
`;
  }

  // ── Private helpers ──────────────────────────────────────────

  private parseStep(text: string, test: TestModel2, errors: string[], scenarioName: string): void {
    let match: RegExpMatchArray | null;

    // Method: the API method is "GET"
    match = text.match(/^the API method is "(\w+)"$/i);
    if (match) {
      test.method = match[1].toUpperCase();
      return;
    }

    // URL: the API URL is "https://..."
    match = text.match(/^the API URL is "(.+)"$/i);
    if (match) {
      test.apiUrl = match[1];
      return;
    }

    // Status code: the expected status code is 200
    match = text.match(/^the expected status code is (\d+)$/i);
    if (match) {
      test.statusCode = parseInt(match[1], 10);
      return;
    }

    // Request header: the header "Key" is "Value"
    match = text.match(/^the header "(.+)" is "(.+)"$/i);
    if (match) {
      test.headers[match[1]] = match[2];
      return;
    }

    // Expected header: the expected header "Key" is "Value"
    match = text.match(/^the expected header "(.+)" is "(.+)"$/i);
    if (match) {
      test.expectedHeaders[match[1]] = match[2];
      return;
    }

    // Input body: the input is '...'
    match = text.match(/^the input is '(.*)'$/i);
    if (match) {
      test.input = match[1];
      return;
    }

    // Expected output: the expected output is '...'
    match = text.match(/^the expected output is '(.*)'$/i);
    if (match) {
      test.expectedOutput = match[1];
      return;
    }

    // Response time: the response time is 5000
    match = text.match(/^the response time is (\d+)$/i);
    if (match) {
      test.responseTime = parseInt(match[1], 10);
      return;
    }

    // When / Then steps — silently accepted
    if (/^I execute the test$/i.test(text)) return;
    if (/^the test should pass$/i.test(text)) return;
    if (/^the test should fail$/i.test(text)) return;

    // Unknown step
    errors.push(`Scenario "${scenarioName}": unrecognized step — "${text}"`);
  }
}
