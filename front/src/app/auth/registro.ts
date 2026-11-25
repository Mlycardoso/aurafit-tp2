import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from './auth.service';

@Component({ standalone: true, imports: [ReactiveFormsModule, RouterLink], templateUrl: './registro.html', styleUrl: './auth-page.scss' })
export class Registro {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  readonly busy = signal(false);
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(15), Validators.maxLength(128)]],
    telefone: ['', [Validators.required, Validators.pattern(/^[0-9]{10,11}$/)]],
    cep: ['', [Validators.required, Validators.pattern(/^[0-9]{8}$/)]],
    logradouro: ['', Validators.required], bairro: ['', Validators.required], numero: ['', Validators.required], complemento: [''],
  });

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.busy()) return;
    this.busy.set(true); this.error.set('');
    const value = this.form.getRawValue();
    const payload = { senha: value.senha, cliente: { nome: value.nome.trim(), email: value.email.trim(), telefone: value.telefone,
      cep: value.cep, logradouro: value.logradouro.trim(), bairro: value.bairro.trim(), numero: value.numero.trim(),
      complemento: value.complemento || null, telefones: [value.telefone], ativo: true } };
    this.auth.registrar(payload).pipe(finalize(() => this.busy.set(false))).subscribe({
      next: () => void this.router.navigate(['/loja']),
      error: error => this.error.set(error.status === 409 ? 'Este e-mail já possui uma conta.' : error.status === 0 ? 'Não foi possível conectar à loja. Tente novamente.' : 'Confira os dados informados e tente novamente.'),
    });
  }
}
