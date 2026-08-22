import { normalizeUrl } from './normalizeUrl'

describe('normalizeUrl', () => {
  test('adds https and lowercases host when protocol is missing', () => {
    expect(normalizeUrl('Example.COM')).toBe('https://example.com')
  })

  test('defaults to http when defaultToSecureProtocol is false', () => {
    expect(normalizeUrl('Example.COM', false)).toBe('http://example.com')
  })

  test('keeps existing protocol and lowercases host', () => {
    expect(normalizeUrl('HTTP://ExAmPlE.com')).toBe('http://example.com')
    expect(normalizeUrl('https://Example.com')).toBe('https://example.com')
  })

  test('strips default ports and keeps non-standard ports', () => {
    expect(normalizeUrl('http://example.com:80')).toBe('http://example.com')
    expect(normalizeUrl('https://example.com:443')).toBe('https://example.com')
    expect(normalizeUrl('example.com:8080')).toBe('https://example.com:8080')
  })

  test('strips trailing slash on path', () => {
    expect(normalizeUrl('https://example.com/path/')).toBe(
      'https://example.com/path'
    )
  })

  test('returns null for invalid URLs', () => {
    expect(normalizeUrl('not a valid url')).toBeNull()
    expect(normalizeUrl('')).toBeNull()
  })
})
