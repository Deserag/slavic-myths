# Boss reimplementation: команды для отдельного тестового мира

Клиент этим заданием автоматически не запускается. Следующие команды выполняются
вручную в новом тестовом мире, с разрешёнными командами.

```mcfunction
/give @s slavicmyths:nightingale_spawn_egg
/give @s slavicmyths:nightingale_dagger
/give @s slavicmyths:nightingale_whistle
/give @s slavicmyths:tugarin_sword
/summon slavicmyths:tugarin_zmey ~ ~ ~8
/summon slavicmyths:nightingale ~ ~ ~8
/give @s minecraft:diamond_helmet
/give @s minecraft:diamond_chestplate
/give @s minecraft:diamond_leggings
/give @s minecraft:diamond_boots
```

Призывайте боссов по одному. Для проверки перехода фаз:

```mcfunction
/data merge entity @e[type=slavicmyths:tugarin_zmey,sort=nearest,limit=1] {Health:175.0f}
/data merge entity @e[type=slavicmyths:nightingale,sort=nearest,limit=1] {Health:125.0f}
```

Для отдельной проверки поселения, вне зоны другого босса:

```mcfunction
/summon minecraft:villager ~ ~ ~4
/summon minecraft:wandering_trader ~ ~ ~5
/summon minecraft:iron_golem ~3 ~ ~4
```

Четыре основные summon/give команды проходят через настоящий серверный command
dispatcher в GameTest, включая положительный result callback. Изменение Health
через команды и внешний вид перехода проверяются вручную. Native NBT roundtrip
в тестах проверяет сериализацию; настоящий выход и повторный вход требует клиента.
