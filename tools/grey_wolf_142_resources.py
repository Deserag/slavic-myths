"""Original clustered pixel sprites and GUI slices; never crops approved posters."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,random
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths';D=ROOT/'src/main/resources/data';P={'black':'#30343A','graphite':'#464A51','steel':'#707782','silver':'#A1A7AA','light':'#C7C5B6','darkwood':'#563E2C','wood':'#856343','ash':'#B88D60','leather':'#64452E','pale':'#9C7B53','amber':'#DD9B38','bright':'#F2C368'}
def write(path,data):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def save(im,path):path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
def canvas():return Image.new('RGBA',(32,32))
def ring():
 im=canvas();d=ImageDraw.Draw(im);d.polygon([(7,14),(6,23),(12,29),(22,29),(27,24),(25,15),(23,13),(9,13)],fill=P['black']);d.polygon([(9,15),(8,23),(13,27),(21,27),(25,23),(23,16)],fill=P['steel']);d.polygon([(12,19),(11,23),(14,25),(20,25),(22,22),(21,18)],fill=(0,0,0,0));d.line([(8,19),(8,23),(13,27),(18,27)],fill=P['silver'],width=1)
 d.polygon([(9,8),(10,3),(13,3),(14,7),(18,7),(19,3),(22,3),(23,8),(24,12),(22,15),(19,18),(15,17),(11,14)],fill=P['black']);d.polygon([(11,7),(12,5),(14,9),(18,9),(20,5),(21,8),(22,13),(19,16),(16,15),(12,13)],fill=P['graphite']);d.polygon([(12,12),(15,12),(17,14),(19,13),(21,12),(21,15),(18,17),(15,15)],fill=P['silver']);d.rectangle((16,14,18,15),fill=P['black']);d.point((13,11),fill=P['amber']);d.point((20,11),fill=P['amber']);d.point((24,16),fill=P['bright']);d.line([(10,7),(11,4),(12,4)],fill=P['light']);return im
def bow(frame):
 im=canvas();d=ImageDraw.Draw(im);bend=frame*1;upper=(25-bend,3+frame//2);lower=(5+frame,29-frame//2);points=[lower,(3+frame,25),(3+frame,21),(6,18),(11,17),(11,13),(14,9),(19-frame,5),upper];d.line(points,fill=P['darkwood'],width=5);d.line([(x-1,y)for x,y in points],fill=P['wood'],width=3);d.line([(x-1,y-1)for x,y in points],fill=P['ash'],width=1)
 mid=(16+2*frame,19+frame);d.line([lower,mid,upper],fill=P['light'],width=1);d.polygon([(9,16),(11,15),(14,18),(12,20)],fill=P['darkwood']);d.line([(9,16),(12,20)],fill=P['steel'],width=2);d.point((10,17),fill=P['silver']);d.polygon([(21-frame,4),(22-frame,2),(25-frame,2),(27-frame,4),(25-frame,6),(23-frame,6)],fill=P['steel']);d.line([(22-frame,3),(25-frame,3)],fill=P['silver']);d.polygon([(25-frame,4),(27-frame,4),(26-frame,6)],fill=P['light']);d.point((24-frame,4),fill=P['amber']);return im
def quiver():
 im=canvas();d=ImageDraw.Draw(im)
 for x in [14,18,22,25]:d.line([(x,3),(x-2,15)],fill=P['ash'],width=1);d.polygon([(x-2,2),(x+1,2),(x,6),(x-2,5)],fill=P['light']);d.point((x,2),fill=P['amber'] if x in [18,25] else P['silver'])
 d.polygon([(10,12),(25,12),(24,28),(11,31),(6,26)],fill='#3F2C21');d.polygon([(11,14),(23,14),(22,27),(12,29),(8,25)],fill='#785638');d.polygon([(20,14),(24,13),(23,27),(20,28)],fill='#513B2C');d.line([(11,15),(9,24),(12,28)],fill=P['pale'],width=1);d.polygon([(11,9),(23,9),(26,11),(24,14),(10,14),(9,11)],fill=P['leather']);d.polygon([(12,10),(22,10),(24,11),(22,12),(11,12)],fill='#3F2C21');d.line([(10,10),(12,9),(22,9)],fill=P['pale'],width=1);d.line([(7,17),(12,23),(16,27)],fill='#3F2C21',width=2);d.rectangle((10,21,13,24),outline=P['steel']);d.polygon([(13,19),(16,20),(18,19),(20,19),(18,22),(16,23),(14,21)],fill=P['silver']);d.point((16,21),fill=P['light']);return im
def quest(i):
 im=canvas();d=ImageDraw.Draw(im)
 if i==0:
  d.polygon([(7,4),(25,4),(26,8),(23,9),(23,26),(7,26),(5,23),(8,22),(8,9),(5,8)],fill='#30343A');d.rectangle((9,6,22,24),fill='#C8B38D');d.line([(10,8),(21,8)],fill='#806B4B',width=2);d.line([(10,22),(21,22)],fill='#806B4B',width=2);d.polygon([(18,10),(19,12),(15,18),(13,19),(14,16)],fill=P['graphite']);d.line([(12,18),(15,20)],fill=P['darkwood'],width=2)
 elif i==1:
  d.polygon([(3,27),(3,22),(7,22),(7,17),(11,17),(11,12),(22,12),(22,17),(26,17),(26,22),(29,22),(29,27)],fill=P['black']);d.rectangle((7,19,25,25),fill=P['steel']);d.rectangle((10,14,22,20),fill=P['graphite']);d.rectangle((14,19,19,27),fill='#17191B');d.rectangle((11,11,22,13),fill='#58604A');d.point((11,10),fill='#698868');d.point((20,10),fill='#698868')
 elif i==2:
  d.polygon([(3,16),(8,10),(16,6),(24,10),(29,16),(24,22),(16,26),(8,22)],fill=P['black']);d.polygon([(6,16),(11,12),(16,10),(22,12),(26,16),(22,20),(16,22),(11,20)],fill=P['light']);d.rectangle((13,12,19,20),fill=P['steel']);d.rectangle((15,13,17,19),fill=P['black']);d.point((14,12),fill=P['silver'])
 else:
  d.rectangle((7,14,25,28),fill=P['black']);d.arc((9,3,23,23),180,360,fill=P['steel'],width=3);d.rectangle((8,15,24,27),fill=P['graphite']);d.rectangle((15,18,18,24),fill=P['black']);d.point((16,18),fill=P['silver'])
 return im
def apply():
 save(ring(),A/'textures/item/ring_of_grey_pack.png');save(quiver(),A/'textures/item/finist_quiver.png')
 for i,name in enumerate(['finist_bow','finist_bow_pulling_0','finist_bow_pulling_1','finist_bow_pulling_2']):save(bow(i),A/f'textures/item/{name}.png');write(A/f'models/item/{name}.json',{'parent':'minecraft:item/bow','textures':{'layer0':f'slavicmyths:item/{name}'}})
 b=json.loads((A/'models/item/finist_bow.json').read_text());b['overrides']=[{'predicate':{'pulling':1,'pull':p},'model':f'slavicmyths:item/finist_bow_pulling_{i}'}for i,p in enumerate([0,.65,.9])];write(A/'models/item/finist_bow.json',b)
 for name in ['ring_of_grey_pack','finist_quiver']:write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
 for slot,name in [('ring','ring_of_grey_pack'),('belt','finist_quiver')]:
  path=D/f'curios/tags/item/{slot}.json';data=json.loads(path.read_text());value='slavicmyths:'+name
  if value not in data['values']:data['values'].append(value)
  write(path,data)
 for tag in ['bow','durability']:
  path=D/f'minecraft/tags/item/enchantable/{tag}.json';data=json.loads(path.read_text(encoding='utf-8'))if path.exists()else{'replace':False,'values':[]}
  additions=['slavicmyths:short_bow','slavicmyths:heavy_bow','slavicmyths:finist_bow']if tag=='bow'else['slavicmyths:finist_bow']
  data['values']=list(dict.fromkeys(data['values']+additions));write(path,data)
 for name in ['finist_inverted_heal_harm','finist_immune_to_effect']:write(D/f'slavicmyths/tags/entity_type/{name}.json',{'replace':False,'values':[]})
 # ArrowRenderer expects 32x32 UV layout; authored native arrow shaft and feather bands.
 arrow=canvas();d=ImageDraw.Draw(arrow);d.rectangle((0,0,15,4),fill=P['light']);d.rectangle((1,1,13,2),fill=P['silver']);d.rectangle((0,0,1,3),fill=P['amber']);d.rectangle((13,0,15,3),fill=P['black']);d.rectangle((0,5,4,9),fill=P['light']);d.rectangle((1,6,3,8),fill=P['silver']);save(arrow,A/'textures/entity/finist_magic_arrow.png')
 marker=canvas();d=ImageDraw.Draw(marker);d.line([(5,10),(15,24),(27,7)],fill=P['silver'],width=3);d.line([(9,10),(15,16),(22,9)],fill=P['amber'],width=2);save(marker,A/'textures/item/finist_target_marker.png');status=canvas();sd=ImageDraw.Draw(status);sd.polygon([(5,15),(7,3),(13,8),(19,8),(25,3),(27,15),(24,25),(16,30),(8,25)],fill=P['black']);sd.polygon([(9,15),(12,20),(16,25),(21,20),(24,15),(20,15),(16,19),(12,15)],fill=P['silver']);sd.point((10,13),fill=P['amber']);sd.point((22,13),fill=P['amber']);save(status,A/'textures/item/ring_wolf_status.png')
 worn=Image.new('RGBA',(64,64),P['leather']);d=ImageDraw.Draw(worn);d.rectangle((0,0,22,14),fill='#785638');d.rectangle((0,16,28,24),fill='#3F2C21');d.line([(0,16),(28,16)],fill=P['pale']);d.rectangle((32,0,40,5),fill=P['silver']);d.rectangle((32,8,42,14),fill='#3F2C21');d.rectangle((32,16,37,24),fill=P['ash']);d.rectangle((40,16,46,22),fill=P['light']);save(worn,A/'textures/armor/finist_quiver_worn.png')
 gui=A/'textures/gui/grey_wolf'
 for i in range(3 if (A/'cursed_volkolak_geometry.json').exists() else 4):save(quest(i),gui/f'quest_{i}.png')
 frame=Image.new('RGBA',(32,32),'#26282C');d=ImageDraw.Draw(frame);d.rectangle((0,0,31,31),outline='#17191B',width=1);d.line([(1,30),(1,1),(30,1)],fill='#777A7A');d.rectangle((4,4,27,27),fill='#30343A');save(frame,gui/'frame.png')
 paper=Image.new('RGBA',(32,32),'#D8C8A4');d=ImageDraw.Draw(paper);d.rectangle((0,0,31,31),outline='#AA936E');r=random.Random(142)
 for i in range(28):x=r.randrange(2,29);y=r.randrange(2,29);d.rectangle((x,y,x+1,y),fill='#D2C19A')
 save(paper,gui/'parchment.png')
 row=Image.new('RGBA',(32,32),'#30343A');d=ImageDraw.Draw(row);d.line([(0,0),(31,0)],fill='#464A51');d.line([(0,31),(31,31)],fill='#17191B');save(row,gui/'quest_row.png');save(row,gui/'button.png');paw=Image.new('RGBA',(8,8));pd=ImageDraw.Draw(paw);pd.rectangle((2,4,5,6),fill=P['silver']);pd.point((1,2),fill=P['silver']);pd.rectangle((3,1,4,2),fill=P['silver']);pd.point((6,2),fill=P['silver']);save(paw,gui/'paw.png')
 well=Image.new('RGBA',(34,34),'#26282C');d=ImageDraw.Draw(well);d.rectangle((0,0,33,33),outline='#877052');d.line([(1,32),(1,1),(32,1)],fill='#17191B');d.line([(2,32),(32,32),(32,2)],fill='#707782');save(well,gui/'reward_slot.png')
 forest=Image.new('RGBA',(96,160),'#35433C');d=ImageDraw.Draw(forest)
 for x,y,h in [(12,20,115),(54,1,151),(85,23,116)]:d.rectangle((x-2,y+15,x+2,y+h),fill='#30343A');d.polygon([(x,y),(x-14,y+35),(x-8,y+34),(x-19,y+69),(x+19,y+69),(x+9,y+34),(x+15,y+35)],fill='#26352E')
 d.rectangle((84,106,85,139),fill=P['darkwood']);d.rectangle((83,103,86,107),fill=P['amber']);save(forest,gui/'forest.png')
 text=json.loads((ROOT/'tools/grey_wolf_142_text.json').read_text(encoding='utf-8'))
 for locale in ['ru_ru','en_us']:
  path=A/f'lang/{locale}.json';existing=json.loads(path.read_text(encoding='utf-8'));existing.update({k:v for k,v in text[locale].items() if not ((A/'cursed_volkolak_geometry.json').exists() and (k.startswith('wolfstory.slavicmyths.dialog.3.') or k.startswith('wolfstory.slavicmyths.dialog.ask.3') or k.startswith('wolfstory.slavicmyths.dialog.claimed.3') or k.startswith('wolfstory.slavicmyths.dialog.accepted.3')))});write(path,existing)
 path=A/'sounds.json';existing=json.loads(path.read_text(encoding='utf-8'));existing.update(text['sounds']);write(path,existing)
if __name__=='__main__':apply()
