import type { Exercise, HealthSnapshot, Workout, Workout as WorkoutMeta } from "./types";

const DAY_MS = 86_400_000;

function round(value: number, decimals: number): number {
  const factor = Math.pow(10, decimals);
  return Math.round(value * factor) / factor;
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value));
}

export interface ProfileOptions {
  age: number;
  gender: "male" | "female";
  heightCm: number;
  startWeightKg: number;
  targetWeightKg: number;
  days: number;
}

const DEFAULT_PROFILE: ProfileOptions = {
  age: 29,
  gender: "male",
  heightCm: 178,
  startWeightKg: 87.4,
  targetWeightKg: 81.6,
  days: 120,
};

function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function generateMetricHistory(options: Partial<ProfileOptions> = {}): HealthSnapshot[] {
  const profile: ProfileOptions = { ...DEFAULT_PROFILE, ...options };
  const snapshots: HealthSnapshot[] = [];
  const today = new Date();
  today.setHours(12, 0, 0, 0);

  let weight = profile.startWeightKg;
  let previousFatPct = 21.4;
  let previousMusclePct = 41.2;

  for (let i = profile.days - 1; i >= 0; i--) {
    const date = new Date(today.getTime() - i * DAY_MS);
    const progress = (profile.days - 1 - i) / (profile.days - 1);

    const weightNoise = (Math.random() - 0.5) * 0.3;
    weight =
      profile.startWeightKg -
      (profile.startWeightKg - profile.targetWeightKg) * progress +
      weightNoise;
    weight = round(clamp(weight, profile.targetWeightKg - 0.5, profile.startWeightKg + 0.5), 1);

    const fatPct = round(
      clamp(previousFatPct - 0.02 - Math.random() * 0.012 + (Math.random() - 0.5) * 0.3, 15, 30),
      1,
    );
    previousFatPct = fatPct;

    const muscleMassPct = round(
      clamp(previousMusclePct + 0.015 + Math.random() * 0.008 + (Math.random() - 0.5) * 0.2, 36, 50),
      1,
    );
    previousMusclePct = muscleMassPct;

    const heightCm = profile.heightCm;
    const bmi = round(weight / Math.pow(heightCm / 100, 2), 1);
    const muscleWeightKg = round((weight * muscleMassPct) / 100, 1);
    const bmr =
      profile.gender === "male"
        ? Math.round(10 * weight + 6.25 * heightCm - 5 * profile.age + 5)
        : Math.round(10 * weight + 6.25 * heightCm - 5 * profile.age - 161);

    snapshots.push({
      date: toIsoDate(date),
      weightKg: weight,
      heightCm,
      muscleMassPct,
      fatPct,
      bmi,
      muscleWeightKg,
      age: profile.age,
      gender: profile.gender,
      bmr,
    });
  }
  return snapshots;
}

interface ProgramSeed {
  id: number;
  title: string;
  subtitle: string;
  muscleGroups: string[];
  weeklyFrequency: number;
  exercises: {
    name: string;
    muscleGroup: string;
    sets: { reps: number; weightKg: number; restSec: number }[];
  }[];
}

