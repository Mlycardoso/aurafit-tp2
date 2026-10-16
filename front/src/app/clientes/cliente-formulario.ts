import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ClienteService } from './cliente.service';
import { getApiError } from '../shared/api-error';
@Component({
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cliente-formulario.html'
})
export class ClienteFormulario {
  private service = inject(ClienteService);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  protected id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id')) || null;
  protected error = signal(''); protected cepError = signal('');
  protected loading = signal(!!this.id); protected loadFailed = signal(false); protected saving = signal(false); protected consulting = signal(false);
  protected city = signal(''); protected uf = signal(''); protected confirmed = signal('');
  private consulta?: Subscription;
  protected today = new Date().toLocaleDateString('sv-SE');
  protected extras = this.fb.nonNullable.array<string>([]);
  protected adicionarTelefone() {
    if (this.extras.length < 4) this.extras.push(this.fb.nonNullable.control('', [Validators.required, Validators.pattern(/^[0-9]{10,11}$/)]));
  }
  protected removerTelefone(index: number) { this.extras.removeAt(index); }
  protected form = this.fb.nonNullable.group({
    cpf: ['', [Validators.pattern(/^[0-9]{11}$/)]],
    dataNascimento: ['', [(control: AbstractControl) => !control.value || control.value < this.today ? null : {past: true}]],
    ativo: [true],
    nome: ['', [Validators.required, Validators.pattern(/.*\S.*/), Validators.minLength(2), Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
    telefone: ['', [Validators.required, Validators.pattern(/^[0-9]{10,11}$/)]],
    cep: ['', [Validators.required, Validators.pattern(/^[0-9]{5}-?[0-9]{3}$/)]],
    logradouro: ['', [Validators.required, Validators.pattern(/.*\S.*/), Validators.maxLength(160)]],
    bairro: ['', [Validators.required, Validators.pattern(/.*\S.*/), Validators.maxLength(100)]],
    numero: ['', [Validators.required, Validators.pattern(/.*\S.*/), Validators.maxLength(20)]],
    complemento: ['', Validators.maxLength(100)]
  });
  constructor() {
    this.form.controls.cep.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => {
      this.consulta?.unsubscribe(); this.consulting.set(false); this.confirmed.set('');
      this.city.set(''); this.uf.set(''); this.cepError.set('');
      this.form.patchValue({logradouro: '', bairro: ''});
      if (this.form.controls.cep.valid) this.consultar();
    });
    if (this.id) this.service.buscar(this.id).subscribe({
      next: c => {
        this.form.patchValue({...c, cpf: c.cpf ?? '', dataNascimento: c.dataNascimento ?? '', ativo: c.ativo ?? true}, {emitEvent: false});
        const telefones = c.telefones?.length ? c.telefones : [c.telefone];
        this.form.controls.telefone.setValue(telefones[0]);
        this.extras.clear();
        telefones.slice(1).forEach(numero => {
          this.adicionarTelefone();
          this.extras.at(this.extras.length - 1).setValue(numero);
        });
        this.city.set(c.municipio.nome); this.uf.set(c.municipio.estado.sigla);
        this.confirmed.set(c.cep); this.loading.set(false);
      },
      error: e => { this.error.set(getApiError(e)); this.loadFailed.set(true); this.loading.set(false); }
    });
  }
  ngOnDestroy() { this.consulta?.unsubscribe(); }
  protected consultar() {
    if (this.form.controls.cep.invalid) { this.form.controls.cep.markAsTouched(); return; }
    const cep = this.form.controls.cep.value.replace('-', '');
    this.consulta?.unsubscribe(); this.consulting.set(true); this.confirmed.set(''); this.cepError.set('');
    this.consulta = this.service.consultar(cep).subscribe({
      next: c => {
        this.form.patchValue({logradouro: c.logradouro, bairro: c.bairro});
        this.city.set(c.municipio); this.uf.set(c.uf); this.confirmed.set(cep); this.consulting.set(false);
      },
      error: e => { this.cepError.set(e.status === 503 ? 'Consulta indisponível. Tente novamente.' : getApiError(e)); this.consulting.set(false); }
    });
  }
  protected save() {
    this.form.markAllAsTouched();
    this.extras.markAllAsTouched();
    if (this.form.invalid || this.extras.invalid || !this.confirmed() || this.saving() || this.consulting() || this.loadFailed()) return;
    const v = this.form.getRawValue();
    const telefones = [v.telefone, ...this.extras.getRawValue()];
    if (new Set(telefones).size !== telefones.length) { this.error.set('Não repita o mesmo telefone.'); return; }
    this.saving.set(true); this.error.set('');
    this.service.salvar(this.id, {...v, cpf: v.cpf || null, dataNascimento: v.dataNascimento || null, telefones, nome: v.nome.trim(), email: v.email.trim(), cep: v.cep.replace('-', '')}).subscribe({
      next: () => this.router.navigate(['/admin/clientes']),
      error: e => { this.error.set(e.status === 503 ? 'Não foi possível confirmar o CEP. Tente novamente.' : getApiError(e)); this.saving.set(false); }
    });
  }
}
