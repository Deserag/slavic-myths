package org.slavicmyths.winter;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.*;
import org.slavicmyths.combat.*;
import org.slavicmyths.icequeen.*;
import org.slavicmyths.morozko.*;
import org.slavicmyths.rpg.PathData;

public final class WinterLegendItem extends SwordItem implements WeaponInputHandler {
    public static final int SPEAR_MELEE=32, SWORD_MELEE=34, SILENCE_WAVE=36, SILENCE_BLADE=11, SILENCE_EXECUTION=72;
    public static final int SPEAR_DASH=48, SPEAR_SWEEP=44, STAFF_BOLT=24, STAFF_SPIKES=42, GOLEM_DAMAGE=24;
    private final boolean queen;
    private static final String MODE="WinterStaffMode";
    private static final String CAST="WinterSilenceCast", HITS="WinterSilenceHits", CAST_UNTIL="WinterSilenceCastUntil";
    private static final String MARK="WinterSilenceMark", MARK_UNTIL="WinterSilenceMarkUntil";
    public WinterLegendItem(boolean queen){
        super(Tiers.DIAMOND,new Item.Properties().rarity(Rarity.EPIC).attributes(
                queen?org.slavicmyths.item.SpearItem.attributes(Tiers.DIAMOND,SPEAR_MELEE):SwordItem.createAttributes(Tiers.DIAMOND,SWORD_MELEE-1-Tiers.DIAMOND.getAttackDamageBonus(),-2.5F)));
        this.queen=queen;
    }
    public static boolean staffMode(ItemStack stack){
        return stack.getItem() instanceof WinterLegendItem item&&item.queen&&stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBoolean(MODE);
    }
    private static void toggle(ItemStack stack){
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        data.putBoolean(MODE,!data.getBoolean(MODE));stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
    }
    @Override public boolean intercepts(ItemStack held,Action action,HitResult.Type hit){
        return action!=Action.LEFT_CLICK||queen&&staffMode(held);
    }
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    private String key(Action action,boolean staff){return "CD_winter_"+(queen?(staff?"staff_":"spear_"):"silence_")+action.id();}
    private boolean ready(ServerPlayer p,Action action,boolean staff){return PathData.data(p).getLong(key(action,staff))<=now(p);}
    private void charge(ServerPlayer p,ItemStack stack,Action action,boolean staff,int ticks){
        PathData.data(p).putLong(key(action,staff),now(p)+ticks);
        if(!p.hasInfiniteMaterials())stack.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
        p.swing(InteractionHand.MAIN_HAND,true);
    }
    private static boolean legal(ServerPlayer p,LivingEntity t){
        return CombatTargeting.canAttack(p,t,CombatTargeting.Context.DIRECT)&&p.hasLineOfSight(t);
    }
    private static LivingEntity aim(ServerPlayer p,double reach){
        Vec3 eye=p.getEyePosition(),end=eye.add(p.getLookAngle().scale(reach));
        return p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().expandTowards(p.getLookAngle().scale(reach)).inflate(1),
                t->legal(p,t)&&t.getBoundingBox().inflate(.25).clip(eye,end).isPresent())
                .stream().min(java.util.Comparator.comparingDouble(t->FrostWorld.bboxDistance(eye,t.getBoundingBox()))).orElse(null);
    }
    private static void damage(ServerPlayer p,LivingEntity t,float amount){
        if(legal(p,t))t.hurt(MythDamageSources.caused("winter_weapon_skill",p),amount);
    }
    private static void area(ServerPlayer p,double radius,double angle,float amount){
        for(var t:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(radius),
                e->legal(p,e)&&e.distanceToSqr(p)<=radius*radius&&(angle>=180||FrostWorld.cone(p,e,radius,angle))
                &&(CombatTargeting.isEnemy(p,e,CombatTargeting.Context.ACQUIRE)||e==p.getLastHurtMob()))){
            damage(p,t,amount);
        }
        FrostVisuals.ring(p.serverLevel(),p.position(),radius,.15,32);
    }
    @Override public void onWeaponAction(ServerPlayer p,ItemStack held,Action action){
        if(p.getMainHandItem()!=held||held.getItem()!=this||!p.isAlive()||p.isSpectator())return;
        if((action==Action.SHIFT_LEFT_CLICK||action==Action.SHIFT_RIGHT_CLICK)!=p.isShiftKeyDown())return;
        boolean staff=staffMode(held);
        if(queen&&action==Action.RIGHT_CLICK){
            String cd="CD_winter_mode";
            if(PathData.data(p).getLong(cd)>now(p))return;
            toggle(held);PathData.data(p).putLong(cd,now(p)+10);
            p.displayClientMessage(net.minecraft.network.chat.Component.translatable(staffMode(held)?"winter.weapon.staff_mode":"winter.weapon.spear_mode"),true);
            return;
        }
        if(!ready(p,action,staff))return;
        if(queen){queenAction(p,held,action,staff);return;}
        switch(action){
            case RIGHT_CLICK -> {charge(p,held,action,false,120);area(p,8,35,SILENCE_WAVE);}
            case SHIFT_LEFT_CLICK -> {
                UUID cast=UUID.randomUUID();var state=p.getPersistentData();
                state.putUUID(CAST,cast);state.put(HITS,new CompoundTag());state.putLong(CAST_UNTIL,now(p)+100);
                for(int i=-3;i<=3;i++)FrostShard.launchSilenceBlade(p,i*2.5F,cast);
                charge(p,held,action,false,400);
            }
            case SHIFT_RIGHT_CLICK -> {
                var state=p.getPersistentData();
                if(!state.hasUUID(MARK)||state.getLong(MARK_UNTIL)<now(p))return;
                if(!(p.serverLevel().getEntity(state.getUUID(MARK)) instanceof LivingEntity target)||!legal(p,target)||target.distanceToSqr(p)>256)return;
                if(CombatTargeting.canControl(p,target)&&!CombatTargeting.controlResistant(target)&&!target.getType().is(CombatTargeting.BOSS)){
                    var delta=p.position().subtract(target.position());
                    if(delta.lengthSqr()>2)target.push(delta.normalize().x*.9,.1,delta.normalize().z*.9);
                }
                state.remove(MARK);state.remove(MARK_UNTIL);charge(p,held,action,false,700);damage(p,target,SILENCE_EXECUTION);
                FrostVisuals.burst(p.serverLevel(),target.position().add(0,1,0),24);
            }
            default -> {}
        }
    }
    public static void bladeHit(ServerPlayer p,LivingEntity target,UUID cast){
        var n=p.getPersistentData();
        if(!n.hasUUID(CAST)||!n.getUUID(CAST).equals(cast)||n.getLong(CAST_UNTIL)<now(p))return;
        var counts=n.getCompound(HITS);String id=target.getUUID().toString();int hits=counts.getInt(id)+1;
        counts.putInt(id,hits);n.put(HITS,counts);
        if(hits==4){n.putUUID(MARK,target.getUUID());n.putLong(MARK_UNTIL,now(p)+400);
            p.displayClientMessage(net.minecraft.network.chat.Component.translatable("winter.weapon.silence_mark"),true);}
    }
    private void queenAction(ServerPlayer p,ItemStack held,Action action,boolean staff){
        if(!staff){
            switch(action){
                case LEFT_CLICK -> {} // Native melee remains authoritative in spear mode.
                case SHIFT_LEFT_CLICK -> {
                    var target=aim(p,6);
                    if(target==null)return;
                    var v=target.position().subtract(p.position());
                    double travel=Math.max(0,Math.min(4,v.horizontalDistance()-1.2));
                    var dest=p.position().add(v.multiply(1,0,1).normalize().scale(travel));
                    var safe=FrostWorld.safe(p.serverLevel(),dest,.6F,1.8F,0);
                    if(safe!=null&&p.serverLevel().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),safe.add(0,1.62,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS)p.teleportTo(safe.x,safe.y,safe.z);
                    if(target.distanceToSqr(p)>25||!legal(p,target))return;
                    charge(p,held,action,false,160);damage(p,target,SPEAR_DASH);
                }
                case SHIFT_RIGHT_CLICK -> {charge(p,held,action,false,240);area(p,4.5,180,SPEAR_SWEEP);}
                default -> {}
            }
        }else{
            switch(action){
                case LEFT_CLICK -> {FrostShard.launch(p,p.getEyePosition().add(p.getLookAngle().scale(24)),STAFF_BOLT,0,null);charge(p,held,action,true,12);}
                case SHIFT_LEFT_CLICK -> {
                    boolean spawned=false;
                    Vec3 direction=p.getLookAngle().multiply(1,0,1).normalize();
                    for(int i=1;i<=5;i++){
                        Vec3 point=FrostWorld.safe(p.serverLevel(),p.position().add(direction.scale(i*1.6)),.6F,1.8F,0);
                        if(point!=null){FrostHazard.spike(p,point,8+i*3,STAFF_SPIKES,0,null);spawned=true;}
                    }
                    if(spawned)charge(p,held,action,true,300);
                }
                case SHIFT_RIGHT_CLICK -> {
                    var kind=QueenContent.Kind.LARGE_GOLEM;
                    var pos=FrostWorld.safe(p.serverLevel(),p.position().add(p.getLookAngle().multiply(1,0,1).normalize().scale(3)),kind.width,kind.height,3);
                    if(pos==null)return;
                    var golem=QueenContent.MINIONS.get(kind).get().create(p.serverLevel());
                    if(golem==null)return;golem.bindPlayer(p);golem.setPos(pos);
                    if(p.serverLevel().addFreshEntity(golem)){charge(p,held,action,true,1800);FrostVisuals.burst(p.serverLevel(),pos.add(0,1,0),25);}
                }
                default -> {}
            }
        }
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){
        super.appendHoverText(stack,context,lines,flag);
        boolean ru=java.util.Locale.getDefault().getLanguage().equals("ru");
        String description;
        if(queen){
            if(staffMode(stack))description=ru?
                "Посох: ЛКМ — ледяной снаряд (%d урона); ПКМ — смена формы; Shift+ЛКМ — шипы (%d урона); Shift+ПКМ — голем (100 HP, %d урона, 45 с; откат 90 с).".formatted(STAFF_BOLT,STAFF_SPIKES,GOLEM_DAMAGE):
                "Staff: LMB frost bolt (%d damage); RMB switch form; Shift+LMB spikes (%d damage); Shift+RMB golem (100 HP, %d damage, 45s; 90s cooldown).".formatted(STAFF_BOLT,STAFF_SPIKES,GOLEM_DAMAGE);
            else description=ru?
                "Копьё: ЛКМ — %d урона; ПКМ — смена формы; Shift+ЛКМ — рывок (%d урона); Shift+ПКМ — круговой удар (%d урона).".formatted(SPEAR_MELEE,SPEAR_DASH,SPEAR_SWEEP):
                "Spear: LMB %d damage; RMB switch form; Shift+LMB dash (%d damage); Shift+RMB circular strike (%d damage).".formatted(SPEAR_MELEE,SPEAR_DASH,SPEAR_SWEEP);
        }else description=ru?
            "ЛКМ — %d урона; ПКМ — волна (%d урона, откат 6 с); Shift+ЛКМ — 7 клинков (%d урона каждый, 20 с); Shift+ПКМ — казнь отмеченной цели (%d урона, 35 с).".formatted(SWORD_MELEE,SILENCE_WAVE,SILENCE_BLADE,SILENCE_EXECUTION):
            "LMB %d damage; RMB wave (%d damage, 6s); Shift+LMB seven blades (%d damage each, 20s); Shift+RMB marked-target execution (%d damage, 35s).".formatted(SWORD_MELEE,SILENCE_WAVE,SILENCE_BLADE,SILENCE_EXECUTION);
        lines.add(net.minecraft.network.chat.Component.literal(description));
    }
}
