# Build и проверка — 2026-10-14

| Проверка | Результат | Доказательство / граница |
|---|---|---|
| `gradlew.bat clean build` Java 21 | PASS | `BUILD_FINAL_DELIVERY.log`, production main/client compiled, обычные 100 deprecation warnings |
| `runesCraftOfflineChecks` | PASS, 3518 | Native JSON/NBT/wire tier codecs; old/new RuneState; admission/duplicates/slots; speed/Fortune/caps; series/healing state; native NeoForge DamageContainer Pre/Post with absorption |
| `verifyRuneRework` | PASS, 398 | Existing balance/legacy provenance/duplicate admission/old oath/III |
| `verifyRareRunes` | PASS, 20491 | Existing rare-rune model rules; не реальный бой |
| `verify_runes_craft.py` | PASS, 21793 | Preserved prior production files/translations/III resources, five exact recipes, source hooks, repeatable generator, unique-key JSON, 6749 packaged resources, exact compiled classes, class version 65, no fixtures |
| Production NeoForge JAR | PASS | `build/libs/slavicmyths-1.9.7.3.jar`; SHA256 `1da8fff39146a1070b4340ea1332ab6c54c8736cbcb85ea5b6263e08d77cbdf8` |
| Git diff whitespace | PASS | `DIFF_CHECK.log`, только предупреждения об уже имеющихся newline conversions чужих файлов |
| Minecraft/server/GameTest/PolyMC | NOT_RUN: 0 / 0 / 0 / 0 | Запуски запрещены заданием; GUI/JEI/loot/MP/real relog/FPS/MSPT не приняты |

Повторить в PowerShell из корня проекта (JAVA_HOME только для этого процесса команды):

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat clean build -I tools/runes-craft-offline.gradle runesCraftOfflineChecks -PruneReworkOfflineCheck verifyRuneRework -PrareRuneOfflineCheck verifyRareRunes --console=plain
python tools/verify_runes_craft.py
```

JavaExec здесь — обычные изолированные JVM fixtures, без Minecraft run configuration, server/game process или loader. Production JAR source set не включает эти tests.

История: `COMPILE.log` PASS; `OFFLINE.log` предварительные 2281/398/20491 PASS; `BUILD.log` FAIL (восстановленный существующий blocked hook); `BUILD_FINAL.log` и `BUILD_RELEASE.log` промежуточные clean PASS; `BUILD_VERIFIED.log` PASS после учёта absorption interruption, `BUILD_DELIVERY.log` PASS после уточнения двух подсказок, `BUILD_FINAL_DELIVERY.log` окончательный PASS после приведения смены подходящего оружия к ТЗ. Всего 6 clean-build попыток: 5 PASS, 1 исправленный FAIL. Resource verifier ранние FAIL связаны с реконструкцией CRLF, отсутствующим JAR после failed clean и штатной Gradle metadata expansion; окончательный `RESOURCE_DELIVERY.log` PASS. Старые logs сохранены и не считаются окончательной приёмкой.

Смежные старые fixtures RewardModel/Gorynych/RuneFoundation не запускались и не исправлялись; чужие HP/art не изменены. Проверки сохранения — codec/admission, не загрузка мира. Списки ручных сценариев: [MANUAL_CHECKS.md](MANUAL_CHECKS.md).
