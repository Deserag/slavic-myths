# Актуальное дополнение #223/#224

После прямого поручения исправлены guidance и mark. [Новая матрица](issue217/finist223/REPORT.md):91 matrix +19 live hits,26 native server tests,19 JAR checks, native marker framebuffer A/B. Стены не обходятся. Ранее сохранённые FAIL — история, не текущий результат. Installed P53 не менялся.

# Актуальный итог23.10.2026

[REPORT](issue217/REPORT.md):26 native server tests,19 Finist releases с промахами, одно-проходная плеть144 real frames,88 GUI bounds PASS, clean build52s,18 JAR gates PASS. Старые capture BLOCKED и ранние FAIL ниже — история. Новых installs/commits нет. Provenance и запуски в issue217/*.json.

# #217 — 23.10.2026, начато

## Последующее разрешение и реальный автоматический runtime

Владелец предложил отдельный мир и проверки без управления рабочим столом. Такой путь работает: tools/issue217-runtime.init.gradle направляет GameTests в work/issue217/server-first-run; существующий weapon-visual-client fixture запущен в work/issue217/client-first-run на отдельной копии служебного VisualQA. Исходный QA-мир и пользовательские saves не изменены. Никаких SendInput/mouse/keyboard действий для этих запусков не использовалось.

- Реальных запусков нового серверного процесса: 2. Первый: 16 tests, 2 FAIL (устаревший count36 вместо37 и heat projectile). Fixture count обновлён под реально проверенные37 рецептов; для legacy heat явно добавлена provenance, современная rune presence не эквивалентна legacy effect.
- Второй серверный прогон: 15 PASS, 1 FAIL nativeKnifeCollisionAndLoss — heat по-прежнему не поджигает цель даже при explicit legacy provenance. Не объявлять это исправленным; требуется аудит event/callsite/test premises.
- Реальных новых клиентских запусков: 1, отдельный dev client с integrated server, автоматические кадры из framebuffer. RunClient BUILD SUCCESSFUL in2m04s. Кадры и gameplay markers в issue217-client-first.log и docs/implementation/weapons87/issue217/client-regression-first. Это существующий scythe/crossbow regression fixture, НЕ полная проверка GUI/плети/щита/рога и НЕ полный #217 PASS.
- Дальше расширить client probe под текущие дефекты (inventory grid, swords/spears/whip, shield/horn, actual attack frames), исследовать оставшийся server FAIL. Task остаётся «В работе». Никакого нового JAR в PolyMC не установлено.

Предыдущий блок о невозможности desktop capture ниже остаётся историей: это ограничение Windows capture, а не запрет или невозможность автоматического Minecraft runtime.

GitHub CLI authenticated as Deserag with project scope. Project9 item PVTI_lAHOBeEK2c4Bn9vzzhC51Nk added and actual Status «В работе» verified. Issue body now includes swords fitting a single cell, permission to create defect sub-issues in «Доработка» after the test run, permission to mark actually fully tested tasks «Готово», and exclusion of all Hell tasks. No tasks marked ready/closed.

Confirmed source cause for oversized GUI models: ThirdPersonWeaponMotion discards the BakedModel returned by NeoForge SeparateTransformsModel.applyTransform; this drops the selected GUI model and draws the held base. Changed non-3P path to return the selected model. WhipClient likewise discarded the selected fallback model; corrected return propagation. compileJava PASS (issue217-compile.log). These are preliminary uncommitted fixes, not runtime-accepted and not delivered to PolyMC.

Used computer-use skill @oai/sky. Opened PolyMC. Existing Minecraft window4457556 (Java21, Minecraft NeoForge1.21.1) discovered, not assumed to be a newly launched test. User explicitly authorized taking the window, then manually restored it on request. Capturing the restored game still fails: `window capture timed out: timed out waiting on channel`; earlier `FrameArrived timed out`. Following skill recovery limit, stop repeated input into an unobservable game. No world changes or gameplay checks performed, zero assistant-started Minecraft launches. Runtime BLOCKED_CAPTURE, not PASS. Existing user world must remain intact.

Remaining: make selected GUI path verifiable through real model-baking/runtime; ensure whip animation wrapper actually installs and displays; diagnose retainer_shield and beast_challenge_horn geometry/texture sampling; compact swords/spears/whip in inventory; full clean build/resources/generator gates after final fixes; separate test world and gameplay matrix after capture access works. No automatic installation, commit, push or Issue closure.
