import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { GatlingApiComponent } from './gatling-api.component';

describe('GatlingApiComponent', () => {
  let component: GatlingApiComponent;
  let fixture: ComponentFixture<GatlingApiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GatlingApiComponent ],
      imports: [ HttpClientTestingModule ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GatlingApiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
