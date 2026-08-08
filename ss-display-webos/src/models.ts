/** Fully resolved and validated backend endpoints. */
export interface EndpointConfig {
  deviceCodeUrl: string; deviceTokenRequestUrl: string; deviceRefreshTokenRequestUrl: string;
  deviceInfoUrl: string; contentDataUrl: string; signalrNegotiationUrl: string;
  signalrAddConnectionUrl: string; signalrRemoveConnectionUrl: string;
}
export interface Tokens { accessToken: string; refreshToken: string }
export interface DeviceInfo { id: string; deviceName: string; tenantId: string }
export interface TemplateProperty { key?: string | null; value?: string | null; label?: string | null }
export interface Layout { templateKey?: string | null; subType?: string | null; templateProperties?: TemplateProperty[] | null }
export interface MediaAsset { type?: number; assetUrl?: string | null; description?: string | null; name?: string | null }
export interface MenuItem { name?: string | null; description?: string | null; price?: string | null; discountPrice?: string | null; iconUrl?: string | null }
export interface Menu { name?: string | null; description?: string | null; title?: string | null; currency?: string | null; iconUrl?: string | null; menuItems?: MenuItem[] | null }
export interface PlaylistItemSerialized { key?: string | null; value?: string | null }
export interface PlaylistData { itemDuration?: string | null; items?: unknown[] | null; itemsSerialized?: PlaylistItemSerialized[] | null }
export interface ContentData {
  id?: string | null; tenantId?: string | null; displayName?: string | null; checksum?: string | null;
  layout?: Layout | null; menu?: Menu | null; mediaAsset?: MediaAsset | null;
  externalMediaSource?: string | null; textEditorData?: string | null; playlistData?: PlaylistData | null;
}
export type Route = 'splash' | 'activation' | 'status' | 'content';
