/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement, NO_ERRORS_SCHEMA } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { TestApiComponent } from './test-api.component';
import { TestApiService } from '../../_services/test-api.service';
import { GherkinParserService } from '../../_services/gherkin-parser.service';
import { testModel2 } from '../../models/testmodel2';

describe('TestApiComponent', () => {
  let component: TestApiComponent;
  let fixture: ComponentFixture<TestApiComponent>;
  let testApiService: TestApiService;
  let gherkinParser: GherkinParserService;
  let httpMock: HttpTestingController;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestApiComponent ],
      imports: [
        HttpClientTestingModule,
        MatDialogModule,
        NoopAnimationsModule,
      ],
      providers: [
        TestApiService,
        GherkinParserService,
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestApiComponent);
    component = fixture.componentInstance;
    testApiService = TestBed.inject(TestApiService);
    gherkinParser = TestBed.inject(GherkinParserService);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();

    // ngOnInit fires loadDefinitions() which makes a GET request
    const reqs = httpMock.match(r => r.method === 'GET' && r.url.includes('/definitions'));
    reqs.forEach(r => r.flush([]));
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should start in table mode (gherkinMode = false)', () => {
    expect(component.gherkinMode).toBeFalse();
  });

  // ── toggleGherkinMode() ─────────────────────────────

  it('should toggle gherkin mode on', () => {
    component.toggleGherkinMode();
    expect(component.gherkinMode).toBeTrue();
  });

  it('should toggle gherkin mode off', () => {
    component.gherkinMode = true;
    component.toggleGherkinMode();
    expect(component.gherkinMode).toBeFalse();
  });

  it('should convert existing tests to gherkin when entering gherkin mode', () => {
    // Add a test to the service
    testApiService.addTestOnList({
      id: 0, method: 'GET', apiUrl: 'https://example.com',
      headers: {}, expectedHeaders: {}, statusCode: 200,
    });
    // Flush the POST request fired by addTestOnList
    httpMock.expectOne(r => r.method === 'POST' && r.url.includes('/definitions')).flush({ id: 'm1' });

    component.getTestList(); // subscribe to service
    // dataTests is populated via subscription
    component.dataTests = [{
      id: 1, method: 'GET', apiUrl: 'https://example.com',
      headers: {}, expectedHeaders: {}, statusCode: 200,
    }];

    component.toggleGherkinMode();

    expect(component.gherkinMode).toBeTrue();
    expect(component.gherkinText).toContain('Feature:');
    expect(component.gherkinText).toContain('https://example.com');
  });

  // ── onGherkinTestsReady() ──────────────────────────

  it('should add parsed tests and exit gherkin mode', () => {
    component.gherkinMode = true;
    const tests: testModel2[] = [
      { id: 1, method: 'POST', apiUrl: 'https://api.test.com', headers: {}, expectedHeaders: {} }
    ];

    component.onGherkinTestsReady(tests);

    // Flush the POST request fired by addTestOnList inside onGherkinTestsReady
    httpMock.expectOne(r => r.method === 'POST' && r.url.includes('/definitions')).flush({ id: 'mg1' });

    expect(component.gherkinMode).toBeFalse();
  });

  // ── onGherkinClose() ──────────────────────────────

  it('should exit gherkin mode on close', () => {
    component.gherkinMode = true;
    component.onGherkinClose();
    expect(component.gherkinMode).toBeFalse();
  });

  // ── editTest() ─────────────────────────────────────

  it('should open dialog with test data when editing', () => {
    const dialogSpy = spyOn(component.dialog, 'open').and.returnValue({
      afterClosed: () => of(null)
    } as any);

    const testData: testModel2 = {
      id: 1, method: 'POST', apiUrl: 'https://api.test.com',
      headers: { 'Content-Type': 'application/json' },
      expectedHeaders: {}, statusCode: 201,
    };

    component.editTest(testData);

    expect(dialogSpy).toHaveBeenCalled();
    const callArgs = dialogSpy.calls.mostRecent().args;
    expect(callArgs[1]?.data).toEqual(testData);
  });
});
