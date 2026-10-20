package org.slavicmyths.marinka;

import java.util.*;

import net.minecraft.core.*;

import net.minecraft.nbt.*;

import net.minecraft.server.level.*;

import net.minecraft.world.level.saveddata.SavedData;

import net.minecraft.world.level.block.*;

import net.minecraft.world.level.block.entity.ChestBlockEntity;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.*;

import net.minecraft.network.chat.Component;

import net.minecraft.world.item.*;

import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Per-dimension placed yard records and persisted ownership. Only addressed jobs are ticked. */

@EventBusSubscriber(modid="slavicmyths")

public final class WitchYard extends SavedData {

 public enum Status {BUILDING,READY,COMBAT,WON,OPEN,BLOCKED}

 public static final class Entry {

  public final UUID id;public final BlockPos center;public final int rotation;public Status status=Status.BUILDING;

  public int cursor,hatchStage;public final Map<Long,String> expected=new HashMap<>();public UUID owner,boss,hero,standby;public boolean debug,lootInstalled;public long wonAt;public final Set<UUID> participants=new HashSet<>();

  public Entry(UUID id,BlockPos pos,int rotation){this.id=id;center=pos.immutable();this.rotation=Math.floorMod(rotation,4);}

  public BlockPos at(int x,int y,int z){return switch(rotation){case 1->center.offset(-z,y,x);case 2->center.offset(-x,y,-z);case 3->center.offset(z,y,-x);default->center.offset(x,y,z);};}

  public double radius(Vec3 p){return Math.hypot(p.x-center.getX()-.5,p.z-center.getZ()-.5);}

  public CompoundTag save(){var n=new CompoundTag();n.putUUID("Id",id);n.putLong("Center",center.asLong());n.putInt("Rotation",rotation);n.putString("Status",status.name());n.putInt("Cursor",cursor);n.putInt("HatchStage",hatchStage);n.putBoolean("basementUnlocked",status==Status.OPEN);n.putLong("Entrance",at(0,0,0).asLong());n.putLong("WonAt",wonAt);n.putBoolean("Debug",debug);n.putBoolean("LootInstalled",lootInstalled);if(owner!=null)n.putUUID("Owner",owner);if(boss!=null)n.putUUID("Boss",boss);if(hero!=null)n.putUUID("Hero",hero);if(standby!=null)n.putUUID("Standby",standby);var p=new ListTag();for(var id:participants)p.add(NbtUtils.createUUID(id));n.put("Participants",p);var expectedList=new ListTag();expected.forEach((pos,state)->{var t=new CompoundTag();t.putLong("Pos",pos);t.putString("State",state);expectedList.add(t);});n.put("Expected",expectedList);return n;}

  static Entry load(CompoundTag n){var a=new Entry(n.getUUID("Id"),BlockPos.of(n.getLong("Center")),n.getInt("Rotation"));try{a.status=Status.valueOf(n.getString("Status"));}catch(Exception ex){a.status=Status.BLOCKED;}a.cursor=n.getInt("Cursor");a.hatchStage=n.getInt("HatchStage");a.wonAt=n.getLong("WonAt");a.debug=n.getBoolean("Debug");a.lootInstalled=n.getBoolean("LootInstalled");if(n.hasUUID("Owner"))a.owner=n.getUUID("Owner");if(n.hasUUID("Boss"))a.boss=n.getUUID("Boss");if(n.hasUUID("Hero"))a.hero=n.getUUID("Hero");if(n.hasUUID("Standby"))a.standby=n.getUUID("Standby");for(var p:n.getList("Participants",Tag.TAG_INT_ARRAY))a.participants.add(NbtUtils.loadUUID(p));for(var raw:n.getList("Expected",Tag.TAG_COMPOUND)){var v=(CompoundTag)raw;a.expected.put(v.getLong("Pos"),v.getString("State"));}return a;}

 }

