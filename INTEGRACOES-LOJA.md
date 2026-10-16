# Loja AuraFit

A vitrine fica em /loja (tambem aberta pela raiz /). As rotas administrativas
existentes, como /produtos e /usuarios, continuam separadas e sem autenticacao.
Nao publique a API administrativa na internet antes de implementar autorizacao.

## Disponivel

- Catalogo lido da API Quarkus, com preco, estoque e categoria.
- Busca, filtros combinados, ordenacao e paginacao no navegador. O carregamento
  percorre todas as paginas da API; para catalogos grandes, migrar esses filtros
  para uma consulta publica paginada no servidor.
- Carrinho e favoritos locais persistidos por ID, sem dados pessoais.
- Consulta de endereco pela integracao de CEP existente.
- Revisao de compra sem envio de pedido e sem cobranca.

## Dependencias pendentes

- Autenticacao, sessao e autorizacao; sincronizacao de favoritos por conta.
- Marca associada ao produto (hoje o cadastro de marcas e independente).
- Campos estruturados de sabor e peso; atualmente extraidos do nome quando presentes.
- Familia de produto e variacoes: as opcoes exibidas sao outros cadastros da mesma categoria,
  nao SKUs com variacoes garantidas pelo backend.
- Upload de fotos, galeria e informacao nutricional por produto.
- Pedidos, reserva de estoque, cotacao de entrega e provedor de pagamentos.

As imagens whey-hero.png e whey-pouch.png foram geradas para esta composicao.
Sao ilustrativas, nao fotografias das mercadorias. A logo existente foi preservada.
Nao ha precos promocionais, avaliacoes, frete ou pagamentos ficticios.
