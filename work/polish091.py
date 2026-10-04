from pathlib import Path
p=Path('tools/verify_hunt_091.py');s=p.read_text(encoding='utf8').replace('generate,ITEMS,SMALL,A,D,R','generate,ITEMS,SMALL,A,D,R,write');p.write_text(s,encoding='utf8')
p=Path('src/main/java/org/slavicmyths/client/ElementModel.java');s=p.read_text().replace('segments.add(neck);ModelRenderer prev=root;', 'segments.add(neck);ModelRenderer neck2=part(root,0,0,-15);box(neck2,1,-3,-3,-6,6,6,6);segments.add(neck2);ModelRenderer prev=root;').replace('length=i<3?11:9','length=i<3?10:8');p.write_text(s,encoding='utf8')
# Rain must use the main body's sky exposure, rather than treating a roof at body Y as outdoor rain.
p=Path('src/main/java/org/slavicmyths/hunt/ElementHuntMob.java');s=p.read_text();s=s.replace(' private void cue(SoundEvent s)', ' @Override public boolean wetRain(){return level.isRainingAt(blockPosition());}\n private void cue(SoundEvent s)');p.write_text(s,encoding='utf8')
