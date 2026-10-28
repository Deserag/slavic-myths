# Чертоги заблудших — baseline, 28.10.2026

Прямое поручение владельца: начать всю цепочку #288–#294, #301–#303.
Это разрешение на исполнение, но не отмена SHA/art gates и запрета runtime.

- Исходная папка: `C:/Users/user/Documents/slavic-myths`, `master`,
  `f3f7e388` (старая 0.8.4), пользовательские изменения сохранены на месте.
- Рабочая папка: `C:/Users/user/.codex/worktrees/lost-halls/slavic-myths`,
  ветка `hell`, HEAD `877b7d69af0e809850827806045d82fde901a1a3`.
- `origin/main`: `e7fcd0e2f761ab81dd2b5694d3ee78f04f733c2c`.
  Перенесены только `docs/concepts/lost-halls/` и два контракта/чертежа
  `NETHER_*HEADS*`; merge/cherry-pick не выполнялись. Main не изменена.
- Fetch последних снимков выполнен с `--depth=1 --no-tags` после отмены
  полной загрузки большой истории. Это shallow history, не полный архив Git.
- Minecraft 1.21.1, NeoForge 21.1.255, Java 21, mod ID `slavicmyths`.
  `mod_version=2.0.8-HELL` сохраняется: 2.0.4 — этап контента, не откат версии.
- Локальный JDK: `C:/Users/user/Documents/slavic-myths/.tools/jdk21/jdk-21.0.12.1+1`.
  JAVA_HOME менялся только в дочерних командах. Системная Java 8 не заменялась.
- Python 3.14; Pillow 13.0.0 установлен в `.tools/python-libs` исходной папки,
  доступен проверкам через дочерний PYTHONPATH.

## Фактические точки подключения

`navya/NavyaWorldgen.java`, `navya/NavyaContent.java`, `navya/NavyaBlocks.java`
обслуживают Навью рощу; `blackcinder/CinderWorldgen.java`, `CinderContent.java`,
`CinderBlocks.java` — Чёрную гарь. `navya/mixin/NetherParametersMixin.java`
добавляет параметры двух биомов в существующий Nether preset.
Ветка содержит biome JSON `navya_grove`, `black_cinder`, структуры `navya_heart`
и `zharenitsa_arena`. Третий биом пока отсутствует в production.

**Интеграционный риск для #294:** mixin сейчас различает лишь cinder/non-cinder;
передача третьего holder без расширения классификации присвоит ему параметры
Навьей рощи. Это вывод по коду, не воспроизведённая игровая ошибка.
Сейчас mixin не менялся: добавление worldgen до готовых зависимостей запрещено.

`blackcinder/CinderBlocks.java` уже регистрирует `zharenitsa_head`.
Его текущий Head имеет горизонтальную ориентацию, но не отдельный wall mount
и не анимационную BE-модель из #302. Существование головы не означает выполнение
нового контракта; её ID требуется сохранить при развитии.

`evidence/registry_inventory.tsv` — source/resource inventory: 2 biome JSON,
351 blockstate candidate, 1152 item/source-model candidate, 119 literal/expanded
entity declarations. **Не полный runtime registry dump**: неподтверждённые
динамические регистрации и resource-only строки явно помечены.
В `evidence/production-sha256.json` записаны 9457 фактических файлов src/main.

## Ограничения

Нет новых worldgen/AI/tick loops, registry IDs, предметов, текстур или заглушек.
Сохранены Nether height/noise/vanilla payload и два предыдущих биома.
Ни сохранения, ни PolyMC не менялись. Minecraft/client/server/GameTest = 0.
Нагрузочные ограничения #274/#275 не измерялись, их выполнение не заявлено.
Commit/push/PR/изменения статусов GitHub/закрытие Issues не выполнялись.
