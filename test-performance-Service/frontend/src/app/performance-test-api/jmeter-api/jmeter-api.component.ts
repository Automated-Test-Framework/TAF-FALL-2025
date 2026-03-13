import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { JMeterHttpRequest } from './jmeter-http-request';
import { JMeterFTPRequest } from './jmeter-ftp-request';
import { JMETER_SCENARIOS } from '../../models/jmeter_scenarios';

import { Subscription } from 'rxjs';
import Swal from 'sweetalert2';
import { PerformanceTestApiService } from 'src/app/_services/performance-test-api.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-jmeter-api',
  templateUrl: './jmeter-api.component.html',
  styleUrls: [
    './jmeter-api.component.css',
  ],
})
export class JmeterApiComponent implements OnInit {

  isHttpSidebarVisible: boolean = false;
  isFtpSidebarVisible: boolean = false;
  showHttpButton: boolean = true;
  showFtpButton: boolean = true;

  modal: HTMLElement | null = document.getElementById('myModal');
  span: Element | null = document.getElementsByClassName('close')[0];
  testResult: any;
  testLog: String = '';
  reportFilePath: String = '';
  busy: Subscription | undefined;

  http_request: JMeterHttpRequest = new JMeterHttpRequest();
  ftp_request: JMeterFTPRequest = new JMeterFTPRequest();
  requestTargets: { [type: string]: any } = {
    'http': this.http_request,
    'ftp': this.ftp_request,
  };

  formType: string = 'http';
  isGlossaryVisible: boolean = false;

  protocolFormGroup!: FormGroup;
  detailsFormGroup!: FormGroup;
  paramsFormGroup!: FormGroup;
  configFormGroup!: FormGroup;

  filteredScenarios: any[] = [];
  selectedScenario: string | null = null;
  scenarioConfigurations = JMETER_SCENARIOS;

  http_description = document.getElementById('http-description');
  ftp_description = document.getElementById('ftp-description');

  testResults: any[] = [];
  result_table: HTMLElement | null = document.getElementById('result_table');
  httpForm: HTMLElement | null = document.getElementById('http-form');
  ftpForm: HTMLElement | null = document.getElementById('ftp-form');
  switchLabel: HTMLElement | null = document.getElementById('switchLabel');
  switchCheckbox: HTMLInputElement | null = document.getElementById(
    'formSwitch'
  ) as HTMLInputElement;

  selectedTest: any = null;

  showAIPrompt = false;
  aiPrompt = '';
  aiGenerating = false;

  constructor(
    private fb: FormBuilder,
    private performanceTestApiService: PerformanceTestApiService) { }

  ngOnInit(): void {
    this.modal = document.getElementById('myModal');
    this.span = document.getElementsByClassName('close')[0];
    this.result_table = document.getElementById('result_table');
    this.httpForm = document.getElementById('http-form');
    this.ftpForm = document.getElementById('ftp-form');
    this.switchLabel = document.getElementById('switchLabel');
    this.http_description = document.getElementById('http-description');
    this.ftp_description = document.getElementById('ftp-description');

    this.updateScenariosFilter();

    // Initialize FormGroups for Stepper
    this.protocolFormGroup = this.fb.group({
      protocol: ['HTTP', Validators.required]
    });

    this.detailsFormGroup = this.fb.group({
      domain: ['', Validators.required],
      port: ['']
    });

    this.paramsFormGroup = this.fb.group({
      method: ['GET', Validators.required],
      path: ['', Validators.required],
      data: ['']
    });

    this.configFormGroup = this.fb.group({
      nbThreads: ['10', Validators.required],
      rampTime: ['5'],
      duration: ['60'],
      loop: ['1', Validators.required]
    });

    // Set default methods on models
    this.http_request.method = 'GET';
    this.ftp_request.method = 'Retrieve';
    this.http_request.protocol = 'HTTP';

    // Watch for protocol changes to update formType
    this.protocolFormGroup.get('protocol')?.valueChanges.subscribe((value: string) => {
      this.http_request.protocol = value;
      this.onProtocolChange();
    });
  }

  get selectedProtocol(): string {
    return this.protocolFormGroup?.get('protocol')?.value || 'HTTP';
  }

  toggleHttpSidebar() {
    this.isHttpSidebarVisible = !this.isHttpSidebarVisible;
    this.adjustFormMargin();
  }

  toggleFtpSidebar() {
    this.isFtpSidebarVisible = !this.isFtpSidebarVisible;
    this.adjustFormMargin();
  }

  adjustFormMargin() {
    const mainContent = document.querySelector('.main') as HTMLElement;
    const headerContainer = document.querySelector('.header-container') as HTMLElement;

    if (mainContent && headerContainer) {
      if (this.isHttpSidebarVisible || this.isFtpSidebarVisible) {
        mainContent.style.marginLeft = '500px';
        headerContainer.style.marginLeft = '500px';
      } else {
        mainContent.style.marginLeft = '0';
        headerContainer.style.marginLeft = '0';
      }
    }
  }

