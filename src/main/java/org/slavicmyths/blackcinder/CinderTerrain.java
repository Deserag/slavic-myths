package org.slavicmyths.blackcinder;
import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slavicmyths.navya.NavyaProfile;
/** Bounded new-chunk geology before ores; decoration after ores, no retro-generation. */
public final class CinderTerrain extends Feature<NoneFeatureConfiguration> {
 private final boolean nature;
 public CinderTerrain(boolean nature){super(NoneFeatureConfiguration.CODEC);this.nature=nature;}
 public static int height(long seed,int x,int z){return (int)Math.round(65+8*NavyaProfile.noise(seed+203,x,z,96)+3*NavyaProfile.noise(seed+301,x,z,40));}
 public static boolean path(int x,int z){double wx=x+12*Math.sin(z/53.0),wz=z+13*Math.sin(x/61.0);return Math.abs(Math.IEEEremainder(wx,64))<2.5||Math.abs(Math.IEEEremainder(wz,64))<2.5;}
 @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){
  var w=c.level();int bx=c.origin().getX()&~15,bz=c.origin().getZ()&~15;var anchor=new BlockPos(bx,80,bz);
  for(var ref:w.getChunk(anchor).getAllReferences().entrySet())if(!ref.getValue().isEmpty()){
   var key=w.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getKey(ref.getKey());
   if(!CinderWorldgen.id("zharenitsa_arena").equals(key))return false;
  }
  boolean changed=false;
  for(int x=bx;x<bx+16;x++)for(int z=bz;z<bz+16;z++){
   if(!w.getBiome(new BlockPos(x,80,z)).is(CinderWorldgen.BIOME)||!w.getFluidState(new BlockPos(x,31,z)).isEmpty())continue;
   int h=height(w.getSeed(),x,z);boolean trail=path(x,z);
   boolean arenaFloor=false;
   for(int y=h-8;y<=h+8;y++){var state=w.getBlockState(new BlockPos(x,y,z));if(state.is(CinderBlocks.get("ash_crust"))||state.is(CinderBlocks.get("charred_stone"))||state.is(CinderBlocks.get("black_cinder_soil"))){arenaFloor=true;break;}}
   if(arenaFloor)continue;
   if(!nature){for(int y=33;y<=174;y++){var p=new BlockPos(x,y,z);var old=w.getBlockState(p);if(w.getBlockEntity(p)!=null||!w.ensureCanWrite(p))continue;if(y<=h&&(old.isAir()||old.is(Blocks.NETHERRACK)))w.setBlock(p,Blocks.NETHERRACK.defaultBlockState(),2);else if(y>h&&old.is(Blocks.NETHERRACK))w.setBlock(p,Blocks.CAVE_AIR.defaultBlockState(),2);}changed=true;continue;}
   var p=new BlockPos(x,h,z);if(!w.getBlockState(p).is(Blocks.NETHERRACK)||!w.isEmptyBlock(p.above()))continue;
   boolean nearTrail=false;for(int delta=-10;delta<=10;delta++)if(path(x+delta,z)||path(x,z+delta)){nearTrail=true;break;}
   double pool=nearTrail?0:NavyaProfile.noise(w.getSeed()+851,x,z,11);
   String material=trail?"ash_crust":pool<-.66?null:pool<-.53?"charred_stone":c.random().nextInt(38)==0?"smoking_cinder":"black_cinder_soil";
   w.setBlock(p,material==null?Blocks.LAVA.defaultBlockState():CinderBlocks.get(material).defaultBlockState(),2);
   if(!trail&&pool>=-.66&&pool<-.59)w.setBlock(p,Blocks.MAGMA_BLOCK.defaultBlockState(),2);
   if(!trail&&pool>.65)w.setBlock(p,CinderBlocks.get(c.random().nextBoolean()?"charred_stone":"cracked_charred_stone").defaultBlockState(),2);
   changed=true;
  }
  if(nature&&c.random().nextInt(7)==0){int x=bx+8,z=bz+8,h=height(w.getSeed(),x,z);var base=new BlockPos(x,h+1,z);if(w.getBiome(base).is(CinderWorldgen.BIOME)&&!path(x,z)&&w.getBlockState(base.below()).is(CinderBlocks.get("black_cinder_soil"))){
   var plan=new CinderTreePlan(c.random().nextLong());boolean clear=true;
   var cells=new java.util.HashSet<>(plan.logs.keySet());cells.addAll(plan.leaves);
   for(var cell:cells){var q=base.offset(cell.x(),cell.y(),cell.z());if(!w.ensureCanWrite(q)||!w.isEmptyBlock(q)){clear=false;break;}}
   if(clear){for(var entry:plan.logs.entrySet()){var cell=entry.getKey();w.setBlock(base.offset(cell.x(),cell.y(),cell.z()),CinderBlocks.get(cell.y()<2?"charred_blackwood_log":"blackwood_log").defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.values()[entry.getValue()]),2);}
    for(var cell:plan.leaves)w.setBlock(base.offset(cell.x(),cell.y(),cell.z()),CinderBlocks.get("charred_blackwood_leaves").defaultBlockState().setValue(LeavesBlock.DISTANCE,1).setValue(LeavesBlock.PERSISTENT,true),2);
   }
  }}
  return changed;
 }
}
