import { ContentRenderer } from '../src/renderers/ContentRenderer';
import type { HtmlVideoPlayer } from '../src/media/HtmlVideoPlayer';

describe('ContentRenderer', () => {
  it('renders the backend media, text, menu, and discount field names', () => {
    const player = { play: vi.fn(), stop: vi.fn() } as unknown as HtmlVideoPlayer;
    const renderer = new ContentRenderer(player); const host = document.createElement('main');
    renderer.render({ layout: { templateKey: 'MediaOnly' }, mediaAsset: { type: 2, assetUrl: 'https://media.test/video.mp4' } }, host);
    expect(player.play).toHaveBeenCalledWith('https://media.test/video.mp4', host, true);
    renderer.render({ layout: { templateKey: 'Text' }, textEditorData: '<b>Notice</b>' }, host);
    expect(host.textContent).toBe('Notice');
    renderer.render({ layout: { templateKey: 'MenuOnly' }, menu: { currency: '$', menuItems: [{ name: 'Tea', price: '4', discountPrice: '3' }] } }, host);
    expect(host.textContent).toContain('Tea'); expect(host.querySelector('s')?.textContent).toBe('$4'); expect(host.textContent).toContain('$3');
  });

  it('clears the clock timer when rendering is stopped', () => {
    const clearIntervalSpy = vi.spyOn(window, 'clearInterval');
    const renderer = new ContentRenderer({ stop: vi.fn() } as unknown as HtmlVideoPlayer); const host = document.createElement('main');
    renderer.render({ layout: { templateKey: 'CurrentDateTime' } }, host); renderer.stop();
    expect(clearIntervalSpy).toHaveBeenCalled();
  });
});
