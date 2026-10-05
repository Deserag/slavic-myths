from pathlib import Path
r=Path('src/main/java/org/slavicmyths');names=['ambient','talk','warn','stir','open','close','hurt','step','hut_creak','hut_step']
p=r/'registry/ModSounds.java';s=p.read_text();i=s.rfind('}');s=s[:i]+''.join(f' public static final RegistryObject<SoundEvent> YAGA_{n.upper()}=sound("yaga_{n}");\n' for n in names)+s[i:];p.write_text(s,'utf-8')
p=r/'yaga/BabaYaga.java';s=p.read_text();s=s.replace('SoundEvents.WOODEN_DOOR_OPEN','org.slavicmyths.registry.ModSounds.YAGA_HUT_CREAK.get()').replace('SoundEvents.IRON_GOLEM_STEP','org.slavicmyths.registry.ModSounds.YAGA_HUT_STEP.get()').replace('playSound(SoundEvents.WITCH_AMBIENT,.45F,.55F)','playSound(org.slavicmyths.registry.ModSounds.YAGA_OPEN.get(),.45F,.55F)').replace('expelled?SoundEvents.WITCH_CELEBRATE:SoundEvents.WITCH_HURT','expelled?org.slavicmyths.registry.ModSounds.YAGA_WARN.get():org.slavicmyths.registry.ModSounds.YAGA_HURT.get()').replace('return SoundEvents.WITCH_AMBIENT;','return org.slavicmyths.registry.ModSounds.YAGA_AMBIENT.get();').replace('SoundEvents.BONE_BLOCK_STEP','org.slavicmyths.registry.ModSounds.YAGA_STEP.get()');p.write_text(s,'utf-8')
p=r/'yaga/YagaMenu.java';s=p.read_text().replace('npc.playSound(SoundEvents.WITCH_AMBIENT,.25F,.7F)','npc.playSound(org.slavicmyths.registry.ModSounds.YAGA_CLOSE.get(),.25F,.7F)').replace('action>=200?SoundEvents.EXPERIENCE_ORB_PICKUP:SoundEvents.WITCH_AMBIENT','action>=200?SoundEvents.EXPERIENCE_ORB_PICKUP:org.slavicmyths.registry.ModSounds.YAGA_TALK.get()').replace('SoundEvents.BREWING_STAND_BREW','org.slavicmyths.registry.ModSounds.YAGA_STIR.get()');p.write_text(s,'utf-8')
p=r/'client/BabaYagaModel.java';s=p.read_text().replace('case 1:p.get("fore-1")','case 1:p.get("chin").y+=(float)Math.sin(t*.5)*.18F*wave;p.get("fore-1")');p.write_text(s,'utf-8')
p=r/'yaga/YagaHut.java';s=p.read_text().replace('if(d.placed||d.anchor!=null)return;','if(d.placed||d.anchor!=null||d.examined.size()>=64)return;');p.write_text(s,'utf-8')
p=Path('tools/yaga_093.py');s=p.read_text();s+='''
# Semantic sound IDs use vanilla event fallbacks, with our own localized context.
sound_defs={
 'ambient':('entity.witch.ambient','Яга бормочет','Yaga mutters'),
 'talk':('entity.witch.ambient','Яга отвечает','Yaga answers'),
 'warn':('entity.witch.celebrate','Яга прогоняет гостя','Yaga dismisses a guest'),
 'stir':('block.brewing_stand.brew','Котёл Яги бурлит','Yaga’s cauldron bubbles'),
 'open':('block.chest.open','Яга принимает гостя','Yaga welcomes a guest'),
 'close':('block.chest.close','Яга прощается','Yaga bids farewell'),
 'hurt':('entity.witch.hurt','Яга предупреждает','Yaga warns'),
 'step':('block.bone_block.step','Костяная нога стучит','A bone leg taps'),
 'hut_creak':('block.wooden_door.open','Избушка скрипит','The hut creaks'),
 'hut_step':('entity.iron_golem.step','Избушка топает','The hut stamps')}
p=A/'sounds.json';sounds=json.loads(p.read_text('utf-8'))
for name,(event,ru,en) in sound_defs.items():sounds['yaga_'+name]={'subtitle':'subtitles.slavicmyths.yaga_'+name,'sounds':[{'name':'minecraft:'+event,'type':'event'}]}
out(p,sounds)
for lang,i in [('ru_ru',1),('en_us',2)]:
 p=A/f'lang/{lang}.json';loc=json.loads(p.read_text('utf-8'));loc.update({'subtitles.slavicmyths.yaga_'+n:v[i] for n,v in sound_defs.items()});out(p,loc)
''';p.write_text(s,'utf-8')
