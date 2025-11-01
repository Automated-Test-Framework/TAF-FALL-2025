import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Chart, ChartConfiguration, ChartOptions} from 'chart.js';
import 'chart.js/auto';
import { Subject, takeUntil } from 'rxjs';

import {
  RunCard, PassratePoint, CaseSearchResponse, CaseItem,
  ToolRate, TypeRate, NamedCount
} from '../models/dashboard.model';
import { BoardAdminService } from './board-admin.service';
import { MatDialog } from '@angular/material/dialog';
import { CaseDetailDialogComponent } from './case-detail-dialog.component';

type StatusFilter = 'all' | 'passed' | 'failed';
type ToolFilter   = 'all' | 'gatling' | 'selenium' | 'restAssured' | 'other';

@Component({
  selector: 'app-board-admin',
  templateUrl: './board-admin.component.html',
  styleUrls: ['./board-admin.component.css']
})
export class BoardAdminComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Etat UI
  loadingRuns = true;
  loadingRate = true;
  loadingCases = true;
  loadingTool = true;
  loadingType = true;

  // Filtres
  project = 'TAF';
  days: 7 | 14 | 30 | 60 = 30;
  fStatus: StatusFilter = 'all';
  fTool: ToolFilter     = 'all';

  // Pagination
  page = 0;
  size = 20;

  // Données
  runs: RunCard[] = [];
  rate: PassratePoint[] = [];
  casesResp: CaseSearchResponse = { page: 0, size: this.size, total: 0, items: [] };

  toolRates: ToolRate[] = [];
  typeRates: TypeRate[] = [];
  topFails: NamedCount[] = [];
  flaky: NamedCount[] = [];

  // KPI
  totalRuns = 0;
  totalPassed = 0;
  passPct = 0;

  /* Palette pour barres (5 couleurs distinctes) */
  private barPalette = ['#6366f1', '#22c55e', '#f59e0b', '#ef4444', '#06b6d4'];
  // -------------










  // -----------------------

  // ====== CHARTS ======

  /** Donut Pass/Fail */

  pieData: ChartConfiguration<'doughnut'>['data'] = {
    labels: ['Passés', 'Échoués'],
    datasets: [{ data: [0, 0], borderWidth: 0, backgroundColor: ['#2ecc71', '#e74c3c'] }]
  };
  pieOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    cutout: '65%',
    plugins: {
      legend: { position: 'bottom' },
      tooltip: { enabled: true }
    },
    animation: { duration: 600, easing: 'easeOutQuart' }


  };




  /** Line chart Passrate journalier */
  lineData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [{ label: 'Passrate (%)', data: [], tension: 0.35, pointRadius: 3, fill: false }]
  };
  lineOptions: ChartOptions<'line'> = {
    responsive: true,
    interaction: { mode: 'index', intersect: false },
    scales: { y: { beginAtZero: true, suggestedMax: 100, ticks: { stepSize: 20 } } },
    plugins: { legend: { display: true } },
    animation: { duration: 500 }
  };

  /** Bar % pass des 5 derniers runs */
  runsBarData: ChartConfiguration<'bar'>['data'] = { labels: [], datasets: [{ label: '% pass du run', data: [] }] };
  runsBarOptions: ChartOptions<'bar'> = {
    responsive: true,
    scales: { y: { beginAtZero: true, suggestedMax: 100 } },
    plugins: { legend: { display: true } }
  };

  /** Bar % par outil */
  toolBarData: ChartConfiguration<'bar'>['data'] = { labels: [], datasets: [{ label: '% pass', data: [] }] };
  toolBarOptions: ChartOptions<'bar'> = {
    responsive: true,
    scales: { y: { beginAtZero: true, suggestedMax: 100, ticks: { stepSize: 20 } } },
    plugins: { legend: { display: true } }
  };

  /** Stacked par type (passed/failed) */
  stackedTypeData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      { label: 'Passés',  data: [], stack: 's', backgroundColor: '#2ecc71' },
      { label: 'Échoués', data: [], stack: 's', backgroundColor: '#e74c3c' }
    ]
  };
  stackedTypeOptions: ChartOptions<'bar'> = {
    responsive: true,
    scales: { x: { stacked: true }, y: { stacked: true, beginAtZero: true } },
    plugins: { legend: { display: true } }
  };

  constructor(private api: BoardAdminService, private dialog: MatDialog) {}

  ngOnInit(): void { this.loadAll(); }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  // ====== Actions ======

  loadAll(): void {
    this.page = 0; // on revient au début sur changement de filtres
    this.fetchRate();
    this.fetchRuns();
    this.fetchCases();
    this.fetchToolRates();
    this.fetchTypeRates();
    this.fetchTopFails();
    this.fetchFlaky();
  }

  changeDays(d: 7 | 14 | 30 | 60): void {
    this.days = d;
    this.loadAll();

  }

  changePage(delta: number): void {
    const maxPage = Math.max(0, Math.ceil(this.casesResp.total / this.size) - 1);
    this.page = Math.min(maxPage, Math.max(0, this.page + delta));
    this.fetchCases();
  }

  // ====== Fetchers ======

  private fetchRuns(): void {
    this.loadingRuns = true;
    const statusParam = this.fStatus === 'all' ? undefined : this.fStatus;
    this.api.getLatestRuns(this.project, 5, statusParam)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (rows) => {
          this.runs = rows ?? [];
          // bar des 5 derniers runs
          const labels = this.runs.map(r => r.runId ?? '—');
          const data   = this.runs.map(r => (r.total ? Math.round(((r.passed || 0) / r.total) * 100) : 0));
          this.runsBarData = { labels, datasets: [{ label: '% pass du run', data }] };
          this.loadingRuns = false;
        },
        error: () => {
          this.runs = [];
          this.runsBarData = { labels: [], datasets: [{ label: '% pass du run', data: [] }] };
          this.loadingRuns = false;
        }
      });
  }

  private fetchRate(): void {
    this.loadingRate = true;
    this.api.getPassrate(this.project, this.days)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (pts) => {
          this.rate = pts ?? [];

          // KPI
          this.totalRuns   = this.rate.reduce((s, p) => s + (p.total || 0), 0);
          this.totalPassed = this.rate.reduce((s, p) => s + (p.passed || 0), 0);
          const totalFailed = this.totalRuns - this.totalPassed;
          this.passPct = this.totalRuns ? Math.round((this.totalPassed / this.totalRuns) * 100) : 0;

          // Donut
          this.pieData = {
            labels: ['Passés', 'Échoués'],
            datasets: [{ data: [this.totalPassed, totalFailed], borderWidth: 0, backgroundColor: ['#2ecc71', '#e74c3c'] }]
          };

          // Line
          const labels = this.rate.map(p => p.day);
          const data   = this.rate.map(p => p.total ? Math.round((p.passed / p.total) * 100) : 0);
          this.lineData = { labels, datasets: [{ label: 'Passrate (%)', data, tension: 0.35, pointRadius: 3, fill: false }] };

          this.loadingRate = false;
        },
        error: () => {
          this.rate = [];
          this.totalRuns = this.totalPassed = this.passPct = 0;
          this.pieData  = { labels: ['Passés', 'Échoués'], datasets: [{ data: [0, 0], borderWidth: 0 }] };
          this.lineData = { labels: [], datasets: [{ label: 'Passrate (%)', data: [], tension: 0.35, pointRadius: 3, fill: false }] };
          this.loadingRate = false;
        }
      });
  }

  private fetchCases(): void {
    this.loadingCases = true;
    const tool = this.fTool === 'all' ? undefined : (this.fTool === 'restAssured' ? 'restAssured' : this.fTool);
    const status = this.fStatus === 'all' ? undefined : this.fStatus;

    this.api.searchCases(this.project, this.page, this.size, { tool, status })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => { this.casesResp = res; this.loadingCases = false; },
        error: () => {
          this.casesResp = { page: 0, size: this.size, total: 0, items: [] };
          this.loadingCases = false;
        }
      });
  }

  private fetchToolRates(): void {
    this.loadingTool = true;
    this.api.getToolRates(this.project, this.days)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: rows => {
          this.toolRates = rows ?? [];
          const labels = this.toolRates.map(r => r.tool || '—');
          const data   = this.toolRates.map(r => r.total ? Math.round((r.passed / r.total) * 100) : 0);
          this.toolBarData = {
            labels,
            datasets: [{ label: '% pass', data ,
              backgroundColor: ['#6366f1', '#22c55e', '#f59e0b', '#ef4444', '#06b6d4'],
              borderRadius: 8,
              maxBarThickness: 28
            }]
          };

          this.loadingTool = false;
        },
        error: () => {
          this.toolRates = [];
          this.toolBarData = { labels: [], datasets: [{ label: '% pass', data: [] }] };
          this.loadingTool = false;
        }
      });


  }

  private fetchTypeRates(): void {
    this.loadingType = true;
    const status = this.fStatus === 'all' ? undefined : this.fStatus;
    const tool   = this.fTool   === 'all' ? undefined : (this.fTool === 'restAssured' ? 'restAssured' : this.fTool);

    this.api.getTypeRates(this.project, this.days, status, tool)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: rows => {
          this.typeRates = rows ?? [];
          const labels   = this.typeRates.map(r => r.type || '—');
          const passed   = this.typeRates.map(r => r.passed || 0);
          const failed   = this.typeRates.map(r => (r.total || 0) - (r.passed || 0));
          this.stackedTypeData = {
            labels,
            datasets: [
              { label: 'Passés',  data: passed, stack: 's', backgroundColor: '#2ecc71' },
              { label: 'Échoués', data: failed, stack: 's', backgroundColor: '#e74c3c' }
            ]
          };
          this.loadingType = false;
        },
        error: () => {
          this.typeRates = [];
          this.stackedTypeData = { labels: [], datasets: [{ label: 'Passés', data: [] }, { label: 'Échoués', data: [] }] as any };
          this.loadingType = false;
        }
      });
  }

  private fetchTopFails(): void {
    this.api.getTopFails(this.project, this.days, 5)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: rows => this.topFails = rows ?? [],
        error: () => this.topFails = []
      });
  }

  private fetchFlaky(): void {
    this.api.getFlaky(this.project, this.days, 5)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: rows => this.flaky = rows ?? [],
        error: () => this.flaky = []
      });
  }

  // ====== Helpers ======

  asDate(iso?: string | null): string {
    if (!iso) return '-';
    const d = new Date(iso);
    return isNaN(d.getTime()) ? '-' : d.toLocaleString();
  }

  statusClass(s?: string | null): string {
    if (s === 'passed') return 'badge pass';
    if (s === 'failed') return 'badge fail';
    return 'badge other';
  }

  // click sur une ligne -> modal
  openCaseDetail(c: CaseItem): void {
    this.dialog.open(CaseDetailDialogComponent, {
      width: '700px',
      data: c
    });
  }

  // pour {{ Math.* }} dans le template
  Math = Math;
}
