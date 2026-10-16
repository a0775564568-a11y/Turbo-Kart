extends Node
class_name RaceManager

signal countdown_tick(count: int, text: String)
signal race_started
signal race_finished(winner_name: String, time_seconds: float)

@export var total_laps: int = 2
var current_lap: int = 1
var race_time: float = 0.0
var is_racing: bool = false

func start_countdown() -> void:
	emit_signal("countdown_tick", 3, "3")
	await get_tree().create_timer(1.0).timeout
	emit_signal("countdown_tick", 2, "2")
	await get_tree().create_timer(1.0).timeout
	emit_signal("countdown_tick", 1, "1")
	await get_tree().create_timer(1.0).timeout
	emit_signal("countdown_tick", 0, "צא!")
	is_racing = true
	emit_signal("race_started")

func _process(delta: float) -> void:
	if is_racing:
		race_time += delta
