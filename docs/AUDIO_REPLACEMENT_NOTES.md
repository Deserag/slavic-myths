# Временные звуки и план замены

39 требуемых отдельных событий зарегистрированы и подключены. Старые dotted IDs сохранены.
Источник — существующие оригинальные звуки проекта и vanilla events; новых заимствованных OGG нет.
Столбец «будущий звук / ориентир» описывает желаемую запись, а не измеренную длительность текущего alias.
Фактическое звучание в Minecraft требует ручного прослушивания; клиент автоматически не запускался.

| Event | Текущий источник | Будущий звук / ориентир |
|---|---|---|
| `tugarin_phase1_idle` | `minecraft:entity.piglin_brute.ambient` | низкое дыхание / рычание, 0.8–1.5 с |
| `tugarin_phase1_aggro` | `minecraft:entity.ravager.roar` | грубый рык / резкий человеческий свист, 0.4–0.8 с |
| `tugarin_phase1_attack` | `minecraft:entity.player.attack.strong` | взмах клинка/когтя с подходящей массой, 0.15–0.4 с |
| `tugarin_phase1_sweep` | `minecraft:entity.player.attack.sweep` | взмах клинка/когтя с подходящей массой, 0.15–0.4 с |
| `tugarin_phase1_charge` | `minecraft:entity.ravager.roar` | выраженная подсказка движения, 0.3–0.6 с |
| `tugarin_phase1_hurt` | `minecraft:entity.piglin_brute.hurt` | короткая боль, 0.2–0.5 с |
| `tugarin_phase1_death` | `minecraft:entity.piglin_brute.death` | затихающий человеческий крик или рёв Змея, 1–2 с |
| `tugarin_transform_start` | `minecraft:entity.ravager.roar` | низкий рык/финальный рёв, 0.7–1.4 с |
| `tugarin_transform_body` | `minecraft:block.bone_block.break` | скрип костей и брони, 0.6–1.0 с |
| `tugarin_transform_wings` | `minecraft:entity.ender_dragon.flap` | раскрытие/взмах мембран, 0.4–0.7 с |
| `tugarin_transform_roar` | `minecraft:entity.ender_dragon.growl` | низкий рык/финальный рёв, 0.7–1.4 с |
| `tugarin_phase2_idle` | `minecraft:entity.ender_dragon.growl` | низкое дыхание / рычание, 0.8–1.5 с |
| `tugarin_phase2_attack` | `minecraft:entity.ender_dragon.growl` | взмах клинка/когтя с подходящей массой, 0.15–0.4 с |
| `tugarin_phase2_fire_charge` | `minecraft:item.firecharge.use` | вдох огня, 0.7–1.2 с |
| `tugarin_phase2_fire_release` | `minecraft:entity.blaze.shoot` | струя огня, 1.0–1.5 с |
| `tugarin_phase2_wing` | `minecraft:entity.ender_dragon.flap` | раскрытие/взмах мембран, 0.4–0.7 с |
| `tugarin_phase2_dive` | `minecraft:entity.ender_dragon.growl` | выраженная подсказка движения, 0.3–0.6 с |
| `tugarin_phase2_hurt` | `minecraft:entity.ender_dragon.hurt` | короткая боль, 0.2–0.5 с |
| `tugarin_phase2_death` | `minecraft:entity.ender_dragon.death` | затихающий человеческий крик или рёв Змея, 1–2 с |
| `nightingale_phase1_idle_whistle` | `slavicmyths:nightingale_idle` | тихий человеческий свист, 0.6–1.2 с |
| `nightingale_phase1_aggro` | `slavicmyths:nightingale_notice` | грубый рык / резкий человеческий свист, 0.4–0.8 с |
| `nightingale_phase1_slash` | `slavicmyths:nightingale_melee` | взмах клинка/когтя с подходящей массой, 0.15–0.4 с |
| `nightingale_phase1_whistle_charge` | `slavicmyths:nightingale_inhale` | нарастающее давление воздуха, 0.4–0.8 с |
| `nightingale_phase1_whistle_release` | `slavicmyths:nightingale_whistle` | человеческий свист и удар воздуха, 0.6–1.2 с |
| `nightingale_phase1_leap` | `minecraft:entity.player.attack.sweep` | выраженная подсказка движения, 0.3–0.6 с |
| `nightingale_phase1_hurt` | `slavicmyths:nightingale_hurt` | короткая боль, 0.2–0.5 с |
| `nightingale_phase1_death` | `slavicmyths:nightingale_death` | затихающий человеческий крик или рёв Змея, 1–2 с |
| `nightingale_phase2_laugh` | `slavicmyths:nightingale_voice` | короткий гротескный человеческий смех, 0.4–0.8 с |
| `nightingale_phase2_slash` | `minecraft:entity.player.attack.sweep` | взмах клинка/когтя с подходящей массой, 0.15–0.4 с |
| `nightingale_phase2_whirlwind` | `slavicmyths:nightingale_destructive` | человеческий свист и удар воздуха, 0.6–1.2 с |
| `nightingale_phase2_dash` | `slavicmyths:nightingale_wave` | выраженная подсказка движения, 0.3–0.6 с |
| `nightingale_phase2_wave` | `slavicmyths:nightingale_destructive` | человеческий свист и удар воздуха, 0.6–1.2 с |
| `nightingale_phase2_hurt` | `slavicmyths:nightingale_hurt` | короткая боль, 0.2–0.5 с |
| `nightingale_phase2_death` | `slavicmyths:nightingale_death` | затихающий человеческий крик или рёв Змея, 1–2 с |
| `nightingale_blade_draw` | `minecraft:item.armor.equip_iron` | извлечение металла, 0.2–0.4 с |
| `nightingale_blade_slash` | `minecraft:entity.player.attack.sweep` | взмах клинка/когтя с подходящей массой, 0.15–0.4 с |
| `nightingale_blade_wind_charge` | `minecraft:entity.breeze.charge` | нарастающее давление воздуха, 0.4–0.8 с |
| `nightingale_blade_wind_release` | `minecraft:entity.breeze.shoot` | короткая ветровая дуга, 0.3–0.6 с |
| `nightingale_blade_wind_hit` | `minecraft:entity.breeze.shoot` | удар воздушной волны, 0.1–0.3 с |
