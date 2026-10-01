package org.slavicmyths.combat;
import net.minecraft.entity.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class CombatHudEvents {
    @SubscribeEvent public static void damage(LivingDamageEvent event){
        if(event.getEntityLiving().level.isClientSide||event.getAmount()<=0)return;
        Entity attacker=event.getSource().getEntity();LivingEntity victim=event.getEntityLiving();
        if(attacker instanceof ServerPlayerEntity)org.slavicmyths.network.LoreNetwork.combat((ServerPlayerEntity)attacker,victim.getId());
        if(victim instanceof ServerPlayerEntity&&attacker instanceof LivingEntity)org.slavicmyths.network.LoreNetwork.combat((ServerPlayerEntity)victim,attacker.getId());
        if(attacker instanceof MobEntity&&((MobEntity)attacker).getMainHandItem().getItem() instanceof MaceItem&&!event.getSource().isProjectile()) {
            MaceItem item=(MaceItem)((MobEntity)attacker).getMainHandItem().getItem();victim.knockback(item.push,attacker.getX()-victim.getX(),attacker.getZ()-victim.getZ());
        }
    }
}
