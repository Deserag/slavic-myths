from pathlib import Path
r=Path('src/main/java/org/slavicmyths')
def write(p,s):(r/p).write_text(s,encoding='utf-8')
write('yaga/YagaHutPlan.java','''package org.slavicmyths.yaga;
import java.util.*;
import net.minecraft.util.math.BlockPos;
/** Pure, bounded hut blueprint. Positions are relative to ground level. */
public final class YagaHutPlan {
 public final LinkedHashMap<BlockPos,String> blocks=new LinkedHashMap<>();
 public static final BlockPos HOME=new BlockPos(0,4,2);
 private void b(int x,int y,int z,String id){blocks.put(new BlockPos(x,y,z),id.contains(":")?id:"minecraft:"+id);}
 public YagaHutPlan(){
  // Clear only the reserved cabin volume; terrain validation happens before placement.
  for(int x=-6;x<=6;x++)for(int z=-7;z<=7;z++)for(int y=1;y<=11;y++)b(x,y,z,"air");
  for(int x=-5;x<=5;x++)for(int z=-6;z<=6;z++)b(x,3,z,"spruce_planks");
  for(int y=4;y<=7;y++)for(int x=-5;x<=5;x++)for(int z=-6;z<=6;z++)if(Math.abs(x)==5||Math.abs(z)==6)b(x,y,z,(y%2==0?"dark_oak_log[axis=x]":"spruce_log[axis=z]"));
  for(int x:new int[]{-5,5})for(int z:new int[]{-6,6})for(int y=4;y<=8;y++)b(x,y,z,"stripped_dark_oak_log[axis=y]");
  for(int x:new int[]{-5,5})for(int z:new int[]{-3,3}){b(x,5,z,"glass_pane");b(x,6,z,"glass_pane");b(x,5,z-1,"spruce_trapdoor[facing=north,open=true]");b(x,5,z+1,"spruce_trapdoor[facing=south,open=true]");}
  for(int x=-6;x<=6;x++){int y=8+(6-Math.abs(x))/2;for(int z=-7;z<=7;z++){b(x,y,z,x<0?"dark_oak_stairs[facing=east]":"dark_oak_stairs[facing=west]");if(x==0)b(x,y+1,z,"dark_oak_slab");}}
  // Close triangular gables below the roof, without lowering interior headroom.
  for(int x=-5;x<=5;x++)for(int y=8;y<8+(6-Math.abs(x))/2;y++)for(int z:new int[]{-6,6})b(x,y,z,"dark_oak_planks");
  for(int y=8;y<=12;y++)b(3,y,3,"mossy_cobblestone_wall");b(3,13,3,"campfire[lit=true]");
  // Two bent legs with three front toes, one rear toe, and distinct dark claws.
  for(int x:new int[]{-3,3}){b(x,3,1,"stripped_acacia_log[axis=y]");b(x,2,1,"stripped_acacia_log[axis=y]");b(x,2,0,"acacia_stairs[facing=south]");b(x,1,0,"stripped_acacia_log[axis=y]");b(x,0,0,"stripped_acacia_log[axis=y]");for(int dx=-1;dx<=1;dx++){b(x+dx,0,-1,"acacia_slab");b(x+dx,0,-2,"acacia_slab");b(x+dx,0,-3,"polished_blackstone_slab");}b(x,0,1,"acacia_slab");b(x,0,2,"polished_blackstone_slab");}
  for(int x=-2;x<=2;x++)for(int z=-9;z<=-7;z++)b(x,3,z,"spruce_planks");
  for(int z=-7;z>=-9;z--)for(int x:new int[]{-2,2})b(x,4,z,"spruce_fence");
  for(int y=0;y<=2;y++)for(int x=-1;x<=1;x++)b(x,y,-12+y,"spruce_stairs[facing=south]");
  b(0,4,-6,"spruce_door[facing=north,half=lower]");b(0,5,-6,"spruce_door[facing=north,half=upper]");
  b(-2,4,3,"slavicmyths:yaga_cauldron");
  b(4,4,4,"campfire");b(4,4,5,"mossy_cobblestone");b(4,5,5,"mossy_cobblestone_wall");b(4,6,5,"mossy_cobblestone_wall");
  for(int x=2;x<=3;x++){b(x,4,-2,"spruce_fence");b(x,5,-2,"spruce_slab");}
  b(-4,4,4,"chest[facing=east]");b(-4,4,5,"barrel[facing=up]");
  for(int z=-3;z<=1;z++){b(-4,6,z,"spruce_slab[type=top]");b(-4,7,z,z%2==0?"slavicmyths:yaga_dried_herbs":"slavicmyths:yaga_bone_charm");}
  for(int x:new int[]{-3,3})b(x,7,-5,"slavicmyths:yaga_dried_herbs");b(0,6,-7,"slavicmyths:yaga_bone_charm");
  b(3,6,-2,"lantern");b(-4,5,0,"slavicmyths:ritual_candle");b(4,5,-4,"flower_pot");
  for(int z=-11;z<=9;z++)for(int x:new int[]{-9,9}){if(z%5!=0)b(x,0,z,"spruce_fence");}
  for(int x=-9;x<=9;x++)if(Math.abs(x)>2){b(x,0,-11,"spruce_fence");b(x,0,9,"spruce_fence");}
  for(int z=-16;z<=-13;z++)for(int x=-1;x<=1;x++)b(x,-1,z,"coarse_dirt");
  for(int x=-8;x<=-6;x++)for(int z=4;z<=6;z++)b(x,0,z,"spruce_log[axis=z]");
  b(7,0,5,"stripped_spruce_log[axis=y]");b(6,0,-4,"campfire[lit=false]");
  for(int x:new int[]{-7,7})for(int z:new int[]{-7,-3,1,7})b(x,0,z,z<0?"slavicmyths:wormwood":"brown_mushroom");
 }
 /** Candidate positions never load chunks. The seeded order is stable across restarts. */
 public static List<BlockPos> candidates(long seed,BlockPos spawn){Random random=new Random(seed^0x596167614875744cL);List<BlockPos> out=new ArrayList<>();for(int i=0;i<64;i++){double a=random.nextDouble()*Math.PI*2,r=820+random.nextDouble()*1650;out.add(new BlockPos(spawn.getX()+Math.round(Math.cos(a)*r),0,spawn.getZ()+Math.round(Math.sin(a)*r)));}return out;}
}
''')
write('yaga/YagaHut.java','''package org.slavicmyths.yaga;
import java.util.*;
import com.mojang.brigadier.StringReader;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.block.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
/** No forced chunks, no global world tick traversal, one durable world home. */
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class YagaHut {
 private static final YagaHutPlan PLAN=new YagaHutPlan();private static final Map<String,BlockState> STATES=new HashMap<>();
 public static void prepare(ServerWorld w){YagaData d=YagaData.get(w);if(d.placed||d.anchor!=null)return;for(BlockPos p:YagaHutPlan.candidates(w.getSeed(),w.getSharedSpawnPos())){if(d.examined.contains(p.asLong()))continue;Biome b=w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(p.getX()>>2,16,p.getZ()>>2);if(b.getBiomeCategory()==Biome.Category.FOREST){d.anchor=p;d.setDirty();return;}d.examined.add(p.asLong());}d.setDirty();}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayerEntity)||e.player.tickCount%80!=0||e.player.level.dimension()!=World.OVERWORLD)return;ServerWorld w=(ServerWorld)e.player.level;YagaData d=YagaData.get(w);if(d.placed)return;prepare(w);if(d.anchor==null||e.player.distanceToSqr(d.anchor.getX(),e.player.getY(),d.anchor.getZ())>128*128)return;BlockPos candidate=d.anchor;if(!loaded(w,candidate))return;if(!place(w,candidate)){d.examined.add(candidate.asLong());d.anchor=null;d.setDirty();prepare(w);}}
 private static boolean loaded(ServerWorld w,BlockPos p){return w.hasChunksAt(p.offset(-10,0,-17),p.offset(10,0,10));}
 private static BlockState state(String id){return STATES.computeIfAbsent(id,s->{try{return new BlockStateParser(new StringReader(s),false).parse(false).getState();}catch(com.mojang.brigadier.exceptions.CommandSyntaxException ex){throw new IllegalStateException(s,ex);}});}
 private static int ground(ServerWorld w,int x,int z){int y=w.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,x,z)-1;for(int i=0;i<48&&y>4;i++,y--){BlockState s=w.getBlockState(new BlockPos(x,y,z));if(s.getMaterial().isLiquid())return -1;if(s.isSolidRender(w,new BlockPos(x,y,z))&&!(s.getBlock() instanceof net.minecraft.block.LogBlock)&&!(s.getBlock() instanceof LeavesBlock))return y;}return -1;}
 public static boolean place(ServerWorld w,BlockPos candidate){YagaData d=YagaData.get(w);if(d.placed||!loaded(w,candidate))return false;int min=255,max=0;for(int x=-9;x<=9;x+=3)for(int z=-15;z<=9;z+=3){int y=ground(w,candidate.getX()+x,candidate.getZ()+z);if(y<0)return false;min=Math.min(min,y);max=Math.max(max,y);}if(max-min>3||max+15>=w.getMaxBuildHeight())return false;BlockPos anchor=new BlockPos(candidate.getX(),max+1,candidate.getZ());LinkedHashMap<BlockPos,BlockState> desired=new LinkedHashMap<>(),old=new LinkedHashMap<>();
  // Never erase inventory blocks, protected burial masonry or player construction.
  for(Map.Entry<BlockPos,String> e:PLAN.blocks.entrySet()){BlockPos p=anchor.offset(e.getKey());BlockState before=w.getBlockState(p);if(w.getBlockEntity(p)!=null||before.getMaterial().isLiquid())return false;net.minecraft.util.ResourceLocation id=before.getBlock().getRegistryName();if(id!=null&&id.getNamespace().equals("slavicmyths")&&!before.getMaterial().isReplaceable())return false;if(!before.isAir(w,p)&&!before.getMaterial().isReplaceable()&&!(before.getBlock() instanceof LeavesBlock)&&!(before.getBlock() instanceof LogBlock)&&!(before.getBlock() instanceof GrassBlock)&&!(before.getBlock() instanceof DirtBlock))return false;desired.put(p,state(e.getValue()));old.put(p,before);}
  BabaYaga npc=ModEntities.BABA_YAGA.get().create(w);if(npc==null)return false;npc.home=anchor.offset(YagaHutPlan.HOME);npc.moveTo(npc.home.getX()+.5,npc.home.getY(),npc.home.getZ()+.5,180,0);
  try{for(Map.Entry<BlockPos,BlockState> e:desired.entrySet())if(!w.setBlock(e.getKey(),e.getValue(),2)&&!w.getBlockState(e.getKey()).equals(e.getValue()))throw new IllegalStateException("Hut placement refused");if(!w.addFreshEntity(npc))throw new IllegalStateException("Yaga spawn refused");d.anchor=anchor;d.npc=npc.getUUID();d.placed=true;d.setDirty();return true;}catch(RuntimeException failure){npc.remove();for(Map.Entry<BlockPos,BlockState> e:old.entrySet())w.setBlock(e.getKey(),e.getValue(),2);org.slavicmyths.SlavicMyths.LOGGER.warn("Yaga hut rolled back",failure);return false;}
 }
 private YagaHut(){}
}
''')
write('yaga/YagaCommands.java','''package org.slavicmyths.yaga;
import net.minecraft.command.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class YagaCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("slavicmyths").requires(s->s.hasPermission(2)).then(Commands.literal("dev").then(Commands.literal("yaga")
  .then(Commands.literal("locate").executes(c->{ServerWorld w=c.getSource().getServer().getLevel(World.OVERWORLD);YagaHut.prepare(w);YagaData d=YagaData.get(w);if(d.anchor==null){c.getSource().sendFailure(new TranslationTextComponent("yaga.fail.home"));return 0;}c.getSource().sendSuccess(new TranslationTextComponent("yaga.located",d.anchor.toShortString(),d.placed),false);return 1;}))
  .then(Commands.literal("generate").executes(c->{ServerPlayerEntity p=c.getSource().getPlayerOrException();if(p.level.dimension()!=World.OVERWORLD)return 0;YagaData d=YagaData.get((ServerWorld)p.level);if(d.placed){c.getSource().sendFailure(new TranslationTextComponent("yaga.fail.exists"));return 0;}boolean ok=YagaHut.place((ServerWorld)p.level,p.blockPosition().offset(0,0,24));if(!ok)c.getSource().sendFailure(new TranslationTextComponent("yaga.fail.terrain"));else c.getSource().sendSuccess(new TranslationTextComponent("yaga.located",d.anchor.toShortString(),true),false);return ok?1:0;}))
  .then(Commands.literal("status").executes(c->{ServerPlayerEntity p=c.getSource().getPlayerOrException();YagaData d=YagaData.get(p.server.getLevel(World.OVERWORLD));YagaData.Progress v=d.progress(p.getUUID());c.getSource().sendSuccess(new TranslationTextComponent("yaga.status",new TranslationTextComponent("yaga.favor."+v.stage),v.active,v.blocked(p.level.getGameTime())),false);return 1;})))));}
}
''')
# Registration additions preserve all existing source.
p=r/'registry/ModEntities.java';s=p.read_text('utf-8-sig');s=s.replace('public final class ModEntities {','public final class ModEntities {');at=s.rfind('}');s=s[:at]+''' public static final RegistryObject<EntityType<org.slavicmyths.yaga.BabaYaga>> BABA_YAGA=ENTITIES.register("baba_yaga",()->EntityType.Builder.of(org.slavicmyths.yaga.BabaYaga::new,EntityClassification.CREATURE).sized(.6F,1.75F).clientTrackingRange(8).build("slavicmyths:baba_yaga"));
'''+s[at:];s=s.replace('event.put(DOMOVOY.get(),','event.put(BABA_YAGA.get(),org.slavicmyths.yaga.BabaYaga.attributes().build());\n        event.put(DOMOVOY.get(),');p.write_text(s,'utf-8')
p=r/'registry/ModBlocks.java';s=p.read_text('utf-8-sig');at=s.rfind('}');s=s[:at]+''' public static final RegistryObject<Block> YAGA_CAULDRON=BLOCKS.register("yaga_cauldron",org.slavicmyths.yaga.YagaCauldronBlock::new);
 public static final RegistryObject<Block> YAGA_DRIED_HERBS=BLOCKS.register("yaga_dried_herbs",()->new Block(AbstractBlock.Properties.of(Material.PLANT).strength(-1,3600000).noCollission().noOcclusion().noDrops()));
 public static final RegistryObject<Block> YAGA_BONE_CHARM=BLOCKS.register("yaga_bone_charm",()->new Block(AbstractBlock.Properties.of(Material.DECORATION).strength(-1,3600000).noCollission().noOcclusion().noDrops()));
'''+s[at:];p.write_text(s,'utf-8')
p=r/'registry/ModItems.java';s=p.read_text('utf-8-sig');at=s.rfind('}');extra=''
for id,kind in [('putevodny_klubok','THREAD'),('svyazka_trav_yagi','HERBS'),('otvar_ochishcheniya','CLEANSE'),('letuchaya_maz','SALVE'),('otvar_lesnoy_zorkosti','SIGHT'),('yagin_nastoy_stoykosti','RESIST'),('otvar_bodrosti','VIGOR'),('lesnoy_nastoy_vosstanovleniya','RESTORE')]:
 extra+=f' public static final RegistryObject<Item> {id.upper()}=ITEMS.register("{id}",()->new org.slavicmyths.yaga.YagaUtilityItem(properties().stacksTo({1 if kind=="THREAD" else 32 if kind=="HERBS" else 16})'+('.rarity(Rarity.RARE)' if kind=='THREAD' else '')+f',org.slavicmyths.yaga.YagaUtilityItem.Kind.{kind}));\n'
s=s[:at]+extra+s[at:];p.write_text(s,'utf-8')
p=r/'SlavicMyths.java';s=p.read_text('utf-8-sig').replace('RpgMenu.MENUS.register(bus);','org.slavicmyths.yaga.YagaMenu.register();\n        RpgMenu.MENUS.register(bus);');p.write_text(s,'utf-8')
p=r/'flight/FlightItem.java';s=p.read_text('utf-8-sig').replace('if(c.getPlayer()==null)return ActionResultType.PASS;','if(c.getPlayer()==null)return ActionResultType.PASS;\n  if(!org.slavicmyths.yaga.FlightRecovery.ready(c.getPlayer()))return ActionResultType.FAIL;');p.write_text(s,'utf-8')
p=r/'flight/FlyingVessel.java';s=p.read_text('utf-8-sig').replace('cargo.clearContent();remove();if(!p.inventory.add(result))','cargo.clearContent();org.slavicmyths.yaga.FlightRecovery.repack(p);remove();if(!p.inventory.add(result))');p.write_text(s,'utf-8')
for p in (r/'yaga').glob('*.java'):p.write_text(p.read_text('utf-8-sig'),'utf-8')
