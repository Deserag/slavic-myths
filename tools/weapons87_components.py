"""#168 source audit only: never rewrites accepted #99/#100 components."""
from pathlib import Path
import hashlib,json
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
R=g.R;O=g.O;A=R/'src/main/resources/assets/slavicmyths'
IDS=['iron_blank','spear_shaft','short_shaft','weapon_wrap','silver_fitting','iron_rings']+[m+'_'+s for s in ['spearhead','light_spearhead'] for m in ['stone','gold','iron','silver','diamond','perunite','netherite']]
REASONS={'iron_blank':'Forged tapered metal blank; short broad body distinct from the wooden rods and socket tips.','spear_shaft':'Long thin wooden shaft, visibly longer than short_shaft.','short_shaft':'Short wooden handle; same stock family as spear_shaft, shorter purpose-specific length.','weapon_wrap':'Open brown leather binding loop; source UV atlas is not the GUI icon.','silver_fitting':'Open angular silver socket/collar with real depth; separate from leather binding.','iron_rings':'Small linked iron loops for mail; QA only, armor component is outside weapon art ownership.'}
def main():
 folder=O/'components-168';folder.mkdir(exist_ok=True);rows=[];sheet=Image.new('RGB',(1400,40+len(IDS)*135),'#253039');draw=ImageDraw.Draw(sheet);draw.text((8,8),'#168 ACTUAL JSON/UV / GUI1x +8x / front / side / 3-4; no art edits; fixed scale; source only',fill='white')
 recipes=[(p,json.loads(p.read_text(encoding='utf-8'))) for p in (R/'src/main/resources/data/slavicmyths/recipe').rglob('*.json')]
 for i,id in enumerate(IDS):
  d=g.model('slavicmyths:item/'+id);faces=g.faces(d);display=d.get('display',{}).get('gui',{});gui=g.project(faces,16,16,display.get('rotation',[0,0,0]),display.get('scale',[1,1,1]),display.get('translation',[0,0,0]));line=40+i*135;draw.text((8,line+4),id,fill='white');sheet.paste(gui,(220,line+8),gui);enlarged=gui.resize((96,96),Image.Resampling.NEAREST);sheet.paste(enlarged,(250,line+8),enlarged)
  for j,angle in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
   im=g.project(faces,120,32,angle);sheet.paste(im,(380+j*140,line),im)
  refs=[p.relative_to(R).as_posix() for p,x in recipes if 'slavicmyths:'+id in json.dumps(x)]
  reason=REASONS.get(id,'Existing accepted #99/#100 '+('light narrow sulitsa point' if '_light_' in id else 'broad socketed spear point')+'; same-material recolor is intentional, heavy/light silhouettes differ. QA only; do not redraw.')
  vertices=np.concatenate([v for _,v,_ in faces]);dims=vertices.max(0)-vertices.min(0);source=A/'models/item'/(id+'.json');row={'id':id,'decision':'KEEP','reason':reason,'scope':'QA_ONLY_EXTERNAL_99_100' if id not in REASONS else 'QA_ONLY_ARMOR' if id=='iron_rings' else 'OWNED_WEAPON_COMPONENT','source':source.relative_to(R).as_posix(),'model_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'dimensions':dims.tolist(),'recipe_references':refs,'runtime':'NOT_RUN / OWNER_PENDING'};rows.append(row)
  for n,text in enumerate([reason[:95],reason[95:190],f'Recipe references: {len(refs)} / extents: '+', '.join(f'{x:.2f}' for x in dims),'KEEP; source only, runtime OWNER_PENDING']):draw.text((820,line+12+n*23),text,fill='white')
 contrast_ids=['iron_rings','bone_arrow','iron_spearhead','iron_light_spearhead','iron_spear','iron_sulitsa','iron_throwing_knife','flail_chain']
 contrast=Image.new('RGB',(1200,430),'#253039');cd=ImageDraw.Draw(contrast);cd.text((8,8),'#168 GUI role contrast / actual models+UV1x and8x; no Minecraft screenshots',fill='white');context=[]
 for j,id in enumerate(contrast_ids):
  model=g.model('slavicmyths:item/'+id);display=model.get('display',{}).get('gui',{});image=g.project(g.faces(model),16,16,display.get('rotation',[0,0,0]),display.get('scale',[1,1,1]),display.get('translation',[0,0,0]));x=j%4*300;y=35+j//4*195;cd.text((x+3,y),id,fill='white');contrast.paste(image,(x+8,y+30),image);big=image.resize((128,128),Image.Resampling.NEAREST);contrast.paste(big,(x+45,y+25),big);context.append({'id':id,'source_context':'actual GUI','role':'comparison only; no extra parts/IDs','runtime':'NOT_RUN / OWNER_PENDING'})
 contrast.save(folder/'ROLE-CONTRAST-1X.png');(folder/'context-comparison.json').write_text(json.dumps(context,indent=2)+'\n')
 sheet.save(folder/'SOURCE-20.png');(folder/'audit.json').write_text(json.dumps(rows,indent=2)+'\n');print('20 actual component source/recipe audits; zero production edits; accepted14 heads and armor rings untouched')
if __name__=='__main__':main()
