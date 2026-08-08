import DOMPurify from 'dompurify';
import type { MediaAsset, PlaylistItemSerialized } from '../models';
import { HtmlVideoPlayer } from '../media/HtmlVideoPlayer';

interface TextPlaylistItem { description?: string; backgroundColor?: string; textColor?: string }

/** Plays serialized playlist entries sequentially with one timer and a malformed-item cycle guard. */
export class PlaylistController {
  private timer = 0;
  private generation = 0;
  public constructor(private readonly player: HtmlVideoPlayer) {}

  /** Starts circular playback; videos advance on completion and static entries use the configured duration. */
  public start(host: HTMLElement, items: PlaylistItemSerialized[], durationValue: string | null | undefined): void {
    this.stop();
    if (!items.length) return this.status(host, 'No item in the playlist, please add items and republish.');
    const generation = this.generation; const duration = this.parseDuration(durationValue);
    const next = (from: number): void => {
      if (generation !== this.generation) return;
      for (let offset = 0; offset < items.length; offset += 1) {
        const index = (from + offset) % items.length;
        if (this.render(host, items[index], duration, () => next((index + 1) % items.length))) return;
      }
      this.status(host, 'No valid item in the playlist, please update and republish.');
    };
    next(0);
  }

  /** Cancels pending advancement and releases the current decoder. */
  public stop(): void { this.generation += 1; window.clearTimeout(this.timer); this.timer = 0; this.player.stop(); }

  private render(host: HTMLElement, item: PlaylistItemSerialized | undefined, duration: number, advance: () => void): boolean {
    if (!item?.key || !item.value) return false;
    let parsed: unknown; try { parsed = JSON.parse(item.value); } catch { return false; }
    if (!parsed || typeof parsed !== 'object') return false;
    host.replaceChildren();
    if (item.key === 'AssetItemModel') return this.asset(host, parsed as MediaAsset, duration, advance);
    if (item.key === 'TextAssetItemModel') return this.text(host, parsed as TextPlaylistItem, duration, advance);
    return false;
  }

  private asset(host: HTMLElement, item: MediaAsset, duration: number, advance: () => void): boolean {
    if (!item.assetUrl) return false;
    if (item.type === 2) { this.player.play(item.assetUrl, host, false, advance); return true; }
    if (item.type !== 1) return false;
    const image = new Image(); image.className = 'media'; image.alt = item.description ?? ''; image.src = item.assetUrl;
    image.addEventListener('error', advance, { once: true }); host.append(image); this.schedule(duration, advance); return true;
  }

  private text(host: HTMLElement, item: TextPlaylistItem, duration: number, advance: () => void): boolean {
    if (typeof item.description !== 'string') return false;
    const section = document.createElement('section'); section.className = 'rich-text'; section.innerHTML = DOMPurify.sanitize(item.description, { USE_PROFILES: { html: true } });
    if (/^#[\da-f]{6}$/i.test(item.backgroundColor ?? '')) section.style.backgroundColor = item.backgroundColor ?? '';
    if (/^#[\da-f]{6}$/i.test(item.textColor ?? '')) section.style.color = item.textColor ?? '';
    host.append(section); this.schedule(duration, advance); return true;
  }

  private schedule(milliseconds: number, advance: () => void): void { this.timer = window.setTimeout(advance, milliseconds); }
  private parseDuration(value: string | null | undefined): number {
    const match = /^(\d{2}):(\d{2}):(\d{2})$/.exec(value ?? ''); if (!match) return 10_000;
    const hours = Number(match[1]); const minutes = Number(match[2]); const seconds = Number(match[3]); const total = hours * 3600 + minutes * 60 + seconds;
    return total > 0 && minutes < 60 && seconds < 60 ? total * 1000 : 10_000;
  }
  private status(host: HTMLElement, message: string): void { host.replaceChildren(); const value = document.createElement('p'); value.className = 'empty'; value.textContent = message; host.append(value); }
}
