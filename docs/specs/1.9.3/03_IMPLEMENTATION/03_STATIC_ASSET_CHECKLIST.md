# Неигровой asset audit (проверяемый список)

- [ ] Оба structure IDs зарегистрированы; нет дублей; Overworld only.
- [ ] Есть structure templates/NBT/pieces или код placement в текущем API.
- [ ] В материалах нет 1.17 copper, несуществующих новых vanilla blocks и несовместимых JSON.
- [ ] Все новые items/blocks имеют модели, все texture пути существуют, raw PNG правильного размера.
- [ ] `blockstates` определяют ориентацию и включают все нужные свойства.
- [ ] Все `loot_tables` валидные, контейнеры seeded one-time, recipes видимы в survival и ссылаются на правильные IDs.
- [ ] `ru_ru.json`/`en_us.json` содержат все отображаемые имена/tooltip/advancement texts.
- [ ] `sounds.json` ссылается на реальные `.ogg`/существующие ресурсы, звуки не играют глобально без attenuation.
- [ ] Worldgen placement проверяет соседние chunks и поворот всех частей.
- [ ] Запечатанные камеры без доступного сюжетного фрагмента и босса.
- [ ] Частицы привязаны к локальным объектам и не создают постоянного большого tick overhead.
- [ ] Server code не импортирует GUI/client renderer классы.
- [ ] Data sync сохранена при stop/start и unload/reload.
- [ ] Старые registry IDs, NBT и сохранения без миграционного краша.
- [ ] Билд production/reobf JAR завершён успешно либо failure честно задокументирован.
