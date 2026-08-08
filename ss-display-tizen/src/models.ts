export interface EndpointConfig {
  deviceCodeUrl: string; deviceTokenRequestUrl: string; deviceRefreshTokenRequestUrl: string;
  deviceInfoUrl: string; contentDataUrl: string; signalrNegotiationUrl: string;
  signalrAddConnectionUrl: string; signalrRemoveConnectionUrl: string;
}
export interface Tokens { accessToken: string; refreshToken: string }
export interface DeviceInfo { id: string; deviceName: string; tenantId: string }
export interface MediaAsset { type: number; assetUrl?: string | null; description?: string | null; name?: string | null }
export interface MenuItem { name?: string | null; description?: string | null; price?: string | null; discountPrice?: string | null; iconUrl?: string | null }
export interface ContentData {
  checksum?: string; layout?: { templateKey?: string; templateProperties?: Record<string, string> };
  mediaAsset?: MediaAsset | null; externalMediaSource?: string | null; textEditorData?: string | null;
  menu?: { title?: string | null; currency?: string | null; menuItems?: MenuItem[] | null } | null;
  playlistData?: { itemsSerialized?: unknown[] | null; itemDuration?: string | null } | null;
}
export type Route = 'splash' | 'activation' | 'status' | 'content';
