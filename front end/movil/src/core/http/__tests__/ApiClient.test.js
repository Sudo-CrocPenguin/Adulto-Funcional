import { ApiClient } from '../ApiClient';
import { ApiError } from '../ApiError';

function response({ ok, status, payload }) {
  return {
    ok,
    status,
    text: jest.fn().mockResolvedValue(JSON.stringify(payload)),
  };
}

describe('ApiClient', () => {
  it('identifica el cliente móvil y desenvuelve data', async () => {
    const fetchImplementation = jest.fn().mockResolvedValue(
      response({
        ok: true,
        status: 201,
        payload: { status: 201, message: 'Cuenta creada', data: { id: '1' } },
      }),
    );
    const client = new ApiClient({
      baseUrl: 'http://localhost:8080',
      fetchImplementation,
    });

    await expect(client.post('/api/auth/register', { email: 'a@b.co' }))
      .resolves.toEqual({ id: '1' });
    expect(fetchImplementation).toHaveBeenCalledWith(
      'http://localhost:8080/api/auth/register',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({
          'X-Client-Type': 'mobile',
          'User-Agent': expect.stringContaining('MiApp/'),
        }),
      }),
    );
  });

  it('conserva el contrato de error y sus errores de campo', async () => {
    const fetchImplementation = jest.fn().mockResolvedValue(
      response({
        ok: false,
        status: 400,
        payload: {
          code: 'VALIDATION_ERROR',
          message: 'Revisa la solicitud',
          fieldErrors: [
            { field: 'email', code: 'Email', message: 'Email inválido' },
          ],
          traceId: 'trace-1',
        },
      }),
    );
    const client = new ApiClient({
      baseUrl: 'http://localhost:8080',
      fetchImplementation,
    });

    await expect(client.post('/api/auth/register', {})).rejects.toMatchObject({
      name: ApiError.name,
      status: 400,
      code: 'VALIDATION_ERROR',
      traceId: 'trace-1',
    });
  });

  it('conserva datos y metadatos al consultar una página autenticada', async () => {
    const fetchImplementation = jest.fn().mockResolvedValue(
      response({
        ok: true,
        status: 200,
        payload: {
          data: [{ id: 'movement-1' }],
          page: {
            hasNext: false,
            number: 0,
            size: 1,
            totalElements: 1,
            totalPages: 1,
          },
          status: 200,
        },
      }),
    );
    const client = new ApiClient({
      baseUrl: 'http://localhost:8080',
      fetchImplementation,
    });

    await expect(client.getPage('/api/finances/movements?page=0', {
      headers: { Authorization: 'Bearer access-token' },
    })).resolves.toEqual({
      items: [{ id: 'movement-1' }],
      page: expect.objectContaining({ totalElements: 1 }),
    });
    expect(fetchImplementation).toHaveBeenCalledWith(
      'http://localhost:8080/api/finances/movements?page=0',
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: 'Bearer access-token',
        }),
        method: 'GET',
      }),
    );
  });
});
