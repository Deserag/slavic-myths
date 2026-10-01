"""Targeted 0.8.0.1 checks. Does not start Minecraft or infer visual/gameplay results."""
from pathlib import Path
import hashlib,json,zipfile,subprocess,struct,zlib,sys
from woodlands_0801 import generate,SPECIES,KINDS,RES,ROOT,A,D

def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
def snapshot():return {str(p.relative_to(RES)):hashlib.sha256(p.read_bytes()).hexdigest()for p in RES.rglob('*')if p.is_file()}
def png_pixels(p):
 b=p.read_bytes();offset=8;data=b''
 while offset<len(b):
  n=struct.unpack('>I',b[offset:offset+4])[0];kind=b[offset+4:offset+8];payload=b[offset+8:offset+8+n];offset+=12+n
  if kind==b'IHDR':w,h=struct.unpack('>II',payload[:8])
  if kind==b'IDAT':data+=payload
 raw=zlib.decompress(data);return [tuple(raw[y*(w*4+1)+1+x*4:y*(w*4+1)+5+x*4])for y in range(h)for x in range(w)]

def run():
 before=snapshot();generate();assert before==snapshot(),'Non-reproducible woodland resources'
 manifest=read(ROOT/'docs/verification/woodlands-0.8.0.1.json');assert len(manifest['blocks'])==69 and len(manifest['items'])==66
 java=(ROOT/'src/main/java/org/slavicmyths/wood/Woodlands.java').read_text()
 for k in KINDS:
  assert ('add("'+k+'"')in java or ('blocks.put("'+k+'"')in java,k
 for s in SPECIES:
  for k in KINDS:
   name=s+'_'+k
   assert (A/f'blockstates/{name}.json').exists();assert(D/f'loot_tables/blocks/{name}.json').exists()
   if k!='wall_sign':assert(A/f'models/item/{name}.json').exists()
  # Use vanilla-derived state schemas, including all door/stair/button orientations.
  for k,n in [('log',3),('wood',3),('stripped_log',3),('stripped_wood',3),('door',32),('stairs',40),('slab',3),('button',24),('pressure_plate',2),('trapdoor',16)]:
   assert len(read(A/f'blockstates/{s}_{k}.json')['variants'])==n,(s,k)
  assert len(read(A/f'blockstates/{s}_fence.json')['multipart'])==5
  for kind in ['planks','stairs','slab','fence','fence_gate','door','trapdoor','pressure_plate','button','sign','wood','stripped_wood']:
   assert (D/f'recipes/{s}_{kind}.json').exists(),kind
  for folder in ['items','blocks']:
   logs=read(D/f'tags/{folder}/{s}_logs.json')['values'];assert len(logs)==4
   for kind,tag in [('log','logs_that_burn'),('planks','planks'),('leaves','leaves'),('sapling','saplings'),('stairs','wooden_stairs'),('slab','wooden_slabs'),('door','wooden_doors'),('trapdoor','wooden_trapdoors'),('fence','wooden_fences'),('button','wooden_buttons'),('pressure_plate','wooden_pressure_plates')]:
    values=read(RES/f'data/minecraft/tags/{folder}/{tag}.json')['values'];assert ('#slavicmyths:'+s+'_logs'if kind=='log'else 'slavicmyths:'+s+'_'+kind)in values
  loot=json.dumps(read(D/f'loot_tables/blocks/{s}_leaves.json'));assert all(k in loot for k in ['minecraft:fortune','minecraft:silk_touch','minecraft:shears',s+'_sapling'])
  assert 'minecraft:apple'not in loot
  door=json.dumps(read(D/f'loot_tables/blocks/{s}_door.json'));assert 'lower'in door
  slab=json.dumps(read(D/f'loot_tables/blocks/{s}_slab.json'));assert 'double'in slab and 'set_count'in slab
  assert len(png_pixels(A/f'textures/entity/signs/{s}.png'))==64*32
 # Geometry and alpha masks distinguish leaves even without their colors.
 masks=[tuple(p[3]for p in png_pixels(A/f'textures/block/{s}_leaves.png'))for s in SPECIES];assert len(set(masks))==4
 assert all(55<sum(a>0 for a in mask)<235 for mask in masks)
 for suffix in ['door_top','door_bottom','trapdoor','log','log_top','planks']:
  assert len({(A/f'textures/block/{s}_{suffix}.png').read_bytes()for s in SPECIES})==4
 assert read(A/'models/block/hanging_willow_leaves.json')['parent']=='minecraft:block/cross'
 rowan=read(A/'blockstates/rowan_leaves.json');assert set(rowan['variants'])=={'berries=false','berries=true'}
 assert 'rowan_berries'not in json.dumps(read(D/'loot_tables/blocks/rowan_leaves.json')),'Harvest state must not be retained in sheared item drops'
 assert read(D/'advancements/woodlands.json')['requirements']==[list(SPECIES)]
 assert read(D/'advancements/all_timber.json')['requirements']==[[s]for s in SPECIES]
 for folder in ['blocks','items']:
  for ns in ['minecraft','forge','slavicmyths']:
   for path in (RES/f'data/{ns}/tags/{folder}').rglob('*.json'):
    for v in read(path)['values']:
     if isinstance(v,str)and v.startswith('slavicmyths:'):
      assert v.split(':')[1] in manifest[folder] or (RES/'assets/slavicmyths'/('blockstates'if folder=='blocks'else'models/item')/(v.split(':')[1]+'.json')).exists(),v
 # Execute only pure geometry Java, no Forge/Minecraft bootstrap.
 out=ROOT/'build/tree-shape-check';out.mkdir(parents=True,exist_ok=True);jdk=ROOT/'.tools/jdk8/jdk8u504-b01/bin'
 subprocess.run([str(jdk/'javac.exe'),'-d',str(out),str(ROOT/'src/main/java/org/slavicmyths/wood/TreeShape.java'),str(ROOT/'tools/checks/TreeShapeCheck.java')],check=True)
 subprocess.run([str(jdk/'java.exe'),'-cp',str(out),'TreeShapeCheck'],check=True)
 with zipfile.ZipFile(ROOT/'build/libs/slavicmyths-0.8.0.1.jar')as jar:
  assert not any('TreeShapeCheck'in n or n.startswith('tools/')for n in jar.namelist())
  for path in RES.rglob('*'):
   if path.is_file()and path.name!='mods.toml':assert jar.read(path.relative_to(RES).as_posix())==path.read_bytes(),path
 print('PASS: 69 woodland blocks, 66 items; complete wood recipes/tags, state variants, leaf/shears/Fortune loot, berry-state separation, four alpha silhouettes, sign textures, advancement requirements, deterministic resources and production JAR equality.')
 print('Minecraft launches: 0. Geometry checks are not gameplay, visual QA, worldgen frequency measurements, or a real save/rejoin test.')
if __name__=='__main__':run()
