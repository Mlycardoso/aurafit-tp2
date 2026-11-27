# Autenticação e autorização AuraFit

O fluxo é **Angular → API Quarkus → Keycloak**. O navegador só conversa com a API. O backend usa `aurafit-api` para password grant, refresh e logout e usa `aurafit-admin` service account para administrar contas.

## Subir no Windows

Pré-requisitos: Docker Desktop, PostgreSQL local acessível em `localhost:5432` com banco `topicos2db` e usuário `topicos2`, JDK 25, Node.js compatível com Angular 22 e npm.

1. Inicie o Docker Desktop e o PostgreSQL usado pelo projeto.
2. Na raiz, rode `./iniciar-storage.ps1` no PowerShell.
3. Rode `./iniciar-keycloak.ps1`. O script completa `back/.env` com valores aleatórios para o administrador master do Keycloak. O arquivo é ignorado pelo Git.
4. Aguarde o Keycloak abrir em `http://127.0.0.1:8081`; no primeiro início ele importa `keycloak/aurafit-realm.json`.
5. Rode `./configurar-keycloak.ps1`. O script lê as credenciais master de `back/.env`, obtém os secrets dos clients `aurafit-api` e `aurafit-admin` pela Admin REST API e grava esses valores no `.env`. Também define uma senha aleatória não temporária para `admin@aurafit.local`, grava-a em `AURAFIT_ADMIN_PASSWORD` e a exibe uma única vez no terminal. Guarde essa senha.
6. Em `back/`, execute `./mvnw.cmd quarkus:dev`.
7. Em `front/`, execute `npm start -- --host 127.0.0.1` e acesse `http://127.0.0.1:4200`.

Como alternativa, os secrets podem ser copiados da console master em **Clients → aurafit-api/aurafit-admin → Credentials**. Defina a senha do admin em **Users → admin@aurafit.local → Credentials → Set password**. Deixe **Temporary** desligado. A ação **Update Password** não pode ficar ativa: o login do admin é intermediado pelo backend usando password grant, que não conclui logins com ações obrigatórias pendentes. A senha não é versionada.

O realm só é importado automaticamente na primeira subida, quando ainda não há realm persistido. Quem já possui o volume `keycloak_data` precisa remover manualmente a ação em **Users → admin@aurafit.local → Details → Required user actions**, ou recriar o volume (o que apaga os dados locais do Keycloak).

O Keycloak usa o modo `start-dev` com H2 no volume nomeado `keycloak_data`; não requer outro PostgreSQL e mantém o realm após reiniciar. As portas estão limitadas a `127.0.0.1`. Para desligar sem apagar os dados, rode `docker compose --env-file back/.env -f compose.keycloak.yaml down`. Evite `down -v`: isso apaga o realm e os usuários locais do Keycloak. O import só aplica automaticamente em um realm ainda inexistente.

## Variáveis do backend

Os scripts definem identificadores e credenciais locais no `.env`; `configurar-keycloak.ps1` busca os secrets dos clients após o import.

| Variável | Valor padrão / origem |
| --- | --- |
| `KEYCLOAK_SERVER_URL` | `http://127.0.0.1:8081` |
| `KEYCLOAK_AUTH_SERVER_URL` | `http://127.0.0.1:8081/realms/aurafit` |
| `KEYCLOAK_REALM` | `aurafit` |
| `KEYCLOAK_API_CLIENT_ID` | `aurafit-api` |
| `KEYCLOAK_API_CLIENT_SECRET` | Secret exibido na console do client `aurafit-api` |
| `KEYCLOAK_ADMIN_CLIENT_ID` | `aurafit-admin` |
| `KEYCLOAK_ADMIN_CLIENT_SECRET` | Secret exibido na console do client `aurafit-admin` |
| `DB_USERNAME` / `DB_PASSWORD` / `DB_URL` | Usuário, senha e URL do PostgreSQL; padrões locais em `application.properties` |

Quarkus roda como OIDC service, valida bearer JWT e lê roles em `realm_access/roles`. O OIDC dev service permanece desativado porque o Docker é iniciado pelos scripts do repositório. O client `aurafit-api` é confidential e tem Direct Access Grants habilitado. O client `aurafit-admin` usa service account, sem fluxo de login de usuário. A service account recebe apenas `manage-users`, `view-users`, `query-users` e `view-realm` de `realm-management`. Não existe client público para o Angular.

## Sessão e credenciais

