from pathlib import Path
import json
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data'
def write(p,j):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(j,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
im=Image.new('RGBA',(16,16));dr=ImageDraw.Draw(im);dr.line((3,1,2,5,5,9,10,9,13,5,12,1),fill='#b89953',width=1);dr.polygon([(7,7),(11,10),(8,15),(4,10)],fill='#dbc583',outline='#756343');dr.polygon([(7,9),(9,10),(8,13),(6,10)],fill='#53a8a9');im.save(A/'textures/item/nether_aura_amulet.png')
write(A/'models/item/nether_aura_amulet.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/nether_aura_amulet'}})
write(D/'slavicmyths/recipe/nether_aura_amulet.json',{'type':'minecraft:crafting_shaped','pattern':[' S ','IRI',' A '],'key':{'S':{'item':'minecraft:string'},'I':{'item':'slavicmyths:alatyr_ingot'},'R':{'item':'slavicmyths:roasted_runnik'},'A':{'item':'slavicmyths:roasted_alatyrite'}},'result':{'id':'slavicmyths:nether_aura_amulet','count':1}})
write(D/'slavicmyths/advancement/recipes/nether/nether_aura_amulet.json',{'parent':'minecraft:recipes/root','criteria':{'material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'slavicmyths:roasted_runnik'}]}}},'rewards':{'recipes':['slavicmyths:nether_aura_amulet']}})
p=D/'curios/tags/item/necklace.json';j=json.loads(p.read_text());j['values']=list(dict.fromkeys(j['values']+['slavicmyths:nether_aura_amulet']));write(p,j)
p=A/'creative_catalog.tsv';s=p.read_text(encoding='utf-8');n='nether_aura_amulet'
if not any(l.split('\t')[0]==n for l in s.splitlines()):s+=n+'\taccessories\tnether_protection\n'
p.write_text(s,encoding='utf-8')
for lang,labels in [('ru_ru',['Оберег адской ауры','В слоте ожерелья Curios защищает только от адской ауры. В сумке не действует. Прототип.']),('en_us',['Nether Aura Ward','In a Curios necklace slot, protects only against Nether Aura. Inactive in inventory. Prototype.'])]:
 p=A/'lang'/f'{lang}.json';j=json.loads(p.read_text(encoding='utf-8'));j['item.slavicmyths.nether_aura_amulet']=labels[0];j['item.slavicmyths.nether_aura_amulet.effect']=labels[1];write(p,j)
print('Independent aura ward prototype; recipe/equip BALANCE_OWNER_PENDING')
