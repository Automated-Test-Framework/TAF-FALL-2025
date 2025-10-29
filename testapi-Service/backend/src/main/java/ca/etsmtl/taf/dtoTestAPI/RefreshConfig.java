package ca.etsmtl.taf.dtoTestAPI;

import java.util.Map;

public class RefreshConfig {
    private String refreshUrl;                // ex: https://auth.example.com/refresh
    private String method = "POST";           // POST/GET/PUT/PATCH
    private Map<String,String> headers;       // ex: Content-Type: application/json
    private String bodyTemplate;              // ex: {"refresh_token":"${refreshToken}"}
    private String accessTokenJsonPath;       // ex: $.access_token
    private String refreshTokenJsonPath;      // optionnel: $.refresh_token
    private String headerName = "Authorization";
    private String scheme = "Bearer";
    private String tokenVarKey = "jwt";       // où stocker le nouveau token dans vars

    public RefreshConfig() {}

    public String getRefreshUrl() { return refreshUrl; }
    public void setRefreshUrl(String refreshUrl) { this.refreshUrl = refreshUrl; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public String getBodyTemplate() { return bodyTemplate; }
    public void setBodyTemplate(String bodyTemplate) { this.bodyTemplate = bodyTemplate; }
    public String getAccessTokenJsonPath() { return accessTokenJsonPath; }
    public void setAccessTokenJsonPath(String accessTokenJsonPath) { this.accessTokenJsonPath = accessTokenJsonPath; }
    public String getRefreshTokenJsonPath() { return refreshTokenJsonPath; }
    public void setRefreshTokenJsonPath(String refreshTokenJsonPath) { this.refreshTokenJsonPath = refreshTokenJsonPath; }
    public String getHeaderName() { return headerName; }
    public void setHeaderName(String headerName) { this.headerName = headerName; }
    public String getScheme() { return scheme; }
    public void setScheme(String scheme) { this.scheme = scheme; }
    public String getTokenVarKey() { return tokenVarKey; }
    public void setTokenVarKey(String tokenVarKey) { this.tokenVarKey = tokenVarKey; }
}
