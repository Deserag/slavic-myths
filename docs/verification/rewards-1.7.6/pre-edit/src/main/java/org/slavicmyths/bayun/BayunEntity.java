package org.slavicmyths.bayun;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.slavicmyths.tales.*;

/** Local loaded-only encounter. Each player has a separate clock; only one physical pounce. */
public final class BayunEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> CLIP=SynchedEntityData.defineId(BayunEntity.class,EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(BayunEntity.class,EntityDataSerializers.LONG);
    public BlockPos home;
    private enum Phase { GRACE,WARNING,SONG,CHARMED,POUNCING,SAFE,COOLDOWN }
    private static final class Session {Phase phase;long due,absent;Session(Phase p,long due){phase=p;this.due=due;}}
    private final Map<UUID,Session> sessions=new HashMap<>();
    private UUID victim;
    private long pounceStart,recoveryUntil,nextAmbient,clipEnd;
    private int soundCursor;private boolean songIntroPlayed;private final Map<String,Long> soundReady=new HashMap<>();
    public BayunEntity(EntityType<? extends BayunEntity> type,Level w){super(type,w);setPersistenceRequired();setInvulnerable(true);setCanPickUpLoot(false);getNavigation().setCanFloat(false);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(CLIP,"idle_sit");b.define(START,0L);}
    public String clip(){return entityData.get(CLIP);}
    public float age(float partial){return level().getGameTime()-entityData.get(START)+partial;}
    private long now(){return level().getGameTime();}
    public void begin(String c){entityData.set(CLIP,c);entityData.set(START,now());soundCursor=0;songIntroPlayed=false;clipEnd=now()+BayunAnimations.duration(c);}
    private void cue(String name,float volume){if(now()<soundReady.getOrDefault(name,0L))return;soundReady.put(name,now()+(name.startsWith("purr")?400:name.equals("breath")?400:5));level().playSound(null,getX(),getY(),getZ(),BayunContent.sound(name),SoundSource.NEUTRAL,volume,1);}
    private boolean eligible(ServerPlayer p){return p!=null&&p.level()==level()&&p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&!p.isPassenger()&&!p.isFallFlying()&&!p.isInWaterOrBubble()&&Math.abs(p.getY()-getY())<4&&distanceToSqr(p)<=16*16&&hasLineOfSight(p);}
    public CompoundTag bond(ServerPlayer p){var data=TaleData.get(p.serverLevel());return TaleData.compound(TaleData.compound(data.player(p.getUUID()),"Bonds"),getUUID().toString());}
    public int trust(ServerPlayer p){return Math.clamp(bond(p).getInt("Trust"),0,100);}
    private boolean safeMeeting(ServerPlayer p){var b=bond(p);return trust(p)>=85||b.getBoolean("FirstGift")&&now()-b.getLong("GiftAt")<24000;}
    private void tell(ServerPlayer p,String key){p.displayClientMessage(Component.translatable("bayun."+key),true);}
    private ServerPlayer player(UUID id){return ((ServerLevel)level()).getServer().getPlayerList().getPlayer(id);}
    private void abort(UUID id,Session s){ServerPlayer p=player(id);if(s.phase==Phase.CHARMED||s.phase==Phase.POUNCING){if(p!=null){BayunNetwork.charm(p,getId(),false);tell(p,"released");}}s.phase=Phase.COOLDOWN;s.due=now()+300;s.absent=now();if(id.equals(victim)){victim=null;getNavigation().stop();setDeltaMovement(Vec3.ZERO);recoveryUntil=now()+120;begin("recover");}}
    @Override public void tick(){super.tick();if(level().isClientSide)return;if(home==null)home=blockPosition();clearFire();setAirSupply(getMaxAirSupply());
        if(tickCount%10==0)notice();
        for(var it=sessions.entrySet().iterator();it.hasNext();){var e=it.next();var s=e.getValue();var p=player(e.getKey());
            if(!eligible(p)){if(s.phase!=Phase.COOLDOWN)abort(e.getKey(),s);if(now()-s.absent>600){it.remove();}continue;}
            if(s.phase==Phase.COOLDOWN){if(now()>=s.due&&distanceToSqr(p)<12*12){s.phase=Phase.GRACE;s.due=now()+160;tell(p,"notice");}continue;}
            if(s.phase==Phase.SAFE)continue;
            if((s.phase==Phase.SONG||s.phase==Phase.CHARMED)&&distanceToSqr(p)>225){abort(e.getKey(),s);continue;}
            if(s.phase==Phase.GRACE&&now()>=s.due){s.phase=Phase.WARNING;s.due=now()+70;tell(p,"warning");}
            else if(s.phase==Phase.WARNING&&now()>=s.due){s.phase=Phase.SONG;s.due=now()+40;tell(p,"song");}
            else if(s.phase==Phase.SONG&&now()>=s.due){s.phase=Phase.CHARMED;s.due=now()+200;BayunNetwork.charm(p,getId(),true);tell(p,"charmed");}
            else if(s.phase==Phase.CHARMED){if(now()>=s.due||!pull(p)){abort(e.getKey(),s);continue;}if(distanceToSqr(p)<=2.2*2.2&&victim==null&&now()>=recoveryUntil){victim=e.getKey();s.phase=Phase.POUNCING;pounceStart=now();getNavigation().stop();begin("pounce_kill");}}
            else if(s.phase==Phase.POUNCING&&e.getKey().equals(victim))pounce(p,s);
        }
        String danger=null;for(Session s:sessions.values()){if(s.phase==Phase.SONG||s.phase==Phase.CHARMED){danger="hypnosis_song";break;}if(s.phase==Phase.WARNING)danger="threat_warn";}
        if(victim==null&&now()>=recoveryUntil&&danger!=null&&!clip().equals(danger)){getNavigation().stop();begin(danger);}
        if(victim==null&&danger==null&&now()>=recoveryUntil){if((clip().equals("hypnosis_song")||clip().equals("threat_warn")||clip().equals("recover"))&&now()>=clipEnd){begin("idle_sit");if(position().distanceToSqr(Vec3.atBottomCenterOf(home))>9&&level().hasChunkAt(home))getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.8);}ambient();}
        animationSounds();
        if(danger!=null&&tickCount%5==0)((ServerLevel)level()).sendParticles(new net.minecraft.core.particles.DustParticleOptions(danger.equals("threat_warn")?new org.joml.Vector3f(.92F,.63F,.24F):new org.joml.Vector3f(.52F,.12F,.48F),.8F),getX(),getY()+1,getZ(),3,.5,.3,.5,.02);
    }
    private void notice(){if(sessions.size()>=32)return;for(ServerPlayer p:level().getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(12),this::eligible)){if(distanceToSqr(p)>144||sessions.containsKey(p.getUUID()))continue;boolean safe=safeMeeting(p);sessions.put(p.getUUID(),new Session(safe?Phase.SAFE:Phase.GRACE,now()+160));if(!safe){tell(p,"notice");cue("mew.soft",.35F);}TaleService.subject(p,"entity/slavicmyths:kot_bayun",2,false);org.slavicmyths.progression.Knowledge.award(p,"meet_bayun");if(clip().startsWith("sleep"))begin("wake_up");if(sessions.size()>=32)break;}}
    private boolean pull(ServerPlayer p){if(!eligible(p)||p.fallDistance>1.5||!p.onGround())return false;Vec3 direction=position().subtract(p.position()).multiply(1,0,1);if(direction.lengthSqr()<.01)return true;Vec3 delta=direction.normalize().scale(.11);AABB next=p.getBoundingBox().move(delta);BlockPos floor=BlockPos.containing(p.position().add(delta));if(!level().hasChunkAt(floor)||!level().getFluidState(floor).isEmpty()||!level().getFluidState(floor.below()).isEmpty()||!level().noCollision(p,next)||!level().getBlockCollisions(p,next.move(0,-.12,0)).iterator().hasNext())return false;
        p.setDeltaMovement(delta.x,p.getDeltaMovement().y,delta.z);p.hurtMarked=true;return true;
    }
    private void pounce(ServerPlayer p,Session s){long age=now()-pounceStart;if(!eligible(p)){abort(p.getUUID(),s);return;}getLookControl().setLookAt(p,35,18);
        if(age==8){Vec3 aim=p.position().subtract(position()).multiply(1,0,1);if(aim.lengthSqr()>.001){Vec3 velocity=aim.normalize().scale(.26);setDeltaMovement(velocity.x,.32,velocity.z);hasImpulse=true;}}
        if(age>=17&&age<=21&&distanceToSqr(p)<=1.8*1.8&&getBoundingBox().inflate(.18).intersects(p.getBoundingBox())&&hasLineOfSight(p)){
            var between=getBoundingBox().minmax(p.getBoundingBox()).inflate(.05);boolean intercepted=!level().getEntitiesOfClass(LivingEntity.class,between,e->e!=this&&e!=p&&e.isAlive()).isEmpty();
            if(!intercepted){ResourceKey<DamageType> key=ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,BayunContent.id("bayun_claws"));p.hurt(new DamageSource(level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key),this),Math.max(1000,p.getMaxHealth()+p.getAbsorptionAmount()+100));abort(p.getUUID(),s);}
        }
        if(age>=30)abort(p.getUUID(),s);
    }
    private void ambient(){if(now()<clipEnd&&!BayunAnimations.loop(clip()))return;if(clip().equals("sleep_curl_in")){begin("sleep_curl_loop");return;}if(clip().equals("wake_up")){begin("stretch_front");return;}if(!getNavigation().isDone()){if(!clip().equals("walk"))begin("walk");return;}if(clip().equals("walk"))begin("idle_sit");if(now()<nextAmbient)return;nextAmbient=now()+600+random.nextInt(1201);int roll=random.nextInt(level().isNight()?107:100);if(roll>=100){begin("sleep_curl_in");return;}if(roll<28)begin("idle_sit");else if(roll<48)begin("loaf_rest");else if(roll<72)begin("sleep_curl_in");else if(roll<82)begin("groom_paw");else if(roll<89)begin("stretch_front");else{BlockPos target=home.offset(random.nextInt(9)-4,0,random.nextInt(9)-4);if(level().hasChunkAt(target)&&level().getFluidState(target).isEmpty()&&level().noCollision(this,new AABB(target).inflate(.1))&&level().getBlockState(target.below()).isSolid()){getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,.8);begin("walk");}else begin("turn_attention");}}
    private void animationSounds(){var events=BayunAnimations.sounds(clip());float age=age(0);while(soundCursor<events.length&&events[soundCursor].tick()<=age){var e=events[soundCursor++];if(e.cue().equals("song.intro")){if(songIntroPlayed)continue;songIntroPlayed=true;}cue(e.cue(),e.volume());}if(BayunAnimations.loop(clip())&&now()>=clipEnd){entityData.set(START,now());soundCursor=0;clipEnd=now()+BayunAnimations.duration(clip());}}
    @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){if(!(p instanceof ServerPlayer player))return InteractionResult.sidedSuccess(level().isClientSide);if(distanceToSqr(p)>9||!hasLineOfSight(p)){tell(player,"far");return InteractionResult.FAIL;}ItemStack stack=p.getItemInHand(hand);boolean milk=stack.is(Items.MILK_BUCKET),fish=stack.is(Items.COD)||stack.is(Items.SALMON);if(sessions.size()>=32&&!sessions.containsKey(p.getUUID()))return InteractionResult.FAIL;var s=sessions.computeIfAbsent(p.getUUID(),id->new Session(Phase.GRACE,now()+160));var b=bond(player);
        if(!TaleData.get(player.serverLevel()).compatible())return InteractionResult.FAIL;
        if(milk||fish){if(b.getBoolean("FirstGift")&&now()-b.getLong("GiftAt")<24000){tell(player,"fed");return InteractionResult.FAIL;}if(s.phase==Phase.CHARMED||s.phase==Phase.POUNCING){tell(player,"danger");return InteractionResult.FAIL;}int trust=trust(player)+(milk?10:8)+(b.getBoolean("FirstGift")?0:15);if(!p.isCreative()){stack.shrink(1);if(milk){ItemStack bucket=new ItemStack(Items.BUCKET);if(!p.getInventory().add(bucket))p.drop(bucket,false);}}b.putBoolean("FirstGift",true);b.putLong("GiftAt",now());b.putInt("Trust",Math.min(100,trust));TaleData.get(player.serverLevel()).setDirty();s.phase=Phase.SAFE;BayunNetwork.charm(player,getId(),false);begin(milk?"eat_milk":"eat_fish");tell(player,milk?"milk":"fish");TaleService.subject(player,"entity/slavicmyths:kot_bayun",3,false);friendAward(player);return InteractionResult.CONSUME;}
        if(!stack.isEmpty()){tell(player,"invalid");cue("mew.annoyed",.35F);return InteractionResult.FAIL;}
        if(s.phase!=Phase.SAFE&&!safeMeeting(player)){tell(player,"danger");return InteractionResult.FAIL;}
        s.phase=Phase.SAFE;if(p.isSecondaryUseActive()){if(distanceToSqr(p)>6.25){tell(player,"far");return InteractionResult.FAIL;}if(b.getBoolean("Pet")&&now()-b.getLong("PetAt")<48000){tell(player,"pet_wait");return InteractionResult.FAIL;}b.putBoolean("Pet",true);b.putLong("PetAt",now());b.putInt("Trust",Math.min(100,trust(player)+4));TaleData.get(player.serverLevel()).setDirty();begin("pet_rub");tell(player,"pet");friendAward(player);}else BayunNetwork.open(player,this);return InteractionResult.CONSUME;
    }
    private void friendAward(ServerPlayer p){if(trust(p)>=85){org.slavicmyths.progression.Knowledge.award(p,"friend_of_bayun");TaleService.subject(p,"entity/slavicmyths:kot_bayun",4,false);}}
    public boolean canTalk(ServerPlayer p){var s=sessions.get(p.getUUID());return p.isAlive()&&p.level()==level()&&distanceToSqr(p)<=9&&hasLineOfSight(p)&&s!=null&&s.phase==Phase.SAFE;}
    @Override public boolean hurt(DamageSource s,float amount){return false;}
    @Override public boolean isInvulnerableTo(DamageSource s){return true;}
    @Override public boolean canBeAffected(MobEffectInstance e){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public void knockback(double strength,double x,double z){}
    @Override public void push(Entity e){}
    @Override public void push(double x,double y,double z){}
    @Override public boolean canBeLeashed(){return false;}
    @Override public boolean startRiding(Entity e,boolean force){return false;}
    @Override public boolean canUsePortal(boolean passengers){return false;}
    @Override public boolean canChangeDimensions(Level from,Level to){return false;}
    @Override public void kill(){discard();}
    @Override public void remove(Entity.RemovalReason reason){if(!level().isClientSide)for(var e:sessions.entrySet()){var p=player(e.getKey());if(p!=null)BayunNetwork.charm(p,getId(),false);}sessions.clear();super.remove(reason);}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putInt("BayunVersion",1);if(home!=null)n.putLong("Home",home.asLong());n.putLong("Recovery",Math.clamp(recoveryUntil-now(),0,120));n.putString("Clip",clip());n.putLong("AmbientRemaining",Math.clamp(nextAmbient-now(),0,1800));}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);home=n.contains("Home")?BlockPos.of(n.getLong("Home")):blockPosition();sessions.clear();victim=null;setInvulnerable(true);recoveryUntil=now()+Math.clamp(n.getLong("Recovery"),0,120);nextAmbient=now()+Math.clamp(n.getLong("AmbientRemaining"),0,1800);begin(n.getString("Clip").startsWith("sleep")?"sleep_curl_loop":"idle_sit");}
}
