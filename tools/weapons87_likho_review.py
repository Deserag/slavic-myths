"""#178 actual GUI clipping/eye-face evidence and unchanged held native paths."""
import collections,hashlib,json,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
import weapons87_fp_review as fp
import armor88_preview as painter
from weapons87_head_clearance import triangle_box

def main():
 folder=g.O/'likho178-after';folder.mkdir(exist_ok=True);z=zipfile.ZipFile(g.O/'before-likho-gui.zip');path='src/main/resources/assets/slavicmyths/models/item/likho_staff.json';before=json.loads(z.read(path));after=g.model('slavicmyths:item/likho_staff');assert before['elements']==after['elements'] and before['textures']==after['textures'];assert {k:v for k,v in before['display'].items() if k!='gui'}=={k:v for k,v in after['display'].items() if k!='gui'};preserved=[]
 for name in z.namelist():
  if name==path:continue
  assert (g.R/name).read_bytes()==z.read(name);preserved.append({'path':name,'sha256':hashlib.sha256(z.read(name)).hexdigest()})
 icons=[];clip=[];eye=g.texture('slavicmyths:item/weapon_scythe_surface')[0,5]
 for model in [before,after]:
  ui=model['display']['gui'];faces=g.faces(model);v=np.concatenate([p for _,p,_ in faces]);p=((v-8)*ui['scale'])@g.rotation(ui['rotation']).T+ui.get('translation',[0,0,0]);clip.append(bool(np.any(abs(p[:,:2])>8+1e-6)));icons.append(g.project(faces,16,16,ui['rotation'],ui['scale'],ui.get('translation',[0,0,0])))
 eye_counts=[int(np.all(np.array(im)==eye,axis=2).sum()) for im in icons];assert clip==[True,False],clip;assert eye_counts[0]==0 and eye_counts[1]>0,eye_counts
 board=Image.new('RGB',(1600,1700),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),'#178 actual before/current JSON UV +native hand/body matrices; fixed source poses, NOT Minecraft',fill='white');fprows=json.loads((g.O/'fp-native.json').read_text());bodies=json.loads((g.O/'player-native.json').read_text());records=[];painter.texture=lambda ref:g.texture(ref.replace(':textures/',':').removesuffix('.png'))
 for left in [False,True]:
  row=next(x for x in fprows if x['id']=='likho_staff' and x['left']==left and x['after']);matrix=np.array(row['matrix']).reshape((4,4),order='F')
  for stage,model in [(False,before),(True,after)]:
   faces=[(r,(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3],uv) for r,v,uv in g.faces(model)];im,pixels=fp.render(faces,400,225);column=int(left)*2+int(stage);board.paste(im,(column*400,30));draw.text((column*400+3,34),'FP '+('L' if left else 'R')+' '+('AFTER' if stage else 'BEFORE'),fill='white');records.append({'context':'FP','left':left,'after':stage,'pixels':pixels})
 from weapons87_grip_blueprints import scene
 for i,(body,pose) in enumerate([('Steve','idle'),('Alex','idle'),('Steve','walk_t10'),('Alex','walk_t10')]):
  sample=next(x for x in bodies if x['body']==body and x['pose']==pose)
  for left in [False,True]:
   for stage,model in [(False,before),(True,after)]:
    rendered=scene(sample,'likho_staff','LEFT' if left else 'RIGHT',model);hits=0
    for layer in rendered['layers'][1:]:
     for face in layer['faces']:
      for indices in [(0,1,2),(0,2,3)]:hits+=int(triangle_box(np.array(face)[list(indices),:3],np.array([-4.,-8.,-4.]),np.array([4.,0.,4.])))
    column=int(left)*2+int(stage);y=280+i*285
    for view,yaw in enumerate([0,90]):
     x=column*400+view*200;board.paste(painter.render(rendered,yaw),(x,y));draw.text((x+3,y+3),body+' '+pose+' '+('L' if left else 'R')+' '+('AFTER' if stage else 'BEFORE'),fill='white')
    records.append({'context':'TP','body':body,'pose':pose,'left':left,'after':stage,'head_triangle_candidates':hits})
 for stage,icon in enumerate(icons):board.paste(icon,(stage*250+10,1480),icon);board.paste(icon.resize((128,128),Image.Resampling.NEAREST),(stage*250+45,1460));draw.text((stage*250+3,1610),'GUI1x/8x '+('AFTER' if stage else 'BEFORE'),fill='white')
 draw.text((570,1480),'Geometry/UV/PNG/all non-GUI transforms KEEP. Existing eye face shown.',fill='white');draw.text((570,1510),'Actual GUI geometry clipping: True ->False; iris-colored pixels '+str(eye_counts),fill='white');draw.text((570,1540),'No boss, spell, stats, animation or accepted staff silhouette changes.',fill='white');board.save(folder/'likho_staff-before-after.png');assert not any(x.get('head_triangle_candidates',0) for x in records if x['after'] and x.get('pose')=='idle')
 (folder/'matrix.json').write_text(json.dumps(records,indent=2)+'\n');(folder/'preserved-files.json').write_text(json.dumps(preserved,indent=2)+'\n');(folder/'geometry-gates.json').write_text(json.dumps({'shape_uv_textures_keep':True,'non_gui_transforms_keep':True,'gui_clipping_before_after':clip,'eye_pixels_before_after':eye_counts,'after_idle_head_candidates':0,'runtime':'NOT_RUN / OWNER_PENDING'},indent=2)+'\n');print('PASS:actual GUI clipped->inside, eye0->'+str(eye_counts[1])+' pixel;20 native source FP/TP cases;geometry/UV/PNG/server unchanged;idle head candidates0')

if __name__=='__main__':main()
