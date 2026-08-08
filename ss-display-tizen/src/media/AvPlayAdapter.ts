declare const webapis: { avplay: { open(url: string): void; close(): void; prepareAsync(success: () => void, failure: (error: unknown) => void): void; play(): void; pause(): void; setDisplayRect(x: number, y: number, width: number, height: number): void; setListener(listener: Record<string, (...args: unknown[]) => void>): void } } | undefined;

/** Encapsulates Samsung AVPlay while retaining a browser video fallback for development. */
export class AvPlayAdapter {
  private video: HTMLVideoElement | null = null;
  public play(url: string, host: HTMLElement, loop: boolean, ended?: () => void): void {
    this.stop();
    if (typeof webapis !== 'undefined') {
      webapis.avplay.open(url); webapis.avplay.setDisplayRect(0, 0, 1920, 1080);
      webapis.avplay.setListener({ onstreamcompleted: () => loop ? this.play(url, host, loop, ended) : ended?.(), onerror: () => ended?.() });
      webapis.avplay.prepareAsync(() => webapis.avplay.play(), () => ended?.()); return;
    }
    this.video = document.createElement('video'); this.video.className = 'media'; this.video.autoplay = true; this.video.loop = loop; this.video.src = url;
    if (ended) this.video.addEventListener('ended', ended, { once: true }); host.append(this.video);
  }
  public pause(): void { if (typeof webapis !== 'undefined') webapis.avplay.pause(); else this.video?.pause(); }
  public stop(): void { if (typeof webapis !== 'undefined') { try { webapis.avplay.close(); } catch { /* AVPlay may not be open. */ } } this.video?.remove(); this.video = null; }
}
