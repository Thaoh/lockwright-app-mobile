import { ConfigPlugin, withPlugins } from '@expo/config-plugins';
import { withAppGroupHelper } from './ios/withAppGroupHelper';
import { withAppBuildGradle } from './android/withAppBuildGradle';
import { withCompileSdk36 } from './android/withCompileSdk36';
import { withMainActivity } from './android/withMainActivity';
import { withMainApplication } from './android/withMainApplication';

const withAppConfig: ConfigPlugin = (config) => {
  return withPlugins(config, [
    // iOS
    withAppGroupHelper,
    // Android
    withAppBuildGradle,
    withCompileSdk36,
    withMainActivity,
    withMainApplication,
  ]);
};

export default withAppConfig;
