package org.slavicmyths.mor;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
public final class MorBlockChecks {
 public static void main(String[] args) throws Exception {
  net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.Map.of());SharedConstants.tryDetectVersion();Bootstrap.bootStrap();int checks=0;
  var registry=(net.minecraft.core.MappedRegistry<Block>)net.minecraft.core.registries.BuiltInRegistries.BLOCK;registry.unfreeze();var holders=net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");holders.setAccessible(true);holders.set(registry,new java.util.IdentityHashMap<>());
  for(int i=0;i<MorBlocks.IDS.length;i++) {
   Block base=MorBlocks.BASE[i],mor=MorBlocks.create(MorBlocks.IDS[i],base);
   for(var state:base.getStateDefinition().getPossibleStates()) {
    var converted=MorBlocks.copy(state,mor.defaultBlockState());
    var restored=MorBlocks.copy(converted,base.defaultBlockState());
    if(!restored.equals(state))throw new AssertionError("state roundtrip "+state);
    if(converted.getDestroySpeed(null,null)!=state.getDestroySpeed(null,null))throw new AssertionError("hardness "+state);
    checks+=2;
   }
   if(mor.getExplosionResistance()!=base.getExplosionResistance())throw new AssertionError("resistance "+i);
   if(mor.defaultBlockState().getLightEmission()!=0)throw new AssertionError("light "+i);
   if(i==4||i==5)if(!(mor instanceof FallingBlock))throw new AssertionError("gravity");
   if(i==7&&!(mor instanceof LeavesBlock))throw new AssertionError("decay");checks+=2;
  }
  System.out.println("PASS "+checks+" native block state/physics assertions. No game launches.");
 }
}
