package br.unitins.tp2.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import br.unitins.tp2.dto.*;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.*;
import br.unitins.tp2.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class UsuarioServiceImpl implements UsuarioService {
    @Inject UsuarioRepository repository;
    @Inject SenhaService senhas;

    @Override
    @Transactional
    public Usuario create(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        copiar(usuario, dto.nome(), dto.email(), dto.ativo(), dto.perfis());
        usuario.senhaHash = senhas.gerarHash(dto.senha());
        repository.persistAndFlush(usuario);
        return usuario;
    }

    @Override
    @Transactional
    public Usuario update(long id, UsuarioUpdateDTO dto) {
        Usuario usuario = findById(id);
        copiar(usuario, dto.nome(), dto.email(), dto.ativo(), dto.perfis());
        if (dto.senha() != null) {
            if (dto.senha().isBlank()) throw ValidationException.of("senha", "Informe uma senha valida.");
            usuario.senhaHash = senhas.gerarHash(dto.senha());
        }
        return usuario;
    }

    private void copiar(Usuario usuario, String nome, String email, boolean ativo, Set<Perfil> perfis) {
        String normalizado = email.strip().toLowerCase(Locale.ROOT);
        Usuario existente = repository.find("email", normalizado).firstResult();
        if (existente != null && !existente.getId().equals(usuario.getId())) {
            throw ValidationException.of("email", "Ja existe um usuario com este e-mail.");
        }
        if (nome.strip().length() < 2) throw ValidationException.of("nome", "Informe um nome com pelo menos 2 caracteres.");
        usuario.nome = nome.strip();
        usuario.email = normalizado;
        usuario.ativo = ativo;
        usuario.perfis.clear();
        usuario.perfis.addAll(perfis);
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!repository.deleteById(id)) throw new NotFoundException("Usuario nao encontrado.");
    }

    @Override
    public Usuario findById(long id) {
        return repository.findByIdOptional(id).orElseThrow(() -> new NotFoundException("Usuario nao encontrado."));
    }

    @Override
    public List<Usuario> findByNome(String nome, int page, int pageSize) {
        return repository.find("lower(nome) like ?1 order by nome, id", filtro(nome)).page(page, pageSize).list();
    }

    @Override
    public long count(String nome) { return repository.count("lower(nome) like ?1", filtro(nome)); }

    private String filtro(String nome) { return "%" + nome.strip().toLowerCase(Locale.ROOT) + "%"; }
}
