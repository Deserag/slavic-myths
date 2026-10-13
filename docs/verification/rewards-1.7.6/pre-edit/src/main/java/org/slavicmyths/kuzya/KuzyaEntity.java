package org.slavicmyths.kuzya;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;

/** A loaded, local household actor. No structure search, world traversal or forced chunks. */
public final class KuzyaEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> CLIP=SynchedEntityData.defineId(KuzyaEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(KuzyaEntity.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> VOICE=SynchedEntityData.defineId(KuzyaEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> VOICE_START=SynchedEntityData.defineId(KuzyaEntity.class,EntityDataSerializers.LONG);
    public BlockPos home;
    public UUID homeHutId;
    public boolean renovated=true;
    private long elapsed,nextActivity,voiceReady,snoreReady,moveDeadline,nextPath,restTicks,turnReady,houseVoiceReady;
    private int eventCursor,bites,waypoint;
    private String purpose="idle";private double previousX,previousZ,stepDistance;private boolean strideReady;
    private final Deque<String> sequence=new ArrayDeque<>();
    private final List<Vec3> route=new ArrayList<>();
    private final Map<UUID,Guest> guests=new HashMap<>();
    private UUID pendingPlayer;
    private int lastChore=-1;
    private String pendingVoice="voice_interact";
    private boolean reloadPending;
    private static final class Guest {int ticks,away;long clickReady,complaintReady;}
    public KuzyaEntity(EntityType<? extends KuzyaEntity> type,net.minecraft.world.level.Level level){super(type,level);setPersistenceRequired();setInvulnerable(true);setCanPickUpLoot(false);getNavigation().setCanFloat(false);moveControl=new net.minecraft.world.entity.ai.control.MoveControl(this){@Override public void tick(){if(!route.isEmpty()&&(purpose.equals("stove")&&waypoint>0||purpose.equals("down"))){Vec3 target=route.get(waypoint);if(position().distanceToSqr(target)<2.6)setWantedPosition(target.x,target.y,target.z,2.6);}super.tick();}};}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(CLIP,0);b.define(START,0L);b.define(VOICE,-1);b.define(VOICE_START,0L);}
    public String clip(){return KuzyaAssets.CLIPS[entityData.get(CLIP)];}
    public float clipAge(float partial){return (float)(level().getGameTime()-entityData.get(START))+partial;}
    public int voice(){return entityData.get(VOICE);}
    public float voiceAge(float partial){return (float)(level().getGameTime()-entityData.get(VOICE_START))+partial;}
    public AABB homeBounds(){return home==null?getBoundingBox():new AABB(home.getX()-4,home.getY()+4,home.getZ()-5,home.getX()+5,home.getY()+8,home.getZ()+6);}
    public void bind(BlockPos origin,UUID hut){home=origin.immutable();homeHutId=hut;getPersistentData().putBoolean("isKuzyaHutResident",true);setCustomName(net.minecraft.network.chat.Component.translatable("entity.slavicmyths.kuzya"));begin("idle_stand");nextActivity=level().getGameTime()+20+random.nextInt(41);}
    private Vec3 anchor(double x,double y,double z){return new Vec3(home.getX()+x,home.getY()+y,home.getZ()+z);}
    private void begin(String name){entityData.set(CLIP,KuzyaAssets.index(name));entityData.set(START,level().getGameTime());elapsed=0;eventCursor=0;}
    private void rest(){sequence.clear();route.clear();getNavigation().stop();purpose="idle";restTicks=0;begin("idle_stand");nextActivity=level().getGameTime()+60+random.nextInt(61);}
    private void queue(String... names){sequence.addAll(Arrays.asList(names));begin(sequence.removeFirst());}
    private boolean sleeping(){return clip().startsWith("sleep")||clip().equals("lie_idle");}
    private boolean transition(){return !route.isEmpty()||clip().equals("climb_stove")||clip().equals("dismount_stove")||clip().equals("lie_down")||clip().equals("wake_up");}
    private boolean safe(Vec3 v){BlockPos p=BlockPos.containing(v);return level().hasChunkAt(p)&&homeBounds().contains(v)&&level().getFluidState(p).isEmpty()&&level().noCollision(this,new AABB(v.x-.26,v.y+.01,v.z-.26,v.x+.26,v.y+.70,v.z+.26))&&level().getBlockCollisions(this,new AABB(v.x-.24,v.y-.08,v.z-.24,v.x+.24,v.y-.001,v.z+.24)).iterator().hasNext();}
    private void travel(String why,Vec3... points){purpose=why;sequence.clear();route.clear();route.addAll(Arrays.asList(points));waypoint=0;moveDeadline=level().getGameTime()+360;nextPath=0;begin(why.equals("down")?"dismount_stove":"walk");}
    private void routeTick(){long now=level().getGameTime();Vec3 target=route.get(waypoint);if(Math.hypot(getX()-target.x,getZ()-target.z)<.5&&Math.abs(getY()-target.y)<.26){if(waypoint==route.size()-1&&purpose.equals("stove")&&elapsed<42){getNavigation().stop();return;}waypoint++;getNavigation().stop();nextPath=0;if(purpose.equals("stove")&&waypoint==1)begin("climb_stove");if(waypoint>=route.size()){route.clear();switch(purpose){case "stove"->{if(random.nextInt(4)==0)queue("lie_down","lie_idle");else queue("lie_down","sleep_enter","sleep_loop");}case "table"->{bites=4;queue("approach_table","sit_down","pick_food","eat_bite","eat_chew");}case "down"->rest();case "inspect"->queue(random.nextBoolean()?"turn_left":"turn_right","domestic_inspect","domestic_sweep","carry_bowl","place_bowl","idle_stand");default->rest();}return;}target=route.get(waypoint);}
        if(now>=moveDeadline){rest();nextActivity=now+100;return;}
        if(now>=nextPath){nextPath=now+40;if(safe(target)){var path=getNavigation().createPath(BlockPos.containing(target),0);if(path!=null)getNavigation().moveTo(path,(purpose.equals("stove")&&waypoint>0||purpose.equals("down"))?2.2:1.2);}else{rest();nextActivity=now+140;}}
    }
    @Override public void tick(){super.tick();if(level().isClientSide||home==null||!isAlive())return;clearFire();setAirSupply(getMaxAirSupply());long now=level().getGameTime();elapsed++;if(strideReady&&clip().equals("walk")&&onGround()){stepDistance+=Math.min(.5,Math.hypot(getX()-previousX,getZ()-previousZ));if(stepDistance>=.11){stepDistance%=.11;sound("step_wood",.3F);}}previousX=getX();previousZ=getZ();strideReady=true;
        if(reloadPending){reloadPending=false;getNavigation().stop();nextPath=0;strideReady=false;}
        if(!homeBounds().inflate(.1).contains(position())){if(now>=nextPath){nextPath=now+80;Vec3 floor=anchor(.5,4,.5);if(safe(floor))getNavigation().moveTo(floor.x,floor.y,floor.z,.8);}return;}
        if(!route.isEmpty()){routeTick();events();if(tickCount%20==0)updateGuests();return;}
        if(elapsed==1&&clip().equals("domestic_inspect"))speak("voice_idle");
        events();
        if(tickCount%20==0){updateGuests();if(!transition())guestsReact();}
        if(sleeping()&&now>=snoreReady){sound(random.nextBoolean()?"snore":"sleep_breath",.22F);snoreReady=now+700+random.nextInt(701);}
        if(elapsed>=KuzyaAssets.TICKS[entityData.get(CLIP)]&&!KuzyaAssets.LOOPS[entityData.get(CLIP)])complete();
        if(sleeping()){restTicks++;if(restTicks>2400+Math.floorMod(getUUID().hashCode(),3600)){sequence.clear();queue("wake_up");purpose="wake";}else if(restTicks>=turnReady&&clip().equals("sleep_loop")){turnReady=restTicks+1800+random.nextInt(1801);begin("sleep_turn");}}
        if((clip().equals("idle_stand")||clip().equals("idle_sit"))&&now>=nextActivity&&tickCount%20==0)choose();
    }
    private void complete(){if(!sequence.isEmpty()){begin(sequence.removeFirst());return;}switch(clip()){
        case "eat_chew"->{if(--bites>0)queue("eat_bite","eat_chew");else queue("eat_swallow","place_bowl","stand_up");}
        case "wake_up"->{if(getY()>home.getY()+5)travel("down",anchor(2.5,5.5,4.5),anchor(2.5,5,3.5),anchor(1.5,4.5,3.5),anchor(.5,4,3.5));else rest();}
        case "sleep_turn"->begin("sleep_loop");
        case "stand_up"->{rest();if(position().distanceToSqr(anchor(1.5,4.5,-2.5))<.3)begin("idle_sit");}
        case "respond_right_click", "notice_guest", "mutter_small", "grumble_medium", "grumble_large", "shoo_guest"->{long activityDue=nextActivity;rest();nextActivity=activityDue;}
        default->rest();}}
    private void choose(){int roll=random.nextInt(100);boolean night=Math.floorMod(level().getDayTime(),24000)>12500;Vec3 bed=anchor(3.5,6,4.5),seat=anchor(1.5,4.5,-2.5);if(renovated&&roll<(night?80:32)&&safe(bed)){restTicks=0;turnReady=1800+random.nextInt(1801);travel("stove",anchor(.5,4,3.5),anchor(1.5,4.5,3.5),anchor(2.5,5,3.5),anchor(2.5,5.5,4.5),bed);}else if(renovated&&roll<(night?90:57)&&safe(seat)&&foodTableAvailable())travel("table",seat);else if(roll<90){chores();}else{var yaga=level().getEntitiesOfClass(org.slavicmyths.yaga.BabaYaga.class,homeBounds(),b->b.isAlive());begin(yaga.isEmpty()?random.nextBoolean()?"turn_left":"turn_right":"react_to_yaga");}}
    private void chores(){
        // Four fixed household anchors; no structure or block scan.
        Vec3[] spots={anchor(-2.5,4,-1.5),anchor(.5,4,-3.5),anchor(-1.5,4,3.5),anchor(-2.5,4,4.5)};
        int start=random.nextInt(spots.length);
        for(int j=0;j<spots.length;j++){int at=(start+j)%spots.length;if(at==lastChore&&j<spots.length-1)continue;if(safe(spots[at])){lastChore=at;travel("inspect",spots[at]);return;}}
        queue("domestic_inspect","domestic_sweep");
    }
    private boolean foodTableAvailable(){return level().getBlockEntity(home.offset(2,4,-2)) instanceof org.slavicmyths.kitchen.TableTile table&&table.food.stream().anyMatch(stack->!stack.isEmpty());}
    private void events(){String c=clip();int id=entityData.get(CLIP);var list=KuzyaAssets.EVENTS[id];while(eventCursor<list.length&&elapsed>=list[eventCursor].tick()){var ev=list[eventCursor++];if(!ev.cue().startsWith("voice_")&&!c.equals("sleep_loop")&&!c.equals("walk"))sound(ev.cue(),.3F);else if(ev.cue().startsWith("voice_")){String cue=c.equals("respond_right_click")?pendingVoice:ev.cue();if(c.equals("grumble_medium"))cue="voice_ask_leave_medium";if(c.equals("grumble_large"))cue="voice_ask_leave_large";speak(cue);}if(c.equals("pick_food")&&level().getGameTime()>=houseVoiceReady)speak("voice_eat");if(c.equals("shoo_guest"))speak("voice_ask_leave_large");}}
    private void sound(String cue,float volume){level().playSound(null,getX(),getY(),getZ(),KuzyaContent.sound(cue),SoundSource.NEUTRAL,volume,1);}
    private void speak(String category){long now=level().getGameTime();boolean response=clip().equals("respond_right_click");if(now<voiceReady||!response&&now<houseVoiceReady)return;int[] choices=KuzyaAssets.voices(category);if(choices.length==0)return;int v=choices[random.nextInt(choices.length)];entityData.set(VOICE,v);entityData.set(VOICE_START,now);voiceReady=now+Math.max(240,KuzyaAssets.VOICE_TICKS[v]+20);houseVoiceReady=now+(category.endsWith("_large")?1400+random.nextInt(801):category.endsWith("_medium")?900+random.nextInt(601):900);sound(KuzyaAssets.VOICES[v],.7F);for(Player listener:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(14),p->distanceToSqr(p)<=196))listener.displayClientMessage(net.minecraft.network.chat.Component.translatable("subtitles.slavicmyths.kuzya."+KuzyaAssets.VOICES[v]),true);}
    private void updateGuests(){var present=level().getEntitiesOfClass(Player.class,homeBounds(),p->p.isAlive()&&!p.isSpectator());Set<UUID> seen=new HashSet<>();for(Player p:present){seen.add(p.getUUID());Guest g=guests.computeIfAbsent(p.getUUID(),k->new Guest());g.away=0;g.ticks=Math.min(24000,g.ticks+20);}for(var it=guests.entrySet().iterator();it.hasNext();){var e=it.next();if(!seen.contains(e.getKey())){Guest g=e.getValue();g.away+=20;if(g.away>=600)g.ticks=0;if(g.away>=1200&&level().getGameTime()>=g.clickReady)it.remove();}}}
    private void guestsReact(){long now=level().getGameTime();if(pendingPlayer!=null&&now>=voiceReady&&!sleeping()&&!purpose.equals("table")){pendingPlayer=null;begin("respond_right_click");return;}if(sleeping()||!clip().startsWith("idle")||now<voiceReady||now<houseVoiceReady)return;if(level().getEntitiesOfClass(Player.class,homeBounds(),p->p.containerMenu instanceof org.slavicmyths.yaga.YagaMenu).size()>0)return;Player target=level().getEntitiesOfClass(Player.class,homeBounds(),p->{Guest g=guests.get(p.getUUID());return g!=null&&g.ticks>=600&&now>=g.complaintReady&&!p.isSpectator();}).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);if(target==null)return;Guest g=guests.get(target.getUUID());getLookControl().setLookAt(target,20,20);if(g.ticks>=4200){g.complaintReady=now+1400+random.nextInt(801);begin(random.nextBoolean()?"grumble_large":"shoo_guest");}else if(g.ticks>=2400){g.complaintReady=now+900+random.nextInt(601);begin("grumble_medium");}else if(g.ticks>=1200&&random.nextFloat()<.35){g.complaintReady=now+700+random.nextInt(301);begin("mutter_small");speak("voice_notice");}else{g.complaintReady=now+300+random.nextInt(201);if(random.nextFloat()<.18)begin("notice_guest");}}
    @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){if(distanceToSqr(p)>9||!hasLineOfSight(p))return InteractionResult.PASS;if(!level().isClientSide){Guest g=guests.computeIfAbsent(p.getUUID(),k->new Guest());long now=level().getGameTime();if(now>=g.clickReady&&now>=voiceReady&&pendingPlayer==null&&!clip().equals("respond_right_click")){g.clickReady=now+100;pendingPlayer=p.getUUID();pendingVoice=sleeping()?"voice_sleep":random.nextInt(5)==0?"voice_enter":"voice_interact";if(sleeping()){sequence.clear();queue("wake_up");}else if(!transition()&&!purpose.equals("table")&&!clip().equals("respond_right_click")){begin("respond_right_click");pendingPlayer=null;}}}return InteractionResult.sidedSuccess(level().isClientSide);}
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override public boolean isInvulnerableTo(DamageSource source){return true;}
    @Override public void kill(){discard();}
    @Override public void knockback(double strength,double x,double z){}
    @Override public boolean isPushable(){return false;}
    @Override public void push(Entity e){}
    @Override public void push(double x,double y,double z){}
    @Override public boolean canBeLeashed(){return false;}
    @Override public boolean startRiding(Entity e,boolean force){return false;}
    @Override public boolean canUsePortal(boolean passengers){return false;}
    @Override public boolean canChangeDimensions(net.minecraft.world.level.Level from,net.minecraft.world.level.Level to){return false;}
    @Override public boolean canBeAffected(MobEffectInstance effect){return false;}
    @Override public void move(MoverType type,Vec3 movement){if(type==MoverType.PISTON)return;if(home!=null&&!level().isClientSide){Vec3 next=position().add(movement);AABB bounds=homeBounds();if(bounds.contains(position())&&!bounds.contains(new Vec3(next.x,Math.clamp(next.y,bounds.minY+.001,bounds.maxY-.001),next.z)))movement=new Vec3(0,movement.y,0);}super.move(type,movement);}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putInt("data_version",2);saveScene(n);n.putBoolean("Renovated",renovated);n.putLong("HouseVoiceRemaining",Math.clamp(houseVoiceReady-level().getGameTime(),0,2400));if(home!=null)n.putLong("HomeOrigin",home.asLong());if(homeHutId!=null)n.putUUID("HomeHutId",homeHutId);n.putString("HomeDimension",level().dimension().location().toString());n.putInt("HomeRotation",0);n.putString("Activity",purpose);n.putLong("VoiceRemaining",Math.clamp(voiceReady-level().getGameTime(),0,2400));ListTag list=new ListTag();for(var e:guests.entrySet()){CompoundTag t=new CompoundTag();t.putUUID("Player",e.getKey());t.putInt("Ticks",e.getValue().ticks);t.putInt("Away",e.getValue().away);t.putLong("ComplaintRemaining",Math.clamp(e.getValue().complaintReady-level().getGameTime(),0,2400));list.add(t);}n.put("Guests",list);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);renovated=!n.contains("Renovated")||n.getBoolean("Renovated");houseVoiceReady=level().getGameTime()+Math.clamp(n.getLong("HouseVoiceRemaining"),0,2400);home=n.contains("HomeOrigin")?BlockPos.of(n.getLong("HomeOrigin")):null;homeHutId=n.hasUUID("HomeHutId")?n.getUUID("HomeHutId"):null;voiceReady=level().getGameTime()+Math.clamp(n.getLong("VoiceRemaining"),0,2400);guests.clear();for(Tag raw:n.getList("Guests",10)){CompoundTag t=(CompoundTag)raw;if(!t.hasUUID("Player")||guests.size()>=64)continue;Guest g=new Guest();g.ticks=Math.clamp(t.getInt("Ticks"),0,24000);g.away=Math.clamp(t.getInt("Away"),0,1200);g.complaintReady=level().getGameTime()+Math.clamp(t.getLong("ComplaintRemaining"),0,2400);guests.put(t.getUUID("Player"),g);}restoreScene(n);reloadPending=true;setInvulnerable(true);}
    private void saveScene(CompoundTag n){
        long now=level().getGameTime();n.putLong("SceneSavedAt",now);n.putString("SceneClip",clip());n.putLong("SceneAge",elapsed);
        n.putLong("ActivityRemaining",Math.clamp(nextActivity-now,0,2400));n.putLong("MoveRemaining",Math.clamp(moveDeadline-now,0,360));
        n.putLong("RestTicks",restTicks);n.putLong("TurnAt",turnReady);n.putLong("SnoreRemaining",Math.clamp(snoreReady-now,0,1400));
        n.putInt("Bites",bites);n.putInt("LastChore",lastChore);n.putInt("Waypoint",waypoint);
        ListTag points=new ListTag();for(Vec3 v:route){CompoundTag p=new CompoundTag();p.putDouble("X",v.x);p.putDouble("Y",v.y);p.putDouble("Z",v.z);points.add(p);}n.put("Route",points);
        ListTag queued=new ListTag();for(String c:sequence)queued.add(net.minecraft.nbt.StringTag.valueOf(c));n.put("SceneQueue",queued);
    }
    private boolean knownClip(String c){return Arrays.asList(KuzyaAssets.CLIPS).contains(c);}
    private void restoreScene(CompoundTag n){
        long now=level().getGameTime(),away=n.contains("SceneSavedAt")?Math.clamp(now-n.getLong("SceneSavedAt"),0,24000):0;
        voiceReady=now+Math.max(0,voiceReady-now-away);houseVoiceReady=now+Math.max(0,houseVoiceReady-now-away);
        for(Guest g:guests.values()){g.away=(int)Math.min(1200,g.away+away);if(g.away>=600)g.ticks=0;}
        route.clear();sequence.clear();pendingPlayer=null;pendingVoice="voice_interact";nextActivity=now;
        if(n.getInt("data_version")<2||!knownClip(n.getString("SceneClip"))){
            purpose="idle";begin("idle_stand");
            if(home!=null&&getY()>home.getY()+5.8){purpose="stove";begin("lie_idle");restTicks=2400;}
            return;
        }
        purpose=Set.of("idle","stove","table","down","inspect","wake").contains(n.getString("Activity"))?n.getString("Activity"):"idle";
        for(Tag raw:n.getList("Route",10)){if(route.size()>=8)break;CompoundTag p=(CompoundTag)raw;Vec3 v=new Vec3(p.getDouble("X"),p.getDouble("Y"),p.getDouble("Z"));if(Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z)&&home!=null&&homeBounds().contains(v))route.add(v);}
        waypoint=Math.clamp(n.getInt("Waypoint"),0,Math.max(0,route.size()-1));
        for(Tag raw:n.getList("SceneQueue",8)){if(sequence.size()>=16)break;String c=raw.getAsString();if(knownClip(c))sequence.add(c);}
        String c=n.getString("SceneClip");if(route.isEmpty()&&(c.equals("walk")||c.equals("climb_stove")||c.equals("dismount_stove"))){c="idle_stand";sequence.clear();purpose="idle";}begin(c);elapsed=Math.clamp(n.getLong("SceneAge"),0,24000);
        if(route.isEmpty()&&!KuzyaAssets.LOOPS[entityData.get(CLIP)])elapsed=Math.min(KuzyaAssets.TICKS[entityData.get(CLIP)],elapsed+away);
        entityData.set(START,now-elapsed);
        // Skip old sound markers on reload rather than replaying an unloaded scene.
        var markers=KuzyaAssets.EVENTS[entityData.get(CLIP)];while(eventCursor<markers.length&&markers[eventCursor].tick()<=elapsed)eventCursor++;
        nextActivity=now+Math.max(0,Math.clamp(n.getLong("ActivityRemaining"),0,2400)-away);
        moveDeadline=now+Math.max(20,Math.clamp(n.getLong("MoveRemaining"),0,360));nextPath=0;
        restTicks=Math.clamp(n.getLong("RestTicks")+(sleeping()?away:0),0,24000);turnReady=Math.clamp(n.getLong("TurnAt"),0,24000);
        snoreReady=now+Math.max(20,Math.clamp(n.getLong("SnoreRemaining"),0,1400)-away);bites=Math.clamp(n.getInt("Bites"),0,4);lastChore=Math.clamp(n.getInt("LastChore"),-1,3);
    }

}
