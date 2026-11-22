package org.slavicmyths.combat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class RangeEnchantment {
    public static final ResourceKey<Enchantment> KEY=ResourceKey.create(Registries.ENCHANTMENT,ResourceLocation.fromNamespaceAndPath("slavicmyths","range"));
    public static double multiplier(Level level,ItemStack stack){
        var holder=level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(KEY);
        int rank=holder.map(h->EnchantmentHelper.getItemEnchantmentLevel(h,stack)).orElse(0);
        return 1+.25*Math.clamp(rank,0,3);
    }
    @SubscribeEvent public static void thrownTrident(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide||event.loadedFromDisk())return;
        if(event.getEntity() instanceof net.minecraft.world.entity.projectile.ThrownTrident shot){
            shot.setDeltaMovement(shot.getDeltaMovement().scale(Math.sqrt(multiplier(event.getLevel(),shot.getWeaponItem()))));
        }
    }
    private RangeEnchantment(){}
}