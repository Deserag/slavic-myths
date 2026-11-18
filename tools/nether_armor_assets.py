"""Authored technical geometry from written lock; ART_BLOCKED_REFERENCE, not original projection."""
from pathlib import Path
import json,math,random,zipfile
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data'
IDS=['zhara_helmet','zhara_chestplate','zhara_leggings','zhara_boots','infernal_crown_helmet','infernal_king_cuirass','infernal_king_leggings','infernal_king_boots']
RU=['Жаровой алатырный шлем','Жаровая алатырная кираса','Жаровые алатырные поножи','Жаровые алатырные сапоги','Венец Пекельного царя','Кираса Пекельного царя','Поножи Пекельного царя','Сапоги Пекельного царя']
EN=['Zhara Alatyr Helmet','Zhara Alatyr Chestplate','Zhara Alatyr Leggings','Zhara Alatyr Boots','Infernal King Crown','Infernal King Cuirass','Infernal King Leggings','Infernal King Boots']
def write(p,j):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(j,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def tag(name,values):
 p=D/'minecraft/tags/item'/f'{name}.json';j=json.loads(p.read_text(encoding='utf-8')) if p.exists() else {'replace':False,'values':[]};j['values']=list(dict.fromkeys(j['values']+['slavicmyths:'+v for v in values]));write(p,j)
def model():
 parts=[]
 def box(n,parent,slot,p,s,mat='metal',off=None,rot=None):parts.append(dict(name=n,parent=parent,slot=slot,pivot=p,size=s,material=mat,offset=off or [-v/2 for v in s],rotation=rot or [0,0,0]))
 box('helmet_shell','head','head',[0,-4,0],[9,9,9]);box('crown_band','head','head',[0,-8.3,0],[10,2,10],'gold');box('brow_bridge','head','head',[0,-5,-4.8],[8,1.2,1],'gold');box('central_red_gem','head','head',[0,-8.7,-5.3],[2,4,1],'ruby')
 for i in range(9):
  a=i*math.pi*2/9;h=[6,5,3,4,2.5,3,4.5,3.5,5][i];box('crown_spire_'+str(i),'head','head',[math.sin(a)*4,-9-h/2,-math.cos(a)*4],[1,h,1],'gold');box('spire_tip_'+str(i),'head','head',[math.sin(a)*4,-9-h,-math.cos(a)*4],[1,1,1],'ember')
 for side in [-1,1]:box('cheek_'+str(side),'head','head',[side*3.8,-1.5,-4.8],[1.2,4,1],'gold')
 box('visor_horizontal','head','head',[0,-3.5,-4.7],[6,1,1],'ember');box('visor_vertical','head','head',[0,-1.5,-4.7],[1,5,1],'ember')
 box('backplate','body','chest',[0,5,2.7],[9,11,1.5]);box('belly_plate','body','chest',[0,8,-3],[7.5,4,3]);box('waist_belt','body','chest',[0,10.5,0],[9.5,2,6],'gold');box('belt_buckle','body','chest',[0,10.5,-3.8],[2.5,2,1],'ruby')
 for side in [-1,1]:
  box('pectoral_'+str(side),'body','chest',[side*2.2,3,-3.6],[4.4,5,3.5]);box('breast_gold_'+str(side),'body','chest',[side*2.2,.6,-4.8],[4.3,1,1],'gold')
  for i in range(3):box('rib_'+str(side)+'_'+str(i),'body','chest',[side*3.5,4.5+i*1.5,-2.8],[2.4,1.4,2.6]);box('rib_heat_'+str(side)+'_'+str(i),'body','chest',[side*(1.5+i*.8),3+i*1.4,-5.4],[1.5,.7,.4],'ember')
  parent='left_arm' if side>0 else 'right_arm'
  for i in range(3):box('pauldron_'+str(side)+'_'+str(i),parent,'chest',[side*1,-1+i*1.3,0],[8-i*.7,2,7-i*.5],'gold' if i==1 else 'metal')
  box('upper_guard_'+str(side),parent,'chest',[side,3,0],[5,3,5]);box('bracer_'+str(side),parent,'chest',[side,7,0],[5,4.5,5],'metal');box('cuff_'+str(side),parent,'chest',[side,9,0],[5.4,1,5.4],'gold')
 box('sternum_seal','body','chest',[0,2.5,-5.6],[2.2,3.5,1],'ruby')
 box('tabard_root','body','chest',[0,11,-4.1],[4,.5,.5],'cloth',off=[-2,0,-.25])
 for i in range(2):
  parent='tabard_root' if i==0 else 'tabard_segment_0';box('tabard_segment_'+str(i),parent,'chest',[0,0 if i==0 else 6,0],[4,6.5,.5],'cloth',off=[-2,0,-.25]);box('tabard_rune_'+str(i),'tabard_segment_'+str(i),'chest',[0,3,-.4],[.7,4,.3],'gold')
 box('cape_root','body','chest',[0,.3,3.7],[12,.5,.5],'cloth',off=[-6,0,0])
 for i in range(3):
  parent='cape_root' if i==0 else 'cape_segment_'+str(i-1);box('cape_segment_'+str(i),parent,'chest',[0,0 if i==0 else 7.8,.2],[12-i*.3,8.5,.5],'cloth',off=[-(12-i*.3)/2,0,0])
  for side in [-1,1]:box('cape_border_'+str(i)+'_'+str(side),'cape_segment_'+str(i),'chest',[side*(5.5-i*.15),4,.55],[.7,8.5,.3],'gold')
  box('cape_rune_'+str(i),'cape_segment_'+str(i),'chest',[0,4,.65],[1,5,.3],'gold');box('cape_rune_arm_'+str(i),'cape_segment_'+str(i),'chest',[0,3,.65],[4,.8,.3],'gold')
 box('cape_hem','cape_segment_2','chest',[0,8,.6],[11.3,.8,.5],'gold')
 box('leggings_belt','body','legs',[0,10,0],[8.8,2.5,5],'gold')
 for side in [-1,1]:
  parent='left_leg' if side>0 else 'right_leg';offset=side*.4
  box('thigh_'+str(side),parent,'legs',[offset,3,0],[3.8,6,4.7]);box('knee_'+str(side),parent,'legs',[offset,7,-2.8],[4,3,1.6],'gold');box('knee_core_'+str(side),parent,'legs',[offset,7,-3.7],[1.5,2,.4],'ruby');box('leg_line_'+str(side),parent,'legs',[offset,3,-2.6],[.6,4,.4],'ember')
  box('shin_'+str(side),parent,'feet',[offset,8.5,0],[4,5,5]);box('ankle_'+str(side),parent,'feet',[offset,8,0],[4.4,1.2,5.4],'gold');box('boot_toe_'+str(side),parent,'feet',[offset,11,-1],[4.2,2,7]);box('toe_step_'+str(side),parent,'feet',[offset,10.5,-3.5],[4.4,1.5,2],'gold');box('boot_core_'+str(side),parent,'feet',[offset,9,-2.7],[2,1,.4],'ember')
 atlas=256;u=v=row=0
 for p in parts:
  x,y,z=p['size'];w,h=math.ceil(2*(x+z)),math.ceil(y+z)
  if u+w+2>atlas:u=0;v+=row+2;row=0
  assert v+h+2<=atlas;p['uv']=[u,v];p['rect']=[w,h];u+=w+2;row=max(row,h)
 write(A/'nether_armor_rigs/infernal_king.json',dict(atlas=atlas,parts=parts))
 image=Image.new('RGBA',(atlas,atlas));glow=Image.new('RGBA',(atlas,atlas));pal={'metal':(34,29,27),'gold':(158,108,35),'ruby':(191,37,22),'cloth':(63,16,28),'ember':(245,105,20)}
 for i,p in enumerate(parts):
  u,v=p['uv'];w,h=p['rect'];rng=random.Random(p['name']);base=pal[p['material']]
  for yy in range(h):
   for xx in range(w):
    noise=((xx//3+yy//3+i)%3-1)*5;col=tuple(max(0,min(255,c+noise)) for c in base);image.putpixel((u+xx,v+yy),(*col,255))
    if p['material'] in ['ruby','ember']:glow.putpixel((u+xx,v+yy),(*col,255))
 for name in IDS[4:]:
  f=A/f'textures/armor/nether/{name}.png';f.parent.mkdir(parents=True,exist_ok=True);image.save(f);glow.save(f.with_name(name+'_glow.png'))
 manifest=R/'docs/implementation/2026-10-29-hell-remaining/armor_model_parts.tsv';manifest.write_text('part\tparent\tslot\tpivot\tsize\tmaterial\tuv\n'+'\n'.join('\t'.join(str(p[k]) for k in ['name','parent','slot','pivot','size','material','uv']) for p in parts)+'\n',encoding='utf-8')
def textures():
 jar=Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
 with zipfile.ZipFile(jar) as z:
  import io
  for i,name in enumerate(IDS[:4]):
   im=Image.open(io.BytesIO(z.read('assets/minecraft/textures/models/armor/diamond_layer_'+('2' if i==2 else '1')+'.png'))).convert('RGBA');px=im.load()
   for y in range(im.height):
    for x in range(im.width):
     r,g,b,a=px[x,y]
     if a:val=int((r+g+b)/3);px[x,y]=(max(18,val//5),max(13,val//7),max(11,val//9),a)
   d=ImageDraw.Draw(im);d.line([(17,18),(21,23),(24,24),(28,19)],fill=(252,127,23,255),width=1);d.line((2,8,6,8,8,11),fill=(255,139,28,255),width=1);d.line((42,18,44,21,46,18),fill=(241,105,20,255),width=1);d.line((2,22,5,26,6,29),fill=(241,105,20,255),width=1)
   folder=A/'textures/armor/nether';folder.mkdir(parents=True,exist_ok=True);im.save(folder/(name+'.png'));glow=Image.new('RGBA',im.size);glow.putdata([c if c[0]>180 and c[1]>60 else (0,0,0,0) for c in im.getdata()]);glow.save(folder/(name+'_glow.png'))
def icons():
 for i,name in enumerate(IDS):
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);base='#302725';gold='#b17a2c' if i>=4 else '#ee8120';ruby='#f1411b';slot=i%4
  if slot==0:
   d.polygon([(2,5),(4,3),(11,3),(13,5),(13,12),(10,12),(10,8),(5,8),(5,12),(2,12)],fill=base);d.line((3,6,12,6),fill=gold,width=2)
   if i>=4:
    for x,h in [(2,3),(5,2),(8,0),(11,2),(13,3)]:d.rectangle((x,h,x+1,5),fill=gold)
    d.rectangle((7,3,8,6),fill=ruby)
  elif slot==1:d.polygon([(1,4),(5,2),(6,5),(9,5),(10,2),(14,4),(12,8),(11,8),(11,14),(4,14),(4,8),(2,8)],fill=base);d.line((3,4,7,8,12,4),fill=gold,width=2);d.rectangle((7,6,8,9),fill=ruby)
  elif slot==2:d.rectangle((3,2,12,6),fill=base);d.rectangle((3,6,6,14),fill=base);d.rectangle((9,6,12,14),fill=base);d.rectangle((3,9,6,11),fill=gold);d.rectangle((9,9,12,11),fill=gold)
  else:d.rectangle((2,3,5,12),fill=base);d.rectangle((10,3,13,12),fill=base);d.rectangle((1,11,6,14),fill=base);d.rectangle((9,11,14,14),fill=base);d.line((1,12,6,12),fill=gold,width=2);d.line((9,12,14,12),fill=gold,width=2)
  p=A/f'textures/item/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p);write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
def data():
 for i,name in enumerate(IDS):
  base=['helmet','chestplate','leggings','boots'][i%4];cat='zhara_plate' if i<4 else ['four_faces_crown_core','infernal_armor_heart','navya_armor_weave','blazing_step_seal'][i-4]
  write(D/f'slavicmyths/recipe/{name}.json',{'type':'minecraft:smithing_transform','template':{'item':'slavicmyths:nether_hardening_template'},'base':{'item':'minecraft:diamond_'+base},'addition':{'item':'slavicmyths:'+cat},'result':{'id':'slavicmyths:'+name,'count':1}})
  write(D/f'slavicmyths/advancement/recipes/nether_armor/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'has_base':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'minecraft:diamond_'+base}]}}},'requirements':[['has_base']],'rewards':{'recipes':['slavicmyths:'+name]}})
  tag('enchantable/'+['head_armor','chest_armor','leg_armor','foot_armor'][i%4],[name])
 for t in ['enchantable/armor','enchantable/durability','trimmable_armor']:tag(t,IDS)
 p=A/'creative_catalog.tsv';lines=p.read_text(encoding='utf-8').splitlines();known={l.split('\t')[0] for l in lines}
 for name in IDS:
  if name not in known:lines.append(name+'\tarmor\tnether_equipment')
 p.write_text('\n'.join(lines)+'\n',encoding='utf-8')
 effects_ru=['Улучшение алмазной основы; ремонт алатырским слитком.']*4+['+20% допустимого прямого урона.','−25% обычного входящего урона.','+15% скорости; полный сет +20% суммарно.','+30% силы прыжка и мягкое падение.']
 effects_en=['Diamond upgrade; repair with Alatyr ingots.']*4+['+20% eligible direct damage.','−25% ordinary incoming damage.','+15% speed; full set +20% total.','+30% jump strength and slow descent.']
 for lang,names,effects in [('ru_ru',RU,effects_ru),('en_us',EN,effects_en)]:
  p=A/f'lang/{lang}.json';j=json.loads(p.read_text(encoding='utf-8'))
  for i,name in enumerate(IDS):j['item.slavicmyths.'+name]=names[i];j['netherarmor.effect.'+name]=effects[i]
  j.update({'netherarmor.hp':'Здоровье: +%s сердца' if lang=='ru_ru' else 'Health: +%s hearts','netherarmor.sockets':'Рунные гнёзда: %s / максимум %s' if lang=='ru_ru' else 'Rune sockets: %s / maximum %s'});write(p,j)
def main():model();textures();icons();data();print('8 armor items/resources + actual royal ModelParts; ART original match BLOCKED_REFERENCE')
if __name__=='__main__':main()
