package org.slavicmyths.morovoy;

import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.sisters.SistersContent;

/** Composed Raven+Fire Serpent staff: separate fire/Mor modes, real owned birds and area strike. */
public final class MorovoyStaff {
    public static final DeferredHolder<Item,Item> STAFF = ModItems.ITEMS.register("morovoy_staff", Staff::new);
    public static final DeferredHolder<Item,Item> CONDUCTOR = ModItems.ITEMS.register("morovoy_conductor", () -> new Item(new Item.Properties().rarity(Rarity.RARE)) {
        @Override public Component getName(ItemStack stack) {return Component.literal("Моровый проводник");}
    });
    public static void init(){}

    private static ItemAttributeModifiers weaponAttributes() {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,6,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,-2.4,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
            .build();
    }

    public static final class Staff extends Item {
        public Staff() {super(new Item.Properties().durability(1500).fireResistant().rarity(Rarity.EPIC).attributes(weaponAttributes()));}
        @Override public Component getName(ItemStack stack){return Component.literal("Посох Мора");}
        private int mode(ItemStack stack) {return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("MorovoyStaffMode")==1?1:0;}
        private void mode(ItemStack stack,int value) {CustomData.update(DataComponents.CUSTOM_DATA,stack,n->n.putInt("MorovoyStaffMode",value));}
        private static boolean cooldown(ServerPlayer player,String id,long duration){
            String key="MorStaff25_"+id;
            long now=player.serverLevel().getGameTime(),until=player.getPersistentData().getLong(key);
            if(now<until){player.displayClientMessage(Component.literal("Посох: перезарядка "+((until-now+19)/20)+" сек."),true);return false;}
            player.getPersistentData().putLong(key,now+duration);return true;
        }
        private static boolean legal(ServerPlayer player,LivingEntity target){
            return target!=player&&target.isAlive()&&!player.isAlliedTo(target)&&org.slavicmyths.rpg.Abilities.canHit(player,target);
        }

