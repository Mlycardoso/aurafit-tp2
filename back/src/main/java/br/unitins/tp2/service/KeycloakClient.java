package br.unitins.tp2.service;

import java.util.List;

public interface KeycloakClient {
    Token login(String email, String password);
    Token refresh(String refreshToken);
    void logout(String refreshToken);
    UserAccount createUser(String name, String email, String password, boolean temporaryPassword);
    UserAccount findUser(String id);
    UserAccount findUserByEmail(String email);
    void updateUser(String id, String name, String email, boolean enabled);
    void disableUser(String id);
    List<String> listRoles();
    List<String> listUserRoles(String id);
    void setRoles(String userId, List<String> roles);

    record Token(String accessToken, String refreshToken, long expiresIn) {}
    record UserAccount(String id, String name, String email, boolean enabled) {}
}
