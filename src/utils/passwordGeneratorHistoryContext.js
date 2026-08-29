import { addHttps } from './addHttps'

/**
 * Derive generator-history context for a Use/insert action.
 * Prefer a site hostname when a website URL is present; otherwise the entry title.
 */
export const resolveHistoryContext = ({ title, websiteUrl } = {}) => {
  const hostname = hostnameFromUrl(websiteUrl)
  if (hostname) {
    return { contextLabel: hostname, contextKind: 'site' }
  }

  const label = typeof title === 'string' ? title.trim() : ''
  if (label) {
    return { contextLabel: label, contextKind: 'entry' }
  }

  return null
}

export const hostnameFromUrl = (url) => {
  if (typeof url !== 'string' || !url.trim()) return null
  try {
    const hostname = new URL(addHttps(url.trim())).hostname
    return hostname || null
  } catch {
    return null
  }
}
