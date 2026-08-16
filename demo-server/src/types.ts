export interface HealthSnapshot {
  date: string;
  weightKg: number;
  heightCm: number;
  muscleMassPct: number;
  fatPct: number;
  bmi: number;
  muscleWeightKg: number;
  age: number;
  gender: string;
  bmr: number;
}

export interface ExerciseSet {
  setOrder: number;
  reps: number;
  weightKg: number;
  restSec: number;
}

export interface Exercise {
  id: number;
  name: string;
  muscleGroup: string;
  sets: ExerciseSet[];
}

export interface Workout {
  id: number;
  title: string;
  subtitle: string;
  muscleGroups: string[];
  weeklyFrequency: number;
  isActive: boolean;
  exercises: Exercise[];
}

export interface DemoData {
  metrics: HealthSnapshot[];
  workouts: Workout[];
}
