"""Actual source/UV six-family and furniture comparisons. Offline, never a game capture."""
from pathlib import Path
from io import BytesIO
import json
from PIL import Image,ImageDraw
from decor106_preview import Sources,prepare
from trophy111_source_preview import render
from decor_all_woods import ROOT,TEXTURES,WOODS,base,name,raw,targets

OUT=ROOT/'docs/implementation/decor106-all-woods'
class WoodSources(Sources):
 def texture(self,ref):
  ns,path=ref.split(':',1);rel=path+'.png'
  if self.before and ns=='slavicmyths' and rel in targets():
   wood,kind,detail=targets()[rel]
   if kind=='alias':raise ValueError('Expected old active sign path was MISSING: '+rel)
   return Image.open(BytesIO(raw(rel))).convert('RGBA')
  # Avoid the linden-only override in the pilot's Sources.texture.
  previous=self.before;self.before=False
  try:return super().texture(ref)
  finally:self.before=previous

def items(names,output):
 sources=[WoodSources(True),WoodSources(False)];board=Image.new('RGB',(760,34+122*len(names)),'#30363c');d=ImageDraw.Draw(board)
 d.text((8,8),'OFFLINE actual source models/UV, BEFORE left / AFTER right, item 1x and 3x nearest.',fill='white');details={}
 for row,n in enumerate(names):
  y=34+row*122;d.text((8,y),n,fill='white');details[n]=[]
  for j,source in enumerate(sources):
   model,atlas=prepare(source,'slavicmyths:item/'+n);pic,pose=render(model,atlas,'gui',48);tiny,_=render(model,atlas,'gui',16);x=j*370+16;pic=pic.resize((96,96),Image.Resampling.NEAREST);board.paste(pic,(x,y+18),pic);board.paste(tiny,(x+125,y+24),tiny);large=tiny.resize((48,48),Image.Resampling.NEAREST);board.paste(large,(x+174,y+24),large)
   details[n].append({'pose':pose,'source_model':'assets/slavicmyths/models/item/'+n+'.json'})
 board.save(output);return details

def main():
 all_details={}
 catalog=[line.split('\t') for line in (ROOT/'src/main/resources/assets/slavicmyths/creative_catalog.tsv').read_text().splitlines() if len(line.split('\t'))==3]
 for wood in WOODS:
  names=[r[0] for r in catalog if r[2]=='wood_'+wood and not r[0].endswith(('_leaves','_sapling'))]
  all_details.update(items(names,OUT/f'{wood}-all-items-before-after.png'))
 furniture=['linden_table','linden_chair','linden_wardrobe','pine_table','pine_chair','pine_wardrobe','wooden_crate','wooden_tub','kitchen_table']
 furniture=[n for n in furniture if (ROOT/f'src/main/resources/assets/slavicmyths/models/item/{n}.json').is_file()]
 all_details.update(items(furniture,OUT/'furniture-before-after.png'))
 # Same original resolution; 3x nearest and 2x2 tiling inspect joints and end grain.
 surfaces=Image.new('RGB',(1100,36+240*len(WOODS)),'#30363c');d=ImageDraw.Draw(surfaces);d.text((8,8),'OFFLINE exact PNGs: before top / after bottom per family. Planks, bark, stripped, end grains.',fill='white')
 for row,wood in enumerate(WOODS):
  y=36+row*240;d.text((8,y),wood,fill='white')
  for col,kind in enumerate(['planks','log','stripped_log','log_top','stripped_log_top']):
   rel='block/'+name(wood,kind)+'.png'
   for j,old in enumerate([True,False]):
    pic=Image.open(BytesIO(raw(rel))).convert('RGBA') if old else base(wood,kind);tiny=pic.resize((16,16),Image.Resampling.NEAREST);x=12+col*220;sy=y+20+j*108;surfaces.paste(tiny,(x,sy),tiny);scaled=pic.resize((pic.width*3,pic.height*3),Image.Resampling.NEAREST);surfaces.paste(scaled,(x+30,sy),scaled)
    tile=Image.new('RGBA',(pic.width*2,pic.height*2))
    for tx in [0,pic.width]:
     for ty in [0,pic.height]:tile.paste(pic,(tx,ty))
    tile=tile.resize((72,72),Image.Resampling.NEAREST);surfaces.paste(tile,(x+134,sy),tile)
 surfaces.save(OUT/'six-family-surfaces-before-after.png')
 # Compact real-model result panel for the handoff, not a reference mockup.
 board=Image.new('RGB',(760,290),'#30363c');d=ImageDraw.Draw(board);d.text((8,8),'OFFLINE actual result: planks / log / stripped log per family (unchanged models).',fill='white');source=WoodSources(False)
 for i,w in enumerate(WOODS):
  x=i*125+8;d.text((x,32),w,fill='white')
  for j,k in enumerate(['planks','log','stripped_log']):
   model,atlas=prepare(source,'slavicmyths:item/'+name(w,k));pic,_=render(model,atlas,'gui',48);pic=pic.resize((72,72),Image.Resampling.NEAREST);board.paste(pic,(x+15,50+j*78),pic)
 board.save(OUT/'six-family-result-models.png')
 (OUT/'rendered-items.json').write_text(json.dumps(all_details,indent=2)+'\n')

if __name__=='__main__':main()
