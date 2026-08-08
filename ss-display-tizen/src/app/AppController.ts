import type { ContentData, DeviceInfo, Tokens } from '../models';
import { ApiClient, ApiError } from '../api/ApiClient';
import { BootstrapService } from '../api/BootstrapService';
import { StorageService } from '../platform/Storage';
import { Router } from '../screens/Router';
import { SignalRService } from '../realtime/SignalRService';

interface DeviceCode { clientId: string; deviceCode: string; userCode: string; verificationUrl: string; interval?: number; expiresIn?: number }

/** Coordinates bootstrap, activation, identity, and content without coupling views to transport details. */
export class AppController {
  private readonly api: ApiClient; private readonly realtime: SignalRService; private polling = false;
  public constructor(private readonly storage: StorageService, private readonly bootstrap: BootstrapService, private readonly router: Router) {
    this.api = new ApiClient(storage); this.realtime = new SignalRService(this.api, () => { void this.loadDisplay(); }, (message, urgent) => this.showNotice(message, urgent));
  }
  public async start(): Promise<void> {
    this.router.show('splash');
    try { this.storage.config = await this.bootstrap.load(); } catch (error) { if (!this.storage.config) return this.fail(error); }
    if (!this.storage.tokens) return this.activate(); await this.loadDisplay();
  }
  public retry(): void { this.polling = false; void this.start(); }
  private async activate(): Promise<void> {
    try {
      const config = this.requiredConfig(); const code = await this.api.request<DeviceCode>(config.deviceCodeUrl, { method: 'POST', body: JSON.stringify({ clientId: 'clientid', grantType: 'user_code' }) });
      this.router.show('activation', { code: code.userCode, url: code.verificationUrl }); this.polling = true;
      const expiresAt = Date.now() + (code.expiresIn ?? 600) * 1000;
      while (this.polling && Date.now() < expiresAt) { await this.delay((code.interval ?? 5) * 1000); try { this.storage.tokens = await this.api.request<Tokens>(config.deviceTokenRequestUrl, { method: 'POST', body: JSON.stringify({ clientId: code.clientId, clientSecret: '', deviceCode: code.deviceCode, grantType: 'urn:ietf:params:oauth:grant-type:access_token' }) }); this.polling = false; return this.loadDisplay(); } catch (error) { if (!(error instanceof ApiError) || error.status !== 428) throw error; } }
      throw new Error('The activation code expired.');
    } catch (error) { this.fail(error); }
  }
  private async loadDisplay(): Promise<void> { try { const config = this.requiredConfig(); const device = await this.api.request<DeviceInfo>(config.deviceInfoUrl); this.storage.device = device; const content = await this.api.request<ContentData>(config.contentDataUrl); this.storage.content = content; this.router.show('content', { content }); void this.realtime.connect(config, device).catch(() => { /* Content remains usable while realtime reconnects on the next refresh. */ }); } catch (error) { this.fail(error); } }
  private requiredConfig() { if (!this.storage.config) throw new Error('Endpoint configuration is unavailable.'); return this.storage.config; }
  private fail(error: unknown): void { this.router.show('status', { message: error instanceof Error ? error.message : 'An unexpected error occurred.' }); }
  private showNotice(message: string, urgent: boolean): void { const banner = document.querySelector<HTMLElement>('#banner'); if (!banner) return; banner.textContent = message; banner.style.background = urgent ? '#b42333' : '#050b14'; banner.style.padding = '2rem'; window.setTimeout(() => { banner.textContent = ''; banner.removeAttribute('style'); }, 10_000); }
  private delay(milliseconds: number): Promise<void> { return new Promise(resolve => window.setTimeout(resolve, milliseconds)); }
}
