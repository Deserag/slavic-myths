"""Only two existing book pages; no new recipes, loot, art or acquisition rules."""
from pathlib import Path
import json
LABELS = {
 'ru_ru': {
  'tales.entry.items_gusliitem_1.studied': 'Удерживайте ПКМ рядом с восприимчивыми существами: после игры они ненадолго успокаиваются. Дальность — 12 блоков; многие особые существа невосприимчивы. Штатный способ получения гуслей в выживании сейчас отсутствует.',
  'tales.entry.items_skatertitem_1.studied': 'Расстелите скатерть на твёрдой опоре: она даёт 6 порций еды. При снятии или истечении времени возвращается одна скатерть, которой нужен отдых 5 минут. Штатный способ получения скатерти в выживании сейчас отсутствует.'
 },
 'en_us': {
  'tales.entry.items_gusliitem_1.studied': 'Hold right-click near susceptible creatures: playing briefly calms them. Range: 12 blocks; many special creatures are immune. A normal Survival acquisition route for the gusli is currently absent.',
  'tales.entry.items_skatertitem_1.studied': 'Unfold the cloth on a solid support for 6 servings. Picking it up or letting it expire returns one cloth, which must rest for 5 minutes. A normal Survival acquisition route for the cloth is currently absent.'
 }
}
def apply(root=None):
 root=Path(root) if root is not None else Path(__file__).resolve().parents[1]
 for lang,labels in LABELS.items():
  p=root/f'src/main/resources/assets/slavicmyths/lang/{lang}.json';d=json.loads(p.read_text(encoding='utf8'))
  assert labels.keys()<=d.keys();d.update(labels);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
if __name__=='__main__':apply()
