extends Control

@onready var car_pivot: Node3D = $SubViewportContainer/SubViewport/CarPivot
@onready var back_btn: Button = $Header/BackButton
@onready var next_btn: Button = $CarSelect/NextButton
@onready var prev_btn: Button = $CarSelect/PrevButton
@onready var car_name_label: Label = $CarInfo/CarName
@onready var car_tagline_label: Label = $CarInfo/CarTagline

var car_names = [
	"באגי חוף", "טורבו ספורט", "ג'יפ שטח", "מפלצת הגלגלים",
	"מכונית פורמולה", "מכונית מדבר", "מכונית שלג", "מכונית עתידנית",
	"מכונית משטרה", "רכב כיבוי אש", "מכונית זהב", "מכונית קלאסית",
	"מכונית מרוץ אדומה", "מכונית שטח משוריינת", "מכונית סופר־טורבו",
	"מכונית חלל", "מכונית ג'ונגל", "מכונית מרוצים כחולה",
	"מכונית אליפות", "מכונית טורבו אגדית"
]
var car_taglines = [
	"קל, זריז ושולט בחולות", "אירודינמיקה חדה ומהירות שיא",
	"עובר כל מכשול בלי למצמץ", "צמיגי ענק שרומסים כל תחרות",
	"דיוק מושלם של מסלולי אספלט", "נבנתה לסופות אבק ומסלולי סלעים",
	"שליטה חלקה בקרח ובפיתולים", "הנעה אנרגטית עם קווי ניאון",
	"סירנות מייללות ומרדף ללא פשרות", "טורבו רב-עוצמה שמכבה את התחרות",
	"מצופה זהב טהור וברק מנקר עיניים", "אלגנטיות נוסטלגית עם מנוע משופר",
	"כוכבת מסלול קלאסית שכולם מזהים", "בלתי ניתנת להסטה או התנגשות",
	"הזרקת נייטרו כפולה בכל לחיצה", "טכנולוגיית אנטי-גרביטציה ומהירות קוסמית",
	"חיית מסלול טורפת עם אחיזה מושלמת", "ברק כחול שחוצה את קו הסיום ראשון",
	"נבחרת האלופים הרשמית", "הקארט העוצמתי ביותר ביקום"
]

var current_car_idx: int = 0

func _ready() -> void:
	if back_btn:
		back_btn.pressed.connect(_on_back_pressed)
	if next_btn:
		next_btn.pressed.connect(_on_next_car)
	if prev_btn:
		prev_btn.pressed.connect(_on_prev_car)
	_update_car_display()

func _process(delta: float) -> void:
	if car_pivot:
		car_pivot.rotate_y(delta * 0.7)

func _update_car_display() -> void:
	if car_name_label:
		car_name_label.text = car_names[current_car_idx]
	if car_tagline_label:
		car_tagline_label.text = car_taglines[current_car_idx]

func _on_next_car() -> void:
	current_car_idx = (current_car_idx + 1) % car_names.size()
	_update_car_display()

func _on_prev_car() -> void:
	current_car_idx = (current_car_idx - 1 + car_names.size()) % car_names.size()
	_update_car_display()

func _on_back_pressed() -> void:
	get_tree().change_scene_to_file("res://scenes/main_menu.tscn")
