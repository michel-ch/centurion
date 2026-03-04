# Prompt: Build a "100 Push-Ups" Android Fitness App

> Copy everything below this line and paste it into your AI coding assistant (Claude, Cursor, etc.) to generate the full Android app.

---

## PROJECT OVERVIEW

Build a native Android app in **Kotlin** using **Jetpack Compose** and **Material 3** called **"CENTURY — 100 Push-Up Challenge"**. The app guides users through a 30-day bodyweight training program that builds them up to 100 consecutive push-ups while shaping their entire body. The app must collect user data, track progress, and provide a motivating, dark-themed experience.

---

## TECH STACK

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 (Material You dynamic theming)
- **Architecture:** MVVM with Clean Architecture layers
- **Local DB:** Room (SQLite)
- **DI:** Hilt
- **Navigation:** Jetpack Navigation Compose
- **Charts:** Vico or MPAndroidChart (Compose wrapper)
- **Image Loading:** Coil (Compose-native, with caching and crossfade)
- **Notifications:** WorkManager for daily reminders
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34

---

## SCREEN-BY-SCREEN SPECIFICATION

### 1. ONBOARDING / USER PROFILE SCREEN (First launch only)

Collect the following user data with a clean, step-by-step form (one field per page with a progress indicator at the top):

| Field | Input Type | Validation | Notes |
|-------|-----------|------------|-------|
| **Name** | Text field | Required, 2-30 chars | Used for greetings throughout the app |
| **Body Weight** | Number field + unit toggle (kg / lbs) | Required, 30-300 kg / 66-660 lbs | Used for calorie estimates and progress tracking |
| **Height** | Number field + unit toggle (cm / ft+in) | Required, 100-250 cm / 3'3"–8'2" | Used for BMI calculation |
| **Age** | Number field | Required, 13-99 | Affects calorie estimates |
| **Gender** | Single select: Male / Female / Other | Required | Affects calorie estimates |
| **Fitness Level** | Single select: Beginner / Intermediate / Advanced | Required | Adjusts starting difficulty |
| **Current Max Push-Ups** | Number field | Required, 0-200 | Used to calibrate Week 1 starting reps |
| **Goal Weight** (optional) | Number field + same unit as body weight | Optional | Displayed on progress screen |
| **Daily Reminder Time** | Time picker | Default 7:00 AM | Sets notification schedule |
| **Profile Photo** (optional) | Camera / Gallery picker | Optional | Circular avatar on home screen |

**Behavior:**
- Store all data in Room database
- Allow editing anytime from Settings screen
- Recalculate BMI and calorie estimates when weight/height changes
- Show a summary card at the end of onboarding before the user confirms

### 2. HOME / DASHBOARD SCREEN

The main screen after onboarding. Show:

- **Greeting:** "Hey {name}, Day {X} of 30" with profile photo
- **Current Week Card:** Week number, title (FOUNDATION / VOLUME / INTENSITY / THE CENTURY), and push-up target
- **Today's Workout Card:** Day label (e.g., "PUSH + CORE"), number of exercises, estimated duration, and a large "START WORKOUT" button
- **Quick Stats Row:** Total push-ups done (all time), current streak (consecutive days completed), body weight trend (small sparkline chart)
- **Weekly Progress Ring:** Circular progress showing days completed this week out of 7
- **Body Weight Log Button:** Quick-add today's weight with a floating action button

### 3. WORKOUT SCREEN

When the user taps "START WORKOUT":

- Show exercises in a scrollable list. Each exercise card displays:
  - **Illustration image** (top of card): A placeholder area (16:9 aspect ratio, rounded corners) showing a demonstration image of the exercise. Load from `res/drawable/exercise_{snake_case_name}.webp`. If no image exists, show a dark placeholder with the exercise name in large monospace text and a camera icon the user can tap to add their own photo from gallery. The app should ship with placeholder silhouette illustrations for all exercises (see EXERCISE ILLUSTRATION SYSTEM below).
  - **Exercise name** (bold, large)
  - **Sets x Reps** (red accent color)
  - **Rest between sets** (displayed as a subtle tag, e.g., "REST: 60s")
  - **Form note** (smaller gray text below)
