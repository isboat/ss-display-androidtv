import DOMPurify from 'dompurify';
import type { ContentData, MenuItem } from '../models';
import { AvPlayAdapter } from '../media/AvPlayAdapter';

/** Maps server template keys to safe, full-screen DOM presentations. */
export class ContentRenderer {
  public constructor(private readonly player: AvPlayAdapter) {}
  public render(content: ContentData, host: HTMLElement): void {
    host.replaceChildren(); const key = content.layout?.templateKey?.toLowerCase() ?? '';
    if (key.includes('playlist')) return this.renderPlaylist(content, host);
    if (key.includes('datetime') || key.includes('clock')) return this.renderClock(content, host);
    if (key.includes('text')) return this.renderText(content.text ?? '', host);
    if (key.includes('menu') && !key.includes('overlay')) return this.renderMenu(content, host);
    this.renderMedia(content, host); if (key.includes('overlay')) this.renderMenu(content, host, true);
  }
  public stop(): void { this.player.stop(); }
  private renderMedia(content: ContentData, host: HTMLElement): void {
    const url = content.externalMediaSource ?? content.media?.url ?? content.media?.source;
    if (!url) return this.status(host, 'No media is assigned to this display.');
    if (content.externalMediaSource) { const frame = document.createElement('iframe'); frame.className = 'media'; frame.src = url; host.append(frame); return; }
    if (content.media?.type === 2) return this.player.play(url, host, true);
    const image = new Image(); image.className = 'media'; image.alt = ''; image.src = url; image.onerror = () => this.status(host, 'The image could not be loaded.'); host.append(image);
  }
  private renderMenu(content: ContentData, host: HTMLElement, overlay = false): void {
    const section = document.createElement('section'); section.className = `menu ${overlay ? 'overlay' : ''}`;
    const heading = document.createElement('h1'); heading.textContent = content.menu?.title ?? 'Menu'; section.append(heading);
    const list = document.createElement('div'); list.className = 'menu-grid'; for (const item of content.menu?.items ?? []) list.append(this.menuItem(item, content.menu?.currency)); section.append(list); host.append(section);
  }
  private menuItem(item: MenuItem, currency = ''): HTMLElement {
    const row = document.createElement('article'); row.className = 'menu-item'; const copy = document.createElement('div');
    const name = document.createElement('h2'); name.textContent = item.name; const description = document.createElement('p'); description.textContent = item.description ?? '';
    const price = document.createElement('div'); price.className = 'price'; price.innerHTML = item.discountedPrice ? `<s>${this.escape(item.price ?? '')}</s> ${this.escape(currency + item.discountedPrice)}` : this.escape(currency + (item.price ?? ''));
    copy.append(name, description); row.append(copy, price); return row;
  }
  private renderText(text: string, host: HTMLElement): void { const section = document.createElement('section'); section.className = 'rich-text'; section.innerHTML = DOMPurify.sanitize(text, { USE_PROFILES: { html: true } }); host.append(section); }
  private renderClock(content: ContentData, host: HTMLElement): void { const clock = document.createElement('time'); clock.className = 'clock'; host.append(clock); const update = () => { clock.textContent = new Intl.DateTimeFormat(undefined, { dateStyle: 'full', timeStyle: 'medium' }).format(new Date()); }; update(); window.setInterval(update, 1000); void content; }
  private renderPlaylist(content: ContentData, host: HTMLElement): void { this.status(host, content.playlistData?.itemsSerialized ? 'Playlist ready' : 'This playlist is empty.'); }
  private status(host: HTMLElement, message: string): void { host.replaceChildren(); const value = document.createElement('p'); value.className = 'empty'; value.textContent = message; host.append(value); }
  private escape(value: string): string { const node = document.createElement('span'); node.textContent = value; return node.innerHTML; }
}
