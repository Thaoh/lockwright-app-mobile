export { URI_MATCH_TYPES } from './constants'
export { normalizeUrl } from './normalizeUrl'
export {
  buildLoginUris,
  fromVaultUriMatch,
  getRecordWebsiteValues,
  resolveUriMatchType,
  toVaultUriMatch,
  websiteRowsFromRecord
} from './uriMatchSetting'
export {
  doesWebsiteMatchPage,
  recordMatchesCurrentSite
} from './doesWebsiteMatchPage'
export {
  getRecordSiteMatchRank,
  getUriMatchSpecificityRank
} from './uriMatchSpecificity'
