import { AvPlayAdapter } from '../src/media/AvPlayAdapter';

describe('AvPlayAdapter', () => {
  it('defers pause and resume actions until AVPlay preparation finishes', () => {
    let prepared: (() => void) | undefined;
    const avplay = {
      open: vi.fn(), close: vi.fn(), setDisplayRect: vi.fn(), setListener: vi.fn(), play: vi.fn(), pause: vi.fn(),
      prepareAsync: vi.fn((success: () => void) => { prepared = success; }),
    };
    vi.stubGlobal('webapis', { avplay });
    const player = new AvPlayAdapter(); player.play('https://media.test/video.mp4', document.createElement('main'), true);

    player.pause();
    expect(avplay.pause).not.toHaveBeenCalled();
    prepared?.();
    expect(avplay.play).not.toHaveBeenCalled();

    player.resume();
    expect(avplay.play).toHaveBeenCalledTimes(1);
    player.resume();
    expect(avplay.play).toHaveBeenCalledTimes(1);
  });
});
