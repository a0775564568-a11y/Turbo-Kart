extends Node3D

@onready var player_car: CharacterBody3D = $PlayerCar
@onready var speedometer_label: Label = $HUD/Speedometer
@onready var back_btn: Button = $HUD/BackButton

@onready var btn_left: Button = $HUD/Controls/BtnLeft
@onready var btn_right: Button = $HUD/Controls/BtnRight
@onready var btn_gas: Button = $HUD/Controls/BtnGas
@onready var btn_brake: Button = $HUD/Controls/BtnBrake

var left_pressed: bool = false
var right_pressed: bool = false
var gas_pressed: bool = false
var brake_pressed: bool = false

func _ready() -> void:
	if back_btn:
		back_btn.pressed.connect(_on_back_pressed)

	if btn_left:
		btn_left.button_down.connect(func(): left_pressed = true)
		btn_left.button_up.connect(func(): left_pressed = false)
	if btn_right:
		btn_right.button_down.connect(func(): right_pressed = true)
		btn_right.button_up.connect(func(): right_pressed = false)
	if btn_gas:
		btn_gas.button_down.connect(func(): gas_pressed = true)
		btn_gas.button_up.connect(func(): gas_pressed = false)
	if btn_brake:
		btn_brake.button_down.connect(func(): brake_pressed = true)
		btn_brake.button_up.connect(func(): brake_pressed = false)

func _physics_process(_delta: float) -> void:
	if player_car:
		if left_pressed:
			Input.action_press("ui_left")
		elif not Input.is_key_pressed(KEY_LEFT):
			Input.action_release("ui_left")

		if right_pressed:
			Input.action_press("ui_right")
		elif not Input.is_key_pressed(KEY_RIGHT):
			Input.action_release("ui_right")

		if gas_pressed:
			Input.action_press("ui_up")
		elif not Input.is_key_pressed(KEY_UP):
			Input.action_release("ui_up")

		if brake_pressed:
			Input.action_press("ui_down")
		elif not Input.is_key_pressed(KEY_DOWN):
			Input.action_release("ui_down")

		var spd = int(player_car.velocity.length() * 3.6)
		if speedometer_label:
			speedometer_label.text = str(spd) + " קמ\"ש"

func _on_back_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/main_menu.tscn")
