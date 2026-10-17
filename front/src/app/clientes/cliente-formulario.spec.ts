import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ClienteFormulario } from './cliente-formulario';
import { ClienteService } from './cliente.service';

describe('ClienteFormulario', () => {
  it('carrega telefones adicionais e salva a situacao do cliente', () => {
    const cliente = {
      id: 1, nome: 'Cliente Teste', email: 'cliente@example.com', telefone: '63999999999',
      telefones: ['63999999999', '1133334444'], cpf: '52998224725', dataNascimento: '2000-01-10',
      ativo: false, cep: '01001000', logradouro: 'Praca da Se', bairro: 'Se',
      numero: '10', complemento: '', municipio: {nome: 'Sao Paulo', estado: {sigla: 'SP'}}
    };
    const salvar = vi.fn().mockReturnValue(of(undefined));
    TestBed.configureTestingModule({imports: [ClienteFormulario], providers: [
      provideRouter([]),
      {provide: ActivatedRoute, useValue: {snapshot: {paramMap: convertToParamMap({id: '1'})}}},
      {provide: ClienteService, useValue: {buscar: () => of(cliente), salvar}}
    ]});
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(ClienteFormulario);
    fixture.detectChanges();
    const documento = fixture.nativeElement.querySelector('#cpf') as HTMLInputElement;
    expect(documento.value).toBe('529.982.247-25');
    documento.value = '529.982.247-25';
    documento.dispatchEvent(new Event('input', {bubbles: true}));
    expect((fixture.nativeElement.querySelector('#telefone-extra-0') as HTMLInputElement).value).toBe('1133334444');
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', {bubbles: true, cancelable: true}));
    expect(salvar).toHaveBeenCalledWith(1, expect.objectContaining({
      cpf: '52998224725', ativo: false, telefones: ['63999999999', '1133334444']
    }));
    fixture.destroy();
  });
});
