package org.slavicmyths.wolf;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundSource;
import org.slavicmyths.registry.ModEntities;
import static org.slavicmyths.wolf.WolfTimeline.*;
/** One physical immortal actor; only a consenting participant owns its current trial. */
public class GreyWolf extends PathfinderMob {
 private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(GreyWolf.class,EntityDataSerializers.INT),MOVE=SynchedEntityData.defineId(GreyWolf.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Long> CLOCK=SynchedEntityData.defineId(GreyWolf.class,EntityDataSerializers.LONG);
 private final ServerBossEvent bar=new ServerBossEvent(Component.translatable("wolf.slavicmyths.bar"),BossEvent.BossBarColor.WHITE,BossEvent.BossBarOverlay.PROGRESS);
 private UUID encounter=UUID.randomUUID(),participant,pending,token;private BlockPos home;private boolean completedBefore,debug;
 private long trialStart,nextAttack,howlReady,dashReady,outsideSince=-1,lastAmbient;private int summons,stuckTicks;private Vec3 direction=Vec3.ZERO,lastMotion=Vec3.ZERO;
 private final List<UUID> pack=new ArrayList<>();private final Map<UUID,Long> packExpiry=new HashMap<>();private List<BlockPos> packSpots=List.of();
 public GreyWolf(EntityType<? extends GreyWolf> type,Level level){super(type,level);setPersistenceRequired();bar.setVisible(false);}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,500).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,13).add(Attributes.FOLLOW_RANGE,40).add(Attributes.KNOCKBACK_RESISTANCE,.85).add(Attributes.STEP_HEIGHT,1);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STATE,0);b.define(MOVE,0);b.define(CLOCK,0L);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new TrialGoal());goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,10));goalSelector.addGoal(7,new RandomLookAroundGoal(this));}
 public UUID encounterId(){return encounter;}public BlockPos home(){return home==null?blockPosition():home;}public UUID participant(){return participant;}public boolean debug(){return debug;}public void debug(boolean value){debug=value;}
 public State state(){return State.values()[entityData.get(STATE)];}public Move move(){return Move.values()[entityData.get(MOVE)];}public float animationTicks(float partial){return Math.max(0,level().getGameTime()-entityData.get(CLOCK)+partial);}
 private int elapsed(){return (int)animationTicks(0);}private void state(State state){entityData.set(STATE,state.ordinal());entityData.set(MOVE,0);entityData.set(CLOCK,level().getGameTime());getNavigation().stop();}
 private void move(Move move){entityData.set(MOVE,move.ordinal());entityData.set(CLOCK,level().getGameTime());stopWalking();}
 public boolean active(){return state()==State.TRIAL_ACTIVE||state()==State.TRIAL_SUMMONING;}
 public boolean busy(){return active()||state()==State.TRIAL_PREPARE||state()==State.TRIAL_COMPLETE_CINEMATIC;}
 private ServerPlayer player(){return participant==null?null:(ServerPlayer)((ServerLevel)level()).getPlayerByUUID(participant);}
 public void voice(String name,float volume){if(level() instanceof ServerLevel w)w.playSound(null,getX(),getY()+2.6,getZ(),WolfSounds.get(name),SoundSource.HOSTILE,volume,1);}
 public static void say(ServerPlayer p,String key){p.sendSystemMessage(Component.translatable("entity.slavicmyths.grey_wolf").withStyle(ChatFormatting.GOLD).append(Component.literal(": ").withStyle(ChatFormatting.GRAY)).append(Component.translatable("wolf.slavicmyths."+key).withStyle(ChatFormatting.WHITE)));}
 public void greet(ServerPlayer p){
  if(p.level()!=level()||distanceToSqr(p)>100||!hasLineOfSight(p)||p.isSpectator())return;
  if(busy()||pending!=null&&!pending.equals(p.getUUID())){say(p,"busy");return;}
  if(WolfProgress.passed(p)){if(state()==State.POST_TRIAL_NPC)WolfStoryNetwork.open(p,this);else say(p,"return");return;}
  WolfProgress.data(p).putBoolean("wolf_met",true);pending=p.getUUID();token=UUID.randomUUID();state(State.GREETING);say(p,"greeting");
 }
 private void choices(ServerPlayer p){var row=Component.empty();for(String action:List.of("accept","decline")){
  var button=Component.translatable("wolf.slavicmyths."+action).withStyle(action.equals("accept")?ChatFormatting.GREEN:ChatFormatting.GOLD).withStyle(s->s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/slavicmyths wolf_decision "+encounter+" "+token+" "+action)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Component.translatable("wolf.slavicmyths."+action+"_hover"))));row.append(button).append("   ");
 }p.sendSystemMessage(row);}
 public boolean decision(ServerPlayer p,UUID nonce,boolean accept){
  if(state()!=State.WAITING_DECISION||pending==null||!pending.equals(p.getUUID())||!Objects.equals(token,nonce)||p.level()!=level()||distanceToSqr(p)>100||!hasLineOfSight(p)){say(p,"invalid");return false;}
  pending=token=null;if(!accept){say(p,"declined");state(completedBefore?State.POST_TRIAL_NPC:State.IDLE_UNMET);return true;}
  return start(p);
 }
 public boolean start(ServerPlayer p){
  if(busy()||WolfProgress.passed(p)||p.isSpectator()||!p.isAlive()||p.level()!=level()||distanceToSqr(p)>100||!hasLineOfSight(p)||level().dimension()!=Level.OVERWORLD||level().getDifficulty()==Difficulty.PEACEFUL)return false;
  participant=p.getUUID();pending=token=null;setHealth(500);summons=0;outsideSince=-1;trialStart=level().getGameTime()+PREPARE;howlReady=trialStart+HOWL_DELAY;dashReady=trialStart;nextAttack=trialStart+25;state(State.TRIAL_PREPARE);say(p,"accepted");voice("growl",1);return true;
 }
 public void abort(){
  recallPack();participant=pending=token=null;setTarget(null);bar.removeAllPlayers();bar.setVisible(false);setHealth(completedBefore?50:500);outsideSince=-1;state(State.TRIAL_ABORTED);
 }
 public void finish(){if(!active())return;setHealth(50);setTarget(null);recallPack();state(State.TRIAL_COMPLETE_CINEMATIC);voice("trial_end",.65F);}
 public boolean debugMove(Move action){if(!debug||state()!=State.TRIAL_ACTIVE||move()!=Move.NONE)return false;var p=player();if(p==null)return false;return begin(action,p,true);}
 public void debugHealth(float health){if(debug&&active())setHealth(Math.max(50,Math.min(500,health)));}
 private boolean begin(Move action,ServerPlayer p,boolean forced){
  long now=level().getGameTime();if(action==Move.HOWL_SUMMON){
   if(summons>=MAX_SUMMONS||!forced&&now<howlReady||packAlive()>=3||!onGround())return false;
   packSpots=WolfEncounter.packSpots((ServerLevel)level(),blockPosition(),32);if(packSpots.size()<2)return false;state(State.TRIAL_SUMMONING);howlReady=now+HOWL_COOLDOWN;
  }
  if(action==Move.FLANK_DASH&&now<dashReady&&!forced)return false;
  Vec3 target=p.position().subtract(position()).multiply(1,0,1);direction=target.normalize();
  if(action==Move.POUNCE&&!clearPounce(direction))return false;
  if(action==Move.FLANK_DASH){direction=new Vec3(-direction.z,0,direction.x).scale(random.nextBoolean()?1:-1);if(!WolfEncounter.safeBody((ServerLevel)level(),getBoundingBox().move(direction.scale(3)),blockPosition().offset((int)(direction.x*3),0,(int)(direction.z*3))))return false;dashReady=now+280;}
  move(action);face(p.position().subtract(position()));if(action!=Move.HOWL_SUMMON)voice(action==Move.BITE||action==Move.DOUBLE_CLAW?"growl":action==Move.POUNCE?"pounce":"dash",.8F);nextAttack=now+Math.max(44,action.duration+14+random.nextInt(8));return true;
 }
 // Bounded swept block checks only when selecting/releasing a jump, never chunk loading.
 private boolean clearPounce(Vec3 heading){
  if(heading.lengthSqr()<.5)return false;
  AABB box=getBoundingBox();Vec3 velocity=heading.scale(1.10).add(0,.55,0);double rise=0;
  for(int i=0;i<24;i++){
   AABB swept=box.expandTowards(velocity).deflate(.001);
   for(int x=(int)Math.floor(swept.minX);x<Math.ceil(swept.maxX);x++)for(int z=(int)Math.floor(swept.minZ);z<Math.ceil(swept.maxZ);z++)if(!level().hasChunkAt(new BlockPos(x,(int)swept.minY,z)))return false;
   // The floor terminates the descending arc; overhead/wall collision rejects launch.
   if(!level().noCollision(this,swept)){if(velocity.y<0&&rise+velocity.y<=.05)return true;return false;}
   box=box.move(velocity);rise+=velocity.y;if(i>0&&rise<=0)return true;
   velocity=velocity.multiply(i==0?.546:.91,1,i==0?.546:.91);velocity=new Vec3(velocity.x,(velocity.y-.08)*.98,velocity.z);
  }
  return false;
 }
 private void stopWalking(){getNavigation().stop();getMoveControl().setWantedPosition(getX(),getY(),getZ(),0);setSpeed(0);setXxa(0);setZza(0);}
 private void face(Vec3 heading){if(heading.horizontalDistanceSqr()<1.0E-6)return;float yaw=(float)(Math.toDegrees(Math.atan2(heading.z,heading.x))-90);setYRot(yaw);yBodyRot=yaw;setYHeadRot(yaw);}
 @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source){return false;}
 private int packAlive(){if(!(level() instanceof ServerLevel w))return 0;pack.removeIf(id->{var e=w.getEntity(id);return e!=null&&!e.isAlive()||e==null&&w.getGameTime()>=packExpiry.getOrDefault(id,0L);});return pack.size();}
 private void summon(){
  var w=(ServerLevel)level();var valid=packSpots.stream().filter(p->WolfEncounter.safeWolf(w,p)).toList();if(valid.size()<2)return;
  int count=Math.min(3-packAlive(),random.nextFloat()<.6F?2:3);count=Math.min(count,valid.size());if(count<2)return;
  var created=new ArrayList<org.slavicmyths.entity.WildlifeEntity>();for(int i=0;i<count;i++){
   var wolf=ModEntities.FOREST_WOLF.get().create(w);if(wolf==null)return;var pos=valid.get(i);wolf.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,getYRot(),0);var d=wolf.getPersistentData();d.putBoolean("summonedByGreyWolf",true);d.putUUID("encounterId",encounter);d.putUUID("greyWolfActor",getUUID());d.putUUID("targetPlayerUUID",participant);d.putLong("expireGameTime",w.getGameTime()+1500);d.putLong("greyWolfActivate",w.getGameTime()+8);wolf.setPersistenceRequired();created.add(wolf);
  }
  for(var wolf:created)if(!w.addFreshEntity(wolf)){created.forEach(Entity::discard);return;}
  created.forEach(wolf->{pack.add(wolf.getUUID());packExpiry.put(wolf.getUUID(),w.getGameTime()+1500);});summons++;voice("pack_answer",.45F);
 }
 private void recallPack(){if(level() instanceof ServerLevel w)for(UUID id:pack){var e=w.getEntity(id);if(e!=null){e.getPersistentData().putLong("greyWolfLeave",w.getGameTime());if(e instanceof Mob mob){mob.setTarget(null);mob.getNavigation().stop();}}}pack.clear();packExpiry.clear();}
 public boolean ownsPack(UUID id){return active()&&Objects.equals(participant,id);}
 public void strike(ServerPlayer p,Move action){
  if(!active()||!Objects.equals(participant,p.getUUID())||!p.isAlive()||p.isSpectator()||p.isCreative()||!hasLineOfSight(p))return;
  Vec3 delta=p.position().subtract(position());double max=action==Move.POUNCE?4.2:3.8;
  if(delta.horizontalDistanceSqr()>max*max||Math.abs(delta.y)>3.5)return;
  Vec3 facing=getLookAngle().multiply(1,0,1).normalize();double cone=action==Move.DOUBLE_CLAW?55:32.5;
  if(delta.multiply(1,0,1).normalize().dot(facing)<Math.cos(Math.toRadians(cone)))return;
  float damage=switch(action){case BITE->13;case DOUBLE_CLAW->11;case POUNCE->17;case FLANK_DASH->15;default->0;};if(damage<=0)return;
  float before=p.getHealth()+p.getAbsorptionAmount();boolean landed=p.hurt(damageSources().mobAttack(this),damage);
  if(landed&&p.getHealth()+p.getAbsorptionAmount()<before){WolfBleeding.apply(this,p);if(action==Move.POUNCE)p.knockback(.65,getX()-p.getX(),getZ()-p.getZ());}
  voice(action==Move.DOUBLE_CLAW?"claw":"bite",.8F);
 }
 public boolean narrativeActor(){return true;}
 public void supportMove(WolfTimeline.Move action){if(!narrativeActor())move(action);}
 public void scenePose(WolfTimeline.Move action){if(level().isClientSide){entityData.set(STATE,State.POST_TRIAL_NPC.ordinal());entityData.set(MOVE,action.ordinal());entityData.set(CLOCK,level().getGameTime());}}
 @Override public boolean hurt(DamageSource source,float amount){
  if(!narrativeActor())return super.hurt(source,amount);
  if(source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)||source.getEntity()==this)return false;
  if(!active()||source.getEntity() instanceof Player p&&!Objects.equals(participant,p.getUUID()))return false;
  return super.hurt(source,amount);
 }
 @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source){return active()?WolfSounds.get("hurt"):null;}
 @Override public void kill(){} // Debug cleanup uses discard, never a death/loot transition.
 @Override protected void playStepSound(BlockPos pos,net.minecraft.world.level.block.state.BlockState block){if(onGround())playSound(WolfSounds.get("step"),.35F,1);}
 @Override public float getPickRadius(){return 1.1F;}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public void tick(){super.tick();if(!narrativeActor()||!(level() instanceof ServerLevel w))return;if(home==null)home=blockPosition().immutable();
  long now=w.getGameTime();int t=elapsed();var p=player();
  if(busy()&&(p==null||!p.isAlive()||p.level()!=level()||level().getDifficulty()==Difficulty.PEACEFUL)){abort();return;}
  if(busy()){if(home.distSqr(p.blockPosition())>38*38){if(outsideSince<0)outsideSince=now;if(now-outsideSince>=240){abort();return;}}else outsideSince=-1;}
  if(state()==State.GREETING&&t>=10){var invited=pending==null?null:(ServerPlayer)w.getPlayerByUUID(pending);if(invited==null){pending=token=null;state(completedBefore?State.POST_TRIAL_NPC:State.IDLE_UNMET);}else{state(State.WAITING_DECISION);choices(invited);}}
  else if(state()==State.WAITING_DECISION&&t>=400){pending=token=null;state(completedBefore?State.POST_TRIAL_NPC:State.IDLE_UNMET);}
  else if(state()==State.TRIAL_PREPARE&&t>=PREPARE){state(State.TRIAL_ACTIVE);bar.setVisible(true);setTarget(p);}
  else if(state()==State.TRIAL_COMPLETE_CINEMATIC){if(t==70)say(p,"recognition");if(t==110)say(p,"help");if(t>=160){WolfProgress.complete(p,this);completedBefore=true;participant=null;bar.removeAllPlayers();bar.setVisible(false);state(State.POST_TRIAL_NPC);voice("sit",.4F);}}
  else if(state()==State.TRIAL_ABORTED&&t>=20){state(completedBefore?State.POST_TRIAL_NPC:State.IDLE_UNMET);}
  if(busy()&&tickCount%20==0){bar.setProgress(Math.max(0,(getHealth()-50)/450));for(var observer:w.getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(32)))bar.addPlayer(observer);for(var observer:List.copyOf(bar.getPlayers()))if(observer.level()!=level()||distanceToSqr(observer)>40*40)bar.removePlayer(observer);}
  if(!busy()&&pending==null&&tickCount%20==0){var near=w.getNearestPlayer(this,8);if(near instanceof ServerPlayer q&&!q.isSpectator()&&!WolfProgress.data(q).getBoolean("wolf_met")&&hasLineOfSight(q))greet(q);}
  if(!busy()&&pending==null&&tickCount%100==0&&random.nextInt(4)==0){var q=net.minecraft.world.entity.ai.util.LandRandomPos.getPos(this,5,2);if(q!=null&&home.distSqr(BlockPos.containing(q))<=100)getNavigation().moveTo(q.x,q.y,q.z,.45);}
  if(!busy()&&home.distSqr(blockPosition())>100&&tickCount%40==0)getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.6);
  if(now>=lastAmbient&&!busy()){voice("ambient",.25F);lastAmbient=now+360+random.nextInt(341);}
 }
 private final class TrialGoal extends Goal {
  TrialGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}public boolean canUse(){return busy();}public boolean requiresUpdateEveryTick(){return true;}
  @Override public void tick(){var p=player();if(p==null)return;int t=elapsed();long now=level().getGameTime();
   if(state()==State.TRIAL_COMPLETE_CINEMATIC||state()==State.TRIAL_PREPARE){getNavigation().stop();getLookControl().setLookAt(p,20,20);return;}
   if(move()!=Move.NONE){
    Move a=move();stopWalking();
    if(a==Move.HOWL_SUMMON){setDeltaMovement(0,getDeltaMovement().y,0);if(t==HOWL_SOUND)voice("summon_howl",3);if(t==PACK_SPAWN)summon();}
    else {
     if(a!=Move.FLANK_DASH||t<10||t>18)getLookControl().setLookAt(p,25,20);
     if(a==Move.POUNCE){
      face(direction);
      if(t==16){direction=p.position().subtract(position()).multiply(1,0,1).normalize();if(!onGround()||!clearPounce(direction)){move(Move.NONE);nextAttack=now+20;return;}face(direction);setDeltaMovement(direction.scale(1.10).add(0,.55,0));hasImpulse=true;}
     }else face(p.position().subtract(position()));
     if(a==Move.FLANK_DASH&&t>=10&&t<=18){if(!horizontalCollision&&level().noCollision(GreyWolf.this,getBoundingBox().move(direction.scale(.36))))setDeltaMovement(direction.scale(.36).add(0,getDeltaMovement().y,0));else setDeltaMovement(0,getDeltaMovement().y,0);}
     if(WolfTimeline.hit(a,t))strike(p,a);
    }
    if(t>=a.duration){move(Move.NONE);if(state()==State.TRIAL_SUMMONING)state(State.TRIAL_ACTIVE);}return;
   }
   getLookControl().setLookAt(p,25,20);double distance=distanceTo(p);
   if(now>=nextAttack&&onGround()){
    if(now>=howlReady&&summons<MAX_SUMMONS&&random.nextFloat()<.25F&&begin(Move.HOWL_SUMMON,p,false))return;
    Move a;if(distance<=3.8){int roll=random.nextInt(100);a=roll<45?Move.BITE:roll<80?Move.DOUBLE_CLAW:Move.FLANK_DASH;}else a=distance<=9&&distance>=4?Move.POUNCE:Move.NONE;
    if(a!=Move.NONE&&begin(a,p,false))return;
   }
   if(tickCount%10==0){Vec3 dest=distance>3.3?p.position():p.position().add(Math.cos(now*.025)*3,0,Math.sin(now*.025)*3);if(home.distSqr(BlockPos.containing(dest))<=30*30)getNavigation().moveTo(dest.x,dest.y,dest.z,1);}
   if(getNavigation().isInProgress()&&position().distanceToSqr(lastMotion)<.001)stuckTicks++;else stuckTicks=0;lastMotion=position();if(stuckTicks>60){getNavigation().stop();nextAttack=now+20;stuckTicks=0;var q=net.minecraft.world.entity.ai.util.LandRandomPos.getPos(GreyWolf.this,5,2);if(q!=null)getNavigation().moveTo(q.x,q.y,q.z,.8);else abort();}
  }
 }
 @Override protected InteractionResult mobInteract(Player player,InteractionHand hand){if(player instanceof ServerPlayer p)greet(p);return InteractionResult.sidedSuccess(level().isClientSide);}
 @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putUUID("WolfEncounter",encounter);tag.putLong("WolfHome",home().asLong());tag.putBoolean("WolfCompleted",completedBefore);tag.putBoolean("WolfDebug",debug);}
 @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);encounter=tag.hasUUID("WolfEncounter")?tag.getUUID("WolfEncounter"):getUUID();home=tag.contains("WolfHome")?BlockPos.of(tag.getLong("WolfHome")):blockPosition();completedBefore=tag.getBoolean("WolfCompleted");debug=tag.getBoolean("WolfDebug");participant=pending=token=null;pack.clear();setTarget(null);setHealth(completedBefore?50:500);state(completedBefore?State.POST_TRIAL_NPC:State.IDLE_UNMET);}
}
