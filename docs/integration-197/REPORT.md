# Итоговая тестовая интеграция 1.9.7 + патч оружия

Исходный commit **61d98910a1654743f75ef521167af9d225af44f7** опубликован в test/integrated-197-20261014. JAR построен из этого воспроизводимого набора. Последующие изменения отчёта относятся только к docs/. Полный состав/границы snapshots — [INVENTORY.md](INVENTORY.md), автоматические gates — [gates.json](gates.json), результаты — [summary.json](summary.json).

## Коса и клиент

Коса повторяет образ одобренного оружия Карачуна: опускающаяся дуга19.3/древко34, тёмный металл и purple seam,5 ледяных зубцов, костяные кольца и две подвески. Общие wood/metal/bone/ice палитры берутся из генератора босса; сама модель босса не менялась оружейным патчем. Отдельная закрытая OBJ,8 текстур32px и32px иконка. Правый/левый first-person хват проверены и сведены к древку; движения руки и оружия используют один плавный motion. Активная двуручная third-person поза работает через исходный PlayerModel, поэтому PlayerRenderer/броня/аксессуары не заменяются. Vanilla swing packet удалена, собственный серверный Pose сохранён.12 HP и passives/abilities сохранены. Нули HUD — время готовности способностей, не урон.

Всего в этом задании **7 реальных dev-client запусков**:5 оружейных +2 интеграционных. Первый интеграционный запуск выявил ошибку smoke fixture: Creative screen закрывался через setScreen(null), без onClose; обновление armor slots игнорировалось ванильным клиентом. Исправлен только fixture; повторный запуск дал настоящую экипированную броню и26 framebuffer PNG. Не выдаём прежние голые кадры за проверенную броню. Сценарии: boss/first/third/attack/after/left/FOV70/90/110/GUI/Steve WIDE/Alex SLIM/Breath/Harvest/walk/run/jump/crouch/left third-person/unique armor attack/perunite/3 других held weapons/последние rune items. Steve/Alex — scripted RemotePlayer actors, не настоящая multiplayer-сессия. Основной игрок отправляет реальный Action packet. Это контролируемая копия мира, не авторский survival тест.

Финальные кадры из Minecraft: [босс](../weapon-stage-a/native-client/boss-approved-scythe.png), [первое лицо](../weapon-stage-a/native-client/first-person-idle.png), [третье лицо](../weapon-stage-a/native-client/third-person-idle.png), [атака](../weapon-stage-a/native-client/third-person-attack.png), [после атаки](../weapon-stage-a/native-client/third-person-after.png), [броня и коса](../weapon-stage-a/native-client/armor-unique-attack.png), [перунит](../weapon-stage-a/native-client/armor-perunite.png), [последние предметы](../weapon-stage-a/native-client/updated-items.png). Программные images/ остаются помеченными software projection. Все smoke исходники исключены из production.

Missing textures мода и ERROR в финальном клиентском логе:0. Подключены три уже существовавших PNG Морозко/Полоза в **assets/minecraft/atlases/blocks.json**. Это ремонт atlas references без перерисовки. Gambeson/plate layer2 не нужны: обе регистрации CHESTPLATE, inner layer2 относится к leggings. Старые пустые ZIP directory entries и заменённые Reaper classes не являются потерянными игровыми ресурсами.

## Проверки

- Финальная clean build PASS24s; предыдущая чистая интеграционная сборка с offline tasks PASS58s. SHA256 production JAR совпадает в обоих запусках.
- 37 assertions косы на реальном GameTest server;100→88 HP, задержка windup, arc/LOS/range, NPC/pet protection, Strength/Weakness/Sharpness, wither/heal/undead kill, Breath/Harvest projectile collision, cooldown/durability/cancel/expiry.
- 107 assertions creature-feedback на отдельном настоящем сервере. Это два интеграционных server runs; плюс один повтор37 в оружейной ветке — всего3 GameTest серверных запуска в этом задании.
- RuneRework390 / RareRune20491 / RuneFoundation75064; CreatureHealth1545; Karachun40 +10000 reward simulations +8352 native UV vertices; armor8771 /22 native модели. Это offline assertions, не дополнительные игровые запуски.
- Production integration8366 gates; creature-feedback8384; health13509. Java21, NeoForge metadata1.9.7, exact source resource/class bytes, JSON syntax, fixture exclusion, atlas resources, rune preservation, geometry/UV и детерминированная генерация косы.
- Исторический verify_runes_patch --jar **FAIL** на старом byte-baseline:5 unchanged model CRLF/LF и5 теперь сознательно обновлённых источников боссов. Нормализованное содержимое моделей совпадает с оригиналами, исходный manifest сохранён. Полный отдельный creature-feedback gate подтвердил все41 rune-owned files, кроме13 документированных исправлений значений ru_ru. Native rune regression tasks PASS. Старый guard не переписан под фиктивный PASS.

Ни production multiplayer/реальный relog, ни измеренный MSPT, ни независимая приёмка не проводились. Визуальные решения остальных оружейных классов не объявляются окончательно принятыми: дальнейший этапB сохранён для отдельного задания.

## PolyMC

**Установлено PASS**, версия1.9.7. Активный JAR:
`C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.9.7.jar`

SHA256:
`26eb2b714527d0573d039457da5f2b474c6c09e286ef059450b56dbfb23b6fc5`

Предыдущий SHA256:
`2108e49ad917af013792b21c43a20ade131bd8d606d40d5ae57eca229b0ab180`

Проверенная резервная копия:
`C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-integrated-197-20261014-101525\slavicmyths-1.9.7.jar`

Перед заменой Minecraft был закрыт, проверены MC1.21.1/NeoForge21.1.255, исходный commit и чистота implementation paths, build/server/client logs, hashes. В mods один активный Slavic Myths JAR.328 других файлов профиля сохранили прежние SHA256, миры/конфиги/Jade/другие моды/старый1.16.5 не менялись. Отображаемое имя уже содержит «Патч оружия — Исправления существ» и оставлено как есть.

**Запуск через PolyMC NOT_RUN**: пользователь остановил Computer Use физическим Escape. Дальнейшие действия через UI и обходной запуск не выполнялись. Графический Windows capture ранее возвращал FrameArrived timeout; доступное accessibility чтение PolyMC не заменяет игровые кадры. Последние26 кадров получены отдельным dev-client, не установленным PolyMC runtime. Настоящие проверки были завершены до установки; установка сама по себе не считается игровой приёмкой.

Откат: закрыть Minecraft, скопировать проверенный backup поверх installed_jar, проверить предыдущий SHA256. Автоматический installer допускает замену только проверенной baseline и candidate, сохранён в tools/install_integrated_197.ps1.

Кратко вручную: запустить Testing → отдельная копия/новый мир → first/third/left и атака/смена оружия с бронёй; реальный survival удар и passives/abilities; новые5 рун и feedback Creative/Peaceful/Полоз/Чудь; runtime logs. Проверка ChatGPT и одобрение автора остаются обязательными.

GitHub: [оружейное исправление](https://github.com/Deserag/slavic-myths/commit/34a827b3ab6a8ef854807cd615ee0eda4d4870cc), [PR№2](https://github.com/Deserag/slavic-myths/pull/2), [исходник установленной интеграции](https://github.com/Deserag/slavic-myths/commit/61d98910a1654743f75ef521167af9d225af44f7). main остался b7616080; PR draft, никаких автоматических merge в main.
