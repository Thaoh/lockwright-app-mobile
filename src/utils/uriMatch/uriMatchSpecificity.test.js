import { URI_MATCH_TYPES } from './constants'
import {
  getRecordSiteMatchRank,
  getUriMatchSpecificityRank
} from './uriMatchSpecificity'

describe('uriMatchSpecificity', () => {
  it('ranks exact > startsWith > host > domain', () => {
    expect(getUriMatchSpecificityRank(URI_MATCH_TYPES.EXACT)).toBe(4)
    expect(getUriMatchSpecificityRank(URI_MATCH_TYPES.STARTS_WITH)).toBe(3)
    expect(getUriMatchSpecificityRank(URI_MATCH_TYPES.HOST)).toBe(2)
    expect(getUriMatchSpecificityRank(URI_MATCH_TYPES.DOMAIN)).toBe(1)
    expect(getUriMatchSpecificityRank('unknown')).toBe(0)
  })

  it('returns 0 when the record does not match the page', () => {
    expect(
      getRecordSiteMatchRank(
        { data: { websites: ['https://other.com'] } },
        'https://example.com'
      )
    ).toBe(0)
  })

  it('uses the best matching uri specificity from vault uris', () => {
    expect(
      getRecordSiteMatchRank(
        {
          id: 'r1',
          data: {
            websites: ['https://example.com'],
            uris: [{ uri: 'https://example.com', match: 'exact' }]
          }
        },
        'https://example.com'
      )
    ).toBe(4)
  })
})