- Each exercise has a **checkbox** to mark complete
- **Rest timer between sets:** After the user completes a set and taps the set counter, auto-start a visible countdown timer for the prescribed rest duration. The timer should:
  - Display prominently as a full-width bar at the bottom of the screen with large countdown numbers
  - Play a notification sound + vibrate when time is up
  - Show a "SKIP REST" button to jump ahead
  - Auto-advance to next set when timer reaches zero
- **Rest timer between exercises:** When all sets of an exercise are done and the user checks it off, start a transition rest timer (default: 90s between exercises, configurable in settings) before highlighting the next exercise
- For timed exercises (planks, wall sits), show an **inline countdown timer** with start/pause/reset buttons directly on the exercise card
- **Push-Up Counter Mode:** For push-up sets, offer an optional screen where the user taps the screen with their nose/chin at the bottom of each rep to auto-count (using proximity sensor or simple touch)
- **Set-by-set tracking:** Each exercise expands to show individual sets as small circles (e.g., ○ ○ ○ ○ for 4 sets). Tap each circle to mark that set done, which triggers the rest timer. Filled circles = completed sets.
- Show a **progress bar** at the top (exercises completed / total)
- When all exercises are checked off, show a **completion celebration screen** with confetti animation, total reps, total rest time, and calories burned estimate
- Save the completed workout to Room DB with timestamp, duration, and all exercise data

### 4. PROGRAM SCREEN (Full 4-Week Calendar View)

- Show all 4 weeks in an expandable accordion or tab layout
- Each day shows: day number, workout label, completion status (checkmark / locked / upcoming)
- Tapping a day shows the full exercise list for that day
- Past days show logged data (actual reps completed, time taken)
- Color coding: completed = green, today = red accent, future = gray

### 5. PROGRESS / STATS SCREEN

- **Body Weight Chart:** Line graph showing weight over time (from weight log entries), with goal weight as a dashed horizontal line
- **Push-Up Progress Chart:** Bar chart showing total push-ups per day over the 30 days
- **Max Push-Up Test Chart:** Line graph of test results (tested on Day 6 of each week)
- **BMI Card:** Current BMI with color-coded category (underweight / normal / overweight / obese)
- **Body Stats Cards:** Current weight, starting weight, weight change, estimated body fat % (basic formula)
- **Streak & Consistency:** Current streak, longest streak, total workouts completed, completion percentage
- **Estimated Calories Burned:** Running total based on exercises x body weight x MET values

### 6. BODY WEIGHT LOG SCREEN

- **Quick Entry:** Large number input at top with +/- 0.1 buttons and the unit (kg/lbs)
- **History List:** Scrollable list of all weight entries with date, weight, and change from previous
- **Trend Line:** Small chart showing last 30 days
- Allow editing and deleting past entries (swipe to delete with undo)
- Option to log at specific past dates (for catching up)

### 7. SETTINGS SCREEN

- **Edit Profile:** All onboarding fields editable
- **Units:** Toggle metric (kg/cm) or imperial (lbs/ft+in) — converts all stored data
- **Rest Timer Between Sets:** Default: 60s (options: 30s / 45s / 60s / 90s / 120s) — overrides per-exercise defaults
- **Rest Timer Between Exercises:** Default: 90s (options: 60s / 90s / 120s / 150s)
- **Auto-Reduce Rest by Week:** Toggle on/off (default on) — progressively shortens rest each week
- **Daily Reminder:** Toggle on/off + time picker
- **Dark/Light Theme:** Toggle (default dark)
- **Export Data:** Export workout history and weight log as CSV
- **Reset Program:** Start the 30-day program over (with confirmation dialog)
- **About:** App version, credits

---

## 4-WEEK TRAINING PROGRAM DATA

Embed the following complete program as a sealed data structure. Each week has 7 days, each day has a label and a list of exercises. Each exercise includes: name, sets, form note, **rest time between sets**, and an **illustrationId** (snake_case string matching the drawable resource name).

**Rest time notation:** Each exercise now includes a rest duration between sets in parentheses after the sets notation, formatted as `[REST Xs]`. Between exercises, use a default of 90s unless specified otherwise.

### WEEK 1 — FOUNDATION (Push-Up Target: 4x10 to 4x12)

