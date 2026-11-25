"""Contact sheets of real game frames only; crops enlarge the player for grip inspection."""
from pathlib import Path
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];S=R/'work/desert-weapons-visual-v2/screenshots';O=R/'art/desert-weapons/revision-2/game'
IDS=['desert_sword','desert_axe','desert_mace','desert_halberd','desert_scythe','scorpion_double_sting','stinger_dagger','sand_sling','desert_glass_blade','dune_mace','buried_hammer','hala_solar_staff','colony_scepter','four_winds_fists','lamya_weapon','quicksand_whip','mirage_bow']
groups={'basic-weapons':[0,1,2,3,4],'legendary-weapons':[10,12,13,14],'special-weapons':[5,6,7,8,9,11,15,16]}
for name,indices in groups.items():
 board=Image.new('RGB',(1120,len(indices)*300+40),'#171c22');d=ImageDraw.Draw(board);d.text((16,12),'REAL MINECRAFT CAPTURES | first person / third person idle / third person attack',fill='white')
 for row,i in enumerate(indices):
  id=IDS[i];y=40+row*300;d.text((12,y+6),id,fill='#ffd16e')
  for col,sample in enumerate([28,28,38]):
   scene=i*2+(0 if col==0 else 1);prefix='motion'
   grips=['desert_halberd','desert_scythe','buried_hammer','lamya_weapon']
   if id in grips and (O/'grip-complete.txt').exists():scene=grips.index(id)*2+(0 if col==0 else 1);prefix='grip'
   p=S/f'{prefix}-{scene:02d}-{id}-{sample}.png'
   im=Image.open(p).convert('RGB')
   if col==0:im=im.resize((480,270),Image.Resampling.LANCZOS);x=8
   else:im=im.crop((440,270,840,610)).resize((300,255),Image.Resampling.NEAREST);x=500+(col-1)*308
   board.paste(im,(x,y+24))
 board.save(O/f'{name}.png')
# Complete original frames for the most important regressions.
import shutil
for src,dst in [('65-mirage_bow-28.png','inventory-and-bow-tooltip.png'),('motion-36-bow_draw-28.png','bow-draw-first-person.png'),('motion-37-bow_draw-28.png','bow-draw-third-person.png'),('motion-26-four_winds_fists-28.png','four-winds-first-person.png'),('motion-27-four_winds_fists-38.png','four-winds-third-person-attack.png'),('grip-01-desert_halberd-28.png','two-handed-third-person.png'),('grip-00-desert_halberd-28.png','two-handed-first-person.png')]:shutil.copyfile(S/src,O/dst)
print('Game evidence sheets and unmodified key frames saved')
