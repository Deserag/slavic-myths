package org.slavicmyths.yaga;
import java.util.*;
import net.minecraft.item.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import org.slavicmyths.registry.ModItems;
public final class YagaUtilityItem extends Item{
 public enum Kind{THREAD,CLEANSE,SALVE,SIGHT,RESIST,VIGOR,RESTORE,HERBS}
 private final Kind kind;public YagaUtilityItem(Properties p,Kind k){super(p);kind=k;}
 @Override public void appendHoverText(ItemStack stack,World w,List<ITextComponent> lines,net.minecraft.client.util.ITooltipFlag flag){lines.add(new TranslationTextComponent(getDescriptionId()+".effect").withStyle(TextFormatting.GRAY));}
 @Override public int getUseDuration(ItemStack s){return kind==Kind.THREAD||kind==Kind.HERBS?0:32;}@Override public UseAction getUseAnimation(ItemStack s){return kind==Kind.SALVE?UseAction.EAT:UseAction.DRINK;}
 @Override public ActionResult<ItemStack> use(World w,PlayerEntity player,Hand hand){ItemStack s=player.getItemInHand(hand);if(kind==Kind.HERBS)return super.use(w,player,hand);if(kind!=Kind.THREAD)return start(w,player,hand);if(player.getCooldowns().isOnCooldown(this))return ActionResult.fail(s);if(!w.isClientSide&&player instanceof ServerPlayerEntity){ServerPlayerEntity p=(ServerPlayerEntity)player;ServerWorld overworld=p.server.getLevel(World.OVERWORLD);YagaHut.prepare(overworld);YagaData d=YagaData.get(overworld);if(d.anchor==null){YagaServices.fail(p,"home");return ActionResult.fail(s);}if(w.dimension()!=World.OVERWORLD){p.displayClientMessage(new TranslationTextComponent("yaga.thread.dimension"),true);return ActionResult.fail(s);}Vector3d delta=new Vector3d(d.anchor.getX()+.5-p.getX(),0,d.anchor.getZ()+.5-p.getZ());String[] directions={"east","southeast","south","southwest","west","northwest","north","northeast"};int sector=Math.floorMod((int)Math.round(Math.atan2(delta.z,delta.x)/(Math.PI/4)),8);p.displayClientMessage(new TranslationTextComponent("yaga.thread.direction",new TranslationTextComponent("hunt.direction."+directions[sector]),Math.round(delta.length()/10)*10),false);p.getCooldowns().addCooldown(this,80);YagaServices.award(p,"follow_the_thread");w.playSound(null,p.blockPosition(),SoundEvents.WOOL_HIT,SoundCategory.PLAYERS,.6F,1.1F);}return ActionResult.success(s);}
 private static ActionResult<ItemStack> start(World w,PlayerEntity p,Hand h){p.startUsingItem(h);return ActionResult.consume(p.getItemInHand(h));}
 @Override public ItemStack finishUsingItem(ItemStack s,World w,LivingEntity e){if(!w.isClientSide){switch(kind){case CLEANSE:for(Effect bad:new Effect[]{Effects.POISON,Effects.WITHER,Effects.BLINDNESS,Effects.MOVEMENT_SLOWDOWN,Effects.WEAKNESS,Effects.HUNGER,Effects.CONFUSION})e.removeEffect(bad);break;case SALVE:if(e instanceof PlayerEntity)FlightRecovery.apply((PlayerEntity)e);break;case SIGHT:e.addEffect(new EffectInstance(Effects.NIGHT_VISION,1200));break;case RESIST:e.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,600));break;case VIGOR:e.addEffect(new EffectInstance(Effects.DIG_SPEED,1200));break;case RESTORE:e.addEffect(new EffectInstance(Effects.REGENERATION,200));break;default:break;}}
  boolean consume=!(e instanceof PlayerEntity)||!((PlayerEntity)e).abilities.instabuild;if(consume){s.shrink(1);if(kind!=Kind.SALVE){ItemStack bottle=new ItemStack(Items.GLASS_BOTTLE);if(s.isEmpty())return bottle;if(e instanceof PlayerEntity){PlayerEntity p=(PlayerEntity)e;if(!p.inventory.add(bottle))p.drop(bottle,false);}}}return s;
 }
}
