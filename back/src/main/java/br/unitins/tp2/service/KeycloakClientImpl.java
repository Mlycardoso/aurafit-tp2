package br.unitins.tp2.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KeycloakClientImpl implements KeycloakClient {
    @ConfigProperty(name = "keycloak.server-url") String serverUrl;
    @ConfigProperty(name = "keycloak.realm", defaultValue = "aurafit") String realm;
    @ConfigProperty(name = "keycloak.api-client-id", defaultValue = "aurafit-api") String apiClientId;
    @ConfigProperty(name = "keycloak.api-client-secret") String apiSecret;
    @ConfigProperty(name = "keycloak.admin-client-id", defaultValue = "aurafit-admin") String adminClientId;
    @ConfigProperty(name = "keycloak.admin-client-secret") String adminSecret;
    @Inject ObjectMapper mapper;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build();

    private String realmUrl() { return serverUrl.replaceAll("/$", "") + "/realms/" + realm; }
    private String adminUrl() { return serverUrl.replaceAll("/$", "") + "/admin/realms/" + realm; }

    @Override public Token login(String email, String password) {
        return token(Map.of("grant_type", "password", "client_id", apiClientId,
                "client_secret", apiSecret, "username", email, "password", password), true);
    }

    @Override public Token refresh(String refreshToken) {
        return token(Map.of("grant_type", "refresh_token", "client_id", apiClientId,
                "client_secret", apiSecret, "refresh_token", refreshToken), true);
    }

    @Override public void logout(String refreshToken) {
        send("POST", realmUrl() + "/protocol/openid-connect/logout", null,
                form(Map.of("client_id", apiClientId, "client_secret", apiSecret, "refresh_token", refreshToken)),
                "application/x-www-form-urlencoded", 204);
    }

    @Override public UserAccount createUser(String name, String email, String password, boolean temporaryPassword) {
        Map<String, Object> credentials = Map.of("type", "password", "value", password, "temporary", temporaryPassword);
        Map<String, Object> account = new LinkedHashMap<>();
        account.put("username", email.toLowerCase(Locale.ROOT));
        account.put("email", email.toLowerCase(Locale.ROOT));
        account.put("firstName", name);
        account.put("enabled", true);
        account.put("emailVerified", true);
        account.put("credentials", List.of(credentials));
        if (temporaryPassword) account.put("requiredActions", List.of("UPDATE_PASSWORD"));
        HttpResponse<String> response = send("POST", adminUrl() + "/users", adminToken(), json(account), "application/json", 201);
        String location = response.headers().firstValue("Location").orElse("");
        String id = location.substring(location.lastIndexOf('/') + 1);
        if (id.isBlank()) throw new IllegalStateException("Keycloak não retornou o identificador do usuário.");
        return new UserAccount(id, name, email.toLowerCase(Locale.ROOT), true);
    }

    @Override public UserAccount findUser(String id) {
        JsonNode user = json(send("GET", adminUrl() + "/users/" + segment(id), adminToken(), null, null, 200));
        String name = (user.path("firstName").asText("") + " " + user.path("lastName").asText("")).trim();
        if (name.isBlank()) name = user.path("username").asText();
        return new UserAccount(user.path("id").asText(), name, user.path("email").asText(), user.path("enabled").asBoolean());
    }

    @Override public UserAccount findUserByEmail(String email) {
        String url = adminUrl() + "/users?email=" + segment(email.toLowerCase(Locale.ROOT)) + "&exact=true";
        JsonNode users = json(send("GET", url, adminToken(), null, null, 200));
        if (!users.isArray() || users.isEmpty()) return null;
        JsonNode user = users.get(0);
        String name = (user.path("firstName").asText("") + " " + user.path("lastName").asText("")).trim();
        if (name.isBlank()) name = user.path("username").asText();
        return new UserAccount(user.path("id").asText(), name, user.path("email").asText(), user.path("enabled").asBoolean());
    }

    @Override public void updateUser(String id, String name, String email, boolean enabled) {
        String[] parts = name.strip().split("\\s+", 2);
        Map<String, Object> user = Map.of("username", email.toLowerCase(Locale.ROOT), "email", email.toLowerCase(Locale.ROOT),
                "firstName", parts[0], "lastName", parts.length == 2 ? parts[1] : "", "enabled", enabled);
        send("PUT", adminUrl() + "/users/" + segment(id), adminToken(), json(user), "application/json", 204);
    }

    @Override public void disableUser(String id) {
        UserAccount current = findUser(id);
        updateUser(id, current.name(), current.email(), false);
    }

    @Override public List<String> listRoles() {
        JsonNode roles = json(send("GET", adminUrl() + "/roles", adminToken(), null, null, 200));
        List<String> names = new ArrayList<>();
        roles.forEach(role -> names.add(role.path("name").asText()));
        return names;
    }

    @Override public List<String> listUserRoles(String id) {
        JsonNode roles = json(send("GET", adminUrl() + "/users/" + segment(id) + "/role-mappings/realm", adminToken(), null, null, 200));
        List<String> names = new ArrayList<>();
        roles.forEach(role -> names.add(role.path("name").asText()));
        return names;
    }

    @Override public void setRoles(String userId, List<String> roleNames) {
        List<JsonNode> remove = new ArrayList<>();
        for (String existing : listUserRoles(userId)) {
            if ((existing.equals("ADMIN") || existing.equals("CLIENTE")) && !roleNames.contains(existing)) {
                remove.add(json(send("GET", adminUrl() + "/roles/" + segment(existing), adminToken(), null, null, 200)));
            }
        }
        if (!remove.isEmpty()) send("DELETE", adminUrl() + "/users/" + segment(userId) + "/role-mappings/realm",
                adminToken(), json(remove), "application/json", 204);
        List<JsonNode> representations = new ArrayList<>();
        for (String role : roleNames) {
            JsonNode json = json(send("GET", adminUrl() + "/roles/" + segment(role), adminToken(), null, null, 200));
            representations.add(json);
        }
        send("POST", adminUrl() + "/users/" + segment(userId) + "/role-mappings/realm", adminToken(),
                json(representations), "application/json", 204);
    }

    private Token token(Map<String, String> parameters, boolean userGrant) {
        HttpResponse<String> response = send("POST", realmUrl() + "/protocol/openid-connect/token", null,
                form(parameters), "application/x-www-form-urlencoded", 200, userGrant ? 400 : -1);
        if (response.statusCode() != 200) throw new KeycloakException(401, "Credenciais inválidas.");
        JsonNode json = json(response.body());
        return new Token(json.path("access_token").asText(), json.path("refresh_token").asText(), json.path("expires_in").asLong());
    }

    private String adminToken() {
        HttpResponse<String> response = send("POST", realmUrl() + "/protocol/openid-connect/token", null,
                form(Map.of("grant_type", "client_credentials", "client_id", adminClientId, "client_secret", adminSecret)),
                "application/x-www-form-urlencoded", 200);
        return json(response.body()).path("access_token").asText();
    }

    private HttpResponse<String> send(String method, String url, String bearer, String body, String contentType, int... expected) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(java.time.Duration.ofSeconds(12)).header("Accept", "application/json");
            if (bearer != null) request.header("Authorization", "Bearer " + bearer);
            if (contentType != null) request.header("Content-Type", contentType);
            request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            for (int status : expected) if (response.statusCode() == status) return response;
            if (response.statusCode() == 409) throw new KeycloakException(409, "Já existe um usuário com este e-mail.");
            throw new KeycloakException(response.statusCode() >= 400 && response.statusCode() < 500 ? response.statusCode() : 502,
                    "Falha na comunicação com o provedor de identidade.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A comunicação com o provedor de identidade foi interrompida.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível comunicar com o provedor de identidade.", e);
        }
    }

    private JsonNode json(HttpResponse<String> response) { return json(response.body()); }
    private JsonNode json(String value) {
        try { return mapper.readTree(value); }
        catch (IOException e) { throw new IllegalStateException("Resposta inválida do provedor de identidade.", e); }
    }
    private String json(Object value) {
        try { return mapper.writeValueAsString(value); }
        catch (IOException e) { throw new IllegalStateException("Não foi possível preparar a requisição ao provedor de identidade.", e); }
    }
    private static String form(Map<String, String> values) {
        return values.entrySet().stream().map(e -> encode(e.getKey()) + "=" + encode(e.getValue())).reduce((a, b) -> a + "&" + b).orElse("");
    }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private static String segment(String value) { return encode(value); }
}
