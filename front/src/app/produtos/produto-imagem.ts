import { Component, inject, input, signal } from '@angular/core';
import { ProdutoService } from './produto.service';
import { getApiError } from '../shared/api-error';

@Component({
  selector: 'app-produto-imagem',
  template: `
    <section class="form-panel" aria-labelledby="imagem-titulo" [attr.aria-busy]="ocupado()">
      <h2 id="imagem-titulo">Imagem do produto</h2>
      <p>PNG ou JPEG de até 5 MB. Uma imagem por produto.</p>
      @if (url()) {
        <img class="preview" [src]="url()" alt="Imagem cadastrada do produto" (error)="erro.set('Não foi possível carregar a imagem. Tente novamente.')" />
      } @else { <p>Nenhuma imagem cadastrada.</p> }
      <div class="field">
        <label for="arquivo-produto">Selecionar imagem</label>
        <input id="arquivo-produto" type="file" accept="image/png,image/jpeg" [disabled]="ocupado()" (change)="selecionar($event)" aria-describedby="imagem-ajuda" />
        <small id="imagem-ajuda">A imagem enviada substituirá a atual. Máximo de 6000 pixels por lado e 16 milhões de pixels.</small>
      </div>
      @if (erro()) { <p class="alert error" role="alert">{{ erro() }}</p> }
      @if (mensagem()) { <p role="status">{{ mensagem() }}</p> }
      @if (ocupado()) { <p role="status">Processando imagem...</p> }
      <div class="form-actions">
        <button type="button" class="btn btn-primary" [disabled]="!arquivo() || ocupado()" (click)="enviar()">Enviar imagem</button>
        @if (url()) {
          <button type="button" class="btn btn-secondary" [disabled]="ocupado()" (click)="baixar()">Baixar imagem</button>
          <button type="button" class="btn btn-secondary" [disabled]="ocupado()" (click)="confirmar.set(true)">Remover imagem</button>
        }
      </div>
      @if (confirmar()) {
        <div role="group" aria-label="Confirmar remoção da imagem">
          <p>A imagem será removida do produto e do armazenamento.</p>
          <button class="btn btn-secondary" type="button" [disabled]="ocupado()" (click)="confirmar.set(false)">Cancelar</button>
          <button class="btn btn-primary" type="button" [disabled]="ocupado()" (click)="remover()">Confirmar remoção</button>
        </div>
      }
    </section>
  `,
  styles: `.preview { display: block; width: 100%; max-width: 280px; height: 240px; object-fit: contain; margin: 1rem 0; background: #fff; } .form-actions { flex-wrap: wrap; } input[type=file] { max-width: 100%; }`,
})
export class ProdutoImagem {
  readonly produtoId = input.required<number>();
  readonly inicial = input<string | null>(null);
  private readonly service = inject(ProdutoService);
  protected readonly atualizado = signal<string | null | undefined>(undefined);
  protected readonly arquivo = signal<File | null>(null);
  protected readonly ocupado = signal(false);
  protected readonly erro = signal('');
  protected readonly mensagem = signal('');
  protected readonly confirmar = signal(false);
  protected url(): string | null { return this.atualizado() === undefined ? this.inicial() : this.atualizado()!; }

  protected selecionar(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.arquivo.set(null); this.erro.set(''); this.mensagem.set('');
    if (!file) return;
    if (!['image/png', 'image/jpeg'].includes(file.type) || file.size === 0 || file.size > 5 * 1024 * 1024) {
      this.erro.set('Selecione uma imagem PNG ou JPEG, não vazia, de até 5 MB.'); input.value = ''; return;
    }
    this.arquivo.set(file);
  }
  protected enviar(): void {
    const file = this.arquivo();
    if (!file || this.ocupado()) return;
    this.iniciar();
    this.service.enviarImagem(this.produtoId(), file).subscribe({
      next: r => { this.atualizado.set(r.imagemUrl); this.arquivo.set(null); this.mensagem.set(r.aviso || 'Imagem salva. Ela já está disponível no catálogo.'); this.ocupado.set(false); },
      error: e => this.falhar(e),
    });
  }
  protected remover(): void {
    if (this.ocupado()) return;
    this.iniciar();
    this.service.removerImagem(this.produtoId()).subscribe({
      next: () => { this.atualizado.set(null); this.confirmar.set(false); this.mensagem.set('Imagem removida.'); this.ocupado.set(false); },
      error: e => this.falhar(e),
    });
  }
  protected baixar(): void {
    if (this.ocupado()) return;
    this.iniciar();
    this.service.baixarImagem(this.produtoId()).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob), a = document.createElement('a');
        a.href = url; a.download = 'produto-' + this.produtoId() + (blob.type === 'image/png' ? '.png' : '.jpg');
        a.click(); setTimeout(() => URL.revokeObjectURL(url), 1000);
        this.mensagem.set('Download iniciado.'); this.ocupado.set(false);
      }, error: e => this.falhar(e),
    });
  }
  private iniciar(): void { this.ocupado.set(true); this.erro.set(''); this.mensagem.set(''); }
  private falhar(e: unknown): void { this.erro.set(getApiError(e)); this.ocupado.set(false); }
}