  resetForms() {
    this.protocolFormGroup.reset({ protocol: 'HTTP' });
    this.detailsFormGroup.reset();
    this.paramsFormGroup.reset({ method: 'GET' });
    this.configFormGroup.reset({ nbThreads: '10', rampTime: '5', duration: '60', loop: '1' });
  }

  updateScenariosFilter() {
    this.filteredScenarios = JMETER_SCENARIOS.filter(
      scenario => scenario.type === this.formType
    );
    console.log('Filtering scenarios for:', this.formType);
    console.log('Results:', this.filteredScenarios);
  }

  onScenarioSelect() {
    const scenario = this.scenarioConfigurations.find(
      s => s.name === this.selectedScenario
    );
    if (!scenario) return;

    const target = this.requestTargets[scenario.type];
    this.applyScenario(target, scenario.config);
  }

  applyScenario<T>(target: T, source: Partial<T>): T {
    Object.keys(source).forEach(key => {
      const configKey = key as keyof T;
      const sourceValue = source[configKey];

      if (typeof sourceValue === 'string' && sourceValue !== '') {
        target[configKey] = sourceValue as T[keyof T];
      }
      else if (sourceValue !== null && sourceValue !== undefined) {
        target[configKey] = sourceValue as T[keyof T];
      }
    });
    return target;
  }

  onProtocolChange() {
    this.formType = this.selectedProtocol === 'FTP' ? 'ftp' : 'http';
    this.updateScenariosFilter();

    // Sync models
    if (this.formType === 'http') {
      this.http_request.protocol = this.selectedProtocol;
    }

    if (this.switchCheckbox) {
      this.switchCheckbox.checked = (this.formType === 'ftp');
    }
  }

  toggleGlossary() {
    this.isGlossaryVisible = !this.isGlossaryVisible;
  }

  selectTestType(type: string) {
    let config = {
      nbThreads: '10',
      rampTime: '5',
      duration: '60',
      loop: '1'
    };

    switch (type) {
      case 'smoke':
        config = { nbThreads: '5', rampTime: '1', duration: '60', loop: '1' };
        break;
      case 'load':
        config = { nbThreads: '50', rampTime: '10', duration: '300', loop: '1' };
        break;
      case 'stress':
        config = { nbThreads: '200', rampTime: '20', duration: '600', loop: '1' };
        break;
      case 'spike':
        config = { nbThreads: '500', rampTime: '5', duration: '120', loop: '1' };
        break;
    }

    // Update models
    const target = this.formType === 'http' ? this.http_request : this.ftp_request;
    target.nbThreads = config.nbThreads;
    target.rampTime = config.rampTime;
    target.duration = config.duration;
    target.loop = config.loop;

    // Update form group
    this.configFormGroup.patchValue(config);

  }


  toggleAIPrompt() {
    this.showAIPrompt = !this.showAIPrompt;
  }

  generateFromPrompt() {
    if (!this.aiPrompt.trim()) {
      Swal.fire('Erreur', 'Veuillez décrire le test à générer.', 'error');
      return;
    }

    this.aiGenerating = true;
    this.busy = this.performanceTestApiService.generateTestPlan(this.aiPrompt).subscribe({
      next: (response: any) => {
        const plan = response?.test_plan || response?.testPlan || {};
        this.formType = 'http';

        const protocol = (plan.protocol || 'HTTP').toUpperCase();
        this.protocolFormGroup.patchValue({ protocol });
        this.detailsFormGroup.patchValue({
          domain: plan.domain || '',
          port: plan.port || ''
        });
        this.paramsFormGroup.patchValue({
          method: plan.method || 'GET',
          path: plan.path || '',
          data: plan.data || ''
        });
        this.configFormGroup.patchValue({
          nbThreads: plan.nbThreads || '10',
          rampTime: plan.rampTime || '5',
          duration: plan.duration || '60',
          loop: plan.loop || '1'
        });

        this.http_request = {
          ...this.http_request,
          nbThreads: this.configFormGroup.get('nbThreads')?.value,
          rampTime: this.configFormGroup.get('rampTime')?.value,
          duration: this.configFormGroup.get('duration')?.value,
          domain: this.detailsFormGroup.get('domain')?.value,
          port: this.detailsFormGroup.get('port')?.value,
          protocol: this.protocolFormGroup.get('protocol')?.value,
          path: this.paramsFormGroup.get('path')?.value,
          method: this.paramsFormGroup.get('method')?.value,
          loop: this.configFormGroup.get('loop')?.value,
          data: this.paramsFormGroup.get('data')?.value
        };

        Swal.fire('Succès', 'Le test JMeter a été généré. Vérifiez et lancez le test.', 'success');
      },
      error: () => {
        this.aiGenerating = false;
        Swal.fire('Erreur', 'Impossible de générer le test depuis la description.', 'error');
      },
      complete: () => {
        this.aiGenerating = false;
      }
    });
  }

