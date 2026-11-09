# Phase 68 - driver seating fix (all vehicles)

Problem: every vehicle used ONE fixed seat (x -0.48, y 0.28, z -0.1). The Lamborghini is low, the monster truck body floats at
y 1.5-2.2 and the jeep seat is far back, so the driver stuck out of the roof / sat inside the chassis / in front of the seat.

Fix:
- `Vehicles.kt`: new `DriverSeat` (hip position, torso lean, leg + arm angles) per model, measured from the real meshes
  (seat cushion, backrest and steering-wheel positions in `vehicles.json`). `Vehicle.seat` gives the seat of its model.
- `CharacterModel.kt`: `CharPose` got `lean / driveLeg / driveKnee / driveArm / driveFore` (DRIVE pose only). Torso, head and arms
  pivot backwards around the hip; the head is counter-rotated so the driver still looks forward.
- `GameRenderer.kt`: `drawDriver()` puts the character's hip joint exactly on the seat's hip point; `seat()` (player anchor) uses the
  same seat. Bot drivers use the same code, so they sit correctly too.
- Lamborghini: its canopy glass (material 0) is now drawn last and translucent, otherwise the driver is hidden inside the closed
  body. Its steering wheel is on the +x side of the mesh, so the driver sits there.

Tuning: the numbers live in `VehicleModels.SEATS` (Vehicles.kt): x / hipY / z / lean / thigh / knee / arm / fore.
