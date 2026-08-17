import { generateMetricHistory, generateWorkouts } from "./generator";
import type { DemoData, HealthSnapshot, Workout } from "./types";

interface ConsoleState {
  data: DemoData;
  mode: "live" | "generated";
}

const state: ConsoleState = {
  data: { metrics: [], workouts: [] },
  mode: "generated",
};

function byId<T extends HTMLElement>(id: string): T {
  const element = document.getElementById(id);
  if (element === null) {
    throw new Error(`Missing element #${id}`);
  }
  return element as T;
}

function formatJson(value: unknown): string {
  return JSON.stringify(value, null, 2);
}

function download(filename: string, content: string): void {
  const blob = new Blob([content], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}

async function copyText(text: string, feedbackId: string): Promise<void> {
  try {
    await navigator.clipboard.writeText(text);
  } catch (_error) {
    const textarea = document.createElement("textarea");
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);
  }
  const feedback = byId<HTMLElement>(feedbackId);
  feedback.textContent = "Copied!";
  window.setTimeout(() => {
    feedback.textContent = "";
  }, 1500);
}

function renderStats(): void {
  const metrics = state.data.metrics;
  const workouts = state.data.workouts;
  const first = metrics[0];
  const last = metrics[metrics.length - 1];
  const exercises = workouts.reduce((total, workout) => total + workout.exercises.length, 0);

  byId<HTMLElement>("stat-data-points").textContent = `${metrics.length}`;
  byId<HTMLElement>("stat-range").textContent =
    first && last ? `${first.date} to ${last.date}` : "-";
  byId<HTMLElement>("stat-workouts").textContent = `${workouts.length}`;
  byId<HTMLElement>("stat-exercises").textContent = `${exercises}`;
  byId<HTMLElement>("stat-mode").textContent =
    state.mode === "live" ? "Live (from server)" : "Generated locally";

  const latest = last;
  if (latest) {
    byId<HTMLElement>("input-weight").value = String(latest.weightKg);
    byId<HTMLElement>("input-height").value = String(latest.heightCm);
    byId<HTMLElement>("input-fat").value = String(latest.fatPct);
    byId<HTMLElement>("input-muscle").value = String(latest.muscleMassPct);
    byId<HTMLElement>("input-age").value = String(latest.age);
    (byId<HTMLSelectElement>("input-gender")).value = latest.gender;
    byId<HTMLElement>("input-bmr").value = String(latest.bmr);
  }
}

function renderPreviews(): void {
  byId<HTMLElement>("preview-metrics").textContent = formatJson(state.data.metrics);
  byId<HTMLElement>("preview-workouts").textContent = formatJson(state.data.workouts);
  byId<HTMLElement>("preview-full").textContent = formatJson(state.data);
}

function render(): void {
  renderStats();
  renderPreviews();
}

function applyLatestEdits(): void {
  const metrics = state.data.metrics;
  if (metrics.length === 0) {
    return;
  }
  const latest = metrics[metrics.length - 1];
  const weightKg = Number(byId<HTMLInputElement>("input-weight").value);
  const heightCm = Number(byId<HTMLInputElement>("input-height").value);
  const fatPct = Number(byId<HTMLInputElement>("input-fat").value);
  const muscleMassPct = Number(byId<HTMLInputElement>("input-muscle").value);
  const age = Number(byId<HTMLInputElement>("input-age").value);
  const gender = byId<HTMLSelectElement>("input-gender").value;

  latest.weightKg = weightKg;
  latest.heightCm = heightCm;
  latest.fatPct = fatPct;
  latest.muscleMassPct = muscleMassPct;
  latest.age = age;
  latest.gender = gender;
  latest.bmi = Math.round((weightKg / Math.pow(heightCm / 100, 2)) * 10) / 10;
  latest.muscleWeightKg = Math.round((weightKg * muscleMassPct) / 100 * 10) / 10;
  latest.bmr =
    gender === "male"
      ? Math.round(10 * weightKg + 6.25 * heightCm - 5 * age + 5)
      : Math.round(10 * weightKg + 6.25 * heightCm - 5 * age - 161);

  render();
  void pushToServer("push-feedback-editor");
}

function regenerate(): void {
  const days = Number(byId<HTMLInputElement>("input-days").value);
  state.data = {
    metrics: generateMetricHistory({ days }),
    workouts: state.data.workouts,
  };
  state.mode = "generated";
  render();
  void pushToServer("push-feedback-generator");
}

async function pushToServer(feedbackId: string): Promise<void> {
  const feedback = byId<HTMLElement>(feedbackId);
  try {
    const response = await fetch("/api/data", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: formatJson(state.data),
    });
    feedback.textContent = response.ok ? "Pushed to server!" : "Push failed";
  } catch (_error) {
    feedback.textContent = "Push failed (server offline)";
  }
  window.setTimeout(() => {
    feedback.textContent = "";
  }, 2000);
}

function exportFull(): void {
  download("demo-data.json", formatJson(state.data));
}

async function tryLoadLive(): Promise<void> {
  try {
    const [metricsResponse, workoutsResponse] = await Promise.all([
      fetch("/api/metrics", { headers: { Accept: "application/json" } }),
      fetch("/api/workouts", { headers: { Accept: "application/json" } }),
    ]);
    if (!metricsResponse.ok || !workoutsResponse.ok) {
      return;
    }
    const metrics = (await metricsResponse.json()) as HealthSnapshot[];
    const workouts = (await workoutsResponse.json()) as Workout[];
    state.data = { metrics, workouts };
    state.mode = "live";
    byId<HTMLElement>("live-note").style.display = "block";
    render();
  } catch (_error) {
    // Not served by the demo server; fall back to generated data.
  }
}

function wireEvents(): void {
  byId<HTMLButtonElement>("btn-apply").addEventListener("click", applyLatestEdits);
  byId<HTMLButtonElement>("btn-regenerate").addEventListener("click", regenerate);
  byId<HTMLButtonElement>("btn-export").addEventListener("click", exportFull);
  byId<HTMLButtonElement>("btn-copy-metrics").addEventListener("click", () => {
    void copyText(formatJson(state.data.metrics), "copy-feedback-metrics");
  });
  byId<HTMLButtonElement>("btn-copy-workouts").addEventListener("click", () => {
    void copyText(formatJson(state.data.workouts), "copy-feedback-workouts");
  });
  byId<HTMLButtonElement>("btn-copy-full").addEventListener("click", () => {
    void copyText(formatJson(state.data), "copy-feedback-full");
  });
}

async function main(): Promise<void> {
  state.data = {
    metrics: generateMetricHistory(),
    workouts: generateWorkouts(),
  };
  wireEvents();
  render();
  await tryLoadLive();
}

void main();
