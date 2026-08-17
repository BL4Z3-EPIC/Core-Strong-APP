import * as fs from "fs";
import * as http from "http";
import * as os from "os";
import * as path from "path";
import { generateMetricHistory, generateWorkouts } from "./generator";
import type { DemoData } from "./types";

const PORT = Number(process.env.PORT ?? 8765);
const ROOT_DIR = path.resolve(__dirname, "..");
const HTML_FILE = path.join(ROOT_DIR, "demo-server.html");
const DATA_FILE = path.join(ROOT_DIR, "demo-data.json");

function loadOrCreateData(): DemoData {
  if (fs.existsSync(DATA_FILE)) {
    try {
      const parsed = JSON.parse(fs.readFileSync(DATA_FILE, "utf8")) as DemoData;
      if (Array.isArray(parsed.metrics) && Array.isArray(parsed.workouts)) {
        return parsed;
      }
    } catch (_error) {
      // fall through to regeneration
    }
  }
  const data: DemoData = {
    metrics: generateMetricHistory(),
    workouts: generateWorkouts(),
  };
  fs.writeFileSync(DATA_FILE, JSON.stringify(data, null, 2), "utf8");
  return data;
}

const data: DemoData = loadOrCreateData();

function sendJson(response: http.ServerResponse, statusCode: number, body: unknown): void {
  const payload = JSON.stringify(body, null, 2);
  response.writeHead(statusCode, {
    "Content-Type": "application/json; charset=utf-8",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type",
  });
  response.end(payload);
}

function sendHtml(response: http.ServerResponse): void {
  response.writeHead(200, {
    "Content-Type": "text/html; charset=utf-8",
    "Access-Control-Allow-Origin": "*",
  });
  response.end(fs.readFileSync(HTML_FILE, "utf8"));
}

function readBody(request: http.IncomingMessage): Promise<string> {
  return new Promise((resolve, reject) => {
    let body = "";
    request.on("data", (chunk: Buffer) => {
      body += chunk.toString("utf8");
      if (body.length > 5 * 1024 * 1024) {
        reject(new Error("Payload too large"));
      }
    });
    request.on("end", () => resolve(body));
    request.on("error", reject);
  });
}

const server = http.createServer((request, response) => {
  const url = new URL(request.url ?? "/", `http://${request.headers.host ?? "localhost"}`);
  const pathname = url.pathname;

  if (request.method === "OPTIONS") {
    response.writeHead(204, {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type",
    });
    response.end();
    return;
  }

  if (request.method === "GET" && pathname === "/") {
    sendHtml(response);
    return;
  }

  if (request.method === "GET" && pathname === "/api/health") {
    sendJson(response, 200, {
      status: "ok",
      server: "PulsePoint Demo Server",
      dataPoints: data.metrics.length,
      workouts: data.workouts.length,
      firstDate: data.metrics[0]?.date ?? null,
      lastDate: data.metrics[data.metrics.length - 1]?.date ?? null,
    });
    return;
  }

  if (request.method === "GET" && pathname === "/api/metrics") {
    const sorted = [...data.metrics].sort((a, b) => a.date.localeCompare(b.date));
    sendJson(response, 200, sorted);
    return;
  }

  if (request.method === "GET" && pathname === "/api/workouts") {
    sendJson(response, 200, data.workouts);
    return;
  }

  if (request.method === "POST" && pathname === "/api/data") {
    void (async () => {
      try {
        const raw = await readBody(request);
        const payload = JSON.parse(raw) as Partial<DemoData>;
        if (Array.isArray(payload.metrics)) {
          data.metrics = payload.metrics;
        }
        if (Array.isArray(payload.workouts)) {
          data.workouts = payload.workouts;
        }
        fs.writeFileSync(DATA_FILE, JSON.stringify(data, null, 2), "utf8");
        sendJson(response, 200, { status: "updated", dataPoints: data.metrics.length });
      } catch (error) {
        sendJson(response, 400, { status: "error", message: (error as Error).message });
      }
    })();
    return;
  }

  sendJson(response, 404, { status: "error", message: `No endpoint at ${pathname}` });
});

function lanAddresses(): string[] {
  const interfaces = os.networkInterfaces();
  const addresses: string[] = [];
  for (const name of Object.keys(interfaces)) {
    for (const entry of interfaces[name] ?? []) {
      if (entry.family === "IPv4" && !entry.internal) {
        addresses.push(entry.address);
      }
    }
  }
  return addresses;
}

server.listen(PORT, "0.0.0.0", () => {
  console.log(`PulsePoint Demo Server listening on port ${PORT} (all interfaces)`);
  console.log(`  HTML console : http://localhost:${PORT}/`);
  console.log(`  Metrics API  : http://localhost:${PORT}/api/metrics`);
  console.log(`  Workouts API : http://localhost:${PORT}/api/workouts`);
  console.log(`  Health check : http://localhost:${PORT}/api/health`);
  for (const ip of lanAddresses()) {
    console.log(`  LAN access   : http://${ip}:${PORT}/ (use this in the app for a physical device)`);
  }
  console.log("  Android emulator uses: http://10.0.2.2:8765/");
  console.log("Data source: demo-data.json (regenerated automatically if missing).");
});
