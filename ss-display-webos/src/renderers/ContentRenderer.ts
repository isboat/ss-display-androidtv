import DOMPurify from 'dompurify';
import type { ContentData, MenuItem } from '../models';
import { PlaylistController } from '../content/PlaylistController';
import { HtmlVideoPlayer } from '../media/HtmlVideoPlayer';

/** Maps allow-listed backend template keys to safe, full-screen DOM presentations. */
export class ContentRenderer {
  private clockTimer = 0;
  private readonly playlist: PlaylistController;

  public constructor(private readonly player: HtmlVideoPlayer) { this.playlist = new PlaylistController(player); }

  /** Replaces the active renderer only after cancelling its timers and media resources. */
  public render(content: ContentData, host: HTMLElement): void {
    this.stop(); host.replaceChildren();
    switch (content.layout?.templateKey) {
      case 'MediaPlaylist': return this.renderPlaylist(content, host);
      case 'CurrentDateTime': return this.renderClock(host);
      case 'Text': return this.renderText(content.textEditorData ?? '', host);
      case 'MenuOnly': return this.renderMenu(content, host);
      case 'MenuOverlay': this.renderMedia(content, host); return this.renderMenu(content, host, true);
      case 'MediaOnly': return this.renderMedia(content, host);
      default: return this.status(host, 'Layout Key is not set, update screen and republish', 'No Layout Key');
    }
  }

  /** Cancels all renderer-scoped work and releases media resources. */
  public stop(): void { this.clearClock(); this.playlist.stop(); this.player.stop(); }

  private renderMedia(content: ContentData, host: HTMLElement): void {
    const url = content.externalMediaSource || content.mediaAsset?.assetUrl;
    if (!url) return this.status(host, 'No media is assigned to this display.');
    if (content.externalMediaSource) { const frame = document.createElement('iframe'); frame.className = 'media'; frame.src = url; frame.title = 'External display content'; host.append(frame); return; }
    if (content.mediaAsset?.type === 2) return this.player.play(url, host, true);
    if (content.mediaAsset?.type !== 1) return this.status(host, 'The assigned media type is unsupported.');
    const image = new Image(); image.className = 'media'; image.alt = content.mediaAsset.description ?? ''; image.src = url;
    image.onerror = () => this.status(host, 'The image could not be loaded.'); host.append(image);
  }

  private renderMenu(content: ContentData, host: HTMLElement, overlay = false): void {
    if (!content.menu?.menuItems?.length) return this.status(host, 'No menu items are available, update and republish.');
    const section = document.createElement('section'); section.className = `menu ${overlay ? 'overlay' : ''}`;
    const heading = document.createElement('h1'); heading.textContent = content.menu.title ?? 'Menu'; section.append(heading);
    const list = document.createElement('div'); list.className = 'menu-grid';
    for (const item of content.menu.menuItems) list.append(this.menuItem(item, content.menu.currency ?? ''));
    section.append(list); host.append(section);
  }

  private menuItem(item: MenuItem, currency: string): HTMLElement {
    const row = document.createElement('article'); row.className = 'menu-item'; const copy = document.createElement('div');
    const name = document.createElement('h2'); name.textContent = item.name ?? ''; const description = document.createElement('p'); description.textContent = item.description ?? '';
    const price = document.createElement('div'); price.className = 'price'; price.innerHTML = item.discountPrice ? `<s>${this.escape(currency + (item.price ?? ''))}</s> ${this.escape(currency + item.discountPrice)}` : this.escape(currency + (item.price ?? ''));
    copy.append(name, description); row.append(copy, price); return row;
  }

  private renderText(text: string, host: HTMLElement): void {
    if (!text.trim()) return this.status(host, 'Error: No text found in the data, republish.');
    const section = document.createElement('section'); section.className = 'rich-text'; section.innerHTML = DOMPurify.sanitize(text, { USE_PROFILES: { html: true } }); host.append(section);
  }

  private renderClock(host: HTMLElement): void {
    const clock = document.createElement('time'); clock.className = 'clock'; host.append(clock);
    const update = (): void => { clock.dateTime = new Date().toISOString(); clock.textContent = new Intl.DateTimeFormat(undefined, { dateStyle: 'full', timeStyle: 'medium' }).format(new Date()); };
    update(); this.clockTimer = window.setInterval(update, 1000);
  }

  private renderPlaylist(content: ContentData, host: HTMLElement): void { this.playlist.start(host, content.playlistData?.itemsSerialized ?? [], content.playlistData?.itemDuration); }
  private status(host: HTMLElement, message: string, title?: string): void { host.replaceChildren(); const value = document.createElement('section'); value.className = 'empty'; if (title) { const heading = document.createElement('h1'); heading.textContent = title; value.append(heading); } const detail = document.createElement('p'); detail.textContent = message; value.append(detail); host.append(value); }
  private escape(value: string): string { const node = document.createElement('span'); node.textContent = value; return node.innerHTML; }
  private clearClock(): void { window.clearInterval(this.clockTimer); this.clockTimer = 0; }
}
