import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from './auth.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './auth-page.scss',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  readonly busy = signal(false);
  readonly error = signal('');
  readonly showPassword = signal(false);
  readonly form = this.fb.nonNullable.group({ email: ['', [Validators.required, Validators.email]], senha: ['', Validators.required] });

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.busy()) return;
    this.busy.set(true); this.error.set('');
    const { email, senha } = this.form.getRawValue();
    this.auth.login(email.trim(), senha).pipe(finalize(() => this.busy.set(false))).subscribe({
      next: user => {
        const target = this.route.snapshot.queryParamMap.get('returnUrl');
        const safeTarget = target?.startsWith('/') && !target.startsWith('//') ? target : null;
        void this.router.navigateByUrl(safeTarget ?? (user.roles.includes('ADMIN') ? '/admin' : '/loja'));
      },
      error: error => this.error.set(error.status === 401 ? 'E-mail ou senha inválidos.' : error.status === 0 ? 'Não foi possível conectar à loja. Confira sua conexão e tente novamente.' : 'Não foi possível entrar agora. Tente novamente.'),
    });
  }
}
