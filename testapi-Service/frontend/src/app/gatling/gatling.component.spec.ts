import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { GatlingComponent } from './gatling.component';

describe('GatlingComponent', () => {
  let component: GatlingComponent;
  let fixture: ComponentFixture<GatlingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GatlingComponent ],
      imports: [ HttpClientTestingModule ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GatlingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
