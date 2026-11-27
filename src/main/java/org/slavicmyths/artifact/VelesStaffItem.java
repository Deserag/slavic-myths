package org.slavicmyths.artifact;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import org.slavicmyths.registry.ModEntities;
public final class VelesStaffItem extends org.slavicmyths.combat.CombatStaffItem {
 private final boolean steppe;
 public VelesStaffItem(Properties p){this(p,false);}
 public VelesStaffItem(Properties p,boolean steppe){super(p.durability(steppe?96:48),steppe?6:4,18);this.steppe=steppe;}
 public boolean steppe(){return steppe;}
 @Override public Component getName(ItemStack stack){return steppe?Component.translatableWithFallback(getDescriptionId(),"Посох Велеса — Владыка зверей"):super.getName(stack);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){super.appendHoverText(s,c,lines,f);lines.add(Component.literal("ПКМ: случайный лесной призыв"));if(steppe){lines.add(Component.literal("Shift+ЛКМ: шакалы или гиены"));lines.add(Component.literal("Shift+ПКМ: зарезервировано для летающих существ"));}lines.add(Component.literal("Шанс усиленного призыва: 10%"));}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){
  ItemStack s=p.getItemInHand(h);if(h!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(s);
  if(steppe&&p.isShiftKeyDown())return InteractionResultHolder.fail(s);
  if(w.isClientSide)return InteractionResultHolder.success(s);
  return p instanceof ServerPlayer player&&summon(player,false)?InteractionResultHolder.consume(s):InteractionResultHolder.fail(s);
 }
 public static void steppeSkill(ServerPlayer p){if(p.isShiftKeyDown()&&p.getMainHandItem().getItem() instanceof VelesStaffItem item&&item.steppe())item.summon(p,true);}
 private boolean summon(ServerPlayer p,boolean savanna){
  if(!p.isAlive()||p.isSpectator()||p.getMainHandItem().getItem()!=this||savanna&&!steppe)return false;
  ServerLevel w=p.serverLevel();ItemStack s=p.getMainHandItem();long time=ArtifactEvents.now(w);
  var data=p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
  if(time<data.getLong("SlavicGuardianReady")||p.getCooldowns().isOnCooldown(this)){p.displayClientMessage(Component.translatable("artifact.slavicmyths.resting"),true);return false;}
  EntityType<? extends Mob> type;int count;
  if(savanna){boolean jackal=w.random.nextBoolean();type=jackal?ModEntities.JACKAL.get():ModEntities.STRIPED_HYENA.get();count=jackal?5:3;}
  else{type=w.random.nextInt(5)==0?ModEntities.BROWN_BEAR.get():ModEntities.FOREST_WOLF.get();count=1;}
  boolean enhanced=w.random.nextDouble()<SummonQuality.enhancedChance(p);long until=time+(enhanced?1500:1200);
  List<Mob> pending=new ArrayList<>();
  for(int n=0;n<count;n++){
   Mob mob=type.create(w);if(mob==null)return false;
   mob.getPersistentData().putUUID("SlavicGuardianOwner",p.getUUID());mob.getPersistentData().putLong("SlavicGuardianUntil",until);mob.setPersistenceRequired();
   if(enhanced)SummonQuality.enhance(mob);
   boolean placed=false;
   for(int i=0;i<32;i++){double a=(i+n*3)*Math.PI/8,rad=2+(i/16)*1.8;mob.moveTo(p.getX()+Math.cos(a)*rad,p.getY()+.1,p.getZ()+Math.sin(a)*rad,p.getYRot(),0);if(w.hasChunkAt(mob.blockPosition())&&w.noCollision(mob)&&w.getBlockState(mob.blockPosition().below()).isSolid()&&pending.stream().noneMatch(m->m.getBoundingBox().intersects(mob.getBoundingBox()))){placed=true;break;}}
   if(!placed)return false;pending.add(mob);
  }
  List<Mob> spawned=new ArrayList<>();for(Mob mob:pending){if(!w.addFreshEntity(mob)){spawned.forEach(Entity::discard);return false;}spawned.add(mob);}
  data.putLong("SlavicGuardianReady",time+2400);data.putUUID("SlavicGuardian",spawned.getFirst().getUUID());p.getPersistentData().put(Player.PERSISTED_NBT_TAG,data);
  p.getCooldowns().addCooldown(this,2400);s.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
  for(Mob mob:spawned)w.sendParticles(enhanced?ParticleTypes.ENCHANT:ParticleTypes.HAPPY_VILLAGER,mob.getX(),mob.getY()+1,mob.getZ(),enhanced?25:10,.4,.5,.4,.02);
  if(enhanced)p.displayClientMessage(Component.literal("Усиленный призыв!"),true);
  w.playSound(null,p.blockPosition(),SoundEvents.WOLF_AMBIENT,SoundSource.NEUTRAL,.5F,.65F);if(!savanna)org.slavicmyths.progression.Knowledge.award(p,"forest_guardian");p.swing(InteractionHand.MAIN_HAND,true);return true;
 }
}
