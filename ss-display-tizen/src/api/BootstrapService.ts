import type { EndpointConfig } from '../models';

const FIELD_MAP: Record<keyof EndpointConfig, string> = {
  deviceCodeUrl: 'device-code-url', deviceTokenRequestUrl: 'device-token-request-url', deviceRefreshTokenRequestUrl: 'device-refresh-token-request-url',
  deviceInfoUrl: 'device-info-url', contentDataUrl: 'content-data-url', signalrNegotiationUrl: 'signalr-negotiation-url',
  signalrAddConnectionUrl: 'signalr-add-connection-url', signalrRemoveConnectionUrl: 'signalr-remove-connection-url',
};

/** Loads and strictly validates the canonical dynamic endpoint document. */
export class BootstrapService {
  public async load(): Promise<EndpointConfig> {
    const response = await fetch('https://www.onscreensync.com/config.json');
    if (!response.ok) throw new Error(`Configuration unavailable (${response.status})`);
    const root = await response.json() as { 'display-api'?: Record<string, unknown> }; const source = root['display-api'];
    if (!source) throw new Error('Configuration is missing display-api');
    return Object.fromEntries(Object.entries(FIELD_MAP).map(([property, field]) => [property, this.httpsUrl(source[field], field)])) as unknown as EndpointConfig;
  }
  private httpsUrl(value: unknown, name: string): string { if (typeof value !== 'string' || new URL(value).protocol !== 'https:') throw new Error(`Invalid ${name}`); return value; }
}