        @Override public InteractionResultHolder<ItemStack> use(Level level,net.minecraft.world.entity.player.Player player,InteractionHand hand) {
            ItemStack held=player.getItemInHand(hand);
            if(hand!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(held);
            if(player instanceof ServerPlayer server){
                if(player.isShiftKeyDown())summon(server,held);
                else bolt(server,held);
            }
            return InteractionResultHolder.success(held);
        }

        @Override public InteractionResult useOn(UseOnContext context) {
            if(context.getHand()!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;
            if(context.getPlayer() instanceof ServerPlayer player) {
                if(player.isShiftKeyDown())area(player,context.getClickLocation(),context.getItemInHand());
                else{
                    int value=mode(context.getItemInHand())==0?1:0;
                    mode(context.getItemInHand(),value);
                    player.displayClientMessage(Component.literal("Посох Мора: "+(value==0?"Мор":"Огонь")),true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        private void bolt(ServerPlayer player,ItemStack held) {
            if(!cooldown(player,"bolt",8))return;
            ServerLevel world=player.serverLevel();
            int element=mode(held);
            Vec3 eye=player.getEyePosition(),end=eye.add(player.getLookAngle().scale(25));
            var wall=world.clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
            if(wall.getType()!=net.minecraft.world.phys.HitResult.Type.MISS)end=wall.getLocation();
            LivingEntity chosen=null;double nearest=eye.distanceToSqr(end);
            for(LivingEntity target:world.getEntitiesOfClass(LivingEntity.class,new AABB(eye,end).inflate(.55),target->legal(player,target))) {
                var impact=target.getBoundingBox().inflate(.35).clip(eye,end);
                if(impact.isPresent()&&eye.distanceToSqr(impact.get())<nearest){nearest=eye.distanceToSqr(impact.get());chosen=target;}
            }
            if(chosen!=null&&chosen.hurt(player.damageSources().indirectMagic(player,player),element==0?8F:11F)){
                player.setLastHurtMob(chosen);
                if(element==0){
                    chosen.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER,80,0),player);
                    chosen.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,100,0),player);
                }else chosen.igniteForSeconds(5);
            }
            Vec3 travel=end.subtract(eye);int steps=Math.min(40,Math.max(1,(int)(travel.length()*2)));
            for(int i=0;i<steps;i++){
                Vec3 p=eye.add(travel.scale((i+.5)/steps));
                world.sendParticles(element==0?ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.FLAME,p.x,p.y,p.z,1,.03,.03,.03,.005);
            }
            held.hurtAndBreak(1,player,EquipmentSlot.MAINHAND);
        }

        private void summon(ServerPlayer player,ItemStack held){
            if(!cooldown(player,"flock",900))return;
            ServerLevel world=player.serverLevel();
            LivingEntity preferred=player.getLastHurtMob();
            if(preferred!=null&&(!legal(player,preferred)||preferred.distanceToSqr(player)>32*32))preferred=null;
            int existing=world.getEntitiesOfClass(MorovoyRaven.class,player.getBoundingBox().inflate(40),bird->bird.summonedBy(player)).size();
            int amount=Math.min(3,3-existing);
            for(int i=0;i<amount;i++){
                MorovoyRaven bird=MorovoyRavenContent.MINION.get().create(world);
                if(bird==null)continue;
                bird.setPos(player.getX()+Math.cos(i*2.1)*1.5,player.getY()+2,player.getZ()+Math.sin(i*2.1)*1.5);
                bird.attachStaff(player,preferred,i);
                world.addFreshEntity(bird);
            }
            player.displayClientMessage(Component.literal("Призвана моровая стая: "+amount),true);
            held.hurtAndBreak(4,player,EquipmentSlot.MAINHAND);
        }

        private void area(ServerPlayer player,Vec3 center,ItemStack held){
            if(center.distanceToSqr(player.position())>20*20){player.displayClientMessage(Component.literal("Слишком далеко для удара"),true);return;}
            if(!cooldown(player,"area",800))return;
            ServerLevel world=player.serverLevel();
            var targets=world.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(5),
                target->legal(player,target)&&target.distanceToSqr(center)<=25);
            targets.sort(Comparator.comparingDouble(t->t.distanceToSqr(center)));
            for(LivingEntity target:targets.stream().limit(12).toList())if(target.hurt(player.damageSources().indirectMagic(player,player),12F)){
                target.igniteForSeconds(5);
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER,100,0),player);
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,60,0),player);
            }
            for(int i=0;i<48;i++){
                double a=i*Math.PI/24,r=2+(i%4),x=center.x+Math.cos(a)*r,z=center.z+Math.sin(a)*r;
                world.sendParticles(i%2==0?ParticleTypes.FLAME:ParticleTypes.SOUL_FIRE_FLAME,x,center.y+.5,z,3,.2,.45,.2,.015);
            }
            held.hurtAndBreak(8,player,EquipmentSlot.MAINHAND);
            player.displayClientMessage(Component.literal("Огненно-моровой удар!"),true);
        }

        @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag) {
            super.appendHoverText(stack,context,lines,flag);
            lines.add(Component.literal("Режим: "+(mode(stack)==0?"Мор (Иссушение I, Слабость)":"Огонь (поджог)")).withStyle(ChatFormatting.GOLD));
            lines.add(Component.literal("ПКМ в воздухе: магический выстрел (8/11 урона)").withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Shift + ПКМ в воздухе: 2–3 боевых ворона (45 сек.)").withStyle(ChatFormatting.DARK_PURPLE));
            lines.add(Component.literal("ПКМ по блоку: сменить режим").withStyle(ChatFormatting.AQUA));
            lines.add(Component.literal("Shift + ПКМ по блоку: удар огнём и мором по области").withStyle(ChatFormatting.RED));
            lines.add(Component.literal("Рунные слоты: "+org.slavicmyths.rpg.Runes.slots(stack)+" / 3").withStyle(ChatFormatting.GRAY));
        }
    }
    private MorovoyStaff(){}
}
