# Файлы патча рун

Production/fixtures/tools: **41** файлов. Чужие исходники стартового HP-патча не включены.

- `src/main/java/org/slavicmyths/rpg/Runes.java`
- `src/main/java/org/slavicmyths/rpg/RuneDefinition.java`
- `src/main/java/org/slavicmyths/rpg/RuneEffects.java`
- `src/main/java/org/slavicmyths/rpg/RuneRework.java`
- `src/main/java/org/slavicmyths/rpg/RpgEvents.java`
- `src/main/java/org/slavicmyths/rpg/RareRuneRuntime.java`
- `src/main/java/org/slavicmyths/rpg/AdvancedRuneRules.java`
- `src/main/java/org/slavicmyths/rpg/AdvancedRunes.java`
- `src/main/java/org/slavicmyths/item/ItemState.java`
- `src/main/java/org/slavicmyths/morozko/FrostCold.java`
- `src/main/java/org/slavicmyths/morozko/FrostEvents.java`
- `src/main/java/org/slavicmyths/rewards/RewardEvents.java`
- `src/main/java/org/slavicmyths/industry/IndustryAccessories.java`
- `src/main/java/org/slavicmyths/yaga/YagaServices.java`
- `src/main/java/org/slavicmyths/yaga/YagaMenu.java`
- `src/main/java/org/slavicmyths/client/YagaScreen.java`
- `src/main/java/org/slavicmyths/equipment/mixin/WardBlockMixin.java`
- `src/main/java/org/slavicmyths/poloz/PolozRewardEvents.java`
- `src/main/java/org/slavicmyths/gorynych/GorynychRewards.java`
- `src/main/resources/assets/slavicmyths/lang/ru_ru.json`
- `src/main/resources/assets/slavicmyths/lang/en_us.json`
- `src/main/resources/data/slavicmyths/tags/entity_type/rune_death_immune.json`
- `src/main/resources/assets/slavicmyths/models/item/rune_counterstrike_iii.json`
- `src/main/resources/assets/slavicmyths/textures/item/rune_counterstrike_iii.png`
- `src/main/resources/data/slavicmyths/recipe/rune_rework_counterstrike_iii.json`
- `src/main/resources/assets/slavicmyths/models/item/rune_hunter_iii.json`
- `src/main/resources/assets/slavicmyths/textures/item/rune_hunter_iii.png`
- `src/main/resources/data/slavicmyths/recipe/rune_rework_hunter_iii.json`
- `src/main/resources/assets/slavicmyths/models/item/rune_last_stand_iii.json`
- `src/main/resources/assets/slavicmyths/textures/item/rune_last_stand_iii.png`
- `src/main/resources/data/slavicmyths/recipe/rune_rework_last_stand_iii.json`
- `src/main/resources/assets/slavicmyths/models/item/rune_depths_iii.json`
- `src/main/resources/assets/slavicmyths/textures/item/rune_depths_iii.png`
- `src/main/resources/data/slavicmyths/recipe/rune_rework_depths_iii.json`
- `src/main/resources/assets/slavicmyths/models/item/rune_veles_oath_iv.json`
- `src/main/resources/assets/slavicmyths/textures/item/rune_veles_oath_iv.png`
- `tools/rune-rework-tests/org/slavicmyths/verify/RuneReworkOffline.java`
- `tools/rare-rune-tests/org/slavicmyths/verify/RareRuneOffline.java`
- `tools/runes_patch_assets.py`
- `tools/verify_runes_patch.py`
- `tools/runes_patch_report.py`

Общие документы дополнены только текущим итогом: `docs/PROJECT_STATUS.md`, `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`. Полный diff этих трёх файлов содержит и чужие незакоммиченные записи, поэтому целиком в index они не добавляются.

Собственные отчёты: REPORT.md, FILES.md, REGRESSION_LIMITATIONS.md, MANUAL_CHECKS.md, SOURCE_CHECKS.json, RESOURCE_JAR_CHECKS.json. Evidence: BUILD/COMPILE/OFFLINE/REGRESSION/RELATED_REGRESSION/REMAINING_REGRESSION logs, предыдущие build logs, BASELINE_HASHES.json, START_STATUS.txt, ICONS.png, spec/*.md.
