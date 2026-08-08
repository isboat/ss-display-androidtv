import { BootstrapService } from '../src/api/BootstrapService';

describe('BootstrapService', () => {
  const routes = {
    'device-code-url': '/device/code', 'device-token-request-url': '/device/token', 'device-refresh-token-request-url': '/device/token/refresh',
    'device-info-url': '/device/info', 'content-data-url': '/display/content', 'signalr-negotiation-url': '/signalr/negotiate',
    'signalr-add-connection-url': '/signalr/add', 'signalr-remove-connection-url': '/signalr/remove',
  };

  it('resolves relative routes against the HTTPS base endpoint', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => ({ 'display-api': { 'base-endpoint': 'https://display.test/api/', ...routes } }) }));
    const config = await new BootstrapService().load();
    expect(config.deviceCodeUrl).toBe('https://display.test/device/code');
    expect(config.signalrRemoveConnectionUrl).toBe('https://display.test/signalr/remove');
  });

  it('rejects non-HTTPS endpoint configuration', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => ({ 'display-api': { 'base-endpoint': 'https://display.test', ...routes, 'device-code-url': 'http://unsafe.test' } }) }));
    await expect(new BootstrapService().load()).rejects.toThrow('Invalid device-code-url');
  });
});
