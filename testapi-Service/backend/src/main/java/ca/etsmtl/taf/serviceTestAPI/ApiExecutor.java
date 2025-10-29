package ca.etsmtl.taf.serviceTestAPI;

import ca.etsmtl.taf.dtoTestAPI.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public class ApiExecutor {

    public RunResult run(ApiSuite suite) {
        Map<String,String> vars = new HashMap<>();
        if (suite.getVariables()!=null) vars.putAll(suite.getVariables());

        RunResult out = new RunResult(suite.getName()!=null ? suite.getName() : "suite");

        for (ApiTest t : suite.getTests()) {
            long t0 = System.nanoTime();
            int http = -1; boolean ok=false; String err=null;

            try {
                var spec = RestAssured.given();

                if (t.getRequest().getHeaders()!=null)
                    t.getRequest().getHeaders().forEach((k,v)-> spec.header(k, interp(v, vars)));

                applyAuth(spec, suite.getAuth(), vars);

                if (t.getRequest().getQuery()!=null)
                    spec.queryParams(resolveMap(t.getRequest().getQuery(), vars));

                if (t.getRequest().getBody()!=null)
                    spec.body(interp(t.getRequest().getBody(), vars));

                String method = t.getRequest().getMethod().toUpperCase(Locale.ROOT);
                String url    = suite.getBaseUrl() + interp(t.getRequest().getPath(), vars);

               /* Response resp = switch (method) {
                    case "GET" -> spec.when().get(url);
                    case "POST" -> spec.when().post(url);
                    case "PUT" -> spec.when().put(url);
                    case "PATCH" -> spec.when().patch(url);
                    case "DELETE" -> spec.when().delete(url);
                    default -> throw new IllegalArgumentException("Unsupported method: " + method);
                };*/

                java.util.function.Supplier<Response> call = () -> {
                    switch (method) {
                        case "GET":    return spec.when().get(url);
                        case "POST":   return spec.when().post(url);
                        case "PUT":    return spec.when().put(url);
                        case "PATCH":  return spec.when().patch(url);
                        case "DELETE": return spec.when().delete(url);
                        default: throw new IllegalArgumentException("Unsupported method: " + method);
                    }
                };

                Response resp = AuthUtils.executeWithAutoRefresh(
                        spec, call, suite.getAuth(), vars
                );

                http = resp.statusCode();

                Expect exp = t.getExpect();
                if (exp != null) {
                    if (exp.getStatus()!=null) Assertions.assertEquals(exp.getStatus().intValue(), http, "HTTP status");

                    if (exp.getHeaders()!=null) {
                        for (var e : exp.getHeaders().entrySet()) {
                            Assertions.assertEquals(interp(e.getValue(), vars), resp.header(e.getKey()), "Header "+e.getKey());
                        }
                    }
                    if (exp.getJson()!=null) {
                        for (var e : exp.getJson().entrySet()) {
                            String jp = e.getKey();
                            Object rule = e.getValue();
                            Object value = resp.jsonPath().get(jp);
                            assertRule(jp, rule, value, vars);
                        }
                    }
                }

                if (t.getSave()!=null) {
                    for (var e : t.getSave().entrySet()) {
                        Object v = resp.jsonPath().get(e.getValue());
                        vars.put(e.getKey(), v==null? null : String.valueOf(v));
                    }
                }

                ok = true;
            } catch (Throwable ex) {
                ok = false; err = ex.getMessage();
            } finally {
                long ms = (System.nanoTime()-t0)/1_000_000;
                out.getTests().add(new StepResult(t.getName(), http, ok, err, ms));
            }
        }
        return out;
    }

    /* Helpers */
    private static String interp(String s, Map<String,String> vars) {
        if (s==null) return null;
        String out = s;
        for (var e : vars.entrySet()) {
            out = out.replace("${"+e.getKey()+"}", e.getValue()==null? "" : e.getValue());
        }
        return out;
    }

    private static Map<String,String> resolveMap(Map<String,String> m, Map<String,String> vars){
        Map<String,String> r = new HashMap<>();
        m.forEach((k,v)-> r.put(k, interp(v, vars)));
        return r;
    }

    private static void applyAuth(io.restassured.specification.RequestSpecification spec, Auth a, Map<String,String> vars){
        if (a==null || a.getType()==null) return;
        switch (a.getType()) {
            case "bearer": spec.auth().oauth2(interp(a.getToken(), vars)); break;
            case "basic":  spec.auth().preemptive().basic(interp(a.getUsername(), vars), interp(a.getPassword(), vars)); break;
            case "header":
                if (a.getHeader()!=null) a.getHeader().forEach((k,v)-> spec.header(k, interp(v, vars)));
                break;
            default: /* none */
        }
    }

    private static void assertRule(String jp, Object rule, Object value, Map<String,String> vars){
        if (rule instanceof String) {
            String s = interp((String) rule, vars);
            if ("present".equalsIgnoreCase(s)) { Assertions.assertNotNull(value, "present "+jp); return; }
            if ("absent".equalsIgnoreCase(s))  { Assertions.assertNull(value, "absent "+jp); return; }
            if (s.startsWith("contains:"))     { String needle = s.substring("contains:".length()).trim();
                Assertions.assertTrue(String.valueOf(value).contains(needle), jp+" contains "+needle); return; }
            if (s.matches("^[<>]=?\\s*\\d+$|^==\\s*\\d+$")) {
                long num = (value instanceof Number) ? ((Number)value).longValue() : Long.parseLong(String.valueOf(value));
                String op = s.replaceAll("\\s+","");
                long rhs = Long.parseLong(op.replaceAll("^[^\\d]+",""));
                if (op.startsWith(">=")) Assertions.assertTrue(num>=rhs, jp+" >= "+rhs);
                else if (op.startsWith("<=")) Assertions.assertTrue(num<=rhs, jp+" <= "+rhs);
                else if (op.startsWith(">"))  Assertions.assertTrue(num> rhs, jp+" > "+rhs);
                else if (op.startsWith("<"))  Assertions.assertTrue(num< rhs, jp+" < "+rhs);
                else                          Assertions.assertEquals(rhs, num, jp+" == "+rhs);
                return;
            }
            Assertions.assertEquals(s, String.valueOf(value), jp+" equals");
            return;
        }
        Assertions.assertEquals(rule, value, "Exact match "+jp);
    }
}
