package ca.etsmtl.taf.dtoTestAPI;

import java.util.Map;

public class Auth {
    private String type;          // bearer|basic|header|none
    private String token;
    private String username;
    private String password;
    private Map<String,String> header;
    private RefreshConfig refresh;

    public Auth() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Map<String, String> getHeader() { return header; }
    public void setHeader(Map<String, String> header) { this.header = header; }

    public RefreshConfig getRefresh() { return refresh; }   // <-- AJOUTER
    public void setRefresh(RefreshConfig refresh) { this.refresh = refresh; }  // <-- AJOUTER
}
