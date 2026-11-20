package br.unitins.tp2.service;

public interface ImagemStorage {
    void salvar(String chave, byte[] bytes, String tipo);
    Arquivo ler(String chave);
    void excluir(String chave);
    record Arquivo(byte[] bytes, String tipo) {}
}
