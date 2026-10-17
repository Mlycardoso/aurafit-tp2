import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ProdutoFormulario } from './produto-formulario';
import { ProdutoService } from '../produto.service';
import { MarcaService } from '../../marcas/marca.service';
import { CategoriaService } from '../../categorias/categoria.service';
import { FornecedorService } from '../../fornecedores/fornecedor.service';

describe('Marca do produto', () => {
  for (const marca of [null, { id: 3, nome: 'Marca exemplo', ativo: true }]) {
    it(marca ? 'carrega e envia a marca vinculada' : 'preserva produto sem marca', async () => {
      const update = vi.fn().mockReturnValue(of({}));
      TestBed.configureTestingModule({
        imports: [ProdutoFormulario],
        providers: [
          provideRouter([]),
          { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } } },
          { provide: ProdutoService, useValue: {
            findById: () => of({ nome: 'Whey exemplo', preco: 99, estoque: 2, ativo: true, categoria: { id: 1 }, marca }),
            update,
          } },
          { provide: MarcaService, useValue: { findAll: () => of(marca ? [marca] : []) } },
          { provide: FornecedorService, useValue: { listarOpcoes: () => of([{ id: 5, nome: 'Fornecedor exemplo', ativo: true }]) } },
          { provide: CategoriaService, useValue: { findAll: () => of([{ id: 1, nome: 'Concentrado', ativo: true }]) } },
        ],
      });
      vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const fixture = TestBed.createComponent(ProdutoFormulario);
      await fixture.whenStable();
      const select = fixture.nativeElement.querySelector('#marca') as HTMLSelectElement;
      expect(select.selectedOptions[0].textContent).toContain(marca?.nome ?? 'Sem marca informada');
      const fornecedor = fixture.nativeElement.querySelector('#fornecedor-5') as HTMLInputElement;
      fornecedor.checked = true;
      fornecedor.dispatchEvent(new Event('change'));
      fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
      expect(update).toHaveBeenCalledWith(1, expect.objectContaining({ idMarca: marca?.id ?? null, idsFornecedores: [5] }));
    });
  }
});
