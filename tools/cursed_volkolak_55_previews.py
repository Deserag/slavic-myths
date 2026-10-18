"""Actual baked model geometry/UV comparisons; never Minecraft screenshots."""
from pathlib import Path
from PIL import Image,ImageDraw
import gzip,json,io,subprocess
from grey_wolf_54_previews import draw
R=Path(__file__).resolve().parents[1];OUT=R/'docs/implementation/cursed-volkolak-55'
def load(path,depth):
 data=json.loads(gzip.decompress(path.read_bytes()))
 # Exact renderer depth scale, both old and new renderer remain 0.85.
 return {k:[[[v[0],v[1],v[2]*depth,v[3],v[4]] for v in f] for f in fs] for k,fs in data.items()}
def main():
 before=load(OUT/'before-native.json.gz',.85);after=load(R/'build/cursed-55/after-native.json.gz',.68)
 old=Image.open(io.BytesIO(subprocess.check_output(['git','show','957b1055:src/main/resources/assets/slavicmyths/textures/entity/cursed_volkolak.png'],cwd=R))).convert('RGB');new=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/cursed_volkolak.png').convert('RGB')
 canvas=Image.new('RGB',(2000,1800),'#202B30');d=ImageDraw.Draw(canvas)
 for row,(data,tex,key,label) in enumerate([(before,old,'quad','BEFORE QUAD'),(after,new,'quad','AFTER QUAD'),(before,old,'upright','BEFORE UPRIGHT'),(after,new,'upright','AFTER UPRIGHT')]):
  for col,yaw in enumerate([0,90,180,-35]):
   canvas.paste(draw(data[key],tex,yaw),(col*500,row*445+25));d.text((col*500+12,row*445+7),label+' / '+str(yaw),fill='#DCE2DD')
 d.text((12,1785),'Actual native ModelPart + UV; actual renderer depth before 0.85 / after 0.68. Same scale. NOT GAME SCREENSHOTS.',fill='#91A382');canvas.save(OUT/'native-before-after.png')
 keys=list(after);canvas=Image.new('RGB',(2000,445*((len(keys)+3)//4)+20),'#202B30');d=ImageDraw.Draw(canvas)
 for i,key in enumerate(keys):
  x=i%4*500;y=i//4*445;canvas.paste(draw(after[key],new,-35),(x,y+25));d.text((x+12,y+7),key.upper(),fill='#DCE2DD')
 canvas.save(OUT/'native-poses.png')
 canvas=Image.new('RGB',(3000,900),'#202B30');d=ImageDraw.Draw(canvas)
 for row,(data,tex,label) in enumerate([(before,old,'BEFORE'),(after,new,'AFTER')]):
  for col,tick in enumerate([0,19,49,87,102,131]):
   canvas.paste(draw(data['escape_'+str(tick)],tex,-35),(col*500,row*445+25));d.text((col*500+12,row*445+7),label+' ESCAPE '+str(tick),fill='#DCE2DD')
 canvas.save(OUT/'escape-before-after.png');print(OUT)
if __name__=='__main__':main()
