# Илья Муромец — анимации, ключевые кадры и VFX

## Общий формат
20 TPS, 1 tick = 50 ms. При interpolated render движение сглажено, игровая фиксация удара строго по server tick. Координаты bone-rotation ниже — градусы `pitch/yaw/roll` относительно default pose, знак и pivot адаптируются к экспорту Blockbench. Все клипы с физическим поворотом реальных рук/ног/щита, а не замена single sprite. Восемь видов по итоговому контенту — `idle`, `walk`, `run`, `taunt`, `shield`, `mace`, `shockwave`, `unyielding`, `retreat`, `drink`, `return`, `hurt`.

| Clip | Длина | Keyframes (тики от начала, пример rotation) | Серверное событие / зрительная норма |
|---|---:|---|---|
| `idle_breath` | 60 loop | t0 body x=0°, head=0°; t20 body x=1.2°; t40 x=-0.8°; t60 0° | ровное дыхание, щит у бедра, палица не машет |
| `walk_heavy` | 22 loop | t0 ноги ±19° и руки ∓8°; t11 противоположное; t22 0 | тяжёлая поступь, step sounds t5/t16, без скольжения |
| `run_to_intercept` | 16 loop | t0 torso x=9°; t4 right leg=+24°; t12 left leg=+24° | бег для перехвата удара, шаги t4/t12 |
| `taunt_bogatyr` | 28 | t0 исходная; t6 right arm pitch=-35°, body yaw=8°; t12 mace raised=-70°, head up=-15°; t15 **голосовой вызов и агро начинает действовать**; t22 arm=-20°, t28 idle | излучение коротких красно-золотых лучей из груди, круг агро 12 блоков; НЕ использовать магическое кольцо-нимб |
| `shield_raise` | 12 | t0 low shield; t4 left arm pitch=-40° yaw=-20°; t8 shield 65° toward incoming source; t12 hold | слышен скрежет железа, shield front перекрывает голову игрока с нужной стороны |
| `shield_hold` | 14 loop | t0 torso x=8°, feet apart ±7°, shield facing source; t7 shield recoil x=7°; t14 reset | при фактическом ударе `impact` добавляет 4-tick shake; удержание max 3.2 сек |
| `shield_lower_recover` | 10 | t0 brace; t6 shield low; t10 idle | cooldown открывает окно уязвимости |
| `protector_dash` | 14 | t0 body 0; t3 body x=15°, t7 left arm forward; t10 shield face attacker; t14 brace | блокирует один удар игроку, range defined by mechanics, dash max 4 blocks |
| `mace_attack` | 31 | t0 idle; t7 right arm x=-98°, body yaw=-20°; t14 x=-120°; t19 x=+42°, torso yaw=+26°, **HIT t19**; t23 follow-through x=80°; t31 idle | arc 120°, metal impact + small dirt puffs, one damage event per enemy |
| `ground_shockwave` | 46 | t0 raise; t14 mace overhead right arm=-155°; t24 both legs bend 12°; t29 mace touches ground **HIT t29**; t30 dust circle; t46 rise | wave radius 4.5, 25 dust particles medium; no real block break |
| `unyielding_activate` | 30 | t0 stumble 15°; t8 shield slam, t12 head up; t16 **automatic taunt**; t20 body straight + golden sparks; t30 brace | короткая вспышка без полного белого экрана; auto taunt не тратит отдельный taunt cooldown, но соблюдает boss resistance rules |
| `hurt_small` | 12 | t0 body yaw=0; t3 head x=8°, torso x=10°; t12 reset | не отменяет shield hold, если фактический урон заблокирован |
| `retreat` | 28 + travel | t0 drop torso=10°; t7 shield raise at 50°; t14 step backward 1 block; t28 run while covering | здоровье <=15%, последняя оборонительная поза, затем следование к безопасной точке |
| `drink_decoction` | 54 | t0 takes flask from belt; t9 right hand moves to mouth x=-130°; t14–38 tilt flask 60° with 3 gulps; t45 flask down; t54 belts | бутылка из сумки **анимируемая**, но не новый player item/reward; green particles very subtle t28,34 |
| `recover_breath` | 70 loop | t0 sits/one knee; t20 chest+2°, head down=12°; t45 head+6°, t70 reset | в безопасной зоне вне радиуса boss aggro, low HP vfx без постоянных зелёных искр |
| `return_ready` | 35 | t0 rises; t12 shield lifts; t22 mace shoulder; t35 follow | 1 return maximum per encounter, UI «Илья вернулся» |

## Про тайминги нанесения урона
Удар палицей даёт hit только один раз в t19; shockwave в t29. При падении TPS ниже 20 server ticks сохраняются; никогда не делать `damage()` из `render`. Во время shockwave `taunt` не кастуется повторно, а во время питья Илья невидим только для выбора целей, но визуально продолжает сидеть в безопасной точке.

## Обратная связь и камера
- Для сильного блока 0.12–0.20 сек небольшие металл-искры в месте столкновения, shield recoil 8°.
- Taunt активен — над Ильёй краткая полоса/иконка внимания на 0.8 сек + дорожка маленьких частиц к NPC; *не сохранять огромную мигающую метку на экране*.
- Взмах палицы виден по плечу и траектории, не телепортировать головку палицы мгновенно из behind в front.
- Retreat не должен приводить к T-pose/пропаданию armature/оружия.

## Приёмочные кадры
Скриншоты/видео: кадры 0/12/19/31 для палицы, 0/8/12/impact для щита, 0/15/28 для вызова, кадры 0/14/28/54 для отвара. В каждом кадре видна именно игровая блочная модель, не только внешний концепт.
