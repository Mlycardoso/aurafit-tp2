# Imagens dos produtos com Object Storage

A AuraFit usa RustFS local, compatível com S3, e AWS SDK for Java v2 no backend Quarkus. O navegador envia o arquivo à API; as credenciais do armazenamento permanecem no servidor. O bucket não precisa ser público.

## Escolha da tecnologia

Foram consultadas as alternativas mencionadas na atividade:

| Alternativa | Adequação para esta entrega |
| --- | --- |
| [RustFS](https://docs.rustfs.com/en/installation/container/docker) | Escolhido: execução local em container, volume persistente e console web para demonstrar os objetos. |
| [SeaweedFS](https://github.com/seaweedfs/seaweedfs) | Oferece S3 e outras interfaces; é uma alternativa, mas não foi necessário adotar esses serviços adicionais. |
| [Garage](https://github.com/deuxfleurs-org/garage) | Alternativa S3 voltada a armazenamento distribuído; requer sua própria configuração de nós, layout e permissões. |
| [Amazon S3](https://docs.aws.amazon.com/AmazonS3/latest/userguide/Welcome.html) | Serviço gerenciado; exige conta, configuração de acesso e avaliação dos custos. A entrega local dispensa conta em nuvem. |

O SDK possui operações oficiais de [gravação, leitura e exclusão](https://docs.aws.amazon.com/java/api/latest/software/amazon/awssdk/services/s3/S3Client.html). O backend configura endpoint, região, bucket e credenciais por ambiente.

## Iniciar localmente

1. Abra o Docker Desktop e espere o mecanismo iniciar.
2. Na raiz deste repositório, execute `./iniciar-storage.ps1` no PowerShell.
3. O script cria credenciais aleatórias em `back/.env`, se o arquivo ainda não existir, e inicia RustFS. Esse arquivo é ignorado pelo Git. Não o envie ao professor nem o copie para o frontend.
4. Console: `http://localhost:9001`; API S3: `http://localhost:9000`. Consulte as credenciais no arquivo local. As portas do container estão restritas ao computador local.
5. Em `back/`, execute `./mvnw.cmd quarkus:dev`. O Quarkus lê o `.env` dessa pasta. O bucket é criado no primeiro upload em desenvolvimento.
6. Em `front/`, execute `npm start -- --host 127.0.0.1`.
7. Abra `http://127.0.0.1:4200/admin/produtos`, edite um produto e use **Imagem do produto**. Para um novo produto, salve primeiro: a aplicação abre sua edição para adicionar a imagem.

A API padrão usa 8080. Se essa porta estiver ocupada por outro aplicativo, não encerre processos desconhecidos: libere a porta conscientemente antes de iniciar a AuraFit. Os testes usam 8082.

O volume `aurafit-storage_imagens` mantém arquivos após reiniciar o container. Não execute `down -v` para reiniciar, pois remove o volume. Para desligar sem apagar: `docker compose --env-file back/.env -f compose.storage.yaml down`.

## Contrato e regras

| Operação | Requisição |
| --- | --- |
| Enviar/substituir | `POST /produtos/{id}/imagem`, multipart, campo `arquivo`; retorna `imagemUrl` e `aviso` opcional |
| Visualizar | `GET /produtos/{id}/imagem` |
| Baixar | `GET /produtos/{id}/imagem?download=true` |
| Remover | `DELETE /produtos/{id}/imagem`, resposta 204 |

- Uma imagem por produto. A resposta do cadastro/listagem contém `imagemUrl` relativo, ou null; o serviço Angular resolve a URL contra a API. A loja usa essa imagem no catálogo e detalhes, mantendo a ilustração identificada quando não há foto.
- PNG e JPEG, até 5 MiB (5 × 1024 × 1024 bytes), não vazios. Limite de 6000 pixels por lado e 16 milhões de pixels. O servidor decodifica a imagem, verifica formato real e MIME e a reencoda sem metadados/conteúdo anexado. A imagem processada também precisa caber no limite.
- O cliente valida MIME e tamanho para dar feedback; a validação de segurança ocorre no backend. Nomes enviados pelo cliente não são usados como caminho: cada arquivo recebe chave com ID do produto e UUID.
- O upload não torna a imagem obrigatória e não altera preço, estoque ou vínculos. Inativar o produto preserva sua imagem. A remoção exige uma ação explícita na tela e pode ser repetida.
- Configuração HTTP limita o corpo multipart a 6 MiB; uploads maiores podem ser rejeitados com 413 antes da validação do serviço.
- A referência é armazenada no campo privado `Produto.imagemChave`. O projeto continua com Hibernate update em desenvolvimento e schema isolado nos testes; não foi introduzido Flyway nem executada migration de produção.
- Uploads concorrentes usam troca condicional da referência; um resultado atrasado não sobrescreve outra imagem. Chamadas S3 não mantêm lock ou transação de banco abertos. DynamicUpdate evita que edições de outros campos regravem uma referência antiga.

## Falhas e limites operacionais

- Se o upload falhar, a referência anterior permanece. Se a troca de referência falhar, o novo objeto é removido por compensação. Após a troca, o arquivo antigo é removido.
- Se a limpeza do arquivo antigo falhar, o upload retorna aviso e o servidor registra a chave pendente para remoção manual no console. Não há fila durável de limpeza automática.
- Na remoção, o S3 é chamado primeiro. Se falhar, a referência é preservada para nova tentativa. Se o S3 remover e o banco falhar depois, a referência pode temporariamente apontar para arquivo ausente; repetir a remoção corrige isso.
- PostgreSQL e S3 não compartilham transação distribuída. Queda abrupta entre etapas pode deixar objeto órfão ou referência sem objeto. Não se promete atomicidade distribuída; antes de remover um órfão manualmente, confirme que sua chave não está referenciada por nenhum produto.
- Os endpoints de upload e remoção agora exigem role `ADMIN`; leitura da imagem permanece pública para a vitrine. Configure Keycloak seguindo [KEYCLOAK.md](KEYCLOAK.md). RustFS e Keycloak locais continuam destinados ao desenvolvimento, não à exposição pública como produção. O escopo é imagem de produto, não fotos de usuários ou galeria múltipla.
- As credenciais geradas são locais. Para nuvem, configure endpoint/região/bucket e credenciais próprias com permissões mínimas; não use o script local como provisionamento de produção. Este cliente foi configurado e testado para RustFS, não para todas as particularidades de regiões/provedores S3.

## Validar e demonstrar

Com PostgreSQL local e RustFS disponíveis, a partir de `back/`:

```powershell
./mvnw.cmd test '-Dquarkus.http.test-port=8082'
```

Os testes recriam apenas o schema configurado `aurafit_test` e usam o bucket `aurafit-produtos-test`. Confirme os destinos e overrides antes de executar. `ProdutoImagemResourceTest` exige S3 real; testes unitários de falhas usam um substituto em memória e não comprovam disponibilidade do S3.

Em `front/`:

```powershell
npm test -- --watch=false
npm run build
```

Roteiro: salvar produto; enviar PNG/JPEG; consultar a foto no catálogo; baixar; substituir; confirmar remoção; verificar catálogo sem foto; tentar arquivo inválido e acima do limite. Demonstre o bucket pelo console para evidenciar o Object Storage.

Para entrega, versionar fontes, configuração sem segredos e este guia. Enviar no Educa o link do repositório e/ou pacote de fontes conforme orientação do professor. Excluir `.env`, `node_modules`, `target`, `dist` e volumes. A existência deste documento não comprova testes executados: consulte o registro de validação da entrega.

## Registro de validação desta implementação

- Em 19/11/2026, `mvnw.cmd -B test -Dquarkus.http.test-port=8082 -Dquarkus.hibernate-orm.log.sql=false`: 24 testes, sem falhas, erros ou ignorados, incluindo integração real PostgreSQL/RustFS. Relatório local: `back/target/upload-tests.log`.
- Em 19/11/2026, `mvnw.cmd -B test -Dtest=ImagemValidatorTest,ProdutoImagemServiceTest`: 7 testes unitários, sem falhas; 3 repetem a validação de arquivos da suíte anterior e 4 cobrem falhas de armazenamento/conflito. Total de 28 testes distintos de backend executados. Relatório: `back/target/upload-unit-tests.log`.
- Em 20/11/2026, `npm test -- --watch=false`: 60 testes em 15 arquivos aprovados. O jsdom informa que `scrollTo()` não é implementado; os testes passaram. Relatório local: `front/.angular/teste-upload.log`.
- Em 20/11/2026, `npm run build`: build de produção aprovado.
- Em 20/11/2026, Chrome automatizado com API e S3 reais: rejeição de SVG no formulário, upload PNG, renderização da imagem, download com arquivo não vazio, exibição no catálogo e exclusão confirmada. Inspeção das capturas em 1440 × 1000 e 390 × 844, incluindo o formulário sem sobreposição após o término da animação de navegação.
- O navegador de testes direcionou as chamadas da porta 8080 para uma API isolada em 8083, sem simular respostas, para preservar o outro aplicativo local que usa 8080. Foram usados somente schema e bucket de testes; as fixtures foram inativadas e suas imagens removidas. Isso não significa que a aplicação de desenvolvimento esteja em execução na porta padrão.
- Capturas locais em `back/target/evidencias-upload/`: `admin-desktop.png`, `admin-mobile.png`, `catalogo-desktop.png`, `catalogo-mobile.png`. A imagem ilustrativa existente foi usada apenas como arquivo de teste.
- Não foi medida cobertura percentual nem feita auditoria completa de acessibilidade ou de autorização. Não houve publicação no GitHub ou envio ao Educa nesta validação.
