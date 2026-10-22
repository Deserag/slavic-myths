"""Actual native player meshes + archived/current item quads in native hand matrices."""
from pathlib import Path
import collections,io,json,math,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
import armor88_preview as painter
R=g.R;O=g.O
def main():
 samples=json.loads((O/'player-native.json').read_text());archive=zipfile.ZipFile(O/'before-source.zip');old={n:archive.read(n) for n in archive.namelist()};current_texture=g.texture
 def old_texture(ref):
  ns,path=ref.split(':');key=f'src/main/resources/assets/{ns}/textures/{path}.png'
  return np.array(Image.open(io.BytesIO(old[key])).convert('RGBA')) if key in old else current_texture(ref)
 def old_model(ref,context):
  ns,path=ref.split(':');key=f'src/main/resources/assets/{ns}/models/{path}.json'
  if key not in old:return g.model(ref,context)
  d=json.loads(old[key]);parent=d.get('parent','')
  if parent:
   base=old_model(parent,context) if parent.startswith('slavicmyths:') else g.model(parent,context);base['textures']=base.get('textures',{})|d.get('textures',{});base['display']=base.get('display',{})|d.get('display',{});base.update({k:v for k,v in d.items() if k not in ['textures','display']});return base
  return d
 def paint_texture(ref):
  if ':textures/' in ref:ref=ref.replace(':textures/',':').removesuffix('.png')
  return g.texture(ref)
 painter.texture=paint_texture;folder=O/'player-flails';folder.mkdir(exist_ok=True);records=[]
 ids=['stone_flail','gold_flail','flail','silver_flail','diamond_flail','perunite_flail','netherite_flail','ataman_flail']
 for id in ids:
  for body in ['Steve','Alex']:
   selected=[s for s in samples if s['body']==body];sheet=Image.new('RGB',(1600,1190),'#253039');draw=ImageDraw.Draw(sheet);draw.text((10,8),f'{id} / {body} / actual ModelPart + item JSON/UV / fixed source poses, NOT game screenshots',fill='white')
   for row,sample in enumerate(selected):
    for h,arm in enumerate(['RIGHT','LEFT']):
     for after in [False,True]:
      g.texture=current_texture if after else old_texture;context='thirdperson_'+('righthand' if arm=='RIGHT' else 'lefthand');d=(g.model if after else old_model)('slavicmyths:item/'+id,context);pose=d.get('display',{}).get(context,{});rotation=list(pose.get('rotation',[0,0,0]));translation=list(pose.get('translation',[0,0,0]));scale=pose.get('scale',[1,1,1])
      if arm=='LEFT':rotation[1]*=-1;rotation[2]*=-1;translation[0]*=-1
      matrix=np.array(sample['hands'][arm]).reshape((4,4),order='F');layers=collections.defaultdict(list)
      for ref,v,uv in g.faces(d):
       local=((v-8)*scale)@g.rotation(rotation).T+translation;homogeneous=np.column_stack((local/16,np.ones(4)));points=(homogeneous@matrix.T)[:,:3]*16;layers[ref].append(np.column_stack((points,uv)).tolist())
      scene={'layers':[{'id':'skin','texture':sample['texture'],'faces':sample['faces']}]+[{'id':id,'texture':ref,'faces':faces} for ref,faces in layers.items()]}
      for view,yaw in enumerate([0,90]):
       x=(h*4+int(after)*2+view)*200;y=45+row*285;sheet.paste(painter.render(scene,yaw),(x,y));draw.text((x+3,y+2),sample['pose']+' '+arm+' '+('AFTER' if after else 'BEFORE'),fill='white');draw.text((x+3,y+18),'FRONT' if view==0 else 'SIDE',fill='white')
      # The production grip is origin of the centered item mesh, a native hand anchor.
      grip=matrix@np.array([0,0,0,1]);headlocal=(np.array([12,-2,8])-8)*scale;head=(matrix@np.append((g.rotation(rotation)@headlocal+translation)/16,1))[:3]*16
      records.append({'id':id,'body':body,'pose':sample['pose'],'arm':arm,'after':after,'grip_native':(grip[:3]*16).tolist(),'ball_center_native':head.tolist() if after else None,'runtime':'NOT_RUN / OWNER_PENDING'})
   sheet.save(folder/(id+'-'+body+'.png'))
 g.texture=current_texture;(folder/'matrix.json').write_text(json.dumps(records,indent=2)+'\n');print('16 native player/source A/B sheets, 128 hand/pose cases; no gameplay pose validation claim')
if __name__=='__main__':main()
