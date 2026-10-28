# #328 — интеграция типов, IN_PROGRESS / OWNER_REVIEW_PROPOSED

20 реальных source IDs сверены с NavyaContent/CinderContent/HallsContent/IslandsContent
и PostenMob. Шептун — один ID, четыре формы. Никаких UNCONFIRMED в новых строках.
Историческая CREATURES1.9.8.1 не изменена: SHA в inventory.json. Новая CREATURES_2.0
объединяет historical rows + literal source registrations + проверенные dynamic-family
registrations; это source inventory, **не запущенный registry**. Исторические unmatched
строки остаются явно HISTORICAL_ONLY_NOT_MATCHED, не выдаются за проверенные.

| REQUIREMENT | EVIDENCE | RESULT |
|---|---|---|
| 20 source IDs,1nature/ID,0fire+ice конфликтов | NEW_NETHER_ENTITIES.tsv, inventory.json, tag JSON | PASS source |
| КорневикPLANT+EARTH,ПекельникELEMENTAL+FIRE,ЭховикMYTHICAL_BEAST безEARTH,ГрибошмыгPLANT | explicit FIX вnether328_inventory.py | IMPLEMENTED; OWNER_REVIEW_PROPOSED |
| Physiological effects | production CreatureProperties functions по20профилям | native-physiology.txt; actualeffects/tagbinding/MP NOT_RUN |
| Нейтральность/питомцы | CombatTargeting.intent explicit species roles ДО Enemy/tag fallback | PASS source; NPC/pet/PvP runtime NOT_RUN |
| HP/фазы/уникальный лут | HP/damage handlers не менялись | PASS source preservation; save/MP NOT_RUN |
| LevelIV/V/VI иA1–A7 | TARGET_COMBAT_PROFILE.tsv рассчитан из14/18/20×.65/.8/.7/1/1.15/1.35/1.5 | PROPOSED; НЕ ВНЕДРЕНО |
| ЭховикIII | формула не придумана, действующий бюджет сохранён | PASS scope |
| Per-action burst/DoT budget | ATTACKS_2.0 содержит actual hurt callsites/source expressions; агрегация поentity/action иполныйtarget budget НЕ ЗАВЕРШЕНЫ | WIP |
| Actual registry/effects/save/MP/5seed | игра не запускалась | NOT_RUN |

Важная конкретная неисправность: CinderMob наследует Monster, поэтому прежний
Enemy fallback считал нейтрального AshSpirit и мирного Sparkling hostile для общей
системы отношений. Теперь AshSpirit neutralдоtarget, Sparkling peaceful. Islands
остаются neutralдоtarget; wildGulokryl neutral/tameddefender,Griboshmyg peaceful.
Это не изменение ихattackAI и не принудительное агро ради уровня.

Ownercheck: сначала безOP наблюдать нейтральных рядомссоюзнымNPC иприрученным
Гулокрылом. Провоцировать одного: ответтолько виновнику, безновыхпризывов. Проверить
кровотечение/физическийяд: недопустимы дляPLANT/SPIRIT/UNDEAD/ELEMENTAL/CONSTRUCT;
существующаяnativefireImmune сохранена. ВMP/послерелога одинаковыеотношения,
дроп/4Postenкопии не дублируются. Runtimeиокончательные уровниownerpending.
