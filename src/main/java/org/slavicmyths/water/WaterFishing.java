package org.slavicmyths.water;
import java.util.Random;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModItems;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class WaterFishing {
 public static Item fish(World w,BlockPos p,Random r){Biome.Category c=w.getBiome(p).getBiomeCategory();if(c==Biome.Category.OCEAN)return r.nextBoolean()?Items.COD:Items.SALMON;int roll=r.nextInt(100);if(roll<(c==Biome.Category.RIVER?30:12))return ModItems.RAW_PIKE.get();if(roll<65)return ModItems.RAW_CARP.get();if(roll<75)return ModItems.RAW_CRAYFISH.get();return Items.COD;}
 public static boolean pressure(World w,BlockPos pos,net.minecraft.entity.player.PlayerEntity p,int amount){if(p==null)return false;boolean angry=false;for(VodyanoyEntity spirit:w.getEntitiesOfClass(VodyanoyEntity.class,new net.minecraft.util.math.AxisAlignedBB(pos).inflate(24))){spirit.pressure(p,amount);angry|=spirit.reputation(p)<=-20;}return angry;}
 @SubscribeEvent public static void fishKilled(net.minecraftforge.event.entity.living.LivingDeathEvent e){if(!e.getEntityLiving().level.isClientSide&&e.getEntityLiving() instanceof RiverFish&&e.getSource().getEntity() instanceof net.minecraft.entity.player.PlayerEntity)pressure(e.getEntityLiving().level,e.getEntityLiving().blockPosition(),(net.minecraft.entity.player.PlayerEntity)e.getSource().getEntity(),3);}
 @SubscribeEvent public static void catchFish(ItemFishedEvent e){if(e.getPlayer().level.isClientSide)return;World w=e.getPlayer().level;BlockPos pos=e.getHookEntity().blockPosition();Random r=w.random;
  for(int i=0;i<e.getDrops().size();i++){ItemStack old=e.getDrops().get(i);if((old.getItem()==Items.COD||old.getItem()==Items.SALMON)&&r.nextFloat()<.4F)e.getDrops().set(i,new ItemStack(fish(w,pos,r),old.getCount()));}
  pressure(w,pos,e.getPlayer(),2);
  if((w.getBiome(pos).getBiomeCategory()==Biome.Category.RIVER||w.getBiome(pos).getBiomeCategory()==Biome.Category.SWAMP)&&r.nextInt(200)==0){e.getDrops().add(new ItemStack(ModItems.ANCIENT_WATER_SIGN.get()));org.slavicmyths.progression.Knowledge.award(e.getPlayer(),"depth_clue");}
  // Additions never replace vanilla treasure or junk rolls.
  if(w.getBiome(pos).getBiomeCategory()!=Biome.Category.OCEAN&&r.nextInt(80)==0)e.getDrops().add(new ItemStack(r.nextBoolean()?ModItems.PEARL_FRAGMENT.get():ModItems.OLD_HOOK.get()));
  if(e.getDrops().stream().anyMatch(s->s.getItem()==ModItems.RAW_PIKE.get()||s.getItem()==ModItems.RAW_CARP.get()||s.getItem()==ModItems.RAW_CRAYFISH.get()))org.slavicmyths.progression.Knowledge.award(e.getPlayer(),"new_catch");
 }
}
