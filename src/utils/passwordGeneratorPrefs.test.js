import AsyncStorage from '@react-native-async-storage/async-storage'

import {
  DEFAULT_CHARACTER_COUNT,
  PASSWORD_GENERATOR_CHARACTERS_KEY,
  loadLastCharacterCount,
  saveLastCharacterCount
} from './passwordGeneratorPrefs'

jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn(),
  setItem: jest.fn()
}))

describe('passwordGeneratorPrefs', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('returns the default when nothing is stored', async () => {
    AsyncStorage.getItem.mockResolvedValueOnce(null)
    await expect(loadLastCharacterCount()).resolves.toBe(
      DEFAULT_CHARACTER_COUNT
    )
    expect(DEFAULT_CHARACTER_COUNT).toBe(20)
    expect(AsyncStorage.getItem).toHaveBeenCalledWith(
      PASSWORD_GENERATOR_CHARACTERS_KEY
    )
  })

  it('round-trips a saved character count on this device', async () => {
    AsyncStorage.setItem.mockResolvedValueOnce()
    await saveLastCharacterCount(36)
    expect(AsyncStorage.setItem).toHaveBeenCalledWith(
      PASSWORD_GENERATOR_CHARACTERS_KEY,
      '36'
    )

    AsyncStorage.getItem.mockResolvedValueOnce('36')
    await expect(loadLastCharacterCount()).resolves.toBe(36)
  })

  it('clamps stored junk, underflow, and overflow', async () => {
    AsyncStorage.getItem.mockResolvedValueOnce('nope')
    await expect(loadLastCharacterCount()).resolves.toBe(20)

    AsyncStorage.setItem.mockResolvedValue()
    await saveLastCharacterCount(2)
    expect(AsyncStorage.setItem).toHaveBeenCalledWith(
      PASSWORD_GENERATOR_CHARACTERS_KEY,
      '4'
    )

    await saveLastCharacterCount(99999)
    expect(AsyncStorage.setItem).toHaveBeenCalledWith(
      PASSWORD_GENERATOR_CHARACTERS_KEY,
      '4096'
    )
  })
})
