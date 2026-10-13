import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UsuarioFormulario } from './usuario-formulario';
import { UsuarioService } from './usuario.service';

describe('UsuarioFormulario', () => {
  let service: {buscar: ReturnType<typeof vi.fn>; salvar: ReturnType<typeof vi.fn>};
  function configurar(id: string | null = null) {
    service = {
      buscar: vi.fn().mockReturnValue(of({id: 1, nome: 'Ana Teste', email: 'ana@example.com', ativo: true, perfis: ['ATENDENTE']})),
      salvar: vi.fn().mockReturnValue(of({id: 1}))
    };
    TestBed.configureTestingModule({imports: [UsuarioFormulario], providers: [
      provideRouter([]), {provide: UsuarioService, useValue: service},
      {provide: ActivatedRoute, useValue: {snapshot: {paramMap: convertToParamMap(id ? {id} : {})}}}
    ]});
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(UsuarioFormulario);
    fixture.detectChanges();
    return fixture;
  }
  it('exige senha e perfil no cadastro', () => {
    const fixture = configurar();
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    form.dispatchEvent(new Event('submit', {bubbles: true, cancelable: true}));
    fixture.detectChanges();
    expect(service.salvar).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Selecione pelo menos um perfil');
    fixture.destroy();
  });
  it('mantem senha ausente ao editar apenas os dados', () => {
    const fixture = configurar('1');
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    form.dispatchEvent(new Event('submit', {bubbles: true, cancelable: true}));
    expect(service.salvar).toHaveBeenCalledWith(1, expect.objectContaining({senha: null, perfis: ['ATENDENTE']}));
    fixture.destroy();
  });
});
