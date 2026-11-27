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
    private boolean dealtDamage,knife,resolved,playerThrown,netherSkill,pairThrow;
    private float pairDamage=18;
    private float desertDamage;
    private int desertPoison,desertPierce;
    private boolean desertKnockback;
    private final java.util.Set<java.util.UUID> desertHits=new java.util.HashSet<>();
    public void configureDesert(float damage,int poisonTicks,boolean push,int pierce){desertDamage=damage;desertPoison=poisonTicks;desertKnockback=push;desertPierce=pierce;pickup=Pickup.DISALLOWED;setNoGravity(true);}
    private java.util.UUID summonedTarget;
    private float summonedDamage;
    public void configureSummonedSabre(java.util.UUID target,float damage){summonedTarget=target;summonedDamage=damage;pickup=Pickup.DISALLOWED;setNoGravity(true);}
    public void configurePairThrow(){configurePairThrow(18);}
    public void configurePairThrow(float damage){pairThrow=true;pairDamage=damage;pickup=Pickup.DISALLOWED;}
    public static java.util.List<WeaponProjectile> pairedInFlight(Player p){return p.level().getEntitiesOfClass(WeaponProjectile.class,p.getBoundingBox().inflate(80),v->v.isAlive()&&v.getOwner()==p&&v.getItem().getItem()==p.getMainHandItem().getItem());}
    public void recallPair(){if(pairThrow&&!returning&&!resolved)beginReturn();}
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
        if(desertDamage>0&&(!(owner instanceof net.minecraft.server.level.ServerPlayer p)||!(entity instanceof LivingEntity t)||desertHits.contains(t.getUUID())||!CombatTargeting.canAttack(p,t,CombatTargeting.Context.PROJECTILE)))return false;
        if(pairThrow&&owner instanceof net.minecraft.server.level.ServerPlayer p&&org.slavicmyths.savanna.SteppeWeapons.kind(getItem())!=null&&entity instanceof LivingEntity t&&!CombatTargeting.canAttack(p,t,CombatTargeting.Context.PROJECTILE))return false;
        if(summonedTarget!=null&&!summonedTarget.equals(entity.getUUID()))return false;
        if(playerThrown&&owner==null)return false; // Logout/dimension changes cannot bypass the owner's PvP/team rules.
        return super.canHitEntity(entity)&&(!(owner instanceof Player player)||!(entity instanceof LivingEntity living)||org.slavicmyths.rpg.Abilities.canHit(player,living));
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if(level().isClientSide||dealtDamage||resolved)return;
        Entity target=hit.getEntity(),owner=getOwner();
        if(desertDamage>0){
            if(owner instanceof net.minecraft.server.level.ServerPlayer p&&target instanceof LivingEntity t&&CombatTargeting.canAttack(p,t,CombatTargeting.Context.PROJECTILE)&&desertHits.add(t.getUUID())){
                boolean needle=getPersistentData().getBoolean("SteppeNeedle");
                if(t.hurt(MythDamageSources.caused(needle?"steppe_weapon_skill":"desert_weapon_skill",p),desertDamage)&&CombatTargeting.canControl(p,t)&&!CombatTargeting.controlResistant(t)){
                    if(needle)org.slavicmyths.savanna.SteppeWeaponSkills.needleHit(p,t,getPersistentData().getInt("SteppeProjectilePoison"));
                    if(desertPoison>0)t.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,desertPoison));
                    if(desertKnockback){Vec3 d=t.position().subtract(p.position());t.knockback(.7,-d.x,-d.z);}
                }
            }
            if(desertPierce<=0||desertHits.size()>desertPierce)discard();else {setPos(hit.getLocation().add(getDeltaMovement().normalize().scale(.15)));}
            return;
        }
        if(summonedTarget!=null){if(owner instanceof net.minecraft.server.level.ServerPlayer p&&target instanceof LivingEntity t&&summonedTarget.equals(t.getUUID())&&CombatTargeting.canAttack(p,t,CombatTargeting.Context.DIRECT)&&p.hasLineOfSight(t)){t.hurt(MythDamageSources.periodic("reward_sweep",p),summonedDamage);if(level() instanceof ServerLevel w)w.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,t.getX(),t.getY()+t.getBbHeight()*.5,t.getZ(),12,.2,.3,.2,.1);}discard();return;}
        ItemStack weapon=getPickupItemStackOrigin();
        float damage=pairThrow?pairDamage:weapon.getItem() instanceof ThrowingWeaponItem item?item.projectileDamage(this):0;
        var source=damageSources().trident(this,owner==null?this:owner);
        if(level() instanceof ServerLevel server)damage=EnchantmentHelper.modifyDamage(server,weapon,target,source,damage);
        dealtDamage=true;
        setPos(hit.getLocation());
        boolean success=target.hurt(source,damage);
        if(success&&level() instanceof ServerLevel server)EnchantmentHelper.doPostAttackEffectsWithItemSource(server,target,source,weapon);
        if(weapon.getItem() instanceof ThrowingWeaponItem item&&target instanceof LivingEntity living&&owner instanceof LivingEntity shooter)item.projectileHit(this,living,shooter,success);
        setDeltaMovement(getDeltaMovement().multiply(-.2,-.1,-.2).add(0,.12,0));
        playSound(SoundEvents.TRIDENT_HIT,.5F,knife?1.6F:1.1F);
        if(pairThrow)beginReturn();else if(isKnife()){if(loyalty()>0)beginReturn();else recover(true);}
    }
    @Override protected void onHitBlock(BlockHitResult hit) {
        if(resolved)return;
        if(desertDamage>0){discard();return;}
        if(summonedTarget!=null){discard();return;}
        super.onHitBlock(hit);
        if(!level().isClientSide){if(pairThrow)beginReturn();else if(isKnife()){if(loyalty()>0)beginReturn();else recover(false);}}
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
        setDeltaMovement(getDeltaMovement().scale(.95).add(delta.normalize().scale( .05*(pairThrow?3:loyalty()))));move(net.minecraft.world.entity.MoverType.SELF,getDeltaMovement());
    }
    @Override public void tick() {
        if(resolved){discard();return;}
        if(desertDamage>0&&!level().isClientSide){
            if(!(getOwner() instanceof net.minecraft.server.level.ServerPlayer p)||!p.isAlive()||p.isSpectator()||++flightTicks>40){discard();return;}
            if(level() instanceof ServerLevel w){if(getPersistentData().getBoolean("SteppeNeedle"))w.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.45F,.95F,.12F),.8F),getX(),getY(),getZ(),2,.04,.04,.04,0);else w.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,getX(),getY(),getZ(),1,.03,.03,.03,0);}
            super.tick();return;
        }
        if(summonedTarget!=null&&level() instanceof ServerLevel w){
            if(++flightTicks>30||!(getOwner() instanceof net.minecraft.server.level.ServerPlayer p)||!p.isAlive()||!(w.getEntity(summonedTarget) instanceof LivingEntity t)||!t.isAlive()||!p.hasLineOfSight(t)||!CombatTargeting.canAttack(p,t,CombatTargeting.Context.DIRECT)){discard();return;}
            setDeltaMovement(t.position().add(0,t.getBbHeight()*.5,0).subtract(position()).normalize().scale(.85));
            super.tick();return;
        }
        if(returning){if(!level().isClientSide)returnToOwner();else{setNoPhysics(true);setNoGravity(true);super.tick();}return;}
        super.tick();
        if(!level().isClientSide&&!inGround&&!isRemoved()&&++flightTicks>(isKnife()?24:70)*RangeEnchantment.multiplier(level(),getPickupItemStackOrigin())){if(pairThrow||loyalty()>0)beginReturn();else recover(false);}
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key==RETURNING){returning=entityData.get(RETURNING);if(returning){setNoPhysics(true);setNoGravity(true);}}}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(summonedTarget!=null){tag.putUUID("KalinSummonedTarget",summonedTarget);tag.putFloat("KalinSummonedDamage",summonedDamage);}tag.putFloat("DesertDamage",desertDamage);tag.putInt("DesertPoison",desertPoison);tag.putInt("DesertPierce",desertPierce);tag.putBoolean("DesertKnockback",desertKnockback);var hitIds=new net.minecraft.nbt.ListTag();for(var id:desertHits)hitIds.add(net.minecraft.nbt.StringTag.valueOf(id.toString()));tag.put("DesertHits",hitIds);tag.putBoolean("NetherSkill",netherSkill);tag.putBoolean("WinterPairThrow",pairThrow);if(pairThrow)tag.putFloat("WinterPairDamage",pairDamage);tag.putBoolean("Knife",knife);tag.putBoolean("DealtDamage",dealtDamage);tag.putBoolean("Resolved",resolved);tag.putBoolean("PlayerThrown",playerThrown);tag.putInt("FlightTicks",flightTicks);tag.putBoolean("Returning",returning);tag.putInt("ReturnTicks",returnTicks);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);if(tag.hasUUID("KalinSummonedTarget"))configureSummonedSabre(tag.getUUID("KalinSummonedTarget"),tag.getFloat("KalinSummonedDamage"));if(tag.getFloat("DesertDamage")>0){configureDesert(tag.getFloat("DesertDamage"),tag.getInt("DesertPoison"),tag.getBoolean("DesertKnockback"),tag.getInt("DesertPierce"));for(var id:tag.getList("DesertHits",8))try{desertHits.add(java.util.UUID.fromString(id.getAsString()));}catch(IllegalArgumentException ignored){}}netherSkill=tag.getBoolean("NetherSkill");pairThrow=tag.getBoolean("WinterPairThrow");if(pairThrow){pickup=Pickup.DISALLOWED;pairDamage=tag.contains("WinterPairDamage")?tag.getFloat("WinterPairDamage"):18;}knife=tag.getBoolean("Knife");dealtDamage=tag.getBoolean("DealtDamage");resolved=tag.getBoolean("Resolved");playerThrown=tag.getBoolean("PlayerThrown");flightTicks=tag.getInt("FlightTicks");entityData.set(DISPLAY,getPickupItemStackOrigin().copy());returnTicks=tag.getInt("ReturnTicks");if(tag.getBoolean("Returning"))beginReturn();}
}
