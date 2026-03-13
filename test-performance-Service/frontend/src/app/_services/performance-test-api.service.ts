import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { JMeterHttpRequest } from '../performance-test-api/jmeter-api/jmeter-http-request';
import { JMeterFTPRequest } from '../performance-test-api/jmeter-api/jmeter-ftp-request';

import { GatlingRequest } from '../performance-test-api/gatling-api/gatling-request';

const GATLING_API = `${environment.apiUrl}/team3/api/performance/gatling/runSimulation`;
const LATEST_REPORT_API = `${environment.apiUrl}/team3/api/performance/gatling/latest-report`;
const JMeter_HttpRequest_API = `${environment.apiUrl}/team3/api/performance/jmeter/http`;
const JMeter_FtpRequest_API = `${environment.apiUrl}/team3/api/performance/jmeter/ftp`;
const TEST_GENERATION_API = `http://localhost:8096/api/performance/test-generation/jmeter`;
console.log('API URL:', TEST_GENERATION_API);
const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class PerformanceTestApiService {
  constructor(private http: HttpClient) { }

  sendGatlingRequest(request: GatlingRequest): Observable<any> {
    const url = `${GATLING_API}`;
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    const response = this.http.post(url, request, httpOptions);
    return response;
  }

  getLatestReportUrl(): string {
    return LATEST_REPORT_API;
  }

  sendHttpJMeterRequest(
    jmeter_http_request: JMeterHttpRequest
  ): Observable<any> {
    const url = `${JMeter_HttpRequest_API}`;
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    return this.http.post(url, jmeter_http_request, httpOptions);
  }

  sendFtpJMeterRequest(jmeter_ftp_request: JMeterFTPRequest): Observable<any> {
    const url = `${JMeter_FtpRequest_API}`;
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    return this.http.post(url, jmeter_ftp_request, httpOptions);
  }

  generateTestPlan(prompt: string): Observable<any> {
    return this.http.post(TEST_GENERATION_API, { prompt }, httpOptions);
  }
}
