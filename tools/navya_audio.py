"""Vanilla event references only. Never extracts/copies audio or runs quarantine generators."""
import json
from pathlib import Path
import zipfile

ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/slavicmyths'
profiles={
 'navka': ['particle.soul_escape','entity.allay.hurt','block.soul_sand.break','block.soul_sand.step','entity.evoker.prepare_attack','entity.evoker.cast_spell'],
 'rootling': ['block.nether_wood.place','block.cherry_wood.break','block.mangrove_roots.break','block.mangrove_roots.step','block.rooted_dirt.break','block.wood.break'],
 'bone_crawler': ['entity.skeleton.ambient','entity.skeleton.hurt','entity.skeleton.death','entity.skeleton.step','item.crossbow.loading_middle','entity.skeleton.shoot'],
 'navya_wisp': ['block.amethyst_block.chime','block.small_amethyst_bud.break','block.amethyst_cluster.break','block.amethyst_block.step','block.amethyst_block.chime','block.amethyst_block.chime'],
 'grove_guardian': ['block.mangrove_roots.place','block.bone_block.break','block.nether_wood.break','block.nether_wood.step','block.nether_wood.place','block.bone_block.fall']}
extra={'roots_warn':'block.rooted_dirt.hit','roots_rise':'block.mangrove_roots.break','roots_recede':'block.rooted_dirt.break',
       'shot_hit':'block.amethyst_cluster.hit','trunk_lift':'block.nether_wood.place','trunk_throw':'entity.breeze.shoot',
       'trunk_hit':'block.nether_wood.break','summon':'block.mangrove_roots.place','phase':'block.bone_block.break'}
ru={'idle':'шуршит','hurt':'ранен','death':'затихает','step':'ступает','warn':'готовит удар','strike':'атакует'}
names={'navka':('Навка','Navka'),'rootling':('Корневик','Rootling'),'bone_crawler':('Костяной ползун','Bone Crawler'),
       'navya_wisp':('Навий огонёк','Navya Wisp'),'grove_guardian':('Сторож чащи','Grove Guardian')}

def main():
 assets=Path.home()/'.gradle/caches/neoformruntime/assets'
 index=json.loads((assets/'indexes/17.json').read_text())['objects']
 sound_hash=index['minecraft/sounds.json']['hash']
 vanilla=json.loads((assets/'objects'/sound_hash[:2]/sound_hash).read_text())
 events={}
 for role,sources in profiles.items():
  for cue,source in zip(['idle','hurt','death','step','warn','strike'],sources):
   if role=='navya_wisp' and cue not in ['idle','hurt','death']:continue
   if role=='navka' and cue=='step':continue
   events[role+'.'+cue]=source
 events.update(extra)
 def files(event,seen=()):
  assert event in vanilla and event not in seen,event
  result=[]
  for entry in vanilla[event]['sounds']:
   if isinstance(entry,str):name=entry;kind='file'
   else:name=entry['name'];kind=entry.get('type','file')
   name=name.removeprefix('minecraft:')
   if kind=='event':result+=files(name,seen+(event,))
   else:
    key='minecraft/sounds/'+name+'.ogg';assert key in index,key
    digest=index[key]['hash'];assert (assets/'objects'/digest[:2]/digest).is_file(),key
    result.append({'asset':key,'sha1':digest})
  return result
 source_files={event:files(source) for event,source in events.items()}
 path=A/'sounds.json';sounds=json.loads(path.read_text(encoding='utf-8'))
 for event,source in events.items():
  sounds['navya.'+event]={'subtitle':'subtitles.slavicmyths.navya.'+event,'sounds':[{'name':'minecraft:'+source,'type':'event','volume':1.0}]}
 path.write_text(json.dumps(sounds,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 for locale,index in [('ru_ru',0),('en_us',1)]:
  path=A/'lang'/(locale+'.json');data=json.loads(path.read_text(encoding='utf-8'))
  for event in events:
   if '.' in event:
    role,cue=event.split('.',1);text=names[role][index]+' '+(ru[cue] if index==0 else {'idle':'rustles','hurt':'hurts','death':'fades','step':'steps','warn':'prepares an attack','strike':'strikes'}[cue])
   else:
    descriptions={'roots_warn':('Под землёй трещат корни','Roots crack below'),'roots_rise':('Корни поднимаются','Roots rise'),'roots_recede':('Корни уходят в землю','Roots recede'),
     'shot_hit':('Призрачный сгусток разбивается','Spectral glob impacts'),'trunk_lift':('Ствол поднимается','Trunk is lifted'),'trunk_throw':('Ствол летит','Trunk is thrown'),
     'trunk_hit':('Ствол разбивается','Trunk shatters'),'summon':('Просыпаются корни','Roots awaken'),'phase':('Сердцевина раскалывается','Core cracks')}
    text=descriptions[event][index]
   data['subtitles.slavicmyths.navya.'+event]=text
  path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 report=ROOT/'docs/implementation/navya-grove/audio-routing.json'
 report.write_text(json.dumps({'sources':'Vanilla 1.21.1 sound events, no copied OGG','hearing':'NOT_RUN','events':events,'actualVanillaOggSources':source_files},indent=2)+'\n',encoding='utf-8')
 print('Validated vanilla 1.21.1 event references:',len(events),'client hearing NOT_RUN')

if __name__=='__main__':main()
