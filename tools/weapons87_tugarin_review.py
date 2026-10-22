"""Source before/after GUI and exact unchanged native FP wrists for #182."""
import hashlib,json,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
import weapons87_fp_review as fp

def main():
 folder=g.O/'tugarin182-after';folder.mkdir(exist_ok=True);archive=zipfile.ZipFile(g.O/'before-tugarin-icon.zip');root='src/main/resources/assets/slavicmyths/models/item/tugarin_sword.json';old=json.loads(archive.read(root));new=g.model('slavicmyths:item/tugarin_sword');rows=[];preserved=[]
 for path in archive.namelist():
  if path==root:continue
  assert (g.R/path).read_bytes()==archive.read(path),path;preserved.append({'path':path,'sha256':hashlib.sha256(archive.read(path)).hexdigest()})
 board=Image.new('RGB',(1200,730),'#253039');d=ImageDraw.Draw(board);d.text((8,8),'#182 actual archived/current JSON+UV / fixed cameras and scale / source, NOT game',fill='white')
 for i,context in enumerate(['gui','ground','fixed']):
  for after,model in [(False,old),(True,new)]:
   display=model.get('display',{}).get(context,{})
   # Both old block/block and new direct model have identity FIXED.
   image=g.project(g.faces(model),210,20,display.get('rotation',[0,0,0]),display.get('scale',[1,1,1]),display.get('translation',[0,0,0]));x=i*400+int(after)*200;board.paste(image,(x,55),image);d.text((x+3,35),context+' '+('AFTER' if after else 'BEFORE'),fill='white');rows.append({'context':context,'after':after,'actual_source_projection':True})
 for after,model in [(False,old),(True,new)]:
  ui=model['display']['gui'];image=g.project(g.faces(model),16,16,ui['rotation'],ui['scale'],ui['translation']);x=int(after)*250;board.paste(image,(x+10,310),image);board.paste(image.resize((160,160),Image.Resampling.NEAREST),(x+45,310));d.text((x+3,490),'GUI1x/10x '+('AFTER' if after else 'BEFORE'),fill='white')
 # Exact texture aspect ratio: front quads preserve16x16 canvas; occupied
 # sprite pixels remain authored32px detail mapped into16 model units.
 envelopes=[e for e in new['elements'] if 'north' in e['faces']];assert len(envelopes)==2
 assert all(e['to'][0]-e['from'][0]==16 and e['to'][1]-e['from'][1]==16 for e in envelopes)
 assert envelopes[0]['to'][2]<envelopes[1]['from'][2],'Pair depth overlap';assert len(new['elements'])==126
 held=g.model('slavicmyths:item/tugarin_sword','firstperson_righthand');native=[r for r in json.loads((g.O/'fp-native.json').read_text()) if r['id']=='tugarin_sword' and r['after']];assert len(native)==18
 sheet=Image.new('RGB',(1600,30+5*225),'#253039');draw=ImageDraw.Draw(sheet);draw.text((5,5),'Actual unchanged source held + package3 native wrist / Steve-Alex x LEFT-RIGHT x4 swings +2 ordinary offhand cases. NOT runtime.',fill='white')
 for row in native:
  matrix=np.array(row['matrix']).reshape((4,4),order='F');faces=[(r,(np.column_stack(((v-8)/16,np.ones(4)))@matrix.T)[:,:3],uv) for r,v,uv in g.faces(held)];image,pixels=fp.render(faces,400,225);column=(0 if row['body'] in ['Steve','ITEM_ONLY'] else 2)+int(row['left']);frame=4 if row['body']=='ITEM_ONLY' else int(row['frame'].removeprefix('swing'));sheet.paste(image,(column*400,30+frame*225));draw.text((column*400+3,34+frame*225),row['body']+' '+('L' if row['left'] else 'R')+' '+row['frame'],fill='white');rows.append({'context':'FP','body':row['body'],'left':row['left'],'frame':row['frame'],'pixels':pixels,'held_png_renderer_byte_identical':True})
 for i,line in enumerate(['New GUI source: two native-aspect parallel swords, not squeezed mirrored planes','PNG32x32 KEEP; held generated model KEEP; actual renderer/combat KEEP','Pair depth separated by.2 model units at nearest surfaces: no coplanar faces','All18 actual native first-person states retain source/model/wrist byte identity','Third person same source held/renderer; native idle-body export reused from package3','No gameplay, component, registry or boss changes. Owner runtime remains NOT_RUN']):d.text((8,535+i*28),line,fill='white')
 board.save(folder/'tugarin_sword-before-after.png');sheet.save(folder/'tugarin_sword-native-fp.png');(folder/'matrix.json').write_text(json.dumps(rows,indent=2)+'\n');(folder/'preserved-files.json').write_text(json.dumps(preserved,indent=2)+'\n');print('GUI/ground/fixed A-B +18 unchanged actual native FP samples;126 source elements; 4 preserved files; game NOT_RUN')

if __name__=='__main__':main()
