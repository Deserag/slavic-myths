package org.slavicmyths.water;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.world.item.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.ModItems;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class WaterFishing {
 public static Item fish(Level w,BlockPos p,RandomSource r){var biome=w.getBiome(p);if(biome.is(Tags.Biomes.IS_OCEAN))return r.nextBoolean()?Items.COD:Items.SALMON;int roll=r.nextInt(100);if(roll<(biome.is(Tags.Biomes.IS_RIVER)?30:12))return ModItems.RAW_PIKE.get();if(roll<65)return ModItems.RAW_CARP.get();if(roll<75)return ModItems.RAW_CRAYFISH.get();return Items.COD;}
 public static boolean pressure(Level w,BlockPos pos,net.minecraft.world.entity.player.Player p,int amount){if(p==null)return false;boolean angry=false;for(VodyanoyEntity spirit:w.getEntitiesOfClass(VodyanoyEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(24))){spirit.pressure(p,amount);angry|=spirit.reputation(p)<=-20;}return angry;}
 @SubscribeEvent public static void fishKilled(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){if(!e.getEntity().level().isClientSide&&e.getEntity() instanceof RiverFish&&e.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player)pressure(e.getEntity().level(),e.getEntity().blockPosition(),(net.minecraft.world.entity.player.Player)e.getSource().getEntity(),3);}
 @SubscribeEvent public static void catchFish(ItemFishedEvent e){if(e.getEntity().level().isClientSide)return;Level w=e.getEntity().level();BlockPos pos=e.getHookEntity().blockPosition();RandomSource r=w.random;
  for(int i=0;i<e.getDrops().size();i++){ItemStack old=e.getDrops().get(i);if((old.getItem()==Items.COD||old.getItem()==Items.SALMON)&&r.nextFloat()<.4F)e.getDrops().set(i,new ItemStack(fish(w,pos,r),old.getCount()));}
  pressure(w,pos,e.getEntity(),2);
  if((w.getBiome(pos).is(Tags.Biomes.IS_RIVER)||w.getBiome(pos).is(Tags.Biomes.IS_SWAMP))&&r.nextInt(200)==0){e.getDrops().add(new ItemStack(ModItems.ANCIENT_WATER_SIGN.get()));org.slavicmyths.progression.Knowledge.award(e.getEntity(),"depth_clue");}
  // Additions never replace vanilla treasure or junk rolls.
  if(!w.getBiome(pos).is(Tags.Biomes.IS_OCEAN)&&r.nextInt(80)==0)e.getDrops().add(new ItemStack(r.nextBoolean()?ModItems.PEARL_FRAGMENT.get():ModItems.OLD_HOOK.get()));
  if(e.getDrops().stream().anyMatch(s->s.getItem()==ModItems.RAW_PIKE.get()||s.getItem()==ModItems.RAW_CARP.get()||s.getItem()==ModItems.RAW_CRAYFISH.get()))org.slavicmyths.progression.Knowledge.award(e.getEntity(),"new_catch");
 }
}
