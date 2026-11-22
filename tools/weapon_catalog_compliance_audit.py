"""Reconcile the live 102-item inventory with assets, translations and explicit HUD coverage."""
import json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def main():
 catalog=json.loads((ROOT/'docs/balance/weapon-audit-20261122/catalog.json').read_text(encoding='utf-8'))
 runtime=json.loads((ROOT/'work/weapon-skill-hud-client-20261122/evidence/all-weapons-runtime-audit.json').read_text(encoding='utf-8'))
 live={row['id']:row for row in runtime}
 locales={code:json.loads((ROOT/f'src/main/resources/assets/slavicmyths/lang/{code}.json').read_text(encoding='utf-8')) for code in ('ru_ru','en_us')}
 hud=(ROOT/'src/main/java/org/slavicmyths/client/WeaponSkillHud.java').read_text(encoding='utf-8')
 supported=set()
 for match in re.finditer(r'case ([^\n]+?) ->',hud):supported.update(re.findall(r'"([a-z0-9_]+)"',match[1]))
 lines=['# Проверка каталога оружия — 22.11.2026','','Проверены все 102 ID исторического каталога в реальном клиенте: регистрация, построение tooltip, текущий класс предмета. Отдельно проверены обе локализации и наличие модели. Это не проверка всех боевых механик и не игровой аудит баланса.','','Иконки: «да» — явная запись в панели; «общий CD» — только резервный индикатор при активном vanilla cooldown. Для обычного оружия без активного навыка иконка готовности не требуется. Наличие слова Shift — подсказка для ручного обзора, а не доказательство доступности навыка.','','| ID | Регистрация / tooltip | RU / EN имя | Модель | Явная иконка | Shift в tooltip | Класс use |','|---|---|---|---|---|---|---|']
 missing=[]
 for entry in catalog:
  id=entry['id'];row=live.get(id)
  if row is None:raise AssertionError('Missing live weapon '+id)
  key='item.slavicmyths.'+id
  names=all(key in lang for lang in locales.values())
  model=(ROOT/f'src/main/resources/assets/slavicmyths/models/item/{id}.json').is_file()
  text=' '.join(row['tooltip']);shift='shift' in text.lower()
  explicit=id in supported
  if shift and not explicit and id!='chud_multitool_axe':missing.append(id)
  owner=row['useOwner'].replace('org.slavicmyths.','').replace('net.minecraft.world.item.','')
  lines.append(f'| `{id}` | да | {"да" if names else "проверить fallback"} | {"да" if model else "проверить custom"} | {"да" if explicit else "общий CD"} | {"да" if shift else "—"} | `{owner}` |')
 lines+=['','## Найденные расхождения и исправления','','- Добавлены арбалеты: обычный выстрел и текущий стихийный режим арбалета трёх дыханий.','- Убраны подписи под всеми иконками и отдельные оружейные описания Морозко, Карачуна и Мора. Аксессуарные/статусные показатели отделены.','- Обычные действия посохов Бездны, Морозко и Мора получили собственные таймеры; остальные предметы каталога имеют резервный индикатор активного vanilla cooldown.','- Коса Невеи фиксирует Shift при начале зарядки; последующее отпускание Shift не меняет выбранный навык.','- Одиночный молот Ломеи больше не выполняет обычный удар вместо недоступного парного Shift-навыка. Парный навык доступен только при втором молоте.','- Адский лук не заменяет Shift-выстрел обычной стрелой при откате дополнительного навыка.','- Общий vanilla cooldown у оружия сестёр сохраняется: оба навыка недоступны до окончания одного общего отката. Изменение на независимые таймеры было бы изменением баланса.','','## Осталось проверить','','- Полноценный бой, переключение рук/предметов, смерть, повторный вход, dedicated server и мультиплеер для каждого семейства.','- Прохождение/эффективный DPS и доступность получения без OP; старые цифры каталога не являются текущим измерением.','- Отдельные пользовательские таймеры и условия пассивных срабатываний не доказываются резервным vanilla-индикатором.']
 lines+=['','Чудской мультитул проверен отдельно: Shift+ПКМ переключает инструмент; это инструментальная функция, исключённая из оружейного диспетчера стандартом управления. Отсутствие оружейной иконки не является ошибкой.','Цепы и метательное оружие дополнительно подключены по классам FlailItem / ThrowingWeaponItem: иконка присутствует и при готовности, и при общем откате.','Последний игровой запуск остановлен по просьбе владельца. До остановки получен SISTERS_BEHAVIOR_PASS и кадры трёх предметов на масштабе 1; полная повторная проверка масштабов финальной версии не завершена.']
 if missing:lines+=['','Предметы с Shift в tooltip без явной записи панели: '+', '.join('`'+id+'`' for id in missing)+'. Требуют ручной классификации (активный навык, переключение режима или обычное контекстное действие).']
 out=ROOT/'docs/requirements/WEAPON_CATALOG_COMPLIANCE_20261122.md';out.write_text('\n'.join(lines)+'\n',encoding='utf-8')
 print(f'102/102 live items reconciled; {len(supported.intersection(live))} explicit HUD entries; {len(missing)} Shift descriptions need manual classification; report: {out}')
if __name__=='__main__':main()
