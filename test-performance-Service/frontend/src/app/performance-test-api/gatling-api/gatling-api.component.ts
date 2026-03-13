import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PerformanceTestApiService } from 'src/app/_services/performance-test-api.service';
import Swal from 'sweetalert2';
import { GatlingRequest, ResponseTimePerPercentile } from './gatling-request';
import { ApiResponse, GatlingAssertionResult, GatlingTestResult } from "../../models/gatlingTestResult";
import { GATLING_SCENARIOS } from "../../models/gatling-scenarios";

enum SIMULATION_STRATEGY {
  DEFAULT = "DEFAULT",
  SMOKE_TEST = "SMOKE_TEST",
  LOAD_TEST = "LOAD_TEST",
  STRESS_TEST = "STRESS_TEST",
  SPIKE_TEST = "SPIKE_TEST",
}

@Component({
  selector: 'app-gatling-api',
  templateUrl: './gatling-api.component.html',
  styleUrls: [
    './gatling-api.component.css'
  ]
})
export class GatlingApiComponent implements OnInit {
  modal: HTMLElement | null = null;
  reportModal: HTMLElement | null = null;
  span: HTMLElement | null = null;
  testResult: any;

  gatlingTestResult: GatlingTestResult | null = null

  percentiles: number[] = [50, 75, 90, 95, 99, 99.9];
  newPercentile: number = 50;
  newResponseTime: number = 0;

  testLog: string = "";
  latestReportContent: SafeHtml | null = null;

  busy: Subscription | undefined;

  request: GatlingRequest = new GatlingRequest({});

  strategies: string[] = ["DEFAULT", "SMOKE_TEST", "LOAD_TEST", "STRESS_TEST", "SPIKE_TEST"];
  strategiesEnum = SIMULATION_STRATEGY;
  selectedStrategy: string = "DEFAULT";

  // Stepper FormGroups
  setupFormGroup!: FormGroup;
  requestFormGroup!: FormGroup;
  strategyFormGroup!: FormGroup;
  criteriaFormGroup!: FormGroup;

  isGlossaryVisible: boolean = false;

  constructor(
    private readonly performanceTestApiService: PerformanceTestApiService,
    private sanitizer: DomSanitizer,
    private fb: FormBuilder
  ) { }

  ngOnInit(): void {
    this.modal = document.getElementById("myModal");
    this.reportModal = document.getElementById("reportModal");
    this.span = document.getElementsByClassName("close")[0] as HTMLElement;

    this.initFormGroups();

    // Watch for strategy changes
    this.strategyFormGroup.get('simulationStrategy')?.valueChanges.subscribe(value => {
      this.selectedStrategy = value;
      this.onStrategySelect();
    });
  }

  initFormGroups() {
    this.setupFormGroup = this.fb.group({
      testScenarioName: ['', Validators.required],
      testRequestName: [''],
      testBaseUrl: ['', [Validators.required]]
    });

    this.requestFormGroup = this.fb.group({
      testUri: ['', Validators.required],
      testMethodType: ['GET', Validators.required],
      testRequestBody: ['']
    });

    this.strategyFormGroup = this.fb.group({
      simulationStrategy: ['DEFAULT', Validators.required],
      testUsersNumber: [10],
      testRampUpDuration: [5],
      testUserRampUpPerSecondMin: [0],
      testUserRampUpPerSecondMax: [10],
      testUserRampUpPerSecondDuration: [30],
      testConstantUsers: [5],
      testConstantUsersDuration: [60],
      testUsersAtOnce: [1]
    });

    this.criteriaFormGroup = this.fb.group({
      assertionMeanResponseTime: [50, Validators.required],
      assertionFailedRequestsPercent: [2, Validators.required]
    });
  }

  onStrategySelect() {
    const match = GATLING_SCENARIOS.find(scenario => scenario.name === this.selectedStrategy);
    if (match) {
      // Create a fresh request model
      this.request = new GatlingRequest(match.config);

      // Patch form values
      this.strategyFormGroup.patchValue({
        testUsersNumber: match.config.testUsersNumber,
        testRampUpDuration: match.config.testRampUpDuration,
        testUserRampUpPerSecondMin: match.config.testUserRampUpPerSecondMin,
        testUserRampUpPerSecondMax: match.config.testUserRampUpPerSecondMax,
        testUserRampUpPerSecondDuration: match.config.testUserRampUpPerSecondDuration,
        testConstantUsers: match.config.testConstantUsers,
        testConstantUsersDuration: match.config.testConstantUsersDuration,
        testUsersAtOnce: match.config.testUsersAtOnce
      }, { emitEvent: false });

      // Also patch criteria if they are in the scenario
      this.criteriaFormGroup.patchValue({
        assertionMeanResponseTime: match.config.assertionMeanResponseTime,
        assertionFailedRequestsPercent: match.config.assertionFailedRequestsPercent
      }, { emitEvent: false });
    }
  }

