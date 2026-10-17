import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';

import { Categoria } from '../../categorias/categoria.model';
import { CategoriaService } from '../../categorias/categoria.service';
import { Marca } from '../../marcas/marca.model';
import { MarcaService } from '../../marcas/marca.service';
import { Fornecedor, FornecedorService } from '../../fornecedores/fornecedor.service';
import { getApiError } from '../../shared/api-error';
import { ProdutoPayload } from '../produto.model';
import { ProdutoService } from '../produto.service';

@Component({
  selector: 'app-produto-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './produto-formulario.html',
})
export class ProdutoFormulario {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(ProdutoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly marcaService = inject(MarcaService);
  protected readonly marcas = signal<Marca[]>([]);
  private readonly fornecedorService = inject(FornecedorService);
  protected readonly fornecedores = signal<Fornecedor[]>([]);

  protected readonly id = Number(this.route.snapshot.paramMap.get('id')) || null;
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly saving = signal(false);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly loadFailed = signal(false);
  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.pattern(/\S/), Validators.minLength(2), Validators.maxLength(80)]],
    sku: ['', [Validators.maxLength(40), Validators.pattern(/^[A-Za-z0-9_-]+$/)]],
    sabor: ['', Validators.maxLength(60)],
    pesoGramas: this.fb.control<number | null>(null, [Validators.min(1), Validators.max(2147483647), Validators.pattern(/^\d+$/)]),
    descricao: ['', [Validators.maxLength(255)]],
    preco: [0, [Validators.required, Validators.min(0.01), Validators.max(99999999.99), Validators.pattern(/^\d+(\.\d{1,2})?$/)]],
    estoque: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
    idCategoria: [0, [Validators.required, Validators.min(1)]],
    idMarca: [0],
    idsFornecedores: this.fb.nonNullable.control<number[]>([]),
    ativo: [true],
  });

  constructor() {
    forkJoin({
      categorias: this.categoriaService.findAll(),
      marcas: this.marcaService.findAll(),
      fornecedores: this.fornecedorService.listarOpcoes(),
      produto: this.id ? this.service.findById(this.id) : of(null)
    }).subscribe({
        next: ({categorias, marcas, fornecedores, produto}) => {
          this.fornecedores.set(fornecedores);
          this.marcas.set(marcas);
          this.categorias.set(categorias);
          if (produto) {
          this.form.patchValue({
            sku: produto.sku ?? '', sabor: produto.sabor ?? '', pesoGramas: produto.pesoGramas ?? null,
            nome: produto.nome,
            descricao: produto.descricao ?? '',
            preco: produto.preco,
            estoque: produto.estoque,
            idCategoria: produto.categoria.id,
            idMarca: produto.marca?.id ?? 0,
            idsFornecedores: produto.fornecedores?.map(f => f.id) ?? [],
            ativo: produto.ativo,
          });
          }
          this.loading.set(false);
        },
        error: (error) => {
          this.error.set(getApiError(error));
          this.loadFailed.set(true);
          this.loading.set(false);
        },
      });
  }

  protected save(): void {
    if (this.saving() || this.loadFailed() || this.loading()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const payload: ProdutoPayload = {
      sku: value.sku.trim().toUpperCase() || null, sabor: value.sabor.trim() || null,
      pesoGramas: value.pesoGramas == null ? null : Number(value.pesoGramas),
      nome: value.nome.trim(),
      descricao: value.descricao.trim() || null,
      preco: Number(value.preco),
      estoque: Number(value.estoque),
      idCategoria: Number(value.idCategoria),
      idMarca: Number(value.idMarca) || null,
      idsFornecedores: value.idsFornecedores,
      ativo: value.ativo,
    };
    const request = this.id ? this.service.update(this.id, payload) : this.service.create(payload);

    this.saving.set(true);
    this.error.set('');
    request.subscribe({
      next: () => this.router.navigate(['/admin/produtos']),
      error: (error) => {
        this.error.set(getApiError(error));
        this.saving.set(false);
      },
    });
  }

  protected selecionarFornecedor(id: number, selecionado: boolean): void {
    const control = this.form.controls.idsFornecedores;
    control.setValue(selecionado ? [...new Set([...control.value, id])] : control.value.filter(value => value !== id));
    control.markAsDirty();
  }
}
