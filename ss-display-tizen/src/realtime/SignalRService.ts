import { HubConnection, HubConnectionBuilder, HttpTransportType, LogLevel } from '@microsoft/signalr';
import type { DeviceInfo, EndpointConfig } from '../models';
import { ApiClient } from '../api/ApiClient';

interface Negotiation { url: string; accessToken: string }
interface RealtimeMessage { messageType?: string; message?: string }

/** Maintains the application-scoped SignalR connection, registration, keep-alive, and command dispatch. */
export class SignalRService {
  private connection: HubConnection | null = null; private keepAliveTimer = 0;
  public constructor(private readonly api: ApiClient, private readonly onPublish: () => void, private readonly onNotice: (message: string, urgent: boolean) => void) {}

  public async connect(config: EndpointConfig, device: DeviceInfo): Promise<void> {
    await this.disconnect(config, device); const negotiationUrl = new URL(config.signalrNegotiationUrl); negotiationUrl.searchParams.set('deviceId', device.id);
    const negotiation = await this.api.request<Negotiation>(negotiationUrl.toString(), { method: 'POST' });
    this.connection = new HubConnectionBuilder().withUrl(negotiation.url, { accessTokenFactory: () => negotiation.accessToken, transport: HttpTransportType.WebSockets }).withAutomaticReconnect([0, 2_000, 10_000, 30_000]).configureLogging(LogLevel.Warning).build();
    this.connection.on('ReceiveChangeMessage', (payload: string) => this.dispatch(payload));
    this.connection.onreconnected(() => { void this.register(config, device); });
    await this.connection.start(); await this.register(config, device);
    this.keepAliveTimer = window.setInterval(() => { if (this.connection?.state === 'Connected') void this.connection.invoke('ManualKeepAlive'); }, 10_000);
  }

  /** Stops timers and unregisters the current TV when identifiers are still available. */
  public async disconnect(config?: EndpointConfig, device?: DeviceInfo): Promise<void> {
    window.clearInterval(this.keepAliveTimer); this.keepAliveTimer = 0;
    if (config && device && this.connection?.connectionId) { const url = this.registrationUrl(config.signalrRemoveConnectionUrl, device); try { await this.api.request(url, { method: 'POST' }); } catch { /* Shutdown is best-effort. */ } }
    if (this.connection) await this.connection.stop(); this.connection = null;
  }

  private async register(config: EndpointConfig, device: DeviceInfo): Promise<void> { if (this.connection?.connectionId) await this.api.request(this.registrationUrl(config.signalrAddConnectionUrl, device), { method: 'POST' }); }
  private registrationUrl(value: string, device: DeviceInfo): string { const url = new URL(value); url.searchParams.set('deviceId', device.id); url.searchParams.set('deviceName', device.deviceName); url.searchParams.set('connectionId', this.connection?.connectionId ?? ''); return url.toString(); }
  private dispatch(payload: string): void {
    if (payload.length > 64_000) return; let command: RealtimeMessage; try { command = JSON.parse(payload) as RealtimeMessage; } catch { return; }
    if (command.messageType === 'content.publish' || command.messageType === 'device.info.update') this.onPublish();
    else if (command.messageType === 'app.restart') window.location.reload();
    else if (command.messageType === 'app.terminate') window.tizen?.application.getCurrentApplication().exit();
    else if (command.messageType === 'app.upgrade.info' || command.messageType === 'operator.info') this.onNotice(command.message ?? '', command.messageType === 'operator.info');
  }
}
