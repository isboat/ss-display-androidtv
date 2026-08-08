import type { ContentData, DeviceInfo, EndpointConfig, Tokens } from '../models';

/** Typed persistence boundary so browser storage can be replaced in tests or future Tizen versions. */
export class StorageService {
  private readonly prefix = 'oss.';
  public get<T>(key: string): T | null { const value = localStorage.getItem(this.prefix + key); return value ? JSON.parse(value) as T : null; }
  public set<T>(key: string, value: T): void { localStorage.setItem(this.prefix + key, JSON.stringify(value)); }
  public remove(key: string): void { localStorage.removeItem(this.prefix + key); }
  public get config(): EndpointConfig | null { return this.get('config'); }
  public set config(value: EndpointConfig | null) { if (value) this.set('config', value); else this.remove('config'); }
  public get tokens(): Tokens | null { return this.get('tokens'); }
  public set tokens(value: Tokens | null) { if (value) this.set('tokens', value); else this.remove('tokens'); }
  public get device(): DeviceInfo | null { return this.get('device'); }
  public set device(value: DeviceInfo | null) { if (value) this.set('device', value); else this.remove('device'); }
  public get content(): ContentData | null { return this.get('content'); }
  public set content(value: ContentData | null) { if (value) this.set('content', value); else this.remove('content'); }
}
