"""Reproducible six-head resources. The actual geometry/UV live in mob ModelParts."""
from pathlib import Path
import json,zipfile

ROOT=Path(__file__).resolve().parents[1]
HEADS={
 'tugarin_head':('Голова Тугарина Змея','Tugarin Serpent Head',1,'tugarin_phase2'),
 'skim_head':('Голова Ским-зверя','Skim Beast Head',1,'skim_beast'),
 'cursed_volkolak_head':('Голова Проклятого волколака','Cursed Volkolak Head',1,'cursed_volkolak'),
 'likho_head':('Голова Лиха Одноглазого','One-Eyed Likho Head',2,'likho_one_eyed'),
 'mor_likho_head':('Голова Морового Лиха','Blighted Likho Head',2,'mor/likho'),
 'large_podzemnik_head':('Голова Крупного Подземника','Large Podzemnik Head',2,'underground/large_podzemnik')}

def apply(resources=ROOT/'src/main/resources'):
 a=resources/'assets/slavicmyths';d=resources/'data/slavicmyths'
 def write(p,value):
  p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
 for n,(_,_,height,texture) in HEADS.items():
  with zipfile.ZipFile(Path(__file__).with_name('trophy140_icons.zip')) as archive:
   p=a/f'textures/item/{n}.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(archive.read(n+'.png'))
  write(a/f'blockstates/{n}.json',{'variants':{f'facing={f},half={h}':{'model':'slavicmyths:block/'+n} for f in ['north','east','south','west'] for h in ['lower','upper']}})
  write(a/f'models/block/{n}.json',{'textures':{'particle':'slavicmyths:entity/'+texture}})
  display={context:{'rotation':rot,'translation':[0,(-3 if height==2 else 0),0],'scale':[scale]*3} for context,rot,scale in [('gui',[15,150,0],.68 if height==1 else .53),('ground',[0,0,0],.35),('fixed',[0,180,0],.55),('thirdperson_righthand',[0,0,0],.4),('firstperson_righthand',[0,0,0],.55)]}
  write(a/f'models/item/{n}_held.json',{'parent':'builtin/entity','textures':{'particle':'slavicmyths:entity/'+texture},'display':display})
  write(a/f'models/item/{n}.json',{'loader':'neoforge:separate_transforms','base':{'parent':'slavicmyths:item/'+n+'_held'},'perspectives':{'gui':{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+n}}},'textures':{'particle':'slavicmyths:item/'+n}})
  write(d/f'loot_table/blocks/{n}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+n}],'conditions':[{'condition':'minecraft:survives_explosion'},{'condition':'minecraft:block_state_property','block':'slavicmyths:'+n,'properties':{'half':'lower'}}]}]})
 for lang,i in [('ru_ru',0),('en_us',1)]:
  p=a/'lang'/f'{lang}.json';values=json.loads(p.read_text(encoding='utf8')) if p.exists() else {}
  for n,v in HEADS.items():values['block.slavicmyths.'+n]=v[i]
  write(p,values)
 p=a/'creative_catalog.tsv';text=p.read_text(encoding='utf8') if p.exists() else '';seen={line.split('\t')[0] for line in text.splitlines()}
 for n in HEADS:
  if n not in seen:text+='\n'+n+'\tresources\ttrophy_heads'
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text.rstrip()+'\n',encoding='utf8')

if __name__=='__main__':apply()
