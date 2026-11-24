package org.slavicmyths.winter;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slavicmyths.combat.WeaponInputHandler;
import org.slavicmyths.morozko.FrostShard;
import org.slavicmyths.rpg.PathData;

@EventBusSubscriber(modid="slavicmyths")
public final class WinterRanged {
    public static final String SHOT="SlavicWinterArrow";
    private static final String CHARGED="WinterCharged", READY="WinterChargeReady";
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    private static boolean ready(ServerPlayer p,String key){return PathData.data(p).getLong("CD_winter_"+key)<=now(p);}
    private static void cd(ServerPlayer p,String key,int ticks){PathData.data(p).putLong("CD_winter_"+key,now(p)+ticks);}
    private static boolean valid(ServerPlayer p,ItemStack held,WeaponInputHandler.Action action){
        return p.getMainHandItem()==held&&p.isAlive()&&!p.isSpectator()&&
                ((action==WeaponInputHandler.Action.SHIFT_LEFT_CLICK||action==WeaponInputHandler.Action.SHIFT_RIGHT_CLICK)==p.isShiftKeyDown());
    }
    private static ItemStack ammo(ServerPlayer p){
        for(var s:p.getInventory().offhand)if(s.is(Items.ARROW)&&!s.isEmpty())return s;
        for(var s:p.getInventory().items)if(s.is(Items.ARROW)&&!s.isEmpty())return s;
        return ItemStack.EMPTY;
    }
    private static boolean hasAmmo(ServerPlayer p){return p.hasInfiniteMaterials()||!ammo(p).isEmpty();}
    private static void wear(ServerPlayer p,ItemStack stack){
        if(!p.hasInfiniteMaterials())stack.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
        p.swing(InteractionHand.MAIN_HAND,true);
    }
    private static boolean arrow(ServerPlayer p,ItemStack weapon,float raw,float yawOffset){
        var ammunition=ammo(p);if(!p.hasInfiniteMaterials()&&ammunition.isEmpty())return false;
        var arrow=new Arrow(p.serverLevel(),p,new ItemStack(Items.ARROW),weapon.copyWithCount(1));
        float speed=2.25F;arrow.setBaseDamage(raw/speed);
        arrow.pickup=p.hasInfiniteMaterials()?AbstractArrow.Pickup.CREATIVE_ONLY:AbstractArrow.Pickup.ALLOWED;
        arrow.getPersistentData().putBoolean(SHOT,true);
        arrow.shootFromRotation(p,p.getXRot(),p.getYRot()+yawOffset,0,speed,0);
        if(!p.serverLevel().addFreshEntity(arrow))return false;
        if(!p.hasInfiniteMaterials())ammunition.shrink(1);
        return true;
    }
    private static boolean shard(ServerPlayer p,float raw){
        var ammunition=ammo(p);if(!p.hasInfiniteMaterials()&&ammunition.isEmpty())return false;
        FrostShard.launch(p,p.getEyePosition().add(p.getLookAngle().scale(24)),raw,0,null);
        if(!p.hasInfiniteMaterials())ammunition.shrink(1);
        return true;
    }
    public static boolean charged(ItemStack stack){
        return stack.getItem() instanceof Crossbow&&stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBoolean(CHARGED);
    }
    private static void charge(ItemStack stack,boolean on,long ready){
        var n=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        n.putBoolean(CHARGED,on);if(on)n.putLong(READY,ready);else n.remove(READY);
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(n));
    }
    public static final class Bow extends BowItem implements WeaponInputHandler {
        public Bow(){super(new Item.Properties().durability(680).rarity(Rarity.RARE));}
        @Override public boolean intercepts(Action a){return a==Action.LEFT_CLICK||a==Action.RIGHT_CLICK||a==Action.SHIFT_RIGHT_CLICK;}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand h){return InteractionResultHolder.fail(p.getItemInHand(h));}
        @Override public void onWeaponAction(ServerPlayer p,ItemStack held,Action a){
            if(!valid(p,held,a)||held.getItem()!=this)return;
            String key="bow_"+a.id();if(!ready(p,key))return;
            if(a==Action.LEFT_CLICK&&arrow(p,held,10,0)){cd(p,key,16);wear(p,held);}
            else if(a==Action.RIGHT_CLICK&&shard(p,16)){cd(p,key,100);wear(p,held);}
            else if(a==Action.SHIFT_RIGHT_CLICK){
                int shots=0;for(int i=-2;i<=2;i++)if(arrow(p,held,6,i*5))shots++;
                if(shots>0){cd(p,key,240);wear(p,held);}
            }
        }
        @Override public void appendHoverText(ItemStack stack,TooltipContext c,List<net.minecraft.network.chat.Component> lines,TooltipFlag f){lines.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".skills"));}
    }
    public static final class Crossbow extends CrossbowItem implements WeaponInputHandler {
        public Crossbow(){super(new Item.Properties().durability(720).rarity(Rarity.UNCOMMON));}
        @Override public boolean intercepts(Action a){return a==Action.LEFT_CLICK||a==Action.RIGHT_CLICK;}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand h){return InteractionResultHolder.fail(p.getItemInHand(h));}
        @Override public void onWeaponAction(ServerPlayer p,ItemStack held,Action a){
            if(!valid(p,held,a)||held.getItem()!=this)return;
            if(a==Action.RIGHT_CLICK){
                if(charged(held)||!ready(p,"crossbow_charge")||!hasAmmo(p))return;
                charge(held,true,now(p)+20);cd(p,"crossbow_charge",120);
                p.displayClientMessage(net.minecraft.network.chat.Component.translatable("winter.weapon.bolt_charged"),true);return;
            }
            if(a!=Action.LEFT_CLICK||!ready(p,"crossbow_fire"))return;
            boolean special=charged(held);
            if(special&&held.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getLong(READY)>now(p))return;
            if(special?shard(p,20):arrow(p,held,13,0)){
                if(special)charge(held,false,0);cd(p,"crossbow_fire",25);wear(p,held);
            }
        }
        @Override public void appendHoverText(ItemStack stack,TooltipContext c,List<net.minecraft.network.chat.Component> lines,TooltipFlag f){
            super.appendHoverText(stack,c,lines,f);lines.add(net.minecraft.network.chat.Component.translatable(getDescriptionId()+".skills"));
        }
    }
    @SubscribeEvent public static void trail(EntityTickEvent.Post e){
        if(e.getEntity() instanceof Arrow arrow&&arrow.level() instanceof net.minecraft.server.level.ServerLevel w&&arrow.tickCount%3==0&&
                arrow.getPersistentData().getBoolean(SHOT)&&arrow.getDeltaMovement().lengthSqr()>.05)
            w.sendParticles(ParticleTypes.SNOWFLAKE,arrow.getX(),arrow.getY(),arrow.getZ(),1,.04,.04,.04,0);
    }
    private WinterRanged(){}
}
