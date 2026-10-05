package org.slavicmyths.hunt;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.sounds.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.Difficulty;
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.registry.*;
import org.slavicmyths.item.FolkAccessoryItem;
public final class HuntItems extends Item {
 public enum Role{ASH,SPARK,FANG,CLAW,HORN,DETECTOR,POUCH,VESSEL}
 public final Role role;
 public HuntItems(Properties p,Role role){super(p);this.role=role;}
 @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag f){lines.add(Component.translatable(getDescriptionId()+".effect").withStyle(ChatFormatting.GRAY));if(role==Role.HORN)lines.add(Component.translatable("hunt.target",Component.translatable("entity.slavicmyths."+org.slavicmyths.item.ItemState.target(stack).id)).withStyle(ChatFormatting.DARK_RED));}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){ItemStack stack=p.getItemInHand(hand);if(role!=Role.HORN&&role!=Role.DETECTOR&&role!=Role.POUCH)return super.use(w,p,hand);if(w.isClientSide)return InteractionResultHolder.success(stack);
  if(role==Role.HORN){if(p.isShiftKeyDown()){HuntTarget next=org.slavicmyths.item.ItemState.target(stack).next();org.slavicmyths.item.ItemState.target(stack,next);p.displayClientMessage(Component.translatable("hunt.target",Component.translatable("entity.slavicmyths."+next.id)),true);}else summon((ServerPlayer)p,org.slavicmyths.item.ItemState.target(stack),false);return InteractionResultHolder.consume(stack);}
  if(role==Role.DETECTOR){if(p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(stack);p.getCooldowns().addCooldown(this,400);detect(p);return InteractionResultHolder.consume(stack);}
  if(role==Role.POUCH){if(!p.containerMenu.getClass().equals(net.minecraft.world.inventory.InventoryMenu.class))return InteractionResultHolder.fail(stack);PouchMenu.open((ServerPlayer)p,hand);return InteractionResultHolder.consume(stack);}return super.use(w,p,hand);
 }
 private static boolean fail(ServerPlayer p,String reason,Object...args){p.displayClientMessage(Component.translatable("hunt.fail."+reason,args),true);p.level().playSound(null,p.blockPosition(),SoundEvents.BONE_BLOCK_HIT,SoundSource.PLAYERS,.25F,.6F);return false;}
 public static boolean summon(ServerPlayer p,boolean oven,boolean debug){return summon(p,oven?HuntTarget.OVINNIK:HuntTarget.VOLKOLAK,debug);}
 public static boolean summon(ServerPlayer p,HuntTarget kind,boolean debug){ServerLevel w=p.serverLevel();if(w.dimension()!=Level.OVERWORLD)return fail(p,"dimension");HuntRecords records=HuntRecords.get(w);if(records.active(p.getUUID()))return fail(p,"active");HuntRecords.Hunt previous=records.hunts.get(p.getUUID());if(!debug&&previous!=null&&previous.ready>w.getGameTime())return fail(p,"cooldown",(previous.ready-w.getGameTime()+19)/20);
  boolean elemental=kind==HuntTarget.FIRE_SERPENT||kind==HuntTarget.PODVEY;
  if(!debug){if(w.getDifficulty()==Difficulty.PEACEFUL)return fail(p,"peaceful");if(kind!=HuntTarget.PODVEY&&w.isDay())return failKey(p,kind==HuntTarget.FIRE_SERPENT?"hunt.fire_serpent.day":"hunt.fail.daytime");if(p.isInWater())return fail(p,"water");if(elemental&&!w.canSeeSky(p.blockPosition()))return failKey(p,"hunt."+kind.id+".no_open_sky");if(kind==HuntTarget.PODVEY&&(!HuntSpawns.openBiome(w,p.blockPosition())||!HuntSpawns.airVolume(w,p.blockPosition(),2,4)))return failKey(p,"hunt.podvey.biome");if(kind==HuntTarget.VOLKOLAK&&!HuntSpawns.forest(w,p.blockPosition()))return fail(p,"biome");if(kind==HuntTarget.OVINNIK&&!HuntSpawns.environment(w,p.blockPosition(),18))return fail(p,"environment");}
  if(!HuntSpawns.alone(w,p.blockPosition(),kind))return fail(p,"nearby");BlockPos spot=null;int min=kind==HuntTarget.FIRE_SERPENT?20:kind==HuntTarget.PODVEY?14:12,max=kind==HuntTarget.FIRE_SERPENT?36:kind==HuntTarget.PODVEY?26:24;
  for(int attempt=0;attempt<(elemental?40:32);attempt++){double angle=p.getRandom().nextDouble()*Math.PI*2,radius=min+p.getRandom().nextInt(max-min+1);BlockPos xz=p.blockPosition().offset((int)Math.round(Math.cos(angle)*radius),0,(int)Math.round(Math.sin(angle)*radius));if(!w.hasChunkAt(xz))continue;BlockPos ground=w.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,xz);BlockPos q=kind==HuntTarget.FIRE_SERPENT?ground.above(4+p.getRandom().nextInt(5)):ground;
   double dist=(q.getX()-p.getX())*(q.getX()-p.getX())+(q.getZ()-p.getZ())*(q.getZ()-p.getZ());if(dist<min*min||dist>max*max||!w.getWorldBorder().isWithinBounds(q.offset(-2,0,-2))||!w.getWorldBorder().isWithinBounds(q.offset(2,0,2))||!HuntSpawns.safe(w,q,ground,kind)||org.slavicmyths.kurgan.BurialRecords.get(w).at(ground)!=null||!HuntSpawns.alone(w,q,kind))continue;spot=q;break;}
  if(spot==null)return failKey(p,kind==HuntTarget.FIRE_SERPENT?"hunt.fire_serpent.no_air_space":kind==HuntTarget.PODVEY?"hunt.podvey.position":"hunt.fail.position");HuntMob mob=HuntSpawns.create(w,kind);if(mob==null)return fail(p,"position");mob.home=spot;mob.owner=p.getUUID();mob.moveTo(spot.getX()+.5,spot.getY(),spot.getZ()+.5,0,0);mob.setPersistenceRequired();mob.setTarget(p);
  if(!w.addFreshEntity(mob))return fail(p,"position");records.hunts.put(p.getUUID(),new HuntRecords.Hunt(mob.getUUID(),spot,kind,w.getGameTime()+6000));records.setDirty();org.slavicmyths.navigation.NavigationManager.remember(mob);var navigationEncounter=org.slavicmyths.navigation.NavigationRecords.get(w).encounters.get(mob.getUUID());if(navigationEncounter!=null)org.slavicmyths.navigation.NavigationManager.assign(p,navigationEncounter,"hunt:"+mob.getUUID(),null,0,false);w.playSound(null,p.blockPosition(),ModSounds.HUNT_HORN.get(),SoundSource.PLAYERS,2.5F,.7F);if(elemental)w.sendParticles(kind==HuntTarget.FIRE_SERPENT?net.minecraft.core.particles.ParticleTypes.FLAME:net.minecraft.core.particles.ParticleTypes.CLOUD,mob.getX(),mob.getY()+1,mob.getZ(),12,.7,.5,.7,.02);FolkAccessoryItem.award(p,"hunt_begins");return true;
 }
 private static boolean failKey(ServerPlayer p,String key){p.displayClientMessage(Component.translatable(key),true);p.level().playSound(null,p.blockPosition(),SoundEvents.BONE_BLOCK_HIT,SoundSource.PLAYERS,.25F,.6F);return false;}
 public static void detect(Player p){List<net.minecraft.world.entity.LivingEntity> candidates=p.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,p.getBoundingBox().inflate(192),e->HuntTags.detects(e,p.distanceToSqr(e)));candidates.sort(Comparator.comparingDouble(p::distanceToSqr));p.level().playSound(null,p.blockPosition(),SoundEvents.WOODEN_BUTTON_CLICK_ON,SoundSource.PLAYERS,.35F,.8F);
  if(candidates.isEmpty()){p.displayClientMessage(Component.translatable("hunt.no_trace"),false);return;}net.minecraft.world.entity.LivingEntity target=candidates.get(0);Vec3 d=target.position().subtract(p.position());String[] directions={"east","southeast","south","southwest","west","northwest","north","northeast"};int sector=Math.floorMod((int)Math.round(Math.atan2(d.z,d.x)/(Math.PI/4)),8);int distance=(int)Math.round(Math.sqrt(p.distanceToSqr(target))/10)*10;p.displayClientMessage(Component.translatable("hunt.trace",target.getDisplayName(),Component.translatable("hunt.direction."+directions[sector]),distance),false);
  boolean same=target instanceof HuntMob&&p.getUUID().equals(((HuntMob)target).owner);if(p.distanceToSqr(target)<=48*48&&(same||p.hasLineOfSight(target)))target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING,100));
 }
}
