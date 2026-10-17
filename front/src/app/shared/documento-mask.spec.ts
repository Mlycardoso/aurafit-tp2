import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { DocumentoMask } from './documento-mask';

@Component({imports: [ReactiveFormsModule, DocumentoMask], template: `
  <input documentoMask="cpf" [formControl]="cpf" />
  <input documentoMask="cnpj" [formControl]="cnpj" />`})
class Host {
  cpf = new FormControl('52998224725', Validators.pattern(/^[0-9]{11}$/));
  cnpj = new FormControl('12345678000195', [Validators.required, Validators.pattern(/^[A-Z0-9]{12}[0-9]{2}$/)]);
}

describe('Máscaras de documentos', () => {
  function setup() {
    const fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    const [cpf, cnpj] = Array.from(fixture.nativeElement.querySelectorAll('input')) as HTMLInputElement[];
    return {fixture, cpf, cnpj, host: fixture.componentInstance};
  }
  function digitar(input: HTMLInputElement, value: string, cursor = value.length) {
    input.value = value;
    input.setSelectionRange(cursor, cursor);
    input.dispatchEvent(new Event('input', {bubbles: true}));
  }
  it('formata valores carregados e mantém os controles sem pontuação', () => {
    const {cpf, cnpj, host} = setup();
    expect(cpf.value).toBe('529.982.247-25');
    expect(cnpj.value).toBe('12.345.678/0001-95');
    expect(host.cpf.value).toBe('52998224725');
    expect(host.cnpj.value).toBe('12345678000195');
  });
  it('aceita colagem com ou sem máscara e CNPJ com letras minúsculas', () => {
    const {cpf, cnpj, host} = setup();
    digitar(cpf, '529.982.247-25');
    expect(host.cpf.value).toBe('52998224725');
    digitar(cnpj, '12abc34501de35');
    expect(cnpj.value).toBe('12.ABC.345/01DE-35');
    expect(host.cnpj.value).toBe('12ABC34501DE35');
    expect(host.cnpj.valid).toBe(true);
    digitar(cnpj, '12.ABC.345/01DE-35');
    expect(host.cnpj.value).toBe('12ABC34501DE35');
  });
  it('preserva posição do cursor ao editar no meio', () => {
    const {cpf, host} = setup();
    digitar(cpf, '529.182.247-25', 5);
    expect(host.cpf.value).toBe('52918224725');
    expect(cpf.selectionStart).toBe(5);
  });
  it('permite apagar junto à pontuação sem prender o cursor', () => {
    const {cpf, host} = setup();
    cpf.setSelectionRange(4, 4);
    cpf.dispatchEvent(new InputEvent('beforeinput', {inputType: 'deleteContentBackward', cancelable: true}));
    expect(host.cpf.value).toBe('5298224725');
    expect(cpf.selectionStart).toBe(2);
    digitar(cpf, '52998224725');
    cpf.setSelectionRange(3, 3);
    cpf.dispatchEvent(new InputEvent('beforeinput', {inputType: 'deleteContentForward', cancelable: true}));
    expect(host.cpf.value).toBe('5298224725');
  });
  it('mantém validação de documentos incompletos e permite limpar CPF opcional', () => {
    const {cpf, cnpj, host} = setup();
    digitar(cpf, '529');
    expect(host.cpf.invalid).toBe(true);
    digitar(cpf, '');
    expect(host.cpf.valid).toBe(true);
    digitar(cnpj, '');
    expect(host.cnpj.invalid).toBe(true);
    digitar(cnpj, '12ABC34501DEAB');
    expect(host.cnpj.invalid).toBe(true);
  });
  it('respeita desabilitação e marca o campo como tocado', () => {
    const {cpf, host, fixture} = setup();
    cpf.dispatchEvent(new Event('blur'));
    expect(host.cpf.touched).toBe(true);
    host.cpf.disable();
    fixture.detectChanges();
    expect(cpf.disabled).toBe(true);
  });
});
