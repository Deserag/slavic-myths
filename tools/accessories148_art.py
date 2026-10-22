"""Hand-authored 16x16 pixel matrices, recipes/tags and GUI-only model split for #148.
No AI raster resampling, no old held/worn geometry changes. Reproducible production pixels.
"""
from pathlib import Path
import json,shutil,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
# Each character is one exact production pixel. Dots are transparent.
PALETTE={'.':(0,0,0,0),'x':'#302823','d':'#604535','b':'#94623e','w':'#c39761','l':'#e6cc93','s':'#7b8b91','S':'#c4d0ca','W':'#f2ead1','g':'#b88d38','G':'#e4c45c','a':'#c77622','A':'#ecab3a','r':'#8c352c','R':'#ce6531','h':'#43614a','H':'#799653','c':'#31626d','C':'#63a4a1'}
PIXELS={
'perun_ring':[
'........Gx......','.......GGx......','......SGx.......','.....SGGGx......','......xGx.......','.....xGx........','....xSx.........','...xSSSSx.......','..xSSxxsSx......','..xSx..xsSx.....','..xSx...xSx.....','...xSx..xSx.....','....xSSSSx......','.....xxxx.......'],
'resin_ring':[
'................','........xx......','.......xAAx.....','...xxxxAWWAx....','..xbwwwxAAAax...','.xbwx..xAAax....','.xwx...xaaax....','.xbx....xax.....','.xbx....xx......','..xbx..xbx......','...xbwwbx.......','....xxxx........'],
'veles_amulet':[
'...dx......xd...','....dx....xd....','.....dx..xd.....','......dxxd......','....xxbwwbxx....','...xbwxwwxwbx...','....xbwxxwbx....','.....xwbbwx.....','.....xbwwbx.....','......xwwx......','......xbwx......','.....xwbwwx.....','......xxxx......'],
'depth_amulet':[
'.....sx...xs....','......sx.xs.....','.......sxs......','......xCCx......','.....xCWWCx.....','....xCWWWWCx....','....xcWSSWCx....','.....xcSSCcx....','......xCccx.....','.......xcx......','.......xsx......','......xSCx......','.......xx.......'],
'retribution_charm':[
'.....ddxdd......','....dx..xd......','.....dxdd.......','......xx........','...xxxlxx.......','..xWllxlWx......','...xllxllx......','.....xrx........','....xWRWx.......','...xWlxlWx......','....xlxlx.......','.....xlx........','......x.........'],
'hunter_belt':[
'................','..........xx....','........xxwwx...','......xxwwbbx...','....xxwwbbxx....','..xxwwbbxx......','.xbwbGGGx.......','..xwbGxdGx......','...xbGddGx......','..xbwGGGx.......','.xbwbxx.........','..xxx...........'],
'invisibility_cap':[
'................','.........xx.....','.......xxrrx....','......xrRrrx....','.....xrRRrx.....','....xrRRRrx.....','...xrRRRrrrx....','...xRrrRrrrrx...','..xRRrrrrrrrx...','..xrrrrrrrrrrx..','.xwwlwwwlwwwwx..','..xxxxxxxxxxx...'],
'pool_ring':[
'................','......xxx.......','.....xWWWx......','....xWWSCWx.....','.....xCCCx......','....xxsSsxx.....','...xSCx.xCSx....','..xSCx...xCSx...','..xCx.....xCx...','..xscx...xcsx...','...xscxxccsx....','....xsssssxx....','.....xxxxx......'],
'bath_ward':[
'.......dx.......','......dhHx......','.....dhHHhx.....','......hHHx......','.....xxhxx......','....xssSSsx.....','...xsSSSSssx....','...xsSwbbwsx....','...xSbwwbSSx....','...xsbSwwssx....','....xsSwbsx.....','.....xxxxd......','.........dx.....','..........x.....'],
'harvest_ring':[
'..........G.....','.........GGx....','.......GxGx.....','........GGGx....','......GxGGx.....','.......GGGx.....','....xxxGGx......','...xggxGx.......','..xGgxGx........','..xGx.ggx.......','..xgx..ggx......','...xGggggx......','....xxxxx.......'],
'podvey_belt':[
'................','..xx......xx....','.xSwx....xwSx...','..xSwwxxwwSx....','...xSwSSwSx.....','..xxdSbSwddxx...','.xwwdbSSbdwwwx..','.xbdwbSSbwdbbx..','..xxxwSSwxxxx...','.....xSwx.......','....xSwSSx......','.....xxxSSx.....','........xxx.....'],
'leshy_necklace':[
'..dx........xd..','...dx......xd...','....dx....xd....','.....dx..xd.....','......dxxd......','...Hhxxwwxx.....','..xHHHhwbbwx....','...xHHHwAAbx....','....xhwwAAwx....','.....xwbwwx.....','......xwwx......','.......xx.......','......xdx.......','.......x........'],
'spark_ring':[
'..........Rx....','.........xGRx...','........xGGRx...','.........xAGR...','......xxRxGGRx..','.....xggGRAGRxx.','....xGgxRAGRx...','...xGgx.xRRx....','...xgx...xx.....','...xgx..xgx.....','....xggggx......','.....xxxx.......'],
}
# Additional defects found in the full GUI review: three repeated U chains and two identical belt masks.
PIXELS.update({
'zolny_obereg':['......dx........','.....dwdx.......','......dx........','.....xxx........','....xsssx.......','...xsrrrsx......','..xsrRArssx.....','..xsRAAxssx.....','..xsrAxsssx.....','...xsrrssx......','....xsssx.......','.....xx.........','....r..a........'],
'obereg_padayuschey_zvezdy':['...........l....','.........lW.....','........lW......','.......lW.......','......xlWx......','...l..xGWx..l...','....lxGWWGxl....','..llGWWWWWGll...','....lxGWWGxl....','...l..xGWx..l...','......xGx.......','.......l........','.......l........'],
'odnoglazyy_obereg':['.....dx.........','......dx........','.......dx.......','......xxx.......','....xxgggxx.....','...xgGWWWGgx....','..xgWWWxWWWgx...','...xGWWxWWGx....','....xGWWWGx.....','.....xgggx......','......xgx.......','.......x........'],
'volchiy_poyas':['................','...ww..ww.......','..xWWxxWWx......','.xwWWwwWWwx.....','xbwwlwwlwwbx....','xbwllllllwbx....','xbwwwwwwwwbx....','.xWxxWWxxWx.....','..Wx.WW..W......','..x...W..x......','......x.........'],
'poyas_tugarina':['................','......GGGx......','....xGGggGx.....','..xxGgx..gGx....','.xGGgx....GGx...','xGggx..xxgGx....','xGgx..xGGgx.....','.xGgxxGGgx......','..xGGGGgx.......','...xgggx........','....xxx.........'],
})
NEW={
'pool_ring':('ring','Перстень омута','Pool Ring','Слот: кольцо. +15% скорости плавания только в воде. Две копии не складываются.','Ring slot. +15% swim speed only in water. Duplicate copies do not stack.', [' M ','MPM',' W '],{'M':'minecraft:iron_ingot','P':'slavicmyths:pearl_fragment','W':'slavicmyths:water_grass'}),
'bath_ward':('charm','Банный оберег','Bath Ward','Слот: оберег. Обычный яд или слабость: длительность −20%, откат 60 с. Не действует на сюжетные и боссовые эффекты.','Charm slot. Ordinary Poison or Weakness lasts 20% less; 60s cooldown. Excludes story and boss effects.', [' S ','BHB',' S '],{'S':'minecraft:string','B':'slavicmyths:bath_stone','H':'minecraft:honeycomb'}),
'harvest_ring':('ring','Перстень жатвы','Harvest Ring','Слот: кольцо. Ручной сбор спелых пищевых культур: 10% шанс на +1 урожай, откат 5 с. Без семян и совместного срабатывания фермерских рун.','Ring slot. Manual mature food-crop harvest: 10% chance of +1 produce, 5s cooldown. No seeds or farming-rune stacking.', [' M ','MNM',' F '],{'M':'minecraft:copper_ingot','N':'slavicmyths:noon_ear','F':'slavicmyths:field_bundle'}),
'podvey_belt':('belt','Пояс Подвея','Podvey Belt','Слот: пояс. При падении с 12 блоков: −70% итогового урона, откат 60 с после полученного урона.','Belt slot. Falls of at least 12 blocks: 70% less final damage; 60s cooldown after actual damage.', ['LLL','VNV','LLL'],{'L':'minecraft:leather','V':'slavicmyths:vetrovoy_uzel','N':'slavicmyths:vihrevaya_nit'}),
'leshy_necklace':('necklace','Ожерелье Лешего',"Leshy's Necklace",'Слот: ожерелье. Враждебный удар от 6 HP в лесу даёт Скорость I на 4 с; откат 60 с.','Necklace slot. A hostile hit for at least 6 HP in a forest grants Speed I for 4s; 60s cooldown.', [' B ','BHB',' A '],{'B':'slavicmyths:birch_bark','H':'slavicmyths:leshy_heart','A':'slavicmyths:amber'}),
'spark_ring':('ring','Перстень искры','Spark Ring','Слот: кольцо. Прямой удар по врагу: 25% шанс поджечь на 3 с, откат 8 с. Не действует на игроков, питомцев и огнеиммунных.','Ring slot. Direct melee against a hostile foe: 25% chance to ignite for 3s; 8s cooldown. Excludes players, pets and fire-immune foes.', [' M ','MSM',' F '],{'M':'minecraft:copper_ingot','S':'slavicmyths:pylayuschaya_cheshuya','F':'slavicmyths:ognennoe_pero'}),
}
OLD=set(PIXELS)-set(NEW)
def sprite(rows):
 im=Image.new('RGBA',(16,16));
 for y,row in enumerate(rows):
  assert len(row)<=16 and y<16
  for x,key in enumerate(row):im.putpixel((x,y),PALETTE[key] if key=='.' else tuple(bytes.fromhex(PALETTE[key][1:]))+(255,))
 return im

