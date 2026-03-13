import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { GherkinEditorComponent } from './gherkin-editor.component';
import { GherkinParserService } from '../../_services/gherkin-parser.service';
import { testModel2 } from '../../models/testmodel2';

describe('GherkinEditorComponent', () => {
  let component: GherkinEditorComponent;
  let fixture: ComponentFixture<GherkinEditorComponent>;
  let parserService: GherkinParserService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [GherkinEditorComponent],
      imports: [
        FormsModule,
        NoopAnimationsModule,
        MatIconModule,
        MatButtonModule,
        MatTooltipModule,
      ],
      providers: [GherkinParserService]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GherkinEditorComponent);
    component = fixture.componentInstance;
    parserService = TestBed.inject(GherkinParserService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load sample template on init when no initialGherkin provided', () => {
    expect(component.gherkinText).toContain('Feature:');
    expect(component.parsedTests.length).toBeGreaterThan(0);
  });

  it('should use initialGherkin if provided', () => {
    const customGherkin = `Feature: Custom
  Scenario: Test
    Given the API method is "GET"
    And the API URL is "https://custom.example.com"`;

    component.initialGherkin = customGherkin;
    component.ngOnInit();

    expect(component.gherkinText).toBe(customGherkin);
    expect(component.parsedTests[0].apiUrl).toBe('https://custom.example.com');
  });

  // ── onTextChange() ──────────────────────────────────────

  it('should parse text and update parsedTests on text change', () => {
    component.gherkinText = `Feature: Parse
  Scenario: A
    Given the API method is "DELETE"
    And the API URL is "https://example.com/1"`;

    component.onTextChange();

    expect(component.parsedTests.length).toBe(1);
    expect(component.parsedTests[0].method).toBe('DELETE');
  });

  it('should populate parseErrors for invalid gherkin', () => {
    component.gherkinText = 'not valid gherkin at all';
    component.onTextChange();

    expect(component.parseErrors.length).toBeGreaterThan(0);
  });

  // ── loadTemplate() ─────────────────────────────────────

  it('should load sample template', () => {
    component.gherkinText = '';
    component.loadTemplate();

    expect(component.gherkinText).toContain('Feature:');
    expect(component.parsedTests.length).toBeGreaterThan(0);
  });

  // ── togglePreview() ────────────────────────────────────

  it('should toggle preview visibility', () => {
    expect(component.showPreview).toBeFalse();

    component.togglePreview();
    expect(component.showPreview).toBeTrue();

    component.togglePreview();
    expect(component.showPreview).toBeFalse();
  });

  // ── getHighlightedHtml() ──────────────────────────────

  it('should return single newline for empty text', () => {
    component.gherkinText = '';
    expect(component.getHighlightedHtml()).toBe('\n');
  });

  it('should highlight Feature keyword', () => {
    component.gherkinText = 'Feature: Test';
    const html = component.getHighlightedHtml();
    expect(html).toContain('gh-keyword');
  });

  it('should highlight Given/When/Then steps', () => {
    component.gherkinText = '    Given something\n    When action\n    Then result';
    const html = component.getHighlightedHtml();
    expect(html).toContain('gh-step');
  });

  it('should highlight strings in double quotes', () => {
    component.gherkinText = 'the method is "GET"';
    const html = component.getHighlightedHtml();
    expect(html).toContain('gh-string');
  });

  it('should highlight comments', () => {
    component.gherkinText = '# This is a comment';
    const html = component.getHighlightedHtml();
    expect(html).toContain('gh-comment');
  });

  it('should highlight tags', () => {
    component.gherkinText = '@smoke';
    const html = component.getHighlightedHtml();
    expect(html).toContain('gh-tag');
  });

  // ── applyTests() ──────────────────────────────────────

  it('should emit parsed tests when apply is called', () => {
    component.gherkinText = parserService.getSampleTemplate();
    component.onTextChange();

    let emitted: testModel2[] | undefined;
    component.testsReady.subscribe((tests: testModel2[]) => emitted = tests);

    component.applyTests();

    expect(emitted).toBeDefined();
    expect(emitted!.length).toBeGreaterThan(0);
  });

  it('should not emit when parsedTests is empty', () => {
    component.parsedTests = [];

    let emitted = false;
    component.testsReady.subscribe(() => emitted = true);

    component.applyTests();

    expect(emitted).toBeFalse();
  });

  // ── cancel() ──────────────────────────────────────────

  it('should emit closeEditor on cancel', () => {
    let closed = false;
    component.closeEditor.subscribe(() => closed = true);

    component.cancel();

    expect(closed).toBeTrue();
  });

  // ── objectKeys() ──────────────────────────────────────

  it('should return keys of an object', () => {
    expect(component.objectKeys({ a: '1', b: '2' })).toEqual(['a', 'b']);
  });

  it('should return empty array for null/undefined', () => {
    expect(component.objectKeys(null as any)).toEqual([]);
    expect(component.objectKeys(undefined as any)).toEqual([]);
  });
});
