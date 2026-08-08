declare const webapis: { avplay: { open(url: string): void; close(): void; prepareAsync(success: () => void, failure: (error: unknown) => void): void; play(): void; pause(): void; setDisplayRect(x: number, y: number, width: number, height: number): void; setListener(listener: Record<string, (...args: unknown[]) => void>): void } } | undefined;

type PlaybackState = 'idle' | 'preparing' | 'ready' | 'playing' | 'paused';

/** Encapsulates Samsung AVPlay while retaining a browser video fallback for development. */
export class AvPlayAdapter {
  private video: HTMLVideoElement | null = null;
  private state: PlaybackState = 'idle';
  private pauseRequested = false;
  public play(url: string, host: HTMLElement, loop: boolean, ended?: () => void): void {
    this.stop();
    this.pauseRequested = document.hidden;
    if (typeof webapis !== 'undefined') {
      this.state = 'preparing';
      webapis.avplay.open(url); webapis.avplay.setDisplayRect(0, 0, 1920, 1080);
      webapis.avplay.setListener({ onstreamcompleted: () => loop ? this.play(url, host, loop, ended) : ended?.(), onerror: () => ended?.() });
      webapis.avplay.prepareAsync(() => { if (this.state !== 'preparing') return; this.state = 'ready'; if (!this.pauseRequested) this.startPreparedPlayback(); }, () => { if (this.state !== 'preparing') return; this.state = 'idle'; ended?.(); }); return;
    }
    this.video = document.createElement('video'); this.video.className = 'media'; this.video.autoplay = !this.pauseRequested; this.video.loop = loop; this.video.src = url; this.state = this.pauseRequested ? 'ready' : 'playing';
    if (ended) this.video.addEventListener('ended', ended, { once: true }); host.append(this.video);
  }
  public pause(): void { this.pauseRequested = true; if (this.state !== 'playing') return; if (typeof webapis !== 'undefined') webapis.avplay.pause(); else this.video?.pause(); this.state = 'paused'; }
  public resume(): void { this.pauseRequested = false; if (this.state !== 'ready' && this.state !== 'paused') return; if (typeof webapis !== 'undefined') this.startPreparedPlayback(); else { void this.video?.play(); this.state = 'playing'; } }
  public stop(): void { if (this.state !== 'idle' && typeof webapis !== 'undefined') { try { webapis.avplay.close(); } catch { /* AVPlay may not be open. */ } } this.video?.remove(); this.video = null; this.state = 'idle'; }
  private startPreparedPlayback(): void { webapis?.avplay.play(); this.state = 'playing'; }
}
