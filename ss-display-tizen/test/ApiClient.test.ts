import { ApiClient, ApiError } from '../src/api/ApiClient';
import { StorageService } from '../src/platform/Storage';
import type { EndpointConfig } from '../src/models';

const config = Object.fromEntries([
  'deviceCodeUrl', 'deviceTokenRequestUrl', 'deviceRefreshTokenRequestUrl', 'deviceInfoUrl', 'contentDataUrl',
  'signalrNegotiationUrl', 'signalrAddConnectionUrl', 'signalrRemoveConnectionUrl',
].map(key => [key, `https://display.test/${key}`])) as unknown as EndpointConfig;

describe('ApiClient', () => {
  it('accepts a successful response with an empty body', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, text: async () => '' }));
    await expect(new ApiClient(new StorageService()).request<void>('https://display.test/register')).resolves.toBeUndefined();
  });

  it('exposes the response status for unsuccessful requests', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false, status: 428, text: async () => '' }));
    await expect(new ApiClient(new StorageService()).request('https://display.test/token')).rejects.toMatchObject({ status: 428 } satisfies Partial<ApiError>);
  });

  it('uses the backend refresh body contract', async () => {
    const storage = new StorageService(); storage.config = config; storage.tokens = { accessToken: 'old', refreshToken: 'refresh' };
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: false, status: 401, text: async () => '' })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ accessToken: 'new', refreshToken: 'next' }) })
      .mockResolvedValueOnce({ ok: true, status: 200, text: async () => '{"value":1}' });
    vi.stubGlobal('fetch', fetchMock);
    await expect(new ApiClient(storage).request<{ value: number }>(config.deviceInfoUrl)).resolves.toEqual({ value: 1 });
    const refreshInit = fetchMock.mock.calls[1]![1]!;
    expect(JSON.parse(refreshInit.body as string)).toEqual({ clientId: '', clientSecret: 'string', deviceCode: '', grantType: 'refresh_token' });
    expect(refreshInit.headers.Authorization).toBe('Bearer refresh');
  });
});
