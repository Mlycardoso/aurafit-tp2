import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Categoria } from '../../categorias/categoria.model';
import { CategoriaService } from '../../categorias/categoria.service';
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

  protected readonly id = Number(this.route.snapshot.paramMap.get('id')) || null;
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly saving = signal(false);
  protected readonly loading = signal(true);
  protected readonly error = signal('');
  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    descricao: ['', [Validators.maxLength(255)]],
    preco: [0, [Validators.required, Validators.min(0.01)]],
    estoque: [0, [Validators.required, Validators.min(0)]],
    idCategoria: [0, [Validators.required, Validators.min(1)]],
    ativo: [true],
  });

  constructor() {
    this.categoriaService.findAll().subscribe({
      next: (categorias) => {
        this.categorias.set(categorias.filter((categoria) => categoria.ativo));
        if (!this.id) this.loading.set(false);
      },
      error: (error) => {
        this.error.set(getApiError(error));
        this.loading.set(false);
      },
    });

    if (this.id) {
      this.service.findById(this.id).subscribe({
        next: (produto) => {
          this.form.patchValue({
            nome: produto.nome,
            descricao: produto.descricao ?? '',
            preco: produto.preco,
            estoque: produto.estoque,
            idCategoria: produto.categoria.id,
            ativo: produto.ativo,
          });
          this.loading.set(false);
        },
        error: (error) => {
          this.error.set(getApiError(error));
          this.loading.set(false);
        },
      });
    }
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const payload: ProdutoPayload = {
      nome: value.nome.trim(),
      descricao: value.descricao.trim() || null,
      preco: Number(value.preco),
      estoque: Number(value.estoque),
      idCategoria: Number(value.idCategoria),
      ativo: value.ativo,
    };
    const request = this.id ? this.service.update(this.id, payload) : this.service.create(payload);

    this.saving.set(true);
    this.error.set('');
    request.subscribe({
      next: () => this.router.navigate(['/produtos']),
      error: (error) => {
        this.error.set(getApiError(error));
        this.saving.set(false);
      },
    });
  }
}
