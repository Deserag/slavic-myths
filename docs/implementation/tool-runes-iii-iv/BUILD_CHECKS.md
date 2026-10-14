# Проверки инструментальных рун III–IV

| Gate | Результат | Область доказательства |
|---|---|---|
| ToolRunesOffline | PASS11731 /40 model scenes | Admission,54 ID context,6 faces/9–18,9–25 field, species/radius/2×2/48–96, placed/house/unloaded negatives, bounded reads, native provenance codecs, installed break/use ABI |
| RunesCraftOffline | PASS3518 | Previous crafted IV/native events/codecs/shared rules |
| RuneRework | PASS398 | Existing balance/admission/legacy provenance |
| RareRune | PASS20491 | Existing rare rules |
| Clean production build | PASS | Java21/MC1.21.1/NeoForge21.1.255, BUILD_DELIVERY.log |
| Current source/resources/JAR | PASS21993 | 6774 exact packaged resource files, exact compiled class bytes,65 class version, no fixtures, old bytes/source ID preservation, recipe/component/count/model/lang/catalog/reproducibility |
| Game/client/server/GameTest/PolyMC | NOT_RUN0/0/0/0 | Actual mixin activation, permissions, drop/XP/wear, GUI/JEI, MP/relog/FPS/MSPT are manual |

Команды PowerShell из repo root, JAVA_HOME только для этого command process:

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.12'
./gradlew.bat clean build -I tools/tool-runes-offline.gradle toolRunesOfflineChecks -I tools/runes-craft-offline.gradle runesCraftOfflineChecks -PruneReworkOfflineCheck verifyRuneRework -PrareRuneOfflineCheck verifyRareRunes --console=plain
python tools/verify_tool_runes.py
```

JavaExec fixtures — обычный JVM процесс, не запуск Minecraft/NeoForge loader/server. Native ABI читается из installed class bytes, full dependency source был временным input и не оставлен в deliverables; hashes в api/API_SOURCE_HASHES.json.

Логи: COMPILE.log — исправленный comparator inference FAIL; COMPILE_SECOND.log — PASS; OFFLINE_FIRST.log —11731/40 PASS; BUILD.log и BUILD_FINAL.log — clean+4offline suites PASS; BUILD_DELIVERY.log — final clean после resource-only main-hand wording; RESOURCE.log — финальный PASS. Повторять build/tests после новых Java/runtime изменений, не считать только compile игровой проверкой.

JAR SHA256: `1ade97e48ee8f8a12348b377e22f4f8edcb8f00cf4927894917d3974a95871f7`. Исторические смежные fixtures не переобъявлены результатом этой задачи. Core/server не импортируют client/JEI. PolyMC installation NOT_REQUESTED, profiles/worlds unchanged. [Manual checks](MANUAL_CHECKS.md).