def apply(root=ROOT):
 root=Path(root);res=root/'src/main/resources';a=res/'assets/slavicmyths';data=res/'data';
 def write(p,d):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
 # A small dedicated body atlas: cloth/wood, amber and leaf, exact native UV cells.
 body=Image.new('RGBA',(32,32),'#805a3b')
 for y in range(32):
  for x in range(32):
   color=('#b09a64' if (x+2*y)%7==0 else '#785a3d') if y<16 else ('#a9be7d' if x>=12 else '#d49b3b')
   body.putpixel((x,y),tuple(bytes.fromhex(color[1:]))+(255,))
 bp=a/'textures/entity/accessories148.png';bp.parent.mkdir(parents=True,exist_ok=True);body.save(bp)
 for id,rows in PIXELS.items():
  tex=a/f'textures/item/{id}.png';tex.parent.mkdir(parents=True,exist_ok=True);sprite(rows).save(tex)
  model=a/f'models/item/{id}.json';gui={'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{id}'}}
  if id in OLD:
   held=a/f'models/item/{id}_held148.json'
   with zipfile.ZipFile(Path(__file__).with_name('accessories148_held.zip')) as z:held.write_bytes(z.read(id+'.json'))
   write(model,{'loader':'neoforge:separate_transforms','base':{'parent':f'slavicmyths:item/{id}_held148'},'perspectives':{'gui':gui},'textures':{'particle':f'slavicmyths:item/{id}'}})
  else:write(model,gui)
 for id,(slot,ru,en,rut,ent,pattern,key) in NEW.items():
  write(data/f'slavicmyths/recipe/{id}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'id':'slavicmyths:'+id,'count':1}})
 for slot in set(v[0] for v in NEW.values()):
  p=data/f'curios/tags/item/{slot}.json';d=json.loads(p.read_text(encoding='utf8')) if p.exists() else {'replace':False,'values':[]}
  for id,v in NEW.items():
   if v[0]==slot and 'slavicmyths:'+id not in d['values']:d['values'].append('slavicmyths:'+id)
  write(p,d)
 for lang in ['ru_ru','en_us']:
  p=a/f'lang/{lang}.json';d=json.loads(p.read_text(encoding='utf8'))
  for id,v in NEW.items():d['item.slavicmyths.'+id]=v[1 if lang=='ru_ru' else 2];d['item.slavicmyths.'+id+'.effect']=v[3 if lang=='ru_ru' else 4]
  write(p,d)
 p=a/'creative_catalog.tsv';lines=p.read_text(encoding='utf8').splitlines();known={l.split('\t')[0] for l in lines}
 for id,v in NEW.items():
  if id not in known:lines.append(id+'\taccessories\t'+v[0])
 # Preserve all unrelated entries; accessories grouped by the six existing types.
 order={'head':0,'necklace':1,'ring':2,'belt':3,'charm':4,'offhand':5,'use':6,'inventory/use':7}
 selected=[l for l in lines if '\taccessories\t' in l];other=[l for l in lines if '\taccessories\t' not in l];selected.sort(key=lambda l:(order[l.split('\t')[2]],l.split('\t')[0]))
 p.write_text('\n'.join(other+selected)+'\n',encoding='utf8')
if __name__=='__main__':apply()