**Day 1 — PUSH + CORE:**
Push-Ups 4x10 [REST 60s] (Strict form, chest to floor) illustrationId: push_up_standard, Incline Push-Ups 3x12 [REST 45s] (Hands on bench or step) illustrationId: push_up_incline, Diamond Push-Ups 3x6 [REST 60s] (Hands close together) illustrationId: push_up_diamond, Plank Hold 3x30s [REST 30s] (Squeeze glutes & abs) illustrationId: plank_hold, Dead Bugs 3x10/side [REST 30s] (Slow & controlled) illustrationId: dead_bug, Mountain Climbers 3x20 [REST 45s] (Keep hips level) illustrationId: mountain_climber

**Day 2 — LEGS + CARDIO:**
Bodyweight Squats 4x15 [REST 45s] (Below parallel) illustrationId: squat_bodyweight, Lunges 3x12/leg [REST 45s] (Long stride, upright torso) illustrationId: lunge_forward, Glute Bridges 3x15 [REST 30s] (Squeeze 2s at top) illustrationId: glute_bridge, Calf Raises 3x20 [REST 30s] (Full range of motion) illustrationId: calf_raise, Jump Squats 3x10 [REST 60s] (Land softly) illustrationId: squat_jump, High Knees 3x30s [REST 45s] (Max effort) illustrationId: high_knees

**Day 3 — REST / ACTIVE RECOVERY:**
Walk or Light Jog 20 min (Stay loose) illustrationId: walk_jog, Full Body Stretch 10 min (Hit every muscle group) illustrationId: stretch_full_body, Foam Roll (if available) 10 min (Focus sore areas) illustrationId: foam_roll

**Day 4 — PUSH + PULL:**
Push-Ups 4x12 [REST 60s] (Push the rep count) illustrationId: push_up_standard, Wide Push-Ups 3x10 [REST 45s] (Hands outside shoulders) illustrationId: push_up_wide, Superman Holds 3x12 [REST 30s] (Squeeze shoulder blades) illustrationId: superman_hold, Towel Rows (door frame) 3x10 [REST 45s] (Or inverted rows) illustrationId: towel_row, Reverse Snow Angels 3x10 [REST 30s] (Face down, arms sweep) illustrationId: reverse_snow_angel, Bicycle Crunches 3x20 [REST 30s] (Slow, touch elbow to knee) illustrationId: bicycle_crunch

**Day 5 — LEGS + CORE:**
Bulgarian Split Squats 3x10/leg [REST 60s] (Rear foot elevated) illustrationId: bulgarian_split_squat, Wall Sit 3x30s [REST 30s] (Thighs parallel) illustrationId: wall_sit, Single-Leg Glute Bridges 3x10/leg [REST 30s] (Drive through heel) illustrationId: glute_bridge_single_leg, Side Plank 3x20s/side [REST 20s] (Hips high) illustrationId: side_plank, Leg Raises 3x12 [REST 30s] (Lower back stays flat) illustrationId: leg_raise, Burpees 3x8 [REST 60s] (Full extension at top) illustrationId: burpee

**Day 6 — PUSH-UP TEST + CONDITIONING:**
MAX Push-Up Test 1 set to failure (Record your number) illustrationId: push_up_standard, Jumping Jacks 3x40 [REST 30s] (Full arm extension) illustrationId: jumping_jack, Squat Thrusts 3x12 [REST 45s] (Explosive) illustrationId: squat_thrust, Plank to Push-Up 3x8 [REST 45s] (Forearm to hand) illustrationId: plank_to_pushup, Flutter Kicks 3x20 [REST 30s] (Lower back on ground) illustrationId: flutter_kick

**Day 7 — FULL REST:**
Complete Rest (Sleep 8+ hours. Hydrate. Recover.) illustrationId: rest_day

### WEEK 2 — VOLUME (Push-Up Target: 5x12 to 5x15)

**Day 1 — PUSH OVERLOAD:**
Push-Ups 5x12 [REST 60s] (60s rest between sets) illustrationId: push_up_standard, Decline Push-Ups 3x10 [REST 45s] (Feet on chair) illustrationId: push_up_decline, Diamond Push-Ups 3x8 [REST 60s] (Elbows stay tight) illustrationId: push_up_diamond, Archer Push-Ups 3x5/side [REST 60s] (Wide, shift weight side to side) illustrationId: push_up_archer, Plank Shoulder Taps 3x16 [REST 30s] (Minimal hip sway) illustrationId: plank_shoulder_tap, Ab Rollouts (towel) 3x8 [REST 45s] (Slide hands forward on towel) illustrationId: ab_rollout_towel

