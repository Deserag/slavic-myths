# #62 / #66 — адресный разбор после пакета Соловья

IN_PROGRESS; runtime NOT_RUN, hearing NOT_LISTENED. Это статический аудит,
не свидетельство слышимости и не завершение аудиопроекта.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Проверить текущие файлы и JAR | ASSETS.csv/gates.json: 1309 OGG, decode failures 0, JAR mismatch 0 | PASS |
| Обновить сквозной acoustic screen | PROJECT_SIMILARITY_METHOD.json: 856086 пар, 445 spectral candidates, 318 high waveform matches | PASS |
| Разделить результаты скрининга по назначению | 51 inactive original pair, 248 recorded physical-prop pairs, 18 active legacy/mixed, 1 recorded vocal pair | PASS |
| Объяснить recorded vocal pair | phase1/phase2 death Соловья — намеренно одна смерть одного человека, отдельные файлы не объявлены различными эмоциями | PASS (source intent); hearing NOT_RUN |
| Разобрать ранее неясные маршруты | MANUAL_ROUTES_19710 + NIGHTINGALE + VOLKOLAK_FROST + LEGACY_LOOPS: 111 уникальных entity/event строк из155 | PASS (manual source classification) |
| Остальные44 неоднозначные строки | MANUAL_ROUTES_SUMMARY.json; scanner по-прежнему показывает155, ручной overlay не маскирует его ограничения | NOT_RUN |
| Сохранить исходные новые performances | NIGHTINGALE_SOURCE_PROOF.json:5 исходных archive members, exact decoded PCM после lossless FLAC | PASS |
| Проверить реальную установленную сборку | TEST_PREVIEW_1.9.7.11.json: source bdf72453, SHA совпадает, один modId JAR, backup | PASS |
| Owner слуховая и игровая приёмка | Нет запусков со стороны Codex | NOT_RUN |

Важные результаты маршрутизации:

- Обычный VolkolakEntity наследует HuntMob. Семейство `volkolak.*` относится к
  отдельному CursedVolkolak и его квесту. В HuntMob остаются прямые vanilla
  growl/pant/hurt/death и ravager roar для отдельных боевых действий. Это
  самостоятельный оставшийся P0, а не исправление, достигнутое заменой файлов
  проклятого Волколака.
- `skull_unearth` и `curse_cleared` — завершённые серверные транзакции игрока;
  grave rustle/dig относятся к могиле. Это не ambient голоса обычного Волколака.
- `morozko.staff_stream` — клиентский цикл посоха игрока. Server FrostSkills
  отправляет Stream при старте, уже активный stream не отправляет повторно;
  FrostStreamSound проверяет жизнь/мир/использование предмета и age cap70.
  Сетевые гонки и фактическое выключение при смене мира требуют runtime проверки.
- В RusalkaSound `APPROVED_VOCALS_AVAILABLE=false`: intro/loop/intense имеют
  описанную маршрутизацию, но запуск намеренно запрещён до принятого вокала.
  Классификация этого пути не означает исправления песни.
- Dotted профили Тугарина в основном legacy, однако blade_draw/sheathe реально
  используются TugarinPair при экипировке оружия игроком. Они не orphan и не
  собственные callbacks босса.
- BayunSong имеет клиентского владельца/UUID map и отмену с fade. Его song loop
  остаётся отдельной нерешённой художественной задачей.

Оставшиеся44 строки включают варианты героев/сокола/Маринки/Полоза и role-based
dispatch псоглавцев. Наличие записанного файла не доказывает действующий callback;
особенно нужно проверить Алёшу hurt/recovery и Finist sword contact/варианты.
