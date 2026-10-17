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

**Status:** em desenvolvimento. A administração e a vitrine estão implementadas; o fluxo de compra ainda não realiza pedidos ou pagamentos.

## Recursos Atuais

### Administração

- Gestão de produtos, categorias, marcas, clientes e usuários.
- Pesquisa por nome, paginação e edição dos cadastros.
- Produtos vinculados a categorias, marcas e fornecedores, com preço, estoque, SKU único, sabor e peso em gramas.
- Inativação e reativação de produtos, preservando os cadastros e seus vínculos.
- Consulta de endereço por CEP, com município e estado estruturados.
- Validações no front-end e no back-end.
- Cadastro de perfis de usuário, ainda sem autenticação de acesso.

### Loja

- Catálogo integrado à API, com busca ao digitar, filtros, ordenação e paginação.
- Página de produto com preço, disponibilidade e quantidade.
- Carrinho e favoritos armazenados no navegador.
- Consulta de endereço por CEP e revisão dos dados da compra.
- Interface adaptada para computador e celular.

As imagens conceituais de whey são ilustrativas. Autenticação, pedidos, cálculo de frete e pagamento ainda dependem de implementação. Consulte os [detalhes das integrações](INTEGRACOES-LOJA.md).

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

Com o banco, a API e o front-end em execução:

| Área | Endereço |
| --- | --- |
| Loja | http://localhost:4200/loja |
| Administração | http://localhost:4200/admin |
| Swagger UI | http://localhost:8080/q/swagger-ui/ |

> O acesso administrativo ainda não possui autenticação ou autorização. Esta versão é destinada ao desenvolvimento local e não deve ser exposta publicamente com dados reais.
