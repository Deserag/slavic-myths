"""Native cuboid drinking vessels, preserving component-selected container variants."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
A=Path(__file__).resolve().parents[1]/'src/main/resources/assets/slavicmyths'
COLORS={'light_beer':'#d7a52c','dark_beer':'#37271d','hopped_beer':'#c87518','honey_beer':'#e0b53b','forest_beer':'#65713a','thunder_beer':'#d79223','veles_dark_beer':'#30212e','witch_berry_beer':'#7d223f','kvass':'#583e27','berry_mors':'#8e294d'}
def cube(lo,hi,t):return {'from':lo,'to':hi,'faces':{side:{'texture':'#'+t} for side in ['north','south','east','west','up','down']}}
def main():
 for drink,color in COLORS.items():
  im=Image.new('RGBA',(16,16),color);d=ImageDraw.Draw(im)
  for y in [3,8,13]:d.line((2,y,12,y),fill=color)
  path=A/f'textures/item/{drink}_liquid.png';im.save(path)
  tex={'wood':'slavicmyths:block/household_wood','metal':'slavicmyths:block/household_metal','liquid':'slavicmyths:item/'+drink+'_liquid','foam':'minecraft:block/white_wool','glass':'minecraft:block/green_stained_glass','particle':'slavicmyths:block/household_wood'}
  # Hollow stave mug, metal hoops, open drink surface and connected handle.
  els=[cube([4,1,4],[11,2,11],'wood')]
  for lo,hi in [([4,2,4],[5,10,11]),([10,2,4],[11,10,11]),([5,2,4],[10,10,5]),([5,2,10],[10,10,11])]:els.append(cube(lo,hi,'wood'))
  els += [cube([5,2,5],[10,8.5,10],'liquid'),cube([5,8.5,5],[10,9,10],'foam')]
  for y in [2,7]:
   for lo,hi in [([3.8,y,3.8],[11.2,y+.6,4.1]),([3.8,y,10.9],[11.2,y+.6,11.2]),([3.8,y,4],[4.1,y+.6,11]),([10.9,y,4],[11.2,y+.6,11])]:els.append(cube(lo,hi,'metal'))
  els += [cube([11,7,6],[14,8,9],'wood'),cube([13,3,6],[14,7,9],'wood'),cube([11,2,6],[14,3,9],'wood')]
  display={'gui':{'rotation':[25,-35,0],'translation':[0,0,0],'scale':[1,1,1]},'ground':{'translation':[0,2,0],'scale':[.5,.5,.5]},'thirdperson_righthand':{'rotation':[0,90,0],'translation':[0,1,0],'scale':[.7,.7,.7]},'firstperson_righthand':{'rotation':[0,-30,0],'translation':[1,2,0],'scale':[.8,.8,.8]}}
  model={'textures':tex,'elements':els,'display':display,'overrides':[{'predicate':{'custom_model_data':1},'model':'slavicmyths:item/'+drink+'_bottled'}]}
  (A/f'models/item/{drink}_mug.json').write_text(json.dumps(model,indent=2)+'\n')
  bottle={'render_type':'minecraft:translucent','textures':tex,'elements':[cube([5,0,5],[11,9,11],'glass'),cube([6,1,6],[10,8,10],'liquid'),cube([6,9,6],[10,11,10],'glass'),cube([7,11,7],[9,14,9],'glass'),cube([6.7,14,6.7],[9.3,15,9.3],'wood')],'display':display}
  (A/f'models/item/{drink}_bottled.json').write_text(json.dumps(bottle,indent=2)+'\n')
if __name__=='__main__':main()
