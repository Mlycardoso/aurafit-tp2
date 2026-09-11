import { HttpErrorResponse } from '@angular/common/http';

export function getApiError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const fieldError = error.error?.errors?.[0]?.message;
    return fieldError ?? error.error?.detail ?? 'Não foi possível concluir a operação.';
  }
  return 'Não foi possível concluir a operação.';
}
