# C/D — проверенный исходный debt, 28.10.2026

Это повторная source сверка, не игровой PASS. Старые отчёты имели устаревшие
утверждения «нет регистрации Чертогов»: сейчас HallsContent и HallsWorldgen
реальноесть, 5species+Posten,6rooms, проверенныйregion route иприроднаяпривязка.
Ихнепересоздавали. C: worldgen/естественнаявстречаемость/oldsave/MP/оригиналы
биомов2.0.2–2.0.4 остаются NOT_RUN/BLOCKED_REFERENCE. Текущие modelchanges #334–336
иmanualPosten отдельно в ../mob332/REPORT.md.

D: реальный current code включает 3natural spawn tables вdata biome,мобов55/55/150HP,
рунуI/ответнуючастьII,850HPboss иhead/loot. **Нет** consumer worldgen burning_islands
вpreset/resolver,terrainfeature/вулкана/природнойарены. Полныйsource search поBIOME
подтверждает толькоIslandsContent+clienttexturepaths. Nativeproof почемуодинclimate
appendнеприемлем: ../burning-islands-205/ocean-architecture-evidence.json и
OCEAN_ARCHITECTURE_BLOCKER.md (исторический5seednativeконтрпример,НЕновыйworldtest).
Не добавлен небезопасныйclimate-onlyappend. Нуженобщийterrain-aware resolver+mask,
3Dselection+geology+locate+arena, потом5realworldgates. Это НЕ завершено.

РунаII: MagmaRunes.java намереннонепишетблоки, пока нетnaturalprovenance/restore,
claims/overlap/сохранения. Новыепостройки#315невыдуманы. Cross-playercrashreward
atomicityиоригинальныеPNG/аудио D остаютсяблокерами. НепроверенныйDнеприсваиваетсяPASS.
Независимые E/F разрешены master, ихproductionизменениясохраненыотдельно.
