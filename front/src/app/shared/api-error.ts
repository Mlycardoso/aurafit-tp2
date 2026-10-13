import { HttpErrorResponse } from '@angular/common/http';

export function getApiError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) return 'Não foi possível conectar ao servidor. Verifique se o back-end está em execução.';
    if (error.status === 404) return 'Registro não encontrado. Volte à listagem e tente novamente.';
    const fieldError = error.error?.errors?.[0]?.message;
    return fieldError ?? error.error?.detail ?? 'Não foi possível concluir a operação.';
  }
  return 'Não foi possível concluir a operação.';
}
