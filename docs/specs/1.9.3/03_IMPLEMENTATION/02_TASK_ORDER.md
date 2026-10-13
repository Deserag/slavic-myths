# Порядок разработки и промежуточные gates

**A. Read-only discover:** worldgen pattern + 1.8 local corruption + loot recipe mechanics + path PolyMC. Записать короткую таблицу реальных точек внедрения.

**B. Fire structure:** создать terrain-safe layout, украсить главный силуэт, вход, огонь, дверь, side caches. Проверить шаблоны/ротацию. Затем интеракция огня.

**C. Spring structure:** surface entrance + descent + cave hall + hydraulic scheme (stable water), ritual pools + visible purity/corruption, blocked chamber and cache. Проверить генерацию в нескольких типах рельефа при кодовых unit/helpers тестах.

**D. Loot/items:** добавить девять типов предметов (из них ancient rune common для обеих), рецепты, loot tables, русские подсказки, избегать несуществующих материалов, optional per-player ceremony claim.

**E. Lore/sounds/effects:** местные анимации, зарегистрированные sound events, Book of Tales, advancements.

**F. Integration:** dedicated server classload, old world, chunk/load/saves, no duplicate chests, two players same action, generation spacing/no village damage.

**G. Build + release:** compile/test/reobf production JAR, update checklist/README по выполненному. Копия в известный PolyMC mods folder без запуска игры и без удаления чужих модов.

**Work discipline:** при исчерпании лимита завершить текущую цельную часть (BUILD PASS), заполнить `CONTINUE` без выдуманных успехов, не начинать новые эпики и не объявлять 1.9.3 готовой.
