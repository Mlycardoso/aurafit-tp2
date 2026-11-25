import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({ standalone: true, imports: [RouterLink], template: `<main class="auth-shell"><section class="auth-card"><a class="brand" routerLink="/loja"><img src="/logo-aurafit.png" alt="" /><span>AuraFit<small>WHEY, DO SEU JEITO.</small></span></a><p class="eyebrow">403 · Acesso negado</p><h1>Esta área não está disponível para sua conta.</h1><p class="intro">Volte à loja ou entre com uma conta autorizada para continuar.</p><a class="primary" routerLink="/loja">Ir para a loja</a><a class="back" routerLink="/login">Entrar com outra conta</a></section></main>`, styleUrl: './auth-page.scss' })
export class AcessoNegado {}
