package org.slavicmyths.sisters;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class BossRewardWeapons {
    private static final String AOE="SlavicLomeyaAreaStrike",DUAL="SlavicLomeyaDualStrike",READY="SlavicLomeyaDualReady",READY_TICK="SlavicLomeyaDualReadyTick",MARK="SlavicNeveyaMarkedUntil";

    public static final class Weapon extends SwordItem {
        private final boolean scythe;
        public Weapon(boolean isScythe){
            super(Tiers.NETHERITE,new Item.Properties().stacksTo(1).durability(isScythe?1900:1650).rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE,isScythe?17:16,isScythe?-2.8F:-2.6F)));
            scythe=isScythe;
        }
        private static boolean valid(Player owner,LivingEntity target){
            if(target==owner||!target.isAlive()||owner.isAlliedTo(target)||target.isAlliedTo(owner)
                ||target.getPersistentData().getBoolean("SlavicViySummoned"))return false;
            return !(target instanceof Player p&&(p.isCreative()||p.isSpectator()||!owner.canHarmPlayer(p)));
        }
        private void debuff(LivingEntity target){
            if(scythe){
                target.addEffect(new MobEffectInstance(MobEffects.WITHER,120,1));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,120,0));
            }else{
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,90,1));
                if(target instanceof Player p)FractureHooks.apply(p);
            }
        }
        @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){
            if(!attacker.level().isClientSide&&!(attacker instanceof Player p&&!valid(p,target))){
                debuff(target);
                if(scythe&&attacker instanceof ServerPlayer player){
                    long now=player.serverLevel().getGameTime();
                    if(target.getPersistentData().getLong(MARK)>now){
                        target.hurt(player.damageSources().magic(),9F);
                        player.serverLevel().sendParticles(ParticleTypes.SOUL_FIRE_FLAME,target.getX(),target.getY()+1,target.getZ(),9,.35,.5,.35,.02);
                    }
                    target.getPersistentData().putLong(MARK,now+100);
                }
            }
            return super.hurtEnemy(stack,target,attacker);
        }
        @Override public int getUseDuration(ItemStack stack,LivingEntity user){return scythe?72000:0;}
        @Override public UseAnim getUseAnimation(ItemStack stack){return scythe?UseAnim.SPEAR:UseAnim.NONE;}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player user,InteractionHand hand){
            ItemStack stack=user.getItemInHand(hand);
            if(hand!=InteractionHand.MAIN_HAND||user.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(stack);
            if(scythe){
                user.startUsingItem(hand);
                return InteractionResultHolder.consume(stack);
            }
            if(level instanceof ServerLevel world)cast(world,user,stack,user.isShiftKeyDown());
            return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
        }
        @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity user,int remaining){
            if(!scythe||!(user instanceof Player player)||!(level instanceof ServerLevel world))return;
            int charged=getUseDuration(stack,user)-remaining;
            if(charged<5||player.getCooldowns().isOnCooldown(this))return;
            cast(world,player,stack,player.isShiftKeyDown());
        }
        private void cast(ServerLevel world,Player user,ItemStack stack,boolean alternate){
            boolean dual=!scythe&&user.getOffhandItem().is(SistersContent.LOMEYA_HAMMER_OFFHAND.get());
            double radius=scythe?(alternate?7.0:5.4):(dual?5.6:4.0);
            float damage=scythe?(alternate?53F:35F):(dual?(alternate?67F:46F):30F);
            Vec3 center=user.position().add(0,.7,0);
            Vec3 look=user.getLookAngle().multiply(1,0,1).normalize();
            int hits=0;
            if(!scythe)user.getPersistentData().putBoolean(AOE,true);
            try{
                for(LivingEntity target:world.getEntitiesOfClass(LivingEntity.class,user.getBoundingBox().inflate(radius,3,radius),t->valid(user,t))){
                    Vec3 difference=target.position().subtract(user.position()).multiply(1,0,1);
                    if(difference.lengthSqr()>radius*radius||!user.hasLineOfSight(target))continue;
                    if(scythe&&alternate&&difference.lengthSqr()>1&&look.dot(difference.normalize())<.62)continue;
                    if(target.hurt(world.damageSources().playerAttack(user),damage)){
                        debuff(target);
                        if(scythe)target.getPersistentData().putLong(MARK,world.getGameTime()+140);
                        if(!scythe){
                            target.knockback(dual?1.4:.85,user.getX()-target.getX(),user.getZ()-target.getZ());
                            if(dual&&alternate)target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,110,1));
                        }else if(alternate)target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,70,2));
                        hits++;
                    }
                }
            }finally{
                if(!scythe)user.getPersistentData().remove(AOE);
            }
            int count=dual?72:48;
            for(int i=0;i<count;i++){
                double angle=i*Math.PI*2/count;
                double distance=radius*(.55+.45*(i%3)/2.0);
                world.sendParticles(scythe?ParticleTypes.SOUL_FIRE_FLAME:dual?ParticleTypes.SOUL:ParticleTypes.CRIT,
                    center.x+Math.cos(angle)*distance,center.y+.08*(i%5),center.z+Math.sin(angle)*distance,1,0,0,0,0);
            }
            if(dual){
                world.sendParticles(ParticleTypes.EXPLOSION,center.x,center.y,center.z,alternate?8:4,1.3,.3,1.3,0);
                user.swing(InteractionHand.OFF_HAND,true);
                user.getOffhandItem().hurtAndBreak(3,user,EquipmentSlot.OFFHAND);
            }
            user.swing(InteractionHand.MAIN_HAND,true);
            user.getCooldowns().addCooldown(this,scythe?(alternate?260:180):dual?(alternate?260:190):150);
            stack.hurtAndBreak(dual?4:3,user,EquipmentSlot.MAINHAND);
        }
        @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag flag){
            super.appendHoverText(stack,context,lines,flag);
            if(scythe){
                lines.add(Component.literal("Удар: иссушение II, слабость и метка душ").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
                lines.add(Component.literal("Повторный удар по метке: +9 магического урона").withStyle(net.minecraft.ChatFormatting.AQUA));
                lines.add(Component.literal("Удержать ПКМ → отпустить: жатва душ, 35 урона, радиус 5.4").withStyle(net.minecraft.ChatFormatting.GOLD));
                lines.add(Component.literal("Shift + ПКМ: направленный разрез, 53 урона, дальность 7").withStyle(net.minecraft.ChatFormatting.YELLOW));
            }else{
                lines.add(Component.literal("Удар: замедление; перелом руки игроков").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
                lines.add(Component.literal("ПКМ: удар по земле, 30 урона, отбрасывание").withStyle(net.minecraft.ChatFormatting.GOLD));
                lines.add(Component.literal("С двумя молотами: волна 46 урона, радиус 5.6").withStyle(net.minecraft.ChatFormatting.YELLOW));
                lines.add(Component.literal("Два молота + Shift + ПКМ: разлом земли, 67 урона").withStyle(net.minecraft.ChatFormatting.AQUA));
                lines.add(Component.literal("Два молота: дополнительный удар второй рукой").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            lines.add(Component.literal("Рунные слоты: "+org.slavicmyths.rpg.Runes.slots(stack)+" / 3").withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
    @SubscribeEvent public static void prepareDualHammer(AttackEntityEvent event){
        if(!(event.getEntity() instanceof ServerPlayer owner))return;
        owner.getPersistentData().remove(READY);
        owner.getPersistentData().remove(READY_TICK);
        if(!(event.getTarget() instanceof LivingEntity target)
            ||!owner.getMainHandItem().is(SistersContent.LOMEYA_HAMMER.get())
            ||!owner.getOffhandItem().is(SistersContent.LOMEYA_HAMMER_OFFHAND.get())
            ||owner.getAttackStrengthScale(.5F)<.9F
            ||!Weapon.valid(owner,target))return;
        owner.getPersistentData().putUUID(READY,target.getUUID());
        owner.getPersistentData().putLong(READY_TICK,owner.serverLevel().getGameTime());
    }
    @SubscribeEvent public static void dualHammer(LivingDamageEvent.Post event){
        LivingEntity target=event.getEntity();
        if(!(event.getSource().getEntity() instanceof ServerPlayer owner)
            ||!event.getSource().is(DamageTypes.PLAYER_ATTACK)
            ||!owner.getMainHandItem().is(SistersContent.LOMEYA_HAMMER.get())
            ||!owner.getOffhandItem().is(SistersContent.LOMEYA_HAMMER_OFFHAND.get())
            ||owner.getPersistentData().getBoolean(AOE)
            ||owner.getPersistentData().getBoolean(DUAL)
            ||!owner.getPersistentData().hasUUID(READY)
            ||!owner.getPersistentData().getUUID(READY).equals(target.getUUID())
            ||owner.getPersistentData().getLong(READY_TICK)!=owner.serverLevel().getGameTime()
            ||!Weapon.valid(owner,target))return;
        owner.getPersistentData().putBoolean(DUAL,true);
        try{
            owner.swing(InteractionHand.OFF_HAND,true);
            if(target.hurt(owner.damageSources().playerAttack(owner),9F)){
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,90,1));
                if(target instanceof Player player)FractureHooks.apply(player);
                owner.getOffhandItem().hurtAndBreak(1,owner,EquipmentSlot.OFFHAND);
            }
        }finally{
            owner.getPersistentData().remove(DUAL);
            owner.getPersistentData().remove(READY);
            owner.getPersistentData().remove(READY_TICK);
        }
    }
    private BossRewardWeapons(){}
}