  onFinalSubmit() {
    // Sync all form values to the request model
    const setup = this.setupFormGroup.value;
    const req = this.requestFormGroup.value;
    const strategy = this.strategyFormGroup.value;
    const criteria = this.criteriaFormGroup.value;

    this.request.testScenarioName = setup.testScenarioName;
    this.request.testRequestName = setup.testRequestName;
    this.request.testBaseUrl = setup.testBaseUrl;

    this.request.testUri = req.testUri;
    this.request.testMethodType = req.testMethodType;
    this.request.testRequestBody = req.testRequestBody;

    this.request.simulationStrategy = strategy.simulationStrategy;
    this.request.testUsersNumber = strategy.testUsersNumber;
    this.request.testRampUpDuration = strategy.testRampUpDuration;
    this.request.testUserRampUpPerSecondMin = strategy.testUserRampUpPerSecondMin;
    this.request.testUserRampUpPerSecondMax = strategy.testUserRampUpPerSecondMax;
    this.request.testUserRampUpPerSecondDuration = strategy.testUserRampUpPerSecondDuration;
    this.request.testConstantUsers = strategy.testConstantUsers;
    this.request.testConstantUsersDuration = strategy.testConstantUsersDuration;
    this.request.testUsersAtOnce = strategy.testUsersAtOnce;

    this.request.assertionMeanResponseTime = criteria.assertionMeanResponseTime;
    this.request.assertionFailedRequestsPercent = criteria.assertionFailedRequestsPercent;

    this.onSubmit();
  }

  onSubmit() {
    console.log("Sending Gatling Request:", this.request);

    this.busy = this.performanceTestApiService.sendGatlingRequest(this.request)
      .subscribe({
        next: (response: ApiResponse) => {
          if (response.message && (response.message.startsWith('Error') || response.message.includes('failed to execute') || response.message.includes('Simulation failed'))) {
            Swal.fire({
              icon: 'error',
              title: 'Erreur de simulation',
              text: response.message,
              footer: 'Veuillez vérifier les logs serveur pour plus de détails.'
            });
            return;
          }

          this.testResult = response.testResult;
          const output = response.message || "";
          const hasReport = output.includes('Generated Report') || output.includes('Please open');

          if (this.testResult) {
            (this.testResult as any).reportGenerated = hasReport;
          }

          if (this.modal) this.modal.style.display = "block";
        },
        error: (error: any) => {
          let errorMessage = "Le test a échoué, révisez votre configuration de test";
          if (error.error) {
            if (typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error.message) {
              errorMessage = error.error.message;
            }
          }
          Swal.fire({
            icon: 'error',
            title: 'Erreur Serveur',
            text: errorMessage
          });
        }
      });
  }

  showLatestReport() {
    const url = this.performanceTestApiService.getLatestReportUrl();
    window.open(url, '_blank');
  }

  getLatestReportUrl(): string {
    return this.performanceTestApiService.getLatestReportUrl();
  }

  openReportModal() {
    if (this.reportModal) this.reportModal.style.display = "block";
  }

  closeReportModal() {
    if (this.reportModal) {
      this.reportModal.style.display = "none";
      this.latestReportContent = null;
    }
  }

  closeModal() {
    if (this.modal) {
      this.modal.style.display = "none";
      this.latestReportContent = null;
    }
  }

  newTest() {
    this.request = new GatlingRequest({});
    this.setupFormGroup.reset({ testScenarioName: '', testBaseUrl: '' });
    this.requestFormGroup.reset({ testMethodType: 'GET', testUri: '' });
    this.strategyFormGroup.reset({ simulationStrategy: 'DEFAULT' });
    this.criteriaFormGroup.reset({ assertionMeanResponseTime: 50, assertionFailedRequestsPercent: 2 });
    this.closeModal();
  }

  isSuccessfull(): boolean {
    if (!this.testResult || !this.testResult.assertions) return false;
    const failures = this.testResult.assertions.filter((assertion: GatlingAssertionResult) => assertion.result == false);
    return failures.length == 0;
  }

  addPercentile(): void {
    if (this.newPercentile && this.newResponseTime) {
      const newAssertion = new ResponseTimePerPercentile(this.newPercentile, this.newResponseTime);
      this.request.assertionsResponseTimePerPercentile.push(newAssertion);
      this.newResponseTime = 0;
    }
  }

  removePercentile(index: number): void {
    this.request.assertionsResponseTimePerPercentile.splice(index, 1);
  }

  toggleGlossary(): void {
    this.isGlossaryVisible = !this.isGlossaryVisible;
  }

  selectTestType(type: string): void {
    let mapping: { [key: string]: string } = {
      'smoke': SIMULATION_STRATEGY.SMOKE_TEST,
      'load': SIMULATION_STRATEGY.LOAD_TEST,
      'stress': SIMULATION_STRATEGY.STRESS_TEST,
      'spike': SIMULATION_STRATEGY.SPIKE_TEST
    };

    const strategy = mapping[type];
    if (strategy) {
      this.strategyFormGroup.patchValue({ simulationStrategy: strategy });
    }
  }
}
