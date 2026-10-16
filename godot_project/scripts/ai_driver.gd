extends CharacterBody3D
class_name AIDriver

@export var waypoints: Array[Node3D] = []
@export var max_speed: float = 34.0
@export var acceleration: float = 20.0
@export var turn_speed: float = 2.4

var current_waypoint_idx: int = 0
var forward_speed: float = 0.0

func _physics_process(dt: float) -> void:
	if waypoints.is_empty():
		return

	var target = waypoints[current_waypoint_idx]
	var dir_to_target = (target.global_position - global_position)
	dir_to_target.y = 0.0

	if dir_to_target.length() < 6.0:
		current_waypoint_idx = (current_waypoint_idx + 1) % waypoints.size
		target = waypoints[current_waypoint_idx]
		dir_to_target = (target.global_position - global_position)
		dir_to_target.y = 0.0

	var desired_rotation = atan2(-dir_to_target.x, -dir_to_target.z)
	rotation.y = lerp_angle(rotation.y, desired_rotation, turn_speed * dt)

	forward_speed = move_toward(forward_speed, max_speed, acceleration * dt)
	var forward_dir = -global_transform.basis.z
	velocity.x = forward_dir.x * forward_speed
	velocity.z = forward_dir.z * forward_speed
	move_and_slide()
