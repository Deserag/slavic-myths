package org.slavicmyths.finist;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
public final class FinistMarks {
 public static CompoundTag data(ServerPlayer p){var root=org.slavicmyths.rpg.PathData.data(p);if(!root.contains("FinistMark",10))root.put("FinistMark",new CompoundTag());return root.getCompound("FinistMark");}
 public static LivingEntity target(ServerPlayer p){var d=data(p);if(!d.hasUUID("Target")||d.getLong("Until")<=GreyPackRing.now(p)||!d.getString("Dimension").equals(p.level().dimension().location().toString())){d.remove("Target");return null;}var e=p.serverLevel().getEntity(d.getUUID("Target"));if(!(e instanceof LivingEntity living)||!GreyPackRing.enemy(p,living)||p.distanceToSqr(living)>80*80){d.remove("Target");return null;}return living;}
 public static void hit(ServerPlayer p,LivingEntity target,boolean manual){if(FinistQuiver.equipped(p).isEmpty()||!p.getMainHandItem().is(FinistItems.BOW.get())&&!p.getOffhandItem().is(FinistItems.BOW.get())||!GreyPackRing.enemy(p,target)||!p.hasLineOfSight(target))return;var old=target(p);if(old!=null&&old!=target&&!manual)return;var d=data(p);d.putUUID("Target",target.getUUID());d.putString("Dimension",p.level().dimension().location().toString());d.putLong("Until",GreyPackRing.now(p)+1200);d.putInt("Entity",target.getId());if(old!=target)p.playNotifySound(FinistSounds.get("finist_target_lock"),net.minecraft.sounds.SoundSource.PLAYERS,.35F,1);org.slavicmyths.rpg.RpgNetwork.sync(p);}
 private FinistMarks(){}
}
