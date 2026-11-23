package org.slavicmyths.auka;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.syncher.*;
import org.slavicmyths.registry.ModEffects;
import org.slavicmyths.combat.CreatureHealthBalance;

/** Auka, a voluntary two-phase winter trial. No natural spawning and no loot pending approval. */
public final class AukaBoss extends Monster {
    public enum Form { HUT_SPIRIT, STALKER, WHITE_WOLF }
    public enum Move {
        IDLE(0), STAFF(30), SWEEP(38), CHARGE(40), STOMP(38),
        FROST_BREATH(48), BEAR_CALL(54), SIDE_LUNGE(38), CLAW(34),
        PACK_CALL(52), WOLF_BITE(28), WOLF_LEAP(45), WOLF_RAM(42),
        PHASE_CHANGE(56), WOLF_RETURN(45);
        public final int length;
        Move(int length) { this.length=length; }
    }
    private static final EntityDataAccessor<Integer> FORM = SynchedEntityData.defineId(AukaBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MOVE = SynchedEntityData.defineId(AukaBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> MOVE_START = SynchedEntityData.defineId(AukaBoss.class, EntityDataSerializers.LONG);
    private final ServerBossEvent bar = new ServerBossEvent(Component.translatable("entity.slavicmyths.auka"),
        BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
    private final Set<UUID> companions = new LinkedHashSet<>();
    private UUID invited, invitationToken, challenger;
    private long invitationEnds, nextAttack, nextBears, wolfReady, wolfUntil, wolfStarted, lastSeen, nextBar;
    private int bearWaves, wolfWaves;
    private boolean battle, hitDone;

    public AukaBoss(EntityType<? extends AukaBoss> type, Level level) {
        super(type,level);
        xpReward=0;
        setPersistenceRequired();
    }
    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes()
            .add(Attributes.MAX_HEALTH,1300)
            .add(Attributes.ATTACK_DAMAGE,12)
            .add(Attributes.ARMOR,10)
            .add(Attributes.ARMOR_TOUGHNESS,4)
            .add(Attributes.MOVEMENT_SPEED,.29)
            .add(Attributes.KNOCKBACK_RESISTANCE,.6)
            .add(Attributes.FOLLOW_RANGE,44);
    }
    @Override public float getMaxHealth() {
        return (float) CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FORM,Form.HUT_SPIRIT.ordinal());
        builder.define(MOVE,Move.IDLE.ordinal());
        builder.define(MOVE_START,0L);
    }
    public Form form() { return Form.values()[entityData.get(FORM)]; }
    public Move move() { return Move.values()[entityData.get(MOVE)]; }
    public float animationTicks(float partial) { return Math.max(0,level().getGameTime()-entityData.get(MOVE_START)+partial); }
    public boolean active() { return battle; }
    public boolean wolf() { return form()==Form.WHITE_WOLF; }
    public int packElapsed() { return (int)(level().getGameTime()-wolfStarted); }
    public ServerPlayer victim() {
        return challenger==null || !(level() instanceof ServerLevel world) ? null :
            world.getServer().getPlayerList().getPlayer(challenger);
    }
    @Override public EntityDimensions getDefaultDimensions(Pose pose) {
        return switch(form()) {
            case HUT_SPIRIT -> EntityDimensions.scalable(1.45F,3.1F);
            case STALKER -> EntityDimensions.scalable(1.25F,3.55F);
            case WHITE_WOLF -> EntityDimensions.scalable(2.05F,2.25F);
        };
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if(key==FORM) refreshDimensions();
    }
    private void form(Form value) {
        entityData.set(FORM,value.ordinal());
        refreshDimensions();
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
            value==Form.HUT_SPIRIT?.29:value==Form.STALKER?.33:.36);
        getAttribute(Attributes.ARMOR).setBaseValue(value==Form.WHITE_WOLF?8:10);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(6,new WaterAvoidingRandomStrollGoal(this,.55));
        goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,8));
    }
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if(hand!=InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if(!(player instanceof ServerPlayer server)) return InteractionResult.SUCCESS;
        if(battle) {
            server.sendSystemMessage(Component.translatable("message.slavicmyths.auka.busy"));
            return InteractionResult.CONSUME;
        }
        if(server.isCreative()||server.isSpectator()||level().getDifficulty()==Difficulty.PEACEFUL) return InteractionResult.PASS;
        invited=server.getUUID();invitationToken=UUID.randomUUID();invitationEnds=level().getGameTime()+400;
        server.sendSystemMessage(Component.translatable("message.slavicmyths.auka.invite").withStyle(ChatFormatting.GOLD));
        String prefix="/slavicmyths auka_decision "+getUUID()+" "+invitationToken+" ";
        server.sendSystemMessage(Component.translatable("message.slavicmyths.auka.accept")
            .withStyle(style->style.withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,prefix+"accept")))
            .append("     ")
            .append(Component.translatable("message.slavicmyths.auka.decline")
                .withStyle(style->style.withColor(ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,prefix+"decline")))));
        return InteractionResult.CONSUME;
    }
    public boolean decide(ServerPlayer player, UUID token, boolean accept) {
        if(battle || invited==null || !invited.equals(player.getUUID()) ||
            !Objects.equals(invitationToken,token) || level().getGameTime()>invitationEnds ||
            player.level()!=level() || distanceToSqr(player)>144 || !player.isAlive()) return false;
        invited=invitationToken=null;
        if(accept) beginTrial(player);
        else player.sendSystemMessage(Component.translatable("message.slavicmyths.auka.farewell"));
        return true;
    }
    private void beginTrial(ServerPlayer player) {
        if(battle || level().getDifficulty()==Difficulty.PEACEFUL) return;
        challenger=player.getUUID();battle=true;
        bearWaves=wolfWaves=0;
        setHealth(getMaxHealth()); form(Form.HUT_SPIRIT);
        nextAttack=level().getGameTime()+35;
        nextBears=level().getGameTime()+80;
        wolfReady=wolfUntil=0;
        lastSeen=level().getGameTime();
        setTarget(player);getNavigation().stop();
        bar.setVisible(true);bar.addPlayer(player);
        playSound(SoundEvents.RAVAGER_ROAR,1,.75F);
        player.sendSystemMessage(Component.translatable("message.slavicmyths.auka.start").withStyle(ChatFormatting.RED));
    }
    @Override public boolean hurt(DamageSource source,float amount) {
        if(!level().isClientSide && !battle) {
            if(source.getEntity() instanceof ServerPlayer p && !p.isCreative() && !p.isSpectator()) beginTrial(p);
            else return false;
        }
        return super.hurt(source,amount);
    }
    private void begin(Move move) {
        entityData.set(MOVE,move.ordinal());
        entityData.set(MOVE_START,level().getGameTime());
        hitDone=false;
        getNavigation().stop();
        if(move==Move.PACK_CALL) playSound(SoundEvents.WOLF_HOWL,1.6F,.65F);
        if(move==Move.BEAR_CALL) playSound(SoundEvents.RAVAGER_ROAR,1.1F,.65F);
        nextAttack=level().getGameTime()+move.length+12;
    }
    private int elapsed() { return (int)(level().getGameTime()-entityData.get(MOVE_START)); }
    private void finishMove() { entityData.set(MOVE,Move.IDLE.ordinal());entityData.set(MOVE_START,level().getGameTime()); }
    private boolean strike(float damage,double reach,double angle,double knock) {
        ServerPlayer player=victim();
        if(player==null||!player.isAlive()||distanceToSqr(player)>reach*reach||!hasLineOfSight(player)) return false;
        Vec3 towards=player.position().subtract(position()).multiply(1,0,1).normalize();
        Vec3 facing=Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();
        if(angle>-1 && towards.dot(facing)<angle) return false;
        if(!player.hurt(damageSources().mobAttack(this),damage)) return false;
        if(knock>0)player.knockback(knock,getX()-player.getX(),getZ()-player.getZ());
        return true;
    }
    private void applyCold(boolean blood) {
        ServerPlayer player=victim();
        if(player==null || distanceToSqr(player)>36 || !hasLineOfSight(player)) return;
        if(blood) {
            player.removeEffect(ModEffects.AUKA_FROST);
            player.addEffect(new MobEffectInstance(ModEffects.AUKA_FROST_BLOOD,120),this);
        } else if(!player.hasEffect(ModEffects.AUKA_FROST_BLOOD))
            player.addEffect(new MobEffectInstance(ModEffects.AUKA_FROST,120),this);
    }
    private void actionTick() {
        Move action=move(); if(action==Move.IDLE)return;
        int tick=elapsed();ServerPlayer p=victim();
        if(p!=null) getLookControl().setLookAt(p,25,20);
        if(tick==10 && (action==Move.CHARGE||action==Move.SIDE_LUNGE||action==Move.WOLF_RAM)) {
            if(p!=null){Vec3 d=p.position().subtract(position()).multiply(1,0,1).normalize();
                setDeltaMovement(d.scale(action==Move.CHARGE?1.15:.95).add(0,getDeltaMovement().y,0));hasImpulse=true;}
        }
        if(tick==13 && action==Move.WOLF_LEAP && p!=null) {
            Vec3 d=p.position().subtract(position()).multiply(1,0,1).normalize();
            setDeltaMovement(d.scale(.95).add(0,.48,0));hasImpulse=true;
        }
        if(!hitDone) switch(action) {
            case STAFF -> {if(tick>=14){strike(form()==Form.HUT_SPIRIT?10:12,3.4,.2,.35);hitDone=true;}}
            case SWEEP -> {if(tick>=20){strike(14,4.4,-.3,.7);hitDone=true;}}
            case CHARGE -> {if(tick>=12&&tick<=25&&strike(16,2.7,-1,1)){hitDone=true;}}
            case STOMP -> {if(tick>=18){strike(10,4.0,-1,.65);hitDone=true;}}
            case FROST_BREATH -> {if(tick>=21){if(strike(2,5.8,.3,0))applyCold(false);hitDone=true;}}
            case SIDE_LUNGE -> {if(tick>=16){strike(14,3.8,.1,.4);hitDone=true;}}
            case CLAW -> {if(tick>=15){strike(12,3.3,.1,.5);hitDone=true;}}
            case WOLF_BITE -> {if(tick>=13){if(strike(10,3.1,.15,.25))applyCold(true);hitDone=true;}}
            case WOLF_LEAP -> {if(tick>=24){if(strike(14,3.3,-1,.65))applyCold(true);hitDone=true;}}
            case WOLF_RAM -> {if(tick>=15&&tick<=27&&strike(12,3.0,-1,.8)){hitDone=true;}}
            case BEAR_CALL -> {if(tick>=25){summon(AukaMinion.Kind.BEAR);bearWaves++;nextBears=level().getGameTime()+380;hitDone=true;}}
            case PACK_CALL -> {if(tick>=27){summon(AukaMinion.Kind.WOLF);hitDone=true;}}
            default -> {}
        }
        if(tick>=action.length) finishMove();
    }
    private int count(AukaMinion.Kind kind) {
        if(!(level() instanceof ServerLevel w))return 0;
        companions.removeIf(id->!(w.getEntity(id) instanceof AukaMinion m) || !m.isAlive());
        int count=0;
        for(UUID id:companions) if(w.getEntity(id) instanceof AukaMinion m && m.kind()==kind) count++;
        return count;
    }
    private void summon(AukaMinion.Kind kind) {
        if(!(level() instanceof ServerLevel world))return;
        int available=Math.max(0,2-count(kind));ServerPlayer p=victim();
        if(p==null)return;
        for(int i=0;i<available;i++) {
            double a=getYRot()*Math.PI/180+(i==0?-1.15:1.15);
            Vec3 at=position().add(Math.cos(a)*3.4,0,Math.sin(a)*3.4);
            BlockPos pos=BlockPos.containing(at);
            while(world.getBlockState(pos).isAir() && pos.getY()>world.getMinBuildHeight()+1 && pos.getY()>blockPosition().getY()-2)pos=pos.below();
            if(world.getBlockState(pos).isSolid())pos=pos.above();
            AukaMinion mob=(kind==AukaMinion.Kind.BEAR?AukaContent.AUKA_BEAR:AukaContent.AUKA_WOLF).get().create(world);
            if(mob==null)continue;
            mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,getYRot(),0);
            if(!world.noCollision(mob))continue;
            mob.bind(getUUID(),i);
            if(world.addFreshEntity(mob))companions.add(mob.getUUID());
        }
    }
    private void dismiss() {
        if(level() instanceof ServerLevel w)for(UUID id:companions)if(w.getEntity(id) instanceof AukaMinion m)m.discard();
        companions.clear();
    }
    private void secondPhase() {
        dismiss();form(Form.STALKER);wolfReady=level().getGameTime()+95;
        begin(Move.PHASE_CHANGE);
        playSound(SoundEvents.WOLF_GROWL,1.2F,.65F);
    }
    private void transformWolf() {
        form(Form.WHITE_WOLF);
        wolfWaves++;
        wolfStarted=level().getGameTime();wolfUntil=wolfStarted+440;
        begin(Move.PACK_CALL);
    }
    private void returnFromWolf() {
        dismiss();form(Form.STALKER);
        wolfReady=level().getGameTime()+600;
        begin(Move.WOLF_RETURN);
        playSound(SoundEvents.WOLF_WHINE,1,.7F);
    }
    private void abort() {
        dismiss();bar.removeAllPlayers();bar.setVisible(false);
        battle=false;challenger=invited=invitationToken=null;
        setTarget(null);getNavigation().stop();form(Form.HUT_SPIRIT);
        finishMove();setHealth(getMaxHealth());
    }
    @Override public void tick() {
        super.tick();
        if(!(level() instanceof ServerLevel w))return;
        long now=w.getGameTime();
        if(!battle)return;
        if(w.getDifficulty()==Difficulty.PEACEFUL){abort();return;}
        ServerPlayer p=victim();
        if(p==null||!p.isAlive()||p.isSpectator()||p.level()!=level()||distanceToSqr(p)>48*48) {
            if(now-lastSeen>160)abort();
            return;
        }
        lastSeen=now;
        if(now>=nextBar) {
            nextBar=now+20;
            bar.setProgress(Math.clamp(getHealth()/getMaxHealth(),0,1));
            List<ServerPlayer> watchers=w.getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(48),
                player->player.isAlive() && !player.isSpectator());
            for(ServerPlayer other:List.copyOf(bar.getPlayers()))if(!watchers.contains(other))bar.removePlayer(other);
            for(ServerPlayer other:watchers)bar.addPlayer(other);
        }
        setTarget(p);
        if(form()==Form.HUT_SPIRIT && getHealth()<=getMaxHealth()*.55F){secondPhase();return;}
        if(wolf() && now>=wolfUntil){returnFromWolf();return;}
        if(move()!=Move.IDLE){actionTick();return;}
        if(now<nextAttack)return;
        double distance=distanceTo(p);
        if(form()==Form.HUT_SPIRIT) {
            if(bearWaves<2 && now>=nextBears && count(AukaMinion.Kind.BEAR)<1){begin(Move.BEAR_CALL);return;}
            if(distance>4.5 && distance<16){begin(Move.CHARGE);return;}
            if(distance<3.6) {begin(random.nextInt(4)==0?Move.STOMP:random.nextBoolean()?Move.STAFF:Move.SWEEP);return;}
            if(distance<=7){begin(Move.FROST_BREATH);return;}
            getNavigation().moveTo(p,1.14);
        } else if(form()==Form.STALKER) {
            if(wolfWaves<2&&now>=wolfReady){transformWolf();return;}
            if(distance<3.8){begin(random.nextBoolean()?Move.CLAW:Move.STAFF);return;}
            if(distance<=12){begin(Move.SIDE_LUNGE);return;}
            getNavigation().moveTo(p,1.12);
        } else {
            // Coordinated pack rhythm: flank, signal, strike, disengage.
            int cycle=(int)((now-wolfStarted)%180);
            if(cycle<44){getNavigation().moveTo(p.getX()+Math.sin(now*.03)*5,p.getY(),p.getZ()+Math.cos(now*.03)*5,1.0);nextAttack=now+8;}
            else if(cycle<75){getNavigation().moveTo(p, .9);nextAttack=now+8;}
            else if(cycle<119){begin(cycle<95?Move.WOLF_LEAP:Move.WOLF_BITE);}
            else if(cycle<155){Vec3 d=position().subtract(p.position()).multiply(1,0,1).normalize();getNavigation().moveTo(getX()+d.x*7,getY(),getZ()+d.z*7,1.12);nextAttack=now+10;}
            else {begin(distance<4?Move.WOLF_BITE:Move.WOLF_RAM);}
        }
    }
    @Override public void die(DamageSource source) {
        dismiss();bar.removeAllPlayers();bar.setVisible(false);
        super.die(source);
    }
    @Override public void onRemovedFromLevel() {
        if(!level().isClientSide){dismiss();bar.removeAllPlayers();}
        super.onRemovedFromLevel();
    }
    @Override public boolean removeWhenFarAway(double distance) {return false;}
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("AukaBattle",battle);
        tag.putInt("AukaForm",form().ordinal());
        if(challenger!=null)tag.putUUID("AukaChallenger",challenger);
        tag.putInt("AukaBears",bearWaves);tag.putInt("AukaWolves",wolfWaves);
        tag.putInt("AukaWolfRemaining",(int)Math.max(0,wolfUntil-level().getGameTime()));
        tag.putInt("AukaWolfReady",(int)Math.max(0,wolfReady-level().getGameTime()));
        tag.putInt("AukaBearReady",(int)Math.max(0,nextBears-level().getGameTime()));
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        battle=tag.getBoolean("AukaBattle")&&tag.hasUUID("AukaChallenger");
        challenger=battle?tag.getUUID("AukaChallenger"):null;
        invited=invitationToken=null;companions.clear();
        bearWaves=Math.clamp(tag.getInt("AukaBears"),0,2);
        wolfWaves=Math.clamp(tag.getInt("AukaWolves"),0,2);
        long now=level().getGameTime();
        wolfUntil=now+Math.max(0,tag.getInt("AukaWolfRemaining"));
        wolfStarted=wolfUntil-440;
        wolfReady=now+Math.max(0,tag.getInt("AukaWolfReady"));
        nextBears=now+Math.max(0,tag.getInt("AukaBearReady"));
        nextAttack=now+40;lastSeen=now;
        int saved=Math.clamp(tag.getInt("AukaForm"),0,2);
        form(battle?Form.values()[saved]:Form.HUT_SPIRIT);
        finishMove();bar.setVisible(battle);
    }
}
