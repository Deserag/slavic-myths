"""Reproducible aliases to Minecraft-owned events; no synthetic or copied audio."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths'
ROUTES={
 'idle':'block.campfire.crackle','hurt':'entity.iron_golem.damage','death':'entity.ravager.death',
 'wake':'entity.ravager.roar','phase':'entity.wither.spawn',
 'claws_charge':'entity.ravager.step','claws_release':'entity.player.attack.sweep',
 'fracture_charge':'block.respawn_anchor.charge','fracture_release':'block.basalt.break',
 'pull_charge':'block.portal.ambient','pull_release':'block.respawn_anchor.deplete',
 'hands_charge':'entity.warden.tendril_clicks','hands_release':'entity.iron_golem.attack',
 'eruption_charge':'entity.blaze.ambient','eruption_release':'entity.generic.explode',
 'ring_charge':'block.lava.ambient','ring_release':'entity.blaze.shoot'}
def main():
 p=A/'sounds.json';d=json.loads(p.read_text(encoding='utf-8'))
 for cue,event in ROUTES.items():
  key='islands.magma_lord.'+cue;d[key]={'subtitle':'subtitles.slavicmyths.'+key,'sounds':[{'name':'minecraft:'+event,'type':'event','volume':.18 if cue=='idle' else .55 if cue=='phase' else .7}]}
 p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 ru={'idle':'Треск магмового тела','hurt':'Повелитель магмы ранен','death':'Повелитель магмы затихает','wake':'Повелитель магмы пробуждается','phase':'Ядро Повелителя раскрывается','claws':'Магмовые когти','fracture':'Магмовый разлом','pull':'Притяжение теней','hands':'Руки преисподней','eruption':'Теневое извержение','ring':'Кольцо извержения'}
 for lang in ['ru_ru','en_us']:
  p=A/f'lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'))
  for cue in ROUTES:
   base=cue.split('_')[0];d['subtitles.slavicmyths.islands.magma_lord.'+cue]=(ru[base]+(' — подготовка' if cue.endswith('_charge') else ' — удар' if cue.endswith('_release') else '')) if lang=='ru_ru' else 'Magma Lord: '+cue.replace('_',' ')
  p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':main()
