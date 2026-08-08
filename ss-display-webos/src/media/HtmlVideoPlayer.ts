/** Owns the single HTML5 decoder and releases it deterministically between layouts. */
export class HtmlVideoPlayer {
  private video: HTMLVideoElement | null = null;
  public play(url: string, host: HTMLElement, loop: boolean, onEnded?: () => void): void {
    this.stop();
    const video = document.createElement('video');
    video.className = 'media'; video.autoplay = true; video.loop = loop; video.playsInline = true;
    video.preload = 'auto'; video.src = url; video.setAttribute('aria-label', 'Display video');
    if (onEnded) video.addEventListener('ended', onEnded, { once: true });
    video.addEventListener('error', () => onEnded?.(), { once: true });
    host.append(video); this.video = video; void video.play().catch(() => { /* Playback resumes on foreground or remote interaction. */ });
  }
  /** Pauses playback while webOS hides the application. */
  public pause(): void { this.video?.pause(); }
  /** Resumes playback after webOS restores the application. */
  public resume(): void { void this.video?.play().catch(() => undefined); }
  /** Stops playback and releases the decoder and URL references. */
  public stop(): void { if (!this.video) return; this.video.pause(); this.video.removeAttribute('src'); this.video.load(); this.video.remove(); this.video = null; }
}
