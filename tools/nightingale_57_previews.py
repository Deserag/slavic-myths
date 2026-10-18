"""Actual baked model/UV projections for #57; never game screenshots."""
from pathlib import Path
from PIL import Image,ImageDraw
import gzip,json,io,subprocess
from grey_wolf_54_previews import draw
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/nightingale-57'
def main():
 a=json.loads(gzip.decompress((R/'build/nightingale-57/after-native.json.gz').read_bytes()));b=json.loads(gzip.decompress((O/'before-native.json.gz').read_bytes()))
 textures={}
 for i,form in enumerate(['first','second'],1):
  path=f'src/main/resources/assets/slavicmyths/textures/entity/nightingale_phase{i}.png'
  old=Image.open(io.BytesIO(subprocess.check_output(['git','show','a3a47d1e:'+path],cwd=R))).convert('RGB');new=Image.open(R/path).convert('RGB');textures[form]=new
  im=Image.new('RGB',(2000,900),'#202B30');d=ImageDraw.Draw(im)
  for row,(data,tex) in enumerate([(b,old),(a,new)]):
   for col,(yaw,label) in enumerate([(0,'FRONT'),(90,'SIDE'),(180,'BACK'),(-35,'3/4')]):
    im.paste(draw(data[form]['idle'],tex,yaw,scale=8,center_y=90),(col*500,row*445+25));d.text((col*500+12,row*445+7),('BEFORE' if row==0 else 'AFTER')+' / '+form.upper()+' / '+label,fill='#DCE2DD')
  d.text((12,885),'Native ModelPart geometry and actual UV. Same scale. NOT MINECRAFT SCREENSHOTS.',fill='#91A382');im.save(O/f'native-{form}-before-after.png')
 groups={
 'combo':[('first',k) for k in ['quick_10','combo_0','combo_8','combo_20','combo_32','combo_36']],
 'inhale-wave':[('first',f'wind6_{t}') for t in [0,8,15,16,22,28]]+[('second',f'wind19_{t}') for t in [0,8,15,16,22,28]],
 'whirlwind':[('second',f'wind17_{t}') for t in [0,8,15,16,22,28]],
 'dash-onslaught':[('second',f'dash_{t}') for t in [0,8,18,22]]+[('second',f'onslaught_{t}') for t in [8,18,22,34,46]],
 'leap':[('first',f'leap_{t}') for t in [0,8,10,18,32]],
 'air-dive':[('second',f'air_{t}') for t in [0,10,18,36,44]],
 'transform-break':[(('first' if t<20 else 'second'),f'transform_{t}') for t in [0,19,20,30,40]]+[('first',f'break_{t}') for t in [0,8,18,24]],
 'death':[(form,f'death_{t}') for form in ['first','second'] for t in [0,10,30,45]]}
 for name,keys in groups.items():
  rows=(len(keys)+3)//4;im=Image.new('RGB',(2000,rows*445+25),'#202B30');d=ImageDraw.Draw(im)
  for i,(form,k) in enumerate(keys):
   x=i%4*500;y=i//4*445;im.paste(draw(a[form][k],textures[form],-35,scale=7,center_y=120),(x,y+25));d.text((x+12,y+7),form.upper()+' / '+k,fill='#DCE2DD')
  d.text((12,rows*445+8),'Native timing samples, no game launch. Collision/AI remain server-owned. Owner acceptance NOT_RUN.',fill='#91A382');im.save(O/f'native-{name}.png')
 print(O)
if __name__=='__main__':main()
