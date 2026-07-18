// One-off: render Data/DYJK_Certificate.html to a static PNG used on the
// landing page's "Get certified" section. Avoids shipping the 2.8MB HTML
// (with base64 fonts) to the browser. Run: node scripts/render-certificate-preview.mjs
import { chromium } from "playwright";
import { pathToFileURL } from "node:url";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "..");
const src = pathToFileURL(path.join(root, "Data", "DYJK_Certificate.html")).href;
const out = path.join(root, "public", "brand", "certificate-preview.png");

// A4 landscape at ~96dpi: 297mm -> 1122px, 210mm -> 794px. Use 2x for crispness.
const scale = 2;
const width = Math.round((297 / 25.4) * 96);
const height = Math.round((210 / 25.4) * 96);

const browser = await chromium.launch();
const page = await browser.newPage({
  viewport: { width, height },
  deviceScaleFactor: scale,
});
await page.goto(src, { waitUntil: "networkidle" });
await page.screenshot({ path: out, clip: { x: 0, y: 0, width, height } });
await browser.close();
console.log(`Wrote ${out} (${width}x${height} @${scale}x)`);
