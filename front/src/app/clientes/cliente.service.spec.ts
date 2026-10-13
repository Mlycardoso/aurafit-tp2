import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ClienteService } from './cliente.service';

describe('ClienteService', () => {
  let service: ClienteService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ClienteService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('envia filtro e pagina para a rota de nome', () => {
    service.listar(' Ana Maria ', 1).subscribe();
    const request = http.expectOne(r => r.url.endsWith('/clientes/nome/Ana%20Maria'));
    expect(request.request.params.get('page')).toBe('1');
    request.flush({items: [], page: 1, pageSize: 10, totalItems: 0, totalPages: 0});
  });
  it('lista todos quando o filtro esta vazio', () => {
    service.listar(' ', 0).subscribe();
    const request = http.expectOne(r => r.url.endsWith('/clientes'));
    request.flush({items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0});
  });
  it('exclui o cliente pela API', () => {
    service.delete(7).subscribe();
    const request = http.expectOne('http://localhost:8080/clientes/7');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, {status: 204, statusText: 'No Content'});
  });
});
