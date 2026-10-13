package org.slavicmyths.brewing;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import org.slavicmyths.kitchen.KitchenTile;
public class BeverageItem extends Item {
 private final String id;
 public BeverageItem(String id,Properties p){super(defaults(id,p));this.id=id;}
 private static Properties defaults(String id,Properties p){if(id.equals("berry_mors_pitcher")){CompoundTag n=new CompoundTag();n.putString("Beverage","berry_mors");n.putString("Quality","ready");n.putString("Container","ceramic_pitcher");n.putInt("Servings",4);p.component(DataComponents.CUSTOM_DATA,CustomData.of(n));}return p;}
 public static CompoundTag data(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
 public static void data(ItemStack s,CompoundTag n){s.set(DataComponents.CUSTOM_DATA,CustomData.of(n));}
 public static String beverage(ItemStack s){String k=Brewing.key(s);return (k.equals("filled_pitcher")||k.equals("berry_mors_pitcher"))?data(s).getString("Beverage"):k.replaceFirst("_(mug|bottle)$","");}
 public static boolean pitcherAllowed(String id){return Set.of("kvass","mead","apple_cider","berry_mors").contains(id);}
 public static boolean mugAllowed(String id){return Arrays.asList(Brewing.BEERS).contains(id)||id.equals("kvass")||id.equals("berry_mors");}
 public static int load(String b){return b.equals("spoiled_brew")?1:b.contains("thunder")||b.contains("veles")||b.contains("witch")?2:Arrays.asList(Brewing.BEERS).contains(b)||b.equals("mead")||b.equals("apple_cider")?1:0;}
 public static String quality(ItemStack s){String q=data(s).getString("Quality");return q.isEmpty()?"ready":q;}
 public static ItemStack filled(String b,String q,String container,int servings){String k=container.equals("ceramic_pitcher")?(b.equals("berry_mors")?"berry_mors_pitcher":"filled_pitcher"):b.equals("spoiled_brew")?b:b+(mugAllowed(b)?"_mug":"_bottle");ItemStack s=new ItemStack(Brewing.item(k));CompoundTag n=new CompoundTag();n.putString("Beverage",b);n.putString("Quality",q);n.putString("Container",container);n.putInt("Servings",container.equals("ceramic_pitcher")?servings:1);data(s,n);if(container.equals("dark_bottle")&&mugAllowed(b))s.set(DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(1));if(container.equals("ceramic_pitcher"))s.set(DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(b.equals("kvass")?1:b.equals("mead")?2:b.equals("apple_cider")?3:b.equals("spoiled_brew")?4:0));return s;}
 public static String empty(ItemStack s){String c=data(s).getString("Container");return c.isEmpty()?Brewing.key(s).endsWith("_mug")?"wooden_mug":"dark_bottle":c;}
 public static void grant(ServerPlayer p,String id){var a=p.server.getAdvancements().get(Brewing.id(id));if(a!=null)p.getAdvancements().award(a,"brewed");}
 public static void obtained(Player p,ItemStack s){if(p instanceof ServerPlayer sp&&quality(s).equals("ready")&&(load(beverage(s))>0)&&!beverage(s).equals("spoiled_brew"))grant(sp,"first_brew");}
 public static boolean emptyContainer(ItemStack s){return Set.of("wooden_mug","dark_bottle","ceramic_pitcher").contains(Brewing.key(s));}
 public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){ItemStack s=p.getItemInHand(hand),other=p.getItemInHand(hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);
  ItemStack pitcher=(id.equals("filled_pitcher")||id.equals("berry_mors_pitcher"))?s:(Brewing.key(other).equals("filled_pitcher")||Brewing.key(other).equals("berry_mors_pitcher"))?other:ItemStack.EMPTY;ItemStack target=(id.equals("filled_pitcher")||id.equals("berry_mors_pitcher"))?other:s;
  if(!pitcher.isEmpty()&&emptyContainer(target)&&!Brewing.key(target).equals("ceramic_pitcher")){String b=beverage(pitcher);if(Brewing.key(target).equals("wooden_mug")&&!mugAllowed(b))return InteractionResultHolder.fail(s);if(!w.isClientSide){CompoundTag n=data(pitcher);int count=n.getInt("Servings");if(count>0){ItemStack out=filled(b,quality(pitcher),Brewing.key(target),1);target.shrink(1);obtained(p,out);KitchenTile.give(p,out);n.putInt("Servings",count-1);if(count==1){pitcher.shrink(1);KitchenTile.give(p,new ItemStack(Brewing.item("ceramic_pitcher")));}else data(pitcher,n);}}return InteractionResultHolder.sidedSuccess(s,w.isClientSide);}
  if(emptyContainer(s)||(id.equals("filled_pitcher")||id.equals("berry_mors_pitcher")))return InteractionResultHolder.pass(s);p.startUsingItem(hand);return InteractionResultHolder.consume(s);
 }
 public int getUseDuration(ItemStack s,LivingEntity e){return 32;}public UseAnim getUseAnimation(ItemStack s){return UseAnim.DRINK;}
 public ItemStack finishUsingItem(ItemStack s,Level w,LivingEntity e){if(!(e instanceof Player p))return s;String b=beverage(s),q=quality(s),container=empty(s);if(!w.isClientSide){apply(p,b,q);if(s.has(DataComponents.FOOD))p.getFoodData().eat(s.get(DataComponents.FOOD));}if(!p.getAbilities().instabuild){s.shrink(1);ItemStack remainder=new ItemStack(Brewing.item(container));if(s.isEmpty())return remainder;if(!w.isClientSide)KitchenTile.give(p,remainder);}return s;}
 private static void positive(Player p,net.minecraft.core.Holder<MobEffect> effect,int seconds,String q){int n=BrewRules.duration(seconds,q);if(n>0)p.addEffect(new MobEffectInstance(effect,n*20,0));}
 public static void apply(Player p,String b,String q){
  if(q.equals("spoiled")||b.equals("spoiled_brew")){p.addEffect(new MobEffectInstance(MobEffects.CONFUSION,400));p.addEffect(new MobEffectInstance(MobEffects.POISON,100));b="spoiled_brew";}else switch(b){case "light_beer"->positive(p,MobEffects.MOVEMENT_SPEED,45,q);case "dark_beer"->positive(p,MobEffects.DAMAGE_RESISTANCE,45,q);case "hopped_beer"->positive(p,MobEffects.DIG_SPEED,40,q);case "honey_beer"->positive(p,MobEffects.ABSORPTION,30,q);case "forest_beer"->{positive(p,MobEffects.NIGHT_VISION,45,q);positive(p,MobEffects.LUCK,45,q);}case "thunder_beer"->{positive(p,MobEffects.MOVEMENT_SPEED,60,q);positive(p,MobEffects.DIG_SPEED,60,q);}case "veles_dark_beer"->{positive(p,MobEffects.DAMAGE_RESISTANCE,60,q);positive(p,MobEffects.NIGHT_VISION,45,q);}case "witch_berry_beer"->positive(p,MobEffects.LUCK,90,q);case "mead"->positive(p,MobEffects.REGENERATION,8,q);case "apple_cider"->positive(p,MobEffects.JUMP,45,q);default->{}}
  int add=load(b);CompoundTag n=p.getData(Brewing.LOAD).copy();long now=p.level().getGameTime();int points=BrewRules.decay(n.getInt("Points"),n.getLong("LastDrink"),now);if(add==0)return;points=(int)Math.min(Integer.MAX_VALUE,(long)points+add);n.putInt("Points",points);n.putLong("LastDrink",now);p.setData(Brewing.LOAD,n);if(points>=5&&p instanceof ServerPlayer sp)grant(sp,"too_much");boolean magic=add==2;int stage=Math.min(5,points),amp=stage>=5?1:0;if(magic&&stage>=3)amp=1;double factor=magic?1.5:1;
  if(stage>=2)negative(p,MobEffects.CONFUSION,new int[]{0,0,10,20,30,40}[stage],amp,factor);
  if(stage>=3)negative(p,MobEffects.WEAKNESS,new int[]{0,0,0,30,45,60}[stage],amp,factor);
  if(stage>=4)negative(p,MobEffects.MOVEMENT_SLOWDOWN,stage==4?30:45,magic&&stage>=5?1:0,factor);
  if(stage>=5)negative(p,MobEffects.POISON,8,0,factor);
  if(p.level() instanceof net.minecraft.server.level.ServerLevel server){var particle= b.equals("forest_beer")?net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER:b.equals("thunder_beer")?net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK:magic?net.minecraft.core.particles.ParticleTypes.WITCH:null;if(particle!=null)server.sendParticles(particle,p.getX(),p.getY()+1.4,p.getZ(),magic?4:3,.25,.2,.25,.02);}
 }
 private static void negative(Player p,net.minecraft.core.Holder<MobEffect> e,int sec,int amp,double f){p.addEffect(new MobEffectInstance(e,(int)(sec*20*f),amp));}
 public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){super.appendHoverText(s,c,lines,f);if(emptyContainer(s))return;lines.add(Component.translatable("brewing.slavicmyths.quality",Component.translatable("brewing.slavicmyths."+quality(s))));int n=load(beverage(s));if(n>0)lines.add(Component.translatable(n==2?"brewing.slavicmyths.magic_load":"brewing.slavicmyths.load",n));if((id.equals("filled_pitcher")||id.equals("berry_mors_pitcher"))){lines.add(Component.translatable("item.slavicmyths."+beverage(s)+(mugAllowed(beverage(s))?"_mug":"_bottle")));lines.add(Component.translatable("brewing.slavicmyths.servings",data(s).getInt("Servings")));}}
}
