import { readFileSync } from "fs";
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
});
