# Loja AuraFit

A vitrine fica em `/loja` e também abre pela raiz. O acesso à API administrativa exige role `ADMIN`; checkout e histórico do cliente exigem role `CLIENTE`. A configuração local está em [KEYCLOAK.md](KEYCLOAK.md).

## Disponível

- Catálogo da API Quarkus com busca, filtros, ordenação e paginação no navegador.
- Carrinho e favoritos por ID persistidos no navegador; esses dados permanecem locais ao sair da conta.
- Login, cadastro e logout intermediados pela API; o navegador não acessa o Keycloak.
- Endereço do cliente usado para criar o snapshot de entrega do pedido.
- Checkout com entrega normal ou rápida, resumo e confirmação do pedido.
- Tela “Meus pedidos” para clientes e “Produtos vendidos” para ADMIN, com paginação, filtros e atualização de status.
- Imagens PNG/JPEG de produto armazenadas em S3 compatível; veja [TRABALHO-05-S3.md](TRABALHO-05-S3.md).

## Regras de entrega e pagamento

- Normal: frete de R$ 14,90 e previsão de 7 dias úteis.
- Rápida: frete de R$ 29,90 e previsão de 2 dias úteis.
- O prazo exclui sábado e domingo e não considera feriados.
- O status inicia como pendente; o administrador pode marcar o pedido como enviado.
- Pagamento está fora do escopo. O sistema não coleta forma de pagamento e não simula cobrança.

O backend valida produto ativo e estoque e desconta a quantidade dentro da transação do pedido. Pedido e itens guardam snapshots do nome e preço do produto, além do nome e endereço do cliente, para preservar o histórico.

## Fora do escopo atual

- Integração com gateway de pagamento, processamento de cobrança e cotação dinâmica de frete.
- Sincronização de favoritos entre dispositivos.
- Variantes por SKU, galeria de múltiplas fotos e dados nutricionais estruturados.

As imagens `whey-hero.png` e `whey-pouch.png` são ilustrativas, não fotografias dos produtos. A aplicação não mostra avaliações, promoções, frete ou pagamentos fictícios.

## Catálogo administrativo

- SKU único, sabor, peso líquido e estoque.
- Inativação preserva produto, vínculos e imagem.
- Upload, visualização, download e remoção de uma imagem PNG/JPEG por produto via API e S3.
- O endpoint legado `DELETE /produtos/{id}` inativa o cadastro em vez de excluir fisicamente.
