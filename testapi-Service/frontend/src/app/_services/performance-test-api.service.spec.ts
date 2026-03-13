/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PerformanceTestApiService } from './performance-test-api.service';

describe('Service: PerformanceTestApi', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PerformanceTestApiService]
    });
  });

  it('should ...', inject([PerformanceTestApiService], (service: PerformanceTestApiService) => {
    expect(service).toBeTruthy();
  }));
});
