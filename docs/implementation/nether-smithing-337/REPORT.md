# #325/#326/#329/#330 — доступный кузнечный блок, IN_PROGRESS

Реализованы 12 промежуточных предметов с разными пиксельными силуэтами,
11 точных 3×3 рецептов, дополнительный ember loot Пекельника и шесть обычных
Жаровых upgrades. Это **не** полная реализация восьми частей брони и пяти
особых оружий. Оригинальные PNG не импортированы, ART_BLOCKED_REFERENCE.
Minecraft/PolyMC/server/GameTest:0 запусков. Приёмки владельца не было.

| REQUIREMENT | REAL_SOURCE / PLAYER_PATH | EVIDENCE | RESULT |
|---|---|---|---|
| Template + plate +4armor catalysts +ember +5weapon catalysts | NetherSmithing.java; 12items/12distincticons | nether337_smithing_assets.py | PASS source/resources; runtime NOT_RUN |
| Ember from existing Pekelnik | один новый loot pool,65%,1–2; остальные soul pools сохранены | loot_table/entities/pekelnik.json | PASS source; частота OWNER_REVIEW_PROPOSED/runtime NOT_RUN |
| Точные 3×3 grids | #326 C/D/E иweapon contract4 | 11recipes,recipe unlocks | PASS source; actualcraft/fullinventory/MP NOT_RUN |
| 6Zhara IDs | sword/axe/spear/sulitsa/throwing_knife/flail | NetherSmithing.java +6smithing_transform JSON | PASS source; registry/UI runtime NOT_RUN |
| Diamond base +template1+plate1→output1 | обычный smithing table, неправильная diamond форма не подходит | verify_nether337_smithing.py | PASS exact JSON; native smithing menu NOT_RUN |
| +15%direct/+20%durability | реальные source attributes сохраняютspeed/reach; separate throw/charged flail multiplier1.15,old constructors default1 | NetherSmithingRules/default component event, native test | PASS shared transformations; event/runtime DPS NOT_RUN |
| Сохранение components/износа | vanilla transmuteCopy, scoped RETURN mixin normalized ceil ratio | NetherSmithingMixin; native ItemStack name/customdata/wear | PASS transformation; mixin invocation/runes/enchants/MP NOT_RUN |
| Два rune slots | шестьпредметовTierDIAMOND, штатныеRunes.capacity/category | source constructors | PASS source; slots0/1/2/denied3 иexisting rune retention NOT_RUN |
| 6originaldiamond forms | ровно прежняя geometry/UV/display; copied models сновымиPNG; sourcealpha unchanged | verify_nether337_smithing.py | PASS source; actualfirst/thirdperson/itemframe/glint NOT_RUN |
| Flail chain / spear / throwing hands | новые IDs подключены в существующие FlailClient/WeaponMotion/ThrowingMotion/PlayerWeaponPose | explicitIDdiff | PASS wiring; runtime clips NOT_RUN |
| 8armor wearables | ещё не зарегистрированы/не реализованы; нетподменыvanilla recolor вместо венца/плаща | ART_IMPLEMENTATION_LOCK | NOT_IMPLEMENTED; ART_BLOCKED_REFERENCE |
| 5special weapons +5active skills | пятькатализаторовесть,самихweapons/skills нет | weapon contract | NOT_IMPLEMENTED; механика ещёWIP,неприписыватьеёартблокеру |
| Infernal furnace/fuel | параметрыfuel duration/speed/recipe остаютсяproposal в#325, рабочаяпечьНЕсоздана | #325 | NOT_IMPLEMENTED; BALANCE_UNAPPROVED |
| Natural survival chain | руды→обжарка→ingot→template/plate→diamond smithing; boss catalysts требуют законные drops | #324/#326recipechain | SOURCE_WIRED; Islandsnatural/арена всёещёBLOCKED |
| Build/resources | Java21 build иnative gates;исходныйтестовыйсбойfrozenregistryисправленчерезобщиеproductionfactories,неunfreeze | gates.txt | PASS technical; gameplay NOT_RUN |

Общий SmithingTransformRecipe не заменён: mixin действует только на результат
шести zhara weapon IDs. Vanilla и прежние upgrades не меняют износ. Старые
ThrowingWeaponItem/FlailItem constructors передают multiplier1; их прежние throw,
Loyalty иloss25% не скопированы и не заменены вторым projectile engine.

Генератор cinder_assets сохраняет новый ember pool через shared apply_ember.
Генератор руд теперь честно описывает существующие кузнечные consumers вместо
устаревшего tooltip «применение в будущем». Ни оружие, ни броня не добавлены
в трофеи Повелителя магмы; его прежний дизайн/модель/HP/фазы не трогались.

## Как проверить в Minecraft — в этой сессии NOT_RUN

Без OP: в новых Nether чанках добыть и обжечь минералы; по схемам #324/#326
сделать ingot,template4 иplate1. В smithing table разместить template,
каждую соответствующую diamond форму и plate → ровноодин zhara weapon.
Проверить изношенную/названную/зачарованную основу с0/1/2рунами: сохраняются
данные и процентизноса,нет бесплатногоремонта/гнёзд. Невернаяформа/нетtemplate/
нетplate/повторное взятие/полныйinventory должныневыдаватьлишнеепредметы.
Проверить обычныеалмазные копьё,сулицу,нож,кистень: иххарактеристики прежние.
У zhara сверить +15%raw melee/throw/chargedflail и1874durability, прежнийspeed/
reach/метание/chainmotion;glint/две руки/GUI/F5/dropped/itemframe. Руннаякузница:
максимум2обычныхслота,третьеотказать;релоад/MPдваигрока/сохранениепроцентаизноса.
Отдельно убитьПекельниканесколькораз:emberpoolтолькоодин,некаждыйkillгарантирован.

Риски: модификация default components и новый mixin не грузились настоящим
NeoForge-клиентом; JEI/rune/MP/UI остаются NOT_RUN. Сложное gear и furnace
ещё отсутствуют. Откат: сохранить копию мира до новых IDs и backup JAR внеmods;
не удалять новые registry IDs из уже используемого сохранения вслепую.
Следующие задачи: реальныеDworldgen/IIrestore; per-attackFbudget;8armor,
5specialskills и furnace после конкретныхпараметров/доступныхоригиналов.
