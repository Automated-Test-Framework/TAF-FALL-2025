import { Component, OnInit } from '@angular/core';
import { TestApiService } from 'src/app/_services/test-api.service';
import { testModel } from "../../models/test-model";
import { MatDialog } from "@angular/material/dialog";
import { AddTestDialogComponent } from "./add-test-dialog/add-test-dialog.component";
import { DeleteTestDialogComponent } from "./delete-test-dialog/delete-test-dialog.component";
import { testModel2 } from "../../models/testmodel2";
import { TestResponseModel } from "../../models/testResponseModel";
import { ErrorDialogComponent } from './error-dialog.component';
import { GherkinParserService } from '../../_services/gherkin-parser.service';

@Component({
  selector: 'app-test-api',
  templateUrl: './test-api.component.html',
  styleUrls: ['./test-api.component.css']
})
export class TestApiComponent implements OnInit {
  isPopupOpened = true;
  // Colonnes à afficher
  displayedColumns: string[] = ['id', 'method', 'apiUrl', 'responseTime', 'statusCode', 'responseStatus', 'action'];
  dataTests: testModel2[] = [];

  // Gherkin mode
  gherkinMode: boolean = false;
  gherkinText: string = '';

  constructor(
    private testApiService: TestApiService,
    public dialog: MatDialog,
    private gherkinParser: GherkinParserService,
  ) { }

  ngOnInit() {
    // Chargement de la liste des tests
    this.getTestList();
  }

  // Récupération de la liste des tests
  getTestList(): void {
    this.testApiService.tests$.subscribe((tests: testModel2[]) => { this.dataTests = tests; });
  }

  // Ouvre le dialogue d'ajout de test
  addTest() {
    this.isPopupOpened = true;
    const dialogRef = this.dialog.open(AddTestDialogComponent, {});
    dialogRef.afterClosed().subscribe(result => {
      this.isPopupOpened = false;
      this.ngOnInit(); // rafraîchit la liste
    });
  }

  // Ouvre le dialogue de suppression de test
  deleteTest(id: string) {
    this.isPopupOpened = true;
    const dialogRef = this.dialog.open(DeleteTestDialogComponent, { data: id });
    dialogRef.afterClosed().subscribe(result => {
      this.isPopupOpened = false;
      this.ngOnInit();
    });
    this.getTestList();
  }

  exportCSV(): void {
    if (this.dataTests.length === 0) {
      return;
    }
  
    const separator = ',';
    const keys = Object.keys(this.dataTests[0]) as string[]; // Forcer en string[]
    const csvContent =
      keys.join(separator) +
      '\n' +
      this.dataTests
        .map(item => {
          return keys
            .map(key => {
              let value = (item as any)[key];
              // Convertir les objets en JSON stringifiés
              if (typeof value === 'object' && value !== null) {
                value = JSON.stringify(value);
              }
              // Vérification correcte pour expectedOutput
              if (key.toLowerCase() === 'expectedoutput' && (!value || value === '')) {
                value = '{}';
              }
              return value;
            })
            .join(separator);
        })
        .join('\n');
  
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    if (link.download !== undefined) {
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', 'dataTests.csv');
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  }
  
  


  importCSV(event: any): void {
    const file = event.target.files[0];
    if (!file) return;
  
    const reader = new FileReader();
    reader.onload = (e: any) => {
      const csv: string = e.target.result;
      const lines: string[] = csv.split('\n').filter((line: string) => line.trim().length > 0);
      if (!lines.length) { return; }
      
      const headers: string[] = lines[0].split(',').map((h: string) => h.trim());
      const tests: testModel2[] = [];
  
      lines.slice(1).forEach((line: string) => {
        const values: string[] = line.split(',').map((v: string) => v.trim());
        const testObj: any = {};
        headers.forEach((header: string, index: number) => {
          testObj[header] = values[index];
        });
        // Conversion des types nécessaires
        testObj.id = Number(testObj.id);
        testObj.statusCode = Number(testObj.statusCode);
        try { 
          testObj.headers = JSON.parse(testObj.headers); 
        } catch { 
          testObj.headers = {}; 
        }
        try { 
          testObj.expectedHeaders = JSON.parse(testObj.expectedHeaders); 
        } catch { 
          testObj.expectedHeaders = {}; 
        }
        // Réinitialise les champs de résultat pour que la colonne soit vide
        testObj.responseStatus = undefined;
        testObj.responseTime = undefined;
        testObj.messages = [];
        tests.push(testObj as testModel2);
      });
  
      // Ajoute chaque test à la liste via le service, sans lancer les tests automatiquement
      tests.forEach((test: testModel2) => this.testApiService.addTestOnList(test));
    };
    reader.readAsText(file);
  }
  

  // Exécution des tests
  lunchTests() {
    this.testApiService.executeTests(this.dataTests).subscribe({
      next: (listTestsReponses: TestResponseModel[]) => {
        this.updateTestsStatusExecution(listTestsReponses);
      },
      error: (error) => {
        this.showErrorPopup("Erreur lors de l'exécution des tests : " + error.message);
      }
    });
  }

  // Affiche une erreur dans un popup
  showErrorPopup(message: string) {
    this.dialog.open(ErrorDialogComponent, {
      data: { message },
      width: '400px'
    });
  }

  // Mise à jour de la liste des tests après exécution
  updateTestsStatusExecution(listTestsReponses: TestResponseModel[]) {
    console.log("========>", listTestsReponses);
    this.testApiService.updateTestsStatusExecution(listTestsReponses);
    this.getTestList();
  }

  // ── Gherkin mode ──────────────────────────────────

  /** Toggle between table mode and Gherkin editor mode */
  toggleGherkinMode(): void {
    if (!this.gherkinMode && this.dataTests.length > 0) {
      // Convert existing table tests to Gherkin when entering Gherkin mode
      this.gherkinText = this.gherkinParser.toGherkin(this.dataTests);
    }
    this.gherkinMode = !this.gherkinMode;
  }

  /** Receive parsed tests from Gherkin editor and add to table */
  onGherkinTestsReady(tests: testModel2[]): void {
    // Clear existing tests and add new ones from Gherkin
    this.testApiService.clearTests();
    tests.forEach(test => this.testApiService.addTestOnList(test));
    this.gherkinMode = false;
    this.getTestList();
  }

  /** Close Gherkin editor without applying */
  onGherkinClose(): void {
    this.gherkinMode = false;
  }
}
