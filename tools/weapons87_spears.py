"""#171/#192: five ordinary spear held models; retain existing GUI pixels and accepted tips."""
from pathlib import Path
import argparse,copy,hashlib,json
from PIL import Image,ImageDraw
import weapons87_flails as f
from weapons87_grips import centered
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/weapons87';D=O/'blueprints/spears'
MATERIALS=['gold','iron','diamond','perunite','netherite']
def held(material):
 b=lambda lo,hi,m=material:f.box(lo,hi,m)
 width={'gold':1.5,'iron':1.6,'diamond':1.65,'perunite':1.75,'netherite':1.8}[material]
 parts=[b([7.4,-10,7.4],[8.6,22.5,8.6],'wood'),b([7.2,-10,7.2],[8.8,-9.1,8.8]),b([7.1,21.3,7.1],[8.9,24,8.9]),b([8-width,24,7.3],[8+width,26.1,8.7]),b([8-width*.72,26.1,7.4],[8+width*.72,28,8.6]),b([7.4,28,7.6],[8.6,29.2,8.4]),b([7.82,29.2,7.8],[8.18,30,8.2])]
 for y in [3.5,5.7,7.9,19.7]:parts.append(b([7.25,y,7.25],[8.75,y+.65,8.75],'leather'))
 display={'ground':{'translation':[0,3,0],'scale':[.25]*3},'fixed':{'rotation':[0,0,-30],'scale':[.4]*3}}
 for left in [False,True]:
  side='lefthand' if left else 'righthand';display['thirdperson_'+side]=centered([8,6,8],[55,180 if left else 0,0],[.7]*3,left)
  display['firstperson_'+side]=centered([8,6,8],[0,-115 if left else -65,-8 if left else 8],[.65]*3,left,target=(-1 if left else 1,-1,-2))
 return {'textures':{'surface':'slavicmyths:item/weapons87_materials','particle':'slavicmyths:item/weapons87_materials'},'elements':parts,'display':display}
def apply():return main(True)
def main(apply=False):
 D.mkdir(parents=True,exist_ok=True);dest=A if apply else D/'draft-assets';owned=[];plans=[]
 if not apply and (D/'draft-manifest.json').exists():raise RuntimeError('Before-production blueprints already exist; preserve them.')
 if not apply:
  import weapons87_geometry as g
 for material in MATERIALS:
  id=material+'_spear';model=held(material);assets={id:{'gui_light':'front','loader':'neoforge:separate_transforms','base':{'parent':'slavicmyths:item/'+id+'_held'},'perspectives':{'gui':{'parent':'slavicmyths:item/'+id+'_icon'}}},id+'_held':model,id+'_icon':{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+id}}}
  for name,data in assets.items():
   p=dest/'models/item'/(name+'.json');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n');owned.append(p)
  if not apply:
   before=g.model('slavicmyths:item/'+id,'thirdperson_righthand');old=g.faces(before);new=g.faces(model);board=Image.new('RGB',(1500,700),'#253039');draw=ImageDraw.Draw(board);draw.text((10,8),id+' #192 PROPOSAL before production / actual draft JSON+UV, same scale, no auto-fit',fill='white')
   for j,angle in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
    for after,faces in [(False,old),(True,new)]:
     x=(j*2+int(after))*250;image=g.project(faces,245,48,angle);board.paste(image,(x,60),image);draw.text((x+3,40),('DRAFT' if after else 'ACTUAL BEFORE')+' '+['front','profile','3-4'][j],fill='white')
   gui=g.model('slavicmyths:item/'+id);display=gui.get('display',{}).get('gui',{});icon=g.project(g.faces(gui),16,16,display.get('rotation',[0,0,0]),display.get('scale',[1,1,1]),display.get('translation',[0,0,0]));board.paste(icon,(20,365),icon);big=icon.resize((128,128),Image.Resampling.NEAREST);board.paste(big,(60,335),big)
   text=['KEEP actual GUI sprite at1x/8x; no PNG changes','Held extents: length40, shaft1.2x1.2, stepped socketed broad tip; thickness1.4','Authored grip (8,6,8); player height32 model units; TP scale0.7 => length28','TP forward tilt55deg; both physical hands grip-centered by native ItemTransform math','Heavy spear point distinct from light sulitsa; accepted14 tip assets are QA-only','Stone/silver40-unit models KEEP; ancient/restored46-unit historical models KEEP','No damage/reach/recipe/ID changes. Runtime game view OWNER_PENDING.']
   for k,line in enumerate(text):draw.text((220,350+k*35),line,fill='white')
   board.save(D/(id+'.png'));plans.append({'id':id,'decision':'FIX_HELD_KEEP_GUI','grip':[8,6,8],'shaft_length':40,'player_height':32,'tp_scale':.7,'source_model':'src/main/resources/assets/slavicmyths/models/item/'+id+'.json','proposed_held_sha256':hashlib.sha256(json.dumps(model,sort_keys=True).encode()).hexdigest(),'reason':'Replace short flat material spear with consistent40-unit socketed 3D spear; retain GUI and tier.'})
 manifest={p.relative_to(dest).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in owned};(D/('applied-manifest.json' if apply else 'draft-manifest.json')).write_text(json.dumps(manifest,indent=2)+'\n')
 if not apply:(D/'plans.json').write_text(json.dumps(plans,indent=2)+'\n')
 print(('APPLIED' if apply else 'DRAFT'),len(owned),'models; zero PNG/gameplay edits');return manifest
if __name__=='__main__':
 parser=argparse.ArgumentParser();parser.add_argument('--apply',action='store_true');main(parser.parse_args().apply)
