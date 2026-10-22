"""#163 actual draw-state storyboard and native FP source camera comparison."""
import argparse,hashlib,json,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
import weapons87_fp_review as fp
IDS=['short_bow','heavy_bow','finist_bow']

def prepare():
 archive=g.O/'before-ranged-timing.zip'
 if archive.exists():raise RuntimeError('Preserve ranged source baseline')
 catalog={x['id']:x for x in json.loads((g.O/'catalogue-provenance.json').read_text(encoding='utf-8'))};paths=set()
 for id in IDS+['serpent_crossbow','three_breath_crossbow']:
  for key in ['models','textures']:paths.update(x['path'] for x in catalog[id][key])
 paths.update('src/main/java/org/slavicmyths/'+p for p in ['combat/FieldBowItem.java','finist/FinistBow.java','client/FieldBowClient.java','client/FinistClient.java','client/WeaponHands.java','client/GorynychClient.java','gorynych/SerpentCrossbow.java','gorynych/CrossbowVolley.java'])
 with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
  for path in sorted(paths):z.write(g.R/path,path)
 rows=[]
 for id in IDS:
  for left in [False,True]:
   context='firstperson_'+('lefthand' if left else 'righthand');m=g.model('slavicmyths:item/'+id,context);rows.append({'id':id,'left':left,'display':m['display'][context]})
 (g.O/'bow-native-inputs.json').write_text(json.dumps(rows,indent=2)+'\n');print('6 native item-transform inputs; actual ranged art/server/client source archived, no production mutation')

def review():
 folder=g.O/'ranged163-bows';folder.mkdir(exist_ok=True);native=json.loads((g.O/'bow-native.json').read_text());assert len(native)==240;records=[]
 for row in json.loads((g.O/'bow-native-inputs.json').read_text()):
  context='firstperson_'+('lefthand' if row['left'] else 'righthand')
  for state in range(3):assert g.model('slavicmyths:item/'+row['id']+'_pulling_'+str(state),context)['display'][context]==row['display'],'Pulling display differs from native input'
 with zipfile.ZipFile(g.O/'before-ranged-timing.zip') as z:
  preserved=[{'path':p,'sha256':hashlib.sha256(z.read(p)).hexdigest()} for p in z.namelist() if (g.R/p).read_bytes()==z.read(p)];assert len(preserved)==len(z.namelist()),'Ranged art/server/old client changed'
 for id in IDS:
  board=Image.new('RGB',(1600,1600),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),id+' #163 / actual pulling JSON+PNG, native FP transforms / fixed FOV70,16:9 / NOT game',fill='white')
  for index,ticks in enumerate([0,6,12,20,30,45]):
   for left in [False,True]:
    for after in [False,True]:
     row=next(x for x in native if x['id']==id and x['ticks']==ticks and x['equip']==0 and x['left']==left and x['after']==after)
     # Actual model predicate already followed actual server power before this fix.
     actual=next(x for x in native if x['id']==id and x['ticks']==ticks and x['equip']==0 and x['left']==left and x['after'])['power'];model=g.model('slavicmyths:item/'+id+'_pulling_'+('2' if actual>=.9 else '1' if actual>=.65 else '0'),'firstperson_lefthand' if left else 'firstperson_righthand');matrix=np.array(row['matrix']).reshape((4,4),order='F');faces=[(r,(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3],uv) for r,v,uv in g.faces(model)];im,pixels=fp.render(faces,400,225);column=int(left)*2+int(after);y=30+index*250;board.paste(im,(column*400,y));draw.text((column*400+4,y+4),('L' if left else 'R')+' '+('AFTER' if after else 'BEFORE')+f' t={ticks} camera={row["power"]:.3f} texture={actual:.3f}',fill='white');records.append({'id':id,'left':left,'after':after,'ticks':ticks,'camera_power':row['power'],'texture_power':actual,'pixels':pixels});assert pixels>0,'Bow left source viewport'
  board.save(folder/(id+'-timing.png'))
  graph=Image.new('RGB',(800,350),'#253039');d=ImageDraw.Draw(graph);d.text((8,8),id+' / camera draw power BEFORE20 vs AFTER actual draw / texture uses actual in both',fill='white');points={after:[] for after in [False,True]}
  duration=12 if id=='short_bow' else 30
  for ticks in range(46):
   for after in [False,True]:
    v=ticks/(duration if after else 20);power=min(1,(v*v+2*v)/3);points[after].append((30+ticks*16,300-power*240))
  d.line(points[False],fill='#d77575',width=3);d.line(points[True],fill='#85c8dc',width=3);d.text((35,320),'RED old20; CYAN actual '+str(duration)+' ticks. No damage/ammo/duration changes.',fill='white');graph.save(folder/(id+'-power.png'))
 (folder/'matrix.json').write_text(json.dumps(records,indent=2)+'\n');(folder/'preserved-files.json').write_text(json.dumps(preserved,indent=2)+'\n');print('3 actual bow storyboards/72 visible FP source cases +3 power graphs; all archived ranged resources and server/client unchanged; runtime NOT_RUN')

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--prepare',action='store_true');args=p.parse_args();prepare() if args.prepare else review()
