import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { Loja } from './loja';
import { ProdutoService } from '../produtos/produto.service';

describe('Loja', () => {
  const products = [
    {
      id: 1,
      nome: 'Whey Chocolate 900g',
      preco: 80,
      estoque: 2,
      ativo: true,
      categoria: { id: 1, nome: 'Concentrado' },
    },
    {
      id: 2,
      nome: 'Whey Baunilha 900g',
      preco: 150,
      estoque: 0,
      ativo: true,
      categoria: { id: 2, nome: 'Isolado' },
    },
    {
      id: 3,
      nome: 'Whey Inativo',
      preco: 10,
      estoque: 1,
      ativo: false,
      categoria: { id: 1, nome: 'Concentrado' },
    },
  ];
  const service = { findAll: vi.fn() };
  beforeEach(async () => {
    service.findAll.mockReturnValue(of({ items: products, page: 0, totalPages: 1 }));
    await TestBed.configureTestingModule({
      imports: [Loja],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        { provide: ProdutoService, useValue: service },
      ],
    }).compileComponents();
  });
  it('filtra durante a busca e nao exibe produtos inativos', () => {
    const component = TestBed.createComponent(Loja).componentInstance;
    expect(component.filtered().length).toBe(2);
    component.search('choco');
    expect(component.filtered().map((p) => p.id)).toEqual([1]);
    component.search('inexistente');
    expect(component.visible()).toEqual([]);
  });
  it('combina filtros e ordenacao', () => {
    const component = TestBed.createComponent(Loja).componentInstance;
    component.sort.set('high');
    expect(component.filtered()[0].id).toBe(2);
    component.maxPrice.set(100);
    expect(component.filtered().map((p) => p.id)).toEqual([1]);
    component.clear();
    component.category.set('Isolado');
    expect(component.filtered().map((p) => p.id)).toEqual([2]);
  });
  it('pagina os resultados e reinicia a pagina ao pesquisar', () => {
    service.findAll.mockReturnValue(
      of({
        items: Array.from({ length: 17 }, (_, index) => ({ ...products[0], id: index + 1 })),
        page: 0,
        totalPages: 1,
      }),
    );
    const component = TestBed.createComponent(Loja).componentInstance;
    expect(component.pages()).toBe(3);
    component.page.set(3);
    expect(component.visible().length).toBe(1);
    component.search('Whey');
    expect(component.page()).toBe(1);
  });
  it('exibe erro recuperavel sem inventar produtos', () => {
    service.findAll.mockReturnValue(throwError(() => new Error('offline')));
    const component = TestBed.createComponent(Loja).componentInstance;
    expect(component.loading()).toBe(false);
    expect(component.error()).toBeTruthy();
    expect(component.products()).toEqual([]);
  });
});
