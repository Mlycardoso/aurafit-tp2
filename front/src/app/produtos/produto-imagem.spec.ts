import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ProdutoImagem } from './produto-imagem';

describe('Imagem do produto', () => {
  function setup(inicial: string | null = null) {
    TestBed.configureTestingModule({ imports: [ProdutoImagem], providers: [provideHttpClient(), provideHttpClientTesting()] });
    const fixture = TestBed.createComponent(ProdutoImagem);
    fixture.componentRef.setInput('produtoId', 7);
    fixture.componentRef.setInput('inicial', inicial);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    const selecionar = (file: File) => {
      const input = fixture.nativeElement.querySelector('input');
      Object.defineProperty(input, 'files', { value: [file], configurable: true });
      input.dispatchEvent(new Event('change')); fixture.detectChanges();
    };
    return { fixture, http, selecionar };
  }
  afterEach(() => TestBed.inject(HttpTestingController).verify());
  it('valida tipo e tamanho sem enviar arquivos inválidos', () => {
    const { fixture, selecionar } = setup();
    selecionar(new File(['texto'], 'x.svg', { type: 'image/svg+xml' }));
    expect(fixture.nativeElement.querySelector('[role=alert]').textContent).toContain('PNG ou JPEG');
    selecionar(new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'x.png', { type: 'image/png' }));
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(true);
  });
  it('envia multipart uma vez e só mostra sucesso depois da resposta', () => {
    const { fixture, http, selecionar } = setup();
    selecionar(new File(['imagem'], 'foto.png', { type: 'image/png' }));
    const enviar = fixture.nativeElement.querySelector('button');
    enviar.click(); fixture.detectChanges(); enviar.click();
    const req = http.expectOne('http://localhost:8080/produtos/7/imagem');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.get('arquivo').name).toBe('foto.png');
    expect(fixture.nativeElement.textContent).not.toContain('Imagem salva.');
    expect(enviar.disabled).toBe(true);
    req.flush({ imagemUrl: '/produtos/7/imagem?v=abc', aviso: null }); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('img').src).toBe('http://localhost:8080/produtos/7/imagem?v=abc');
    expect(fixture.nativeElement.textContent).toContain('Imagem salva.');
  });
  it('preserva imagem e permite repetir upload após falha', () => {
    const { fixture, http, selecionar } = setup('http://localhost:8080/antiga');
    selecionar(new File(['imagem'], 'foto.png', { type: 'image/png' }));
    fixture.nativeElement.querySelector('button').click();
    http.expectOne('http://localhost:8080/produtos/7/imagem').flush({ detail: 'Armazenamento indisponível' }, { status: 503, statusText: 'Unavailable' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('img').src).toContain('/antiga');
    expect(fixture.nativeElement.querySelector('[role=alert]').textContent).toContain('Armazenamento indisponível');
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(false);
  });
  it('exige confirmação, remove e exibe estado vazio', () => {
    const { fixture, http } = setup('http://localhost:8080/imagem');
    const btn = (text: string) => Array.from(fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>).find(b => b.textContent?.includes(text))!;
    btn('Remover imagem').click(); fixture.detectChanges();
    http.expectNone('http://localhost:8080/produtos/7/imagem');
    btn('Confirmar remoção').click();
    const req = http.expectOne('http://localhost:8080/produtos/7/imagem');
    expect(req.request.method).toBe('DELETE'); req.flush(null); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('img')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Nenhuma imagem cadastrada');
  });
});
