import { Directive, ElementRef, forwardRef, inject, Input, Pipe, PipeTransform } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

type Documento = 'cpf' | 'cnpj';

export function limparDocumento(value: string, tipo: Documento): string {
  return tipo === 'cpf' ? value.replace(/\D/g, '') : value.toUpperCase().replace(/[^A-Z0-9]/g, '');
}

export function formatarDocumento(value: string | null | undefined, tipo: Documento): string {
  const raw = limparDocumento(value ?? '', tipo);
  const grupos = tipo === 'cpf' ? [3, 3, 3, 2] : [2, 3, 3, 4, 2];
  const separadores = tipo === 'cpf' ? ['.', '.', '-'] : ['.', '.', '/', '-'];
  let offset = 0;
  return grupos.map((tamanho, i) => {
    const parte = raw.slice(offset, offset + tamanho);
    offset += tamanho;
    return parte ? (i ? separadores[i - 1] : '') + parte : '';
  }).join('');
}

@Pipe({name: 'documento'})
export class DocumentoPipe implements PipeTransform {
  transform(value: string | null | undefined, tipo: Documento): string {
    return formatarDocumento(value, tipo);
  }
}

@Directive({
  selector: 'input[documentoMask]',
  providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => DocumentoMask), multi: true}],
  host: {'(input)': 'onInput()', '(blur)': 'onTouched()', '(beforeinput)': 'beforeInput($event)'}
})
export class DocumentoMask implements ControlValueAccessor {
  @Input({required: true}) documentoMask!: Documento;
  private input = inject<ElementRef<HTMLInputElement>>(ElementRef).nativeElement;
  private onChange: (value: string) => void = () => {};
  protected onTouched: () => void = () => {};

  writeValue(value: string | null): void {
    this.input.value = formatarDocumento(value, this.documentoMask);
  }
  registerOnChange(fn: (value: string) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(disabled: boolean): void { this.input.disabled = disabled; }

  protected onInput(): void {
    const value = this.input.value;
    const position = this.input.selectionStart ?? value.length;
    const count = limparDocumento(value.slice(0, position), this.documentoMask).length;
    const raw = limparDocumento(value, this.documentoMask).slice(0, this.documentoMask === 'cpf' ? 11 : 14);
    this.writeValue(raw);
    let caret = 0;
    let seen = 0;
    while (caret < this.input.value.length && seen < count) {
      if (/[A-Z0-9]/.test(this.input.value[caret])) seen++;
      caret++;
    }
    this.input.setSelectionRange(caret, caret);
    this.onChange(raw);
  }

  protected beforeInput(event: InputEvent): void {
    const start = this.input.selectionStart ?? 0;
    if (start !== this.input.selectionEnd) return;
    const backward = event.inputType === 'deleteContentBackward';
    const forward = event.inputType === 'deleteContentForward';
    if (!backward && !forward) return;
    const value = this.input.value;
    const index = backward ? start - 1 : start;
    if (index < 0 || index >= value.length || /[A-Z0-9]/.test(value[index])) return;
    event.preventDefault();
    const from = backward ? Math.max(0, index - 1) : start;
    const to = backward ? start : Math.min(value.length, start + 2);
    this.input.value = value.slice(0, from) + value.slice(to);
    this.input.setSelectionRange(from, from);
    this.onInput();
  }
}
