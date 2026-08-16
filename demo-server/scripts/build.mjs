import { execFileSync } from "node:child_process";
import { readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { fileURLToPath } from "node:url";

const root = fileURLToPath(new URL("..", import.meta.url));

console.log("[build] Compiling server (tsc) ...");
execFileSync("tsc", ["-p", join(root, "tsconfig.json")], { stdio: "inherit" });

console.log("[build] Bundling console (esbuild) ...");
execFileSync(
  "esbuild",
  [
    join(root, "src/console.ts"),
    "--bundle",
    "--format=iife",
    "--minify",
    "--outfile=dist/console.bundle.js",
  ],
  { cwd: root, stdio: "inherit" },
);

console.log("[build] Assembling demo-server.html ...");
const template = readFileSync(join(root, "demo-server.template.html"), "utf8");
const bundle = readFileSync(join(root, "dist/console.bundle.js"), "utf8");
const tsSource = ["types.ts", "generator.ts", "console.ts"]
  .map((file) => `// ===== src/${file} =====\n${readFileSync(join(root, "src", file), "utf8")}`)
  .join("\n\n");

const output = template
  .replace("/*__CONSOLE_BUNDLE__*/", bundle)
  .replace("/*__TS_SOURCE__*/", tsSource);

writeFileSync(join(root, "demo-server.html"), output, "utf8");
console.log("[build] demo-server.html written.");
