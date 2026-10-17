package org.slavicmyths.bogatyr;

import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.*;
import net.minecraft.core.BlockPos;

/** Canonical UUID ownership and victory history. Never resolves by loading a chunk. */
public final class BogatyrLedger extends SavedData {
    public final Map<UUID,UUID> heroes=new HashMap<>();
    public final Map<UUID,CompoundTag> victories=new HashMap<>();
    public static BogatyrLedger get(ServerLevel w){return w.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(BogatyrLedger::new,BogatyrLedger::load),"slavicmyths_bogatyr_171");}
    public static BogatyrLedger load(CompoundTag n,HolderLookup.Provider p){var d=new BogatyrLedger();for(var t:n.getList("Heroes",Tag.TAG_COMPOUND)){var a=(CompoundTag)t;if(a.hasUUID("Owner")&&a.hasUUID("Entity"))d.heroes.put(a.getUUID("Owner"),a.getUUID("Entity"));}for(var t:n.getList("Victories",Tag.TAG_COMPOUND)){var a=(CompoundTag)t;if(a.hasUUID("playerId"))d.victories.put(a.getUUID("playerId"),a.copy());}return d;}
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider p){n.putInt("schemaVersion",1);var h=new ListTag();heroes.forEach((owner,entity)->{var a=new CompoundTag();a.putUUID("Owner",owner);a.putUUID("Entity",entity);h.add(a);});n.put("Heroes",h);var v=new ListTag();victories.values().forEach(a->v.add(a.copy()));n.put("Victories",v);return n;}
    public void victory(ServerPlayer p,IdolishcheEntity boss,boolean hero){var a=victories.computeIfAbsent(p.getUUID(),k->new CompoundTag());long now=boss.level().getGameTime();if(a.getString("lastEncounter").equals(boss.encounter.toString()))return;a.putUUID("playerId",p.getUUID());a.putString("bossId","slavicmyths:idolishche_poganoe");if(!a.contains("firstDefeatedWorldTick"))a.putLong("firstDefeatedWorldTick",now);a.putLong("lastDefeatedWorldTick",now);a.putLong("defeatedAtWorldTime",now);a.putInt("defeatCount",a.getInt("defeatCount")+1);a.putString("lastEncounter",boss.encounter.toString());a.putBoolean("participationFlag",true);a.putBoolean("eligibleForFutureRewards",true);a.putBoolean("allyWasAliveAtStart",hero);var ids=a.getList("usedHeroIds",Tag.TAG_STRING);if(hero&&!ids.contains(StringTag.valueOf("ilya_muromets")))ids.add(StringTag.valueOf("ilya_muromets"));a.put("usedHeroIds",ids);setDirty();org.slavicmyths.progression.AdvancementMilestones.nativeVictory(p,boss,"defeat_idolishche");org.slavicmyths.rewards.RewardLedger.victory(p,0);p.sendSystemMessage(Component.translatable("gui.slavicmyths.171.boss_defeated"));}
    public static IlyaEntity hero(ServerPlayer p){var id=get(p.serverLevel()).heroes.get(p.getUUID());return id!=null&&p.serverLevel().getEntity(id) instanceof IlyaEntity h?h:null;}
    public static boolean active(ServerPlayer p){return !p.serverLevel().getEntitiesOfClass(IdolishcheEntity.class,p.getBoundingBox().inflate(80),b->b.active&&b.isAlive()&&(b.origin.multiply(1,0,1).distanceToSqr(p.position().multiply(1,0,1))<=26*26||p.getUUID().equals(b.initiator))).isEmpty();}
    public static boolean call(ServerPlayer p){var w=p.serverLevel();var d=get(w);if(!p.isAlive()||p.isSpectator())return false;if(!org.slavicmyths.marinka.StoryPolicy.canSummon(p,"ilya_muromets"))return false;if(active(p)){p.sendSystemMessage(Component.translatable("gui.slavicmyths.171.no_midbattle_summon"));return false;}if(d.heroes.containsKey(p.getUUID())){p.sendSystemMessage(Component.translatable("gui.slavicmyths.171.already_called"));return false;}var h=BogatyrContent.ILYA.get().create(w);if(h==null)return false;var at=safe(w,p.position(),.93,2.45,6);if(at==null){p.sendSystemMessage(Component.translatable("gui.slavicmyths.171.no_space"));return false;}h.moveTo(at.x,at.y,at.z,p.getYRot(),0);h.owner=p.getUUID();if(!w.addFreshEntity(h))return false;d.heroes.put(p.getUUID(),h.getUUID());d.setDirty();return true;}
    public static Vec3 safe(ServerLevel w,Vec3 center,double width,double height,int radius){for(int r=2;r<=radius;r++)for(int i=0;i<12;i++){double a=i*Math.PI/6;for(int dy:new int[]{0,1,-1,2,-2}){var p=BlockPos.containing(center.x+Math.cos(a)*r,center.y+dy,center.z+Math.sin(a)*r);if(!w.hasChunkAt(p)||!w.hasChunkAt(p.offset(0,(int)Math.ceil(height),0))||!w.getWorldBorder().isWithinBounds(p)||!w.getBlockState(p.below()).isCollisionShapeFullBlock(w,p.below())||!w.getFluidState(p).isEmpty())continue;var box=new AABB(p.getX()+.5-width/2,p.getY(),p.getZ()+.5-width/2,p.getX()+.5+width/2,p.getY()+height,p.getZ()+.5+width/2);if(w.noCollision(box)&&w.getEntities(null,box).isEmpty())return new Vec3(p.getX()+.5,p.getY(),p.getZ()+.5);}}return null;}
}
