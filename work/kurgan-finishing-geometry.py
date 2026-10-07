from pathlib import Path
import json
A=Path('src/main/resources/assets/slavicmyths');p=A/'models/block/pale_blue_kurgan_stone_variant.json';p.write_text(json.dumps({'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/pale_blue_kurgan_stone_variant'}},indent=2)+'\n');(A/'blockstates/pale_blue_kurgan_stone.json').write_text(json.dumps({'variants':{'':[{'model':'slavicmyths:block/pale_blue_kurgan_stone','weight':3},{'model':'slavicmyths:block/pale_blue_kurgan_stone_variant','weight':1}]}},indent=2)+'\n')
p=Path('src/main/java/org/slavicmyths/kurgan/KurganArchitecture.java');s=p.read_text(encoding='utf-8-sig');s=s.replace('put(x,y,z,stone(l.palette,x,y,z));','put(x,y,z,l.style==2&&y<=s.y+2?KurganBlocks.stone(1,3):l.style==1&&side?KurganBlocks.stone(l.palette,2):stone(l.palette,x,y,z));',1)
s=s.replace('if(pass==2&&side&&i%6==0)', 'if(pass==2&&(i==0||i==l.steps.size()-1||i%6==0))put(x,s.y+h+1,z,KurganBlocks.get("carved_burial_stone").defaultBlockState());\n    if(pass==2&&side&&i%6==0)')
p.write_text(s,encoding='utf-8')
p=Path('tools/kurgan-tests/org/slavicmyths/verify/KurganReworkHeadless.java');s=p.read_text();s=s.replace('maps.add(n.toString());','var fingerprint=n.copy();fingerprint.remove("Seed");maps.add(fingerprint.toString());');p.write_text(s,encoding='utf-8')
p=Path('gradle.properties');s=p.read_text().replace('mod_version=0.9.5','mod_version=0.9.6');p.write_text(s,encoding='utf-8')
