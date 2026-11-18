package org.slavicmyths.losthalls;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import org.slavicmyths.rpg.*;
import org.slavicmyths.registry.ModItems;
/** Own hidden lease; never creates/removes vanilla potion effects or copied inventory. */
@EventBusSubscriber(modid="slavicmyths")
public final class ShadowFace {
 public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Item> RUNE=ModItems.ITEMS.register("rune_shadow_face",()->new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
 public static void init(){}
 public static boolean equipped(net.minecraft.world.entity.player.Player p){return Runes.has(p.getItemBySlot(EquipmentSlot.CHEST),"shadow_face");}
 private static CompoundTag data(ServerPlayer p){var d=PathData.data(p);if(!d.contains("ShadowFace",10))d.put("ShadowFace",new CompoundTag());return d.getCompound("ShadowFace");}
 public static void activate(ServerPlayer p){var d=data(p);long now=RareRuneRuntime.now(p);if(d.getBoolean("Hidden")){exit(p,true);return;}if(!equipped(p)||d.getLong("Ready")>now||!p.isAlive()||p.isSpectator())return;d.putBoolean("BeforeHidden",p.isInvisible());d.putBoolean("Hidden",true);d.putLong("Until",now+80);p.setInvisible(true);p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,p.getX(),p.getY()+1,p.getZ(),12,.4,.6,.4,.01);}
 private static void exit(ServerPlayer p,boolean spawn){var d=data(p);if(!d.getBoolean("Hidden"))return;d.putBoolean("Hidden",false);p.setInvisible(d.getBoolean("BeforeHidden")||p.hasEffect(MobEffects.INVISIBILITY));d.putLong("Ready",RareRuneRuntime.now(p)+1500);RareRuneRuntime.data(p).putLong("CD_shadow_face",d.getLong("Ready"));RareRuneRuntime.refresh(p);d.remove("Until");d.remove("BeforeHidden");if(!spawn||!p.isAlive())return;
  ListTag ids=new ListTag();List<Integer> order=new ArrayList<>();for(int i=0;i<12;i++)order.add(i);Collections.shuffle(order,new Random(p.getRandom().nextLong()));int made=0;
  for(int i:order){if(made==3)break;double a=i*Math.PI/6+p.getYRot()*Math.PI/180;Vec3 at=p.position().add(Math.cos(a)*2.5,0,Math.sin(a)*2.5);BlockPos feet=BlockPos.containing(at);var s=p.serverLevel();var box=p.getBoundingBox().move(at.subtract(p.position()));boolean safe=true;
   for(BlockPos q:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY-1,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))if(!s.hasChunkAt(q)||!s.getWorldBorder().isWithinBounds(q)||!s.getFluidState(q).isEmpty()){safe=false;break;}
   if(!safe||!s.getBlockState(feet.below()).isFaceSturdy(s,feet.below(),net.minecraft.core.Direction.UP)||!s.noCollision(p,box)||!s.getEntities(p,box).isEmpty())continue;
   var copy=ShadowDecoy.TYPE.get().create(s);if(copy==null)continue;copy.owner(p.getUUID());copy.moveTo(at.x,at.y,at.z,p.getYRot(),0);if(s.addFreshEntity(copy)){ids.add(NbtUtils.createUUID(copy.getUUID()));made++;}
  }d.put("Copies",ids);if(made<3)p.displayClientMessage(net.minecraft.network.chat.Component.translatable("halls.shadow.partial",made),true);
 }
 private static void clearCopies(ServerPlayer p){var d=data(p);for(Tag raw:d.getList("Copies",11)){UUID id=NbtUtils.loadUUID(raw);var e=p.serverLevel().getEntity(id);if(e instanceof ShadowDecoy c&&p.getUUID().equals(c.owner()))e.discard();}d.remove("Copies");}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;var paths=PathData.data(p);if(!paths.contains("ShadowFace",10))return;var d=data(p);if(!d.getBoolean("Hidden"))return;if(!p.isAlive()||!equipped(p)){exit(p,false);clearCopies(p);}else if(d.getLong("Until")<=RareRuneRuntime.now(p))exit(p,true);else p.setInvisible(true);}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){exit(p,false);clearCopies(p);}}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){exit(p,false);clearCopies(p);}}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){exit(p,false);clearCopies(p);}}
 @SubscribeEvent public static void target(LivingChangeTargetEvent e){if(!(e.getEntity() instanceof Mob mob)||!(e.getNewAboutToBeSetTarget() instanceof ServerPlayer p)||mob instanceof HallsMob h&&(h.kind==HallsContent.Kind.EKHOVIK||h.kind==HallsContent.Kind.GRIBOSHMYG||h.isTame()))return;
  if(!(mob instanceof net.minecraft.world.entity.monster.Enemy)&&!(mob instanceof HallsMob)&&!(mob instanceof PostenMob))return;
  if(!PathData.data(p).contains("ShadowFace",10))return;var d=data(p);if(mob.getRandom().nextFloat()>=.7F)return;List<ShadowDecoy> visible=new ArrayList<>();for(Tag raw:d.getList("Copies",11)){var entity=p.serverLevel().getEntity(NbtUtils.loadUUID(raw));if(entity instanceof ShadowDecoy c&&c.isAlive()&&mob.distanceToSqr(c)<24*24&&mob.hasLineOfSight(c))visible.add(c);}if(!visible.isEmpty())e.setNewAboutToBeSetTarget(visible.get(mob.getRandom().nextInt(visible.size())));
 }
 @SubscribeEvent public static void death(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p){exit(p,false);clearCopies(p);}}
 private ShadowFace(){}
}
