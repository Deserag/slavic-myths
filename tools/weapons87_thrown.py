"""#173 source blueprints BEFORE renderer change; no production asset writes."""
import hashlib,json,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
O=g.O;D=O/'blueprints/thrown'
def main():
 with zipfile.ZipFile(O/'before-thrown-orientation.zip') as z:
  source='src/main/java/org/slavicmyths/client/WeaponProjectileRenderer.java'
  if (g.R/source).read_bytes()!=z.read(source):raise RuntimeError('Preserve existing before-production source sheets; renderer already changed.')
 rows=[]
 for kind in ['throwing_knife','sulitsa']:
  for material in ['stone','gold','iron','silver','diamond','perunite','netherite']:
   id=material+'_'+kind;model=g.model('slavicmyths:item/'+id);faces=g.faces(model);scale=.65 if kind=='throwing_knife' else 1.25;board=Image.new('RGB',(1400,540),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),id+' #192/#173 / actual unchanged archetype+UV; flight correction PROPOSAL before source change',fill='white')
   for j,angles in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
    image=g.project(faces,200,20,angles);board.paste(image,(j*215,45),image);draw.text((j*215+4,30),['front','thickness','3-4'][j],fill='white')
   for after in [False,True]:
    rot=g.rotation([0,0,-45])@(np.eye(3) if after else g.rotation([0,180,0]));moved=[(r,((v-8)*scale)@rot.T+8,uv) for r,v,uv in faces];image=g.project(moved,300,32,(15,30,0));x=660+int(after)*350;board.paste(image,(x,50),image);draw.text((x+4,32),'FLIGHT yaw90/pitch0 '+('PROPOSED NONE' if after else 'ACTUAL FIXED'),fill='white')
   display=model.get('display',{}).get('gui',{});icon=g.project(faces,16,16,display.get('rotation',[0,0,0]),display.get('scale',[1,1,1]),display.get('translation',[0,0,0]));board.paste(icon,(20,355),icon);big=icon.resize((96,96),Image.Resampling.NEAREST);board.paste(big,(55,330),big)
   points=np.concatenate([v for _,v,_ in faces]);dims=points.max(0)-points.min(0);visible=Image.open(g.R/'src/main/resources/assets/slavicmyths/textures/item'/(id+'.png')).getbbox();lines=[f'KEEP sprite/held: {kind}; material variant within same functional family',f'Quad canvas16x16x1; opaque sprite bounds{visible}; flight scale{scale}; player height32', 'Knife broad short blade + grip; sulitsa long wood shaft + small point. Different silhouettes.', 'Fix only flight context: FIXED Y180 makes blade axis perpendicular; NONE points it along velocity.', 'Do not change item frame/GUI transforms, damage, speed, pickup, loyalty or accepted tip assets.']
   for k,line in enumerate(lines):draw.text((180,342+k*31),line,fill='white')
   board.save(D/(id+'.png'));rows.append({'id':id,'family':kind,'decision':'KEEP_ART_FIX_FLIGHT_CONTEXT','display_fixed':model.get('display',{}).get('fixed',{}),'display_none':model.get('display',{}).get('none',{}),'dimensions':dims.tolist(),'opaque_sprite_bounds':visible,'scale':scale,'model':model,'grip':'existing native handheld transform unchanged','reason':'Different two archetypes; material recolor within family is intentional; no GUI/held art defect confirmed'})
 (O/'thrown-native-inputs.json').write_text(json.dumps(rows,indent=2)+'\n');print('14 per-material before-production source archetype/flight proposal sheets; zero production writes')
if __name__=='__main__':main()
