"""Addressed source art QA for18 ordinary weapons. No production mutation."""
import hashlib,json,zipfile
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g
GROUPS={169:['bone_knife','silver_dagger','ancient_carolingian_sword','restored_carolingian_sword','silver_sword','perunite_sword'],170:['club','wooden_mace','stone_mace','mace','silver_mace','perunite_mace','ancient_chekan','chekan','battle_axe'],172:['short_bow','heavy_bow','bone_arrow']}
def main():
 O=g.O;folder=O/'family-art-review';folder.mkdir(exist_ok=True);catalog={x['id']:x for x in json.loads((O/'catalogue-provenance.json').read_text(encoding='utf-8'))};archive=zipfile.ZipFile(O/'before-source.zip');models={};rows=[];art={}
 for name in ['silver-mace','storm-staff']:
  file=O/'blueprints'/name/'applied-manifest.json'
  if file.exists():art.update(json.loads(file.read_text()))
 for name in archive.namelist():
  key='src/main/resources/assets/slavicmyths/models/'
  if name.startswith(key) and name.endswith('.json'):models['slavicmyths:'+name[len(key):-5]]=json.loads(archive.read(name))
 for issue,ids in GROUPS.items():
  gallery=Image.new('RGB',(1200,40+len(ids)*175),'#253039');draw=ImageDraw.Draw(gallery);draw.text((8,8),f'#{issue} actual JSON/UV source art QA / fixed scale / BEFORE vs CURRENT; display grips separate #115',fill='white')
  for i,id in enumerate(ids):
   before=g.model('slavicmyths:item/'+id,'thirdperson_righthand',models);current=g.model('slavicmyths:item/'+id,'thirdperson_righthand');old=g.faces(before);new=g.faces(current);y=40+i*175;draw.text((5,y+4),id,fill='white')
   for j,angle in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
    for after,faces in [(False,old),(True,new)]:
     x=175+(j*2+int(after))*140;image=g.project(faces,135,48,angle);gallery.paste(image,(x,y+20),image);draw.text((x+3,y+4),('CURRENT' if after else 'BEFORE')+[' F',' S',' 3/4'][j],fill='white')
   gui_model=g.model('slavicmyths:item/'+id);display=gui_model.get('display',{}).get('gui',{});gui=g.project(g.faces(gui_model),16,16,display.get('rotation',[0,0,0]),display.get('scale',[1,1,1]),display.get('translation',[0,0,0]));gallery.paste(gui,(5,y+30),gui);big=gui.resize((96,96),Image.Resampling.NEAREST);gallery.paste(big,(35,y+35),big)
   checked=[]
   for kind in ['models','textures']:
    for source in catalog[id][kind]:
     path=source['path'];data=(g.R/path).read_bytes();old_data=archive.read(path)
     artpath=path.removeprefix('src/main/resources/assets/slavicmyths/');same=True
     if kind=='models':
      a=json.loads(old_data);b=json.loads(data);same={k:v for k,v in a.items() if k!='display'}=={k:v for k,v in b.items() if k!='display'}
      if artpath in art:assert hashlib.sha256(data).hexdigest()==art[artpath],'Approved art differs from blueprint '+path
      else:assert same,'Unexpected art change '+path
     else:assert data==old_data,'Texture changed '+path
     checked.append({'path':path,'sha256':hashlib.sha256(data).hexdigest(),'art_matches_before':same})
   vertices=np.concatenate([v for _,v,_ in new]);rows.append({'issue':issue,'id':id,'decision':'PENDING_VISUAL_REVIEW','checked_files':checked,'dimensions':(vertices.max(0)-vertices.min(0)).tolist(),'actual_texture_refs':sorted({ref for ref,_,_ in new}),'runtime':'NOT_RUN / OWNER_PENDING'})
  gallery.save(folder/(str(issue)+'-source.png'))
 # Full-draw art states are actual resources, timing intentionally belongs to #163.
 for id in ['short_bow','heavy_bow']:
  board=Image.new('RGB',(800,260),'#253039');d=ImageDraw.Draw(board)
  for i,suffix in enumerate(['','_pulling_0','_pulling_1','_pulling_2']):
   model=g.model('slavicmyths:item/'+id+suffix);faces=g.faces(model);image=g.project(faces,180,20);board.paste(image,(i*200,35),image);d.text((i*200+3,8),id+suffix,fill='white')
  board.save(folder/(id+'-draw-states.png'))
 (folder/'pending-review.json').write_text(json.dumps(rows,indent=2)+'\n');print('18 addressed source art comparisons and two actual bow-state sheets; only explicit approved art overlays may differ; all other geometry/UV/PNG preserved; decisions in review.json')
if __name__=='__main__':main()
