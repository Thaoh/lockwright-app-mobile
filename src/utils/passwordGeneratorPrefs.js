import AsyncStorage from '@react-native-async-storage/async-storage'

export const PASSWORD_GENERATOR_CHARACTERS_KEY = 'password-generator-characters'
export const DEFAULT_CHARACTER_COUNT = 20
export const MIN_CHARACTER_COUNT = 4
export const MAX_CHARACTER_COUNT = 4096

const clampCharacterCount = (value) => {
  const parsed = Number.parseInt(String(value), 10)
  if (!Number.isFinite(parsed)) return null
  return Math.min(MAX_CHARACTER_COUNT, Math.max(MIN_CHARACTER_COUNT, parsed))
}

export const loadLastCharacterCount = async () => {
  try {
    const raw = await AsyncStorage.getItem(PASSWORD_GENERATOR_CHARACTERS_KEY)
    return clampCharacterCount(raw) ?? DEFAULT_CHARACTER_COUNT
  } catch {
    return DEFAULT_CHARACTER_COUNT
  }
}

export const saveLastCharacterCount = async (count) => {
  const next = clampCharacterCount(count)
  if (next === null) return
  try {
    await AsyncStorage.setItem(PASSWORD_GENERATOR_CHARACTERS_KEY, String(next))
  } catch {
    // Device-local only. Ignore storage failures.
  }
}
