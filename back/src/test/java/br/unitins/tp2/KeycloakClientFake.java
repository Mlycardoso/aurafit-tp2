package br.unitins.tp2;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import br.unitins.tp2.service.KeycloakClient;
import br.unitins.tp2.service.KeycloakException;

@Alternative
@Priority(1)
@ApplicationScoped
class KeycloakClientFake implements KeycloakClient {
    private final Map<String, UserAccount> users = new ConcurrentHashMap<>();
    private final Map<String, List<String>> roles = new ConcurrentHashMap<>();

    @Override public Token login(String email, String password) {
        if (password == null || password.isBlank() || "wrong".equals(password)) throw new KeycloakException(401, "Credenciais inválidas.");
        UserAccount account = findUserByEmail(email);
        if (account == null) {
            account = new UserAccount(UUID.randomUUID().toString(), email, email, true);
            users.put(account.id(), account);
            roles.put(account.id(), List.of("ADMIN"));
        }
        return new Token("test-access-token", "test-refresh-token", 300);
    }
    @Override public Token refresh(String refreshToken) {
        if (!"test-refresh-token".equals(refreshToken)) throw new KeycloakException(401, "Refresh token inválido.");
        return new Token("test-access-token-renovado", "test-refresh-token-renovado", 300);
    }
    @Override public void logout(String refreshToken) {}
    @Override public UserAccount createUser(String name, String email, String password, boolean temporaryPassword) {
        if (users.values().stream().anyMatch(u -> u.email().equalsIgnoreCase(email))) throw new KeycloakException(409, "E-mail duplicado.");
        String id = UUID.randomUUID().toString();
        UserAccount account = new UserAccount(id, name, email, true);
        users.put(id, account);
        return account;
    }
    @Override public UserAccount findUser(String id) { return users.get(id); }
    @Override public UserAccount findUserByEmail(String email) { return users.values().stream().filter(u -> u.email().equalsIgnoreCase(email)).findFirst().orElse(null); }
    @Override public void updateUser(String id, String name, String email, boolean enabled) {
        users.put(id, new UserAccount(id, name, email, enabled));
    }
    @Override public void disableUser(String id) { updateUser(id, users.get(id).name(), users.get(id).email(), false); }
    @Override public List<String> listRoles() { return List.of("ADMIN", "CLIENTE"); }
    @Override public List<String> listUserRoles(String id) { return roles.getOrDefault(id, List.of()); }
    @Override public void setRoles(String userId, List<String> value) { roles.put(userId, List.copyOf(value)); }
}
