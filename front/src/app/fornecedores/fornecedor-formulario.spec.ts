import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { FornecedorFormulario } from './fornecedor-formulario';
import { FornecedorService } from './fornecedor.service';

describe('FornecedorFormulario', () => {
  it('carrega telefones adicionais e salva a situacao do fornecedor', () => {
    const fornecedor = {
      id: 1, nome: 'Fornecedor Teste', email: 'fornecedor@example.com', telefone: '63999999999',
      telefones: ['63999999999', '1133334444'], cnpj: '12345678000195',
      ativo: false, cep: '01001000', logradouro: 'Praca da Se', bairro: 'Se',
      numero: '10', complemento: '', municipio: {nome: 'Sao Paulo', estado: {sigla: 'SP'}}
    };
    const salvar = vi.fn().mockReturnValue(of(undefined));
    TestBed.configureTestingModule({imports: [FornecedorFormulario], providers: [
      provideRouter([]),
      {provide: ActivatedRoute, useValue: {snapshot: {paramMap: convertToParamMap({id: '1'})}}},
      {provide: FornecedorService, useValue: {buscar: () => of(fornecedor), salvar}}
    ]});
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(FornecedorFormulario);
    fixture.detectChanges();
    expect((fixture.nativeElement.querySelector('#telefone-extra-0') as HTMLInputElement).value).toBe('1133334444');
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', {bubbles: true, cancelable: true}));
    expect(salvar).toHaveBeenCalledWith(1, expect.objectContaining({
      cnpj: '12345678000195', ativo: false, telefones: ['63999999999', '1133334444']
    }));
    fixture.destroy();
  });
});
