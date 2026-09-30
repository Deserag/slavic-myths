package org.slavicmyths.water;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.progression.Knowledge;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class RusalkaSong {
 public static void expose(PlayerEntity p,RusalkaEntity r){CompoundNBT d=p.getPersistentData();long now=p.level.getGameTime();if(now-d.getLong("SlavicSongApplied")<19)return;d.putLong("SlavicSongApplied",now);float charm=d.getFloat("SlavicWaterCharm");if(!r.canSee(p)){d.putFloat("SlavicWaterCharm",Math.max(0,charm-3));return;}float amount=r.distanceToSqr(p)<100?5:2.5F;if(p.isUsingItem()&&p.getUseItem().getItem()==ModItems.GUSLI.get())amount*=.25F;if(org.slavicmyths.item.FolkEquipmentEffects.wears(p,ModItems.VELES_AMULET.get()))amount*=.5F;d.putFloat("SlavicWaterCharm",Math.min(100,charm+amount));d.putInt("SlavicSinger",r.getId());d.putLong("SlavicSongLast",now);if(charm>=50){d.putBoolean("SlavicStrongSong",true);Knowledge.award(p,"water_call");}}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){PlayerEntity p=e.player;if(e.phase!=TickEvent.Phase.END||p.level.isClientSide||p.tickCount%5!=0)return;CompoundNBT d=p.getPersistentData();float charm=d.getFloat("SlavicWaterCharm");if(charm<=0)return;net.minecraft.entity.Entity entity=p.level.getEntity(d.getInt("SlavicSinger"));boolean active=entity instanceof RusalkaEntity&&((RusalkaEntity)entity).singing()&&p.distanceToSqr(entity)<=400&&p.level.getGameTime()-d.getLong("SlavicSongLast")<25;
  if(!active||p.isCreative()||p.isSpectator()||!p.isAlive()){charm=Math.max(0,charm-5);d.putFloat("SlavicWaterCharm",charm);if(charm==0&&d.getBoolean("SlavicStrongSong")){Knowledge.award(p,"break_water_call");d.remove("SlavicStrongSong");}return;}
  double moved=Math.pow(p.getX()-d.getDouble("SlavicSongX"),2)+Math.pow(p.getZ()-d.getDouble("SlavicSongZ"),2);d.putDouble("SlavicSongX",p.getX());d.putDouble("SlavicSongZ",p.getZ());
  if(charm>=25&&!p.isPassenger()&&(charm>=50||moved<.015)){double strength=charm>=90?.11:charm>=75?.065:charm>=50?.035:.012;if(org.slavicmyths.item.FolkEquipmentEffects.wears(p,ModItems.DEPTH_AMULET.get()))strength*=.7;Vector3d toward=entity.position().subtract(p.position()).multiply(1,0,1).normalize().scale(strength);p.setDeltaMovement(p.getDeltaMovement().add(toward));p.hurtMarked=true;}
  if(p.tickCount%40==0)p.displayClientMessage(new net.minecraft.util.text.TranslationTextComponent("water.slavicmyths.charm",(int)charm),true);
 }
}
