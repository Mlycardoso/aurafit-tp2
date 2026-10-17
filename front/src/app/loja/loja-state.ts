import { Injectable, computed, signal } from '@angular/core';
import { Produto } from '../produtos/produto.model';

export interface CartItem {
  id: number;
  quantity: number;
}
export interface ShopProduct extends Produto {
  sabor?: string | null;
  peso?: string;
  imagemUrl?: string;
}
export function flavor(p: ShopProduct): string {
  return p.sabor || p.nome.match(/chocolate|baunilha|morango|natural/i)?.[0] || '';
}
export function weight(p: ShopProduct): string {
  return p.pesoGramas ? `${p.pesoGramas} g` : p.peso || p.nome.match(/\d+(?:[.,]\d+)?\s*(?:kg|g)\b/i)?.[0] || '';
}
export function readStored(key: string): unknown {
  try {
    return JSON.parse(localStorage.getItem(key) || '[]');
  } catch {
    return [];
  }
}
@Injectable({ providedIn: 'root' })
export class LojaState {
  readonly cart = signal<CartItem[]>(this.restoreCart());
  readonly favorites = signal<number[]>(this.restoreFavorites());
  readonly count = computed(() => this.cart().reduce((sum, row) => sum + row.quantity, 0));
  readonly notice = signal('');
  private timer?: ReturnType<typeof setTimeout>;
  private restoreCart(): CartItem[] {
    const data = readStored('aurafit.cart.v1');
    if (!Array.isArray(data)) return [];
    const unique = new Map<number, CartItem>();
    for (const row of data) {
      if (
        row &&
        Number.isSafeInteger(row.id) &&
        row.id > 0 &&
        Number.isSafeInteger(row.quantity) &&
        row.quantity > 0
      ) {
        unique.set(row.id, { id: row.id, quantity: Math.min(row.quantity, 99) });
      }
    }
    return [...unique.values()];
  }
  private restoreFavorites(): number[] {
    const data = readStored('aurafit.favorites.v1');
    return Array.isArray(data)
      ? [...new Set(data.filter((id) => Number.isSafeInteger(id) && id > 0))]
      : [];
  }
  private persist(key: string, data: unknown): void {
    try {
      localStorage.setItem(key, JSON.stringify(data));
    } catch {
      this.say('O navegador não permitiu salvar sua seleção.');
    }
  }
  say(message: string): void {
    this.notice.set(message);
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.notice.set(''), 4000);
  }
  favorite(id: number): void {
    this.favorites.update((ids) => (ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id]));
    this.persist('aurafit.favorites.v1', this.favorites());
  }
  add(product: ShopProduct, quantity = 1): void {
    if (!product.ativo || product.estoque < 1 || !Number.isSafeInteger(quantity) || quantity < 1)
      return;
    const current = this.cart().find((row) => row.id === product.id)?.quantity || 0;
    if (current + quantity > Math.min(product.estoque, 99)) {
      this.say('Você já selecionou a quantidade disponível.');
      return;
    }
    this.cart.update((rows) => [
      ...rows.filter((row) => row.id !== product.id),
      { id: product.id, quantity: current + quantity },
    ]);
    this.persist('aurafit.cart.v1', this.cart());
    this.say('Whey adicionado ao carrinho.');
  }
  quantity(id: number, quantity: number, stock: number): void {
    if (!Number.isSafeInteger(quantity) || quantity < 1 || quantity > Math.min(stock, 99)) return;
    this.cart.update((rows) => rows.map((row) => (row.id === id ? { ...row, quantity } : row)));
    this.persist('aurafit.cart.v1', this.cart());
  }
  remove(id: number): void {
    this.cart.update((rows) => rows.filter((row) => row.id !== id));
    this.persist('aurafit.cart.v1', this.cart());
  }
}
