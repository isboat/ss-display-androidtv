import { PlaylistController } from '../src/content/PlaylistController';
import type { HtmlVideoPlayer } from '../src/media/HtmlVideoPlayer';

describe('PlaylistController', () => {
  it('advances static items using the configured duration and loops', () => {
    vi.useFakeTimers();
    const player = { stop: vi.fn(), play: vi.fn() } as unknown as HtmlVideoPlayer;
    const host = document.createElement('main'); const playlist = new PlaylistController(player);
    playlist.start(host, [
      { key: 'TextAssetItemModel', value: JSON.stringify({ description: '<b>Welcome</b>' }) },
      { key: 'AssetItemModel', value: JSON.stringify({ type: 1, assetUrl: 'https://media.test/image.jpg' }) },
    ], '00:00:02');
    expect(host.textContent).toBe('Welcome');
    vi.advanceTimersByTime(2_000); expect(host.querySelector('img')?.src).toBe('https://media.test/image.jpg');
    vi.advanceTimersByTime(2_000); expect(host.textContent).toBe('Welcome');
    playlist.stop(); vi.useRealTimers();
  });

  it('plays video once and advances after completion', () => {
    let ended: (() => void) | undefined;
    const player = { stop: vi.fn(), play: vi.fn((_url, _host, _loop, onEnded) => { ended = onEnded; }) } as unknown as HtmlVideoPlayer;
    const host = document.createElement('main'); const playlist = new PlaylistController(player);
    playlist.start(host, [
      { key: 'AssetItemModel', value: JSON.stringify({ type: 2, assetUrl: 'https://media.test/video.mp4' }) },
      { key: 'TextAssetItemModel', value: JSON.stringify({ description: 'Next' }) },
    ], '00:00:01');
    expect(player.play).toHaveBeenCalledWith('https://media.test/video.mp4', host, false, expect.any(Function));
    ended?.(); expect(host.textContent).toBe('Next');
  });

  it('diagnoses a full cycle of malformed entries', () => {
    const player = { stop: vi.fn(), play: vi.fn() } as unknown as HtmlVideoPlayer; const host = document.createElement('main');
    new PlaylistController(player).start(host, [{ key: 'Unknown', value: '{}' }, { key: 'AssetItemModel', value: 'bad json' }], null);
    expect(host.textContent).toContain('No valid item');
  });
});
