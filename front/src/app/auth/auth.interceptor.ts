import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { API_BASE_URL, AuthService } from './auth.service';

function isAuthRequest(request: HttpRequest<unknown>): boolean {
  return /\/auth\/(login|registro|refresh)$/.test(request.url);
}
const RETRIED = new HttpContextToken<boolean>(() => false);

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.accessToken();
  const apiRequest = request.url.startsWith(`${API_BASE_URL}/`);
  const outgoing = token && apiRequest
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(outgoing).pipe(catchError((error: unknown) => {
    if (!(error instanceof HttpErrorResponse)) return throwError(() => error);
    if (error.status === 403) {
      void router.navigate(['/acesso-negado']);
      return throwError(() => error);
    }
    if (error.status !== 401 || !apiRequest || isAuthRequest(request) || request.context.get(RETRIED)) {
      return throwError(() => error);
    }
    return auth.refresh().pipe(
      switchMap((freshToken) => next(request.clone({
        setHeaders: { Authorization: `Bearer ${freshToken}` },
        context: request.context.set(RETRIED, true),
      }))),
      catchError((refreshError) => {
        auth.limparSessao();
        void router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
        return throwError(() => refreshError);
      }),
    );
  }));
};
