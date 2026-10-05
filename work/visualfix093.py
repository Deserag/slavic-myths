from pathlib import Path
r=Path('src/main/java/org/slavicmyths')
p=r/'client/BabaYagaModel.java';s=p.read_text();marker='a("chin","head"';idx=s.index(marker)
s=s[:idx]+'''a("socketL","head",-1.15F,-1.65F,-2.2F,new float[]{-.65F,-.35F,-.12F,1.3F,.7F,.15F},9,0F,0F,0F);
a("socketR","head",1.15F,-1.65F,-2.2F,new float[]{-.65F,-.35F,-.12F,1.3F,.7F,.15F},9,0F,0F,0F);
a("amberL","head",-1.15F,-1.65F,-2.38F,new float[]{-.35F,-.18F,-.05F,.7F,.36F,.1F},1,0F,0F,0F);
a("amberR","head",1.15F,-1.65F,-2.38F,new float[]{-.35F,-.18F,-.05F,.7F,.36F,.1F},1,0F,0F,0F);
a("pupilL","head",-1.15F,-1.65F,-2.45F,new float[]{-.08F,-.16F,-.05F,.16F,.32F,.1F},9,0F,0F,0F);
a("pupilR","head",1.15F,-1.65F,-2.45F,new float[]{-.08F,-.16F,-.05F,.16F,.32F,.1F},9,0F,0F,0F);
a("bonePendant","body",0F,4.3F,-2.65F,new float[]{-.35F,0F,-.25F,.7F,2F,.5F},8,0F,0F,0F);
a("bonePendantTop","bonePendant",0F,.1F,0F,new float[]{-.8F,-.3F,-.3F,1.6F,.6F,.6F},8,0F,0F,0F);
a("scarfTailL","head",-.6F,1.4F,-1.7F,new float[]{-.7F,0F,-.3F,1.4F,2.6F,.6F},4,0F,0F,.3F);
a("scarfTailR","head",.6F,1.4F,-1.7F,new float[]{-.7F,0F,-.3F,1.4F,2F,.6F},4,0F,0F,-.3F);
'''+s[idx:];p.write_text(s,'utf-8')
p=r/'yaga/YagaData.java';s=p.read_text().replace('placed=n.getBoolean("Placed")&&anchor!=null;','placed=n.getBoolean("Placed")&&anchor!=null&&npc!=null;');p.write_text(s,'utf-8')
p=Path('tools/yaga_093.py');s=p.read_text('utf-8-sig').replace("'#cebd94'];rng", "'#cebd94','#292420'];rng");p.write_text(s,'utf-8')
# Reproducible geometry reviews read the real Java model; do not generate source from temporary scripts.
p=Path('tools/yaga_review_093.py');p.write_text('''"""Offline geometry/art review, no game launch. Reads the production model/plan export."""
from pathlib import Path
import json,re,sys
from PIL import Image,ImageDraw
import world_boss_models_092 as review
R=Path(__file__).resolve().parents[1];review.OUT=R/'docs/verification/yaga-0.9.3';review.OUT.mkdir(exist_ok=True)
review.PAL=['#b8a785','#ae9b54','#827a70','#3e3433','#6d382f','#614835','#c1b498','#71483b','#cebd94','#292420']
s=(R/'src/main/java/org/slavicmyths/client/BabaYagaModel.java').read_text('utf-8')
pat=r'a\\("([^"\\n]+)","([^"\\n]*)",([^;\\n]+)\\);'
nodes=[]
for name,parent,rest in re.findall(pat,s):
 rest=rest.replace('F','');pre,tail=rest.split('null',1) if 'null' in rest else rest.split('new float[]{',1);pos=list(map(float,pre.strip(',').split(',')))
 if 'null' in rest:box=None;remaining=tail
 else:boxtext,remaining=tail.split('}',1);box=list(map(float,boxtext.split(',')))
 material,rx,ry,rz=remaining.strip(',').split(',');nodes.append(dict(name=name,parent=parent,pivot=pos,box=box,mat=int(material),rot=list(map(float,[rx,ry,rz]))))
assert len(nodes)>=75
review.review('baba_yaga',nodes);(review.OUT/'baba-yaga-geometry.json').write_text(json.dumps(nodes,indent=2),'utf-8')
items=['putevodny_klubok','svyazka_trav_yagi','otvar_ochishcheniya','letuchaya_maz','otvar_lesnoy_zorkosti','yagin_nastoy_stoykosti','otvar_bodrosti','lesnoy_nastoy_vosstanovleniya'];im=Image.new('RGBA',(1024,512),'#26231f');d=ImageDraw.Draw(im)
for i,id in enumerate(items):
 icon=Image.open(R/('src/main/resources/assets/slavicmyths/textures/item/'+id+'.png')).resize((200,200),Image.Resampling.NEAREST);x=(i%4)*256+28;y=(i//4)*256;im.alpha_composite(icon,(x,y));d.text((x-10,y+210),id,fill='#ead5a5')
im.save(review.OUT/'items.png')
print('Reviewed',len(nodes),'production model parts and 8 item sprites; offline only.')
''','utf-8')
