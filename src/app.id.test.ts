import { readdirSync, readFileSync } from "fs";
import path from "path";

const APP_ID = "works.dexterity.lockwright";

describe("Lockwright app id", () => {
  it("is works.dexterity.lockwright in app.json", () => {
    const app = JSON.parse(
      readFileSync(path.resolve(__dirname, "../app.json"), "utf8"),
    );
    expect(app.expo.android.package).toBe(APP_ID);
    expect(app.expo.ios.bundleIdentifier).toBe(APP_ID);
    expect(
      app.expo.ios.entitlements["com.apple.security.application-groups"],
    ).toEqual([`group.${APP_ID}`]);
  });

  it("pins @tetherto/pearpass-lib-constants to Thaoh git, not Tether or file:", () => {
    const pkg = JSON.parse(
      readFileSync(path.resolve(__dirname, "../package.json"), "utf8"),
    );
    expect(pkg.dependencies["@tetherto/pearpass-lib-constants"]).toBe(
      "git+https://github.com/Thaoh/lockwright-lib-constants.git",
    );
  });

  it("ships expo display name Lockwright, not PearPass", () => {
    const app = JSON.parse(
      readFileSync(path.resolve(__dirname, "../app.json"), "utf8"),
    );
    expect(app.expo.name).toBe("Lockwright");
    expect(app.expo.name).not.toMatch(/PearPass/);
  });

  it("autofill chips and system pickers say Lockwright, not PearPass", () => {
    const roots = [
      path.resolve(
        __dirname,
        "../plugins/expo-autofill-plugin/android-template",
      ),
      path.resolve(__dirname, "../plugins/expo-autofill-plugin/ios-template"),
    ];
    const leftover = [
      '"PearPass"',
      '"Unlock PearPass"',
      '"Save passkey in PearPass"',
      '"Authenticate to PearPass"',
      '"Authenticate to access PearPass"',
      '"PearPass is already running"',
      "the PearPass app",
      '"Finish setting up PearPass"',
      '"Open the PearPass app"',
      '"PearPass is not configured"',
      '"PearPass User"',
      "other PearPass instances",
    ];
    const hits: string[] = [];
    const walk = (dir: string) => {
      for (const name of readdirSync(dir, { withFileTypes: true })) {
        const full = path.join(dir, name.name);
        if (name.isDirectory()) {
          walk(full);
          continue;
        }
        if (!/\.(java|swift|xml|strings)$/.test(name.name)) continue;
        const text = readFileSync(full, "utf8");
        for (const phrase of leftover) {
          if (text.includes(phrase)) {
            hits.push(`${path.relative(roots[0], full)}: ${phrase}`);
          }
        }
      }
    };
    for (const root of roots) walk(root);
    expect(hits).toEqual([]);
  });
});
