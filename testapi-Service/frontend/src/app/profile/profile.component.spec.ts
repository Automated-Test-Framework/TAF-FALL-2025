import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { ProfileComponent } from './profile.component';
import { TokenStorageService } from '../_services/token-storage.service';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;

  const mockTokenService = {
    getUser: () => ({
      fullName: 'Test User',
      email: 'test@test.com',
      accessToken: 'mock-access-token-1234567890abcdefghijklmnopqrstuvwxyz',
      roles: ['ROLE_USER']
    })
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProfileComponent ],
      providers: [ { provide: TokenStorageService, useValue: mockTokenService } ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
