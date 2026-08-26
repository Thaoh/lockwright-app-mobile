import Constants from 'expo-constants'

import { formatDisplayVersion } from './formatDisplayVersion'
import { version } from '../../package.json'

export function getDisplayVersion() {
  const extra = Constants.expoConfig?.extra || {}
  if (
    typeof extra.displayVersion === 'string' &&
    extra.displayVersion.length > 0
  ) {
    return extra.displayVersion
  }

  return formatDisplayVersion(
    Constants.expoConfig?.version || extra.appVersion || version,
    extra.gitSha
  )
}
