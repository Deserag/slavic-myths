"""Idempotent completion report; preserve previous milestones."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/winters-1.9.7'
def main():
 gate=json.loads((O/'resource-gates.json').read_text(encoding='utf-8-sig'));install=json.loads((O/'polymc-installation.json').read_text(encoding='utf-8-sig'))
 log=(R/'.tools/winters-clean-build.log').read_text(encoding='utf-8',errors='replace')
 markers=[line for line in log.splitlines() if '_OFFLINE_PASS' in line or '120 native' in line]
 text=f"""# Оберег двух зим 1.9.7 — фактическая проверка

Микропатч архива Slavic_Myths_1.9.7_CODEX_PROMPT_TZ.zip для Minecraft 1.21.1 / NeoForge 21.1.255 / Java 21.

Изменены: пакет winters (один предмет, точные рецепты, серверные квитанции, два режима, общий persisted кулдаун, щит, аура); регистрация в SlavicMyths и «Магии»; WintersClient (tooltip/property), узкий Inventory.setChanged mixin и запрет автоматического Crafter для двух удостоверяемых рецептов; три модели/иконки, RU/EN, два entity type тега. Подробнее: [TWO_WINTERS_1.9.7.md](../../TWO_WINTERS_1.9.7.md).

Механики/награды и production bytecode Морозко, Карачуна, Горыныча и Полоза сверены побайтно с установленной 1.9.6. Существующие registry IDs и старые ресурсы сохранены; изменённые прежние ресурсы: {', '.join(gate['changed_old_resources'])}.

Чистый gradlew.bat clean build с десятью offline наборами: **PASS**. Ресурсные/JAR gates: **{gate['status']}**, {gate['checks']} проверок; event subscribers: {gate['event_subscribers']}. Генератор новых ресурсов воспроизводит их побайтно. В production нет тестовых классов.

{chr(10).join(markers)}

Новые 4853 offline-проверки используют реальные Minecraft CraftingInput/CustomRecipe/ItemStack/getRemainingItems и компоненты: точные рецепты, запрет копии как шаблона, возврат одного неизменённого оригинала, неверный тип/отсутствующий токен, сохранение компонентов, серверные квитанции NBT, суммарный щит 10 и границы 30/60/90 с. Изолированный test fixture открывает замороженный vanilla item registry только в отдельном offline JVM; этот код не входит в мод.

Production: build/libs/slavicmyths-1.9.7.jar; SHA256 {gate['sha256']}. PolyMC Testing обновлён: {install['installed_jar']}; SHA256 совпадает; предыдущий JAR в {install['backup']}. Побайтно сохранены {install['other_files_preserved']} остальных файлов профиля, включая миры/конфигурации. Старый 1.16.5 не затронут.

Фактических запусков клиента **0**, сервера **0**, GameTest **0**. Shift-click/полный инвентарь/быстрые клики/отмена/выброс исследованы по штатному ResultSlot и контракту remaining-items, но в игре не выполнялись. Визуал/звук, реальные удары/вероятности/чужие эффекты, dedicated startup, multiplayer, смерть/перезаход/передача, MSPT не проверены. NBT roundtrip не является перезаходом. Запуск без отдельного разрешения не выполнялся.
"""
 (O/'REPORT.md').write_text(text,encoding='utf-8')
 section=f"""## 1.9.7 — Оберег двух зим (2026-10-13)

Один артефакт из существующих наград Морозко/Карачуна; точные рецепты оригинала и копии с серверной квитанцией и возвратом шаблона. Два серверных режима, пассивы во всех измерениях, активный щит/волна только в Нижнем мире, общий persisted кулдаун, персональная поэтапная аура и собственный эффект без удаления чужой Слабости. Старые боссы и награды не изменены.

Чистая production сборка + десять offline наборов **PASS**; новый набор 4853, ресурсные gates {gate['checks']}. PolyMC Testing обновлён до **1.9.7**, SHA256 {gate['sha256']}; предыдущий JAR в backup, остальные файлы/миры/конфигурации сохранены. Клиент/сервер/GameTest: **0** запусков; игровые проверки не заявляются.

Архитектура: существующий PathData/RPG sync; WintersEvents — события боя и локальный cast, inventory/aura раз в 20 тиков плюс лёгкий Inventory.setChanged; WintersRecipe — exact 3×3 CustomRecipe/native remaining-items; WintersTemplates — SavedData выданных оригиналов. CLIENT property/tooltip; stable KB modifier, свой щит и эффект aura. Автоматический Crafter отключён для этих двух рецептов. Будущие milestone не реализованы.

Подробнее: docs/TWO_WINTERS_1.9.7.md, docs/verification/winters-1.9.7/REPORT.md.

"""
 for path in ['docs/PROJECT_STATUS.md','docs/ROADMAP.md','docs/ARCHITECTURE.md','CHANGELOG.md']:
  p=R/path;old=p.read_text(encoding='utf-8-sig')
  if not old.startswith('## 1.9.7 — Оберег двух зим'):p.write_text(section+old,encoding='utf-8')
 print('WINTERS_REPORT_PASS')
if __name__=='__main__':main()
