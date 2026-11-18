package org.slavicmyths.sisters;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;

/** Installed tier-III rune: every legal weapon impact creates a visible tremor and slow. */
@EventBusSubscriber(modid="slavicmyths")
public final class TremorRune {
    public static final DeferredHolder<Item,Item> ITEM=ModItems.ITEMS.register("rune_tremor_iii", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)) {
        @Override public Component getName(ItemStack stack){return Component.literal("Руна дрожи III");}
        @Override public void appendHoverText(ItemStack stack,TooltipContext ctx,List<Component> lines,TooltipFlag flag){
            super.appendHoverText(stack,ctx,lines,flag);
            lines.add(Component.literal("Удары оружием: Замедление II (4,5 сек.)").withStyle(ChatFormatting.LIGHT_PURPLE));
            lines.add(Component.literal("Видимая волна дрожи при попадании").withStyle(ChatFormatting.GRAY));
        }
    });
    public static void init(){}

    @SubscribeEvent public static void hit(LivingDamageEvent.Post event) {
        if(event.getNewDamage()<=0||!(event.getSource().getEntity() instanceof ServerPlayer player))return;
        LivingEntity victim=event.getEntity();
        boolean melee=event.getSource().getDirectEntity()==player;
        boolean arrow=event.getSource().getDirectEntity() instanceof AbstractArrow;
        if(!melee&&!arrow||!org.slavicmyths.rpg.Abilities.canHit(player,victim)
                ||!org.slavicmyths.rpg.Runes.has(player.getMainHandItem(),"tremor_iii"))return;
        long now=player.serverLevel().getGameTime();
        if(victim.getPersistentData().getLong("SistersTremorRuneUntil")>now)return;
        victim.getPersistentData().putLong("SistersTremorRuneUntil",now+60);
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,90,1),player);
        player.serverLevel().sendParticles(ParticleTypes.PORTAL,victim.getX(),victim.getY()+.65,victim.getZ(),20,.6,.3,.6,.25);
        player.serverLevel().sendParticles(ParticleTypes.CRIT,victim.getX(),victim.getY()+.55,victim.getZ(),8,.5,.2,.5,.04);
    }
    private TremorRune(){}
}
