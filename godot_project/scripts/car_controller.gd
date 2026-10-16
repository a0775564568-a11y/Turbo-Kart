extends CharacterBody3D
class_name CarController

@export var max_speed: float = 38.0
@export var acceleration: float = 24.0
@export var braking: float = 35.0
@export var turn_speed: float = 2.6
@export var drift_multiplier: float = 1.4
@export var gravity: float = 28.0

var forward_speed: float = 0.0
var steer_angle: float = 0.0
var is_drifting: bool = false
var drift_charge: float = 0.0
var turbo_time: float = 0.0
var shield_time: float = 0.0
var ghost_time: float = 0.0
var is_airborne: bool = false
var in_air_roll: float = 0.0
var stunt_count: int = 0

@onready var camera_mount: Node3D = $CameraMount
@onready var wheel_fl: Node3D = $Wheels/FL
@onready var wheel_fr: Node3D = $Wheels/FR
@onready var wheel_rl: Node3D = $Wheels/RL
@onready var wheel_rr: Node3D = $Wheels/RR
@onready var turbo_particles: GPUParticles3D = $TurboParticles

signal stunt_completed(toast_text: String, turbo_boost: float)

func _physics_process(dt: float) -> void:
	# Power-up timers
	if turbo_time > 0.0:
		turbo_time -= dt
	if shield_time > 0.0:
		shield_time -= dt
	if ghost_time > 0.0:
		ghost_time -= dt

	var effective_max_speed = max_speed * (1.45 if turbo_time > 0.0 else 1.0)
	var effective_accel = acceleration * (2.0 if turbo_time > 0.0 else 1.0)

	# Input handling
	var steer_input = Input.get_action_strength("ui_right") - Input.get_action_strength("ui_left")
	var throttle_input = Input.get_action_strength("ui_up") - Input.get_action_strength("ui_down")
	var drift_input = Input.is_action_pressed("drift")

	# Airborne & Gravity
	if not is_on_floor():
		is_airborne = true
		velocity.y -= gravity * dt
		# Stunt rotation in air
		if abs(steer_input) > 0.2:
			rotate_object_local(Vector3.FORWARD, steer_input * 6.0 * dt)
			in_air_roll += abs(steer_input * 6.0 * dt)
			if in_air_roll >= TAU:
				stunt_count += 1
				in_air_roll = 0.0
	else:
		if is_airborne:
			# Landed
			is_airborne = false
			rotation.z = 0.0
			if stunt_count > 0:
				var toast = "נחיתה מושלמת! +טורבו"
				if stunt_count >= 2:
					toast = "סופר פעלול אגדי! +מגה טורבו"
				turbo_time = float(stunt_count) * 1.5
				emit_signal("stunt_completed", toast, turbo_time)
			stunt_count = 0
			in_air_roll = 0.0

	# Drifting
	if drift_input and not is_airborne and abs(steer_input) > 0.2 and forward_speed > 15.0:
		is_drifting = true
		drift_charge += dt * 1.5
	else:
		if is_drifting:
			if drift_charge > 1.5:
				turbo_time = 2.0 # Mini-turbo release!
			is_drifting = false
			drift_charge = 0.0

	# Acceleration & Speed
	if throttle_input > 0.0:
		forward_speed = move_toward(forward_speed, effective_max_speed, effective_accel * dt)
	elif throttle_input < 0.0:
		forward_speed = move_toward(forward_speed, -effective_max_speed * 0.35, braking * dt)
	else:
		forward_speed = move_toward(forward_speed, 0.0, braking * 0.4 * dt)

	# Steering
	var actual_turn = steer_input * turn_speed * (drift_multiplier if is_drifting else 1.0)
	if abs(forward_speed) > 1.0:
		rotate_y(-actual_turn * dt * sign(forward_speed))

	# Apply motion
	var forward_dir = -global_transform.basis.z
	velocity.x = forward_dir.x * forward_speed
	velocity.z = forward_dir.z * forward_speed
	move_and_slide()

	# Wheel animations
	if wheel_fl and wheel_fr:
		wheel_fl.rotation.y = -actual_turn * 0.3
		wheel_fr.rotation.y = -actual_turn * 0.3
		wheel_fl.rotate_x(forward_speed * dt * 2.0)
		wheel_fr.rotate_x(forward_speed * dt * 2.0)
	if wheel_rl and wheel_rr:
		wheel_rl.rotate_x(forward_speed * dt * 2.0)
		wheel_rr.rotate_x(forward_speed * dt * 2.0)

	# Smooth camera follow
	if camera_mount:
		camera_mount.global_position = camera_mount.global_position.lerp(global_position + Vector3(0, 2.2, 0), dt * 10.0)
