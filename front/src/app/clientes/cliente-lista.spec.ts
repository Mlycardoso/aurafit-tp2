import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ClienteLista } from './cliente-lista';
import { ClienteService } from './cliente.service';

describe('ClienteLista', () => {
  const cliente = {id: 1, nome: 'Cliente Teste', email: 'teste@example.com',
    municipio: {nome: 'Palmas', estado: {sigla: 'TO'}}};
  let service: {listar: ReturnType<typeof vi.fn>; delete: ReturnType<typeof vi.fn>};

  beforeEach(() => {
    service = {
      listar: vi.fn().mockReturnValue(of({items: [cliente], page: 0, totalPages: 1, totalItems: 1})),
      delete: vi.fn().mockReturnValue(of(undefined))
    };
    TestBed.configureTestingModule({
      imports: [ClienteLista], providers: [provideRouter([]), {provide: ClienteService, useValue: service}]
    });
  });
  afterEach(() => vi.restoreAllMocks());

  it('respeita cancelamento e confirma exclusao pela tela', async () => {
    const fixture = TestBed.createComponent(ClienteLista);
    fixture.detectChanges();
    await vi.waitFor(() => expect(service.listar).toHaveBeenCalled());
    fixture.detectChanges();
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false);
    const button = fixture.nativeElement.querySelector('[title="Excluir cliente"]') as HTMLButtonElement;
    button.click();
    expect(service.delete).not.toHaveBeenCalled();
    confirm.mockReturnValue(true);
    button.click();
    expect(service.delete).toHaveBeenCalledWith(1);
    fixture.destroy();
  });

  it('busca apenas o ultimo texto digitado', async () => {
    const fixture = TestBed.createComponent(ClienteLista);
    fixture.detectChanges();
    await vi.waitFor(() => expect(service.listar).toHaveBeenCalled());
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    input.value = 'A'; input.dispatchEvent(new Event('input'));
    input.value = 'Ana'; input.dispatchEvent(new Event('input'));
    await vi.waitFor(() => expect(service.listar).toHaveBeenLastCalledWith('Ana', 0));
    expect(service.listar).not.toHaveBeenCalledWith('A', 0);
    fixture.destroy();
  });
});
