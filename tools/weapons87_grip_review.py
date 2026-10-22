"""Full 68-ID native hand-anchor/source geometry inventory; no entity or game launch."""
from pathlib import Path
import argparse,collections,json,math
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
import armor88_preview as painter
R=g.R;O=g.O
CUSTOM={'karachun_scythe':'NightClient player poses/layer + WeaponHands 1P; this fixed source pose is not that runtime path','tugarin_sword':'TugarinPairClient chooses _held + second blade; sheet shows one physical item per hand','serpent_crossbow':'WeaponHands custom1P; ordinary third-person item layer','three_breath_crossbow':'WeaponHands custom1P; ordinary third-person item layer'}
def main():
 parser=argparse.ArgumentParser();parser.add_argument('--after',action='store_true');args=parser.parse_args();samples=[s for s in json.loads((O/'player-native.json').read_text()) if s['pose']=='idle'];rows=json.loads((O/'audit-68.json').read_text(encoding='utf-8'));folder=O/('grip-68-after' if args.after else 'grip-68')
 if not args.after and (folder/'matrix.json').exists():raise RuntimeError('Preserve the original before inventory; use --after for current production.')
 folder.mkdir(exist_ok=True);records=[]
 def texture(ref):return g.texture(ref.replace(':textures/',':').removesuffix('.png'))
 painter.texture=texture
 for body in ['Steve','Alex']:
  sample=next(s for s in samples if s['body']==body)
  for family in dict.fromkeys(r['family'] for r in rows):
   selected=[r for r in rows if r['family']==family];sheet=Image.new('RGB',(1600,45+math.ceil(len(selected)/2)*290),'#253039');draw=ImageDraw.Draw(sheet);draw.text((10,7),f'{family} / {body} / actual item JSON + native hand matrices; fixed ITEM arm pose / NOT game screenshots',fill='white')
   for i,row in enumerate(selected):
    id=row['id'];originx=i%2*800;originy=45+i//2*290;draw.text((originx+5,originy),id,fill='white')
    for h,arm in enumerate(['RIGHT','LEFT']):
     ctx='thirdperson_'+('righthand' if arm=='RIGHT' else 'lefthand');d=g.model('slavicmyths:item/'+id,ctx);display=d.get('display',{});pose=display.get(ctx,display.get(ctx.replace('lefthand','righthand'),{}));rotation=list(pose.get('rotation',[0,0,0]));translation=list(pose.get('translation',[0,0,0]));scale=pose.get('scale',[1,1,1])
     if arm=='LEFT':rotation[1]*=-1;rotation[2]*=-1;translation[0]*=-1
     anchor=np.array(sample['hands'][arm]).reshape((4,4),order='F');layers=collections.defaultdict(list);allpoints=[]
     for ref,v,uv in g.faces(d):
      local=((v-8)*scale)@g.rotation(rotation).T+translation;points=(np.column_stack((local/16,np.ones(4)))@anchor.T)[:,:3]*16;allpoints.extend(points.tolist());layers[ref].append(np.column_stack((points,uv)).tolist())
     scene={'layers':[{'id':'skin','texture':sample['texture'],'faces':sample['faces']}]+[{'id':id,'texture':ref,'faces':faces} for ref,faces in layers.items()]}
     for view,yaw in enumerate([0,90]):
      x=originx+(h*2+view)*200;y=originy+18;sheet.paste(painter.render(scene,yaw),(x,y));draw.text((x+3,y+2),arm+(' FRONT' if view==0 else ' SIDE'),fill='white')
     p=np.array(allpoints);lo,hi=p.min(0),p.max(0);coarse=bool(np.all(hi>=np.array([-4,-8,-4])) and np.all(lo<=np.array([4,0,4])))
     records.append({'id':id,'body':body,'hand':arm,'display':pose,'native_aabb':[lo.tolist(),hi.tolist()],'coarse_head_aabb_overlap_candidate':coarse,'custom_path':CUSTOM.get(id),'runtime':'NOT_RUN / OWNER_PENDING','interpretation':'AABB is a broad candidate only, not a measured clipping test'})
   sheet.save(folder/(family+'-'+body+'.png'))
 (folder/'matrix.json').write_text(json.dumps(records,indent=2)+'\n');print('68 IDs / 2 actual native body models / both hand anchors; custom paths explicitly marked; no gameplay claim')
if __name__=='__main__':main()
