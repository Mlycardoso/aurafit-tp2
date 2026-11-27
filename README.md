<p align="center">
  <img src="front/public/logo-aurafit.png" alt="Logo AuraFit" width="200" />
</p>

<h1 align="center">AuraFit</h1>

<p align="center">
  Uma experiência dedicada ao whey protein.
</p>

<p align="center">
  Angular · Quarkus · PostgreSQL
</p>

## Sobre

A AuraFit é uma plataforma web para uma loja especializada exclusivamente em whey protein. Reúne uma vitrine de produtos e uma área administrativa para organizar o catálogo e gerenciar clientes e usuários.

A identidade visual combina rosa, verde-menta e superfícies claras, com foco na apresentação dos produtos e na facilidade de navegação.

**Status:** loja e administração com autenticação Keycloak, criação e acompanhamento de pedidos. O pagamento continua fora do escopo e nenhuma cobrança é simulada.

## Recursos Atuais

### Administração

- Gestão de produtos, categorias, marcas, clientes e usuários.
- Pesquisa por nome, paginação e edição dos cadastros.
- Produtos vinculados a categorias, marcas e fornecedores, com preço, estoque, SKU único, sabor e peso em gramas.
- Inativação e reativação de produtos, preservando os cadastros e seus vínculos.
- Consulta de endereço por CEP, com município e estado estruturados.
- Validações no front-end e no back-end.
- Autorização administrativa por role ADMIN via Keycloak.
- Cadastro e gerenciamento de usuários sincronizados com o Keycloak; senhas não ficam no banco da loja.

### Loja

- Catálogo integrado à API, com busca ao digitar, filtros, ordenação e paginação.
- Página de produto com preço, disponibilidade e quantidade.
- Carrinho e favoritos armazenados no navegador.
- Consulta de endereço por CEP, seleção de entrega e finalização do pedido.
- Acompanhamento de pedidos pelo cliente e pela administração.
- Interface adaptada para computador e celular.

As imagens conceituais de whey são ilustrativas. Consulte o [guia de autenticação Keycloak](KEYCLOAK.md), os [detalhes da loja](INTEGRACOES-LOJA.md) e o [guia S3](TRABALHO-05-S3.md).

## Tecnologias

| Camada | Tecnologias |
| --- | --- |
| Front-end | Angular 22, TypeScript, SCSS e RxJS |
| Back-end | Java 25, Quarkus e API REST |
| Persistência | PostgreSQL e Hibernate ORM com Panache |
| Documentação da API | OpenAPI e Swagger UI |
| Testes | Vitest, JUnit e RestAssured |

## Organização

```text
aurafit-tp2/
├── front/                 Aplicação Angular
│   ├── public/            Logo, imagens e ícones
│   └── src/app/
│       ├── admin/         Layout e rotas administrativas
│       ├── loja/          Vitrine e fluxo de compra
│       ├── produtos/      Componentes, model e service de produtos
│       ├── categorias/    Cadastro de categorias
│       ├── marcas/        Cadastro de marcas
│       ├── clientes/      Cadastro de clientes e endereço
│       ├── usuarios/      Cadastro de usuários e perfis
│       └── shared/        Recursos compartilhados
└── back/                  API Quarkus e testes de integração
```

Front-end e back-end são aplicações independentes, mantidas no mesmo repositório. O Angular se comunica com a API por HTTP; a API aplica as regras de negócio e acessa o PostgreSQL.

## Acesso Local

Subida local no Windows/PowerShell, nesta ordem:

1. Inicie o Docker Desktop e o PostgreSQL local (banco `topicos2db`, usuário `topicos2`).
2. Na raiz, `./iniciar-storage.ps1`.
3. Na raiz, `./iniciar-keycloak.ps1` e depois `./configurar-keycloak.ps1`; a segunda etapa grava a configuração necessária em `back/.env` e exibe a senha do admin uma vez. Veja [KEYCLOAK.md](KEYCLOAK.md).
4. Em `back/`, `./mvnw.cmd quarkus:dev`. `DB_USERNAME`, `DB_PASSWORD` e `DB_URL` podem ser definidos no ambiente; os padrões são para desenvolvimento local.
5. Em `front/`, `npm start -- --host 127.0.0.1`.

Para configurar storage e seus limites de upload, veja [TRABALHO-05-S3.md](TRABALHO-05-S3.md).

| Área | Endereço |
| --- | --- |
| Loja | http://localhost:4200/loja |
| Administração | http://localhost:4200/admin (requer role ADMIN) |
| Swagger UI | http://localhost:8080/q/swagger-ui/ |
| Keycloak | http://127.0.0.1:8081 |

O Keycloak usa volume persistente em modo de desenvolvimento. A configuração não deve ser exposta publicamente como produção.
