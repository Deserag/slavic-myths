# Идолище Поганое — покадровые анимации 9-блочного босса

## Общие правила
Время в **server ticks при 20 TPS**; визуал interpolate. У каждого нажатия атаки обязательна читаемая поза подготовки, отдельный контакт, инерция и возвращение. НИКАКОГО instant slam без замаха. Bone pivots: `root, pelvis, belly, chest, left/right_shoulder, left/right_forearm, 4x fingers per hand, head, jaw, thigh, shin, back_rack, trophies`. `jaw` настоящий bone с открыванием 0°→36°→0°. Удар начинается не в тот тик, когда начата анимация; хит только на прописанном серверном кадре.

| Animation | Длина | Keyframes и поведение частей | Gameplay event |
|---|---:|---|---|
| `idle_groan` | 90 loop | t0 тело neutral; t22 belly scale breathing visual +1px; t45 head roll +4°, jaw +5°; t70 trophies sway 6°; t90 neutral | idle growl шанс 1/45 сек |
| `walk_groundshake` | 36 loop | t0 right thigh +14°, left -13°, arm counter; t9 right foot lands, shoulder drop 3°; t18 inverse; t27 left foot lands; t36 start | step t9/t27, dust 2–5 parts, физика boss position sync |
| `turn_heavy` | 18 | t0 head yaw+20; t5 chest yaw+9; t11 pelvis yaw+7; t18 aligned | поворачивается с инерцией, без резкого 180° snap |
| `fist_windup` | 22 | t0 upright; t6 right shoulder x=-35°, torso yaw=-12°; t15 elbow behind +65°, gaze target; t22 fist raised and shoulder locked | большой понятный кулак до атаки, предупр. частицы на земле в секторе |
| `fist_slam` | 17 | t0 fist cocked; t5 torso x=+14°, shoulder +82°; **t8 CONTACT** fist ground/front; t12 rebound; t17 recovery | контакт только t8, после полная пауза 16 ticks |
| `stomp_prepare` | 32 | t0 pelvis rises; t6 right thigh+30°, knee bends+30°; t14 leg lifted 1.4b, arms balance; t25 stomp about to fall; t32 foot ready | 3 концентрических кольца видны уже t6–32, границы на земле |
| `stomp_3_rings` | 22 | t0 нога движется вниз; **t3 impact** ground, pelvis down 4px; t4 near ring visible; t10 mid ring pulses; t16 outer pulse; t22 returns | damage individually near t4, mid t10, outer t16; **каждая цель получает damage только по одному кольцу за каст**, самый близкий подходящий на t момент |
| `grab_windup` | 25 | t0 eye locks target; t7 shoulder right yaw=-22°, left hand braces belly; t15 right elbow forward+38°, fingers wide; t25 ready | игрок видит раскрытую ладонь, подсветка узкого сектора |
| `grab_clamp` | 18 | t0 extend; **t8 check hit**; t9 fingers bend 60° around target AABB, target becomes attached near palm; t18 arm lifts target chest-level | grab status `grabbed`, но без бесконечного stun и без привязки к голове |
| `grab_throw` | 24 | t0 hold at chest; t8 torso yaw=+25°, arm+75°; **t12 release** along forward arc; t17 hand open; t24 recovery | отдельный velocity impulse only once, fall damage capped |
| `feed_summon` | 50 | t0 hunch; t10 left palm touches earth; t20 two pulses; **t29 minions spawn** in marked positions; t50 upright | новая «призыв прислужников» вместо найти случайных мобов |
| `feed_grab` | 24 | t0 picks own marked minion; t8 jaw+15°, t14 palm closes; t24 brings it to mouth | если нет minion, finish without heal |
| `feed_chew` | 52 | t0 jaw+36°, holding target in front; t14 move inside mouth; t18–38 jaw open/close 25° alternating at t20/26/32/38; **t40 gain heal**; t52 swallow | interrupt window t12–t38; interrupt cancels healing, minion is released/dies according to safe handling |
| `poison_breath_windup` | 25 | t0 shoulders draw back; t9 jaw opens 20°, belly inflate 3px; t20 jaw+36°, throat green dim glow; t25 aimed | впереди зелёная translucent cone decal |
| `poison_breath_active` | 60 | t0 jaw+36°, t8 head yaw sweep left -12°, t30 right+12°, t55 0, t60 jaw shuts | cone active t0–60, облако обновляется max 1 per 10t |
| `charge_windup` | 28 | t0 head lowers; t9 knees flex; t16 shoulders forward x=+18°, jaw shout; t28 lock heading | прямолинейная опасная полоса на земле |
| `charge_run` | max 44 | t0 torso forward 23°; running legs ±27° every 10ticks; shoulder=+15°; on wall contact slam with shoulder tilt 30° | hit once per target; wall stun 50 ticks; если нет стены, recovery 15 |
| `rage_transition` | 65 | t0 bends down, t15 fists on ground; t25 jaw wide +40°, t30 long roar; t40 shoulders rise, red eyes fade-in; t65 upright | phase2 one-shot at <=315 HP; cannot be interrupted by ordinary stagger |
| `hurt_light` | 11 | t0 steady; t3 head yaw 5°; t7 recoil torso 3°, t11 neutral | **не** откидывать giant физически от мелкого урона |
| `stunned` | 50 | t0 head forward +25°, jaw half open, arms slack; t18 body rocks; t50 resume | появляется только после wall charge failure / special interrupt |
| `death` | 110 | t0 knees buckle; t25 right knee down, t43 left arm braces; t65 massive torso collapses to right, rack sways; t88 jaw slack and body down; t110 despawn/end | loot **не спаунить**; сделать aftermath dust and battle record |

## Три кольца топота — обязательно визуально
В preparatory frames на земле видны три кольца: **красно-бурое 0–5** (внутреннее, очень опасное), **оранжевое 5–10** (среднее), **приглушённо-жёлтое 10–15** (внешнее). Не делать одинаковые ring materials. Кольца не светят через камеру и не рисуются поверх HUD. На pulse t4, t10, t16 соответствующее кольцо кратко (0.25 сек) поднимает пыль/обломки на высоту 0.5–1.0 блока. ВАЖНО: игрок, переходящий границу между кольцами, не получает многократные независимые попадания от одной атаки.

## Анимация захвата — важнейший отличительный элемент
Тело жертвы физически показано зажатым большой рукой: четыре пальца закрываются постепенно; player controls на 0.9 секунды ограничены, камера остаётся управляемой с ограниченным pitch и не рвётся внутри куба; поднятие до плеча 2.5 блоков по траектории, бросок на дальность 6–9 блоков с мягкой серверной предсказуемостью. Илью можно схватить, но он не должен проваливаться в руке; Илья уникальным grunt/counter triggers сценарий спасения.

## Кадры для QA
Скриншоты f0/f15/f22/f30 кулака; f0/f16/f32/f4/f10/f16 для трёх волн; f0/f8/f18/f12 для grab; f0/f14/f32/f40 для eat; f0/f20/f30/f60 для rage; 4 ракурса death collapse. В каждой проверке хорошо различимы визуальный замах и момент попадания.
