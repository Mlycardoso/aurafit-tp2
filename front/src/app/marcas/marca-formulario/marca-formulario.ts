import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { getApiError } from '../../shared/api-error';
import { MarcaPayload } from '../marca.model';
import { MarcaService } from '../marca.service';

@Component({
  selector: 'app-marca-formulario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './marca-formulario.html',
})
export class MarcaFormulario {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(MarcaService);

  protected readonly id = Number(this.route.snapshot.paramMap.get('id')) || null;
  protected readonly saving = signal(false);
  protected readonly loading = signal(!!this.id);
  protected readonly error = signal('');
  protected readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    descricao: ['', [Validators.maxLength(255)]],
    ativo: [true],
  });

  constructor() {
    if (this.id) {
      this.service.findById(this.id).subscribe({
        next: (marca) => {
          this.form.patchValue({ nome: marca.nome, descricao: marca.descricao ?? '', ativo: marca.ativo });
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
    const payload: MarcaPayload = {
      nome: value.nome.trim(),
      descricao: value.descricao.trim() || null,
      ativo: value.ativo,
    };
    const request = this.id ? this.service.update(this.id, payload) : this.service.create(payload);

    this.saving.set(true);
    this.error.set('');
    request.subscribe({
      next: () => this.router.navigate(['/marcas']),
      error: (error) => {
        this.error.set(getApiError(error));
        this.saving.set(false);
      },
    });
  }
}
