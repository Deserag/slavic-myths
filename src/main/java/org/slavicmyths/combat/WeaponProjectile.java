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
    private boolean dealtDamage,knife,resolved,playerThrown,netherSkill;
    public void netherSkill(boolean value){netherSkill=value;}
    public boolean netherSkill(){return netherSkill;}
    private int flightTicks,returnTicks;
    private boolean returning;
    private static final EntityDataAccessor<Boolean> RETURNING=SynchedEntityData.defineId(WeaponProjectile.class,EntityDataSerializers.BOOLEAN);
    public WeaponProjectile(EntityType<? extends WeaponProjectile> type,Level level){super(type,level);}
    public WeaponProjectile(Level level,LivingEntity owner,ItemStack stack,boolean knife,boolean creative) {
        super(ModEntities.WEAPON_PROJECTILE.get(),owner,level,stack,stack);
        this.knife=knife;
        this.playerThrown=owner instanceof Player;
        pickup=creative?Pickup.CREATIVE_ONLY:Pickup.ALLOWED;
        entityData.set(DISPLAY,stack.copyWithCount(1));
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(DISPLAY,ItemStack.EMPTY);builder.define(RETURNING,false);}
    @Override protected ItemStack getDefaultPickupItem(){return new ItemStack(Items.STICK);}
    @Override public ItemStack getItem(){return entityData.get(DISPLAY);}
    public boolean isKnife(){return getItem().getItem() instanceof ThrowingWeaponItem item?item.knife:knife;}
    @Override public void playerTouch(Player player){if(isRemoved()||resolved||returning&&getOwner()!=player)return;super.playerTouch(player);if(isRemoved())resolved=true;}
    @Override protected EntityHitResult findHitEntity(Vec3 start,Vec3 end){return dealtDamage||returning?null:super.findHitEntity(start,end);}
    @Override protected boolean canHitEntity(Entity entity) {
        Entity owner=getOwner();
        if(playerThrown&&owner==null)return false; // Logout/dimension changes cannot bypass the owner's PvP/team rules.
        return super.canHitEntity(entity)&&(!(owner instanceof Player player)||!(entity instanceof LivingEntity living)||org.slavicmyths.rpg.Abilities.canHit(player,living));
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if(level().isClientSide||dealtDamage||resolved)return;
        Entity target=hit.getEntity(),owner=getOwner();
        ItemStack weapon=getPickupItemStackOrigin();
        float damage=weapon.getItem() instanceof ThrowingWeaponItem item?item.projectileDamage(this):0;
        var source=damageSources().trident(this,owner==null?this:owner);
        if(level() instanceof ServerLevel server)damage=EnchantmentHelper.modifyDamage(server,weapon,target,source,damage);
        dealtDamage=true;
        setPos(hit.getLocation());
        boolean success=target.hurt(source,damage);
        if(success&&level() instanceof ServerLevel server)EnchantmentHelper.doPostAttackEffectsWithItemSource(server,target,source,weapon);
        if(weapon.getItem() instanceof ThrowingWeaponItem item&&target instanceof LivingEntity living&&owner instanceof LivingEntity shooter)item.projectileHit(this,living,shooter,success);
        setDeltaMovement(getDeltaMovement().multiply(-.2,-.1,-.2).add(0,.12,0));
        playSound(SoundEvents.TRIDENT_HIT,.5F,knife?1.6F:1.1F);
        if(isKnife()){if(loyalty()>0)beginReturn();else recover(true);}
    }
    @Override protected void onHitBlock(BlockHitResult hit) {
        if(resolved)return;
        super.onHitBlock(hit);
        if(!level().isClientSide&&isKnife()){if(loyalty()>0)beginReturn();else recover(false);}
    }
    private void recover(boolean lossAllowed) {
        if(resolved||level().isClientSide)return;
        resolved=true; // Set before spawning; persisted resolution cannot mint another item.
        if(pickup==Pickup.ALLOWED&&(!lossAllowed||loyalty()>0||random.nextFloat()>=KNIFE_LOSS_CHANCE)) {
            var dropped=spawnAtLocation(getPickupItem(),.1F);
            if(dropped!=null){dropped.setDeltaMovement(getDeltaMovement().scale(.35).add(0,.12,0));dropped.setPickUpDelay(10);}
        }
        discard();
    }
    public int loyalty(){if(!isKnife())return 0;var holder=level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOYALTY);return EnchantmentHelper.getItemEnchantmentLevel(holder,getPickupItemStackOrigin());}
    private void beginReturn(){returning=true;inGround=false;dealtDamage=true;entityData.set(RETURNING,true);setNoPhysics(true);setNoGravity(true);}
    private void returnToOwner(){var owner=getOwner();
        if(!(owner instanceof LivingEntity living)||!living.isAlive()||living.isRemoved()||living.isSpectator()||living.level()!=level()||++returnTicks>600){recover(false);return;}
        if(owner instanceof net.minecraft.server.level.ServerPlayer p&&p.getServer().getPlayerList().getPlayer(p.getUUID())!=p){recover(false);return;}
        Vec3 delta=owner.getEyePosition().subtract(position());
        if(delta.lengthSqr()<1.5){if(pickup==Pickup.ALLOWED&&owner instanceof Player p){ItemStack carried=getPickupItem().copy();p.getInventory().add(carried);if(!carried.isEmpty()){var drop=p.spawnAtLocation(carried);if(drop!=null)drop.setNoPickUpDelay();}}resolved=true;discard();return;}
        setDeltaMovement(getDeltaMovement().scale(.95).add(delta.normalize().scale(.05*loyalty())));move(net.minecraft.world.entity.MoverType.SELF,getDeltaMovement());
    }
    @Override public void tick() {
        if(resolved){discard();return;}
        if(returning){if(!level().isClientSide)returnToOwner();else{setNoPhysics(true);setNoGravity(true);super.tick();}return;}
        super.tick();
        if(!level().isClientSide&&!inGround&&!isRemoved()&&++flightTicks>(isKnife()?24:70)){if(loyalty()>0)beginReturn();else recover(false);}
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key==RETURNING){returning=entityData.get(RETURNING);if(returning){setNoPhysics(true);setNoGravity(true);}}}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("NetherSkill",netherSkill);tag.putBoolean("Knife",knife);tag.putBoolean("DealtDamage",dealtDamage);tag.putBoolean("Resolved",resolved);tag.putBoolean("PlayerThrown",playerThrown);tag.putInt("FlightTicks",flightTicks);tag.putBoolean("Returning",returning);tag.putInt("ReturnTicks",returnTicks);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);netherSkill=tag.getBoolean("NetherSkill");knife=tag.getBoolean("Knife");dealtDamage=tag.getBoolean("DealtDamage");resolved=tag.getBoolean("Resolved");playerThrown=tag.getBoolean("PlayerThrown");flightTicks=tag.getInt("FlightTicks");entityData.set(DISPLAY,getPickupItemStackOrigin().copy());returnTicks=tag.getInt("ReturnTicks");if(tag.getBoolean("Returning"))beginReturn();}
}
