import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-case-detail-dialog',
  templateUrl: './case-detail-dialog.component.html',
  styleUrls: ['./case-detail-dialog.component.css']
})
export class CaseDetailDialogComponent {

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<CaseDetailDialogComponent>
  ) {}

  close(): void {
    this.dialogRef.close();
  }

  get statusClass(): string {
    const s = this.data?.status?.toLowerCase();
    if (s === 'passed') return 'badge pass';
    if (s === 'failed') return 'badge fail';
    return 'badge';
  }
}
