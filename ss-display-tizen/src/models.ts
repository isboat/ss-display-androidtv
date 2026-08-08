export interface EndpointConfig {
  deviceCodeUrl: string; deviceTokenRequestUrl: string; deviceRefreshTokenRequestUrl: string;
  deviceInfoUrl: string; contentDataUrl: string; signalrNegotiationUrl: string;
  signalrAddConnectionUrl: string; signalrRemoveConnectionUrl: string;
}
export interface Tokens { accessToken: string; refreshToken: string }
export interface DeviceInfo { id: string; deviceName: string; tenantId: string }
export interface Media { type: number; url?: string; source?: string }
export interface MenuItem { name: string; description?: string; price?: string; discountedPrice?: string; imageUrl?: string }
export interface ContentData {
  checksum?: string; layout?: { templateKey?: string; templateProperties?: Record<string, string> };
  media?: Media; externalMediaSource?: string; text?: string; menu?: { title?: string; currency?: string; items?: MenuItem[] };
  playlistData?: { itemsSerialized?: string; itemDuration?: string };
}
export type Route = 'splash' | 'activation' | 'status' | 'content';