**Day 2 — LEGS + PLYOMETRICS:**
Jump Squats 4x12 [REST 60s] (Explode upward) illustrationId: squat_jump, Walking Lunges 4x12/leg [REST 45s] (Big steps) illustrationId: lunge_walking, Pistol Squat Progressions 3x5/leg [REST 60s] (Use a chair for support) illustrationId: pistol_squat_assisted, Box Jumps (stair/bench) 3x10 [REST 60s] (Step down, don't jump down) illustrationId: box_jump, Calf Raises 4x20 [REST 30s] (3s pause at top) illustrationId: calf_raise, Sprint Intervals 6x20s on / 40s off (All out effort) illustrationId: sprint_interval

**Day 3 — REST / MOBILITY:**
Yoga Flow or Walk 25 min (Focus on hips & shoulders) illustrationId: yoga_flow, Deep Stretching 15 min (Hold each stretch 45s) illustrationId: stretch_deep

**Day 4 — UPPER BODY + CORE:**
Push-Ups 5x15 [REST 60s] (Keep pushing volume) illustrationId: push_up_standard, Pike Push-Ups 3x8 [REST 60s] (Hips high, targets shoulders) illustrationId: push_up_pike, Towel/Band Rows 4x12 [REST 45s] (Squeeze shoulder blades) illustrationId: towel_row, Superman Pulses 3x15 [REST 30s] (Small controlled pulses) illustrationId: superman_pulse, Hanging Knee Raises (or lying) 3x12 [REST 30s] (Control the descent) illustrationId: knee_raise, Russian Twists 3x20 [REST 30s] (Feet off ground) illustrationId: russian_twist

**Day 5 — LEGS + GLUTES:**
Sumo Squats 4x15 [REST 45s] (Wide stance, toes out) illustrationId: squat_sumo, Single-Leg Deadlifts 3x10/leg [REST 45s] (Balance challenge) illustrationId: deadlift_single_leg, Donkey Kicks 3x15/leg [REST 30s] (Squeeze at top) illustrationId: donkey_kick, Fire Hydrants 3x15/leg [REST 30s] (Keep core tight) illustrationId: fire_hydrant, Side Lunges 3x10/side [REST 45s] (Sit deep into the hip) illustrationId: lunge_side, Tabata Burpees 4 min 20s on/10s off (Survive it) illustrationId: burpee

**Day 6 — PUSH-UP LADDER + CARDIO:**
Push-Up Ladder 1-2-3...10 then back down [REST 15s between rungs] (110 total reps) illustrationId: push_up_standard, Mountain Climbers 4x20 [REST 30s] (Fast but controlled) illustrationId: mountain_climber, Star Jumps 3x15 [REST 45s] (Max height) illustrationId: star_jump, Plank Hold 3x45s [REST 30s] (Don't let hips sag) illustrationId: plank_hold

**Day 7 — FULL REST:**
Complete Rest (Eat well. Sleep deep.) illustrationId: rest_day

### WEEK 3 — INTENSITY (Push-Up Target: 5x18 to 4x25)

**Day 1 — PUSH POWER:**
Push-Ups 5x18 [REST 45s] (45s rest between sets) illustrationId: push_up_standard, Explosive Push-Ups 3x8 [REST 60s] (Hands leave the floor) illustrationId: push_up_explosive, Decline Diamond Push-Ups 3x8 [REST 60s] (Feet elevated, close grip) illustrationId: push_up_decline_diamond, Tempo Push-Ups (3s down) 3x8 [REST 60s] (Slow eccentric) illustrationId: push_up_tempo, L-Sit Hold (on floor) 3x15s [REST 30s] (Legs straight if possible) illustrationId: l_sit_hold, V-Ups 3x12 [REST 30s] (Touch toes at top) illustrationId: v_up

**Day 2 — LEGS + POWER:**
Squat Jumps 5x12 [REST 60s] (Max height each rep) illustrationId: squat_jump, Pistol Squats (assisted) 4x6/leg [REST 60s] (Use wall or doorframe) illustrationId: pistol_squat_assisted, Nordic Curl Negatives 3x5 [REST 60s] (Slow 5s descent) illustrationId: nordic_curl, Step-Ups (high step) 3x12/leg [REST 45s] (Drive through the heel) illustrationId: step_up, Wall Sit Hold 3x45s [REST 30s] (Add arm raises for difficulty) illustrationId: wall_sit, Tuck Jumps 3x10 [REST 60s] (Knees to chest) illustrationId: tuck_jump

**Day 3 — ACTIVE RECOVERY:**
Light Jog or Walk 25 min (Conversational pace) illustrationId: walk_jog, Dynamic Stretching 15 min (Leg swings, arm circles) illustrationId: stretch_dynamic, Push-Up Greasing the Groove 5 sets of 10 throughout day (Never go to failure) illustrationId: push_up_standard

**Day 4 — FULL UPPER:**
Push-Ups 4x20 [REST 60s] (Getting close to the goal) illustrationId: push_up_standard, Handstand Wall Hold 3x20s [REST 45s] (Face the wall) illustrationId: handstand_wall_hold, Pike Push-Ups 4x10 [REST 60s] (Deeper range) illustrationId: push_up_pike, Inverted Rows (table) 4x12 [REST 45s] (Body straight like a plank) illustrationId: inverted_row, Plank Up-Downs 3x12 [REST 30s] (Alternate leading arm) illustrationId: plank_up_down, Dragon Flags (progression) 3x5 [REST 60s] (Tuck knees if needed) illustrationId: dragon_flag

**Day 5 — LEGS + HIIT:**
Bulgarian Split Squats 4x12/leg [REST 60s] (Deep stretch at bottom) illustrationId: bulgarian_split_squat, Broad Jumps 4x8 [REST 60s] (Stick the landing) illustrationId: broad_jump, Hip Thrusts (elevated) 4x15 [REST 45s] (Shoulders on couch/bench) illustrationId: hip_thrust, HIIT Circuit 3 rounds 30s each [REST 10s between exercises, 60s between rounds] (Burpees then Squats then High Knees then Lunges) illustrationId: hiit_circuit, Calf Raises (single leg) 3x15/leg [REST 30s] (Full ROM) illustrationId: calf_raise_single

**Day 6 — PUSH-UP VOLUME DAY:**
Push-Ups 4x25 [REST 90s] (Break into micro-sets if needed) illustrationId: push_up_standard, Close-Grip Push-Ups 3x10 [REST 45s] (Tricep focus) illustrationId: push_up_close_grip, Wide Push-Ups 3x12 [REST 45s] (Chest focus) illustrationId: push_up_wide, Plank Hold 3x60s [REST 30s] (One minute, no breaks) illustrationId: plank_hold, Hollow Body Hold 3x20s [REST 30s] (Press lower back down) illustrationId: hollow_body_hold

**Day 7 — FULL REST:**
Complete Rest (Trust the process.) illustrationId: rest_day

### WEEK 4 — THE CENTURY (Push-Up Target: 100 total to 100 straight)

**Day 1 — PUSH — DENSITY TRAINING:**
Push-Ups 100 total in minimum sets possible [REST as needed, aim under 30s] (Time yourself. Rest as little as possible.) illustrationId: push_up_standard, Archer Push-Ups 3x6/side [REST 60s] (Unilateral strength) illustrationId: push_up_archer, Plank Shoulder Taps 3x20 [REST 30s] (Zero hip rotation) illustrationId: plank_shoulder_tap, Leg Raises 3x15 [REST 30s] (Control every inch) illustrationId: leg_raise

**Day 2 — LEGS + EXPLOSIVE:**
Pistol Squats 4x6/leg [REST 60s] (Full depth) illustrationId: pistol_squat_assisted, Box Jump Burpees 4x8 [REST 60s] (Combo movement) illustrationId: box_jump_burpee, Walking Lunges 4x16/leg [REST 45s] (Long steps) illustrationId: lunge_walking, Single-Leg Calf Raises 4x15/leg [REST 30s] (Slow & controlled) illustrationId: calf_raise_single, Sprint Intervals 8x20s on / 40s off (Leave nothing) illustrationId: sprint_interval

**Day 3 — ACTIVE RECOVERY:**
Easy Walk or Swim 30 min (Flush the muscles) illustrationId: walk_jog, Full Body Stretch 15 min (Deep holds, breathe) illustrationId: stretch_full_body, Greasing the Groove Push-Ups 8 sets of 10 spread throughout day (Never near failure) illustrationId: push_up_standard

**Day 4 — UPPER BODY — PEAK:**
Push-Ups 2x50 [REST 120s] (Two big sets. Push through.) illustrationId: push_up_standard, Handstand Push-Up Negatives 3x3 [REST 90s] (Against wall, 5s down) illustrationId: handstand_pushup_negative, Inverted Rows 4x12 [REST 45s] (Table or sturdy bar) illustrationId: inverted_row, Superman + Push-Up Combo 3x8 [REST 60s] (Superman then push-up) illustrationId: superman_pushup_combo, Ab Wheel / Towel Rollouts 3x10 [REST 45s] (Full extension) illustrationId: ab_rollout_towel

**Day 5 — FULL BODY BURN:**
Circuit 5 rounds minimal rest [REST 10s between exercises, 90s between rounds]: Push-Ups x20 illustrationId: push_up_standard then Jump Squats x15 illustrationId: squat_jump then Burpees x10 illustrationId: burpee then Mountain Climbers x20 illustrationId: mountain_climber then Plank Hold 30s illustrationId: plank_hold

**Day 6 — THE 100 PUSH-UP CHALLENGE:**
Warm-Up: Arm Circles + Light Push-Ups 2x10 [REST 30s] (Get blood flowing) illustrationId: warmup_arm_circles, 100 CONSECUTIVE PUSH-UPS 1 set of 100 (If you stall, hold plank and continue. YOU GOT THIS.) illustrationId: push_up_standard, Celebration Stretch 15 min (You earned it. Stretch everything.) illustrationId: stretch_full_body

**Day 7 — VICTORY REST:**
You did it. (Reflect. Recover. Plan what's next.) illustrationId: rest_day

---

## EXERCISE ILLUSTRATION SYSTEM

Every exercise in the app must have a dedicated illustration area. Implement the following system:

### Image Architecture

```
res/drawable/
├── exercise_push_up_standard.webp
├── exercise_push_up_incline.webp
├── exercise_push_up_decline.webp
├── exercise_push_up_diamond.webp
├── exercise_push_up_wide.webp
├── exercise_push_up_close_grip.webp
├── exercise_push_up_archer.webp
├── exercise_push_up_explosive.webp
├── exercise_push_up_decline_diamond.webp
├── exercise_push_up_tempo.webp
├── exercise_push_up_pike.webp
├── exercise_plank_hold.webp
├── exercise_plank_shoulder_tap.webp
├── exercise_plank_to_pushup.webp
├── exercise_plank_up_down.webp
├── exercise_side_plank.webp
├── exercise_dead_bug.webp
├── exercise_mountain_climber.webp
├── exercise_squat_bodyweight.webp
├── exercise_squat_jump.webp
├── exercise_squat_sumo.webp
├── exercise_lunge_forward.webp
├── exercise_lunge_walking.webp
├── exercise_lunge_side.webp
├── exercise_glute_bridge.webp
├── exercise_glute_bridge_single_leg.webp
├── exercise_calf_raise.webp
├── exercise_calf_raise_single.webp
├── exercise_high_knees.webp
├── exercise_burpee.webp
├── exercise_superman_hold.webp
├── exercise_superman_pulse.webp
├── exercise_towel_row.webp
├── exercise_reverse_snow_angel.webp
├── exercise_bicycle_crunch.webp
├── exercise_bulgarian_split_squat.webp
├── exercise_wall_sit.webp
├── exercise_leg_raise.webp
├── exercise_jumping_jack.webp
├── exercise_squat_thrust.webp
├── exercise_flutter_kick.webp
├── exercise_ab_rollout_towel.webp
├── exercise_pistol_squat_assisted.webp
├── exercise_box_jump.webp
├── exercise_russian_twist.webp
├── exercise_knee_raise.webp
├── exercise_donkey_kick.webp
├── exercise_fire_hydrant.webp
├── exercise_deadlift_single_leg.webp
├── exercise_star_jump.webp
├── exercise_nordic_curl.webp
├── exercise_step_up.webp
├── exercise_tuck_jump.webp
├── exercise_l_sit_hold.webp
├── exercise_v_up.webp
├── exercise_handstand_wall_hold.webp
├── exercise_handstand_pushup_negative.webp
├── exercise_inverted_row.webp
├── exercise_dragon_flag.webp
├── exercise_broad_jump.webp
├── exercise_hip_thrust.webp
├── exercise_hollow_body_hold.webp
├── exercise_box_jump_burpee.webp
├── exercise_superman_pushup_combo.webp
├── exercise_warmup_arm_circles.webp
├── exercise_hiit_circuit.webp
├── exercise_sprint_interval.webp
├── exercise_walk_jog.webp
├── exercise_yoga_flow.webp
├── exercise_foam_roll.webp
├── exercise_stretch_full_body.webp
├── exercise_stretch_deep.webp
├── exercise_stretch_dynamic.webp
├── exercise_rest_day.webp
└── exercise_placeholder.webp       ← Generic fallback
```

### Image Card UI Specification

Each exercise card in the workout screen must include:

```
┌─────────────────────────────────────────┐
│  ┌─────────────────────────────────┐    │
│  │                                 │    │
│  │     ILLUSTRATION IMAGE          │    │
│  │     (16:9 ratio, rounded 12dp)  │    │
│  │     Dark overlay gradient at    │    │
│  │     bottom for readability      │    │
│  │                                 │    │
│  │  [📷 Tap to replace] (if user   │    │
│  │   wants custom photo)           │    │
│  └─────────────────────────────────┘    │
│                                         │
│  ☐  Push-Ups                    4×10    │
│      REST: 60s between sets             │
│      Strict form, chest to floor        │
│                                         │
│      ○ ○ ○ ○  (set tracker circles)     │
│                                         │
└─────────────────────────────────────────┘
```

### Placeholder Behavior

For exercises without a bundled image:
- Show a dark card (#1A1A1A) with the exercise name in large monospace uppercase text
- Display a subtle body silhouette icon (use a vector drawable)
- Overlay a small camera icon button (bottom-right corner) that lets the user:
  - Take a photo of themselves doing the exercise
  - Pick an image from gallery
  - Photos are saved to internal storage and linked to the exercise's illustrationId
- Once the user adds a custom image, it replaces the placeholder permanently (stored in Room as a URI)

### Custom Image Upload

Allow users to replace ANY exercise image (even bundled ones) with their own:
- Long-press on any exercise image → shows "Replace image" / "Reset to default" options
- Custom images are stored in app internal storage: `files/exercise_images/{illustrationId}.webp`
- Compress to max 800px width, WebP format, 80% quality
- Track custom overrides in Room: **ExerciseImage** table (illustrationId, customImageUri, updatedAt)

### Image Loading

Use **Coil** (Compose-native image loader):
- Load bundled drawable by resource ID as default
- Check for custom override URI first
- Apply crossfade animation (300ms)
- Cache aggressively (memory + disk)
- Placeholder: dark shimmer animation while loading

---

## REST TIME REFERENCE TABLE

Summary of all rest periods used in the program. These values are embedded per-exercise in the data above and should be stored as an integer field (seconds) on each exercise entity.

| Exercise Type | Rest Between Sets | Rest Between Exercises |
|--------------|------------------|----------------------|
| Push-Up variations (standard, wide, incline) | 60s | 90s |
| Push-Up variations (diamond, archer, decline) | 60s | 90s |
| Push-Up volume sets (4x25, 2x50) | 90–120s | 120s |
| Push-Up ladder (rungs) | 15s between rungs | 90s after |
| Plank / isometric holds | 30s | 60s |
| Core exercises (crunches, leg raises, V-ups) | 30s | 60s |
| Squats / lunges (bodyweight) | 45s | 90s |
| Plyometrics (jump squats, box jumps, tuck jumps) | 60s | 90s |
| Glute / isolation (bridges, donkey kicks, fire hydrants) | 30s | 60s |
| HIIT circuit exercises | 10s between exercises | 60–90s between rounds |
| Sprint intervals | Work/rest built into protocol | 120s after block |
| Active recovery / stretching | No rest timer | No rest timer |

### Rest Time Progression

Rest times should automatically decrease as the program advances to build endurance:
- **Week 1:** Use rest times as listed (baseline)
- **Week 2:** Reduce all rest times by 5s (minimum 15s)
- **Week 3:** Reduce all rest times by 10s from baseline (minimum 10s)
- **Week 4:** Reduce all rest times by 15s from baseline (minimum 10s)

Display the adjusted rest time in the UI but allow the user to override with +10s / -10s buttons on the timer.

---

## FITNESS LEVEL ADJUSTMENTS

If the user selects **Beginner** (or current max push-ups < 10):
- Reduce all push-up sets by 40% (round up)
- Allow knee push-ups as a substitution option
- Extend program rest times by 15s
- Week 4 final target: 50 consecutive push-ups (still impressive)

If the user selects **Advanced** (or current max push-ups > 40):
- Increase all push-up sets by 25%
- Add weighted vest option (user inputs vest weight for calorie calculations)
- Reduce rest times by 15s
- Add bonus exercises on push days

---

## CALORIE ESTIMATION FORMULA

Use MET (Metabolic Equivalent of Task) values:
- Push-ups / bodyweight strength: MET 3.8
- Plyometrics / HIIT: MET 8.0
- Walking / active recovery: MET 3.0
- Stretching: MET 2.5

**Formula:** Calories = MET x body weight (kg) x duration (hours)

---

## DESIGN SYSTEM

- **Theme:** Dark by default (#0A0A0A background, #E8E8E8 text)
- **Accent Color:** Red #D4121A (primary actions, push-up related elements)
- **Success Color:** Green #22C55E (completed items)
- **Typography:** Monospace-inspired font for headings (like JetBrains Mono or Roboto Mono), clean sans-serif for body
- **Aesthetic:** Brutalist fitness — bold, high contrast, no soft curves. Sharp corners, heavy text weights, uppercase headings with wide letter-spacing
- **Animations:** Confetti on workout completion, number counters that animate up, progress rings that fill smoothly
- **Haptics:** Light haptic feedback on exercise checkbox taps, strong haptic on workout completion

---

## DATABASE SCHEMA (Room)

**UserProfile:** id, name, bodyWeight, bodyWeightUnit, height, heightUnit, age, gender, fitnessLevel, currentMaxPushUps, goalWeight, reminderTime, profilePhotoUri, createdAt, updatedAt

**WeightLog:** id, userId, weight, unit, loggedAt

**WorkoutSession:** id, userId, weekNumber, dayNumber, dayLabel, startedAt, completedAt, totalDuration, totalRestTime, totalReps, estimatedCalories, isCompleted

**ExerciseLog:** id, sessionId, exerciseName, illustrationId, targetSets, targetReps, completedSets, completedReps, restBetweenSetsSec, restBetweenExercisesSec, actualRestTimeSec, notes, completedAt

**ExerciseImage:** id, illustrationId (unique), customImageUri (nullable), isCustom (boolean), updatedAt — Tracks user-uploaded custom exercise images that override bundled defaults

**PushUpTest:** id, userId, weekNumber, maxReps, testedAt

---

## NOTIFICATIONS

- Daily reminder at user's chosen time: "Day {X}: {workout label} is waiting. Let's go."
- Streak at risk (missed yesterday): "Don't break your {X}-day streak! Quick workout today?"
- Weekly milestone: "Week {X} complete! You've done {total} push-ups so far."
- Rest day: "Recovery day — stretch, hydrate, and sleep well tonight."

---

## DELIVERABLES

Generate the complete Android project with:
1. All Kotlin source files organized by feature (onboarding, home, workout, program, progress, settings)
2. Room database with entities, DAOs, and migrations (including ExerciseImage table)
3. ViewModels for each screen
4. Compose UI for all screens described above
5. Navigation graph
6. Hilt modules for dependency injection
7. WorkManager setup for notifications
8. Complete program data as a Kotlin object (with restBetweenSetsSec, restBetweenExercisesSec, and illustrationId per exercise)
9. Exercise illustration system: Coil image loading, placeholder composable, custom image upload/replace logic, and internal storage management
10. Rest timer system: between-sets countdown, between-exercises countdown, auto-reduce by week, user override (+10s / -10s), skip button, sound + haptic alerts
11. Placeholder drawable resources: a generic `exercise_placeholder.webp` and a vector silhouette icon for missing images
12. Gradle build files with all dependencies (including Coil)
13. A README with build instructions and a note about replacing placeholder images with real exercise illustrations

Build the app step by step, starting with the data layer, then the domain/viewmodel layer, then the UI layer.
