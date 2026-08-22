import { addHttps } from '../addHttps'
import { URI_MATCH_TYPES } from './constants'
import { normalizeUrl } from './normalizeUrl'

/** @typedef {import('./constants').UriMatchType} UriMatchType */

const VALID_MATCH_TYPES = new Set(Object.values(URI_MATCH_TYPES))

/** Vault schema uses Bitwarden-style `baseDomain`; UI uses `domain`. */
const VAULT_MATCH_BASE_DOMAIN = 'baseDomain'

/**
 * @param {unknown} value
 * @returns {value is UriMatchType}
 */
const isValidMatchType = (value) => VALID_MATCH_TYPES.has(value)

/**
 * @param {unknown} vaultMatch
 * @returns {UriMatchType|null}
 */
export const fromVaultUriMatch = (vaultMatch) => {
  if (vaultMatch === VAULT_MATCH_BASE_DOMAIN) return URI_MATCH_TYPES.DOMAIN
  if (isValidMatchType(vaultMatch)) return vaultMatch
  return null
}

/**
 * @param {string} matchType
 * @returns {string}
 */
export const toVaultUriMatch = (matchType) => {
  if (matchType === URI_MATCH_TYPES.DOMAIN) return VAULT_MATCH_BASE_DOMAIN
  if (isValidMatchType(matchType)) return matchType
  return VAULT_MATCH_BASE_DOMAIN
}

/**
 * @param {string|null|undefined} website
 * @returns {string|null}
 */
const normalizeWebsiteKey = (website) => {
  if (!website || typeof website !== 'string') return null
  const trimmed = website.trim()
  if (!trimmed) return null
  return addHttps(trimmed) || null
}

/**
 * @param {{ data?: { uris?: Array<{ uri?: string, match?: string }> } }} record
 * @param {string|null} websiteKey
 * @returns {string|null}
 */
const matchFromRecordUris = (record, websiteKey) => {
  if (!websiteKey) return null
  const uris = record?.data?.uris
  if (!Array.isArray(uris) || uris.length === 0) return null
  for (const entry of uris) {
    if (!entry || typeof entry.uri !== 'string') continue
    const entryKey = normalizeWebsiteKey(entry.uri)
    if (entryKey && entryKey === websiteKey) {
      return fromVaultUriMatch(entry.match)
    }
  }
  return null
}

/**
 * @param {{ id?: string, data?: { uris?: Array<{ uri?: string, match?: string }> } }|string|null|undefined} recordOrId
 * @param {string} website
 * @returns {UriMatchType}
 */
export const resolveUriMatchType = (recordOrId, website) => {
  const key = normalizeWebsiteKey(website)

  if (recordOrId && typeof recordOrId === 'object') {
    const fromVault = matchFromRecordUris(recordOrId, key)
    if (fromVault) return fromVault
  }

  return URI_MATCH_TYPES.DOMAIN
}

/**
 * @param {Array<{ website?: string, matchType?: string }>} websiteRows
 * @returns {Array<{ uri: string, match: string }>}
 */
export const buildLoginUris = (websiteRows) => {
  /** @type {Array<{ uri: string, match: string }>} */
  const uris = []
  if (!Array.isArray(websiteRows)) return uris
  for (const row of websiteRows) {
    const trimmed = typeof row?.website === 'string' ? row.website.trim() : ''
    if (!trimmed) continue
    const uri = addHttps(trimmed)
    if (!uri) continue
    const matchType =
      row.matchType && isValidMatchType(row.matchType)
        ? row.matchType
        : URI_MATCH_TYPES.DOMAIN
    uris.push({ uri, match: toVaultUriMatch(matchType) })
  }
  return uris
}

/**
 * Website strings used for page matching. Prefer `data.websites`, and
 * always include `data.uris[].uri` so a host stored only on the v2 URI
 * list is still detected.
 *
 * @param {{ data?: { websites?: string[]|null, uris?: Array<{ uri?: string }>|null } }|null|undefined} record
 * @returns {string[]}
 */
export const getRecordWebsiteValues = (record) => {
  const websites = Array.isArray(record?.data?.websites)
    ? record.data.websites.filter(
        (website) => typeof website === 'string' && website.trim() !== ''
      )
    : []
  const fromUris = Array.isArray(record?.data?.uris)
    ? record.data.uris
        .map((entry) =>
          entry && typeof entry.uri === 'string' && entry.uri.trim() !== ''
            ? entry.uri
            : null
        )
        .filter((uri) => uri !== null)
    : []

  if (fromUris.length === 0) return websites
  if (websites.length === 0) return fromUris

  const seen = new Set()
  const merged = []
  for (const website of [...websites, ...fromUris]) {
    const key = normalizeUrl(website, true) || website.trim().toLowerCase()
    if (seen.has(key)) continue
    seen.add(key)
    merged.push(website)
  }
  return merged
}

/**
 * Form rows for create/edit login: website + UI match type.
 *
 * @param {{ data?: { websites?: string[], uris?: Array<{ uri?: string, match?: string }> } }|null|undefined} record
 * @returns {Array<{ website: string, matchType: UriMatchType }>}
 */
export const websiteRowsFromRecord = (record) => {
  const websites = getRecordWebsiteValues(record)
  if (websites.length === 0) {
    return [{ website: '', matchType: URI_MATCH_TYPES.DOMAIN }]
  }
  return websites.map((website) => ({
    website,
    matchType: resolveUriMatchType(record, website)
  }))
}
