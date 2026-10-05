package org.slavicmyths.hunt;
import java.util.List;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
/** Vanilla use-item duration reserves the held stack; all conditions rechecked at successful spawn. */
public final class BossRitualItem extends Item {
 private final BossKind kind;
 public BossRitualItem(Properties p,BossKind k){super(p);kind=k;}
 @Override public int getUseDuration(ItemStack s){return 60;}
 @Override public UseAction getUseAnimation(ItemStack s){return UseAction.BOW;}
 @Override public void appendHoverText(ItemStack s,World w,List<ITextComponent> lines,net.minecraft.client.util.ITooltipFlag f){lines.add(new TranslationTextComponent(getDescriptionId()+".effect").withStyle(TextFormatting.GRAY));}
 private static boolean fail(ServerPlayerEntity p,String reason){p.displayClientMessage(new TranslationTextComponent("boss.fail."+reason),true);return false;}
 private boolean valid(ServerPlayerEntity p,boolean debug){ServerWorld w=p.getLevel();if(w.dimension()!=World.OVERWORLD)return fail(p,"dimension");if(HuntRecords.get(w).bossActive(p.getUUID()))return fail(p,"active");if(BossAnchors.nearby(w,p.blockPosition(),kind,kind==BossKind.LIKHO?96:128))return fail(p,"nearby");if(w.getDifficulty()==Difficulty.PEACEFUL)return fail(p,"peaceful");if(!debug){if(!BossAnchors.biome(w,w.getBiome(p.blockPosition()),kind))return fail(p,"biome");if(kind==BossKind.LIKHO&&!BossAnchors.night(w))return fail(p,"night");if(kind==BossKind.TUGARIN&&!w.canSeeSky(p.blockPosition()))return fail(p,"sky");}return true;}
 @Override public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand hand){ItemStack s=p.getItemInHand(hand);if(!w.isClientSide&&!valid((ServerPlayerEntity)p,false))return ActionResult.fail(s);p.startUsingItem(hand);return ActionResult.consume(s);}
 @Override public void onUseTick(World w,net.minecraft.entity.LivingEntity e,ItemStack stack,int remaining){if(!w.isClientSide&&remaining%10==0){((ServerWorld)w).sendParticles(kind==BossKind.LIKHO?ParticleTypes.SMOKE:ParticleTypes.CLOUD,e.getX(),e.getY()+.5,e.getZ(),4,.6,.2,.6,.02);e.playSound(kind==BossKind.LIKHO?SoundEvents.BONE_BLOCK_HIT:SoundEvents.WOOL_HIT,.3F,.6F);}}
 @Override public ItemStack finishUsingItem(ItemStack s,World w,net.minecraft.entity.LivingEntity entity){if(!w.isClientSide&&entity instanceof ServerPlayerEntity&&summon((ServerPlayerEntity)entity,kind,false))s.shrink(1);return s;}
 public static boolean summon(ServerPlayerEntity p,BossKind kind,boolean debug){BossRitualItem item=(BossRitualItem)(kind==BossKind.LIKHO?org.slavicmyths.registry.ModItems.UZEL_DURNOY_DOLI.get():org.slavicmyths.registry.ModItems.STEPNOY_SHTANDART.get());if(!item.valid(p,debug))return false;ServerWorld w=p.getLevel();BlockPos spot=BossAnchors.find(w,p.blockPosition(),kind,kind==BossKind.LIKHO?12:20,kind==BossKind.LIKHO?18:28,64,debug);if(spot==null)return fail(p,"position");HuntRecords d=HuntRecords.get(w);String key="ritual:"+p.getUUID();HuntRecords.Boss b=new HuntRecords.Boss(key,kind,spot,false);b.owner=p.getUUID();HuntRecords.Boss previous=d.bosses.put(key,b);if(!BossAnchors.spawn(w,b,p)){if(previous==null)d.bosses.remove(key);else d.bosses.put(key,previous);return fail(p,"position");}d.bossOwners.put(p.getUUID(),key);d.setDirty();p.displayClientMessage(new TranslationTextComponent("boss.summoned",new TranslationTextComponent("entity.slavicmyths."+kind.id)),true);return true;}
}
