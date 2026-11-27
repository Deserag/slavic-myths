package org.slavicmyths.savanna;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class DivCurse {
    private static final String MARKS="DivCurseMarks",OWNER="DivCurseOwner";
    private static final ResourceLocation SLOW=ResourceLocation.fromNamespaceAndPath("slavicmyths","div_curse_slow");
    private DivCurse(){}
    static long[] active(LivingEntity x){long now=x.level().getGameTime();return Arrays.stream(x.getPersistentData().getLongArray(MARKS)).filter(t->t>now).limit(4).toArray();}
    static boolean owned(LivingEntity x,UUID id){return x.getPersistentData().hasUUID(OWNER)&&id.equals(x.getPersistentData().getUUID(OWNER));}
    static int add(LivingEntity x,UUID owner){var n=x.getPersistentData();if(!owned(x,owner))clear(x);long[] old=active(x);if(old.length>=4)return 4;long[] next=Arrays.copyOf(old,old.length+1);next[old.length]=x.level().getGameTime()+240;n.putUUID(OWNER,owner);n.putLongArray(MARKS,next);applySlow(x,true);if(x instanceof ServerPlayer p)p.displayClientMessage(Component.translatableWithFallback("message.slavicmyths.div_curse","Проклятие Дива: %s/4",next.length),true);return next.length;}
    private static void applySlow(LivingEntity x,boolean active){AttributeInstance a=x.getAttribute(Attributes.MOVEMENT_SPEED);if(a==null)return;if(active&&!a.hasModifier(SLOW))a.addTransientModifier(new AttributeModifier(SLOW,-.1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));else if(!active)a.removeModifier(SLOW);}
    static void clear(LivingEntity x){x.getPersistentData().remove(MARKS);x.getPersistentData().remove(OWNER);applySlow(x,false);}
    @SubscribeEvent public static void tick(EntityTickEvent.Post event){if(!(event.getEntity() instanceof LivingEntity x)||x.level().isClientSide||!x.getPersistentData().contains(MARKS))return;long[] marks=active(x);if(marks.length==0){clear(x);return;}if(marks.length!=x.getPersistentData().getLongArray(MARKS).length){x.getPersistentData().putLongArray(MARKS,marks);if(x instanceof ServerPlayer p)p.displayClientMessage(Component.translatableWithFallback("message.slavicmyths.div_curse","Проклятие Дива: %s/4",marks.length),true);}applySlow(x,true);}
    @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){if(!e.getEntity().level().isClientSide&&e.getSource().getEntity() instanceof LivingEntity attacker&&attacker.getPersistentData().contains(MARKS)&&active(attacker).length>=2)e.setAmount(e.getAmount()*.85F);}
    @SubscribeEvent public static void heal(LivingHealEvent e){if(!e.getEntity().level().isClientSide&&e.getEntity().getPersistentData().contains(MARKS)&&active(e.getEntity()).length>=3)e.setAmount(e.getAmount()*.7F);}
}
