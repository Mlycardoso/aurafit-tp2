import { Component, signal, inject, effect } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router, NavigationEnd, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';

@Component({
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private readonly title = inject(Title);
  private readonly router = inject(Router);
  protected readonly storePage = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map(() => this.router.url === '/' || this.router.url.startsWith('/loja')),
    ),
    { initialValue: this.router.url === '/' || this.router.url.startsWith('/loja') },
  );
  protected readonly menuOpen = signal(false);
  constructor() {
    effect(() =>
      this.title.setTitle(this.storePage() ? 'AuraFit | Whey protein' : 'AuraFit | Administração'),
    );
  }

  protected closeMenu(): void {
    this.menuOpen.set(false);
  }
}
