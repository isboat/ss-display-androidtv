import { HtmlVideoPlayer } from '../src/media/HtmlVideoPlayer';

describe('HtmlVideoPlayer', () => {
  it('creates, pauses, resumes, and releases one HTML video element', () => {
    const play = vi.spyOn(HTMLMediaElement.prototype, 'play').mockResolvedValue();
    const pause = vi.spyOn(HTMLMediaElement.prototype, 'pause').mockImplementation(() => undefined);
    const load = vi.spyOn(HTMLMediaElement.prototype, 'load').mockImplementation(() => undefined);
    const host = document.createElement('main'); const player = new HtmlVideoPlayer();
    player.play('https://media.test/video.mp4', host, true);
    expect(host.querySelector('video')?.src).toBe('https://media.test/video.mp4');
    player.pause(); player.resume(); player.stop();
    expect(play).toHaveBeenCalledTimes(2); expect(pause).toHaveBeenCalledTimes(2);
    expect(load).toHaveBeenCalledOnce(); expect(host.querySelector('video')).toBeNull();
  });
});
