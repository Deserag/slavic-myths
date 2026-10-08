"""Final 1.1.8 overlay. Native pixels and bounded cuboids; safe to rerun after older generators."""
from pathlib import Path
import json
from io import BytesIO
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]; R=ROOT/'src/main/resources'; A=R/'assets/slavicmyths'; D=R/'data/slavicmyths'
def js(p,v):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def save(im,p):
 data=BytesIO();im.save(data,format="PNG");value=data.getvalue()
 if not p.exists() or p.read_bytes()!=value:p.write_bytes(value)
def cube(a,b,t,uv=(0,0,16,16)):
 return {'from':a,'to':b,'faces':{f:{'texture':t,'uv':list(uv)} for f in ['north','south','east','west','up','down']}}
def shaped(id,pattern,key,count=1):
 js(D/f'recipe/{id}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'tag' if v.startswith('#') else 'item':v.lstrip('#')}for k,v in key.items()},'result':{'id':'slavicmyths:'+id,'count':count}})
def main():
 # Staff silhouette: long narrow dark wooden shaft, three root branches above the grip,
 # leather bindings and a small cold crystal. All faces use an opaque material atlas.
 for name in ['staff_wood','staff_binding','staff_crystal']:
  im=Image.new('RGBA',(16,16),{'staff_wood':'#4c3326','staff_binding':'#806042','staff_crystal':'#659baf'}[name]);d=ImageDraw.Draw(im)
  if name=='staff_wood':
   for x in [2,7,12]:d.line((x,0,x,15),fill='#654734');d.line((x+1,2,x+1,12),fill='#392a21')
  elif name=='staff_binding':
   for y in [2,6,10,14]:d.line((0,y,15,y),fill='#b09567')
  else:d.rectangle((2,2,6,13),fill='#9ac5ca');d.rectangle((12,1,15,15),fill='#406477')
  save(im,A/f'textures/item/{name}.png')
 for id in ['carved_staff','storm_staff']:
  els=[cube([7,0,7],[9,25,9],'#wood'),cube([6.5,10,6.5],[9.5,13,9.5],'#binding'),cube([7,22,7],[9,29,9],'#wood'),cube([4,23,7],[7,25,9],'#wood'),cube([3,24,7],[5,29,9],'#wood'),cube([9,25,7],[12,27,9],'#wood'),cube([11,26,7],[13,30,9],'#wood'),cube([7,26,9],[9,28,12],'#wood'),cube([7,27,11],[9,30,13],'#wood'),cube([6.7,22,6.7],[9.3,23,9.3],'#binding'),cube([6.5,26,6],[9.5,29,7.5],'#crystal' if id=='storm_staff' else '#binding')]
  display={}
  for side in ['right','left']:
   display[f'thirdperson_{side}hand']={'rotation':[0,0,0],'translation':[0,4,1],'scale':[.8,.8,.8]}
   display[f'firstperson_{side}hand']={'rotation':[0,10 if side=='right' else -10,-8 if side=='right' else 8],'translation':[0,3,1],'scale':[.65,.65,.65]}
  display.update(gui={'rotation':[0,0,-25],'translation':[0,-3,0],'scale':[.45,.45,.45]},ground={'translation':[0,3,0],'scale':[.35,.35,.35]},fixed={'rotation':[0,0,-25],'translation':[0,-4,0],'scale':[.45,.45,.45]})
  js(A/f'models/item/{id}.json',{'textures':{'wood':'slavicmyths:item/staff_wood','binding':'slavicmyths:item/staff_binding','crystal':'slavicmyths:item/staff_crystal','particle':'slavicmyths:item/staff_wood'},'display':display,'elements':els,'ambientocclusion':False})
 # Open crescent, with no closed ring; diagonal wooden handle is 40% of its length.
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 d.polygon([(3,1),(6,1),(9,3),(10,6),(9,9),(7,10),(5,8),(7,8),(8,6),(7,4),(5,3),(2,3)],fill='#525d62')
 d.line([(3,1),(6,1),(9,3),(10,6),(9,9)],fill='#c6ced0');d.line([(3,2),(6,2),(8,4),(9,6),(8,8)],fill='#8e9ca2')
 d.line((7,9,13,14),fill='#493224',width=3);d.line((8,10,13,14),fill='#a27a43');d.line((8,11,11,13),fill='#78552e');d.point((7,9),fill='#252c30');save(im,A/'textures/item/sickle.png')
 palettes={'raspberry':('#802f39','#b34750','#d76b6c'),'blueberry':('#28334e','#465b7d','#7a94ad'),'blackcurrant':('#25212e','#463548','#705569'),'lingonberry':('#862f35','#b84143','#d76a57'),'cranberry':('#63232e','#933541','#b9575d')}
 for berry,colors in palettes.items():
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.line([(6,6),(8,4),(11,5)],fill='#405938');d.polygon([(5,3),(8,4),(6,5),(3,4)],fill='#719452');d.polygon([(9,4),(12,2),(13,4),(10,5)],fill='#56753e')
  for x,y in [(4,7),(8,6),(11,8),(6,10),(9,11)]:
   d.rectangle((x,y,x+2,y+2),fill=colors[0]);d.rectangle((x,y,x+1,y+1),fill=colors[1]);d.point((x,y),fill=colors[2])
   if berry=='raspberry':d.point((x+2,y),fill=colors[2]);d.point((x+1,y+2),fill=colors[2])
  save(im,A/f'textures/item/{berry}.png');js(A/f'models/item/{berry}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{berry}'}})
  for p in (A/'textures/block').glob(berry+'_bush_*.png'):
   stage=int(p.stem[-1]);upper='_upper_' in p.stem;im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
   height=3+stage*2 if stage<2 else 11;bottom=13;d.line((8,bottom,7,bottom-height),fill='#705139',width=2)
   for x,y in [(4,10),(10,9),(6,6),(10,4),(3,5)][:min(5,stage+2)]:
    if stage==0:y+=3
    d.polygon([(x-2,y),(x,y-2),(x+3,y-1),(x+2,y+2),(x-1,y+2)],fill='#3c6037');d.line((x-1,y,x+2,y),fill='#68854c')
   if stage>=3:
    for x,y in ([(4,9),(10,5),(8,11)] if stage==4 else [(6,7)]):d.rectangle((x,y,x+1,y+1),fill=colors[1]);d.point((x,y),fill=colors[2])
   if upper:im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
   save(im,p)
 # Restrained plate atlas: dark steel, raised-face highlight, bronze trim, small cyan accents.
 for layer in [1,2]:
  im=Image.new('RGBA',(64,32),'#344557');d=ImageDraw.Draw(im)
  for x,y,w,h in [(0,0,16,12),(16,0,16,12),(40,0,20,12),(0,16,16,16),(16,16,24,16),(40,16,20,16)]:
   d.rectangle((x,y,x+w-1,y+h-1),fill='#536c82');d.line((x,y,x+w-1,y),fill='#a49664');d.line((x,y,x,y+h-1),fill='#849baa');d.line((x+w-1,y+1,x+w-1,y+h-1),fill='#253442')
  for x,y in [(23,5),(46,6),(6,23)]:d.rectangle((x,y,x+1,y+2),fill='#72adb5')
  save(im,A/f'textures/models/armor/perunite_layer_{layer}.png')
 shaped('kitchen_table',['PPP','PCP','S S'],{'P':'#minecraft:planks','C':'minecraft:chest','S':'minecraft:stick'})
 shaped('dark_bottle',[' G ','GSG',' G '],{'G':'minecraft:glass','S':'minecraft:stick'},2)
 shaped('small_keg',['PNP','P P','PSP'],{'P':'#minecraft:planks','N':'minecraft:iron_nugget','S':'minecraft:stick'})
 shaped('fruit_press',['SIS','P P','PPP'],{'S':'minecraft:stick','I':'minecraft:iron_ingot','P':'#minecraft:planks'})
 shaped('fermentation_vat',['PIP','P P','PIP'],{'P':'#minecraft:planks','I':'minecraft:iron_ingot'})
 js(D/'worldgen/configured_feature/giant_pine_sapling.json',{'type':'slavicmyths:pine_tree','config':{'sapling':True,'giant':True}})
 # Fixed values, never multiply already-polished data when rerunning generators.
 placements={'bandit_camp_large':(320,238),'kurgan_small':(50,30),'kurgan_warrior':(160,110),'kurgan_great':(320,238)}
 for id,(spacing,separation) in placements.items():
  p=D/f'worldgen/structure_set/{id}.json';v=json.loads(p.read_text());v['placement'].update(spacing=spacing,separation=separation)
  # Shared candidates prevent independent tier clusters. Approximate previous 1/spacing^2
  # tier rarity by 41:4:1; retain legacy set IDs as inactive data for compatibility.
  v['placement'].pop('exclusion_zone',None)
  if id=='kurgan_small':
   v['structures']=[{'structure':'slavicmyths:kurgan_'+tier,'weight':weight}for tier,weight in [('small',41),('warrior',4),('great',1)]]
   v['placement'].pop('frequency',None)
  elif id.startswith('kurgan_'):v['placement']['frequency']=0.0
  js(p,v)
 p=D/'worldgen/placed_feature/apple_tree.json';v=json.loads(p.read_text());v['placement'][0]['chance']=6;js(p,v)
 labels={'kitchen_table':('Кухонный стол','Kitchen Table'),'tools':('Инструменты','Tools'),'ingredients':('Ингредиенты','Ingredients'),'cooking':('Готовка','Cooking'),'result':('Результат','Result'),'container_returns':('Возврат тары','Container Returns'),'pot':('Горшок','Pot'),'servings':('Порций: %s / %s','Servings: %s / %s'),'inventory':('Инвентарь','Inventory')}
 for lang,n in [('ru_ru',0),('en_us',1)]:
  p=A/f'lang/{lang}.json';v=json.loads(p.read_text(encoding='utf-8'))
  v.update(json.loads((ROOT/'tools/integration_rune_text.json').read_text(encoding='utf-8'))[lang])
  for k,t in labels.items():v['screen.slavicmyths.'+k]=t[n]
  v['key.slavicmyths.flight_down']=('Снижение на ступе/метле','Descend on Mortar/Broom')[n]
  v['block.slavicmyths.produce_crate']=('Ящик','Produce Crate')[n];v['item.slavicmyths.produce_crate']=v['block.slavicmyths.produce_crate']
  v['rpg.slavicmyths.incompatible_rune']=('Эта руна несовместима с предметом','This rune is incompatible with this item')[n]
  if lang=='ru_ru':
   for k,t in list(v.items()):
    try:
     corrected=t.encode('cp1251').decode('utf-8')
     if len(corrected)<len(t):v[k]=corrected
    except UnicodeError:pass
   v['item.slavicmyths.rye_flour']='Ржаная мука';v['item.slavicmyths.rye_bread']='Ржаной хлеб'
  js(p,v)
 # Keep the current village assets/localization intact when older resource generators run.
 from beer_models_125 import main as beer_models
 beer_models()
 from village_resources import main as village_assets
 village_assets()
 from rare_weapon_resources_1322 import main as rare_weapon_assets
 rare_weapon_assets()
 from class_ui_resources_1323 import main as class_ui_assets
 class_ui_assets()
 from armor_rune_resources_133 import main as armor_rune_text
 armor_rune_text()
if __name__=='__main__':main()
