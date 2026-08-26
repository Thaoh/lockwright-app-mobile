import { getDisplayVersion } from './appDisplayVersion'

jest.mock('expo-constants', () => ({
  expoConfig: {
    version: '0.0.1',
    extra: {
      appVersion: '0.0.1',
      gitSha: 'abcdef1234',
      displayVersion: '0.0.1-abcdef'
    }
  }
}))

describe('getDisplayVersion', () => {
  it('returns extra.displayVersion when Expo baked it', () => {
    expect(getDisplayVersion()).toBe('0.0.1-abcdef')
  })
})
