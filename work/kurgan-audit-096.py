from pathlib import Path
import re,json,zipfile,shutil
from PIL import Image
root=Path('.');backup=root/'.tools/port-backups/slavicmyths-0.9.5-pre-0.9.6.zip';backup.parent.mkdir(parents=True,exist_ok=True)
if not backup.exists():
 with zipfile.ZipFile(backup,'w',zipfile.ZIP_DEFLATED) as z:
  for directory in ['src','docs']:
   for p in (root/directory).rglob('*'):
    if p.is_file():z.write(p,p.as_posix())
  for name in ['build.gradle','gradle.properties','packaging/test-pack-lock.json']:z.write(root/name,name)
shutil.copy2(root/'build/libs/slavicmyths-0.9.5.jar',root/'.tools/port-backups/slavicmyths-0.9.5-final.jar')
out=root/'docs/kurgan';out.mkdir(parents=True,exist_ok=True)
lang=json.loads((root/'src/main/resources/assets/slavicmyths/lang/ru_ru.json').read_text(encoding='utf-8'));en=json.loads((root/'src/main/resources/assets/slavicmyths/lang/en_us.json').read_text(encoding='utf-8'))
source=(root/'src/main/java/org/slavicmyths/registry/ModItems.java').read_text(encoding='utf-8');section=source[source.index('BURIAL_LOG_COFFIN='):source.index('OVINNAYA_ZOLA=')];ids=re.findall(r'(?:register|egg)\("([a-z0-9_]+)"',section)
lines=['# Kurgan item art audit — исходное состояние перед 0.9.6','','ID сохранены. Spawn eggs используют vanilla tinted egg model и не требуют самостоятельной перерисовки.','', '| ID | RU / EN | PNG | Размер | Назначение и текущая проблема | Перерисовка / целевой рисунок |','|---|---|---|---|---|---|']
for id in ids:
 p=root/f'src/main/resources/assets/slavicmyths/textures/item/{id}.png';size=Image.open(p).size if p.exists() else None
 if 'spawn_egg' in id:purpose='Яйцо существующего моба';target='Нет: vanilla egg model'
 elif id=='burial_log_coffin':purpose='Существующий двухблочный loot container';target='Block model; сохранить тёмное дерево и металлические скобы'
 else:
  purpose='Оружие/восстановление' if any(x in id for x in ['sword','spear','chekan']) else 'Археология, украшение или курганный трофей'
  purpose+='; старый спрайт низкого разрешения' if size and min(size)<256 else '; проверить силуэт и стиль'
  target='Да: новый >=256px pixel-cluster sprite, отдельный узнаваемый объект; palette согласно 06'
 lines.append(f'| `slavicmyths:{id}` | {lang.get("item.slavicmyths."+id,id)} / {en.get("item.slavicmyths."+id,id)} | `{p.as_posix()}` | {size or "block/vanilla model"} | {purpose} | {target} |')
(out/'KURGAN_ITEM_ART_AUDIT_0.9.6.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
loots=list((root/'src/main/resources/data/slavicmyths/loot_table/chests').glob('kurgan_*.json'))+list((root/'src/main/resources/data/slavicmyths/loot_table/chests').glob('burial_*.json'))
lines=['# Исходный курганный loot до правок 0.9.6','','Все перечисленные ID сохраняются. Новые room tables расширяют распределение, старые tables остаются доступны.','']
for p in sorted(set(loots)):
 data=json.loads(p.read_text(encoding='utf-8'));lines+=['## `slavicmyths:chests/'+p.stem+'`','','```json',json.dumps(data,ensure_ascii=False,indent=2),'```','']
(out/'KURGAN_LOOT_CURRENT_0.9.6.md').write_text('\n'.join(lines),encoding='utf-8')
print('Backup and before-edit audits saved:',len(ids),'items;',len(loots),'tables')