  onFinalSubmit() {
    // Final sync from FormGroups to models
    const target = this.formType === 'http' ? this.http_request : this.ftp_request;

    // Details
    target.domain = this.detailsFormGroup.get('domain')?.value;
    target.port = this.detailsFormGroup.get('port')?.value;

    // Params
    target.method = this.paramsFormGroup.get('method')?.value;
    if (this.formType === 'http') {
      this.http_request.path = this.paramsFormGroup.get('path')?.value;
      this.http_request.data = this.paramsFormGroup.get('data')?.value;
      this.http_request.protocol = this.protocolFormGroup.get('protocol')?.value;
    }

    // Config
    target.nbThreads = this.configFormGroup.get('nbThreads')?.value;
    target.rampTime = this.configFormGroup.get('rampTime')?.value;
    target.duration = this.configFormGroup.get('duration')?.value;
    target.loop = this.configFormGroup.get('loop')?.value;

    // Trigger specific submit
    if (this.formType === 'http') {
      this.onHttpSubmit();
    } else {
      this.onFtpSubmit();
    }
  }

  validateHttpForm(): boolean {
    let isValid = true;
    const requiredFields = [
      { element: 'loop', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'nbThreads', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'domain', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'path', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'methodType', errorMessage: 'Veuillez sélectionner un type de requête' },
      // { element: 'duration', errorMessage: 'Veuillez entrer une durée' }, // si lors d'un nouveau teste il est requis
      // { element: 'rampTime', errorMessage: 'Veuillez entrer un temps de montée' }, // lors d'un nouveau teste il est requis
      { element: 'protocol', errorMessage: 'Veuillez entrer un protocole' } // lors d'un nouveau teste il est requis
    ];

    requiredFields.forEach(field => {
      const inputElement = document.getElementsByName(field.element)[0] as HTMLInputElement | null;
      const errorDiv = document.createElement('div');
      errorDiv.className = 'text-danger';

      if (inputElement?.nextElementSibling) {
        inputElement.nextElementSibling.remove();
      }
      if (inputElement && inputElement.value.trim() === '') {
        isValid = false;
        inputElement.classList.add('is-invalid');
        errorDiv.innerText = field.errorMessage;
        inputElement.insertAdjacentElement('afterend', errorDiv);
      } else {
        inputElement?.classList.remove('is-invalid');
      }
    });

    return isValid;
  }

  onHttpSubmit() {
    if (!this.http_request.domain || !this.http_request.path) {
      Swal.fire('Erreur', 'Veuillez remplir les champs obligatoires (URL et Chemin)', 'error');
      return;
    }

    this.busy = this.performanceTestApiService
      .sendHttpJMeterRequest(this.http_request)
      .subscribe((response: any) => {
        this.testResults = response;

        // Transformation de la réponse pour inclure des informations sur le succès ou l'échec global
        const successMessage = response.length != 0;
        this.testResult = [{
          success: successMessage,
          details: response.details // Assurez-vous que les détails sont inclus dans la réponse
        }];

        // Ajouter un message indiquant que le rapport a été généré
        if (successMessage) {
          this.testResult.push({
            message: 'Le rapport a été généré avec succès.',
            success: true
          });
        }

        if (successMessage) {
          this.modal!.style.display = 'block';
        } else {
          Swal.fire({
            icon: 'error',
            title: 'Erreur',
            text: "Le test a échoué, révisez votre configuration de test",
          })
        }
      }, (error: any) => {
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: "Le test a échoué, révisez votre configuration de test",
        })
      });
  }

  onFtpSubmit() {
    if (!this.ftp_request.domain || !this.ftp_request.remotefile) {
      Swal.fire('Erreur', 'Veuillez remplir les champs obligatoires (URL et Fichier distant)', 'error');
      return;
    }

    this.busy = this.performanceTestApiService
      .sendFtpJMeterRequest(this.ftp_request)
      .subscribe((response: any) => {
        this.testResults = response;
        this.testResult = response;
        if (response.length != 0) {
          this.modal!.style.display = 'block';
        } else {
          Swal.fire({
            icon: 'error',
            title: 'Erreur',
            text: "Le test a échoué, révisez votre configuration de test",
          })
        }

      }, (error: any) => {
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: "Le test a échoué, révisez votre configuration de test",
        })
      });
  }

  closeModal() {
    this.modal!.style.display = 'none';
  }

  showTestDetails(test: any) {
    console.log("Test details:", test);
    this.selectedTest = test;
  }

  closeTestDetails() {
    this.selectedTest = null;
  }

  closeModalOnOutsideClick(event: MouseEvent) {
    if ((event.target as HTMLElement).id === 'detailModal') {
      this.closeTestDetails();
    }
  }

  newTest() {
    this.testResults = [];
    this.selectedTest = null;
    this.modal!.style.display = 'none';
    if (this.httpForm) {
      (this.httpForm as HTMLFormElement).reset();
    }
    if (this.ftpForm) {
      (this.ftpForm as HTMLFormElement).reset();
    }
  }

  //  Afficher le dernier rapport
  showLatestReport() {
    if (this.testResult && this.testResult.length > 0) {
      const reportUrl = `${environment.apiUrl}${this.testResult[0].details['location-url']}`;
      window.open(reportUrl, '_blank');
    } else {
      Swal.fire({
        icon: 'error',
        title: 'Erreur',
        text: "Aucun rapport disponible",
      });
    }
  }
}