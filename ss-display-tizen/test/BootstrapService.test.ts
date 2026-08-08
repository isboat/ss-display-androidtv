import { BootstrapService } from '../src/api/BootstrapService';

describe('BootstrapService', () => {
  it('rejects non-HTTPS endpoint configuration', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => ({ 'display-api': { 'device-code-url': 'http://unsafe.test' } }) }));
    await expect(new BootstrapService().load()).rejects.toThrow('Invalid device-code-url');
  });
});
