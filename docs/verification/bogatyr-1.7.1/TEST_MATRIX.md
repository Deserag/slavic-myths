# Матрица 46 критериев из ZIP

PASS означает подтверждённую серверную/offline проверку указанной части. BLOCKED означает, что полный исходный критерий требует ещё игровой приёмки или отдельного сценария; это не найденная ошибка кода. Клиент не запускался по AGENTS.md. Логи: [сервер](runtime-08.log), [offline](offline.log), [restart](restart-read.log), [production/resource](resource-gates.json). Не считать 48 test cases равными 48 исходным критериям.

| № | Статус | Фактическое доказательство и предел проверки |
|---:|---|---|
|1|BLOCKED|2.5 блока native mesh PASS; 2.45 collision height; игровая стена/движение не сняты|
|2|BLOCKED|9.0 native mesh PASS; collision 8.8; игровая колонна не снята|
|3|BLOCKED|19 bones/107 cuboids; offline front/side/back/¾; игровые front/side/back/top не сняты|
|4|BLOCKED|39 bones/131 cuboids; offline четыре вида; игровой rack/кисти не проверены|
|5|BLOCKED|UV bounds/padding и packaged PNG PASS; missing texture/ориентация в клиенте не проверены|
|6|PASS|finiteShield: состояние держится64t, на65t выходит, на80t shield=false; offline pose finite|
|7|PASS|maceHitFrame:900HP доt19, 890 после; native clip t19|
|8|BLOCKED|54t drink server + flask.visible/mace.hidden offline PASS; звук и бутылочка в игре не проверены|
|9|BLOCKED|Серверные импульсы4/10/16 и one-hit PASS; renderer цвет/штриховка статически есть, клиент не проверен|
|10|BLOCKED|Server grab/rescue/escape PASS, jaw>=36° offline; пальцы/жертва и отсутствие client ghost не проверены|
|11|BLOCKED|chargeWallStun thick wall PASS; незагруженная граница статически guarded, игровой boundary не проверен|
|12|BLOCKED|rageOnce PASS и отдельные rage-eye cubes offline; изменение выражения/скорости в игре не проверено|
|13|BLOCKED|Death110/cleanup реализованы, offline collapse PASS; полного наблюдения в игре нет|
|14|BLOCKED|Предупреждения — геометрия, не частицы; клиент на минимум particles не запускался|
|15|BLOCKED|Обычный taunt goal с120t TTL реализован; отдельного runtime обычного врага/возврата цели нет|
|16|BLOCKED|tauntFocus и finite owner participation PASS; длительный AFK/контрприёмы игровым боем не измерены|
|17|PASS|tauntFocus cooldown400t, unyieldingOnce HP84 одноразовый без reset после heal|
|18|PASS|physicalPassive:20→15 HP два раза без stacking; magic20→20; crisis20→11.25|
|19|PASS|shieldFront20→6, shieldRear20→15, massive20→12; heavy block3→recover|
|20|PASS|interceptExactlyOnce: owner24 unchanged, hero274; следующийowner16; aoe не redirect|
|21|BLOCKED|maceHitFrame поboss10HP PASS; отсутствие shove/interruption статически предусмотрено, отдельно runtime не измерено|
|22|BLOCKED|Shockwave реализован t29/R4.5 без block edits; отдельный runtime3zombies/allies отсутствует|
|23|PASS|realRetreatAnd55Seconds: HP42, refuge>=24, на500t<140/noReturn; на1450>=140/returned; returnBudget второй раз DOWN|
|24|PASS|recoveryAfterBossDeath: bossId=null, RETURNING; restart HP finite; полноценное следование в клиенте не проверялось|
|25|BLOCKED|handRescue25 и shared wrist660frames PASS; visual attach именноИльи в клиенте не проверен|
|26|PASS|duplicateAndMidfightCall запрещает оба вызова сервером; screen работает через тот же валидируемый packet|
|27|PASS|registeredDimensions maxHP900; rageOnce315→phase2, heal45 не отменяет/не повторяет переход|
|28|PASS|diamondFist:24→16 net8, armor20/toughness8|
|29|PASS|outer24→16 net8; mid24→14.760 net9.24; inner24→12.040 net11.96|
|30|PASS|ResistanceI net6.4 вместо8; shieldFront/massive PASS; штатный hurt path|
|31|PASS|fistHasWindup нетурона до21t; кулак22+8, grab25+8, breath25, charge28, stomp32; атаки один state|
|32|PASS|stompOneHit смена трёх зон: единственная потеря11.96 из100HP|
|33|PASS|stompEscape R17:24HP без изменений|
|34|BLOCKED|timelyJumpReduction raw26*.45=11.7 net3.709 PASS; отдельный ceiling collision jump не проверен|
|35|BLOCKED|sixActionEscape/handRescue PASS; raw18+10 штатно≈9.84netdiamond, реальная landing sequence не измерена|
|36|BLOCKED|summonMinions2..3 и minionCap<=5 PASS; cooldown840t установлен, отдельного ожидания повторного cast нет|
|37|PASS|feedingHealsExactly+45, feedingInterrupt32, heroFeedInterrupt40+8, feedingMaxTwo PASS|
|38|PASS|poisonBudgetAndDebuffs:до24t нетурона, послеhit net8, триэффекта, следующийbounded1HP/40t|
|39|PASS|chargeLockedHeading yaw неизменен и recovery; chargeWallStun50 PASS|
|40|BLOCKED|noIdleAutoplay PASS: без hit ownerActive=false, среальнымdamage=true; пятиминутный бой не выполнялся|
|41|PASS|progressParticipation/debugNoVictory: once, no duplicate; loot/XP0, без loot table наград|
|42|BLOCKED|Disk server reopen PASS: тотжеUUID, finiteHP, budgets/hero once, boss abort/minioncleanup; клиентrejoin не выполнен|
|43|BLOCKED|Dedicated server загружается без client linkage; две настоящие клиентские сессии/HUD/звук не запускались|
|44|PASS|progressParticipation, spectatorNoProgress, bystanderNoProgress PASS; restart count1|
|45|BLOCKED|33 monoVorbis полныйdecode+registry33+byteidentity+RU/EN PASS; MissingSound в клиентском playback не проверено|
|46|BLOCKED|clean build/resource49,497checks PASS, runtime48 совпадающийcode; actual productionJAR client load не выполнен; legacy6753 entries retained/attacks не пишут блоки|

Для полной приёмки требуется отдельный тестовый мир, клиентские перспективы и playback, два реальных игрока, реальный relog, 5 минут боя и MSPT. Offline previews не игровые screenshot. Тестовые игроки имеют dummy connection.
