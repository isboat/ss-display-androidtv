import { StorageService } from '../src/platform/Storage';

describe('StorageService', () => {
  it('keeps access and refresh tokens together', () => { const storage = new StorageService(); storage.tokens = { accessToken: 'a', refreshToken: 'r' }; expect(storage.tokens).toEqual({ accessToken: 'a', refreshToken: 'r' }); });
});
