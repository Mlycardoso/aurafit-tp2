package br.unitins.tp2.service;

import java.util.List;
import java.util.Locale;

import br.unitins.tp2.dto.AuthDTOs;
import br.unitins.tp2.dto.ClienteRequestDTO;
import br.unitins.tp2.dto.ClienteResponseDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Perfil;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.SecurityContext;

@ApplicationScoped
public class AuthService {
    @Inject KeycloakClient keycloak;
    @Inject UsuarioRepository usuarios;
    @Inject ClienteService clientes;

    @Transactional
    public AuthDTOs.TokenResponse login(String email, String password) {
        var tokens = keycloak.login(email.strip().toLowerCase(Locale.ROOT), password);
        var account = keycloak.findUserByEmail(email);
        if (account == null) throw new KeycloakException(401, "Credenciais inválidas.");
        List<String> roles = keycloak.listUserRoles(account.id());
        Usuario local = usuarios.find("keycloakId", account.id()).firstResult();
        if (local == null) {
            local = new Usuario();
            local.keycloakId = account.id();
            local.nome = account.name();
            local.email = account.email();
            local.ativo = account.enabled();
            for (String role : roles) if (role.equals("ADMIN") || role.equals("CLIENTE"))
                local.perfis.add(br.unitins.tp2.model.Perfil.valueOf(role));
            usuarios.persistAndFlush(local);
        } else {
            local.perfis.clear();
            for (String role : roles) if (role.equals("ADMIN") || role.equals("CLIENTE"))
                local.perfis.add(br.unitins.tp2.model.Perfil.valueOf(role));
            local.ativo = account.enabled();
        }
        return new AuthDTOs.TokenResponse(tokens.accessToken(), tokens.refreshToken(), tokens.expiresIn(),
                toAuthUser(local, account.name(), account.email(), roles));
    }

    public AuthDTOs.TokenResponse refresh(String refreshToken) {
        var tokens = keycloak.refresh(refreshToken);
        return new AuthDTOs.TokenResponse(tokens.accessToken(), tokens.refreshToken(), tokens.expiresIn(), null);
    }

    public void logout(String refreshToken) { keycloak.logout(refreshToken); }

    @Transactional
    public AuthDTOs.TokenResponse register(AuthDTOs.RegistroRequest request) {
        ClienteRequestDTO customer = request.cliente();
        String email = customer.email().strip().toLowerCase(Locale.ROOT);
        if (usuarios.find("lower(email)", email).firstResult() != null)
            throw new KeycloakException(409, "Já existe um usuário com este e-mail.");
        var account = keycloak.createUser(customer.nome(), email, request.senha(), false);
        keycloak.setRoles(account.id(), List.of(Perfil.CLIENTE.name()));
        var normalized = new ClienteRequestDTO(customer.nome().strip(), email, customer.telefone(), customer.cep(),
                customer.logradouro(), customer.bairro(), customer.numero(), customer.complemento(), customer.cpf(),
                customer.dataNascimento(), true, customer.telefones());
        var cliente = clientes.create(normalized);
        Usuario usuario = new Usuario();
        usuario.nome = customer.nome().strip();
        usuario.email = email;
        usuario.ativo = true;
        usuario.keycloakId = account.id();
        usuario.cliente = cliente;
        usuario.perfis.add(Perfil.CLIENTE);
        usuarios.persistAndFlush(usuario);
        var tokens = keycloak.login(email, request.senha());
        return new AuthDTOs.TokenResponse(tokens.accessToken(), tokens.refreshToken(), tokens.expiresIn(),
                toAuthUser(usuario, usuario.nome, usuario.email, List.of("CLIENTE")));
    }

    @Transactional
    public AuthDTOs.UsuarioAuth me(SecurityContext context) {
        String id = context.getUserPrincipal() == null ? null : context.getUserPrincipal().getName();
        Usuario local = id == null ? null : usuarios.find("keycloakId", id).firstResult();
        String email = local == null ? null : local.email;
        String name = local == null ? (id == null ? "" : id) : local.nome;
        List<String> roles = context.getUserPrincipal() == null ? List.of() :
                java.util.stream.Stream.of("ADMIN", "CLIENTE").filter(context::isUserInRole).toList();
        return toAuthUser(local, name, email, roles);
    }

    private AuthDTOs.UsuarioAuth toAuthUser(Usuario local, String name, String email, List<String> roles) {
        ClienteResponseDTO cliente = local == null || local.cliente == null ? null :
                br.unitins.tp2.mapper.ClienteResponseMapper.toResponse(local.cliente);
        return new AuthDTOs.UsuarioAuth(local == null ? null : local.getId(), name, email, roles, cliente);
    }
}