- A senha só transita em `POST /auth/login` ou `POST /auth/registro`; o backend não a grava. O formulário administrativo de usuários não aceita senha. Contas criadas pelo administrador recebem uma senha aleatória temporária no Keycloak e exigem troca antes do login.
- O access token fica em memória no Angular. O refresh token fica em `sessionStorage` para permitir restauração após recarregar a página na mesma aba; fechar a aba encerra essa sessão do navegador. Nenhuma senha é armazenada no navegador.
- O interceptor acrescenta o bearer token às chamadas da API, tenta refresh uma vez após 401 e redireciona para login se a renovação falhar. 403 abre a tela de acesso negado.
- Logout revoga a sessão no Keycloak com o refresh token e remove o token e o usuário do estado local. Carrinho e favoritos continuam no navegador porque são seleções locais sem dados da conta.
- As roles `ADMIN` e `CLIENTE` têm o Keycloak como fonte de verdade. `Usuario.perfis` é uma projeção local para a tabela administrativa, sincronizada pelo CRUD e no login do usuário; alterações feitas diretamente na console são refletidas no registro local no próximo login.
- O realm de desenvolvimento importa `admin@aurafit.local` sem senha versionada nem ação obrigatória de troca. Use `configurar-keycloak.ps1` ou defina a senha pela console como não temporária; mantenha **Update Password** desativada para permitir o password grant do backend.

## Rotas e permissões

| Recurso | Acesso |
| --- | --- |
| `GET /`, leitura e busca de `/produtos`, `/categorias`, `/marcas`, `GET /produtos/{id}/imagem` | Público |
| `GET /clientes/cep/{cep}`, `GET /fornecedores/cep/{cep}` | Público (consulta de CEP) |
| `POST /auth/login`, `POST /auth/registro`, `POST /auth/refresh` | Público |
| `POST /auth/logout`, `GET /auth/me` | ADMIN ou CLIENTE autenticado |
| `POST /pedidos`, `GET /pedidos/meus` | CLIENTE |
| `GET /pedidos/{id}` | ADMIN ou CLIENTE proprietário; pedido de outro cliente responde 404 |
| `GET /pedidos`, `PATCH /pedidos/{id}/enviar` | ADMIN |
| `POST`, `PUT`, `PATCH`, `DELETE /produtos`, `/categorias`, `/marcas`, `/fornecedores`, `/clientes`, `/usuarios` | ADMIN |
| `GET /fornecedores`, `GET /clientes`, `GET /usuarios` e respectivas consultas administrativas | ADMIN |
| `POST` e `DELETE /produtos/{id}/imagem` | ADMIN |

401, 403, 404 e 409/422 relevantes usam `application/problem+json`; respostas não incluem token, secret ou stack trace. A vitrine mantém leitura pública; demais consultas de cadastros administrativos exigem ADMIN.

## Pedidos, entrega e pagamento

- O frete é fixo nesta atividade: normal custa R$ 14,90 e prevê 7 dias úteis; rápida custa R$ 29,90 e prevê 2 dias úteis. Sábados e domingos não contam; feriados não são descontados.
- O pedido guarda endereço, nome, preço, nome de produto e SKU como snapshots. Estoque é validado e baixado na mesma transação, com lock pessimista por produto. Estoque insuficiente retorna 422.
- O status inicia como `PENDENTE`; marcar como enviado muda para `ENVIADO` e repetir a ação não produz efeitos adicionais.
- Pagamento está fora do escopo. A interface não simula cobrança e informa que nenhuma cobrança foi feita.

## Testes

Os testes usam `KeycloakClientFake` e não precisam de um Keycloak ativo. Os testes de integração existentes continuam usando PostgreSQL e S3 conforme `TRABALHO-05-S3.md`.

```powershell
cd back
./mvnw.cmd test '-Dquarkus.http.test-port=8082'
cd ../front
npm test -- --watch=false
npm run build
```

Na inicialização, o backend remove `senhahash` da tabela local antiga. Os perfis locais existentes continuam como projeção; contas sem `keycloakId` são associadas a uma conta temporária do Keycloak ao serem editadas, e exigem redefinição de senha pelo administrador do realm.

## Referências oficiais

- [Quarkus OIDC Bearer Token Authentication](https://quarkus.io/guides/security-oidc-bearer-token-authentication)
- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/)
- [Keycloak Server Administration Guide](https://www.keycloak.org/docs/latest/server_admin/)
