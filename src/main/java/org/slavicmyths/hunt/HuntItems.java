package org.slavicmyths.hunt;
import java.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import org.slavicmyths.registry.*;
import org.slavicmyths.item.FolkAccessoryItem;
public final class HuntItems extends Item {
 public enum Role{ASH,SPARK,FANG,CLAW,HORN,DETECTOR,POUCH,VESSEL}
 public final Role role;
 public HuntItems(Properties p,Role role){super(p);this.role=role;}
 @Override public void appendHoverText(ItemStack stack,World w,List<ITextComponent> lines,net.minecraft.client.util.ITooltipFlag f){lines.add(new TranslationTextComponent(getDescriptionId()+".effect").withStyle(TextFormatting.GRAY));if(role==Role.HORN)lines.add(new TranslationTextComponent("hunt.target",new TranslationTextComponent("entity.slavicmyths."+HuntTarget.read(stack.getOrCreateTag(),"HuntTarget").id)).withStyle(TextFormatting.DARK_RED));}
 @Override public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand hand){ItemStack stack=p.getItemInHand(hand);if(role!=Role.HORN&&role!=Role.DETECTOR&&role!=Role.POUCH)return super.use(w,p,hand);if(w.isClientSide)return ActionResult.success(stack);
  if(role==Role.HORN){if(p.isShiftKeyDown()){HuntTarget next=HuntTarget.read(stack.getOrCreateTag(),"HuntTarget").next();stack.getOrCreateTag().putString("HuntTarget",next.id);p.displayClientMessage(new TranslationTextComponent("hunt.target",new TranslationTextComponent("entity.slavicmyths."+next.id)),true);}else summon((ServerPlayerEntity)p,HuntTarget.read(stack.getOrCreateTag(),"HuntTarget"),false);return ActionResult.consume(stack);}
  if(role==Role.DETECTOR){if(p.getCooldowns().isOnCooldown(this))return ActionResult.fail(stack);p.getCooldowns().addCooldown(this,400);detect(p);return ActionResult.consume(stack);}
  if(role==Role.POUCH){if(!p.containerMenu.getClass().equals(net.minecraft.inventory.container.PlayerContainer.class))return ActionResult.fail(stack);PouchMenu.open((ServerPlayerEntity)p,hand);return ActionResult.consume(stack);}return super.use(w,p,hand);
 }
 private static boolean fail(ServerPlayerEntity p,String reason,Object...args){p.displayClientMessage(new TranslationTextComponent("hunt.fail."+reason,args),true);p.level.playSound(null,p.blockPosition(),SoundEvents.BONE_BLOCK_HIT,SoundCategory.PLAYERS,.25F,.6F);return false;}
 public static boolean summon(ServerPlayerEntity p,boolean oven,boolean debug){return summon(p,oven?HuntTarget.OVINNIK:HuntTarget.VOLKOLAK,debug);}
 public static boolean summon(ServerPlayerEntity p,HuntTarget kind,boolean debug){ServerWorld w=p.getLevel();if(w.dimension()!=World.OVERWORLD)return fail(p,"dimension");HuntRecords records=HuntRecords.get(w);if(records.active(p.getUUID()))return fail(p,"active");HuntRecords.Hunt previous=records.hunts.get(p.getUUID());if(!debug&&previous!=null&&previous.ready>w.getGameTime())return fail(p,"cooldown",(previous.ready-w.getGameTime()+19)/20);
  boolean elemental=kind==HuntTarget.FIRE_SERPENT||kind==HuntTarget.PODVEY;
  if(!debug){if(w.getDifficulty()==Difficulty.PEACEFUL)return fail(p,"peaceful");if(kind!=HuntTarget.PODVEY&&w.isDay())return failKey(p,kind==HuntTarget.FIRE_SERPENT?"hunt.fire_serpent.day":"hunt.fail.daytime");if(p.isInWater())return fail(p,"water");if(elemental&&!w.canSeeSky(p.blockPosition()))return failKey(p,"hunt."+kind.id+".no_open_sky");if(kind==HuntTarget.PODVEY&&(!HuntSpawns.openBiome(w,p.blockPosition())||!HuntSpawns.airVolume(w,p.blockPosition(),2,4)))return failKey(p,"hunt.podvey.biome");if(kind==HuntTarget.VOLKOLAK&&!HuntSpawns.forest(w,p.blockPosition()))return fail(p,"biome");if(kind==HuntTarget.OVINNIK&&!HuntSpawns.environment(w,p.blockPosition(),18))return fail(p,"environment");}
  if(!HuntSpawns.alone(w,p.blockPosition(),kind))return fail(p,"nearby");BlockPos spot=null;int min=kind==HuntTarget.FIRE_SERPENT?20:kind==HuntTarget.PODVEY?14:12,max=kind==HuntTarget.FIRE_SERPENT?36:kind==HuntTarget.PODVEY?26:24;
  for(int attempt=0;attempt<(elemental?40:32);attempt++){double angle=p.getRandom().nextDouble()*Math.PI*2,radius=min+p.getRandom().nextInt(max-min+1);BlockPos xz=p.blockPosition().offset((int)Math.round(Math.cos(angle)*radius),0,(int)Math.round(Math.sin(angle)*radius));if(!w.hasChunkAt(xz))continue;BlockPos ground=w.getHeightmapPos(net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,xz);BlockPos q=kind==HuntTarget.FIRE_SERPENT?ground.above(4+p.getRandom().nextInt(5)):ground;
   double dist=(q.getX()-p.getX())*(q.getX()-p.getX())+(q.getZ()-p.getZ())*(q.getZ()-p.getZ());if(dist<min*min||dist>max*max||!w.getWorldBorder().isWithinBounds(q.offset(-2,0,-2))||!w.getWorldBorder().isWithinBounds(q.offset(2,0,2))||!HuntSpawns.safe(w,q,ground,kind)||org.slavicmyths.kurgan.BurialRecords.get(w).at(ground)!=null||!HuntSpawns.alone(w,q,kind))continue;spot=q;break;}
  if(spot==null)return failKey(p,kind==HuntTarget.FIRE_SERPENT?"hunt.fire_serpent.no_air_space":kind==HuntTarget.PODVEY?"hunt.podvey.position":"hunt.fail.position");HuntMob mob=HuntSpawns.create(w,kind);if(mob==null)return fail(p,"position");mob.home=spot;mob.owner=p.getUUID();mob.moveTo(spot.getX()+.5,spot.getY(),spot.getZ()+.5,0,0);mob.setPersistenceRequired();mob.setTarget(p);
  if(!w.addFreshEntity(mob))return fail(p,"position");records.hunts.put(p.getUUID(),new HuntRecords.Hunt(mob.getUUID(),spot,kind,w.getGameTime()+6000));records.setDirty();w.playSound(null,p.blockPosition(),ModSounds.HUNT_HORN.get(),SoundCategory.PLAYERS,2.5F,.7F);if(elemental)w.sendParticles(kind==HuntTarget.FIRE_SERPENT?net.minecraft.particles.ParticleTypes.FLAME:net.minecraft.particles.ParticleTypes.CLOUD,mob.getX(),mob.getY()+1,mob.getZ(),12,.7,.5,.7,.02);FolkAccessoryItem.award(p,"hunt_begins");return true;
 }
 private static boolean failKey(ServerPlayerEntity p,String key){p.displayClientMessage(new TranslationTextComponent(key),true);p.level.playSound(null,p.blockPosition(),SoundEvents.BONE_BLOCK_HIT,SoundCategory.PLAYERS,.25F,.6F);return false;}
 private void detect(PlayerEntity p){List<net.minecraft.entity.LivingEntity> candidates=p.level.getEntitiesOfClass(net.minecraft.entity.LivingEntity.class,p.getBoundingBox().inflate(128),e->e.isAlive()&&e.getType().is(HuntTags.TARGETS)&&p.distanceToSqr(e)<=128*128);candidates.sort(Comparator.comparingDouble(p::distanceToSqr));p.level.playSound(null,p.blockPosition(),SoundEvents.WOODEN_BUTTON_CLICK_ON,SoundCategory.PLAYERS,.35F,.8F);
  if(candidates.isEmpty()){p.displayClientMessage(new TranslationTextComponent("hunt.no_trace"),false);return;}net.minecraft.entity.LivingEntity target=candidates.get(0);Vector3d d=target.position().subtract(p.position());String[] directions={"east","southeast","south","southwest","west","northwest","north","northeast"};int sector=Math.floorMod((int)Math.round(Math.atan2(d.z,d.x)/(Math.PI/4)),8);int distance=(int)Math.round(Math.sqrt(p.distanceToSqr(target))/10)*10;p.displayClientMessage(new TranslationTextComponent("hunt.trace",target.getDisplayName(),new TranslationTextComponent("hunt.direction."+directions[sector]),distance),false);
  boolean same=target instanceof HuntMob&&p.getUUID().equals(((HuntMob)target).owner);if(p.distanceToSqr(target)<=48*48&&(same||p.canSee(target)))target.addEffect(new net.minecraft.potion.EffectInstance(net.minecraft.potion.Effects.GLOWING,100));
 }
}
