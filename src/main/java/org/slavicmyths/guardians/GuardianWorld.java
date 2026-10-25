package org.slavicmyths.guardians;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.sanctuary.*;
@EventBusSubscriber(modid="slavicmyths")
public final class GuardianWorld {
 public static Rotation rotation(CompoundTag n){try{return Rotation.valueOf(n.getString("Rotation"));}catch(IllegalArgumentException bad){return Rotation.NONE;}}
 public static BlockPos at(CompoundTag n,int x,int y,int z){return BlockPos.of(n.getLong("Origin")).offset(SanctuaryGeometry.rotate(x,y,z,rotation(n)));}
 public static AABB arena(CompoundTag n){int[] a=n.getIntArray("ArenaBounds");return a.length==6?new AABB(a[0],a[1],a[2],a[3]+1,a[4]+1,a[5]+1):new AABB(BlockPos.ZERO);}
 public static AABB chamber(CompoundTag n){int[] a=n.getIntArray("GuardianBounds");return a.length==6?new AABB(a[0],a[1],a[2],a[3]+1,a[4]+1,a[5]+1):new AABB(BlockPos.ZERO);}
 public static BlockPos valveAt(CompoundTag n,int role){return at(n,role==0?70:90,1,20);}
 public static BlockPos ritualAt(CompoundTag n){return n.getBoolean("Spring")?at(n,74,1,10):at(n,65,2,36);}
 public static BlockPos healingSource(CompoundTag n){return at(n,79,1,21);}
 public static boolean loaded(ServerLevel w,AABB box){for(int x=((int)box.minX)>>4;x<=((int)box.maxX)>>4;x++)for(int z=((int)box.minZ)>>4;z<=((int)box.maxZ)>>4;z++)if(w.getChunkSource().getChunkNow(x,z)==null)return false;return true;}
 public static void visit(ServerPlayer p,UUID id,BlockPos origin,Rotation r,boolean spring){var s=SanctuaryRecords.get(p.serverLevel());s.ensure(id,origin,r,spring);ensure(p.serverLevel(),id);}
 public static void ensure(ServerLevel w,UUID id){var d=GuardianLedger.get(w);d.ensure(id,SanctuaryRecords.get(w).row(id));var n=d.row(id);d.unlock(id,SanctuaryRecords.get(w));install(w,id,n);if(w.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL&&!n.getString("Stage").equals("CLEARED")&&!n.getString("Stage").equals("PURIFICATION"))return;if(!n.contains("Origin")||n.getBoolean("Standalone"))return;long now=w.getGameTime();if(n.contains("PopulationCheckAt")&&now<n.getLong("PopulationCheckAt"))return;n.putLong("PopulationCheckAt",now+20);populate(w,id,n);}
 public static void generated(ServerLevel w,UUID id,BlockPos origin,Rotation r,boolean spring){SanctuaryRecords.get(w).ensure(id,origin,r,spring);ensure(w,id);}
 /** Chunk-local population; never loads chunks and does not depend on arena entry. */
 private static void populate(ServerLevel w,UUID id,CompoundTag n){var d=GuardianLedger.get(w);if(w.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL&&!n.getString("Stage").equals("CLEARED")&&!n.getString("Stage").equals("PURIFICATION"))return;if(!loaded(w,chamber(n)))return;
  // UUID and last loaded position prevent treating an unloaded guardian as missing.
  var found=w.getEntitiesOfClass(GuardianEntity.class,chamber(n).inflate(2),e->id.equals(e.instance));
  if(!found.isEmpty()){var keeper=found.stream().filter(e->n.hasUUID("Boss")&&n.getUUID("Boss").equals(e.getUUID())).findFirst().orElse(found.getFirst());for(var e:found)if(e!=keeper)e.discard();n.putUUID("Boss",keeper.getUUID());n.remove("MissingSince");return;}
  if(n.hasUUID("Boss")){if(w.getEntity(n.getUUID("Boss")) instanceof GuardianEntity)return;if(n.contains("LastPosition")&&!w.hasChunkAt(BlockPos.of(n.getLong("LastPosition"))))return;if(!n.contains("MissingSince")){n.putLong("MissingSince",w.getGameTime());d.setDirty();return;}if(w.getGameTime()-n.getLong("MissingSince")<100)return;}
  var boss=n.getBoolean("Spring")?GuardianContent.WATER.get().create(w):GuardianContent.FIRE.get().create(w);if(boss==null)return;var anchor=BlockPos.of(n.getLong("SpawnAnchor"));boss.configure(id,n);boss.setPos(Vec3.atBottomCenterOf(anchor));if(!w.noCollision(boss,boss.getBoundingBox())){return;}if(w.addFreshEntity(boss)){n.putUUID("Boss",boss.getUUID());n.remove("MissingSince");d.setDirty();}
 }
 @SubscribeEvent public static void player(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){if(!(event.getEntity() instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator()||p.tickCount%20!=0||p.serverLevel().dimension()!=net.minecraft.world.level.Level.OVERWORLD)return;var d=GuardianLedger.get(p.serverLevel());for(UUID id:d.near(p.blockPosition()))update(p,id);}
 private static void update(ServerPlayer p,UUID id){var w=p.serverLevel();var d=GuardianLedger.get(w);var n=d.row(id);if(n.getBoolean("Standalone"))return;if(!chamber(n).inflate(5,2,5).contains(p.position()))return;
  boolean spring=n.getBoolean("Spring");var old=SanctuaryRecords.get(w);boolean ready=spring?old.cleansed(id):old.restored(id);
  if(d.stage(id).equals("SEALED")){if(!ready){if(p.tickCount%100==0)p.displayClientMessage(GuardianContent.text(spring?"open_water_hint":"open_fire_hint"),true);return;}d.unlock(id,old);}
  org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:"+(spring?"cleansed_spring_keeper":"cleansed_flame_guardian"),d.stage(id).equals("CLEARED")?4:3,d.stage(id).equals("CLEARED"));install(w,id,n);if(!arena(n).contains(p.position())||!loaded(w,arena(n)))return;
  populate(w,id,n);
 }
 private static void node(ServerLevel w,UUID id,BlockPos p,int role,boolean lit){if(!w.hasChunkAt(p))return;var block=role<0?GuardianContent.PEDESTAL.get():GuardianContent.VALVE.get();var current=w.getBlockState(p);if(current.isAir()||role<0&&current.is(Blocks.CHISELED_STONE_BRICKS))w.setBlockAndUpdate(p,block.defaultBlockState());if(w.getBlockEntity(p) instanceof GuardianNode h&&w.getBlockState(p).is(block)){if(h.instance==null)h.configure(id,role);if(!id.equals(h.instance)||h.role!=role)return;if(w.getBlockState(p).getValue(SanctuaryBlock.LIT)!=lit)w.setBlockAndUpdate(p,w.getBlockState(p).setValue(SanctuaryBlock.LIT,lit));}}
 public static void install(ServerLevel w,UUID id,CompoundTag n){if(n.getBoolean("Standalone")||!n.contains("Origin")||n.getIntArray("ArenaBounds").length!=6||n.getString("Stage").equals("SEALED"))return;boolean spring=n.getBoolean("Spring");openDoor(w,n,SanctuaryContent.block(spring?"seal_spring_door":"seal_fire_door"));
  boolean clean=n.getString("Stage").equals("CLEARED");node(w,id,BlockPos.of(n.getLong("Pedestal")),-1,clean);if(spring)for(int i=0;i<2;i++)node(w,id,valveAt(n,i),i,clean||(n.getInt("Valves")&(1<<i))!=0);
  var p=ritualAt(n);if(w.hasChunkAt(p)&&w.getBlockState(p).isAir()){w.setBlockAndUpdate(p,SanctuaryContent.block(spring?"pure_spring_heart":"unfading_brazier").defaultBlockState().setValue(SanctuaryBlock.LIT,true));if(w.getBlockEntity(p) instanceof SanctuaryHeart heart)heart.configure(id,BlockPos.of(n.getLong("Origin")),rotation(n),spring,-1);}
  if(spring){var source=healingSource(n);if(w.hasChunkAt(source)&&w.getBlockState(source).isAir()){w.setBlockAndUpdate(source,SanctuaryContent.block("pure_spring_heart").defaultBlockState().setValue(SanctuaryBlock.LIT,true));if(w.getBlockEntity(source) instanceof SanctuaryHeart heart)heart.configure(id,BlockPos.of(n.getLong("Origin")),rotation(n),true,-1);}}
 }
 /** Loaded cells only; remove exactly this sanctuary's seal blocks, never player replacements. */
 public static void openDoor(net.minecraft.world.level.LevelAccessor w,CompoundTag n,Block seal){boolean spring=n.getBoolean("Spring");for(int i=-1;i<=1;i++)for(int y=spring?1:2;y<=(spring?5:6);y++){var p=spring?at(n,81+i,y,29):at(n,61,y,43+i);if(w.hasChunkAt(p)&&w.getBlockState(p).is(seal))w.setBlock(p,Blocks.AIR.defaultBlockState(),3);}}
 public static GuardianEntity boss(ServerLevel w,UUID id){var n=GuardianLedger.get(w).row(id);return n.hasUUID("Boss")&&w.getEntity(n.getUUID("Boss")) instanceof GuardianEntity e?e:null;}
 public static boolean ritual(ServerPlayer p,UUID id){var d=GuardianLedger.get(p.serverLevel());if(!d.stage(id).equals("PURIFICATION"))return false;var b=boss(p.serverLevel(),id);if(b!=null)b.finishPurification(p);else p.displayClientMessage(GuardianContent.text("return_guardian"),true);return true;}
 public static void valve(ServerPlayer p,UUID id,int role){if(role<0||role>1)return;var d=GuardianLedger.get(p.serverLevel());var n=d.row(id);var b=boss(p.serverLevel(),id);if(!(b instanceof SpringKeeper keeper)||!keeper.fighting()){p.displayClientMessage(GuardianContent.text("valve_idle"),true);return;}if(n.getInt("Valves")==3){p.displayClientMessage(GuardianContent.text("valve_cooldown"),true);return;}if((n.getInt("Valves")&(1<<role))!=0)return;String key="PendingValve"+role;if(n.contains(key,10))return;var action=new CompoundTag();action.putUUID("Player",p.getUUID());action.putLong("At",p.serverLevel().getGameTime()+GuardianRules.VALVE_USE);n.put(key,action);d.setDirty();p.displayClientMessage(GuardianContent.text("valve_turn"),true);}
 @SubscribeEvent public static void leftBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock e){if(e.getEntity() instanceof ServerPlayer p)prisonAction(p);}
 @SubscribeEvent public static void right(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem e){if(e.getEntity() instanceof ServerPlayer p)prisonAction(p);}
 @SubscribeEvent public static void rightBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock e){if(e.getEntity() instanceof ServerPlayer p)prisonAction(p);}
 @SubscribeEvent public static void attack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p)prisonAction(p);}
 public static void prisonAction(ServerPlayer p){var d=GuardianLedger.get(p.serverLevel());for(UUID id:d.near(p.blockPosition()))if(boss(p.serverLevel(),id) instanceof SpringKeeper keeper)keeper.prisonHit(p);}
 private GuardianWorld(){}
}
