package org.slavicmyths.finist;
import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slavicmyths.entity.WildlifeEntity;
import org.slavicmyths.item.FolkAccessoryItem;
import org.slavicmyths.military.GreyWolfStory;
import org.slavicmyths.rpg.PathData;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
/** Player-owned cooldown; item copies and reconnects never reset it. Existing wolves/AI are reused. */
public final class GreyPackRing extends Item implements ICurioItem {
 public GreyPackRing(Properties p){super(p.stacksTo(1));}
 public boolean canEquip(SlotContext c,ItemStack s){return c.identifier().equals("ring");}
 public static net.minecraft.nbt.CompoundTag data(ServerPlayer p){var d=PathData.data(p);if(!d.contains("GreyPack",10))d.put("GreyPack",new net.minecraft.nbt.CompoundTag());return d.getCompound("GreyPack");}
 public static ItemStack equipped(ServerPlayer p){var inv=CuriosApi.getCuriosInventory(p).orElse(null);if(inv==null)return ItemStack.EMPTY;var slots=inv.getStacksHandler("ring").orElse(null);if(slots==null)return ItemStack.EMPTY;for(int i=0;i<Math.min(8,slots.getSlots());i++){var s=slots.getStacks().getStackInSlot(i);if(inv.isSlotActive("ring",i)&&s.is(FinistItems.RING.get()))return s;}return ItemStack.EMPTY;}
 public static long now(ServerPlayer p){return p.getServer().overworld().getGameTime();}
 public static boolean activate(ServerPlayer p){var d=data(p);long now=now(p);if(!p.isAlive()||p.isSpectator()||equipped(p).isEmpty())return false;if(d.getLong("Ready")>now){p.displayClientMessage(GreyWolfStory.text("ring_cooldown",(d.getLong("Ready")-now+19)/20),true);return false;}
  var spots=spots(p);if(spots.size()<2){p.displayClientMessage(GreyWolfStory.text("no_space"),true);return false;}
  d.putLong("Ready",now+1800);d.putLong("SpawnAt",now+10);d.putLong("Expires",now+810);d.putString("Dimension",p.level().dimension().location().toString());d.putLong("Spot0",spots.get(0).asLong());d.putLong("Spot1",spots.get(1).asLong());d.putUUID("Activation",UUID.randomUUID());sound(p,"ring_activate",.65F);p.serverLevel().playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.WOLF_AMBIENT,net.minecraft.sounds.SoundSource.PLAYERS,.18F,.85F);return true;
 }
 public static List<BlockPos> spots(ServerPlayer p){var w=p.serverLevel();var list=new ArrayList<BlockPos>();for(int i=0;i<24;i++){double a=i*Math.PI/12,r=2.5+(i%3)*.5;var xz=p.blockPosition().offset((int)Math.round(Math.cos(a)*r),0,(int)Math.round(Math.sin(a)*r));if(!w.hasChunkAt(xz))continue;for(int dy:new int[]{0,1,-1,2,-2}){var pos=xz.offset(0,dy,0);if(!org.slavicmyths.wolf.WolfEncounter.safeWolf(w,pos)||list.stream().anyMatch(q->q.distSqr(pos)<4))continue;list.add(pos);break;}if(list.size()==2)break;}return list;}
 public static void tick(ServerPlayer p){if(!PathData.data(p).contains("GreyPack",10))return;var d=data(p);long now=now(p);if(!d.contains("SpawnAt"))return;if(now>=d.getLong("Expires")||!p.isAlive()||p.isSpectator()||!d.getString("Dimension").equals(p.level().dimension().location().toString())){d.remove("SpawnAt");return;}if(now<d.getLong("SpawnAt"))return;d.remove("SpawnAt");var w=p.serverLevel();var made=new ArrayList<WildlifeEntity>();
  for(int i=0;i<2;i++){var pos=BlockPos.of(d.getLong("Spot"+i));if(!org.slavicmyths.wolf.WolfEncounter.safeWolf(w,pos)){made.forEach(Entity::discard);return;}var wolf=org.slavicmyths.registry.ModEntities.FOREST_WOLF.get().create(w);wolf.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20);wolf.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(6);wolf.setHealth(20);wolf.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,p.getYRot(),0);var tag=wolf.getPersistentData();tag.putBoolean("GreyPackAlly",true);tag.putUUID("GreyPackOwner",p.getUUID());tag.putUUID("GreyPackActivation",d.getUUID("Activation"));tag.putLong("GreyPackExpires",d.getLong("Expires"));made.add(wolf);}
  for(var wolf:made)if(!w.addFreshEntity(wolf)){made.forEach(Entity::discard);return;}sound(p,"ring_pack_arrive",.45F);for(var wolf:made)w.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,wolf.getX(),wolf.getY()+.4,wolf.getZ(),3,.12,.1,.12,0);
 }
 public static boolean enemy(ServerPlayer owner,LivingEntity target){if(!owner.isAlive()||owner.isSpectator()||target==null||target==owner||!target.isAlive()||target.level()!=owner.level()||target instanceof org.slavicmyths.wolf.GreyWolf||target.getType().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","combat_protected")))||target.getPersistentData().getBoolean("GreyPackAlly")||target.isAlliedTo(owner)||target instanceof net.minecraft.world.entity.TamableAnimal pet&&pet.isTame())return false;
  if(target instanceof Player p)return !p.isCreative()&&!p.isSpectator()&&owner.getServer()!=null&&owner.getServer().isPvpAllowed()&&owner.canHarmPlayer(p)&&org.slavicmyths.combat.CombatTargeting.canAttack(owner,target);return target instanceof net.minecraft.world.entity.monster.Enemy||target.getType().is(org.slavicmyths.military.Military.HOSTILES)||target instanceof org.slavicmyths.kurgan.KurganCreature||target instanceof org.slavicmyths.hunt.WorldBoss;
 }
 public static LivingEntity target(ServerPlayer p,WildlifeEntity wolf){var d=data(p);long now=now(p);if(d.hasUUID("Enemy")&&d.getLong("EnemyUntil")>now){var e=p.serverLevel().getEntity(d.getUUID("Enemy"));if(e instanceof LivingEntity living&&enemy(p,living)&&wolf.distanceToSqr(living)<=400&&wolf.hasLineOfSight(living))return living;}return null;}
 public static void attacked(ServerPlayer p,LivingEntity target){if(!PathData.data(p).contains("GreyPack",10)&&equipped(p).isEmpty())return;if(!enemy(p,target))return;var d=data(p);d.putUUID("Enemy",target.getUUID());d.putLong("EnemyUntil",now(p)+160);}
 public static void sound(ServerPlayer p,String name,float volume){p.serverLevel().playSound(null,p.blockPosition(),FinistSounds.get(name),net.minecraft.sounds.SoundSource.PLAYERS,volume,.95F);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){for(String key:List.of("ring.effect","ring.health","ring.duration","ring.cooldown","ring.use"))lines.add(GreyWolfStory.text(key).withStyle(net.minecraft.ChatFormatting.GRAY));}
}
