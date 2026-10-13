# Slavic Myths1.7.4 — результат 2026-10-11

Мод реализован по пользовательскому ZIP и **установлен** в `Slavic-Myths-1.21.1-Testing`. Minecraft1.21.1, NeoForge21.1.255, Java21, mod ID `slavicmyths` сохранены. Призыв доступен в игре через зарегистрированные предметы и прежние Creative Tabs; клиентская игровая приёмка ещё не выполнялась.

## Как призвать

В основной вкладке «Славянские мифы»: **Перо Финиста** (вызов/отзыв) и **Печать чёрного знамени** (испытание). Во вкладке «Мобы»: **Яйцо призыва Картауса**. Также сохранены предметы Ильи/Добрыни/Алёши и яйца Идолища/Маринки/Скима.

```mcfunction
/give @s slavicmyths:finist_call_feather
/give @s slavicmyths:kartaus_black_seal
/give @s slavicmyths:kartaus_spawn_egg
```

Перо не расходуется: успешный cooldown40t, отказ8t. Для сюжетного Картауса сначала призвать Финиста и отозвать остальных богатырей; использовать печать на открытом сухом участке Overworld. Проверяются17 точек, boss появляется после bounded intro; блоки не меняются, новая крепость/worldgen не добавлены. Во время испытания новый призыв/отзыв/замена запрещены. В обычном мире остаётся общий максимум2 героя. Survival quest/craft для новых tools не выдуман: доступны через Creative/admin до существующего будущего progression. Яйцо Картауса — свободный debug spawn без сюжетной победы.

## Что реализовано

Финист170HP/armor12/2.25b: слабый straight sword5raw, mark владельцу+15% direct damage, scout/dive/intercept с отдельным соколом, Inspiration, короткий collision-checked winged dash и personal morph максимум36t/600t cooldown/один на фазу. Только8t initial evasive frames; AOE не отменяется. Last Stand на25.5HP сохраняет занятый слот и однократный lethal budget; безопасный отход/настойка либо адресный dormant snapshot, без бесплатного heal через recall.

Картаус650HP/2.70b: commander staff/wave/chains/locked eye, до2 damageable banners45HP и до3 tagged vanilla guards, curse anchor12HP/240t timeout/180t rescue immunity, фазовые protection/stagger/rage. Нет teleport/illusion clones. Damage reduction10%+15%/banner максимум40%; last banner даёт punish window и160t запрет recast. Все helper UUID привязаны к event; конец/abort/restart очищают доступных помощников, поздно загруженные tagged entities отбрасываются. Story victory и deferred1.7.6 eligibility записываются владельцу, уникальных наград/достижений не добавлено.

Модели по `APPROVED_Finist_Character_Animations.png` и `APPROVED_Kartaus_Character_Animations.png`: Finist20 bones/116 cubes, Kartaus21/128, companion9/22. Main atlases256, falcon128, supplied items64. Straight slim steel/gold sword и длинный carved eye staff имеют разные пропорции/силуэты. Companion и larger morph имеют отдельные markings.54 original procedural OGG скопированы побайтно; это эффекты, не человеческая озвучка. RU/EN messages/subtitles, HUD и projected warnings изолированы в client.

Основные новые classes: `finist/FalconActor`, `FalconAction`, `FinistContent`, `FinistEntity`, `KartausEntity`, `BannerEntity`, `KartausEncounter`, `FalconCallItem`, `FinistEvents`; `client/BrightFalconClient`, `FinistModel` и5 mesh classes. Старые gameplay classes не перебалансированы: интеграция только в `SlavicMyths`, `HeroCalls`, `StoryPolicy`, версия/additive lang/sounds. Прежний `client/FinistClient` для лука сохранён. Генератор `tools/finist_assets.py`; opt-in `finist-runtime`, `finist-tests`, `verify_finist.py`, `install_finist.py` не входят в production JAR.

## Фактические проверки

- `gradlew.bat clean build --console=plain`: **PASS** (последний62s); стандартный `test NO-SOURCE` не выдаётся за gameplay test.
- `gradlew.bat -I tools/finist-runtime.init.gradle runFinistChecks -PfinistRun=final-04 -PwithoutJei --console=plain`: **1 последовательный GameTest /75 assertions PASS**. Реальные `/give`, Creative tab membership всех9 новых/прежних items, повторный вызов, max2, item count, lava refusal, delayed spawn, locks/overlap/dimension isolation, banner/guards/anchor, фактический mark/Guard/Last Stand, recall90HP, реальные AI eye21→42 и traveling wave, victory/debug/abort/reset доказаны. Это не75 разных игровых тестов.
- Measured Normal,24HP/full unenchanted diamond: melee6HP, wave6.5HP, eye7HP, chains initial2HP. ResistanceI снижает eye до5.6HP. Accessories/Protection отдельно не проверялись.
- `gradlew.bat -I tools/finist-offline.gradle finistOfflineChecks --console=plain`: **126918 native checks /26 profiles PASS**; finite poses/UV/height. Offline views — не Minecraft screenshots.
- `python tools/verify_finist.py`: **36387 resource/production checks PASS**.54 OGG SHA/probe/full decode; PNG/JSON/UV/locales; Java21, common/client separation, отсутствие test leakage. Повтор generator0 изменений; 7331 старых JAR entries идентичны, только8 разрешённых старых entries изменены.

Реальных GameTest server запусков **6**, включая исправленные неуспешные итерации; клиентов **0**. Исправлены текущий CollisionContext API, task-name fixture и инициализация cooldown/реальной AI cadence в тесте. Последний запуск успешен. Старые полные1.7.1–1.7.3 боевые suites не повторялись; ограниченный shared summon regression и сохранность прежнего JAR проверены.

[51-кейсовая матрица](verification/finist-1.7.4/TEST_MATRIX.md): полный клиентский render/first-person/perch/morph/low particles/audio, длительный бой всех фаз/DOT/полетов, реальные2 клиента/disconnect/порталы/relog, новый disk restart двумя процессами и measured MSPT **NOT_RUN**. Новый NBT/SavedData roundtrip не объявляется реальным перезаходом. Arena sampling не гарантирует отсутствие всех промежуточных препятствий; полноценная маршрутная приёмка остаётся ручной.

## Артефакт и установка

JAR: `D:/slavic-myths/build/libs/slavicmyths-1.7.4.jar`; release: `D:/slavic-myths/release/slavicmyths-1.7.4.jar`.

PolyMC: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.minecraft\mods\slavicmyths-1.7.4.jar`.

SHA256 всех трёх: `d5539688425f79b1a9d93e1cf11d21f14ebc3827d030999e52b046198958f4fc`.

Backup1.7.3: `C:\Users\pavel\AppData\Roaming\PolyMC\instances\Slavic-Myths-1.21.1-Testing\.slavicmyths-backups\pre-finist-20261011-204051\slavicmyths-1.7.3.jar`. SHA2566496e20b1d1dfc39ae3c05eead9e45126991c0ef10db33ad158676cb601133e6. Остальные261 файлов экземпляра сохранены; старый1.16.5 и пользовательские миры не перезаписывались. [Installation receipt](verification/finist-1.7.4/polymc-installation.json). Клиент не запускался, commit не создавался; большой исходный пользовательский dirty tree сохранён.
