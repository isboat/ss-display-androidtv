interface Window {
  tizen?: {
    application: { getCurrentApplication(): { exit(): void } };
    power?: {
      request(resource: 'SCREEN', state: 'SCREEN_NORMAL'): void;
      release(resource: 'SCREEN'): void;
    };
  };
  webapis?: {
    appcommon?: {
      AppCommonScreenSaverState: {
        SCREEN_SAVER_OFF: number;
        SCREEN_SAVER_ON: number;
      };
      setScreenSaver(state: number, successCallback?: () => void, errorCallback?: (error: unknown) => void): void;
    };
  };
}
