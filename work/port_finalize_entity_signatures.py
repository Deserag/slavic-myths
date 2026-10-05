from pathlib import Path
import re,zipfile
source=Path('work/port_finalize_types.py').read_text(encoding='utf-8-sig');ns={};exec(source[:source.index('changed=[]')],ns)
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-sources.jar');index={n[:-5].replace('/','.') for n in z.namelist() if n.endswith('.java')}
lex=re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/|\bnet\.[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)+\b')
def fqn(m):
 t=m.group();parts=t.split('.')
 if not t.startswith('net.minecraft.'):return t
 for end in range(len(parts),2,-1):
  head='.'.join(parts[:end])
  if head in index:return t
  name=parts[end-1];pre='.'.join(parts[:end-1]);candidate=ns['exact'].get(head,ns['classes'].get(name,ns['prefix'].get(pre,pre)+'.'+name))
  if candidate in index:return '.'.join([candidate]+parts[end:])
 return t
count=0
for p in Path('src/main/java').rglob('*.java'):
 old=s=p.read_text(encoding='utf-8');s=lex.sub(fqn,s)
 s=s.replace('AttributeSupplier.Attribute','AttributeSupplier.Builder').replace('BossEvent.Color','BossEvent.BossBarColor').replace('BossEvent.Overlay','BossEvent.BossBarOverlay')
 pattern=re.compile(r'protected void defineSynchedData\(\)\s*\{')
 while m:=pattern.search(s):
  start=m.end();end=start;depth=1
  while depth:
   if s[end]=='{':depth+=1
   elif s[end]=='}':depth-=1
   end+=1
  body=s[start:end-1].replace('super.defineSynchedData()','super.defineSynchedData(builder)').replace('entityData.define(','builder.define(')
  s=s[:m.start()]+'protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){'+body+s[end-1:]
 s=re.sub(r'(?:@Override\s*)?public\s+(?:net\.minecraft\.network\.protocol\.)?Packet<\?>\s+getAddEntityPacket\(\)\s*\{return (?:net\.minecraftforge\.fml\.network\.)?NetworkHooks\.getEntitySpawningPacket\(this\);\}', '',s)
 if 'NetworkHooks.' not in s:s=s.replace('import net.minecraftforge.fml.network.NetworkHooks;','')
 if s!=old:p.write_text(s,encoding='utf-8');count+=1
print('Migrated',count,'files: verified FQN class prefixes, attributes, synced data builders and inherited vanilla entity spawning')
entity=z.read('net/minecraft/world/entity/Entity.java').decode()
for term in ['getPassengerRidingPosition','positionRider(Entity','getVehicleAttachmentPoint','getControllingPassenger','causeFallDamage(','lerpTo(']:
 for m in list(re.finditer(re.escape(term),entity))[-3:]:print(entity[max(0,m.start()-80):m.start()+650])
