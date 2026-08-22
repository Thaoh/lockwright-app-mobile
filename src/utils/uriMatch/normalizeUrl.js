/**
 * @param {string} urlString
 * @param {boolean} [defaultToSecureProtocol=true]
 * @returns {string | null}
 */
export const normalizeUrl = (urlString, defaultToSecureProtocol = true) => {
  try {
    const defaultProtocolPrefix = defaultToSecureProtocol
      ? 'https://'
      : 'http://'
    const withProtocol = /^https?:\/\//i.test(urlString)
      ? urlString
      : `${defaultProtocolPrefix}${urlString}`
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
