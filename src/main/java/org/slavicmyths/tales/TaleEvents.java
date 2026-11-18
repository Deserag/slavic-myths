package org.slavicmyths.tales;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.util.AbortableIterationConsumer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class TaleEvents {
    @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent e){TaleService.invalidateRecipes();}
    @SubscribeEvent public static void reload(AddReloadListenerEvent e){e.addListener(new TaleCatalog());}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){TaleService.deliver(p);TaleService.reconcile(p);org.slavicmyths.progression.AdvancementJourney.deliverBeer(p);}}
    @SubscribeEvent public static void pickup(ItemEntityPickupEvent.Post e){if(e.getPlayer() instanceof ServerPlayer p){TaleService.subject(p,"item/"+BuiltInRegistries.ITEM.getKey(e.getOriginalStack().getItem()),2,false);TaleService.deliver(p);}}
    @SubscribeEvent public static void crafted(PlayerEvent.ItemCraftedEvent e){if(e.getEntity() instanceof ServerPlayer p){TaleService.subject(p,"item/"+BuiltInRegistries.ITEM.getKey(e.getCrafting().getItem()),4,false);if(BuiltInRegistries.ITEM.getKey(e.getCrafting().getItem()).toString().equals("slavicmyths:linen_cloth"))org.slavicmyths.progression.AdvancementJourney.award(p,"linen_woven");TaleService.deliver(p);}}
    @SubscribeEvent public static void smelted(PlayerEvent.ItemSmeltedEvent e){if(e.getEntity() instanceof ServerPlayer p)TaleService.subject(p,"item/"+BuiltInRegistries.ITEM.getKey(e.getSmelting().getItem()),4,false);}
    @SubscribeEvent public static void container(PlayerContainerEvent.Close e){if(e.getEntity() instanceof ServerPlayer p){for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);if(!stack.isEmpty())TaleService.subject(p,"item/"+BuiltInRegistries.ITEM.getKey(stack.getItem()),2,false);}TaleService.deliver(p);}}
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract e){if(e.getTarget() instanceof org.slavicmyths.entity.LandSpiritEntity m&&m.morClone())return;if(e.getEntity() instanceof ServerPlayer p&&p.distanceToSqr(e.getTarget())<=9&&p.hasLineOfSight(e.getTarget()))TaleService.subject(p,"entity/"+BuiltInRegistries.ENTITY_TYPE.getKey(e.getTarget().getType()),3,false);}
    @SubscribeEvent public static void block(PlayerInteractEvent.RightClickBlock e){if(e.getEntity() instanceof ServerPlayer p)TaleService.subject(p,"block/"+BuiltInRegistries.BLOCK.getKey(p.level().getBlockState(e.getPos()).getBlock()),2,false);}
    @SubscribeEvent public static void earned(AdvancementEvent.AdvancementEarnEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getAdvancement().id().getNamespace().equals("slavicmyths")){String id=e.getAdvancement().id().toString();if(org.slavicmyths.yaga.YagaAccessRules.victory(e.getAdvancement().id().getPath()))org.slavicmyths.yaga.YagaDiscovery.refresh(p);org.slavicmyths.guides.GuideQuests.reconcile(p);org.slavicmyths.guides.GuideDiscovery.reconcile(p);for(var entry:TaleCatalog.advancement(id))TaleService.promote(p,entry.id(),TaleService.advancementStage(id),false);var batch=new net.minecraft.nbt.ListTag();for(var raw:TaleService.advancements(p)){var n=(net.minecraft.nbt.CompoundTag)raw;if(n.getString("Id").equals(id)||n.getString("Parent").equals(id)){batch.add(n);if(batch.size()==4){var delta=new net.minecraft.nbt.CompoundTag();delta.put("Advancements",batch);TaleNetwork.send(p,delta,false,false);batch=new net.minecraft.nbt.ListTag();}}}if(!batch.isEmpty()){var delta=new net.minecraft.nbt.CompoundTag();delta.put("Advancements",batch);TaleNetwork.send(p,delta,false,false);}}}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void killed(LivingDeathEvent e){if(e.getEntity() instanceof org.slavicmyths.entity.LandSpiritEntity m&&m.morClone())return;if(e.getSource().getEntity() instanceof ServerPlayer p&&!p.isCreative()&&!p.isSpectator()&&!e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))TaleService.subject(p,"entity/"+BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()),4,true);}
    @SubscribeEvent public static void revoked(AdvancementEvent.AdvancementProgressEvent e){if(e.getProgressType()==AdvancementEvent.AdvancementProgressEvent.ProgressType.REVOKE&&e.getEntity() instanceof ServerPlayer p&&e.getAdvancement().id().getNamespace().equals("slavicmyths")){var delta=new net.minecraft.nbt.CompoundTag();delta.putBoolean("AdvancementReset",true);delta.put("Advancements",TaleService.advancements(p));TaleNetwork.send(p,delta,false,false);}}
    @SubscribeEvent public static void seen(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.isSpectator()||!p.isAlive()||Math.floorMod(p.tickCount+p.getId(),20)!=0)return;
        org.slavicmyths.guides.GuideQuests.reconcile(p);
        loadedStructure(p);
        int[] count={0};p.serverLevel().getEntities().get(EntityTypeTest.forClass(LivingEntity.class),p.getBoundingBox().inflate(12),entity->{
            if(entity.isAlive()&&!(entity instanceof org.slavicmyths.entity.LandSpiritEntity m&&m.morClone())){var id=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());if(entity.getType().is(org.slavicmyths.military.Military.BANDITS)&&p.hasLineOfSight(entity))org.slavicmyths.progression.AdvancementJourney.award(p,"meet_bandits");if(id.getNamespace().equals("slavicmyths")&&TaleCatalog.subject("entity/"+id).stream().anyMatch(entry->TaleData.get(p.serverLevel()).stage(p.getUUID(),entry.id())<2)&&p.hasLineOfSight(entity))TaleService.subject(p,"entity/"+id,2,false);}
            return ++count[0]>=64?AbortableIterationConsumer.Continuation.ABORT:AbortableIterationConsumer.Continuation.CONTINUE;
        });
    }
    /** One moved-position sample per second, at most 32 already loaded starts.
     * Never invokes getStructureWithPieceAt/locate or loads a referenced chunk. */
    private static void loadedStructure(ServerPlayer p){var pos=p.blockPosition();var memory=p.getPersistentData();String dimension=p.level().dimension().location().toString();if(memory.contains("TalePosition")&&memory.getLong("TalePosition")==pos.asLong()&&memory.getString("TaleDimension").equals(dimension)&&!memory.getBoolean("TaleChunkPending"))return;memory.putLong("TalePosition",pos.asLong());memory.putString("TaleDimension",dimension);var source=p.serverLevel().getChunkSource();var chunk=source.getChunkNow(pos.getX()>>4,pos.getZ()>>4);if(chunk==null){memory.putBoolean("TaleChunkPending",true);return;}memory.putBoolean("TaleChunkPending",false);int checked=0;var registry=p.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);for(var reference:chunk.getAllReferences().entrySet()){var id=registry.getKey(reference.getKey());if(id==null||!id.getNamespace().equals("slavicmyths"))continue;for(long at:reference.getValue()){if(++checked>32)return;org.slavicmyths.chud.ChudResearch.visitedReference(p,reference.getKey(),at);var startChunk=source.getChunkNow(net.minecraft.world.level.ChunkPos.getX(at),net.minecraft.world.level.ChunkPos.getZ(at));if(startChunk==null)continue;var start=startChunk.getStartForStructure(reference.getKey());if(start!=null&&start.isValid()&&start.getBoundingBox().isInside(pos)){org.slavicmyths.chud.ChudResearch.visited(p,start);org.slavicmyths.progression.AdvancementJourney.structure(p,start);TaleService.subject(p,"structure/"+id,2,false);org.slavicmyths.rpg.classes.ClassXpService.once(p,org.slavicmyths.rpg.classes.ClassXpService.AwardSource.EXPLORATION,"structure/"+id+"/"+dimension+"/"+at,180);}}}}
    private TaleEvents(){}
}
