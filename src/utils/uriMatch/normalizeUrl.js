/**
 * @param {string} urlString
 * @param {boolean} [defaultToSecureProtocol=true]
 * @returns {string | null}
 */
export const normalizeUrl = (urlString, defaultToSecureProtocol = true) => {
  try {
    if (typeof urlString !== 'string') return null
    let trimmed = urlString.trim()
    if (!trimmed) return null
    trimmed = trimmed.replace(/^(https?:\/\/)((?:android|ios)app:\/\/)/i, '$2')

    const defaultProtocolPrefix = defaultToSecureProtocol
      ? 'https://'
      : 'http://'
    const withProtocol = /^[a-z][a-z0-9+.-]*:\/\//i.test(trimmed)
      ? trimmed
      : `${defaultProtocolPrefix}${trimmed}`
    const url = new URL(withProtocol)

    const protocol = url.protocol.toLowerCase()
    const hostname = url.hostname.toLowerCase()

    const port =
      url.port && url.port !== '80' && url.port !== '443' ? `:${url.port}` : ''

    const path = url.pathname.replace(/\/$/, '')

    return `${protocol}//${hostname}${port}${path}`
  } catch {
    return null
  }
}
