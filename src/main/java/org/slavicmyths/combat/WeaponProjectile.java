package org.slavicmyths.combat;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.sounds.SoundEvents;
import org.slavicmyths.registry.ModEntities;

/** Native arrow collision/save/pickup, with one server resolution and exact ItemStack recovery. */
public final class WeaponProjectile extends AbstractArrow implements ItemSupplier {
    public static final float KNIFE_LOSS_CHANCE=.25F;
    private static final EntityDataAccessor<ItemStack> DISPLAY=SynchedEntityData.defineId(WeaponProjectile.class,EntityDataSerializers.ITEM_STACK);
    private boolean dealtDamage,knife,resolved,playerThrown;
    private int flightTicks;
    public WeaponProjectile(EntityType<? extends WeaponProjectile> type,Level level){super(type,level);}
    public WeaponProjectile(Level level,LivingEntity owner,ItemStack stack,boolean knife,boolean creative) {
        super(ModEntities.WEAPON_PROJECTILE.get(),owner,level,stack,stack);
        this.knife=knife;
        this.playerThrown=owner instanceof Player;
        pickup=creative?Pickup.CREATIVE_ONLY:Pickup.ALLOWED;
        entityData.set(DISPLAY,stack.copyWithCount(1));
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(DISPLAY,ItemStack.EMPTY);}
    @Override protected ItemStack getDefaultPickupItem(){return new ItemStack(Items.STICK);}
    @Override public ItemStack getItem(){return entityData.get(DISPLAY);}
    public boolean isKnife(){return getItem().getItem() instanceof ThrowingWeaponItem item?item.knife:knife;}
    @Override public void playerTouch(Player player){if(isRemoved()||resolved)return;super.playerTouch(player);if(isRemoved())resolved=true;}
    @Override protected EntityHitResult findHitEntity(Vec3 start,Vec3 end){return dealtDamage?null:super.findHitEntity(start,end);}
    @Override protected boolean canHitEntity(Entity entity) {
        Entity owner=getOwner();
        if(playerThrown&&owner==null)return false; // Logout/dimension changes cannot bypass the owner's PvP/team rules.
        return super.canHitEntity(entity)&&(!(owner instanceof Player player)||!(entity instanceof LivingEntity living)||org.slavicmyths.rpg.Abilities.canHit(player,living));
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if(level().isClientSide||dealtDamage||resolved)return;
        Entity target=hit.getEntity(),owner=getOwner();
        ItemStack weapon=getPickupItemStackOrigin();
        float damage=weapon.getItem() instanceof ThrowingWeaponItem item?item.thrownDamage():0;
        var source=damageSources().trident(this,owner==null?this:owner);
        if(level() instanceof ServerLevel server)damage=EnchantmentHelper.modifyDamage(server,weapon,target,source,damage);
        dealtDamage=true;
        setPos(hit.getLocation());
        if(target.hurt(source,damage)&&level() instanceof ServerLevel server)EnchantmentHelper.doPostAttackEffectsWithItemSource(server,target,source,weapon);
        setDeltaMovement(getDeltaMovement().multiply(-.2,-.1,-.2).add(0,.12,0));
        playSound(SoundEvents.TRIDENT_HIT,.5F,knife?1.6F:1.1F);
        if(knife)recover(true);
    }
    @Override protected void onHitBlock(BlockHitResult hit) {
        if(resolved)return;
        super.onHitBlock(hit);
        if(!level().isClientSide&&knife)recover(false);
    }
    private void recover(boolean lossAllowed) {
        if(resolved||level().isClientSide)return;
        resolved=true; // Set before spawning; persisted resolution cannot mint another item.
        if(pickup==Pickup.ALLOWED&&(!lossAllowed||random.nextFloat()>=KNIFE_LOSS_CHANCE)) {
            var dropped=spawnAtLocation(getPickupItem(),.1F);
            if(dropped!=null){dropped.setDeltaMovement(getDeltaMovement().scale(.35).add(0,.12,0));dropped.setPickUpDelay(10);}
        }
        discard();
    }
    @Override public void tick() {
        if(resolved){discard();return;}
        super.tick();
        if(!level().isClientSide&&!inGround&&!isRemoved()&&++flightTicks> (knife?24:70))recover(false);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("Knife",knife);tag.putBoolean("DealtDamage",dealtDamage);tag.putBoolean("Resolved",resolved);tag.putBoolean("PlayerThrown",playerThrown);tag.putInt("FlightTicks",flightTicks);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);knife=tag.getBoolean("Knife");dealtDamage=tag.getBoolean("DealtDamage");resolved=tag.getBoolean("Resolved");playerThrown=tag.getBoolean("PlayerThrown");flightTicks=tag.getInt("FlightTicks");entityData.set(DISPLAY,getPickupItemStackOrigin().copy());}
}
