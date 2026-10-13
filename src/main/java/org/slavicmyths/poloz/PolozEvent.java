package org.slavicmyths.poloz;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
/** Overworld single-event compare-and-set, persistent preparation and UUID-based restart reconciliation. */
public final class PolozEvent extends SavedData {
 public enum State{IDLE,PREPARING,ACTIVE,PAUSED,DEFEATED,FAILED}
 public State state=State.IDLE;public UUID event,boss,owner,token;public BlockPos ritual=BlockPos.ZERO,spawn=BlockPos.ZERO;public long cooldownUntil;public int elapsed,emptyTicks;public int cost;public boolean previousHorn,refundPending;private float yaw;
 private static final TicketType<Long> TICKET=TicketType.create("slavicmyths_poloz",Long::compare);
 private final Set<ChunkPos> tickets=new HashSet<>();
 public final Set<UUID> participants=new HashSet<>();
 public final Map<UUID,Boolean> hornRefunds=new HashMap<>();
 public static PolozEvent get(ServerLevel w){return w.getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(PolozEvent::new,PolozEvent::load),"slavicmyths_poloz_1832");}
 public boolean running(){return state==State.PREPARING||state==State.ACTIVE||state==State.PAUSED;}
 public static boolean canStart(State state){return state==State.IDLE||state==State.DEFEATED||state==State.FAILED;}
 public static boolean active(ServerLevel w){return w.dimension()==Level.OVERWORLD&&get(w).running();}
 public GreatPoloz loaded(ServerLevel w){return boss!=null&&w.getEntity(boss) instanceof GreatPoloz p?p:null;}
 public boolean commit(ServerPlayer player,net.minecraft.world.item.ItemStack horn,BlockPos altar){var w=player.serverLevel();if(w.dimension()!=Level.OVERWORLD||!canStart(state)||!horn.is(PolozContent.HORN.get()))return false;
  boolean used=MorHorn.used(horn);int required=0;
  BlockPos selected=null;float angle=0;
  for(int i=0;i<32;i++){double a=w.random.nextDouble()*Math.PI*2,r=200+w.random.nextDouble()*100;BlockPos q=altar.offset((int)(Math.cos(a)*r),0,(int)(Math.sin(a)*r));if(!w.hasChunkAt(q)||!w.getWorldBorder().isWithinBounds(q))continue;int y=w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,q.getX(),q.getZ());q=new BlockPos(q.getX(),y,q.getZ());if(y<w.getMinBuildHeight()+5||y>w.getMaxBuildHeight()-6)continue;boolean safe=true;for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int h=0;h<5;h++){var pos=q.offset(x,h,z);if(!w.hasChunkAt(pos)||w.getBlockState(pos).getDestroySpeed(w,pos)<0||w.getBlockEntity(pos)!=null){safe=false;break;}}if(safe){selected=q;angle=(float)(a*180/Math.PI+90);break;}}
  if(selected==null){for(int radius:new int[]{64,48,32,24}){for(int i=0;i<8;i++){double a=Math.toRadians(player.getYRot())+i*Math.PI/4;BlockPos q=altar.offset((int)(-Math.sin(a)*radius),0,(int)(Math.cos(a)*radius));if(!w.hasChunkAt(q)||!w.getWorldBorder().isWithinBounds(q))continue;q=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,q);if(q.getY()<w.getMinBuildHeight()+5||q.getY()>w.getMaxBuildHeight()-6||!w.getFluidState(q).isEmpty()||w.getBlockEntity(q)!=null||w.getBlockEntity(q.below())!=null)continue;selected=q;angle=(float)(a*180/Math.PI+90);break;}if(selected!=null)break;}}
  if(selected==null){player.displayClientMessage(Component.translatable("poloz.no_spawn"),false);return false;}
  participants.clear();participants.add(player.getUUID());event=UUID.randomUUID();token=UUID.randomUUID();boss=UUID.randomUUID();owner=player.getUUID();ritual=altar.immutable();spawn=selected;cost=required;previousHorn=used;refundPending=false;yaw=angle;elapsed=0;emptyTicks=0;state=State.PREPARING;setDirty();
  MorHorn.consume(player,required);MorHorn.mark(horn,true,token);
  w.playSound(null,altar,PolozContent.sound("mor_horn_call"),net.minecraft.sounds.SoundSource.PLAYERS,10,1);
  for(var p:w.players()){p.displayClientMessage(Component.translatable("poloz.awakened"),false);org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:great_poloz",2,false);}hold(w,Set.of(new ChunkPos(spawn)));return true;
 }
 private void hold(ServerLevel w,Set<ChunkPos> wanted){for(var p:new ArrayList<>(tickets))if(!wanted.contains(p)){w.getChunkSource().removeRegionTicket(TICKET,p,2,p.toLong());tickets.remove(p);}for(var p:wanted)if(tickets.size()<16&&tickets.add(p))w.getChunkSource().addRegionTicket(TICKET,p,2,p.toLong());}
 private void release(ServerLevel w){hold(w,Set.of());}
 public void tick(ServerLevel w){if(!running()){release(w);refund(w);return;}if(w.players().isEmpty())emptyTicks++;else emptyTicks=0;if(emptyTicks>=6000){state=State.PAUSED;release(w);setDirty();return;}if(state==State.PAUSED){state=State.ACTIVE;setDirty();}
  var entity=loaded(w);Set<ChunkPos> wanted=new HashSet<>();if(entity!=null){wanted.add(new ChunkPos(entity.blockPosition()));for(var part:entity.parts)wanted.add(new ChunkPos(part.blockPosition()));Vec3 forward=Vec3.directionFromRotation(0,entity.getYRot()).scale(8);wanted.add(new ChunkPos(BlockPos.containing(entity.position().add(forward))));spawn=entity.blockPosition();}else wanted.add(new ChunkPos(spawn));hold(w,wanted);
  if(state==State.PREPARING){elapsed++;setDirty();if(elapsed==120){w.playSound(null,spawn,PolozContent.sound("poloz_distant_reply"),net.minecraft.sounds.SoundSource.HOSTILE,10,1);w.playSound(null,ritual,PolozContent.sound("poloz_earth_rumble"),net.minecraft.sounds.SoundSource.BLOCKS,5,1);}if(elapsed>=180){if(entity==null){var p=PolozContent.BOSS.get().create(w);if(p==null){fail(w);return;}p.setUUID(boss);p.moveTo(spawn.getX()+.5,spawn.getY(),spawn.getZ()+.5,yaw,0);p.setHealth(3000);p.initializeBody();if(!w.addFreshEntity(p)){fail(w);return;}}state=State.ACTIVE;setDirty();}}
  else elapsed++;
  if(state==State.ACTIVE&&elapsed<=580&&elapsed%4==0)for(int h=0;h<120;h+=8)w.sendParticles(org.slavicmyths.mor.MorContent.SPARK.get(),spawn.getX()+.5,spawn.getY()+h,spawn.getZ()+.5,2,.5,2,.5,.01);
  if(elapsed%40==0){setDirty();for(var p:w.players())if(p.getMainHandItem().is(PolozContent.HORN.get()))direction(p);}
 }
 private void fail(ServerLevel w){state=State.FAILED;refundPending=true;if(token!=null)hornRefunds.put(token,previousHorn);setDirty();release(w);refund(w);}
 private void refund(ServerLevel w){if(!refundPending||owner==null)return;var p=w.getServer().getPlayerList().getPlayer(owner);if(p==null)return;var n=p.getPersistentData();if(!n.hasUUID("PolozLastRefund")||!token.equals(n.getUUID("PolozLastRefund"))){var dust=new net.minecraft.world.item.ItemStack(PolozContent.DUST.get(),cost);if(cost>0&&!p.getInventory().add(dust))p.drop(dust,false);n.putUUID("PolozLastRefund",token);}refundPending=false;setDirty();p.displayClientMessage(Component.translatable("poloz.failed"),false);}
 public void direction(ServerPlayer p){var entity=loaded(p.serverLevel());Vec3 at=entity==null?Vec3.atCenterOf(spawn):entity.position();if(!running()){p.displayClientMessage(Component.translatable("poloz.call_ready"),true);return;}double dx=at.x-p.getX(),dz=at.z-p.getZ();int direction=Math.floorMod((int)Math.round(Math.atan2(dx,-dz)/(Math.PI/4)),8);p.displayClientMessage(Component.translatable("poloz.direction",Component.translatable("poloz.compass."+direction),(int)Math.hypot(dx,dz)),true);}
 public void finish(ServerLevel w,boolean defeated){state=defeated?State.DEFEATED:State.FAILED;cooldownUntil=w.getGameTime()+24000;setDirty();release(w);for(var p:w.players())if(org.slavicmyths.skim.HeroCalls.count(p)>2)for(var id:List.of("alyosha_popovich","dobrynya_nikitich","ilya_muromets"))if(org.slavicmyths.skim.HeroCalls.count(p)>2&&org.slavicmyths.skim.HeroCalls.loaded(p,id)!=null)org.slavicmyths.skim.HeroCalls.toggle(p,id);}
 @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider lookup){var refundTags=new CompoundTag();hornRefunds.forEach((id,used)->refundTags.putBoolean(id.toString(),used));n.put("HornRefunds",refundTags);var participantTags=new ListTag();for(UUID id:participants)participantTags.add(StringTag.valueOf(id.toString()));n.put("Participants",participantTags);n.putString("State",state.name());if(event!=null)n.putUUID("Event",event);if(boss!=null)n.putUUID("Boss",boss);if(owner!=null)n.putUUID("Owner",owner);if(token!=null)n.putUUID("Token",token);n.putLong("Ritual",ritual.asLong());n.putLong("Spawn",spawn.asLong());n.putInt("Elapsed",elapsed);n.putLong("Cooldown",cooldownUntil);n.putInt("Cost",cost);n.putBoolean("PreviousHorn",previousHorn);n.putBoolean("RefundPending",refundPending);n.putFloat("Yaw",yaw);n.putInt("EmptyTicks",emptyTicks);return n;}
 public static PolozEvent load(CompoundTag n,HolderLookup.Provider lookup){var d=new PolozEvent();var refundTags=n.getCompound("HornRefunds");for(String id:refundTags.getAllKeys())try{d.hornRefunds.put(UUID.fromString(id),refundTags.getBoolean(id));}catch(IllegalArgumentException ignored){}for(var participant:n.getList("Participants",Tag.TAG_STRING))try{d.participants.add(UUID.fromString(participant.getAsString()));}catch(IllegalArgumentException ignored){}try{d.state=State.valueOf(n.getString("State"));}catch(IllegalArgumentException ignored){}d.event=n.hasUUID("Event")?n.getUUID("Event"):null;d.boss=n.hasUUID("Boss")?n.getUUID("Boss"):null;d.owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;d.token=n.hasUUID("Token")?n.getUUID("Token"):null;d.ritual=BlockPos.of(n.getLong("Ritual"));d.spawn=BlockPos.of(n.getLong("Spawn"));d.elapsed=Math.max(0,n.getInt("Elapsed"));d.cooldownUntil=n.getLong("Cooldown");d.cost=Math.clamp(n.getInt("Cost"),0,4);d.previousHorn=n.getBoolean("PreviousHorn");d.refundPending=n.getBoolean("RefundPending");d.yaw=n.getFloat("Yaw");d.emptyTicks=Math.clamp(n.getInt("EmptyTicks"),0,6000);return d;}
}
