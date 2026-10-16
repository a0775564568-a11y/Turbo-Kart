extends Control

@onready var start_race_btn: Button = $Buttons/StartRace
@onready var championships_btn: Button = $Buttons/Championships
@onready var garage_btn: Button = $Buttons/Garage
@onready var track_select_btn: Button = $Buttons/TrackSelect
@onready var track_editor_btn: Button = $Buttons/TrackEditor
@onready var settings_btn: Button = $Buttons/Settings

@onready var car_pivot: Node3D = $SubViewportContainer/SubViewport/CarPivot

func _ready() -> void:
	if start_race_btn:
		start_race_btn.pressed.connect(_on_start_race_pressed)
	if championships_btn:
		championships_btn.pressed.connect(_on_championships_pressed)
	if garage_btn:
		garage_btn.pressed.connect(_on_garage_pressed)
	if track_select_btn:
		track_select_btn.pressed.connect(_on_track_select_pressed)
	if track_editor_btn:
		track_editor_btn.pressed.connect(_on_track_editor_pressed)
	if settings_btn:
		settings_btn.pressed.connect(_on_settings_pressed)

func _process(delta: float) -> void:
	if car_pivot:
		car_pivot.rotate_y(delta * 0.8)

func _on_start_race_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/race.tscn")

func _on_championships_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/race.tscn")

func _on_garage_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/garage.tscn")

func _on_track_select_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/race.tscn")

func _on_track_editor_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/track_editor.tscn")

func _on_settings_pressed() -> void:
	# Show settings dialog or toggle sound
	pass
