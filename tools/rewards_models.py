from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
root=ROOT/'src/main/resources/assets/slavicmyths/models/item'
p=ROOT/'src/main/java/org/slavicmyths/client/ClientSetup.java'
s=p.read_text(encoding='utf-8').replace('org.slavicmyths.equipment.UniqueEquipment.WARD_SHIELD.get() })','org.slavicmyths.equipment.UniqueEquipment.WARD_SHIELD.get(),org.slavicmyths.rewards.RewardContent.item("gift_ilya_shield") })')
p.write_text(s,encoding='utf-8')
p=root/'gift_ilya_shield.json';d=json.loads(p.read_text())
d['display']={'firstperson_lefthand':{'rotation':[0,180,5],'translation':[2,0,-1],'scale':[1,1,1]},'thirdperson_lefthand':{'rotation':[0,180,0],'translation':[0,3,0],'scale':[.9,.9,.9]}}
d['overrides']=[{'predicate':{'blocking':1},'model':'slavicmyths:item/gift_ilya_shield_blocking'}]
p.write_text(json.dumps(d,indent=2)+'\n')
faces={face:{'uv':[0,0,16,16],'texture':'#front'} for face in ['north','south','east','west','up','down']}
d={'textures':{'front':'slavicmyths:item/gift_ilya_shield','particle':'slavicmyths:item/gift_ilya_shield'},'elements':[{'from':[3,1,7],'to':[13,15,8],'faces':faces},{'from':[4,0,6.8],'to':[12,16,7.2],'faces':faces}]}
d['display']={}
for hand,rotation,translation in [('lefthand',[0,180,-5],[5,2,-3]),('righthand',[0,0,5],[-5,2,-3])]:
 d['display']['firstperson_'+hand]={'rotation':rotation,'translation':translation,'scale':[1.2,1.2,1.2]}
 d['display']['thirdperson_'+hand]={'rotation':rotation,'translation':[0,3,0],'scale':[1,1,1]}
(root/'gift_ilya_shield_blocking.json').write_text(json.dumps(d,indent=2)+'\n')
p=root/'trophy_kalin_sabre.json';d=json.loads(p.read_text());d['parent']='minecraft:item/handheld';p.write_text(json.dumps(d,indent=2)+'\n')
from PIL import Image
for id,palette in [('trophy_idol_chest',['#493524','#806544','#ddd7c7','#e7d4ab']),('trophy_marinka_cloak',['#1e1a24','#39323a','#5c2035','#981e43']),('trophy_kartaus_mantle',['#15161b','#343238','#625a61','#901921'])]:
 im=Image.new('RGBA',(128,128));px=im.load()
 for y in range(128):
  for x in range(128):
   tile=2 if id=='trophy_idol_chest' and x>=40 and y<32 else 3 if id=='trophy_kartaus_mantle' and x>=60 and y>=48 else 0 if y<48 else 1
   px[x,y]=tuple(bytes.fromhex(palette[tile][1:]))+(255,)
 dest=ROOT/'src/main/resources/assets/slavicmyths/textures/models/armor'/(id+'.png');dest.parent.mkdir(parents=True,exist_ok=True);im.save(dest)

from weapon_patch_assets import main as weapon_patch
weapon_patch()

# Preserve current armor after both the original and weapon overlays.
from armor_revision_assets import apply as armor_revision
armor_revision()
