package org.slavicmyths.nethersmithing;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/** Both bows preserve vanilla ammunition, enchantments and the existing FieldBow shot pipeline. */
public class InfernalBow extends org.slavicmyths.combat.FieldBowItem {
    private static final class Shot {
        final ServerPlayer owner;
        final ItemStack stack;
        final boolean requested;
        boolean committed, special;
        Shot(ServerPlayer player, ItemStack held, boolean skill) {
            owner=player;stack=held;requested=skill;
        }
    }
    private static final ThreadLocal<Shot> SHOT=new ThreadLocal<>();
    private final boolean empowered;

    public InfernalBow() { this(false); }
    protected InfernalBow(boolean upgrade) {
        super(true,new Item.Properties().durability(upgrade?1600:1200).fireResistant().rarity(Rarity.EPIC));
        empowered=upgrade;
    }
    public static final class Empowered extends InfernalBow {
        public Empowered() { super(true); }
    }

    public boolean empowered() { return empowered; }
    private String skillId() { return empowered?"empowered_infernal_bow":"infernal_bow"; }
    @Override public int drawTicks() { return empowered?14:16; }

    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity user,int remaining) {
        if (!(user instanceof ServerPlayer player)) {super.releaseUsing(stack,level,user,remaining);return;}
        Shot prior=SHOT.get();
        SHOT.set(new Shot(player,stack,player.isShiftKeyDown()&&NetherSkills.ready(player,stack,skillId())));
        try { super.releaseUsing(stack,level,user,remaining); }
        finally { if(prior==null)SHOT.remove();else SHOT.set(prior); }
    }

    @Override protected void shoot(ServerLevel server,LivingEntity owner,InteractionHand hand,ItemStack weapon,List<ItemStack> ammo,float velocity,float inaccuracy,boolean critical,LivingEntity target) {
        if(ammo.isEmpty())return;
        super.shoot(server,owner,hand,weapon,List.of(ammo.getFirst()),velocity,inaccuracy,critical,target);
    }

    @Override protected int getDurabilityUse(ItemStack ammo) {
        Shot shot=SHOT.get();
        return shot!=null&&shot.special?4:1;
    }

    @Override protected void shootProjectile(LivingEntity owner,Projectile projectile,int index,float velocity,float inaccuracy,float angle,LivingEntity target) {
        super.shootProjectile(owner,projectile,index,velocity,inaccuracy,angle,target);
        if(!(projectile instanceof AbstractArrow arrow))return;
        Shot shot=SHOT.get();
        boolean special=shot!=null&&shot.owner==owner&&shot.requested&&velocity>=3.599F;
        float raw=(special?(empowered?17F:14F):(empowered?14F:11.5F)*Math.min(1F,velocity/3.6F));
        arrow.getPersistentData().putFloat("InfernalRaw",raw);
        arrow.getPersistentData().putBoolean("InfernalSkill",special);
        arrow.getPersistentData().putBoolean("InfernalEmpowered",empowered);
        arrow.setBaseDamage(NetherWeaponRules.bowBase(raw,arrow.getDeltaMovement().length()));
        arrow.setRemainingFireTicks(Math.max(arrow.getRemainingFireTicks(),120));
        if(special&&!shot.committed){
            shot.special=true;shot.committed=true;
            NetherSkills.consume(shot.owner,shot.stack,skillId());
            shot.owner.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SMALL_FLAME,shot.owner.getX(),shot.owner.getEyeY(),shot.owner.getZ(),5,.15,.1,.15,.01);
        }
    }

    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack addition) {
        return NetherWeapons.TIER.getRepairIngredient().test(addition);
    }
}
