import {
  ConfigPlugin,
  withGradleProperties
} from '@expo/config-plugins'

// Expo 53 / RN 0.79 pin AGP 8.8.2. That plugin's recommended
// compileSdk is 35. Play still requires target 36. This flag
// lets the build use platform 36 without jumping to AGP 9.
export const withCompileSdk36: ConfigPlugin = (config) => {
  return withGradleProperties(config, (cfg) => {
    const key = 'android.suppressUnsupportedCompileSdk'
    cfg.modResults = cfg.modResults.filter((item) => {
      return !(item.type === 'property' && item.key === key)
    })
    cfg.modResults.push({
      type: 'property',
      key,
      value: '36'
    })
    return cfg
  })
}
