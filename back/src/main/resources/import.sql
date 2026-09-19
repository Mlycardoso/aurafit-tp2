insert into marca (nome, descricao, ativo) values ('AuraFit', 'Linha propria de suplementos', true);
insert into marca (nome, descricao, ativo) values ('Growth Supplements', 'Suplementos para performance e saude', true);
insert into marca (nome, descricao, ativo) values ('Max Titanium', 'Nutricao esportiva nacional', true);
insert into marca (nome, descricao, ativo) values ('Integralmedica', 'Suplementos para diferentes objetivos', true);

insert into categoria (nome, descricao, ativo) values ('Whey Protein', 'Proteinas para recuperacao e ganho muscular', true);
insert into categoria (nome, descricao, ativo) values ('Creatina', 'Suplementos de forca e desempenho', true);
insert into categoria (nome, descricao, ativo) values ('Pre-treino', 'Energia e foco para o treino', true);
insert into categoria (nome, descricao, ativo) values ('Vitaminas', 'Vitaminas, minerais e bem-estar', true);

insert into produto (nome, descricao, preco, estoque, ativo, id_categoria) values ('Whey Protein 3W 900g', 'Blend de proteinas nos sabores chocolate e baunilha', 149.90, 24, true, (select id from categoria where nome = 'Whey Protein'));
insert into produto (nome, descricao, preco, estoque, ativo, id_categoria) values ('Creatina Monohidratada 300g', 'Creatina pura para forca e desempenho', 89.90, 38, true, (select id from categoria where nome = 'Creatina'));
insert into produto (nome, descricao, preco, estoque, ativo, id_categoria) values ('Pre-Workout Energy 300g', 'Formula para energia e foco durante o treino', 109.90, 17, true, (select id from categoria where nome = 'Pre-treino'));
insert into produto (nome, descricao, preco, estoque, ativo, id_categoria) values ('Multivitaminico 120 capsulas', 'Vitaminas e minerais para a rotina diaria', 54.90, 42, true, (select id from categoria where nome = 'Vitaminas'));
insert into produto (nome, descricao, preco, estoque, ativo, id_categoria) values ('Whey Isolado 900g', 'Proteina isolada de alta concentracao', 199.90, 12, true, (select id from categoria where nome = 'Whey Protein'));
insert into produto (nome, descricao, preco, estoque, ativo, id_categoria) values ('Creatina Creapure 250g', 'Creatina monohidratada com materia-prima Creapure', 129.90, 9, true, (select id from categoria where nome = 'Creatina'));
insert into produto (nome, descricao, preco, estoque, ativo, id_categoria) values ('Pre-Treino Focus 200g', 'Cafeina e taurina para foco nos treinos', 79.90, 21, true, (select id from categoria where nome = 'Pre-treino'));
