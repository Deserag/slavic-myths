from pathlib import Path
p=Path('tools/kurgan_loot_materials_096.py');s=p.read_text(encoding='utf-8-sig');start=s.index("for p in (A/'models/block').glob('*pale_blue_kurgan_cobblestone*.json'):");end=s.index('# New recipes',start)
s=s[:start]+'''for family,top in [('pale_blue','burial_stone_top'),('bog_green','bog_green_kurgan_stone')]:
 for p in (A/'models/block').glob('*'+family+'_kurgan_cobblestone*.json'):
  v=json.loads(p.read_text(encoding='utf-8-sig'));t=v.get('textures',{})
  if t.get('all'):
   side=t.pop('all');v['parent']='minecraft:block/cube_bottom_top';t.update({'side':side,'top':'slavicmyths:block/'+top,'bottom':'slavicmyths:block/'+top})
  elif 'top' in t:t['top']=t['bottom']='slavicmyths:block/'+top
  put(p,v)
'''+s[end:];p.write_text(s,encoding='utf-8')
