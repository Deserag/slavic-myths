from pathlib import Path
p=Path('src/main/java/org/slavicmyths/kurgan/KurganLayout.java');s=p.read_text();s=s.replace('case RELIQUARY->{rx=3;rz=4;', 'case RELIQUARY->{rx=p.tier==0?2:3;rz=p.tier==2?4:3;');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/kurgan/KurganDebug.java');s=p.read_text();needle='        KurganPlan.Room room=i.room(pos);';s=s.replace(needle,'        if(selected.plan.formatVersion==2)source.sendSuccess(()->Component.literal("layout=v2 "+KurganLayout.metrics(selected.plan)),false);\n'+needle);p.write_text(s,encoding='utf-8')
p=Path('tools/kurgan_resources_096.py');s=p.read_text(encoding='utf-8-sig');marker="put(Path('docs/kurgan/registry-additions-0.9.6.json')";i=s.index(marker);s=s[:i]+'''put(A/'models/block/pale_blue_kurgan_stone_variant.json',{'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/pale_blue_kurgan_stone_variant'}})
put(A/'blockstates/pale_blue_kurgan_stone.json',{'variants':{'':[{'model':'slavicmyths:block/pale_blue_kurgan_stone','weight':3},{'model':'slavicmyths:block/pale_blue_kurgan_stone_variant','weight':1}]}})
'''+s[i:];p.write_text(s,encoding='utf-8')
