# SOUND_PRODUCTION_NOTES

1.3.4.5.2, первый атомарный этап общего Boss Rework. Стабильные keys зарегистрированы в ModSounds, source aliases действуют через sounds.json. Существующие авторские OGG Соловья и vanilla events сохранены; новых аудиофайлов нет. Phase-two Nightingale keys подготовлены по прямому требованию ТЗ, новая фаза пока не реализована.

**REQUIRES_MANUAL_CLIENT_CHECK**: тембр, громкость, attenuation, различимость telegraph и release. Alias не подтверждает качество звучания. Для окончательного production audio нужны собственные/свободные low heavy breathing/taunt/metal whoosh Тугарина, serpent hiss/fire inhale/dive и человеческий свист/air-pressure/laugh Соловья. Не использовать bow/bird/goat/sheep записи. Idle Тугарина раз в400тиков (20с), не каждый tick.

| Stable key | Текущий source event |
|---|---|
| `tugarin.phase1.idle_breath` | `minecraft:entity.piglin_brute.ambient` |
| `tugarin.phase1.aggro` | `minecraft:entity.ravager.roar` |
| `tugarin.phase1.step` | `minecraft:entity.iron_golem.step` |
| `tugarin.phase1.blade_draw` | `minecraft:item.armor.equip_iron` |
| `tugarin.phase1.slash` | `minecraft:entity.player.attack.strong` |
| `tugarin.phase1.sweep` | `minecraft:entity.player.attack.sweep` |
| `tugarin.phase1.charge` | `minecraft:entity.ravager.roar` |
| `tugarin.phase1.hurt` | `minecraft:entity.piglin_brute.hurt` |
| `tugarin.phase1.death` | `minecraft:entity.piglin_brute.death` |
| `tugarin.transform.start` | `minecraft:entity.ravager.roar` |
| `tugarin.transform.crack` | `minecraft:block.bone_block.break` |
| `tugarin.transform.wings` | `minecraft:entity.ender_dragon.flap` |
| `tugarin.transform.burst` | `minecraft:entity.ender_dragon.growl` |
| `tugarin.phase2.idle` | `minecraft:entity.ender_dragon.growl` |
| `tugarin.phase2.fire_charge` | `minecraft:item.firecharge.use` |
| `tugarin.phase2.fire_release` | `minecraft:entity.blaze.shoot` |
| `tugarin.phase2.wing` | `minecraft:entity.ender_dragon.flap` |
| `tugarin.phase2.dive` | `minecraft:entity.ender_dragon.growl` |
| `tugarin.phase2.lunge` | `minecraft:entity.ender_dragon.growl` |
| `tugarin.phase2.hurt` | `minecraft:entity.ender_dragon.hurt` |
| `tugarin.phase2.death` | `minecraft:entity.ender_dragon.death` |
| `nightingale.phase1.idle_whistle` | `slavicmyths:nightingale_idle` |
| `nightingale.phase1.aggro` | `slavicmyths:nightingale_notice` |
| `nightingale.phase1.whistle_charge` | `slavicmyths:nightingale_inhale` |
| `nightingale.phase1.whistle_release` | `slavicmyths:nightingale_whistle` |
| `nightingale.phase1.slash` | `slavicmyths:nightingale_melee` |
| `nightingale.phase1.leap` | `minecraft:entity.player.attack.sweep` |
| `nightingale.phase1.land` | `minecraft:block.grass.step` |
| `nightingale.phase1.smoke_dash` | `slavicmyths:nightingale_wave` |
| `nightingale.phase1.hurt` | `slavicmyths:nightingale_hurt` |
| `nightingale.phase1.death` | `slavicmyths:nightingale_death` |
| `nightingale.phase2.laugh` | `slavicmyths:nightingale_voice` |
| `nightingale.phase2.whirlwind` | `slavicmyths:nightingale_destructive` |
| `nightingale.phase2.dash` | `slavicmyths:nightingale_wave` |
| `nightingale.phase2.wave` | `slavicmyths:nightingale_destructive` |
| `nightingale.phase2.combo` | `slavicmyths:nightingale_melee` |
| `nightingale.phase2.death` | `slavicmyths:nightingale_death` |
| `nightingale.weapon.wind_slash` | `slavicmyths:nightingale_wave` |
| `tugarin.weapon.sheathe` | `minecraft:item.armor.equip_iron` |
