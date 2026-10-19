# #73 — локализация: IN_PROGRESS / OWNER_PENDING

Исправлены ChudGenerateCommands.java и client/RpgScreen.java; добавлены8 ключей
RU/EN в оба lang JSON. Подтверждено8 повреждённых пользовательских мест:
6 сообщений команд (9 строковых фрагментов) и2 UI-символа с двойной перекодировкой.
Других подтверждённых повреждений в4831 production текстовом файле не найдено.
Примеры: `РќРµ ...` → «Не хватает высоты мира…»; `РЎРѕР·РґР°РЅ ...` →
«Создан %s; %s; границы: %s…»; двойная перекодировка знаков → `✓` / `×`.

Новый tools/verify_user_text.py читает UTF-8 строго, проверяет литералы Java,
assets/data JSON, повторную cp1251/latin1/cp1252 перекодировку, replacement/control
символы, новые command ключи и их RU/EN placeholders, язык в JAR. Не переписывает
ресурсы; не сканирует архивы, документацию или инструменты. Эвристика не заменяет
человеческую проверку всех формулировок; динамические ключи за пределами command
пространства не объявляются полностью проверенными.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Исправлены6 командных сообщений и2 UI-символа | Production diff; RU/EN8 ключей | PASS |
| Назначение, аргументы и механики сохранены | Только текст/Component.translatable в diff | PASS |
| Повторяемая проверка UTF-8/JSON/placeholders | python tools/verify_user_text.py --jar | PASS |
| Java21 clean build | .tools/localization-73-build.log; BUILD SUCCESSFUL1m13s | PASS |
| Production JAR/языковые ресурсы | build/libs/slavicmyths-1.9.7.19.jar; --jar parity | PASS |
| Отображение и ручная приёмка | Запусков0; RUNTIME_NOT_RUN | NOT_RUN |

PolyMC не изменяется этой задачей. Проверить владельцу: ошибки/успех генератора
Чуди, galочки и закрытые навыки в экране развития; RU и EN.
Issue остаётся OPEN до приёмки владельцем.
