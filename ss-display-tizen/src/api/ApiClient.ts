import type { Tokens } from '../models';
import { StorageService } from '../platform/Storage';

/** Central HTTP gateway implementing timeout, trusted-origin authentication, and single-flight token refresh. */
export class ApiClient {
  private refreshPromise: Promise<boolean> | null = null;
  public constructor(private readonly storage: StorageService, private readonly timeoutMs = 15_000) {}

  public async request<T>(url: string, init: RequestInit = {}, retry = true): Promise<T> {
    const controller = new AbortController(); const timeout = window.setTimeout(() => controller.abort(), this.timeoutMs);
    const headers = new Headers(init.headers); headers.set('Accept', 'application/json');
    if (init.body) headers.set('Content-Type', 'application/json');
    if (this.isTrusted(url) && this.storage.tokens) headers.set('Authorization', `Bearer ${this.storage.tokens.accessToken}`);
    try {
      const response = await fetch(url, { ...init, headers, signal: controller.signal });
      if (response.status === 401 && retry && await this.refresh()) return this.request<T>(url, init, false);
      if (!response.ok) throw new Error(`Request failed (${response.status})`);
      return response.status === 204 ? undefined as T : await response.json() as T;
    } finally { window.clearTimeout(timeout); }
  }

  /** Refreshes credentials once even when several requests fail concurrently. */
  private refresh(): Promise<boolean> {
    if (this.refreshPromise) return this.refreshPromise;
    const refreshUrl = this.storage.config?.deviceRefreshTokenRequestUrl; const refreshToken = this.storage.tokens?.refreshToken;
    if (!refreshUrl || !refreshToken) return Promise.resolve(false);
    this.refreshPromise = fetch(refreshUrl, { method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${refreshToken}` }, body: JSON.stringify({ grantType: 'refresh_token', refreshToken }) })
      .then(async response => { if (!response.ok) throw new Error('Refresh rejected'); this.storage.tokens = await response.json() as Tokens; return true; })
      .catch(() => { this.storage.tokens = null; return false; }).finally(() => { this.refreshPromise = null; });
    return this.refreshPromise;
  }

  private isTrusted(url: string): boolean {
    const config = this.storage.config; if (!config) return false;
    const trusted = Object.values(config).map(value => new URL(value).origin); return trusted.includes(new URL(url).origin);
  }
}
