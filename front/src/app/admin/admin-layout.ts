import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../auth/auth.service';

@Component({
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss',
})
export class AdminLayout {
  protected readonly menuOpen = signal(false);
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  constructor() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntilDestroyed(),
    ).subscribe(() => this.closeMenu());
  }
  protected closeMenu(): void {
    this.menuOpen.set(false);
  }
  protected sair(): void { this.auth.logout().subscribe(() => this.router.navigate(['/login'])); }
}
