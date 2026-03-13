export interface testModel2 {
    id: number;
    mongoId?: string;
    method: string;
    apiUrl: string;
    headers: { [key: string]: string };
    responseTime?: number;
    actualResponseTime?: number;
    input?: string;
    expectedOutput?: string;
    statusCode?: number;
    responseStatus?: boolean;
    messages?: string[];
    expectedHeaders:  { [key: string]: string };
    pending?: boolean;
}
