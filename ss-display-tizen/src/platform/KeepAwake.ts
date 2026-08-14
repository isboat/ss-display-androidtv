/** Prevents Tizen's display power management and Samsung's screen saver while the app is active. */
export class KeepAwake {
  private active = false;

  enable(): void {
    if (this.active) return;
    this.active = true;

    try {
      window.tizen?.power?.request('SCREEN', 'SCREEN_NORMAL');
    } catch (error) {
      console.warn('Unable to request a Tizen screen power lock', error);
    }

    const appCommon = window.webapis?.appcommon;
    if (appCommon) {
      try {
        appCommon.setScreenSaver(appCommon.AppCommonScreenSaverState.SCREEN_SAVER_OFF);
      } catch (error) {
        console.warn('Unable to disable the Samsung screen saver', error);
      }
    }
  }

  disable(): void {
    if (!this.active) return;
    this.active = false;

    try {
      window.tizen?.power?.release('SCREEN');
    } catch (error) {
      console.warn('Unable to release the Tizen screen power lock', error);
    }

    const appCommon = window.webapis?.appcommon;
    if (appCommon) {
      try {
        appCommon.setScreenSaver(appCommon.AppCommonScreenSaverState.SCREEN_SAVER_ON);
      } catch (error) {
        console.warn('Unable to restore the Samsung screen saver', error);
      }
    }
  }
}