 public final Map<UUID,Entry> yards=new LinkedHashMap<>();public final Map<UUID,UUID> heroes=new HashMap<>();public final Map<UUID,CompoundTag> progress=new HashMap<>();

 private final Set<UUID> jobs=new HashSet<>();private boolean restore;

 public static WitchYard get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(WitchYard::new,WitchYard::load),"slavicmyths_witch_yard_172");}

 public Entry lock(UUID owner){return yards.values().stream().filter(e->e.status==Status.COMBAT&&(owner.equals(e.owner)||e.participants.contains(owner))).findFirst().orElse(null);}

 public Entry near(Vec3 p){return yards.values().stream().filter(e->e.radius(p)<=43&&Math.abs(e.center.getY()-p.y)<20).findFirst().orElse(null);}

 public static WitchYard load(CompoundTag n,HolderLookup.Provider provider){var d=new WitchYard();for(var t:n.getList("Yards",Tag.TAG_COMPOUND)){var a=Entry.load((CompoundTag)t);d.yards.put(a.id,a);if(a.status==Status.BUILDING||a.status==Status.WON)d.jobs.add(a.id);}for(var t:n.getList("Heroes",Tag.TAG_COMPOUND)){var a=(CompoundTag)t;d.heroes.put(a.getUUID("Owner"),a.getUUID("Entity"));}for(var t:n.getList("Progress",Tag.TAG_COMPOUND)){var a=(CompoundTag)t;d.progress.put(a.getUUID("Player"),a.copy());}d.restore=true;return d;}

 @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider provider){n.putInt("Schema",1);var a=new ListTag();yards.values().forEach(e->a.add(e.save()));n.put("Yards",a);var h=new ListTag();heroes.forEach((p,id)->{var t=new CompoundTag();t.putUUID("Owner",p);t.putUUID("Entity",id);h.add(t);});n.put("Heroes",h);var v=new ListTag();progress.values().forEach(t->v.add(t.copy()));n.put("Progress",v);return n;}

 public static DobrynyaEntity hero(ServerPlayer p){var id=get(p.serverLevel().getServer().overworld()).heroes.get(p.getUUID());return id!=null&&p.serverLevel().getEntity(id) instanceof DobrynyaEntity h?h:null;}

 public static boolean call(ServerPlayer p){if(!p.isAlive()||p.isSpectator()||!StoryPolicy.canSummon(p,"dobrynya_nikitich"))return false;var w=p.serverLevel();var d=get(w.getServer().overworld());if(d.heroes.containsKey(p.getUUID())){p.sendSystemMessage(Component.translatable("msg.slavicmyths.dobrynya_duplicate"));return false;}if(!org.slavicmyths.skim.HeroCalls.available(p,"dobrynya_nikitich"))return false;boolean poloz=org.slavicmyths.poloz.PolozEvent.active(w);if(org.slavicmyths.skim.HeroCalls.count(p)>=(poloz?3:2))return false;

  // Existing lore-book discovery is the prerequisite, not a new reward/quest framework.

  if(!org.slavicmyths.progression.Knowledge.knows(p,"first_book_entry")&&!p.getInventory().contains(new ItemStack(org.slavicmyths.registry.ModItems.LORE_BOOK.get()))&&!p.hasPermissions(2)){p.sendSystemMessage(Component.translatable("msg.slavicmyths.story_missing_dobrynya"));return false;}

  var at=org.slavicmyths.bogatyr.BogatyrLedger.safe(w,p.position(),.8,2.2,6);if(at==null)return false;var h=MarinkaContent.DOBRYNYA.get().create(w);h.owner=p.getUUID();h.moveTo(at.x,at.y,at.z,p.getYRot(),0);if(!w.addFreshEntity(h))return false;d.heroes.put(p.getUUID(),h.getUUID());h.bark(1);d.setDirty();org.slavicmyths.skim.HeroCalls.summoned(p,"dobrynya_nikitich",h);return true;}

 private static boolean natural(ServerLevel w,BlockPos p){if(!w.getFluidState(p).isEmpty()||w.getBlockEntity(p)!=null)return false;var b=w.getBlockState(p);return b.isAir()||b.canBeReplaced()||b.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)||b.is(net.minecraft.tags.BlockTags.DIRT)||b.is(Blocks.GRAVEL)||b.is(Blocks.SAND)||b.is(Blocks.DEEPSLATE);}

 public static boolean place(ServerPlayer p,BlockPos center,int rotation){var w=p.serverLevel();var d=get(w);if(w.dimension()!=net.minecraft.world.level.Level.OVERWORLD||center.getY()<w.getMinBuildHeight()+12||center.getY()+14>=w.getMaxBuildHeight()||d.yards.values().stream().anyMatch(e->e.radius(center.getCenter())<86))return false;var entry=new Entry(UUID.randomUUID(),center,rotation);var plan=YardBlueprint.plan();

  for(int x=-30;x<30;x++)for(int z=-30;z<30;z++){var at=entry.at(x,0,z);if(!w.hasChunkAt(at)||!w.getWorldBorder().isWithinBounds(at)||!w.mayInteract(p,at)||!natural(w,at)||!natural(w,at.below())||!natural(w,at.below(2))||!w.getBlockState(at.below(3)).isCollisionShapeFullBlock(w,at.below(3)))return false;for(int y=1;y<=14;y++)if(!w.getFluidState(at.above(y)).isEmpty()||(!w.getBlockState(at.above(y)).isAir()&&!w.getBlockState(at.above(y)).canBeReplaced()))return false;}

  for(var cell:plan){var at=entry.at(cell.local().getX(),cell.local().getY(),cell.local().getZ());if(!w.hasChunkAt(at)||!natural(w,at)||!w.mayInteract(p,at))return false;if(cell.local().getY()<0&&net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(w,at,w.getBlockState(at),p)).isCanceled())return false;entry.expected.put(at.asLong(),w.getBlockState(at).toString());}

  // Protection hook: one break check per surface column plus basement footprint, before mutation.

  for(int x=-30;x<30;x++)for(int z=-30;z<30;z++){var at=entry.at(x,0,z);if(net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(w,at,w.getBlockState(at),p)).isCanceled())return false;}

  d.yards.put(entry.id,entry);d.jobs.add(entry.id);d.setDirty();p.sendSystemMessage(Component.translatable("msg.slavicmyths.yard_building",center.getX(),center.getY()+1,center.getZ()));return true;}

 public static boolean start(ServerPlayer p,BlockPos seal,boolean debug){var w=p.serverLevel();var d=get(w);var e=d.near(seal.getCenter());if(e==null||!e.at(0,1,0).equals(seal)||e.status!=Status.READY||p.distanceToSqr(seal.getCenter())>20.25||!p.isAlive()||p.isSpectator()||d.lock(p.getUUID())!=null||org.slavicmyths.bogatyr.BogatyrLedger.active(p))return false;var h=hero(p);if(h==null||!h.isAlive()||h.owner==null||!h.owner.equals(p.getUUID())||h.distanceToSqr(seal.getCenter())>144){p.sendSystemMessage(Component.translatable("msg.slavicmyths.story_missing_dobrynya"));return false;}

  var b=MarinkaContent.MARINKA.get().create(w);var at=e.at(-3,1,-4);if(!w.hasChunkAt(at))return false;b.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5);if(!w.noCollision(b))return false;e.owner=p.getUUID();e.hero=h.getUUID();e.boss=b.getUUID();e.debug=debug;e.status=Status.COMBAT;e.participants.clear();e.participants.add(p.getUUID());

  var ilya=org.slavicmyths.bogatyr.BogatyrLedger.hero(p);if(ilya!=null){var safe=org.slavicmyths.bogatyr.BogatyrLedger.safe(w,e.at(0,1,35).getCenter(),.93,2.45,5);if(safe==null){e.status=Status.READY;e.owner=null;return false;}ilya.moveTo(safe.x,safe.y,safe.z);ilya.getNavigation().stop();ilya.setTarget(null);e.standby=ilya.getUUID();}

  h.bind(e);b.arena=e.id;b.owner=p.getUUID();b.encounter=UUID.randomUUID();b.debug=debug;b.active=true;b.begin(WitchAction.INTRO);if(!w.addFreshEntity(b)){e.status=Status.READY;release(w,e);return false;}d.setDirty();return true;}

 public static void release(ServerLevel w,Entry e){if(e.hero!=null&&w.getEntity(e.hero) instanceof DobrynyaEntity h)h.endEncounter();if(e.standby!=null&&w.getEntity(e.standby) instanceof org.slavicmyths.bogatyr.IlyaEntity h){h.getNavigation().stop();h.begin(org.slavicmyths.bogatyr.BogatyrRules.State.FOLLOW,0);}e.standby=null;}

 public void victory(ServerLevel w,Entry e,MarinkaEntity boss){if(e.status!=Status.COMBAT||boss.debug)return;e.status=Status.WON;e.wonAt=w.getGameTime();jobs.add(e.id);for(var id:e.participants){var p=w.getServer().getPlayerList().getPlayer(id);if(p==null||p.isSpectator()||p.isCreative()||p.level()!=w||e.radius(p.position())>43||!boss.contributed(id))continue;var n=progress.computeIfAbsent(id,k->new CompoundTag());n.putUUID("Player",id);n.putBoolean("marinka_defeated",true);n.putBoolean("marinka_participation",true);n.putBoolean("dobrynya_released",boss.rescued||(e.hero!=null&&w.getEntity(e.hero) instanceof DobrynyaEntity h&&h.isAlive()));n.putBoolean("dobrynya_anchor_broken",boss.rescued);n.putBoolean("eligibleForFutureRewards",true);n.putLong("DefeatedAt",w.getGameTime());n.putUUID("LastArena",e.id);n.putInt("DefeatCount",n.getInt("DefeatCount")+1);org.slavicmyths.rewards.RewardLedger.victory(p,1);p.sendSystemMessage(Component.translatable("msg.slavicmyths.basement_open"));}release(w,e);setDirty();}

 public static void fail(ServerLevel w,Entry e){if(e.status!=Status.COMBAT)return;if(e.boss!=null&&w.getEntity(e.boss) instanceof MarinkaEntity b)b.cleanup();e.status=Status.READY;release(w,e);get(w).setDirty();}

 @SubscribeEvent public static void tick(ServerTickEvent.Post event){for(var w:event.getServer().getAllLevels()){var d=get(w);if(d.restore){for(var e:d.yards.values())if(e.status==Status.COMBAT)fail(w,e);d.restore=false;}for(var id:new ArrayList<>(d.jobs)){var e=d.yards.get(id);if(e==null){d.jobs.remove(id);continue;}if(e.status==Status.BUILDING){var plan=YardBlueprint.plan();int budget=256;while(budget-->0&&e.cursor<plan.size()){var c=plan.get(e.cursor);var at=e.at(c.local().getX(),c.local().getY(),c.local().getZ());if(!w.hasChunkAt(at))break;if(!w.getBlockState(at).toString().equals(e.expected.get(at.asLong()))&&!w.getBlockState(at).equals(c.state())){e.status=Status.BLOCKED;break;}w.setBlock(at,c.state(),3);e.cursor++;}if(e.cursor>=plan.size()){if(!w.hasChunkAt(e.at(-3,-9,-9))||!w.hasChunkAt(e.at(3,-9,-9)))continue;if(!(w.getBlockEntity(e.at(-3,-9,-9)) instanceof ChestBlockEntity)||!(w.getBlockEntity(e.at(3,-9,-9)) instanceof ChestBlockEntity)){e.status=Status.BLOCKED;}else{e.status=Status.READY;e.expected.clear();installLoot(w,e);}}if(e.status!=Status.BUILDING)d.jobs.remove(id);d.setDirty();}

    else if(e.status==Status.WON){long elapsed=w.getGameTime()-e.wonAt;int stage=elapsed>=44?4:elapsed>=36?3:elapsed>=24?2:elapsed>=12?1:0;if(stage>e.hatchStage&&w.hasChunkAt(e.center)){e.hatchStage=stage;if(stage>=2)w.sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK,Blocks.CHISELED_DEEPSLATE.defaultBlockState()),e.center.getX()+.5,e.center.getY()+1.05,e.center.getZ()+.5,16,.7,.1,.7,.05);if(stage>=3&&w.getBlockState(e.at(0,1,0)).is(MarinkaContent.SEAL.get()))w.setBlock(e.at(0,1,0),Blocks.AIR.defaultBlockState(),3);w.playSound(null,e.center,MarinkaContent.sound("arena.ritual_circle_pulse_01"),net.minecraft.sounds.SoundSource.BLOCKS,.4F,.8F);d.setDirty();}if(elapsed<56)continue;boolean loaded=true;for(int x=0;x<2;x++)for(int z=0;z<3;z++)loaded&=w.hasChunkAt(e.at(x,0,z));if(!loaded)continue;for(int x=0;x<2;x++)for(int z=0;z<3;z++){var at=e.at(x,0,z);if(w.getBlockState(at).is(Blocks.CHISELED_DEEPSLATE))w.setBlock(at,Blocks.AIR.defaultBlockState(),3);for(int y=1;y<=2;y++)if(w.getBlockState(at.above(y)).is(MarinkaContent.SEAL.get())||w.getBlockState(at.above(y)).is(Blocks.DEEPSLATE_BRICK_SLAB))w.setBlock(at.above(y),Blocks.AIR.defaultBlockState(),3);}e.status=Status.OPEN;d.jobs.remove(id);w.playSound(null,e.center,MarinkaContent.sound("arena.hatch_open_01"),net.minecraft.sounds.SoundSource.BLOCKS,1,1);d.setDirty();}

   }}}

 @SubscribeEvent public static void discover(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event){if(event.getEntity() instanceof ServerPlayer p){var d=get(p.serverLevel());var e=d.near(event.getPos().getCenter());if(e!=null&&e.status==Status.OPEN&&event.getPos().getY()<e.center.getY()-4){var n=d.progress.computeIfAbsent(p.getUUID(),k->new CompoundTag());n.putUUID("Player",p.getUUID());n.putBoolean("basement_discovered",true);n.putBoolean("marinka_basement_discovered",true);d.setDirty();}}}

 private static ItemStack notes(int side){var stack=new ItemStack(Items.WRITTEN_BOOK);var pages=new ArrayList<net.minecraft.server.network.Filterable<Component>>();for(int i=side<0?1:3;i<=(side<0?2:4);i++)pages.add(net.minecraft.server.network.Filterable.passThrough(Component.translatable("lore.slavicmyths.172.note_"+i)));stack.set(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT,new net.minecraft.world.item.component.WrittenBookContent(net.minecraft.server.network.Filterable.passThrough(side<0?"Записи мастерской":"Записи убежища"),"Маринка",0,pages,true));return stack;}

 private static void installLoot(ServerLevel w,Entry e){if(e.lootInstalled)return;for(int x:new int[]{-3,3}){var at=e.at(x,-9,-9);if(w.getBlockEntity(at) instanceof ChestBlockEntity c){c.setItem(0,new ItemStack(Items.CANDLE,3));c.setItem(2,new ItemStack(Items.BONE,2));c.setItem(5,new ItemStack(Items.REDSTONE,3));c.setItem(7,new ItemStack(Items.GLOWSTONE_DUST,2));c.setItem(12,notes(x));if(x==3)c.setItem(10,new ItemStack(Items.EMERALD));c.setChanged();}}e.lootInstalled=true;}

}

