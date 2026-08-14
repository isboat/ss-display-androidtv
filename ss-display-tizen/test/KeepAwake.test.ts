import { KeepAwake } from '../src/platform/KeepAwake';

describe('KeepAwake', () => {
  afterEach(() => {
    delete window.tizen;
    delete window.webapis;
  });

  it('locks the screen and disables the screen saver until released', () => {
    const request = vi.fn();
    const release = vi.fn();
    const setScreenSaver = vi.fn();
    window.tizen = { application: { getCurrentApplication: () => ({ exit: vi.fn() }) }, power: { request, release } };
    window.webapis = { appcommon: { AppCommonScreenSaverState: { SCREEN_SAVER_OFF: 0, SCREEN_SAVER_ON: 1 }, setScreenSaver } };
    const keepAwake = new KeepAwake();

    keepAwake.enable();
    keepAwake.enable();
    expect(request).toHaveBeenCalledOnce();
    expect(request).toHaveBeenCalledWith('SCREEN', 'SCREEN_NORMAL');
    expect(setScreenSaver).toHaveBeenCalledWith(0);

    keepAwake.disable();
    keepAwake.disable();
    expect(release).toHaveBeenCalledOnce();
    expect(release).toHaveBeenCalledWith('SCREEN');
    expect(setScreenSaver).toHaveBeenLastCalledWith(1);

    keepAwake.enable();
    expect(request).toHaveBeenCalledTimes(2);
    expect(setScreenSaver).toHaveBeenLastCalledWith(0);
  });

  it('does not fail when platform APIs are unavailable', () => {
    const keepAwake = new KeepAwake();
    expect(() => { keepAwake.enable(); keepAwake.disable(); }).not.toThrow();
  });
});
