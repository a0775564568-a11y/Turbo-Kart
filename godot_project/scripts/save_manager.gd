extends Node
class_name SaveManager

const SAVE_PATH = "user://turbo_kart_save.json"

static func save_game_data(data: Dictionary) -> void:
	var file = FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file:
		var json_str = JSON.stringify(data)
		file.store_string(json_str)
		file.close()

static func load_game_data() -> Dictionary:
	if not FileAccess.file_exists(SAVE_PATH):
		return {
			"coins": 1500,
			"unlocked_cars": ["car_01"],
			"selected_car": "car_01"
		}
	var file = FileAccess.open(SAVE_PATH, FileAccess.READ)
	if file:
		var json_str = file.get_as_text()
		file.close()
		var parsed = JSON.parse_string(json_str)
		if parsed is Dictionary:
			return parsed
	return {}
