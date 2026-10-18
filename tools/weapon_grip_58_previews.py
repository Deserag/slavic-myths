"""#58 native old/new source projections, not game screenshots."""
from pathlib import Path
from PIL import Image,ImageDraw
import gzip,json,math
from grey_wolf_54_previews import draw
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapon-grip-58'
def main():
 before=json.loads(gzip.decompress((O/'before-native.json.gz').read_bytes()));after=json.loads(gzip.decompress((R/'build/weapon-grip-58/after-native.json.gz').read_bytes()))
 for mob in ['karachun','morozko']:
  tex=Image.open(R/f'src/main/resources/assets/slavicmyths/textures/entity/{mob}.png').convert('RGB')
  im=Image.new('RGB',(1500,1800),'#202B30');d=ImageDraw.Draw(im)
  for row,(data,key,word) in enumerate([(before,'a0_0','BEFORE idle'),(after,'a0_0','AFTER idle'),(before,'walk','BEFORE walk'),(after,'walk','AFTER walk')]):
   for col,(yaw,name) in enumerate([(0,'FRONT'),(90,'SIDE'),(-35,'3/4')]):
    im.paste(draw(data[mob][key],tex,yaw),(500*col,445*row+25));d.text((500*col+12,445*row+7),mob.upper()+' / '+word+' / '+name,fill='#DCE2DD')
  d.text((12,1785),'Actual baked mesh/UV. Same camera and scale. NOT MINECRAFT SCREENSHOTS.',fill='#91A382');im.save(O/f'native-{mob}-idle-walk.png')
  actions=[(1,10),(2,10),(3,14),(4,16),(5,20),(6,16),(7,24),(8,24),(9,20)] if mob=='karachun' else [(1,10),(2,13),(3,50),(4,5),(5,16),(6,24),(7,7),(8,48)]
  im=Image.new('RGB',(2000,((len(actions)+1)//2)*445+25),'#202B30');d=ImageDraw.Draw(im)
  for i,(action,tick) in enumerate(actions):
   for j,(data,word) in enumerate([(before,'BEFORE'),(after,'AFTER')]):
    x=(i%2*2+j)*500;y=i//2*445;key=f'a{action}_{tick}';key=key if key in data[mob] else f'a{action}_5';actual_tick=int(key.split('_')[1]);im.paste(draw(data[mob][key],tex,-35,scale=3,center_y=250),(x,y+25));d.text((x+12,y+7),f'{word} / {mob} / action{action} tick{actual_tick}',fill='#DCE2DD')
  d.text((12,im.height-18),'Native source poses. Real damage/cast scheduler remains unchanged; manual acceptance NOT_RUN.',fill='#91A382');im.save(O/f'native-{mob}-actions.png')
 # Close-ups carry markers from actual native hand transforms, not authored image positions.
 markers=json.loads((R/'build/weapon-grip-58/contact-points.json').read_text())
 for mob in ['karachun','morozko']:
  tex=Image.open(R/f'src/main/resources/assets/slavicmyths/textures/entity/{mob}.png').convert('RGB')
  keys=['a0_0','a1_5','a1_10','a2_10','a3_14','a7_24'] if mob=='karachun' else ['a0_0','a1_5','a1_10','a2_13','a7_7','a8_48']
  sheet=Image.new('RGB',(1500,900),'#202B30');d=ImageDraw.Draw(sheet)
  for i,key in enumerate(keys):
   yaw=-35;im=draw(after[mob][key],tex,yaw);mark=markers[mob][key][0];x,y,z=mark[1:];angle=math.radians(yaw);pitch=math.radians(7)
   px=215+(x*math.cos(angle)+z*math.sin(angle))*4;pz=z*math.cos(angle)-x*math.sin(angle);py=185+((y+4)*math.cos(pitch)-pz*math.sin(pitch))*4
   painter=ImageDraw.Draw(im);painter.ellipse((px-3,py-3,px+3,py+3),outline='#00FFFF',width=2)
   if mob=='karachun' and key.startswith(('a1_','a2_','a3_')):
    markL=markers[mob][key][1];lx,ly,lz=markL[1:];lpx=215+(lx*math.cos(angle)+lz*math.sin(angle))*4;lpz=lz*math.cos(angle)-lx*math.sin(angle);lpy=185+((ly+4)*math.cos(pitch)-lpz*math.sin(pitch))*4
    painter.ellipse((lpx-3,lpy-3,lpx+3,lpy+3),outline='#FF72DF',width=2)
   crop=Image.new('RGB',(170,170),'#202B30');crop.paste(im,(-int(px-85),-int(py-85)));im=crop.resize((400,400),Image.Resampling.NEAREST)
   xx=i%3*500;yy=i//3*445;sheet.paste(im,(xx+25,yy+25));d.text((xx+12,yy+7),mob+' / '+key+' / actual right palm centre',fill='#DCE2DD')
  d.text((12,885),'Cyan = actual right palm; magenta = actual left palm in 1..3. Native grip gap, NOT GAME SCREENSHOT.',fill='#91A382');sheet.save(O/f'native-{mob}-grip-closeups.png')
 # Actual preparation -> hit -> recovery for all three scythe swings, plus source contact markers.
 mob='karachun';tex=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/karachun.png').convert('RGB');im=Image.new('RGB',(2000,1360),'#202B30');d=ImageDraw.Draw(im)
 for row,action in enumerate([1,2,3]):
  for col,tick in enumerate([0,5,14 if action==3 else 10,28]):
   key=f'a{action}_{tick}';xx=col*500;yy=row*445;im.paste(draw(after[mob][key],tex,-35,scale=3,center_y=250),(xx,yy+25));d.text((xx+12,yy+7),f'action{action} / tick{tick} / '+('HIT' if col==2 else 'POSE'),fill='#DCE2DD')
 d.text((12,1342),'Three existing actions, original damage ticks10/10/14. Actual ModelPart poses; NOT GAME SCREENSHOT.',fill='#91A382');im.save(O/'native-karachun-three-swings.png')
 print(O)
if __name__=='__main__':main()
