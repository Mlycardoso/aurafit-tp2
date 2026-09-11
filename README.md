# AuraFit - Trabalho 02

Sistema administrativo com dois CRUDs completos: Marcas e Categorias.

## Estrutura

- `back`: API REST em Java com Quarkus e PostgreSQL.
- `front`: interface administrativa em Angular.

## Executar o back-end

Abra um terminal na pasta `back` e execute:

```powershell
.\mvnw.cmd quarkus:dev
```

A API ficará disponível em `http://localhost:8080` e a documentação em
`http://localhost:8080/q/swagger-ui`.

## Executar o front-end

Abra outro terminal na pasta `front` e execute:

```powershell
npm install
npm start
```

A interface ficará disponível em `http://localhost:4200`.

## Banco de dados

- Banco: `topicos2db`
- Usuário: `topicos2`
- Senha: `123456`
- Porta: `5432`

As tabelas e os dados iniciais são criados automaticamente pelo Quarkus.

## Funcionalidades

- Cadastro de marcas e categorias.
- Listagem e busca por nome.
- Edição dos registros.
- Exclusão com confirmação.
- Validação no front-end e no back-end.
- Layout responsivo para computador e celular.
