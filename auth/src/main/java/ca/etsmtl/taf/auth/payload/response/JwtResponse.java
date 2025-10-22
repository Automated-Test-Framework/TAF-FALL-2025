package ca.etsmtl.taf.auth.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String refresh;
    private String type = "Bearer";
    private String id;
    private String fullName;
    private String username;
    private String email;
    private List<String> roles;

    // Full constructor (for BasicAuthStrategy and JWT flow)
    public JwtResponse(String token, String refresh, String type,
                       String id, String fullName, String username,
                       String email, List<String> roles) {
        this.token = token;
        this.refresh = refresh;
        this.type = type != null ? type : "Bearer";
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.roles = roles != null ? roles : Collections.emptyList();
    }

    // Existing 7-arg constructor (still valid for existing JWT flow)
    public JwtResponse(String accessToken, String refresh, String id,
                       String fullName, String username, String email,
                       List<String> roles) {
        this(accessToken, refresh, "Bearer", id, fullName, username, email, roles);
    }

    // Convenience constructor used by strategies that only return a token
    public JwtResponse(String token) {
        this(token, null, "Bearer", null, null, null, null, Collections.emptyList());
    }
}
