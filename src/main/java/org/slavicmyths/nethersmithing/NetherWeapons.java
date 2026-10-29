package org.slavicmyths.nethersmithing;
import java.util.*;import net.minecraft.world.level.Level;import net.minecraft.world.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;import net.minecraft.server.level.*;import net.minecraft.world.item.*;import net.minecraft.world.item.crafting.Ingredient;import net.minecraft.resources.ResourceLocation;import net.minecraft.core.component.DataComponents;
import org.slavicmyths.combat.*;import org.slavicmyths.registry.ModItems;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public final class NetherWeapons {
 public static final Map<String,net.neoforged.neoforge.registries.DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
 public static final Tier TIER=new Tier(){public int getUses(){return 1800;}public float getSpeed(){return 8;}public float getAttackDamageBonus(){return 3;}public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops(){return net.minecraft.tags.BlockTags.INCORRECT_FOR_DIAMOND_TOOL;}public int getEnchantmentValue(){return 18;}public Ingredient getRepairIngredient(){return Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths","alatyr_ingot")));}};
 static {ITEMS.put("navya_ostroga",ModItems.ITEMS.register("navya_ostroga",Ostroga::new));ITEMS.put("ashen_greatsword",ModItems.ITEMS.register("ashen_greatsword",Greatsword::new));ITEMS.put("shadow_throwing_knife",ModItems.ITEMS.register("shadow_throwing_knife",ShadowKnife::new));ITEMS.put("volcanic_heart_flail",ModItems.ITEMS.register("volcanic_heart_flail",VolcanicFlail::new));ITEMS.put("infernal_bow",ModItems.ITEMS.register("infernal_bow",InfernalBow::new));}
 public static void init(){}
 public static boolean owns(ItemStack s){return s.getItem() instanceof Ostroga||s.getItem() instanceof Greatsword||s.getItem() instanceof ShadowKnife||s.getItem() instanceof VolcanicFlail||s.getItem() instanceof InfernalBow;}
 public static boolean upgraded(ItemStack s){return owns(s)||NetherSmithing.ordinary(s)||s.getItem() instanceof NetherArmor.Piece;}
 private static Item.Properties props(String id){return new Item.Properties().durability(NetherWeaponRules.durability(id)).fireResistant().rarity(Rarity.EPIC);}
 @net.neoforged.bus.api.SubscribeEvent public static void defaults(net.neoforged.neoforge.event.ModifyDefaultComponentsEvent e){for(var entry:ITEMS.entrySet()){String id=entry.getKey();e.modify(entry.getValue().get(),b->{b.set(DataComponents.MAX_DAMAGE,NetherWeaponRules.durability(id));if(!id.equals("infernal_bow")){var attrs=org.slavicmyths.combat.MaceItem.attributes(NetherWeaponRules.raw(id),NetherWeaponRules.speed(id));if(id.equals("navya_ostroga"))attrs=attrs.withModifierAdded(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE,new net.minecraft.world.entity.ai.attributes.AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","spear_reach"),1.25,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);b.set(DataComponents.ATTRIBUTE_MODIFIERS,attrs);}});}}
 private static InteractionResultHolder<ItemStack> begin(Level w,Player p,InteractionHand hand,String id,boolean twoHands){var s=p.getItemInHand(hand);if(hand!=InteractionHand.MAIN_HAND||twoHands&&!p.getOffhandItem().isEmpty())return InteractionResultHolder.fail(s);if(p instanceof ServerPlayer server&&!NetherSkills.ready(server,s,id)){NetherSkills.waitMessage(server,s,id);return InteractionResultHolder.fail(s);}p.startUsingItem(hand);return InteractionResultHolder.consume(s);}
 public static class Ostroga extends SwordItem {
  public Ostroga(){super(TIER,props("navya_ostroga").attributes(org.slavicmyths.combat.MaceItem.attributes(10,1.15F)));}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}@Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.SPEAR;}
  @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){return begin(w,p,hand,"navya_ostroga",false);}
  @Override public void releaseUsing(ItemStack s,Level w,LivingEntity e,int remaining){if(e instanceof ServerPlayer p)NetherSkills.release(p,s,"navya_ostroga",getUseDuration(s,e)-remaining);}
  @Override public boolean hurtEnemy(ItemStack s,LivingEntity target,LivingEntity user){if(user instanceof ServerPlayer p&&NetherSkills.legal(p,target)){long now=NetherSkills.now(p);var d=target.getPersistentData();if(d.getLong("OstrogaProc")<=now&&p.getRandom().nextFloat()<.2){d.putLong("OstrogaProc",now+80);target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,40),p);}}return super.hurtEnemy(s,target,user);}
 }
 public static final class Greatsword extends SwordItem {
  public Greatsword(){super(TIER,props("ashen_greatsword").attributes(org.slavicmyths.combat.MaceItem.attributes(15,.9F)));}
  @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}@Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.SPEAR;}
  @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){return p.isShiftKeyDown()?begin(w,p,hand,"ashen_greatsword",true):InteractionResultHolder.pass(p.getItemInHand(hand));}
  @Override public void releaseUsing(ItemStack s,Level w,LivingEntity e,int remaining){if(e instanceof ServerPlayer p)NetherSkills.release(p,s,"ashen_greatsword",getUseDuration(s,e)-remaining);}
  @Override public boolean hurtEnemy(ItemStack s,LivingEntity t,LivingEntity user){if(!user.level().isClientSide){var d=t.position().subtract(user.position());t.knockback(.35,-d.x,-d.z);}return super.hurtEnemy(s,t,user);}
 }
 public static final class ShadowKnife extends ThrowingWeaponItem {
  public ShadowKnife(){super(TIER,true,props("shadow_throwing_knife"));}
  private boolean special(Player p){return p.getPersistentData().getBoolean("NetherKnifeSkill");}
  @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){if(hand!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(p.getItemInHand(hand));if(p instanceof ServerPlayer server){if(p.isShiftKeyDown()&&!NetherSkills.ready(server,p.getItemInHand(hand),"shadow_throwing_knife")){NetherSkills.waitMessage(server,p.getItemInHand(hand),"shadow_throwing_knife");return InteractionResultHolder.fail(p.getItemInHand(hand));}p.getPersistentData().putBoolean("NetherKnifeSkill",p.isShiftKeyDown());}return super.use(w,p,hand);}
  @Override protected int throwingWear(Player p,ItemStack s){return special(p)?3:1;}
  @Override protected void prepareThrownStack(Player p,ItemStack s){if(special(p)&&p instanceof ServerPlayer player)NetherSkills.stackReady(s,"shadow_throwing_knife",NetherSkills.now(player)+240);}
  @Override protected void configureProjectile(WeaponProjectile projectile,Player p,ItemStack s){projectile.netherSkill(special(p));}
  @Override protected void afterThrow(Player p,ItemStack s){if(special(p)&&p instanceof ServerPlayer player)NetherSkills.consume(player,s,"shadow_throwing_knife");p.getPersistentData().remove("NetherKnifeSkill");}
  @Override public boolean hurtEnemy(ItemStack s,LivingEntity t,LivingEntity user){if(user instanceof ServerPlayer p&&NetherSkills.legal(p,t))NetherSkills.knifeHit(p,t,false);return super.hurtEnemy(s,t,user);}
  @Override public float thrownDamage(){return 8;}@Override public float projectileDamage(WeaponProjectile p){return p.netherSkill()?10:8;}
  @Override public void projectileHit(WeaponProjectile projectile,LivingEntity target,LivingEntity owner,boolean success){if(success&&owner instanceof ServerPlayer p&&NetherSkills.legal(p,target))NetherSkills.knifeHit(p,target,projectile.netherSkill());}
 }
 public static final class VolcanicFlail extends FlailItem {
  public VolcanicFlail(){super(TIER,props("volcanic_heart_flail"),13F/9F,.9F,.75F);}
  @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){if(!w.isClientSide)p.getPersistentData().putBoolean("NetherFlailSkill",p.isShiftKeyDown());return p.isShiftKeyDown()?begin(w,p,hand,"volcanic_heart_flail",false):super.use(w,p,hand);}
  @Override public void releaseUsing(ItemStack s,Level w,LivingEntity user,int remaining){if(user.getPersistentData().getBoolean("NetherFlailSkill")){if(user instanceof ServerPlayer p)NetherSkills.release(p,s,"volcanic_heart_flail",getUseDuration(s,user)-remaining);}else super.releaseUsing(s,w,user,remaining);user.getPersistentData().remove("NetherFlailSkill");}
 }
 private NetherWeapons(){}
}
