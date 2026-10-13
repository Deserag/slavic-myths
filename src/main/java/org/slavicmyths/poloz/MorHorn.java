package org.slavicmyths.poloz;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.core.BlockPos;
/** Possession of the horn authorizes a 60-tick call; one active world event, no story gate. */
public final class MorHorn extends Item {
 public MorHorn(Properties p){super(p);}

 private static CompoundTag data(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
 public static boolean used(ItemStack s){return data(s).getBoolean("hasSummonedOnce");}
 public static UUID token(ItemStack s){var n=data(s);return n.hasUUID("polozCommit")?n.getUUID("polozCommit"):null;}
 public static void mark(ItemStack s,boolean used,UUID token){CustomData.update(DataComponents.CUSTOM_DATA,s,n->{n.putBoolean("hasSummonedOnce",used);if(token==null)n.remove("polozCommit");else n.putUUID("polozCommit",token);});}
 public static int dust(ServerPlayer p){int count=0;for(var s:p.getInventory().items)if(s.is(PolozContent.DUST.get()))count+=s.getCount();return count;}
 public static void consume(ServerPlayer p,int amount){for(var s:p.getInventory().items)if(s.is(PolozContent.DUST.get())){int take=Math.min(amount,s.getCount());s.shrink(take);amount-=take;if(amount==0)break;}}
 @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.TOOT_HORN;}
 @Override public int getUseDuration(ItemStack s,LivingEntity e){return 60;}
 @Override public void inventoryTick(ItemStack stack,Level level,Entity holder,int slot,boolean selected){if(level instanceof net.minecraft.server.level.ServerLevel w){UUID receipt=token(stack);if(receipt!=null){var data=PolozEvent.get(w);Boolean prior=data.hornRefunds.remove(receipt);if(prior!=null){mark(stack,prior,null);data.setDirty();}}}}
 @Override public InteractionResult useOn(UseOnContext c){if(c.getPlayer()==null||c.getHand()!=InteractionHand.MAIN_HAND)return InteractionResult.PASS;return activate(c.getPlayer(),c.getHand());}
 private InteractionResult activate(Player p,InteractionHand hand){if(p instanceof ServerPlayer server){var w=server.serverLevel();var d=PolozEvent.get(w);if(w.dimension()!=Level.OVERWORLD){p.displayClientMessage(Component.translatable("poloz.overworld"),true);return InteractionResult.FAIL;}if(!PolozEvent.canStart(d.state)){d.direction(server);return InteractionResult.FAIL;}var n=p.getPersistentData();n.putInt("PolozUseSlot",p.getInventory().selected);n.putFloat("PolozUseHealth",p.getHealth());n.putLong("PolozAltar",p.blockPosition().asLong());w.playSound(null,p.blockPosition(),PolozContent.sound("mor_horn_charge"),net.minecraft.sounds.SoundSource.PLAYERS,.5f,1);}p.startUsingItem(hand);return InteractionResult.CONSUME;}
 @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){if(hand!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(p.getItemInHand(hand));var result=activate(p,hand);return new InteractionResultHolder<>(result,p.getItemInHand(hand));}
 @Override public void onUseTick(Level level,LivingEntity user,ItemStack s,int remaining){if(user instanceof ServerPlayer p&&!valid(p))p.stopUsingItem();}
 private boolean valid(ServerPlayer p){var n=p.getPersistentData();return p.isAlive()&&!p.isSpectator()&&p.serverLevel().dimension()==Level.OVERWORLD&&p.getInventory().selected==n.getInt("PolozUseSlot")&&p.getMainHandItem().is(PolozContent.HORN.get())&&p.getHealth()>=n.getFloat("PolozUseHealth");}
 @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user){if(user instanceof ServerPlayer p&&valid(p))PolozEvent.get(p.serverLevel()).commit(p,stack,p.blockPosition());return stack;}

 @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("poloz.horn_lore"));lines.add(Component.translatable("poloz.irreversible").withStyle(net.minecraft.ChatFormatting.RED));lines.add(Component.translatable("poloz.call_ready"));}
}
