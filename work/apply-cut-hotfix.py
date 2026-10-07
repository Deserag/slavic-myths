from pathlib import Path
import json
p=Path('src/main/java/org/slavicmyths/registry/ModEffects.java');s=p.read_text(encoding='utf-8-sig');s=s.replace(' private static final class CurseEffect',' public static final DeferredHolder<MobEffect,MobEffect> CUT=EFFECTS.register("cut",org.slavicmyths.combat.CutEffect::new);\n private static final class CurseEffect');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/kurgan/BurialRecords.java');s=p.read_text(encoding='utf-8-sig');s=s.replace('instances.put(i.id,i);index(i);setDirty();','for(var r:i.plan.rooms)i.encounters.computeIfAbsent(r.id,key->new KurganEncounterState()).prepare(KurganRoster.roster(i,r));instances.put(i.id,i);index(i);setDirty();');p.write_text(s,encoding='utf-8')
root=Path('src/main/resources')
def output(path,data):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
output(Path('data/slavicmyths/damage_type/cut.json'),{'message_id':'slavic_cut','scaling':'never','exhaustion':0.0})
output(Path('data/slavicmyths/tags/entity_type/bleed_immune.json'),{'replace':False,'values':['#minecraft:undead','slavicmyths:nav','slavicmyths:kurgan_druzhinnik','slavicmyths:kurgan_voevoda','slavicmyths:buried_volkhv','slavicmyths:unresting_prince','slavicmyths:upyr']})
for lang in ['ru_ru','en_us']:
 p=root/'assets/slavicmyths/lang'/f'{lang}.json';data=json.loads(p.read_text(encoding='utf-8-sig'))
 data['effect.slavicmyths.cut']='Порез' if lang=='ru_ru' else 'Cut'
 data['death.attack.slavic_cut']='%1$s погибает от порезов' if lang=='ru_ru' else '%1$s succumbed to cuts'
 data['death.attack.slavic_cut.player']='%1$s погибает от порезов, нанесённых %2$s' if lang=='ru_ru' else '%1$s succumbed to cuts inflicted by %2$s'
 key='tooltip.slavicmyths.nightingale_dagger';data[key]=data.get(key,'')+(' Порез: до 5 зарядов; каждое попадание обновляет общий таймер до 5 сек. 0,25 урона/сек. за заряд. Нежить невосприимчива.' if lang=='ru_ru' else ' Cut: up to 5 shared stacks; each hit refreshes all stacks to 5s. 0.25 damage/sec per stack. Undead are immune.')
 p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
# A small native pixel status icon, not an equipment redesign.
import struct,zlib
pixels=bytearray()
for y in range(18):
 pixels.append(0)
 for x in range(18):
  slash=any(abs(x-(base+(15-y)//3))<=0 and 3<=y<=14 for base in [4,8,12])
  pixels.extend((137,32,44,255) if slash else (0,0,0,0))
def chunk(t,d):return struct.pack('>I',len(d))+t+d+struct.pack('>I',zlib.crc32(t+d)&0xffffffff)
p=root/'assets/slavicmyths/textures/mob_effect/cut.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',18,18,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(pixels))+chunk(b'IEND',b''))
# Normalize files written by Windows PowerShell without BOM for javac.
for file in ['kurgan/KurganRoster.java','kurgan/KurganEncounterState.java','kurgan/KurganSurface.java','combat/CutEffect.java']:
 p=Path('src/main/java/org/slavicmyths')/file;p.write_text(p.read_text(encoding='utf-8-sig'),encoding='utf-8')
