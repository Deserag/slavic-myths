"""Placement and text overlay; preserves existing silhouettes, materials and registry IDs."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1]
A=R/'src/main/resources/assets/slavicmyths'
# Grip positions in authored model coordinates. ItemInHandLayer rotates X -90/Y 180;
# the X rotation plus entity Y inversion keeps the long axis upright and centers the actual grip.
GRIPS={'ancient_carolingian_sword_held':-2,'restored_carolingian_sword_held':-2,
 'ancient_spear_held':6,'restored_spear_held':6,'ancient_chekan_held':-2,'chekan_held':-2,
 'likho_staff':7,'victor_spear':7,'ataman_flail':6,'trophy_kalin_sabre':2,
 'nightingale_dagger':2}
def write(p,d):p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def apply():
 from weapon_visual_repair_assets import apply as repair
 repair()
 for name,grip in GRIPS.items():
  p=A/f'models/item/{name}.json';d=json.loads(p.read_text(encoding='utf-8'));display=d.setdefault('display',{})
  for side in ['righthand','lefthand']:
   old=display.get('thirdperson_'+side,display.get('thirdperson_righthand',{}));scale=old.get('scale',[.75]*3)
   display['thirdperson_'+side]={'rotation':[90,0,0],'translation':[0,0,(8-grip)*scale[1]],'scale':scale}
  if 'firstperson_lefthand' not in display and 'firstperson_righthand' in display:
   left=json.loads(json.dumps(display['firstperson_righthand']));rot=left.get('rotation',[0,0,0]);left['rotation']=[rot[0],-rot[1],-rot[2]];display['firstperson_lefthand']=left
  write(p,d)
 for lang in ['ru_ru','en_us']:
  p=A/f'lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));ru=lang=='ru_ru'
  d['item.slavicmyths.three_breath_crossbow.desc']=('Всегда три стрелы. Shift+ПКМ: Мор / Пламя / Буря, без бутылочек. Shift+ЛКМ: веер / очередь по направлению взгляда.' if ru else 'Always three arrows. Sneak-use: Mor / Flame / Storm, no bottles. Sneak-attack: fan / burst along your aim.')
  for bottle,ru_name,en_name in [('mor','Мора','Mor'),('flame','пламени','flame'),('storm','бури','storm')]:
   d[f'item.slavicmyths.gorynych_{bottle}_breath_bottle.desc']=(f'Собранное дыхание {ru_name}. Компонент улучшения арбалета; для стрельбы не требуется.' if ru else f'Collected {en_name} breath. Crossbow upgrade ingredient; not required for shooting.')
  for i,n in enumerate(['Мор','Пламя','Буря'] if ru else ['Mor','Flame','Storm']):d[f'gorynych196.mode_{i}']=n
  d['gorynych196.volley_fan']='Тройной выстрел: веер' if ru else 'Triple shot: fan'
  d['gorynych196.volley_burst']='Тройной выстрел: очередь' if ru else 'Triple shot: burst'
  for k in list(d):
   if k.startswith('tales.entry.great_poloz.'):
    d[k]=('Полоз получает урон по любой части тела с первой фазы. Сражение проходит в три фазы; руины и порча остаются после победы. Ядро и чешуя — трофеи. Образ вдохновлён уральскими сказами; порча и бой — игровая интерпретация.' if ru else 'Poloz takes damage on every body part from phase one. The fight has three phases; ruins and corruption remain after victory. The core and scales are trophies. Inspired by Ural tales; corruption and combat are game interpretations.')
  d['poloz.armor']='Полоз уязвим по всему телу.' if ru else 'Poloz is vulnerable across his whole body.'
  write(p,d)
if __name__=='__main__':apply()
