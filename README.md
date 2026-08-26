<p align="center">
  <img src="docs/logo.svg" alt="Lockwright" width="128"/>
</p>

# Lockwright Mobile

Mobile app for Lockwright. Peer-to-peer password manager. Vaults stay on the device. Sync is device to device.

Community fork of PearPass (Apache 2.0). Not affiliated with or endorsed by Tether Data or the Pears project.

npm names, store listings, and shipped binaries still say PearPass until identity `works.dexterity.lockwright` lands. Do not take over Play listing `com.pears.pass`.

---

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Architecture](#architecture)
- [Starting the Application](#starting-the-application)
- [Prebuild](#prebuild)
- [Testing](#testing)
- [Dependencies](#dependencies)
- [Related Projects](#related-projects)
- [Contributing](#contributing)
- [Logging](#logging)
- [Error reporting](#error-reporting)
- [License](#license)

---

## Introduction

Lockwright encrypts and stores credentials on the device. Sync is peer to peer. No cloud account.

The on-disk vault at the fork point is PearPass's. Vault work in this tree aims to open those vaults in place. Test that on a copy.

---

## Features

- Encrypted-at-rest storage for passwords, cards, notes, and custom fields
- Biometric unlock
- Direct device-to-device sync, no central server
- Offline access
- Password health and a random password generator

---

## Installation

### Prerequisites

- **Node.js** — version in `.nvmrc`
- **pnpm** `11.10.0` (`packageManager` in `package.json`)

```bash
git clone git@github.com:Thaoh/lockwright-app-mobile.git
cd lockwright-app-mobile
pnpm install
pnpm run lingui:extract
pnpm run lingui:compile
pnpm run bundle-bare
npx expo prebuild --clean
```

In the Lockwright superproject, run `./scripts/fetch-packages.sh --layout` so `file:../pearpass-lib-vault` resolves.

---

## Usage

PearPass docs at [docs.pass.pears.com](https://docs.pass.pears.com) describe the product at the fork point. They are not Lockwright docs.

---

## Architecture

Expo plugins manage native iOS and Android config. Git does not track `ios/` or `android/`. Prebuild generates them.

- Native directories are gitignored
- Plugins live under `plugins/`
- `npx expo prebuild --clean` generates native trees

---

## Starting the Application

Build first. That produces iOS, iOS extension, and Android bundles and runs custom prebuild.

```bash
pnpm run build
pnpm run ios
pnpm run android
```

---

## Prebuild

This repo is Expo-managed. Prebuild generates `android/` and `ios/`. They are not committed.

### Play / normal Android

```bash
pnpm run bundle-bare
npx expo prebuild --platform android --clean
```

### F-Droid Android

```bash
pnpm run bundle-bare
PEARPASS_DISTRIBUTION=fdroid npx expo prebuild --platform android --clean
```

The env var is still `PEARPASS_DISTRIBUTION` until identity lands.

- [`docs/fdroid/build.md`](docs/fdroid/build.md)
- [`docs/fdroid/version-check.md`](docs/fdroid/version-check.md)

---

## Testing

```bash
pnpm test
```

End-to-end: WebdriverIO + Appium on Android and iOS, optional BrowserStack and Qase.

See [`e2e/SETUP_AND_RUN_GUIDE.md`](e2e/SETUP_AND_RUN_GUIDE.md) and [`e2e/AUTOMATED_TEST_CASES.md`](e2e/AUTOMATED_TEST_CASES.md).

---

## Dependencies

- [React Native](https://reactnative.dev/)
- [Expo](https://expo.dev/)
- [React](https://reactjs.org/)
- [React Navigation](https://reactnavigation.org/)
- [Lingui](https://lingui.dev/)
- [Redux](https://redux.js.org/)

---

## Related Projects

| Project | Description |
| --- | --- |
| [`lockwright-app-desktop`](https://github.com/Thaoh/lockwright-app-desktop) | Desktop |
| [`lockwright-app-browser-extension`](https://github.com/Thaoh/lockwright-app-browser-extension) | Browser extension |
| [`lockwright-lib-vault`](https://github.com/Thaoh/lockwright-lib-vault) | Vault |
| [`lockwright-lib-vault-core`](https://github.com/Thaoh/lockwright-lib-vault-core) | Vault core |
| [`lockwright-lib-constants`](https://github.com/Thaoh/lockwright-lib-constants) | Shared constants |

---

## Contributing

See [`CONTRIBUTING.md`](./CONTRIBUTING.md).

---

## Logging

Off by default. When enabled, logs go to the app cache directory: `main.log` from the JS host, `core-logs.txt` from the Bare vault worker. The worker sink redacts known sensitive fields. The host logger does not. Treat anything passed to `logger.*` on the JS side as visible on disk in `main.log`.

- In-app: Settings → Diagnostics → **Enable logs**. Persists. Off stops writes and keeps files. On again appends.
- Nightly (`PearPass-nightly`): logging defaults to `debug` on first launch. Channel name is still PearPass until identity lands.

Diagnostics **Share logs** zips both files plus metadata (app version, distribution channel).

---

## Error reporting

Public releases and self-built versions send nothing. Sentry is only on the nightly distribution channel.

- Gate: `isNightly()` in `src/constants/distribution.js`. False unless the channel is `nightly`.
- Expo Sentry plugin loads only when `PEARPASS_DISTRIBUTION=nightly` at build time. `app.json` has no Sentry plugin, so standard and F-Droid builds never include it.
- Bare Sentry (`sentry-bare`) is an optional peer of `pearpass-lib-vault-core`. Public builds do not install it.

---

## License

Apache License 2.0. See `LICENSE.md` and `NOTICE.md`.
