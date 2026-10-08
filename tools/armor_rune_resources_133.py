"""Localization overlay for the existing armor/rune UI; no new items or textures."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
TEXT={
 'armor_runes_inactive':('Руны на броне сохраняются, но пока не дают эффектов.','Armor stores runes; their effects are not active yet.'),
 'armor_rune_storage':('Броня: хранение рун без эффектов.','Armor: rune storage without effects.')
}
def main():
 for index,lang in enumerate(('ru_ru','en_us')):
  path=ROOT/f'src/main/resources/assets/slavicmyths/lang/{lang}.json'
  data=json.loads(path.read_text(encoding='utf-8'))
  data.update({'rpg.slavicmyths.'+key:value[index] for key,value in TEXT.items()})
  path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print('Armor rune localization: RU/EN; no new registry IDs.')
if __name__=='__main__':main()
