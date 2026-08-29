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

  it("pins @tetherto/pearpass-lib-ui-kit to Thaoh git, not Tether", () => {
    const pkg = JSON.parse(
      readFileSync(path.resolve(__dirname, "../package.json"), "utf8"),
    );
    expect(pkg.dependencies["@tetherto/pearpass-lib-ui-kit"]).toBe(
      "git+https://github.com/Thaoh/lockwright-lib-ui-react-native-components.git#design-system-v2",
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

  it("points Lockwright import help at lockwright.dexterity.works, not PearPass docs", () => {
    const src = readFileSync(
      path.resolve(__dirname, "./screens/ImportItems/index.tsx"),
      "utf8",
    );
    expect(src).not.toMatch(/docs\.pass\.pears\.com/);
    expect(src).toMatch(/PEARPASS_WEBSITE/);
  });

  it("report-a-problem opens the Lockwright contact form, not Tether Slack or Google Form", () => {
    const src = readFileSync(
      path.resolve(__dirname, "./screens/Settings/Feedback/index.jsx"),
      "utf8",
    );
    expect(src).not.toMatch(/sendSlackFeedback|sendGoogleFormFeedback/);
    expect(src).toMatch(/PEARPASS_WEBSITE/);
    expect(src).toMatch(/\/contact\//);
    expect(src).toMatch(/Linking\.openURL/);
  });

  it("in-app logos use the hatch plate, not PearPass lime", () => {
    const files = [
      path.resolve(__dirname, "./svgs/LogoLock/index.jsx"),
      path.resolve(__dirname, "./svgs/LogoTextWithLock/index.jsx"),
    ];
    for (const file of files) {
      const src = readFileSync(file, "utf8");
      expect(src).toContain("#b08d57");
      expect(src).not.toMatch(/#B0D944|#BADE5B/i);
    }
  });

  it("Play listing is Lockwright with a 512 icon", () => {
    const title = readFileSync(
      path.resolve(__dirname, "../metadata/en-US/title.txt"),
      "utf8",
    ).trim();
    expect(title).toBe("Lockwright");
    const icon = readFileSync(
      path.resolve(__dirname, "../metadata/en-US/images/icon.png"),
    );
    expect(icon.readUInt32BE(16)).toBe(512);
    expect(icon.readUInt32BE(20)).toBe(512);
    const listing = readFileSync(
      path.resolve(__dirname, "../metadata/en-US/full_description.txt"),
      "utf8",
    );
    expect(listing).not.toMatch(/PearPass is a fully local/);
  });

  it("app.config.ts has one default export and no leftover tail", () => {
    const src = readFileSync(
      path.resolve(__dirname, "../app.config.ts"),
      "utf8",
    );
    expect([...src.matchAll(/^export default /gm)]).toHaveLength(1);
    expect(src).not.toMatch(/}\s+plugins\.push/);
  });
});
