> Обновление домашнего QA29.10.2026: актуальные ограниченные headless результаты и source SHA в [матрице](../home-desktop-sync-20261029/ACCEPTANCE.md). Ниже исходная таблица первичных жалоб. O-01 проверен на initial tick в3измерениях; O-07 —4BlockItem/BE/rotations/drop; O-08 —тело босса/нейтральная саламандра. Эти PASS не заменяют complete lifecycle/visual/MP. Новые жалобы #349 перечислены ниже. Другой чат напрямую недоступен: CHAT_SOURCE_NOT_DIRECTLY_ACCESSIBLE.

# Реестр ранее выявленных владельцем ошибок — обязательные повторные тесты
**Создан 29.10.2026.** Источники — ранее записанные пользовательские замечания в GitHub [#327](https://github.com/Deserag/slavic-myths/issues/327), [#331](https://github.com/Deserag/slavic-myths/issues/331), [#332](https://github.com/Deserag/slavic-myths/issues/332) и дочерних. Список **не претендует на полный доступ к другому чату**: если там есть дополнительные отдельные наблюдения, Codex должен получить их из реально доступной истории/Issues, включить сюда, не приписывать владельцу неподтверждённые жалобы. **Ни один пункт ниже ещё не прошёл новый игровой ретест 29.10.2026.** Не закрывать ошибку на основании одного source fix.

| ID | Сообщённое владельцем / технический ретест | Где зафиксировано | Статус в НОВОМ QA | Доказательства |
| --- | --- | --- | --- | --- |
| O-01 | Постень исчезает при ручном summon в Overworld. Проверить /summon и яйцо во всех измерениях, 4 маски/копии, без бесплатных наград. | #332/#333 | NOT_RUN | — |
| O-02 | Скованные/«склеенные» лапы Костяного ползуна: настоящие 8 подвижных суставных ног. | #332/#334 | NOT_RUN | — |
| O-03 | Угольный ползун: невнятная/жёсткая походка и движения конечностей; проверить source-current rig и утверждённую форму. | #332/#334 | NOT_RUN | — |
| O-04 | Новые обычные мобы однообразны по цветам и анимациям. Особенно саламандра/краб/Жерловик, угольный ползун/Пекельник; проверить все 19 вариантов 16 видов. | #332/#335/#336 | NOT_RUN | — |
| O-05 | Плоская голова Сторожа чащи, не видны заявленные idle/proximity FX. | #327/#331 | NOT_RUN | — |
| O-06 | Слишком маленькая голова Постеня, при этом поворот на 90° работает. Не ломать поворот, сделать читабельный масштаб. | #327/#331 | NOT_RUN | — |
| O-07 | У одной головы-трофея отсутствует результат создания/выдачи/появления; точный ID неизвестен. Отдельно item/GUI/размещение/добыча 4 боссовых голов, distinction manual vs legal claim. | #327/#331 | NOT_RUN; EXACT_ID_UNKNOWN | — |
| O-08 | Повелитель магмы не удаляется при включении Peaceful; проверить и bossbar. | #327/#317 | NOT_RUN | — |
| O-09 | После предыдущих визуальных исправлений владелец «не видит изменений»: проверить installed JAR SHA и реальное source→render отличие, art match. | #327/#331 | NOT_RUN | — |
| O-10 | Два bossbar на прежнем кадре: **это НЕ подтверждённая ошибка**, может быть два отдельных босса. См. UNCERTAIN_FINDINGS Q-001. | #327 | NEED_REPRO | — |
| O-11 | Поиск/природная генерация новых биомов 2.0.2–2.0.5: обязательно проверить как самостоятельную регрессию. Прямого старого сообщения «поиск сломан» здесь нет — это новая просьба владельца от 29.10. | поручение 29.10 + #294/#313 | NOT_RUN | — |

## Порядок заполнения
Для каждого пункта после выполнения указать **head SHA**, установленный test JAR SHA256, сид, XYZ, `/locate biome`/команды (где уместно), OP vs survival, шаги, фактический результат и ссылку на лог/кадр. Разрешённые статусы: `REPRODUCED_FAIL`, `REPRODUCED_FIXED`, `NOT_REPRODUCED_CURRENT_BUILD`, `BLOCKED`, `NOT_RUN`. При `NOT_REPRODUCED_CURRENT_BUILD` показать минимум 3 воспроизведения/попытки; не объявлять, что исходная жалоба была неверной. Добавить остальные достоверные владельческие замечания из доступных источников, не изобретая их.

**Доступ к другому ChatGPT-чату напрямую не подтверждён в этом GitHub-документе.** Если Codex не может его прочитать — статус `CHAT_SOURCE_NOT_DIRECTLY_ACCESSIBLE`; новые детали владелец и Codex смогут добавить отдельно, но это не освобождает от тестирования всех известных проблем.


## Добавлено из #349 — точный scope
| ID | Owner requirement | Current result | Evidence |
|---|---|---|---|
| O-12 | King cape/body anchor, colors/glow, Steve/Alex; approved design сохранить | GUI run IN_PROGRESS; native UV/vertices проверены, визуальный PASS не дан | home-desktop-sync-20261029/ENVIRONMENT.md |
| O-13 | Infernal bow actual creative/survival shots/ammo/special/CD/hit | HEADLESS_PASS для ammo/undercharge/one arrow/one consumption/damage/fire; actual GUI pending | bow349.log, fire-integration-v4.log |
| O-14 | Short/heavy bow GUI/first/third orientation/draw | source/timing есть, current in-game visual pending | GitHub isolated visual40904333746 |
| O-15 | Separate heads tab, six Chud tools category, RU/EN | Java2115 + static20005 PASS; explicit RU heads добавлена; actual creative tab GUI NOT_RUN | creative-inventory/GATES.json |
| O-16 | Chud4 approved16×16preview before production replacement | OWNER_PENDING; финальныеPNG не менялись | chud298-preview-20261027/APPROVED_REFERENCE.md |

Source: [#349](https://github.com/Deserag/slavic-myths/issues/349), [#298](https://github.com/Deserag/slavic-myths/issues/298). Не приписывать первичным регрессиям эти статусы как полную приёмку.