const PROGRAM_SEEDS: ProgramSeed[] = [
  {
    id: 1,
    title: "Push Day",
    subtitle: "Chest, shoulders and triceps strength",
    muscleGroups: ["Chest", "Shoulders", "Triceps"],
    weeklyFrequency: 2,
    exercises: [
      {
        name: "Barbell Bench Press",
        muscleGroup: "Chest",
        sets: [
          { reps: 10, weightKg: 60, restSec: 90 },
          { reps: 8, weightKg: 70, restSec: 90 },
          { reps: 6, weightKg: 80, restSec: 120 },
          { reps: 8, weightKg: 70, restSec: 120 },
        ],
      },
      {
        name: "Seated Dumbbell Shoulder Press",
        muscleGroup: "Shoulders",
        sets: [
          { reps: 12, weightKg: 16, restSec: 75 },
          { reps: 10, weightKg: 18, restSec: 75 },
          { reps: 8, weightKg: 20, restSec: 90 },
        ],
      },
      {
        name: "Cable Triceps Pushdown",
        muscleGroup: "Triceps",
        sets: [
          { reps: 15, weightKg: 25, restSec: 60 },
          { reps: 12, weightKg: 30, restSec: 60 },
          { reps: 12, weightKg: 30, restSec: 60 },
        ],
      },
    ],
  },
  {
    id: 2,
    title: "Pull Day",
    subtitle: "Back, biceps and grip work",
    muscleGroups: ["Back", "Biceps"],
    weeklyFrequency: 2,
    exercises: [
      {
        name: "Deadlift",
        muscleGroup: "Back",
        sets: [
          { reps: 6, weightKg: 100, restSec: 120 },
          { reps: 5, weightKg: 110, restSec: 120 },
          { reps: 5, weightKg: 110, restSec: 150 },
        ],
      },
      {
        name: "Lat Pulldown",
        muscleGroup: "Back",
        sets: [
          { reps: 12, weightKg: 55, restSec: 75 },
          { reps: 10, weightKg: 60, restSec: 75 },
          { reps: 8, weightKg: 65, restSec: 90 },
        ],
      },
      {
        name: "Barbell Biceps Curl",
        muscleGroup: "Biceps",
        sets: [
          { reps: 12, weightKg: 25, restSec: 60 },
          { reps: 10, weightKg: 30, restSec: 60 },
          { reps: 8, weightKg: 35, restSec: 75 },
        ],
      },
    ],
  },
  {
    id: 3,
    title: "Leg Day",
    subtitle: "Lower-body power and stability",
    muscleGroups: ["Quads", "Hamstrings", "Glutes"],
    weeklyFrequency: 2,
    exercises: [
      {
        name: "Back Squat",
        muscleGroup: "Quads",
        sets: [
          { reps: 10, weightKg: 70, restSec: 120 },
          { reps: 8, weightKg: 85, restSec: 120 },
          { reps: 6, weightKg: 95, restSec: 150 },
          { reps: 6, weightKg: 95, restSec: 150 },
        ],
      },
      {
        name: "Romanian Deadlift",
        muscleGroup: "Hamstrings",
        sets: [
          { reps: 10, weightKg: 60, restSec: 90 },
          { reps: 10, weightKg: 65, restSec: 90 },
          { reps: 8, weightKg: 70, restSec: 120 },
        ],
      },
      {
        name: "Leg Press",
        muscleGroup: "Glutes",
        sets: [
          { reps: 12, weightKg: 120, restSec: 75 },
          { reps: 12, weightKg: 130, restSec: 75 },
          { reps: 10, weightKg: 140, restSec: 90 },
        ],
      },
    ],
  },
  {
    id: 4,
    title: "Core & Mobility",
    subtitle: "Stability, balance and range of motion",
    muscleGroups: ["Core"],
    weeklyFrequency: 1,
    exercises: [
      {
        name: "Plank Hold",
        muscleGroup: "Core",
        sets: [
          { reps: 60, weightKg: 0, restSec: 60 },
          { reps: 60, weightKg: 0, restSec: 60 },
          { reps: 90, weightKg: 0, restSec: 60 },
        ],
      },
      {
        name: "Hanging Leg Raise",
        muscleGroup: "Core",
        sets: [
          { reps: 10, weightKg: 0, restSec: 60 },
          { reps: 10, weightKg: 0, restSec: 60 },
          { reps: 8, weightKg: 0, restSec: 75 },
        ],
      },
      {
        name: "Bird Dog",
        muscleGroup: "Core",
        sets: [
          { reps: 12, weightKg: 0, restSec: 45 },
          { reps: 12, weightKg: 0, restSec: 45 },
        ],
      },
    ],
  },
];

export function generateWorkouts(): Workout[] {
  let exerciseId = 1;
  const workouts: Workout[] = PROGRAM_SEEDS.map((seed) => {
    const exercises: Exercise[] = seed.exercises.map((exercise) => ({
      id: exerciseId++,
      name: exercise.name,
      muscleGroup: exercise.muscleGroup,
      sets: exercise.sets.map((set, index) => ({
        setOrder: index + 1,
        reps: set.reps,
        weightKg: set.weightKg,
        restSec: set.restSec,
      })),
    }));
    return {
      id: seed.id,
      title: seed.title,
      subtitle: seed.subtitle,
      muscleGroups: seed.muscleGroups,
      weeklyFrequency: seed.weeklyFrequency,
      isActive: true,
      exercises,
    };
  });
  return workouts;
}
