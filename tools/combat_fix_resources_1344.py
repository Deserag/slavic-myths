"""Canonical additive 1.3.4.4 resources; preserves all previous unrelated entries."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
def apply():
 res=ROOT/'src/main/resources'
 texts=json.loads((ROOT/'tools/combat_rune_text_1344.json').read_text(encoding='utf-8'))
 for locale,updates in texts.items():
  p=res/f'assets/slavicmyths/lang/{locale}.json';data=json.loads(p.read_text(encoding='utf-8'));data.update(updates);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 for p in (ROOT/'tools/templates/combat-1344').glob('*.json'):(res/'assets/slavicmyths/models/item'/p.name).write_bytes(p.read_bytes())
 import tugarin_resources_13451
 tugarin_resources_13451.apply()
 import boss_resources_13452
 boss_resources_13452.apply()
if __name__=='__main__':apply()
