extends Node
class_name PowerUpManager

enum Type { TURBO, SHIELD, ICE, TORNADO, LIGHTNING, MAGNET, GHOST, PAINT_MISSILE, MEGA_JUMP, DOUBLE_NITRO }

func get_random_powerup() -> Type:
	return Type.values()[randi() % Type.size()]

func apply_powerup(racer: CarController, powerup: Type) -> void:
	match powerup:
		Type.TURBO:
			racer.turbo_time = 3.0
		Type.DOUBLE_NITRO:
			racer.turbo_time = 5.0
		Type.SHIELD:
			racer.shield_time = 5.0
		Type.GHOST:
			racer.ghost_time = 4.5
		Type.MEGA_JUMP:
			racer.velocity.y = 15.0
			racer.is_airborne = true
		_:
			racer.turbo_time = 2.5
