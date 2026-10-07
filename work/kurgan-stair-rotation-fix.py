import json,zipfile
from pathlib import Path
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar');template=Path('tools/templates/minecraft-1.21.1-stone-brick-stairs.json');template.parent.mkdir(parents=True,exist_ok=True);template.write_bytes(z.read('assets/minecraft/blockstates/stone_brick_stairs.json'))
p=Path('tools/kurgan_resources_096.py');s=p.read_text(encoding='utf-8-sig');start=s.index('    variants={}');end=s.index("    state={'variants':variants}",start)
s=s[:start]+'''    native=json.loads(Path('tools/templates/minecraft-1.21.1-stone-brick-stairs.json').read_text(encoding='utf-8'))
    variants={key:{**value,'model':value['model'].replace('minecraft:block/stone_brick_stairs',model)} for key,value in native['variants'].items()}
'''+s[end:];p.write_text(s,encoding='utf-8')
p=Path('tools/verify_kurgan_096.py');s=p.read_text(encoding='utf-8-sig');needle="additions=json.loads((ROOT/'docs/kurgan/registry-additions-0.9.6.json')";start=s.index(needle);s=s[:start]+'''native=json.loads((ROOT/'tools/templates/minecraft-1.21.1-stone-brick-stairs.json').read_text(encoding='utf-8'))
for row in stones:
 name=row['name']+'_stairs';file=RES/'assets/slavicmyths/blockstates'/(name+'.json')
 if file.exists():
  expected={'variants':{key:{**value,'model':value['model'].replace('minecraft:block/stone_brick_stairs','slavicmyths:block/'+name)} for key,value in native['variants'].items()}}
  require(json.loads(file.read_text(encoding='utf-8'))==expected,'Stair rotation differs from real Minecraft 1.21.1 '+name)
'''+s[start:];p.write_text(s,encoding='utf-8')
