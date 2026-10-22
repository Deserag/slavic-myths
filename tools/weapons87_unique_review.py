"""Per-ID source art inventory for #175-189. Read-only production resources."""
import hashlib,json
import numpy as np
from PIL import Image,ImageDraw
import weapons87_geometry as g

IDS=['three_breath_crossbow','serpent_crossbow','karachun_scythe','likho_staff','morozko_staff','poloz_whip','ataman_flail','tugarin_sword','nightingale_dagger','trophy_kalin_sabre','victor_spear','finist_bow','thunder_axe','thunder_spear','pool_spear']

def main():
 folder=g.O/'unique-source-review';folder.mkdir(exist_ok=True)
 catalog={x['id']:x for x in json.loads((g.O/'catalogue-provenance.json').read_text(encoding='utf-8'))};rows=[]
 gallery=Image.new('RGB',(1500,5*300),'#253039');gd=ImageDraw.Draw(gallery)
 for index,id in enumerate(IDS):
  ref='slavicmyths:item/'+id+('_pose_0' if id=='poloz_whip' else '')
  model=g.model(ref,'thirdperson_righthand');faces=g.faces(model);vertices=np.concatenate([v for _,v,_ in faces]);lo=vertices.min(0);hi=vertices.max(0);center=(lo+hi)/2;shift=(8-center).tolist()
  board=Image.new('RGB',(1100,430),'#253039');draw=ImageDraw.Draw(board)
  draw.text((8,8),f'#{175+index} {id} / actual JSON-UV-PNG, no game / units: 16 = one block',fill='white')
  draw.text((8,28),'Source-only assessment; fixed span48; translation centers full authored bounds, no scaling per item',fill='white')
  for j,angle in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
   image=g.project(faces,260,48,angle,translation=shift);board.paste(image,(j*275,75),image);draw.text((j*275+8,56),['FRONT','SIDE / thickness','3-4'][j],fill='white')
  gui=g.model('slavicmyths:item/'+id);display=gui.get('display',{}).get('gui',{});icon=g.project(g.faces(gui),16,16,display.get('rotation',[0,0,0]),display.get('scale',[1,1,1]),display.get('translation',[0,0,0]));board.paste(icon,(850,95),icon);board.paste(icon.resize((128,128),Image.Resampling.NEAREST),(875,95));draw.text((850,62),'GUI 1x + nearest8x',fill='white');draw.text((8,365),'Bounds '+str(np.round(lo,3).tolist())+' .. '+str(np.round(hi,3).tolist()),fill='white');draw.text((8,385),'Dimensions '+str(np.round(hi-lo,3).tolist())+'; actual texture references recorded in manifest',fill='white');board.save(folder/(id+'.png'))
  x=(index%3)*500;y=(index//3)*300;gd.text((x+5,y+5),f'#{175+index} '+id,fill='white');im=g.project(faces,245,48,(15,35,0),translation=shift);gallery.paste(im,(x,y+30),im);gallery.paste(icon.resize((112,112),Image.Resampling.NEAREST),(x+310,y+70))
  files=[]
  for kind in ['models','textures']:
   for source in catalog[id][kind]:
    path=source['path'];files.append({'path':path,'sha256':hashlib.sha256((g.R/path).read_bytes()).hexdigest()})
  rows.append({'issue':175+index,'id':id,'bounds':[lo.tolist(),hi.tolist()],'dimensions':(hi-lo).tolist(),'source_camera_span':48,'source_center_translation':shift,'textures':sorted({r for r,_,_ in faces}),'files':files,'decision':'PENDING_VISUAL_REVIEW','runtime':'NOT_RUN / OWNER_PENDING'})
 gallery.save(folder/'SOURCE-15.png');(folder/'source-manifest.json').write_text(json.dumps(rows,indent=2)+'\n')
 board=Image.new('RGB',(1500,380),'#253039');d=ImageDraw.Draw(board)
 for i in range(5):
  m=g.model('slavicmyths:item/poloz_whip_pose_'+str(i));im=g.project(g.faces(m),290,48,translation=(0,-6,0));board.paste(im,(i*300,45),im);d.text((i*300+5,10),'ACTUAL override '+str(i),fill='white')
 d.text((5,345),'PolozClient: idle .1 ->pose0; attack .3+attackAnim*.6 ->poses0..3; cooldown ->pose4. Source, NOT runtime.',fill='white');board.save(folder/'poloz-whip-actual-states.png')
 print('15 per-ID actual source sheets and file hashes + actual whip override states; no production mutation; visual decisions pending')

if __name__=='__main__':main()
