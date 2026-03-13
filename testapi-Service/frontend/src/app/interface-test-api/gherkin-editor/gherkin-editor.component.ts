import { Component, EventEmitter, Input, Output, OnInit, ViewChild, ElementRef, AfterViewChecked, ViewEncapsulation } from '@angular/core';
import { GherkinParserService } from '../../_services/gherkin-parser.service';
import { testModel2 } from '../../models/testmodel2';

@Component({
  selector: 'app-gherkin-editor',
  templateUrl: './gherkin-editor.component.html',
  styleUrls: ['./gherkin-editor.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class GherkinEditorComponent implements OnInit, AfterViewChecked {

  @Input() initialGherkin: string = '';
  @Output() testsReady = new EventEmitter<testModel2[]>();
  @Output() closeEditor = new EventEmitter<void>();

  @ViewChild('editorTextarea') editorTextarea!: ElementRef<HTMLTextAreaElement>;
  @ViewChild('highlightPre') highlightPre!: ElementRef<HTMLPreElement>;

  gherkinText: string = '';
  parsedTests: testModel2[] = [];
  parseErrors: string[] = [];
  showPreview: boolean = false;
  highlightedHtml: string = '';
  private needsSync = false;

  constructor(private gherkinParser: GherkinParserService) {}

  ngOnInit(): void {
    if (this.initialGherkin) {
      this.gherkinText = this.initialGherkin;
    } else {
      this.gherkinText = this.gherkinParser.getSampleTemplate();
    }
    this.onTextChange();
  }

  ngAfterViewChecked(): void {
    if (this.needsSync) {
      this.syncScroll();
      this.needsSync = false;
    }
  }

  onTextChange(): void {
    this.needsSync = true;
    this.highlightedHtml = this.getHighlightedHtml();
    const result = this.gherkinParser.parse(this.gherkinText);
    this.parsedTests = result.tests;
    this.parseErrors = result.errors;
  }

  /** Sync scroll position between textarea and highlight layer */
  syncScroll(): void {
    if (this.editorTextarea && this.highlightPre) {
      this.highlightPre.nativeElement.scrollTop = this.editorTextarea.nativeElement.scrollTop;
      this.highlightPre.nativeElement.scrollLeft = this.editorTextarea.nativeElement.scrollLeft;
    }
  }

  /** Return highlighted HTML from raw Gherkin text */
  getHighlightedHtml(): string {
    if (!this.gherkinText) return '';
    return this.gherkinText
      // 1. HTML escape
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      // 2. Comments (whole lines — before anything else)
      .replace(/^(\s*#.*)$/gm, '<span class="gh-comment">$1</span>')
      // 3. Tags (@tag)
      .replace(/^(\s*@\S+)/gm, '<span class="gh-tag">$1</span>')
      // 4. Strings MUST come before keywords/steps to avoid matching class attributes
      .replace(/"([^"]*)"/g, '<span class="gh-string">"$1"</span>')
      .replace(/'([^']*)'/g, '<span class="gh-string">\'$1\'</span>')
      // 5. Numbers (standalone)
      .replace(/\b(\d+)\b/g, '<span class="gh-number">$1</span>')
      // 6. Keywords (after strings, so class="..." won't be re-matched)
      .replace(/^(\s*)(Feature:)/gm, '$1<span class="gh-keyword">$2</span>')
      .replace(/^(\s*)(Scenario:)/gm, '$1<span class="gh-keyword">$2</span>')
      .replace(/^(\s*)(Scenario Outline:)/gm, '$1<span class="gh-keyword">$2</span>')
      .replace(/^(\s*)(Background:)/gm, '$1<span class="gh-keyword">$2</span>')
      // 7. Step keywords (last)
      .replace(/^(\s*)(Given|When|Then|And|But)\b/gm, '$1<span class="gh-step">$2</span>');
  }

  /** Load template example */
  loadTemplate(): void {
    this.gherkinText = this.gherkinParser.getSampleTemplate();
    this.onTextChange();
  }

  /** Toggle preview panel */
  togglePreview(): void {
    this.showPreview = !this.showPreview;
  }

  /** Import .feature file */
  importFeature(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.gherkinText = e.target.result;
      this.onTextChange();
    };
    reader.readAsText(file);
    // Reset input so the same file can be re-imported
    input.value = '';
  }

  /** Export current Gherkin as .feature file */
  exportFeature(): void {
    const blob = new Blob([this.gherkinText], { type: 'text/plain;charset=utf-8' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'api-tests.feature';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  /** Emit parsed tests to parent and close editor */
  applyTests(): void {
    if (this.parsedTests.length === 0) return;
    this.testsReady.emit(this.parsedTests);
  }

  /** Close without applying */
  cancel(): void {
    this.closeEditor.emit();
  }

  /** Helper for template: Object.keys() */
  objectKeys(obj: { [key: string]: string }): string[] {
    return obj ? Object.keys(obj) : [];
  }

  /** Handle Tab key for indentation */
  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Tab') {
      event.preventDefault();
      const textarea = this.editorTextarea.nativeElement;
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      this.gherkinText = this.gherkinText.substring(0, start) + '  ' + this.gherkinText.substring(end);
      // Restore cursor position after Angular updates the view
      setTimeout(() => {
        textarea.selectionStart = textarea.selectionEnd = start + 2;
      }, 0);
      this.onTextChange();
    }
  }
}
