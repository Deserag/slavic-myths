package org.slavicmyths.ninemor;

import java.util.Arrays;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.slavicmyths.gorynych.GorynychContent;

/**
 * Summoned nine-headed plague serpent.
 * Raw damage is scaled against vanilla's 1024 max-health attribute ceiling:
 * the effective unarmored boss health is 3000, shown by the boss bar.
 * Independently scheduled heads never exceed three simultaneous attacks.
 */
public final class NineMorSerpent extends Monster {
    public static final float EFFECTIVE_MAX_HEALTH = 3000F;
    private static final float INTERNAL_MAX_HEALTH = 1024F;
    public static final int GRAB = 1, PROJECTILE = 2, BEAM = 3, AREA = 4;
    public static final int[] COLORS = {
        0xD95B62, 0x9DD45D, 0xAC7AD2, 0x6DC7E6, 0xE8974A,
        0xE2D16B, 0x62B79C, 0xC39761, 0x7B91DB
    };

    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Integer>[] HEADS = new EntityDataAccessor[9];
    static {
        for (int i = 0; i < HEADS.length; i++) HEADS[i] =
            SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.INT);
    }
    @SuppressWarnings("unchecked") private static final EntityDataAccessor<Integer>[] HEAD_TARGETS=new EntityDataAccessor[9];
    @SuppressWarnings("unchecked") private static final EntityDataAccessor<Float>[] HEAD_YAWS=new EntityDataAccessor[9],HEAD_PITCHES=new EntityDataAccessor[9];
    static {for(int i=0;i<9;i++){HEAD_TARGETS[i]=SynchedEntityData.defineId(NineMorSerpent.class,EntityDataSerializers.INT);HEAD_YAWS[i]=SynchedEntityData.defineId(NineMorSerpent.class,EntityDataSerializers.FLOAT);HEAD_PITCHES[i]=SynchedEntityData.defineId(NineMorSerpent.class,EntityDataSerializers.FLOAT);}}
    private final java.util.UUID[] headVictims=new java.util.UUID[9];
    private final Vec3[] headMarks=new Vec3[9];
    private double takeoffY;    private static final EntityDataAccessor<Integer> FLIGHT =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLIGHT_AGE =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RITUAL_AGE =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TAIL_MASK =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AIM_YAW =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_PITCH =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HAS_GAZE =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bar = new org.slavicmyths.combat.CreatureBossBar(this,
        Component.literal("Моровой Змей — Насылатель Мора"),
        BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_20);
    private final int[] cooldown = new int[9];
    private final int[] tailCooldown = {60, 95, 130};
    private final int[] tailSwingAge = new int[3];
    private int nextHead;
    private int nextFlight = 450;
    private boolean ritual = true;

    public NineMorSerpent(EntityType<? extends NineMorSerpent> type, Level level) {
        super(type, level);
        xpReward = 150;
        setPersistenceRequired();
        Arrays.fill(cooldown, 20);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, INTERNAL_MAX_HEALTH)
            .add(Attributes.ARMOR, 10).add(Attributes.ARMOR_TOUGHNESS, 6)
            .add(Attributes.MOVEMENT_SPEED, .25).add(Attributes.FLYING_SPEED, .32)
            .add(Attributes.FOLLOW_RANGE, 64).add(Attributes.ATTACK_DAMAGE, 12)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        for (EntityDataAccessor<Integer> head : HEADS) builder.define(head, 0);
        for(int i=0;i<9;i++){builder.define(HEAD_TARGETS[i],-1);builder.define(HEAD_YAWS[i],0F);builder.define(HEAD_PITCHES[i],0F);}
        builder.define(FLIGHT, 0);
        builder.define(FLIGHT_AGE, 0);
        builder.define(RITUAL_AGE, 0);
        builder.define(TAIL_MASK, 0);
        builder.define(AIM_YAW, 0F);
        builder.define(AIM_PITCH, 0F);
        builder.define(HAS_GAZE, false);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 40F));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
            candidate -> candidate instanceof Player player && !player.isCreative() && !player.isSpectator()));
    }

    public int headMode(int head) { return entityData.get(HEADS[head]) & 15; }
    public int headAge(int head) { return entityData.get(HEADS[head]) >>> 4; }
    public int headTargetId(int head){return entityData.get(HEAD_TARGETS[head]);}
    public LivingEntity headTarget(int head){
        var entity=level() instanceof ServerLevel w&&headVictims[head]!=null?w.getEntity(headVictims[head]):level().getEntity(headTargetId(head));
        return entity instanceof LivingEntity living?living:null;
    }
    public float headYaw(int head){return headMode(head)==0?aimYaw()*.45F:entityData.get(HEAD_YAWS[head]);}
    public float headPitch(int head){return headMode(head)==0?aimPitch()*.45F:entityData.get(HEAD_PITCHES[head]);}
    private boolean validVictim(LivingEntity e){return e!=null&&e!=this&&!(e instanceof NineMorSerpent)&&e.isAlive()&&!e.isRemoved()&&e.level()==level()&&e.distanceToSqr(this)<=64*64&&org.slavicmyths.combat.CombatTargeting.canAttack(this,e,org.slavicmyths.combat.CombatTargeting.Context.DIRECT);}    public int flightPhase() { return entityData.get(FLIGHT); }
    public int flightAge() { return entityData.get(FLIGHT_AGE); }
    public int ritualAge() { return entityData.get(RITUAL_AGE); }
    public boolean isRitual() { return ritualAge() < 60; }
    public int tailMask() { return entityData.get(TAIL_MASK); }
    public boolean hasGaze() { return entityData.get(HAS_GAZE); }
    public float aimYaw() { return entityData.get(AIM_YAW); }
    public float aimPitch() { return entityData.get(AIM_PITCH); }
    @Override public Component getName() {
        return Component.literal("Моровой Змей — Насылатель Мора");
    }

    public void beginRitual() {
        entityData.set(RITUAL_AGE, 0);
        ritual = true;
        setHealth(getMaxHealth());
        setDeltaMovement(Vec3.ZERO);
    }

    @Override public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bar.addPlayer(player);
    }

    @Override public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bar.removePlayer(player);
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        // Independent attacks are not cancelled when the boss takes a hit.
        if (ritual || isDeadOrDying()) return false;
        return super.hurt(source, amount * (INTERNAL_MAX_HEALTH / EFFECTIVE_MAX_HEALTH));
    }

    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        bar.setProgress(Mth.clamp(getHealth() / INTERNAL_MAX_HEALTH, 0F, 1F));

        if (isDeadOrDying()) { setNoGravity(false); return; }
        if (ritual) {
            int age = ritualAge() + 1;
            entityData.set(RITUAL_AGE, age);
            getNavigation().stop();
            setDeltaMovement(Vec3.ZERO);
            if ((age & 3) == 0) ritualParticles(server, age);
            if (age == 42) sound("roar_ground", 1.6F);
            if (age >= 60) ritual = false;
            return;
        }

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()
                || target instanceof Player player && (player.isCreative() || player.isSpectator())
                || target.distanceToSqr(this) > 80 * 80) target = null;
        trackNearbyPlayer(server, target);
        tickFlight(target);
        tickHeads(target);
        tickTails(target);

        // Move deliberately toward the opponent instead of random strolling,
        // and stop before the oversized body collides with the player.
        if (flightPhase() == 0 && target != null && tickCount % 12 == 0) {
            if (distanceToSqr(target) > 144D) getNavigation().moveTo(target, .87D);
            else getNavigation().stop();
        }
        if (tickCount % 23 == 0 && getDeltaMovement().horizontalDistanceSqr() > .009D
                && onGround()) sound("step_heavy", .7F);
        if (tickCount % 110 == 0 && random.nextFloat() < .7F) sound("ambient_low", .58F);
        if (tickCount % 70 == 0) ambientMor(server);
    }

    private void trackNearbyPlayer(ServerLevel server, LivingEntity enemy) {
        // Creative players can inspect the creature's tracking without being attacked.
        LivingEntity focus = enemy;
        if (focus == null) {
            double nearest = 56D * 56D;
            for (ServerPlayer observer : server.players()) {
                double dist = observer.distanceToSqr(this);
                if (observer.isAlive() && !observer.isSpectator() && dist < nearest) {
                    nearest = dist;
                    focus = observer;
                }
            }
        }
        if (focus == null || focus.distanceToSqr(this) > 56D * 56D) {
            entityData.set(HAS_GAZE, false);
            return;
        }
        getLookControl().setLookAt(focus, 40F, 40F);
        double dx = focus.getX() - getX(), dz = focus.getZ() - getZ();
        float worldYaw = (float)(Math.atan2(dz, dx) * 180D / Math.PI) - 90F;
        float nextYaw = Mth.approachDegrees(getYRot(), worldYaw, 1.8F);
        setYRot(nextYaw);
        setYBodyRot(nextYaw);
        float localYaw = Mth.wrapDegrees(worldYaw - nextYaw);
        double distance = Math.sqrt(dx * dx + dz * dz);
        float pitch = (float)(Math.atan2((getY() + 9.5D) - focus.getEyeY(), Math.max(2D, distance)) * 180D / Math.PI);
        entityData.set(AIM_YAW, Mth.clamp(localYaw, -68F, 68F));
        entityData.set(AIM_PITCH, Mth.clamp(pitch, -32F, 60F));
        entityData.set(HAS_GAZE, true);
    }

    private void tickFlight(LivingEntity target) {
        int phase=flightPhase(),age=flightAge()+1;
        if(phase==0){
            setNoGravity(false);
            if(target!=null&&onGround()&&--nextFlight<=0){takeoffY=getY();flight(1);getNavigation().stop();sound("wings_unfold",1.3F);}
            return;
        }
        entityData.set(FLIGHT_AGE,age);getNavigation().stop();
        if(target==null&&phase<3){flight(3);phase=3;}
        if(phase==1){
            setNoGravity(true);
            Vec3 motion=new Vec3(0,getY()<takeoffY+2.2?.12:0,0);
            setDeltaMovement(safeFlightMotion(motion));
            if(age>=18||getDeltaMovement().lengthSqr()<.00001){flight(2);sound("wings_flap",1);}
        }else if(phase==2){
            setNoGravity(true);
            Vec3 offset=target!=null?target.position().subtract(position()):Vec3.ZERO;
            var motion=new Vec3(Mth.clamp(offset.x*.008,-.08,.08),Mth.clamp((takeoffY+1.8-getY())*.05,-.06,.04),Mth.clamp(offset.z*.008,-.08,.08));
            setDeltaMovement(safeFlightMotion(motion));
            if(age%20==0)sound("wings_flap",.8F);
            if(age>=40){flight(3);setNoGravity(false);}
        }else{
            setNoGravity(false);
            setDeltaMovement(getDeltaMovement().multiply(.5,1,.5));
            if(onGround()){
                flight(0);nextFlight=420+random.nextInt(180);
                setDeltaMovement(Vec3.ZERO);sound("land",1.2F);
            }
        }
    }
    private Vec3 safeFlightMotion(Vec3 motion){
        var box=getBoundingBox().expandTowards(motion);
        if(!level().hasChunksAt(net.minecraft.core.BlockPos.containing(box.minX,box.minY,box.minZ),net.minecraft.core.BlockPos.containing(box.maxX,box.maxY,box.maxZ))
            ||!level().getWorldBorder().isWithinBounds(box)||!level().noCollision(this,box))return Vec3.ZERO;
        return motion;
    }
    private void flight(int phase) {
        entityData.set(FLIGHT, phase);
        entityData.set(FLIGHT_AGE, 0);
    }

    private void clearHead(int head,int rest){entityData.set(HEADS[head],0);entityData.set(HEAD_TARGETS[head],-1);headVictims[head]=null;headMarks[head]=null;cooldown[head]=rest;}
    private void aimHead(int head,LivingEntity target){
        Vec3 delta=target.getEyePosition().subtract(headMouth(head));
        float yaw=Mth.wrapDegrees((float)Math.toDegrees(Math.atan2(delta.z,delta.x))-90-getYRot());
        float pitch=(float)-Math.toDegrees(Math.atan2(delta.y,delta.horizontalDistance()));
        entityData.set(HEAD_YAWS[head],Mth.approachDegrees(entityData.get(HEAD_YAWS[head]),Mth.clamp(yaw,-120,120),12));
        entityData.set(HEAD_PITCHES[head],Mth.approachDegrees(entityData.get(HEAD_PITCHES[head]),Mth.clamp(pitch,-35,65),8));
    }
    private LivingEntity chooseHeadVictim(int head,LivingEntity primary){
        var options=level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(64),p->validVictim(p)&&hasLineOfSight(p));
        java.util.List<LivingEntity> candidates=new java.util.ArrayList<>(options);
        if(validVictim(primary)&&hasLineOfSight(primary)&&!candidates.contains(primary))candidates.add(primary);
        LivingEntity best=null;double score=Double.MAX_VALUE;
        for(var p:candidates){
            int assigned=0;for(int i=0;i<9;i++)if(headMode(i)!=0&&p.getUUID().equals(headVictims[i]))assigned++;
            double s=assigned*100000+headMouth(head).distanceToSqr(p.getEyePosition());
            if(s<score){score=s;best=p;}
        }
        return best;
    }
    private void tickHeads(LivingEntity primary) {
        int active=0;
        for(int i=0;i<9;i++){
            if(cooldown[i]>0)cooldown[i]--;
            int mode=headMode(i),age=headAge(i);if(mode==0)continue;
            LivingEntity victim=headTarget(i);
            if(!validVictim(victim)||!hasLineOfSight(victim)){clearHead(i,25);continue;}
            active++;age++;entityData.set(HEADS[i],(age<<4)|mode);aimHead(i,victim);
            if(age<=24&&age%6==0){
                Vec3 from=headMouth(i),direction=victim.getEyePosition().subtract(from).normalize();
                for(int k=0;k<4;k++)coloredParticle((ServerLevel)level(),i,from.add(direction.scale(k*.65)),2);
                circle((ServerLevel)level(),i,victim.position(),1.1,12);
            }
            if(age==17&&mode==AREA){headMarks[i]=victim.position();circle((ServerLevel)level(),i,headMarks[i],5.5,36);}
            if(age==17)sound(mode==GRAB?"head_predator_grab":mode==AREA?"ground_spikes_warn":mode==BEAM?"head_fire_breath":"head_mor_shot",.9F);
            if(age==25)executeHead(i,mode,victim);
            if(age>=38){clearHead(i,mode==AREA?185+random.nextInt(75):82+random.nextInt(65));active--;}
        }
        int start=nextHead;
        for(int attempt=0;attempt<9&&active<3;attempt++){
            int index=(start+attempt)%9;
            if(cooldown[index]>0||headMode(index)!=0)continue;
            LivingEntity victim=chooseHeadVictim(index,primary);if(victim==null)continue;
            int mode=(index==0||index==7)?GRAB:(index==2||index==5||index==8)&&random.nextInt(5)==0?AREA:(index==2||index==5||index==8)?BEAM:PROJECTILE;
            if(mode==GRAB&&distanceToSqr(victim)>100)continue;
            headVictims[index]=victim.getUUID();headMarks[index]=null;entityData.set(HEAD_TARGETS[index],victim.getId());
            entityData.set(HEADS[index],(1<<4)|mode);aimHead(index,victim);active++;nextHead=(index+1)%9;
        }
    }
    private void executeHead(int head, int mode, LivingEntity target) {
        if (!validVictim(target)||!hasLineOfSight(target)) return;
        if (mode == GRAB) {
            if (distanceToSqr(target) <= 100D && hasLineOfSight(target)) {
                if (target.hurt(damageSources().mobAttack(this), 12F)) {
                    target.knockback(1.4D, getX() - target.getX(), getZ() - target.getZ());
                    applyElement(target, head);
                }
                sound("head_predator_bite", 1.1F);
            }
            return;
        }
        if (mode == AREA) {
            Vec3 center = headMarks[head]!=null?headMarks[head]:target.position();
            circle((ServerLevel) level(), head, center, 6.5D, 42);
            areaDamage(center, 5.5D, head, 10F);
            sound("ground_spikes_impact", 1.1F);
        } else if (mode == BEAM) {
            Vec3 origin = headMouth(head), end = target.getEyePosition();
            end=level().clip(new net.minecraft.world.level.ClipContext(origin,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getLocation();
            Vec3 delta = end.subtract(origin);
            if (delta.lengthSqr() < .1D) return;
            Vec3 direction = delta.normalize();
            Vec3 stop = origin.add(direction.scale(Math.min(42, delta.length())));
            int steps = (int)(origin.distanceTo(stop) * 3D);
            for (int n = 0; n <= steps; n += 2) {
                Vec3 point = origin.add(direction.scale(n / 3D));
                coloredParticle((ServerLevel)level(), head, point, 2);
            }
            for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class,
                    new AABB(origin, stop).inflate(2D), candidate ->
                    validVictim(candidate))) {
                Vec3 p = victim.getBoundingBox().getCenter();
                double progress = Mth.clamp(p.subtract(origin).dot(direction), 0D, origin.distanceTo(stop));
                if (p.distanceToSqr(origin.add(direction.scale(progress))) < 2.6D) {
                    if (victim.hurt(damageSources().mobAttack(this), 9F)) applyElement(victim, head);
                }
            }
        } else {
            NineMorShot shot = new NineMorShot(level(), this, head);
            Vec3 origin = headMouth(head);
            Vec3 direction = target.getEyePosition().subtract(origin).normalize();
            shot.setPos(origin.x, origin.y, origin.z);
            shot.shoot(direction.x, direction.y, direction.z, 1.32F, 1.0F);
            level().addFreshEntity(shot);
            coloredParticle((ServerLevel)level(), head, origin, 14);
            if (head == 4) sound("head_fireball", 1F);
        }
    }

    public Vec3 headMouth(int head) { return NineMorRig.mouth(this,head); }

    private void tickTails(LivingEntity target) {
        int mask = 0;
        for (int i = 0; i < 3; i++) {
            if (tailCooldown[i] > 0) tailCooldown[i]--;
            if (tailSwingAge[i] > 0) {
                tailSwingAge[i]--;
                mask |= 1 << i;
            }
            if (target != null && tailCooldown[i] <= 0 && distanceToSqr(target) < 75D) {
                tailCooldown[i] = 95 + i * 17 + random.nextInt(30);
                tailSwingAge[i] = 22;
                if (target.hurt(damageSources().mobAttack(this), 7.0F))
                    target.knockback(.8D, getX() - target.getX(), getZ() - target.getZ());
                sound("tail_sweep", .8F);
                mask |= 1 << i;
            }
        }
        entityData.set(TAIL_MASK, mask);
    }

    private void ambientMor(ServerLevel server) {
        for (LivingEntity living : server.getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(7D), this::validVictim)) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 90, 0));
        }
        coloredParticle(server, 2, position().add(0, 2D, 0), 26);
    }

    private void areaDamage(Vec3 center, double radius, int head, float damage) {
        AABB area = new AABB(center, center).inflate(radius, 3D, radius);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, area,
                candidate -> validVictim(candidate))) {
            if (e.distanceToSqr(center) <= radius * radius
                    && e.hurt(damageSources().mobAttack(this), damage)) applyElement(e, head);
        }
    }

    public static void applyElement(LivingEntity victim, int head) {
        switch (head) {
            case 1, 6 -> victim.addEffect(new MobEffectInstance(MobEffects.POISON, 90, 0));
            case 2 -> victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 75, 0));
            case 3 -> victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 85, 1));
            case 4 -> victim.igniteForSeconds(3);
            case 5 -> victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 85, 1));
            case 7 -> victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, 155, 1));
            case 8 -> victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 65, 0));
            default -> victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, 0));
        }
    }

    private void circle(ServerLevel server, int head, Vec3 center, double radius, int count) {
        for (int j = 0; j < count; j++) {
            double a = j * 2D * Math.PI / count;
            coloredParticle(server, head, center.add(Math.cos(a) * radius, .15D, Math.sin(a) * radius), 2);
        }
    }

    private void ritualParticles(ServerLevel server, int age) {
        for (int i = 0; i < 9; i++) {
            double theta = (i / 9D) * 2D * Math.PI + age * .026D;
            double radius = 4D * (1D - Math.min(1D, age / 65D) * .42D);
            coloredParticle(server, i, position().add(Math.cos(theta) * radius,
                .22D + age * .036D, Math.sin(theta) * radius), 7);
        }
    }

    private void coloredParticle(ServerLevel server, int head, Vec3 at, int count) {
        int color = COLORS[head];
        server.sendParticles(new DustParticleOptions(
            new Vector3f(((color >> 16) & 255) / 255F,
                ((color >> 8) & 255) / 255F, (color & 255) / 255F), 1.2F),
            at.x, at.y, at.z, count, .20, .21, .20, .02);
    }

    private void sound(String sound, float volume) {
        var key = GorynychContent.SOUNDS.get(sound);
        if (key != null) level().playSound(null, blockPosition(),
            key.get(), SoundSource.HOSTILE, volume, .8F + random.nextFloat() * .22F);
    }

    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return GorynychContent.SOUNDS.get("head_predator_grab").get();
    }

    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return GorynychContent.SOUNDS.get("purified_roar").get();
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("RitualAge", ritualAge());
        tag.putBoolean("Ritual", ritual);
        tag.putInt("NextFlight", nextFlight);
        tag.putInt("NextHead", nextHead);
        for (int i = 0; i < 9; i++) {
            tag.putInt("Head"+i, entityData.get(HEADS[i]));
            tag.putInt("HeadCooldown"+i, cooldown[i]);
        }
        for (int i = 0; i < 3; i++) tag.putInt("TailCooldown"+i, tailCooldown[i]);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(RITUAL_AGE, tag.getInt("RitualAge"));
        ritual = tag.getBoolean("Ritual");
        nextFlight = Math.max(100, tag.getInt("NextFlight"));
        nextHead = Math.floorMod(tag.getInt("NextHead"), 9);
        for (int i = 0; i < 9; i++) {
            clearHead(i,0);
            cooldown[i] = Math.max(5, tag.getInt("HeadCooldown"+i));
        }
        for (int i = 0; i < 3; i++) tailCooldown[i] = Math.max(25, tag.getInt("TailCooldown"+i));
        flight(0);
        setNoGravity(false);
    }
}
