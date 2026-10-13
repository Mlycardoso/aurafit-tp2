import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UsuarioService, Perfil, perfis } from './usuario.service';
import { getApiError } from '../shared/api-error';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './usuario-formulario.html'
})
export class UsuarioFormulario {
  private service = inject(UsuarioService);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  protected id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id')) || null;
  protected loading = signal(!!this.id);
  protected loadFailed = signal(false);
  protected saving = signal(false);
  protected error = signal('');
  protected options = perfis;
  protected selected = signal<Perfil[]>([]);
  protected submitted = signal(false);
  protected form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.pattern(/\S[\s\S]*\S/), Validators.minLength(2), Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
    senha: ['', [Validators.minLength(15), Validators.maxLength(128), Validators.pattern(/\S/)]],
    confirmacao: [''],
    ativo: [true]
  });

  constructor() {
    if (!this.id) this.form.controls.senha.addValidators(Validators.required);
    if (this.id) this.service.buscar(this.id).subscribe({
      next: u => {
        this.form.patchValue({nome: u.nome, email: u.email, ativo: u.ativo});
        this.selected.set(u.perfis); this.loading.set(false);
      },
      error: e => { this.error.set(getApiError(e)); this.loading.set(false); this.loadFailed.set(true); }
    });
  }
  protected toggle(perfil: Perfil, checked: boolean) {
    this.selected.update(current => checked ? [...current, perfil] : current.filter(p => p !== perfil));
  }
  protected save() {
    if (this.saving() || this.loading() || this.loadFailed()) return;
    this.submitted.set(true);
    this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    if (this.form.invalid || !this.selected().length || v.senha !== v.confirmacao) return;
    this.saving.set(true); this.error.set('');
    this.service.salvar(this.id, {
      nome: v.nome.trim(), email: v.email.trim(), ativo: v.ativo,
      perfis: this.selected(), senha: v.senha || null
    }).subscribe({
      next: () => { this.form.controls.senha.reset(); this.form.controls.confirmacao.reset(); this.router.navigate(['/usuarios']); },
      error: e => { this.error.set(getApiError(e)); this.saving.set(false); }
    });
  }
}
