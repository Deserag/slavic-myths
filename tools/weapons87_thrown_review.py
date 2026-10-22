"""Actual native projectile transforms x existing production PNG/UV,14 source A/B sheets."""
import json,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
O=g.O

def main():
 inputs={x['id']:x for x in json.loads((O/'thrown-native-inputs.json').read_text())};rows=json.loads((O/'thrown-native.json').read_text());folder=O/'thrown-after';folder.mkdir(exist_ok=True);records=[]
 for id,item in inputs.items():
  board=Image.new('RGB',(1200,540),'#253039');d=ImageDraw.Draw(board);d.text((8,8),id+' / actual Java flightPose + native ItemTransform + unchanged sprite UV / source, NOT game',fill='white')
  for i,(yaw,pitch) in enumerate([(90,0),(45,35),(180,-45)]):
   for after in [False,True]:
    row=next(x for x in rows if x['id']==id and x['yaw']==yaw and x['pitch']==pitch and x['after']==after);matrix=np.array(row['matrix']).reshape((4,4),order='F');faces=[]
    for ref,v,uv in g.faces(item['model']):faces.append((ref,(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3]*16+8,uv))
    image=g.project(faces,160,32,(15,30,0));x=int(after)*600;y=35+i*165;board.paste(image,(x,y));d.text((x+290,y+15),('AFTER NONE' if after else 'BEFORE FIXED')+f' yaw{yaw} pitch{pitch}',fill='white');d.text((x+290,y+40),f'blade dot velocity: {row["direction_dot"]:.6f}',fill='white');records.append({'id':id,'after':after,'yaw':yaw,'pitch':pitch,'direction_dot':row['direction_dot'],'scale':item['scale'],'frame':'fixed span32/camera15,30/source only'})
  board.save(folder/(id+'.png'))
 with zipfile.ZipFile(O/'before-thrown-orientation.zip') as z:
  preserved=[]
  for name in z.namelist():
   if name.endswith('/client/WeaponProjectileRenderer.java'):continue
   assert (g.R/name).read_bytes()==z.read(name),'Unexpected art/server edit '+name;preserved.append(name)
 (folder/'preserved-files.json').write_text(json.dumps(preserved,indent=2)+'\n');(folder/'matrix.json').write_text(json.dumps(records,indent=2)+'\n');print('14 actual projectile source A/B sheets,84 displayed native frame cases; all28 model/PNG +2 server files unchanged; game NOT_RUN')
if __name__=='__main__':main()
