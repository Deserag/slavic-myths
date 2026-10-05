from pathlib import Path
import json
base=Path('src/main/java/org/slavicmyths')
common='''import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
'''
(base/'swamp/SwampStructures.java').write_text('''package org.slavicmyths.swamp;
import java.util.*;
'''+common+'''public final class SwampStructures {
 public static final DeferredRegister<StructureType<?>> STRUCTURES=DeferredRegister.create(Registries.STRUCTURE_TYPE,"slavicmyths");
 public static final DeferredRegister<StructurePieceType> PIECES=DeferredRegister.create(Registries.STRUCTURE_PIECE,"slavicmyths");
 public static final String[] IDS={"swamp_hut","abandoned_settlement","bog_causeway","flooded_shrine","fishing_camp","underwater_ruins","swamp_remnants"};
 public static final Map<String,DeferredHolder<StructureType<?>,StructureType<SwampStructure>>> TYPES=new LinkedHashMap<>();
 public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=PIECES.register("swamp_piece",()->(context,tag)->new SwampPiece(context.structureTemplateManager(),tag));
 static{for(String id:IDS)TYPES.put(id,STRUCTURES.register(id,()->()->SwampStructure.codec(id)));}
 public static Structure get(net.minecraft.server.level.ServerLevel world,String id){return world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));}
 public static boolean occupied(net.minecraft.world.level.WorldGenLevel world,net.minecraft.core.BlockPos pos){
 if(!(world instanceof net.minecraft.server.level.WorldGenRegion region))return false;
 var manager=world.getLevel().structureManager().forWorldGenRegion(region);
 for(String id:IDS){Structure structure=get(world.getLevel(),id);if(structure!=null&&manager.startsForStructure(net.minecraft.core.SectionPos.of(pos),structure).anyMatch(net.minecraft.world.level.levelgen.structure.StructureStart::isValid))return true;}return false;
 }
 private SwampStructures(){}
}
''',encoding='utf-8')
(base/'bandit/CampStructures.java').write_text('''package org.slavicmyths.bandit;
'''+common+'''import org.slavicmyths.swamp.SwampStructures;
public final class CampStructures {
 public static final DeferredHolder<StructureType<?>,StructureType<CampStructure>> SMALL=SwampStructures.STRUCTURES.register("bandit_camp_small",()->()->CampStructure.codec(false));
 public static final DeferredHolder<StructureType<?>,StructureType<CampStructure>> MEDIUM=SwampStructures.STRUCTURES.register("bandit_camp_medium",()->()->CampStructure.codec(true));
 public static final DeferredHolder<StructureType<?>,StructureType<LargeCampStructure>> LARGE=SwampStructures.STRUCTURES.register("bandit_camp_large",()->()->LargeCampStructure.CODEC);
 public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("bandit_camp_piece",()->(context,tag)->new CampPiece(context.structureTemplateManager(),tag));
 public static final DeferredHolder<StructurePieceType,StructurePieceType> LARGE_PIECE=SwampStructures.PIECES.register("large_camp_piece",()->(context,tag)->new LargeCampPiece(context.structureTemplateManager(),tag));
 public static void register(){}
 private CampStructures(){}
}
''',encoding='utf-8')
(base/'kurgan/KurganStructures.java').write_text('''package org.slavicmyths.kurgan;
'''+common+'''import org.slavicmyths.swamp.SwampStructures;
public final class KurganStructures {
 public static final DeferredHolder<StructureType<?>,StructureType<KurganStructure>> SMALL=SwampStructures.STRUCTURES.register("kurgan_small",()->()->KurganStructure.codec(0));
 public static final DeferredHolder<StructureType<?>,StructureType<KurganStructure>> WARRIOR=SwampStructures.STRUCTURES.register("kurgan_warrior",()->()->KurganStructure.codec(1));
 public static final DeferredHolder<StructureType<?>,StructureType<KurganStructure>> GREAT=SwampStructures.STRUCTURES.register("kurgan_great",()->()->KurganStructure.codec(2));
 public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=SwampStructures.PIECES.register("kurgan_shell",()->(context,tag)->new KurganPiece(context.structureTemplateManager(),tag));
 public static final DeferredHolder<StructurePieceType,StructurePieceType> DUNGEON=SwampStructures.PIECES.register("kurgan_dungeon",()->(context,tag)->new KurganDungeonPiece(context.structureTemplateManager(),tag));
 public static void register(){}
 public static StructureType<KurganStructure> type(int i){return i==0?SMALL.get():i==1?WARRIOR.get():GREAT.get();}
 public static String id(int i){return i==0?"kurgan_small":i==1?"kurgan_warrior":"kurgan_great";}
 private KurganStructures(){}
}
''',encoding='utf-8')
p=base/'SlavicMyths.java';s=p.read_text(encoding='utf-8').replace('org.slavicmyths.swamp.SwampStructures.STRUCTURES.register(bus);', 'org.slavicmyths.swamp.SwampStructures.STRUCTURES.register(bus);\n        org.slavicmyths.swamp.SwampStructures.PIECES.register(bus);')
for name in ['swamp.SwampStructures','bandit.CampStructures','kurgan.KurganStructures']:s=s.replace('        event.enqueueWork(org.slavicmyths.'+name+'::setup);\n','')
p.write_text(s,encoding='utf-8')
r=Path('src/main/resources/data/slavicmyths')
def write(folder,id,value):
 p=r/folder/(id+'.json');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n',encoding='utf-8')
swamp=['swamp_hut','abandoned_settlement','bog_causeway','flooded_shrine','fishing_camp','underwater_ruins','swamp_remnants']
alltypes=[(id,[24,52,18,48,30,22,10][i],[24,52,18,48,30,22,10][i]//2,730031+i*619, ['core_swamp','core_river']if id=='underwater_ruins'else ['core_swamp']) for i,id in enumerate(swamp)]
alltypes +=[(id,sp,se,sa,['core_plains','core_forest','core_taiga']) for id,sp,se,sa in [('bandit_camp_small',48,16,803701),('bandit_camp_medium',64,24,803729),('bandit_camp_large',192,64,813797)]]
alltypes +=[(id,64,24,841973,['core_plains']) for id in ['kurgan_small','kurgan_warrior','kurgan_great']]
for id,spacing,separation,salt,tags in alltypes:
 vals=['#slavicmyths:worldgen/'+tag for tag in tags]
 if id.startswith('kurgan_'):vals.append('minecraft:birch_forest')
 write('tags/worldgen/biome','structures/'+id,{'replace':False,'values':vals})
 write('worldgen/structure',id,{'type':'slavicmyths:'+id,'biomes':'#slavicmyths:structures/'+id,'step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'none'})
 write('worldgen/structure_set',id,{'structures':[{'structure':'slavicmyths:'+id,'weight':1}],'placement':{'type':'minecraft:random_spread','spacing':spacing,'separation':separation,'salt':salt,'spread_type':'linear'}})
