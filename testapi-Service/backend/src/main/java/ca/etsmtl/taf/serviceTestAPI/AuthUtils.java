package ca.etsmtl.taf.serviceTestAPI;

import ca.etsmtl.taf.dtoTestAPI.Auth;
import ca.etsmtl.taf.dtoTestAPI.RefreshConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.function.Supplier;

public final class AuthUtils {
    private AuthUtils(){}

    public static Response executeWithAutoRefresh(RequestSpecification spec,
                                                  Supplier<Response> originalCall,
                                                  Auth auth,
                                                  Map<String,String> vars) {
        Response resp = originalCall.get();
        if (!isAuthExpired(resp)) return resp;

        if (auth == null || auth.getRefresh() == null) return resp; // pas de refresh → on rend la 401/403

        if (!refreshToken(auth, vars)) return resp; // refresh KO → on rend la 401/403

        // réapplique le bearer et retente 1 fois
        reapplyBearerHeader(spec, auth, vars);
        return originalCall.get();
    }

    private static boolean isAuthExpired(Response r) {
        int sc = r.statusCode();
        return sc == 401 || sc == 403;
    }

    private static void reapplyBearerHeader(RequestSpecification spec, Auth auth, Map<String,String> vars) {
        if (auth == null || auth.getType()==null) return;
        if (!"bearer".equalsIgnoreCase(auth.getType())) return;

        RefreshConfig rc = auth.getRefresh();
        String token = interpolate(auth.getToken(), vars);
        if ((token == null || token.isBlank()) && rc!=null && rc.getTokenVarKey()!=null) {
            token = vars.get(rc.getTokenVarKey());
        }
        if (token != null && !token.isBlank()) {
            String headerName = rc!=null && rc.getHeaderName()!=null ? rc.getHeaderName() : "Authorization";
            String scheme = rc!=null && rc.getScheme()!=null ? rc.getScheme() : "Bearer";
            spec.header(headerName, scheme + " " + token);
        }
    }

    private static boolean refreshToken(Auth auth, Map<String,String> vars) {
        RefreshConfig rc = auth.getRefresh();
        if (rc == null || rc.getRefreshUrl()==null || rc.getAccessTokenJsonPath()==null) return false;

        RequestSpecification r = RestAssured.given();
        if (rc.getHeaders()!=null) rc.getHeaders().forEach((k,v)-> r.header(k, interpolate(v, vars)));
        if (rc.getBodyTemplate()!=null) r.body(interpolate(rc.getBodyTemplate(), vars));

        String url = interpolate(rc.getRefreshUrl(), vars);
        String method = rc.getMethod()==null ? "POST" : rc.getMethod().toUpperCase();
        Response resp = switch (method) {
            case "GET"   -> r.when().get(url);
            case "POST"  -> r.when().post(url);
            case "PUT"   -> r.when().put(url);
            case "PATCH" -> r.when().patch(url);
            default      -> r.when().post(url);
        };
        if (resp.statusCode() / 100 != 2) return false;

        // récupère nouveaux tokens
        String newAccess = String.valueOf(resp.jsonPath().get(rc.getAccessTokenJsonPath()));
        if (newAccess == null || newAccess.equals("null") || newAccess.isBlank()) return false;

        String key = rc.getTokenVarKey()==null ? "jwt" : rc.getTokenVarKey();
        vars.put(key, newAccess);
        auth.setToken(newAccess); // utile si applyAuth lit encore auth.token

        if (rc.getRefreshTokenJsonPath()!=null) {
            Object v = resp.jsonPath().get(rc.getRefreshTokenJsonPath());
            if (v != null) vars.put("refreshToken", String.valueOf(v));
        }
        return true;
    }

    private static String interpolate(String s, Map<String,String> vars){
        if (s==null) return null;
        String out = s;
        for (var e : vars.entrySet()) out = out.replace("${"+e.getKey()+"}", e.getValue()==null? "" : e.getValue());
        return out;
    }
}