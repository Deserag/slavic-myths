package org.slavicmyths.hunt;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;
/** Vanilla use-item duration reserves the held stack; all conditions rechecked at successful spawn. */
public final class BossRitualItem extends Item {
 private final BossKind kind;
 public BossRitualItem(Properties p,BossKind k){super(p);kind=k;}
 @Override public int getUseDuration(ItemStack s,net.minecraft.world.entity.LivingEntity user){return 60;}
 @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.BOW;}
 @Override public void appendHoverText(ItemStack s,net.minecraft.world.item.Item.TooltipContext w,List<Component> lines,net.minecraft.world.item.TooltipFlag f){lines.add(Component.translatable(getDescriptionId()+".effect").withStyle(ChatFormatting.GRAY));}
 private static boolean fail(ServerPlayer p,String reason){p.displayClientMessage(Component.translatable("boss.fail."+reason),true);return false;}
 private boolean valid(ServerPlayer p,boolean debug){ServerLevel w=p.serverLevel();if(w.dimension()!=Level.OVERWORLD)return fail(p,"dimension");if(HuntRecords.get(w).bossActive(p.getUUID()))return fail(p,"active");if(BossAnchors.nearby(w,p.blockPosition(),kind,kind==BossKind.LIKHO?96:128))return fail(p,"nearby");if(w.getDifficulty()==Difficulty.PEACEFUL)return fail(p,"peaceful");if(!debug){if(!BossAnchors.biome(w,w.getBiome(p.blockPosition()),kind))return fail(p,"biome");if(kind==BossKind.LIKHO&&!BossAnchors.night(w))return fail(p,"night");if(kind==BossKind.TUGARIN&&!w.canSeeSky(p.blockPosition()))return fail(p,"sky");}return true;}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){ItemStack s=p.getItemInHand(hand);if(!w.isClientSide&&!valid((ServerPlayer)p,false))return InteractionResultHolder.fail(s);p.startUsingItem(hand);return InteractionResultHolder.consume(s);}
 @Override public void onUseTick(Level w,net.minecraft.world.entity.LivingEntity e,ItemStack stack,int remaining){if(!w.isClientSide&&remaining%10==0){((ServerLevel)w).sendParticles(kind==BossKind.LIKHO?ParticleTypes.SMOKE:ParticleTypes.CLOUD,e.getX(),e.getY()+.5,e.getZ(),4,.6,.2,.6,.02);e.playSound(kind==BossKind.LIKHO?SoundEvents.BONE_BLOCK_HIT:SoundEvents.WOOL_HIT,.3F,.6F);}}
 @Override public ItemStack finishUsingItem(ItemStack s,Level w,net.minecraft.world.entity.LivingEntity entity){if(!w.isClientSide&&entity instanceof ServerPlayer&&summon((ServerPlayer)entity,kind,false))s.shrink(1);return s;}
 public static boolean summon(ServerPlayer p,BossKind kind,boolean debug){BossRitualItem item=(BossRitualItem)(kind==BossKind.LIKHO?org.slavicmyths.registry.ModItems.UZEL_DURNOY_DOLI.get():org.slavicmyths.registry.ModItems.STEPNOY_SHTANDART.get());if(!item.valid(p,debug))return false;ServerLevel w=p.serverLevel();BlockPos spot=BossAnchors.find(w,p.blockPosition(),kind,kind==BossKind.LIKHO?12:20,kind==BossKind.LIKHO?18:28,64,debug);if(spot==null)return fail(p,"position");HuntRecords d=HuntRecords.get(w);String key="ritual:"+p.getUUID();HuntRecords.Boss b=new HuntRecords.Boss(key,kind,spot,false);b.owner=p.getUUID();HuntRecords.Boss previous=d.bosses.put(key,b);if(!BossAnchors.spawn(w,b,p)){if(previous==null)d.bosses.remove(key);else d.bosses.put(key,previous);return fail(p,"position");}d.bossOwners.put(p.getUUID(),key);d.setDirty();var entity=w.getEntity(b.target);if(entity!=null)org.slavicmyths.navigation.NavigationManager.remember(entity);var nav=org.slavicmyths.navigation.NavigationRecords.get(w).encounters.get(b.target);if(nav!=null)org.slavicmyths.navigation.NavigationManager.assign(p,nav,key,null,0,false);p.displayClientMessage(Component.translatable("boss.summoned",Component.translatable("entity.slavicmyths."+kind.id)),true);return true;}
}
