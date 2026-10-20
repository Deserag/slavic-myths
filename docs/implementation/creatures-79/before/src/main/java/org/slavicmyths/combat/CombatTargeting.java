package org.slavicmyths.combat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.slavicmyths.entity.LandSpiritEntity;

/** Local combat/control policy. No faction manager or global entity scans. */
public final class CombatTargeting {
    private static TagKey<EntityType<?>> tag(String id){return TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths",id));}
    public static final TagKey<EntityType<?>> HOSTILE=tag("village_hostiles"),PROTECTED=tag("combat_protected"),CONTROL_IMMUNE=tag("control_immune");
    public static boolean canAttack(LivingEntity owner,LivingEntity target){
        if(owner==null||target==null||owner==target||owner.isSpectator()||target instanceof net.minecraft.world.entity.decoration.ArmorStand||!owner.isAlive()||!target.isAlive()||target.isRemoved()||target.isSpectator()||CreatureSafety.protectedPlayer(target)||target.isInvulnerable()||target.getType().is(PROTECTED)||owner.isAlliedTo(target)||target.isAlliedTo(owner))return false;
        if(target instanceof TamableAnimal pet&&pet.getOwnerUUID()!=null&&pet.getOwnerUUID().equals(owner.getUUID()))return false;
        if(owner instanceof Player player){
            if(target instanceof Player other&&!player.canHarmPlayer(other))return false;
            if(target instanceof LandSpiritEntity spirit&&spirit.permits(player))return false;
        }
        return true;
    }
    public static boolean canControl(LivingEntity owner,LivingEntity target){return canAttack(owner,target)&&!target.getType().is(CONTROL_IMMUNE);}
    public static boolean isHostileCombatTarget(LivingEntity owner,LivingEntity target){return canAttack(owner,target)&&isHostileTo(owner,target);}
    public static boolean isHostileTo(LivingEntity owner,LivingEntity target){
        // Neutral spirits/pets become automatic targets only while actually attacking this owner.
        if(target.getType().is(HOSTILE))return true;
        if(target instanceof LandSpiritEntity||target instanceof TamableAnimal)return target instanceof Mob mob&&mob.getTarget()==owner;
        return target instanceof Enemy||target.getType().is(HOSTILE)||target instanceof Mob mob&&mob.getTarget()==owner;
    }
    public static boolean controlResistant(LivingEntity target){return org.slavicmyths.rpg.classes.ClassControl.resistant(target)||target.getType().is(org.slavicmyths.rpg.RareRuneRuntime.IMMUNE);}
    private CombatTargeting(){}
}
