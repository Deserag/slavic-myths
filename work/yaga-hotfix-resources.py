from pathlib import Path
r=Path('src/main/java/org/slavicmyths')
(r/'yaga/YagaSpawnEgg.java').write_text('''package org.slavicmyths.yaga;
import java.util.List;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraft.util.text.*;
public final class YagaSpawnEgg extends net.minecraftforge.common.ForgeSpawnEggItem {
 public YagaSpawnEgg(Item.Properties p){super(org.slavicmyths.registry.ModEntities.BABA_YAGA,0x493B33,0x8D493B,p);}
 @Override public void appendHoverText(ItemStack s,World w,List<ITextComponent> lines,net.minecraft.client.util.ITooltipFlag flag){super.appendHoverText(s,w,lines,flag);lines.add(new TranslationTextComponent(getDescriptionId()+".effect").withStyle(TextFormatting.GRAY));}
}
''','utf-8')
p=r/'registry/ModItems.java';s=p.read_text().replace('new net.minecraftforge.common.ForgeSpawnEggItem(ModEntities.BABA_YAGA,0x493B33,0x8D493B,properties())','new org.slavicmyths.yaga.YagaSpawnEgg(properties())');p.write_text(s,'utf-8')
p=Path('tools/yaga_093.py');s=p.read_text();s+='''
# Generation hotfix and vanilla-tinted Baba Yaga egg; scoped to newly added resources.
egg='baba_yaga_spawn_egg'
out(A/f'models/item/{egg}.json',{'parent':'minecraft:item/template_spawn_egg'})
reasons={
 'chunks':('Не вся площадка загружена. Подойди ближе или увеличь дальность прорисовки.','The clearing is not fully loaded. Move closer or increase view distance.'),
 'surface':('Не найдена твёрдая поверхность.','No solid surface found.'),
 'water':('На площадке обнаружена вода или другая жидкость.','Water or another liquid occupies the site.'),
 'slope':('Слишком большой перепад высот.','The terrain is too steep.'),
 'height':('Дом не помещается по высоте мира.','The hut exceeds the world height.'),
 'container':('На месте дома есть контейнер или блок с данными.','A container or block entity occupies the site.'),
 'protected':('На месте дома есть защищённый блок мода.','A protected mod block occupies the site.'),
 'obstruction':('На месте дома есть препятствие или постройка.','An obstruction or building occupies the site.'),
 'support':('У одного из блоков дома нет опоры; размещение отменено.','A hut block has no support; placement was rolled back.'),
 'write':('Мир отклонил установку блока; размещение отменено.','The world refused a block; placement was rolled back.'),
 'npc':('Спавн Яги отклонён; размещение отменено.','Yaga’s spawn was refused; placement was rolled back.'),
 'exists':('Избушка уже существует в этом мире.','A hut already exists in this world.'),
 'internal':('Ошибка генератора, подробности в latest.log.','Generator error; see latest.log.'),
 'rollback':('Ошибка отката, подробности в latest.log.','Rollback error; see latest.log.')}
for lang,i in [('ru_ru',0),('en_us',1)]:
 p=A/f'lang/{lang}.json';loc=json.loads(p.read_text('utf-8'));loc['item.slavicmyths.'+egg]=['Яйцо призыва Бабы-яги','Baba Yaga spawn egg'][i];loc['item.slavicmyths.'+egg+'.effect']=['В обычном мире открывает разговор и поручения. Котёл работает только в избушке.','Offers conversation and errands in the Overworld. Brewing requires the hut.'][i]
 loc['yaga.fail.dimension']=['Избушка и услуги Яги доступны в обычном мире.','The hut and Yaga’s services are available in the Overworld.'][i]
 loc['yaga.generation.failed']=['Избушка не создана: %s Место: %s','Hut not generated: %s Site: %s'][i]
 loc.update({'yaga.generation.reason.'+key:v[i] for key,v in reasons.items()});out(p,loc)
p=ROOT/'docs/verification/yaga-0.9.3.json';m=json.loads(p.read_text('utf-8'));m['items'].append(egg);m['placement_hotfix']=True;out(p,m)
''';p.write_text(s,'utf-8')
p=Path('tools/verify_yaga_093.py');s=p.read_text();a=s.index("hut=source('yaga/YagaHut.java')");b=s.index("utility=source",a);s=s[:a]+'''hut=source('yaga/YagaHut.java');placement=source('yaga/YagaPlacement.java')
assert placement.index('!w.hasChunksAt')<placement.index('int min=')
assert all(n in hut for n in ['getNoiseBiome','YagaPlacement.prepare','YagaPlacement.commit','w.addFreshEntity(npc)','d.placed=true'])
assert '2|16' in placement and 'y>=0' in placement and 'site.old.entrySet()' in placement
assert all(n not in hut+placement for n in ['setChunkForced','forceChunk','getAllEntities'])
assert 'baba_yaga_spawn_egg' in items and 'SpawnReason.SPAWN_EGG' in source('yaga/BabaYaga.java')
assert 'YagaEggSummoned' in source('yaga/BabaYaga.java') and 'eggSummoned||d.placed' in source('yaga/BabaYaga.java')
assert read(A/'models/item/baba_yaga_spawn_egg.json')['parent']=='minecraft:item/template_spawn_egg'
'''+s[b:];p.write_text(s,'utf-8')
