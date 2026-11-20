import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ProdutoService } from './produto.service';

describe('Contrato HTTP das imagens', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] }));
  afterEach(() => TestBed.inject(HttpTestingController).verify());
  it('resolve a URL retornada pelo catálogo contra a API', () => {
    TestBed.inject(ProdutoService).findAll().subscribe(page => {
      expect(page.items[0].imagemUrl).toBe('http://localhost:8080/produtos/1/imagem?v=abc');
      expect(page.items[1].imagemUrl).toBeNull();
    });
    TestBed.inject(HttpTestingController).expectOne('http://localhost:8080/produtos?page=0&pageSize=5').flush({
      items: [{ id: 1, imagemUrl: '/produtos/1/imagem?v=abc' }, { id: 2 }], page: 0, pageSize: 5, totalItems: 2, totalPages: 1,
    });
  });
  it('solicita o conteúdo binário para download', () => {
    const arquivo = new Blob(['imagem'], { type: 'image/png' });
    TestBed.inject(ProdutoService).baixarImagem(1).subscribe(blob => expect(blob).toBe(arquivo));
    const req = TestBed.inject(HttpTestingController).expectOne('http://localhost:8080/produtos/1/imagem?download=true');
    expect(req.request.responseType).toBe('blob'); req.flush(arquivo);
  });
});
