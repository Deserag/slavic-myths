# Полные итоговые файлы патча оружия

Полные локализации: src/main/resources/assets/slavicmyths/lang/{ru_ru,en_us}.json. PNG лежат в исходных ресурсах.

## src/main/java/org/slavicmyths/client/GorynychClient.java

```java
package org.slavicmyths.client;
import org.slavicmyths.gorynych.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class GorynychClient {
 @SubscribeEvent public static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(GorynychContent.BOSS.get(),GorynychRenderer::new);e.registerEntityRenderer(GorynychContent.CLOUD.get(),Effects::new);e.registerEntityRenderer(GorynychContent.SHOT.get(),Effects::new);}
 @SubscribeEvent public static void extensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent e){e.registerItem(new DragonArmor(),GorynychContent.ARMOR.values().stream().map(h->h.get()).toArray(net.minecraft.world.item.Item[]::new));}
 @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->{for(String name:new String[]{"serpent_crossbow","three_breath_crossbow"}){var item=GorynychContent.item(name);net.minecraft.client.renderer.item.ItemProperties.register(item,net.minecraft.resources.ResourceLocation.withDefaultNamespace("pulling"),(s,w,l,seed)->l!=null&&l.isUsingItem()&&l.getUseItem()==s?1:0);net.minecraft.client.renderer.item.ItemProperties.register(item,net.minecraft.resources.ResourceLocation.withDefaultNamespace("pull"),(s,w,l,seed)->l==null||!l.isUsingItem()||l.getUseItem()!=s||net.minecraft.world.item.CrossbowItem.isCharged(s)?0:(float)(s.getUseDuration(l)-l.getUseItemRemainingTicks())/(net.minecraft.world.item.CrossbowItem.getChargeDuration(s,l)+10));net.minecraft.client.renderer.item.ItemProperties.register(item,net.minecraft.resources.ResourceLocation.withDefaultNamespace("charged"),(s,w,l,seed)->net.minecraft.world.item.CrossbowItem.isCharged(s)?1:0);net.minecraft.client.renderer.item.ItemProperties.register(item,net.minecraft.resources.ResourceLocation.withDefaultNamespace("firework"),(s,w,l,seed)->s.getOrDefault(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES,net.minecraft.world.item.component.ChargedProjectiles.EMPTY).contains(net.minecraft.world.item.Items.FIREWORK_ROCKET)?1:0);}});}
 public static final class Effects<T extends Entity> extends EntityRenderer<T>{public Effects(EntityRendererProvider.Context c){super(c);}@Override public net.minecraft.resources.ResourceLocation getTextureLocation(T e){return GorynychContent.id("textures/entity/gorynych_effect.png");}@Override public void render(T e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){int type=e instanceof BreathCloud c?c.type():e instanceof BreathShot s?s.type():0;var out=buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(e)));int color=type==0?0x557721CC:type==1?0x66F26518:0x558FCBE5;if(e instanceof BreathCloud){for(int i=0;i<12;i++){double a=i*Math.PI/6+e.tickCount*.02;GuardianClient.cube(pose,out,Math.cos(a)*2.7-.2,.08+Math.sin(a*2)*.08,Math.sin(a)*2.7-.2,.4,type==2?.7:.18,.4,color);}}else GuardianClient.cube(pose,out,-.3,0,-.3,.6,.6,.6,color);super.render(e,yaw,partial,pose,buffers,light);}}
 private GorynychClient(){}
}
```

## src/main/java/org/slavicmyths/client/NightClient.java

```java
package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.karachun.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class NightClient {
 public static final KeyMapping BREATH=new KeyMapping("key.slavicmyths.night_breath",71,"key.categories.slavicmyths.night"),HARVEST=new KeyMapping("key.slavicmyths.night_harvest",86,"key.categories.slavicmyths.night"),WING=new KeyMapping("key.slavicmyths.night_wing",66,"key.categories.slavicmyths.night"),REDUCED=new KeyMapping("key.slavicmyths.night_reduced",297,"key.categories.slavicmyths.night");
 private static CompoundTag view=new CompoundTag();private static long received,lastPacket;private static boolean reduced,rendering;private static PlayerRenderer normal,slim;private static NightPhantomRenderer phantomRenderer;
 private record Swing(int mode,int combo,long at,int duration){}private static final Map<Integer,Swing> SWINGS=new HashMap<>();
 private record Illusion(LivingEntity actor,long at){}private static final List<Illusion> PHANTOMS=new ArrayList<>();private static long nextPhantom;private static net.minecraft.client.multiplayer.ClientLevel world;
 private static void send(int a){PacketDistributor.sendToServer(new NightNetwork.Action(a));}
 @SubscribeEvent public static void receive(NightNetwork.Received e){view=e.tag.copy();var mc=Minecraft.getInstance();received=mc.level==null?0:mc.level.getGameTime();lastPacket=received;if(view.getDouble("Fear")<50)PHANTOMS.clear();}
 @SubscribeEvent public static void pose(NightNetwork.PoseReceived e){var mc=Minecraft.getInstance();if(e.pose.duration()<=0){SWINGS.remove(e.pose.entity());return;}if(mc.level!=null)SWINGS.put(e.pose.entity(),new Swing(e.pose.action(),e.pose.combo(),mc.level.getGameTime(),e.pose.duration()));}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.level!=world){clear();world=mc.level;}while(REDUCED.consumeClick()){reduced=!reduced;saveOptions();if(mc.player!=null)mc.player.displayClientMessage(NightContent.text("reduced",reduced?"✓":"—"),true);}boolean scythe=mc.player!=null&&mc.player.getMainHandItem().is(NightContent.SCYTHE.get());while(BREATH.consumeClick())if(scythe&&mc.screen==null)send(1);while(HARVEST.consumeClick())if(scythe&&mc.screen==null)send(2);while(WING.consumeClick())if(mc.player!=null&&mc.screen==null)send(3);if(mc.level==null||mc.player==null||!mc.player.isAlive()){clear();return;}long now=mc.level.getGameTime();SWINGS.entrySet().removeIf(s->now-s.getValue().at>=s.getValue().duration||!(mc.level.getEntity(s.getKey()) instanceof LivingEntity l)||!l.isAlive()||!l.getMainHandItem().is(NightContent.SCYTHE.get()));if(now-lastPacket>30){view=new CompoundTag();PHANTOMS.clear();}for(var illusion:PHANTOMS){var actor=illusion.actor;Vec3 step=mc.player.position().subtract(actor.position()).normalize().scale(.012);actor.xo=actor.getX();actor.yo=actor.getY();actor.zo=actor.getZ();if(actor.distanceToSqr(mc.player)>4&&mc.level.noCollision(actor.getBoundingBox().move(step)))actor.setPos(actor.position().add(step));}int cap=NightRules.phantoms(view.getDouble("Fear"));while(PHANTOMS.size()>cap)PHANTOMS.removeLast();PHANTOMS.removeIf(i->now-i.at>180||i.actor.level()!=mc.level);if(PHANTOMS.size()<cap&&now>=nextPhantom){nextPhantom=now+60;LivingEntity actor=PHANTOMS.size()%2==0?EntityType.ZOMBIE.create(mc.level):EntityType.SKELETON.create(mc.level);if(actor!=null){double angle=mc.player.getYRot()*Math.PI/180+(PHANTOMS.size()-1)*1.4;Vec3 at=mc.player.position().add(-Math.sin(angle)*5,0,Math.cos(angle)*5);if(mc.level.hasChunkAt(net.minecraft.core.BlockPos.containing(at))){actor.setPos(at);actor.xo=at.x;actor.yo=at.y;actor.zo=at.z;PHANTOMS.add(new Illusion(actor,now));local("fear_phantom_spawn",reduced?.12f:.3f);}}}}
 @SubscribeEvent public static void click(InputEvent.InteractionKeyMappingTriggered e){var mc=Minecraft.getInstance();if(mc.player==null||!e.isAttack())return;if(mc.hitResult==null||mc.hitResult.getType()!=HitResult.Type.ENTITY){Vec3 from=mc.player.getEyePosition(),to=from.add(mc.player.getLookAngle().scale(4));var it=PHANTOMS.iterator();while(it.hasNext()){var i=it.next();if(i.actor.getBoundingBox().inflate(.15).clip(from,to).isPresent()){for(int j=0;j<6;j++)mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL,i.actor.getX(),i.actor.getY()+1,i.actor.getZ(),(j-3)*.02,.04,0);it.remove();nextPhantom=mc.level.getGameTime()+60;local("fear_phantom_dispel",.3f);break;}}}if(mc.player.getMainHandItem().is(NightContent.SCYTHE.get())&&(mc.hitResult==null||mc.hitResult.getType()!=HitResult.Type.BLOCK)){e.setCanceled(true);e.setSwingHand(false);send(0);}}
 @SubscribeEvent public static void scene(RenderLevelStageEvent e){if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;Vec3 camera=e.getCamera().getPosition();var out=mc.renderBuffers().bufferSource();float partial=e.getPartialTick().getGameTimeDeltaPartialTick(false);for(var illusion:PHANTOMS){var actor=illusion.actor;actor.tickCount=(int)(mc.level.getGameTime()-illusion.at);actor.setYRot((float)(Math.atan2(mc.player.getX()-actor.getX(),mc.player.getZ()-actor.getZ())*180/Math.PI));actor.yBodyRot=actor.yHeadRot=actor.getYRot();if(phantomRenderer!=null){var pose=e.getPoseStack();pose.pushPose();pose.translate(actor.getX()-camera.x,actor.getY()-camera.y,actor.getZ()-camera.z);phantomRenderer.render(actor,pose,out,LevelRenderer.getLightColor(mc.level,actor.blockPosition()),partial);pose.popPose();}}}
 @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){if(view.getBoolean("Night")){float factor=reduced?.8f:.5f;e.setRed(e.getRed()*factor+.025f);e.setGreen(e.getGreen()*factor+.015f);e.setBlue(e.getBlue()*factor+.065f);}}
 @SubscribeEvent public static void fogDistance(ViewportEvent.RenderFog e){if(!view.getBoolean("Night"))return;e.setNearPlaneDistance(Math.min(e.getNearPlaneDistance(),reduced?12:6));e.setFarPlaneDistance(Math.min(e.getFarPlaneDistance(),reduced?52:32));e.setCanceled(true);}
 @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){if(reduced||view.getDouble("Waning")<50)return;var mc=Minecraft.getInstance();if(mc.level==null)return;double t=(mc.level.getGameTime()+e.getPartialTick())%100;if(t<24)e.setRoll(e.getRoll()+(float)Math.sin(t*Math.PI/12)*1.5f);}
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.screen!=null||mc.options.hideGui)return;var g=e.getGuiGraphics();int x=12,y=g.guiHeight()-120;double waning=view.getDouble("Waning"),fear=view.getDouble("Fear");if(waning>0){g.fill(x,y,x+112,y+15,0xAA10121B);g.fill(x+1,y+12,x+1+(int)waning,y+14,0xFF8C91B5);g.drawString(mc.font,NightContent.text("waning",(int)waning,new String[]{"—","I","II","III"}[NightRules.tier(waning)]),x+3,y+1,0xD9D7E8,false);}if(fear>0){g.fill(x,y+17,x+112,y+32,0xAA16111F);g.fill(x+1,y+29,x+1+(int)fear,y+31,0xFF9866DC);g.drawString(mc.font,NightContent.text("fear",(int)fear),x+3,y+18,0xCDA9EF,false);}long now=view.getLong("Now")+Math.max(0,mc.level.getGameTime()-received);if(mc.player.getMainHandItem().is(NightContent.SCYTHE.get())||view.getBoolean("WingEquipped"))g.drawString(mc.font,NightContent.text("cooldowns",seconds("BreathReady",now),seconds("HarvestReady",now),seconds("WingReady",now)),x,y-14,0xC4AFDF,false);}
 private static long seconds(String key,long now){return Math.max(0,(view.getLong(key)-now+19)/20);}
 @SubscribeEvent public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e){if(e.getItemStack().is(NightContent.SCYTHE.get())||e.getItemStack().is(NightContent.WING.get()))e.getToolTip().add(NightContent.text("bindings",BREATH.getTranslatedKeyMessage(),HARVEST.getTranslatedKeyMessage(),WING.getTranslatedKeyMessage()));}
 private static float swing(AbstractClientPlayer p,float partial){var s=SWINGS.get(p.getId());if(s==null)return 0;float age=p.level().getGameTime()-s.at+partial,hit=s.mode==0?(s.combo==2?7:5):10;return age<hit?-age/hit:Math.max(0,1-(age-hit)/(s.duration-hit));}
 @SubscribeEvent public static void player(RenderPlayerEvent.Pre e){if(!(e.getEntity() instanceof AbstractClientPlayer p))return;if(rendering||normal==null||!p.getMainHandItem().is(NightContent.SCYTHE.get())||p.isInvisible())return;e.setCanceled(true);rendering=true;try{(p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM?slim:normal).render(p,p.getYRot(),e.getPartialTick(),e.getPoseStack(),e.getMultiBufferSource(),e.getPackedLight());}finally{rendering=false;}}
 private static final class Reaper extends PlayerRenderer {Reaper(EntityRendererProvider.Context c,boolean slim){super(c,slim);model=new PlayerModel<AbstractClientPlayer>(c.bakeLayer(slim?net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM:net.minecraft.client.model.geom.ModelLayers.PLAYER),slim){@Override public void setupAnim(AbstractClientPlayer p,float a,float b,float age,float yaw,float pitch){super.setupAnim(p,a,b,age,yaw,pitch);var sw=SWINGS.get(p.getId());if(sw==null)return;float f=swing(p,age-(int)age);float sign=sw!=null&&sw.combo==1?-1:1;boolean left=p.getMainArm()==HumanoidArm.LEFT;var main=left?leftArm:rightArm;var support=left?rightArm:leftArm;body.yRot=f*.28f*sign;main.xRot=-.95f+(sw!=null&&sw.combo==2?f*1.2f:0);main.yRot=-.35f+f*1.15f*sign;main.zRot=left?.25f:-.25f;support.xRot=-1.15f;support.yRot=left?-.55f:.55f;support.zRot=left?-.1f:.1f;rightSleeve.copyFrom(rightArm);leftSleeve.copyFrom(leftArm);}};addLayer(new FolkEquipmentLayer(this));}}
 @SubscribeEvent public static void hand(RenderHandEvent e){var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return;if(e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightRules.tier(view.getDouble("Waning"))>=3){e.setCanceled(true);var lowered=e.getPoseStack();lowered.pushPose();boolean offLeft=p.getMainArm()==HumanoidArm.RIGHT;lowered.translate(offLeft?-.4:.4,-.9,-.6);lowered.mulPose(Axis.XP.rotationDegrees(-20));var arms=(PlayerRenderer)mc.getEntityRenderDispatcher().getRenderer(p);if(offLeft)arms.renderLeftHand(lowered,e.getMultiBufferSource(),e.getPackedLight(),p);else arms.renderRightHand(lowered,e.getMultiBufferSource(),e.getPackedLight(),p);lowered.popPose();return;}if(e.getHand()!=net.minecraft.world.InteractionHand.MAIN_HAND||!p.getMainHandItem().is(NightContent.SCYTHE.get()))return;var sw=SWINGS.get(p.getId());if(sw==null)return;float motion=swing(p,e.getPartialTick());int sign=p.getMainArm()==HumanoidArm.LEFT?-1:1;float dir=sw.combo==1?-1:1;var pose=e.getPoseStack();pose.mulPose(Axis.YP.rotationDegrees(sign*motion*30*dir));pose.mulPose(Axis.XP.rotationDegrees(motion*(sw.combo==2?35:12)));}

 private static void local(String name,float volume){var mc=Minecraft.getInstance();if(mc.player!=null)mc.player.playSound(NightContent.SOUNDS.get(name).get(),volume,1);}
 private static void clear(){view=new CompoundTag();SWINGS.clear();PHANTOMS.clear();nextPhantom=0;received=lastPacket=0;}
 private static void saveOptions(){try{java.nio.file.Files.writeString(Minecraft.getInstance().gameDirectory.toPath().resolve("config/slavicmyths-night192-client.txt"),Boolean.toString(reduced));}catch(java.io.IOException ignored){}}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){clear();world=null;}
 @EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static final class Registration {
  @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){for(var k:List.of(BREATH,HARVEST,WING,REDUCED))e.register(k);try{var p=Minecraft.getInstance().gameDirectory.toPath().resolve("config/slavicmyths-night192-client.txt");if(java.nio.file.Files.exists(p))reduced=Boolean.parseBoolean(java.nio.file.Files.readString(p));}catch(java.io.IOException ignored){}}
  @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(NightContent.BOSS.get(),KarachunRenderer::new);e.registerEntityRenderer(NightContent.RAVEN.get(),NightRavenRenderer::new);e.registerEntityRenderer(NightContent.BLADE.get(),NightBladeRenderer::new);}
  @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){normal=new Reaper(e.getContext(),false);slim=new Reaper(e.getContext(),true);phantomRenderer=new NightPhantomRenderer(e.getContext());}
  @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){for(String s:List.of("_icon","_held"))e.register(net.minecraft.client.resources.model.ModelResourceLocation.standalone(NightContent.id("item/karachun_scythe"+s)));}
  @SubscribeEvent public static void extension(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent e){e.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions(){private final Quest185ItemRenderer renderer=new Quest185ItemRenderer();@Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){return renderer;}},NightContent.SCYTHE.get());}
 }
 private NightClient(){}
}
```

## src/main/java/org/slavicmyths/client/Quest185ItemRenderer.java

```java
package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.*;
/** Explicit display context keeps 16px icons in GUI and distinct voxels in the hand. */
public final class Quest185ItemRenderer extends BlockEntityWithoutLevelRenderer {
 public Quest185ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
 @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){var mc=Minecraft.getInstance();String name=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();String suffix=context==ItemDisplayContext.GUI||(!name.equals("karachun_scythe")&&(context==ItemDisplayContext.GROUND||context==ItemDisplayContext.FIXED))?"_icon":"_held";var model=mc.getModelManager().getModel(ModelResourceLocation.standalone(org.slavicmyths.quest185.Quest185Content.id("item/"+name+suffix)));pose.pushPose();pose.translate(.5,.5,.5);mc.getItemRenderer().render(stack,context,context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND,pose,buffers,light,overlay,model);pose.popPose();}
}
```

## src/main/java/org/slavicmyths/karachun/NightCombat.java

```java
package org.slavicmyths.karachun;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import org.slavicmyths.morozko.FrostWorld;
/** Delayed, server-owned swings. All three blade lanes share a cast hit ledger. */
public final class NightCombat {
 public static final String KEY="Night192Swing";
 public static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
 public static boolean valid(LivingEntity t,LivingEntity p){return t!=p&&(!(t instanceof NightRaven r)||!r.allied())&&!(t instanceof org.slavicmyths.morozko.PlayerDouble)&&!(t instanceof net.minecraft.world.entity.decoration.ArmorStand)&&!(t instanceof org.slavicmyths.yaga.BabaYaga)&&!(t instanceof org.slavicmyths.bayun.BayunEntity)&&org.slavicmyths.combat.CombatTargeting.canAttack(p,t);}
 private static CompoundTag data(ServerPlayer p){return NightLedger.get(p.serverLevel()).player(p.getUUID());}
 private static boolean pay(ServerPlayer p,String key,int cd){var n=data(p);if(now(p)<n.getLong(key))return false;n.putLong(key,now(p)+cd);NightLedger.get(p.serverLevel()).setDirty();return true;}
 public static void action(ServerPlayer p,int a){if(a<0||a>3||!p.isAlive()||p.isSpectator()||p.containerMenu!=p.inventoryMenu)return;if(a==3){wing(p);return;}if(!p.getMainHandItem().is(NightContent.SCYTHE.get())||p.getPersistentData().contains(KEY))return;
  if(a==0&&p.getAttackStrengthScale(.5f)<.9f)return;String key=a==1?"BreathReady":a==2?"HarvestReady":"SwingReady";int cd=a==1?NightConfig.ticks("breathCooldown"):a==2?NightConfig.ticks("harvestCooldown"):16;if(!pay(p,key,cd))return;identity(p.getMainHandItem());var n=new CompoundTag();int combo=now(p)-p.getPersistentData().getLong("Night192ComboTime")>25?0:p.getPersistentData().getInt("Night192Combo")%3;n.putInt("Mode",a);n.putInt("Combo",combo);n.putInt("Slot",p.getInventory().selected);n.put("Stack",p.getMainHandItem().save(p.registryAccess()));n.putLong("Start",now(p));n.putInt("Windup",a==0?(combo==2?7:5):10);n.putInt("Duration",a==0?(combo==2?20:combo==1?15:14):26);n.putFloat("Yaw",p.getYRot());p.getPersistentData().put(KEY,n);p.getPersistentData().putInt("Night192Combo",combo+1);p.getPersistentData().putLong("Night192ComboTime",now(p));p.resetAttackStrengthTicker();p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);NightContent.sound(p,a==1?"last_breath":a==2?"black_harvest":"scythe_windup");NightNetwork.pose(p,a,combo,n.getInt("Duration"));}
 public static void tick(ServerPlayer p){var n=p.getPersistentData().getCompound(KEY);if(n.isEmpty())return;var snapshot=ItemStack.parseOptional(p.registryAccess(),n.getCompound("Stack"));snapshot.setDamageValue(p.getMainHandItem().getDamageValue());if(!p.isAlive()||p.containerMenu!=p.inventoryMenu||p.getInventory().selected!=n.getInt("Slot")||!ItemStack.isSameItemSameComponents(snapshot,p.getMainHandItem())){cancel(p);return;}int age=(int)(now(p)-n.getLong("Start"));if(age>=n.getInt("Duration")){cancel(p);return;}if(age!=n.getInt("Windup")||n.getBoolean("Hit"))return;n.putBoolean("Hit",true);if(n.getInt("Mode")==2){UUID cast=UUID.randomUUID();for(int i=-1;i<=1;i++)NightBlade.launch(p,cast,i*Math.PI/12);return;}double radius=n.getInt("Mode")==1?NightConfig.value("breathRadius"):3.5;for(var t:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(radius+1),t->valid(t,p)&&FrostWorld.bboxDistance(p.getEyePosition(),t.getBoundingBox())<=radius*radius&&p.hasLineOfSight(t))){if(n.getInt("Mode")==0&&!FrostWorld.cone(p,t,radius,70))continue;damage(p,t,snapshot,n.getInt("Mode")==1?1.4f:1);}}
 public static boolean claim(ServerPlayer p,UUID cast,UUID target){var n=p.getPersistentData().getCompound("Night192Casts");long now=now(p);for(String key:new ArrayList<>(n.getAllKeys()))if(n.getCompound(key).getLong("Until")<now)n.remove(key);var row=n.getCompound(cast.toString());if(row.getBoolean(target.toString()))return false;row.putBoolean(target.toString(),true);row.putLong("Until",now+60);n.put(cast.toString(),row);p.getPersistentData().put("Night192Casts",n);return true;}
 public static void damage(ServerPlayer p,LivingEntity t,ItemStack weapon,float scale){if(!valid(t,p))return;float damage=(float)Math.max(0, (p.getMainHandItem().is(NightContent.SCYTHE.get()) ? p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) : 12) + NightConfig.value("baseDamage") - 12)*scale;boolean undead=t.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD);if(undead)damage*=1+NightConfig.value("undeadBonus");var source=p.damageSources().playerAttack(p);damage=net.minecraft.world.item.enchantment.EnchantmentHelper.modifyDamage(p.serverLevel(),weapon,t,source,damage);float before=t.getHealth();if(!t.hurt(source,damage))return;float actual=Math.max(0,before-t.getHealth());if(actual<=0)return;net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffectsWithItemSource(p.serverLevel(),t,source,weapon);if(p.getMainHandItem().is(NightContent.SCYTHE.get()))p.getMainHandItem().hurtAndBreak(1,p,EquipmentSlot.MAINHAND);var n=data(p);if(pay(p,"LifeReady",NightConfig.ticks("lifeCooldown"))){p.heal((float)Math.min(actual,NightConfig.value("lifeHeal")));NightContent.sound(p,"life_eater");p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,p.getX(),p.getY()+1,p.getZ(),5,.2,.3,.2,.03);}var souls=n.getCompound("Souls");String key=identity(weapon)+"/"+t.getUUID();var row=souls.getCompound(key);int hits=now(p)-row.getLong("At")>NightConfig.ticks("soulWindow")?0:row.getInt("Hits");hits++;row.putInt("Hits",hits%3);row.putLong("At",now(p));souls.put(key,row);for(String k:new ArrayList<>(souls.getAllKeys()))if(now(p)-souls.getCompound(k).getLong("At")>NightConfig.ticks("soulWindow"))souls.remove(k);n.put("Souls",souls);NightLedger.get(p.serverLevel()).setDirty();if(hits%3==0)NightEffects.add(t,p.getUUID(),net.minecraft.world.effect.MobEffects.WITHER,80,0);if(undead&&!t.isAlive())p.heal((float)NightConfig.value("undeadHeal"));NightContent.sound(t,"scythe_hit");}
 private static void wing(ServerPlayer p){if(org.slavicmyths.item.FolkAccessoryItem.equipped(p,NightContent.WING.get()).isEmpty()||now(p)<data(p).getLong("WingReady")||!p.serverLevel().getEntitiesOfClass(NightRaven.class,p.getBoundingBox().inflate(64),r->r.allied()&&p.getUUID().equals(r.owner)).isEmpty())return;var positions=new ArrayList<net.minecraft.world.phys.Vec3>();for(int i=0;i<2;i++){var at=p.position().add(i==0?1.5:-1.5,1.5,0);var box=new net.minecraft.world.phys.AABB(at.x-.3,at.y,at.z-.3,at.x+.3,at.y+.65,at.z+.3);if(!p.serverLevel().hasChunkAt(net.minecraft.core.BlockPos.containing(at))||!p.serverLevel().noCollision(box)||p.serverLevel().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),at,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getType()!=net.minecraft.world.phys.HitResult.Type.MISS){p.displayClientMessage(NightContent.text("space"),true);return;}positions.add(at);}var added=new ArrayList<NightRaven>();for(int i=0;i<2;i++){var r=NightContent.RAVEN.get().create(p.serverLevel());if(r==null){added.forEach(Entity::discard);return;}r.configure(p,i);r.setPos(positions.get(i));if(!p.serverLevel().addFreshEntity(r)){added.forEach(Entity::discard);return;}added.add(r);}pay(p,"WingReady",NightConfig.ticks("wingCooldown"));NightContent.sound(p,"wing_activate");}
 public static boolean unlock(ServerPlayer p,Entity npc){if(!p.isAlive()||p.isSpectator()||p.distanceToSqr(npc)>64||!p.hasLineOfSight(npc))return false;boolean allowed=npc instanceof org.slavicmyths.yaga.BabaYaga&&org.slavicmyths.quest185.Quest185Story.yagaTrust(p)>=2||npc instanceof org.slavicmyths.bayun.BayunEntity b&&b.bond(p).getBoolean("FirstGift");if(!allowed){p.displayClientMessage(NightContent.text("trust"),true);return false;}var d=NightLedger.get(p.serverLevel());var n=d.player(p.getUUID());if(!n.getBoolean("Unlocked")){if(org.slavicmyths.rpg.Runes.inventoryCount(p,Items.OBSIDIAN)<4||org.slavicmyths.rpg.Runes.inventoryCount(p,Items.BLUE_ICE)<1){p.displayClientMessage(NightContent.text("offering"),true);return false;}org.slavicmyths.quest185.Quest185Story.spend(p,Items.OBSIDIAN,4);org.slavicmyths.quest185.Quest185Story.spend(p,Items.BLUE_ICE,1);n.putBoolean("Unlocked",true);}if(org.slavicmyths.rpg.Runes.inventoryCount(p,NightContent.TOTEM.get())==0&&n.getList("Pending",10).isEmpty())d.enqueue(p,new ItemStack(NightContent.TOTEM.get()));d.setDirty();d.collect(p);org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:karachun_trial",2,false);return true;}
 private static UUID identity(ItemStack s){var n=s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();if(!n.hasUUID("Night192Weapon")){n.putUUID("Night192Weapon",UUID.randomUUID());s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(n));}return n.getUUID("Night192Weapon");}
 public static void cancel(ServerPlayer p){p.getPersistentData().remove(KEY);NightNetwork.pose(p,0,0,0);}
 private NightCombat(){}
}
```

## src/main/java/org/slavicmyths/karachun/NightEvents.java

```java
package org.slavicmyths.karachun;
import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
@EventBusSubscriber(modid="slavicmyths")
public final class NightEvents {
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void clamp(LivingDamageEvent.Pre e){if(e.getEntity() instanceof Karachun b&&b.fighting())e.setNewDamage(NightRules.clamp(b.getHealth(),b.getMaxHealth(),e.getNewDamage()));}
 @SubscribeEvent public static void friendly(LivingIncomingDamageEvent e){if(e.getSource().getEntity() instanceof NightRaven r&&!r.allowed(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void damage(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||e.getEntity().level().isClientSide)return;var from=e.getSource().getEntity();if(from==null)return;UUID owner=NightOwners.owner(from);if(e.getEntity() instanceof Karachun b&&b.fighting()&&owner!=null){b.credit(owner,e.getNewDamage(),false);if(from instanceof LivingEntity l)NightStatus.reduceFear(l,b.session);}if(from instanceof LivingEntity l&&NightCombat.valid(e.getEntity(),l)&&org.slavicmyths.rpg.Runes.has(l.getMainHandItem(),"waning")){var t=e.getEntity();var n=t.getPersistentData();long now=t.level().getGameTime();if(now>=n.getLong("Night192RuneReady")){n.putLong("Night192RuneReady",now+20);if(l.getRandom().nextDouble()<NightConfig.value("runeChance"))NightStatus.add(t,l.getUUID(),NightConfig.value("runeGain"),0,false);}}}
 @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.getMainHandItem().is(NightContent.SCYTHE.get())){e.setCanceled(true);NightCombat.action(p,0);}}
 @SubscribeEvent public static void offhand(PlayerInteractEvent.RightClickItem e){if(e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightStatus.offhand(e.getEntity())){e.setCanceled(true);notice(e.getEntity());}}
 @SubscribeEvent public static void block(PlayerInteractEvent.RightClickBlock e){if(e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightStatus.offhand(e.getEntity())){e.setCanceled(true);notice(e.getEntity());}}
 @SubscribeEvent public static void entity(PlayerInteractEvent.EntityInteract e){if(e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightStatus.offhand(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void specific(PlayerInteractEvent.EntityInteractSpecific e){if(e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightStatus.offhand(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void use(LivingEntityUseItemEvent.Start e){if(e.getEntity().getUsedItemHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightStatus.offhand(e.getEntity()))e.setCanceled(true);}
 private static void notice(net.minecraft.world.entity.player.Player p){if(p instanceof ServerPlayer s&&s.level().getGameTime()>=s.getPersistentData().getLong("Night192OffhandNotice")){s.getPersistentData().putLong("Night192OffhandNotice",s.level().getGameTime()+40);s.displayClientMessage(NightContent.text("offhand"),true);NightContent.sound(s,"waning_stage_up");}}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post e){if(e.getEntity() instanceof LivingEntity l)NightStatus.tick(l);if(e.getEntity() instanceof ServerPlayer p){NightCombat.tick(p);if(p.tickCount%5!=0)return;var d=NightLedger.get(p.serverLevel());if(p.tickCount%20==0&&!d.player(p.getUUID()).getList("Pending",10).isEmpty())d.collect(p);boolean show=p.getPersistentData().contains(NightStatus.KEY)||p.getMainHandItem().is(NightContent.SCYTHE.get())||!org.slavicmyths.item.FolkAccessoryItem.equipped(p,NightContent.WING.get()).isEmpty();if(show||p.getPersistentData().getBoolean("Night192HUD"))NightNetwork.send(p);p.getPersistentData().putBoolean("Night192HUD",show);}}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){leave(p);var d=NightLedger.get(p.serverLevel());d.collect(p);d.progress(p);NightNetwork.send(p);}}
 public static void leave(ServerPlayer p){for(String key:new ArrayList<>(p.getPersistentData().getCompound("Night192Effects").getAllKeys()))try{NightEffects.clear(p,UUID.fromString(key));}catch(IllegalArgumentException ignored){}NightStatus.clearAll(p);NightCombat.cancel(p);p.getPersistentData().remove("Night192Casts");for(var r:p.serverLevel().getEntitiesOfClass(NightRaven.class,p.getBoundingBox().inflate(64),r->p.getUUID().equals(r.owner)))r.discard();}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)leave(p);}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)leave(p);}
 @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)leave(p);}
 private NightEvents(){}
}
```

## tools/gorynych_assets.py

```python
"""Original reference-guided native rig, pixel assets and synthesized freely authored audio."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math,random,wave,struct,subprocess,shutil
from morozko_assets import Rig,write,merge
import karachun_assets as exporter
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';O=R/'docs/verification/gorynych-1.9.6'
PAL={'scale':['28272D','131519','555158'],'belly':['514049','2B252D','78666A'],'horn':['88847F','41414A','B4ACA1'],'mor':['5A147D','A42FF4','D352EE'],'fire':['A52918','FF6D19','FFB43E'],'predator':['211F23','7C2024','F54B4F'],'glow':['D52B37','68131C','FB5760'],'membrane':['481C28','211922','68131C'],'tooth':['DAD3C0','928D83','F5EEDC']}
PAL.update({'mor_eye':['A42FF4','D352EE','EDBAFF'],'fire_eye':['FF6D19','FFB43E','FFF4C4'],'predator_eye':['F54B4F','D52B37','FFD4D5']})
def rig():
 r=Rig();r.bone('body',None,0,44,12);r.box('body',0,23,12,62,38,88,'scale');r.box('body',0,22,-8,46,12,52,'belly');r.box('body',0,42,4,68,20,58,'scale')
 for side in (-1,1):
  for front,z in [('front',-20),('back',43)]:
   n=f'leg_{front}_{"left" if side<0 else "right"}';r.bone(n,'body',side*28,36,z);r.box(n,side*31,10,z,17,27,20,'scale');r.bone(n+'_foot',n,side*31,12,z);r.box(n+'_foot',side*31,0,z-4,21,12,27,'scale')
   for toe in range(3):r.box(n+'_foot',side*31-7+toe*7,1,z-19,4,5,11,'horn')
   for layer in range(3):r.box(n,side*(32+layer),26-layer*5,z-2,18,5,23,'scale')
  for i in range(9):r.box('body',side*(27+i%2),31+(i%3)*7,-21+i*8,4,6,8,'scale')
 for i in range(11):r.box('body',0,60-i%2*3,-24+i*7,5,8+(i%3)*2,6,'glow' if i%3==0 else 'scale')
 for h in range(3):
  x=(h-1)*30;mat=['mor','fire','predator'][h];parent='body'
  for j in range(5):
   y=40+j*15;z=-27-j*5;n=f'neck_{h}_{j}';r.bone(n,parent,x,y,z);r.box(n,x,y-1,z,18-j,18,19-j,'scale');r.box(n,x,y,z-9,10,16,2,'belly');r.box(n,x,y+8,z+9,4,9,4,mat);parent=n
  n=f'head_{h}';r.bone(n,parent,x,111,-55);r.box(n,x,105,-56,25 if h==1 else 22,18,27,'scale');r.box(n,x,104,-75,20 if h==2 else 18,10,22,'scale');r.box(n,x,103,-83,15,4,9,mat)
  r.bone(f'jaw_{h}',n,x,106,-61);r.box(f'jaw_{h}',x,97,-72,19,7,32,'scale');r.box(f'jaw_{h}',x,102,-77,13,1,18,mat)
  for side in (-1,1):
   r.box(n,x+side*10,115,-64,5,3,10,'scale');r.box(n,x+side*11.8,114,-69,1,2,7,mat+'_eye')
   r.box(n,x+side*9,119,-49,4,12,5,mat if h==0 else 'horn');r.box(n,x+side*11,129,-45,3,8,4,mat if h==0 else 'horn')
   for tooth in range(5):
    r.box(n,x+side*7,100,-66-tooth*4,2,5+(tooth%2)*(3 if h==2 else 1),2,'tooth');r.box(f'jaw_{h}',x+side*7,103,-66-tooth*4,2,3,2,'tooth')
  for i in range(4):r.box(n,x,122,-52+i*5,4,5+i%2*3,4,mat)
 parent='body'
 for i in range(8):
  n=f'tail_{i}';z=52+i*17;y=29-i*2;r.bone(n,parent,0,y,z);w=27-i*2.8;r.box(n,0,y-8,z+8,w,16-i,23,'scale');r.box(n,0,y+6,z+7,3,9,5,'mor' if i>5 else 'glow');parent=n
 for side in (-1,1):
  n='wing_left' if side<0 else 'wing_right';r.bone(n,'body',side*25,56,14)
  for j in range(8):
   x=side*(35+j*16);height=56+math.sin((j+1)/9*math.pi)*48;previous=56 if j==0 else 56+math.sin(j/9*math.pi)*48;web=22+math.sin((j+1)/9*math.pi)*34
   r.box(n,x,min(previous,height)-2,12+j*4,17,abs(previous-height)+5,7,'scale');r.box(n,x,height-web,26+j*4,17,web-3,2,'membrane')
   r.box(n,x,height-web-4,31+j*4,4,web+3,4,'scale')
   if j%2==0:r.box(n,x,height-web-5,33+j*4,5,6,2,'glow')
 return r
def model():
 r=rig();exporter.PAL=PAL;exporter.O=O;exporter.emit(r,'zmey_gorynych_corrupted','GorynychMesh',1024)
 atlas=Image.open(A/'textures/entity/zmey_gorynych_corrupted.png').convert('RGBA');glow=Image.open(A/'textures/entity/zmey_gorynych_corrupted_glow.png').convert('RGBA')
 for box in r.boxes:
  if box['material'].endswith('_eye'):u,v,w,h=box['uv'];glow.paste(atlas.crop((u,v,u+w,v+h)),(u,v))
 glow.save(A/'textures/entity/zmey_gorynych_corrupted_glow.png');clean=atlas.copy();pixels=clean.load()
 replacements={'scale':['E8E3D4','B8B5A8','D3D3CB'],'belly':['D3C7AA','BCA879','EEE4CC'],'horn':['BC8F45','8F7139','F5C16C'],'membrane':['E8E0C4','C9B994','F0E7D4'],'glow':['BC8F45','D3C7AA','F5C16C'],'mor':['E8E3D4','F5C16C','BC8F45'],'fire':['BC8F45','F5C16C','FBE4A0'],'predator':['D3D3CB','BC8F45','F5C16C'],'mor_eye':['F5C16C','BC8F45','FFE9AB'],'fire_eye':['F5C16C','BC8F45','FFE9AB'],'predator_eye':['F5C16C','BC8F45','FFE9AB']}
 mapping={tuple(bytes.fromhex(c)):tuple(bytes.fromhex(replacements[m][i])) for m in replacements for i,c in enumerate(PAL[m])}
 for y in range(clean.height):
  for x in range(clean.width):
   rgba=pixels[x,y]
   if rgba[:3] in mapping:pixels[x,y]=mapping[rgba[:3]]+(rgba[3],)
 clean.save(A/'textures/entity/zmey_gorynych_purified.png')
 for mask in range(1,8):
  partial=atlas.copy()
  for box in r.boxes:
   bone=box['bone'];head=int(bone.split('_')[1]) if bone.startswith(('neck_','head_','jaw_')) else -1
   if head>=0 and mask&1<<head:u,v,w,h=box['uv'];partial.paste(clean.crop((u,v,u+w,v+h)),(u,v))
  partial.save(A/f'textures/entity/zmey_gorynych_clean_{mask}.png')
 # Local flat particle sheet for geometry renderers; pixels remain nearest sampled.
 im=Image.new('RGBA',(16,16),(238,235,224,220));im.save(A/'textures/entity/gorynych_effect.png')
 write(O/'silhouettes.json',{'boss':{'length_blocks':19,'head_height_blocks':8.5,'wingspan_blocks':22,'legs':4,'heads':3},'serpent_crossbow':{'shape':'wide transverse steel limbs, heavy longitudinal stock, forked snake jaws at bow tips','stock_units':14,'limb_span_units':20,'materials':'dark serpent steel, red seam, brown grip','distinguishing':'physical string, paired serpent jaw ends'},'three_breath_crossbow':{'shape':'same crossbow family with three distinct essence chambers above stock','stock_units':15,'limb_span_units':22,'materials':'serpent steel, ivory scale plates, orange/purple/blue chambers','distinguishing':'three raised chambers and colored loaded bolt'}})
def crossbows():
 def element(a,b,uv=(0,0,8,8),texture='metal'):
  return {'from':a,'to':b,'faces':{s:{'texture':'#'+texture,'uv':list(uv)} for s in ['north','south','east','west','up','down']}}
 display={'gui':{'rotation':[28,45,0],'scale':[.75,.75,.75]},'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.45,.45,.45]},'fixed':{'rotation':[0,0,0],'scale':[.6,.6,.6]},'firstperson_righthand':{'rotation':[0,-90,25],'translation':[1,2,0],'scale':[.65,.65,.65]},'firstperson_lefthand':{'rotation':[0,90,-25],'translation':[1,2,0],'scale':[.65,.65,.65]},'thirdperson_righthand':{'rotation':[-90,0,0],'translation':[0,2,0],'scale':[.55,.55,.55]},'thirdperson_lefthand':{'rotation':[-90,0,0],'translation':[0,2,0],'scale':[.55,.55,.55]}}
 for name in ['serpent_crossbow','three_breath_crossbow']:
  special=name.startswith('three');im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
  for y in range(32):
   for x in range(32):d.point((x,y),fill=(45+((x*3+y*5)%15),42,47,255))
  d.rectangle((16,0,23,7),fill='#C63440');d.rectangle((24,0,31,7),fill='#49382A');d.rectangle((16,8,23,15),fill='#B9ADA0');d.rectangle((24,8,31,15),fill='#FF7424');d.rectangle((16,16,23,23),fill='#AC36EB');d.rectangle((24,16,31,23),fill='#9BDBF2');im.save(A/f'textures/item/{name}.png')
  elements=[element([6.5,6,2],[9.5,9,16]),element([7,5,10],[9,6,15],texture='grip'),element([-2,6,3],[18,8,5]),element([-2,5,1],[1,8,5]),element([15,5,1],[18,8,5]),element([-1,6,4],[0,7,11],texture='seam'),element([16,6,4],[17,7,11],texture='seam'),element([0,6,10],[16,6.3,10.3],texture='ivory')]
  if special:
   for i,mat in enumerate(['fire','mor','storm']):elements.append(element([4+i*3,9,5],[6+i*3,12,8],texture=mat))
  textures={'metal':f'slavicmyths:item/{name}','grip':f'slavicmyths:item/{name}','seam':f'slavicmyths:item/{name}','ivory':f'slavicmyths:item/{name}','fire':f'slavicmyths:item/{name}','mor':f'slavicmyths:item/{name}','storm':f'slavicmyths:item/{name}','particle':f'slavicmyths:item/{name}'}
  uv={'grip':[24,0,31,7],'seam':[16,0,23,7],'ivory':[16,8,23,15],'fire':[24,8,31,15],'mor':[16,16,23,23],'storm':[24,16,31,23]}
  for e in elements:
   for face in e['faces'].values():face['uv']=uv.get(face['texture'][1:],[0,0,15,15])
  for state in ['','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework']:
   es=json.loads(json.dumps(elements))
   if state in ['_arrow','_firework']:es.append(element([7.6,7.2,0],[8.4,8,11],texture='ivory'))
   if '_pulling_' in state:
    progress=int(state[-1])+1
    es[-(4 if special else 1)]['from'][2]=6+progress
   m={'textures':textures,'elements':es,'display':display,'gui_light':'front'}
   if not state:m['overrides']=[{'predicate':{'pulling':1},'model':f'slavicmyths:item/{name}_pulling_0'},{'predicate':{'pulling':1,'pull':.58},'model':f'slavicmyths:item/{name}_pulling_1'},{'predicate':{'pulling':1,'pull':1},'model':f'slavicmyths:item/{name}_pulling_2'},{'predicate':{'charged':1},'model':f'slavicmyths:item/{name}_arrow'},{'predicate':{'charged':1,'firework':1},'model':f'slavicmyths:item/{name}_firework'}]
   write(A/f'models/item/{name}{state}.json',m)
def icons():
 names=['gorynych_scale','ancient_serpent_steel','serpent_seal','gorynych_flame_breath_bottle','gorynych_mor_breath_bottle','gorynych_storm_breath_bottle','rune_mor_breath','rune_dragon_flame','rune_storm_strike','dragon_helmet','dragon_chestplate','dragon_leggings','dragon_boots']
 for name in names:
  im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);dark='#202126';metal='#59565C';red='#D52B37';white='#E8E3D4';color='#A42FF4' if 'mor' in name else '#9ED5E8' if 'storm' in name else '#FF7929'
  if 'bottle' in name:
   d.polygon([(12,2),(19,2),(19,10),(25,16),(26,25),(22,30),(8,30),(5,25),(6,16),(12,10)],fill='#657680',outline=white);d.polygon([(9,18),(15,13),(23,19),(23,25),(20,28),(10,28),(8,24)],fill=color);d.rectangle((11,2,20,5),fill='#806340');d.line((8,17,8,23),fill=white,width=2)
  elif name.startswith('rune'):
   shape=[(7,3),(25,4),(29,24),(23,30),(4,28),(2,11)];d.polygon(shape,fill=dark,outline=metal)
   points=[(8,24),(8,13),(15,19),(23,11),(23,25)] if 'mor' in name else [(7,24),(16,6),(25,24),(18,20),(13,26)] if 'flame' in name else [(5,23),(17,5),(12,18),(26,10),(17,28)]
   d.line(points,fill=color,width=3)
  elif name=='gorynych_scale':d.polygon([(15,1),(28,9),(26,21),(16,31),(3,20),(4,8)],fill=white,outline=metal);d.line([(16,5),(21,13),(14,21),(18,27)],fill='#BC8F45',width=3)
  elif name=='ancient_serpent_steel':d.polygon([(3,11),(24,5),(29,12),(27,22),(6,28),(2,22)],fill=metal,outline=dark);d.line([(4,15),(24,9),(27,12)],fill=red,width=3);d.line([(7,24),(24,19)],fill='#ABA4A1',width=2)
  elif name=='serpent_seal':
   d.ellipse((2,2,29,29),fill='#BC8F45',outline=dark,width=2)
   for x in [9,15,21]:d.line([(16,25),(x,17),(x,9)],fill=dark,width=2);d.rectangle((x-2,7,x+2,10),fill=metal)
  elif name.endswith('helmet'):
   d.polygon([(5,7),(12,4),(20,4),(27,8),(26,28),(21,30),(21,15),(11,15),(10,29),(5,26)],fill=metal,outline=dark);d.line([(4,9),(2,2),(7,6),(12,3),(13,9)],fill=dark,width=3);d.line([(27,8),(30,2),(25,5)],fill=dark,width=3);d.line([(7,12),(15,9),(24,12)],fill=red,width=2)
  elif name.endswith('chestplate'):
   d.polygon([(3,7),(10,3),(12,8),(20,8),(22,3),(29,7),(30,15),(25,15),(25,29),(6,29),(6,15),(1,15)],fill=metal,outline=dark);d.line([(16,10),(21,17),(16,24),(11,17),(16,10)],fill=red,width=3);d.line((5,6,4,2),fill=dark,width=3);d.line((27,6,29,2),fill=dark,width=3)
  elif name.endswith('leggings'):
   d.rectangle((6,2,25,9),fill=metal,outline=dark);d.rectangle((7,10,13,29),fill=metal,outline=dark);d.rectangle((18,10,24,29),fill=metal,outline=dark);d.line((10,12,10,25),fill=red,width=2);d.line((21,12,21,25),fill=red,width=2)
  else:
   for x in [3,18]:d.rectangle((x+3,3,x+10,23),fill=metal,outline=dark);d.polygon([(x+3,21),(x+10,20),(x+12,26),(x+11,30),(x,30),(x,25)],fill=dark,outline=metal);d.line((x+6,5,x+6,21),fill=red,width=2)
  im.save(A/f'textures/item/{name}.png');write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
 write(A/'models/item/zmey_gorynych_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
 im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
 for y in range(64):
  for x in range(64):d.point((x,y),fill=(50 if (x//3+y//3)%3 else 80,43,49,255))
 for x in range(4,60,9):d.line([(x,0),(x+3,15),(x,27),(x+4,43),(x,63)],fill='#C02A39',width=2)
 im.save(A/'textures/models/armor/dragon.png')
def integrate_hill_loot():
 p=D/'loot_table/chests/serpent_fortress.json';n=json.loads(p.read_text(encoding='utf-8'))
 if not any(pool.get('name')=='gorynych196' for pool in n['pools']):n['pools'].append({'name':'gorynych196','rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:ancient_serpent_steel','weight':5,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':2}}]},{'type':'minecraft:item','name':'slavicmyths:serpent_crossbow','weight':1},{'type':'minecraft:empty','weight':2}]})
 for pool in n['pools']:
  if pool.get('name')=='gorynych196':
   for entry in pool['entries']:
    for function in entry.get('functions',[]):
     if function.get('function')=='minecraft:set_count' and isinstance(function.get('count'),dict):function['count']['type']='minecraft:uniform'
 write(p,n)
def data():
 integrate_hill_loot();write(D/'recipe/gorynych_upgrade.json',{'type':'slavicmyths:gorynych_upgrade','category':'equipment'})
 for name in ['zmey_gorynych','gorynych_breath_cloud','gorynych_breath_shot']:write(D/f'loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':[]})
 for name in ['gorynych_mor','gorynych_fire','gorynych_impact']:write(D/f'damage_type/{name}.json',{'message_id':name,'scaling':'when_caused_by_living_non_player','exhaustion':.1})
 p=R/'src/main/resources/data/minecraft/tags/damage_type/is_fire.json';n=json.loads(p.read_text());n['values']=list(dict.fromkeys(n['values']+['slavicmyths:gorynych_fire']));write(p,n)
 # Existing quest item and IDs; no competing amulet or new access system.
 write(D/'recipe/mor_ward.json',{'type':'slavicmyths:gorynych_upgrade','category':'misc','ingredients':[{'item':'slavicmyths:mor_fire_amulet_fragment'},{'item':'slavicmyths:mor_water_amulet_fragment'},{'item':'slavicmyths:eye_of_mor'}],'result':{'id':'slavicmyths:mor_ward','count':1}})
 write(D/'tales/zmey_gorynych.json',{'part':'creatures','chapter':'legends','section':'inhabitants','title':'tale.slavicmyths.zmey_gorynych.title','icon':'slavicmyths:gorynych_scale','initial':0,'subjects':['entity/slavicmyths:zmey_gorynych'],'advancements':[],'links':[]})
 im=Image.new('RGBA',(128,96),'#19171E');d=ImageDraw.Draw(im);d.ellipse((28,55,104,85),fill='#49424E')
 for x,c in [(36,'#A42FF4'),(64,'#FF7929'),(92,'#F54B4F')]:d.line([(64,65),(x,35),(x,20)],fill='#555158',width=10);d.rectangle((x-9,14,x+12,23),fill='#302A35');d.point((x+9,17),fill=c)
 (A/'textures/tales').mkdir(exist_ok=True);im.save(A/'textures/tales/zmey_gorynych.png')
def language():
 names={'gorynych_scale':('Чешуя Горыныча','Gorynych Scale'),'ancient_serpent_steel':('Древняя змеиная сталь','Ancient Serpent Steel'),'serpent_crossbow':('Змеиный арбалет','Serpent Crossbow'),'three_breath_crossbow':('Арбалет трёх дыханий','Three Breath Crossbow'),'serpent_seal':('Змеиная печать','Serpent Seal'),'gorynych_flame_breath_bottle':('Пламенное дыхание','Flame Breath'),'gorynych_mor_breath_bottle':('Моровое дыхание','Mor Breath'),'gorynych_storm_breath_bottle':('Дыхание бури','Storm Breath'),'rune_mor_breath':('Руна морового дыхания','Mor Breath Rune'),'rune_dragon_flame':('Руна драконьего пламени','Dragon Flame Rune'),'rune_storm_strike':('Руна буревого удара','Storm Strike Rune'),'dragon_helmet':('Драконий шлем','Dragon Helmet'),'dragon_chestplate':('Драконий нагрудник','Dragon Chestplate'),'dragon_leggings':('Драконьи поножи','Dragon Leggings'),'dragon_boots':('Драконьи ботинки','Dragon Boots'),'zmey_gorynych_spawn_egg':('Яйцо призыва Змея Горыныча','Zmey Gorynych Spawn Egg')}
 desc={'gorynych_scale':('Светлая чешуя спасённого Горыныча.','Ivory scale of the rescued dragon.'),'ancient_serpent_steel':('Редкий металл древней крепости.','Rare metal from the serpent fortress.'),'serpent_crossbow':('+18% урона болтов, более долгая зарядка.','+18% bolt damage, slower loading.'),'three_breath_crossbow':('Shift+ПКМ разряженным: режим. Бутылка при зарядке даёт 3 особых выстрела; без неё обычные стрелы.','Sneak-use unloaded: mode. One breath bottle loads 3 special shots; ordinary arrows work without essence.'),'rune_mor_breath':('Каждое пятое прямое попадание: облако слабости на 4 секунды. Оружие и посохи.','Every fifth direct hit: a 4-second weakness cloud. Melee weapons and staves.'),'rune_dragon_flame':('Каждый четвёртый прямой удар: огненная вспышка по врагам рядом.','Every fourth melee hit: a small flame burst against nearby enemies.'),'rune_storm_strike':('Каждое четвёртое прямое попадание из лука/арбалета: волна отбрасывания.','Every fourth bow/crossbow hit: a knockback wave.'),'dragon_helmet':('Ослепление, тьма и тошнота на 25% короче.','Blindness, darkness and nausea last 25% less.'),'dragon_chestplate':('Урон огня/взрывов −15%.','Fire/explosion damage −15%.'),'dragon_leggings':('Отбрасывание −30%.','Knockback −30%.'),'dragon_boots':('Урон падения −35%.','Fall damage −35%.'),'serpent_seal':('Подтверждение помощи Горынычу. Не открывает портал.','Proof of rescuing Gorynych.'),'zmey_gorynych_spawn_egg':('Самостоятельный бой для творческого режима; сюжетные награды остаются в горе.','Standalone creative encounter; story rewards remain bound to the hill.')}
 ui={'boss':('Змей Горыныч','Zmey Gorynych'),'health':('Горыныч — %s / %s HP','Gorynych — %s / %s HP'),'inventory_full':('Для награды освободите 3 слота и вернитесь к Горынычу.','Free 3 inventory slots and return to Gorynych.'),'heads_hint':('Очистите три головы: порошок, пепел и очищенная вода.','Cleanse the three heads: powder, ash and purified water.'),'cleanse_0':('Моровой голове нужен очищающий порошок.','The Mor head needs cleansing powder.'),'cleanse_1':('Огненной голове нужен пепел очищения.','The fire head needs purifying ash.'),'cleanse_2':('Хищной голове нужна очищенная вода.','The predator head needs purified water.'),'reward':('Горыныч спасён. Получены четыре чешуи, печать и руна.','Gorynych is saved. You receive four scales, a seal and a rune.'),'shed_wait':('Следующая чешуя через %s секунд игрового времени.','Next shed scale in %s seconds of world time.'),'mode_0':('Мор — %s зарядов','Mor — %s charges'),'mode_1':('Огонь — %s зарядов','Flame — %s charges'),'mode_2':('Буря — %s зарядов','Storm — %s charges'),'compatible_mor_breath':('Оружие ближнего боя и посохи.','Melee weapons and staves.'),'compatible_dragon_flame':('Оружие ближнего боя.','Melee weapons.'),'compatible_storm_strike':('Луки и арбалеты.','Bows and crossbows.')}
 desc.update({'gorynych_flame_breath_bottle':('Остаток огненной атаки. Даёт 3 огненных заряда арбалету трёх дыханий.','Collected flame essence. Loads 3 flame shots into a Three Breath Crossbow.'),'gorynych_mor_breath_bottle':('Собранное дыхание Мора. Даёт 3 моровых заряда арбалету трёх дыханий.','Collected Mor essence. Loads 3 Mor shots into a Three Breath Crossbow.'),'gorynych_storm_breath_bottle':('Дыхание крыльев. Даёт 3 буревых заряда арбалету трёх дыханий.','Collected wing storm essence. Loads 3 storm shots into a Three Breath Crossbow.')})
 for i,locale in enumerate(['ru_ru','en_us']):
  values={f'item.slavicmyths.{k}':v[i] for k,v in names.items()};values.update({f'item.slavicmyths.{k}.desc':v[i] for k,v in desc.items()});values.update({f'gorynych196.{k}':v[i] for k,v in ui.items()});values['serpenthill195.ward_needed']='Для защиты горы нужны победа над заражённым Лихом, оба фрагмента хранителей и оберег: в инвентаре или действующий.' if i==0 else 'Hill protection requires defeating blighted Likho, both guardian fragments and a carried or active Mor Ward.';values.update({'entity.slavicmyths.zmey_gorynych':names['zmey_gorynych_spawn_egg'][i].replace('Яйцо призыва ','').replace(' Spawn Egg',''),'entity.slavicmyths.gorynych_breath_cloud':'Облако дыхания' if i==0 else 'Breath Cloud','entity.slavicmyths.gorynych_breath_shot':'Сгусток дыхания' if i==0 else 'Breath Shot','tale.slavicmyths.zmey_gorynych.title':'Спасение Горыныча' if i==0 else 'Rescuing Gorynych','tale.slavicmyths.zmey_gorynych.body':'Игровая интерпретация: три головы заражены Мором. Очищение сохраняет древнему змею жизнь.' if i==0 else 'Game interpretation: three heads infected by Mor. Cleansing preserves the ancient dragon’s life.'})
  for rune in ['mor_breath','dragon_flame','storm_strike']:values['rpg.slavicmyths.effect_'+rune]=desc['rune_'+rune][i];values['rpg.slavicmyths.rune_'+rune]=names['rune_'+rune][i]

  for stage in ['unknown','rumor','seen','witnessed','known','discovered','studied','verified']:values[f'tales.entry.zmey_gorynych.{stage}']=values['tale.slavicmyths.zmey_gorynych.body']
  for kind in ['mor','fire','impact']:
   values['death.attack.gorynych_'+kind]='%1$s повержен атакой %2$s' if i==0 else '%1$s was overcome by %2$s'
   values['death.attack.gorynych_'+kind+'.item']='%1$s повержен атакой %2$s с помощью %3$s' if i==0 else '%1$s was overcome by %2$s using %3$s'
  merge(A/f'lang/{locale}.json',values)
def sounds():
 source=(R/'src/main/java/org/slavicmyths/gorynych/GorynychContent.java').read_text();line=source.split('SOUNDS_LIST=List.of(')[1].split(');')[0];names=[s.strip().strip('"') for s in line.split(',')];ff=shutil.which('ffmpeg');assert ff;work=R/'.tools/gorynych-audio';work.mkdir(exist_ok=True);out=A/'sounds/gorynych';out.mkdir(parents=True,exist_ok=True);events={}
 for i,name in enumerate(names):
  duration=1.4 if 'roar' in name or 'transform' in name else .7 if 'charge' in name or 'breath' in name else .35;rate=22050;rng=random.Random(19600+i);samples=[]
  for j in range(int(rate*duration)):
   t=j/rate;fade=math.sin(math.pi*t/duration)**.6;low=55+i*3;freq=low*(1+.15*math.sin(t*9));noise=rng.uniform(-1,1)
   if 'cleanse' in name or 'purified' in name or 'bottle' in name:v=(math.sin(2*math.pi*(500+i*31)*t)+.3*math.sin(2*math.pi*(850+i*27)*t))*fade*.18
   elif 'fire' in name:v=(noise*.65+math.sin(2*math.pi*120*t)*.2)*(1+.2*math.sin(t*70))*fade*.35
   elif 'mor' in name:v=(math.sin(2*math.pi*(170+i*4)*t+.7*math.sin(t*15))*.4+noise*.18)*fade*.4
   elif 'wind' in name or 'wings' in name or 'dive' in name:v=(noise*.5+math.sin(2*math.pi*(60+i*2)*t)*.12)*fade*.35
   elif 'step' in name or 'impact' in name or 'land' in name:v=(math.sin(2*math.pi*(65-30*t/duration)*t)+noise*.2)*math.exp(-t*10)*.4
   elif 'crossbow_charge' in name:v=(noise*.3*(1 if (j//500)%2 else .1)+math.sin(2*math.pi*380*t)*.1)*fade*.4
   else:v=(.5*math.sin(2*math.pi*freq*t)+.24*math.sin(2*math.pi*(freq*1.47)*t)+.2*noise)*fade*.45
   samples.append(struct.pack('<h',int(max(-1,min(1,v))*32767)))
  p=work/(name+'.wav')
  with wave.open(str(p),'wb') as wav:wav.setnchannels(1);wav.setsampwidth(2);wav.setframerate(rate);wav.writeframes(b''.join(samples))
  subprocess.run([ff,'-y','-hide_banner','-loglevel','error','-i',str(p),'-c:a','libvorbis','-q:a','3','-flags:a','+bitexact','-fflags','+bitexact',str(out/(name+'.ogg'))],check=True)
  events['gorynych.'+name]={'sounds':['slavicmyths:gorynych/'+name]}
 merge(A/'sounds.json',events)
def main():
 O.mkdir(parents=True,exist_ok=True);model();crossbows();icons();data();language();sounds();__import__('weapon_patch_assets').main();print('GORYNYCH_ASSETS_PASS native rig, 9 appearances, 12 crossbow states, 31 original OGG')
if __name__=='__main__':main()
```

## tools/karachun_assets.py

```python
"""Original reference-guided 1.9.2 geometry/pixels/sounds; never uses poster pixels."""
from pathlib import Path
from PIL import Image,ImageDraw
import math,json,random,wave,struct,subprocess,shutil
from morozko_assets import Rig,write,merge,add_tag
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';O=R/'docs/verification/karachun-1.9.2'
PAL={'cloth':['10121B','242735','313441'],'fur':['42404A','34333D','53515E'],'skin':['7A7B84','555760','A2A6B2'],'hair':['8A8B94','676974','B4B8C4'],'bone':['BAC4D8','7B8291','D0D3DE'],'wood':['252025','17151C','44323C'],'metal':['242331','14131F','464255'],'ice':['6A70A1','434B76','A9C6EE'],'glow':['6F49BC','8E68DC','C5ABFF']}
def scythe(r,n,x,y,z):
 r.box(n,x,y,z,2,42,2,'wood')
 for yy in [1,8,16,24,32,39]:r.box(n,x,y+yy,z,3,1.5,3,'bone' if yy in [1,24] else 'metal')
 for yy in [9,19,29]:r.box(n,x,y+yy,z,2.5,4,2.5,'wood');r.box(n,x,y+yy+.6,z-1.4,.6,2.5,.3,'glow')
 # Thick asymmetric crescent: lateral sweep and descending taper; never an axe silhouette.
 for i in range(13):
  xx=x-i*1.8;yy=y+42-(i/12)**2*11;h=4.5-i*.23
  r.box(n,xx,yy,z,2.5,h,2.4,'metal');r.box(n,xx,yy-.3,z-.05,2.1,.65,2.7,'glow')
  if i%2==0:r.box(n,xx,yy-2.5-i*.16,z,.9,2.5+i*.15,1.3,'ice')
 for off in [3,9]:
  for j in range(5):r.box(n,x-off,y+36-j*1.5,z,.65,.85,.65,'bone')
  r.box(n,x-off,y+27,z,1.3,2,1.3,'ice')
 r.box(n,x,y,z,3.5,2.5,3.5,'bone');r.box(n,x,y+.6,z-1.85,1,1,.2,'glow')
def boss():
 r=Rig();r.bone('pelvis',None,0,24,0);r.bone('torso','pelvis',0,48,0);r.box('torso',0,27,1,16,23,10,'cloth');r.box('pelvis',0,24,0,18,3,11,'wood')
 for side,tag in [(-1,'right'),(1,'left')]:
  r.bone('leg_'+tag,'pelvis',side*5,24,0);r.box('leg_'+tag,side*5,5,0,7,19,7,'cloth');r.box('leg_'+tag,side*5,0,-1.2,8,6,9,'metal')
  r.bone('arm_'+tag,'torso',side*11,46,0);r.box('arm_'+tag,side*11,31,0,7,15,8,'cloth');r.bone('forearm_'+tag,'arm_'+tag,side*11,32,0);r.box('forearm_'+tag,side*11,21,0,6,11,7,'cloth');r.bone('hand_'+tag,'forearm_'+tag,side*11,22,0);r.box('hand_'+tag,side*11,17,0,5,5,5,'skin')
  r.bone('fur_'+tag,'torso',side*10,48,0)
  for i in range(7):
   xx=side*(6+i*1.7);r.box('fur_'+tag,xx,42-i*.5,1,3,4,12,'fur');r.box('fur_'+tag,xx,39-i*.6,-5,2,4,3,'fur')
   if i%2==0:r.box('fur_'+tag,xx,36-i*.6,-5.5,.9,3,1,'ice')
  for face,z in [('front',-6),('back',6),('side',1)]:
   n='cloak_'+face+'_'+tag;r.bone(n,'pelvis',side*7,27,z)
   for layer in range(4):
    y=25-layer*6;xx=side*(7+layer*.7) if face!='side' else side*(11+layer*.8)
    r.box(n,xx,y-7,z,8 if face!='side' else 3,8,2 if face!='side' else 11,'cloth')
    for j in range(4):r.box(n,xx-3+j*2,y-10-(j%2),z-1.3,1.3,5+(j%2),.7,'fur')
  r.bone('chains_'+tag,'torso',side*6,41,-6)
  for j in range(12):r.box('chains_'+tag,side*(6+math.sin(j*.4)),39-j*1.5,-6.6,.9,1.1,.8,'bone')
  for j in [2,6,10]:r.box('chains_'+tag,side*7,38-j*1.5,-7,2,2,1,'bone');r.box('chains_'+tag,side*7,38.5-j*1.5,-7.6,.8,.5,.3,'metal')
 r.bone('head','torso',0,52,-2);r.box('head',0,48,-3,9,11,8,'skin');r.box('head',0,49,-7,2,4,2,'skin')
 for side in [-1,1]:
  r.box('head',side*2.5,53,-7.3,3,2.3,.7,'metal');r.box('head',side*2.5,53.5,-7.8,1.5,.8,.4,'glow');r.box('head',side*3.5,50,-7.2,2,2.8,1,'skin')
  n='hair_'+('right' if side<0 else 'left');r.bone(n,'head',side*5,59,-2)
  for j in range(5):r.box(n,side*(4.5+j*.45),47-j*2,-5+j*1.6,1.8,11+j*2,2,'hair')
 r.bone('hair_back','head',0,59,2)
 for i in range(6):r.box('hair_back',-5+i*2,43+i%3,3,1.8,16-i%3,2,'hair')
 r.bone('beard','head',0,50,-7.5)
 for i in range(5):r.box('beard',-4+i*2,40+abs(i-2),-7.5,1.7,10-abs(i-2),2,'hair')
 r.bone('scythe','hand_right',-14,20,-3);scythe(r,'scythe',-14,2,-3)
 for b in r.boxes:
  bottom=b['center'][1]-b['size'][1]/2
  if bottom<0:b['size'][1]+=bottom;b['center'][1]=b['size'][1]/2
 return r
def raven():
 r=Rig();r.bone('bird',None,0,24,0);r.box('bird',0,20,0,5,5,8,'fur');r.bone('head','bird',0,25,-4);r.box('head',0,23,-4,4,4,4,'metal');r.box('head',0,23.5,-7,2.5,2,3,'metal');r.box('head',0,23,-9,1.5,1,2,'metal')
 for side,tag in [(-1,'right'),(1,'left')]:
  r.box('head',side*2.05,25,-5.2,.3,.8,1.1,'glow');r.bone('wing_'+tag,'bird',side*2.5,24,0)
  for i in range(6):
   xx=side*(3.5+i*1.3);r.box('wing_'+tag,xx,22+i*.1,i*.25,2.4,1.2,5+i*.5,'metal');r.box('wing_'+tag,xx,21.8,i*.5+2.8,1.2,.7,3+i*.25,'fur');r.box('wing_'+tag,xx,21.6,i*.5+5,1,.5,1,'ice')
  r.bone('claw_'+tag,'bird',side*1.4,21,1);r.box('claw_'+tag,side*1.4,17.5,1,.6,3,.6,'metal')
  for i in range(3):r.box('claw_'+tag,side*1.4+(i-1)*.6,17.2,-.2,.4,.5,2,'bone')
 r.bone('tail','bird',0,22,4)
 for i in range(5):r.box('tail',-2+i,20.2,5+i%2,1,1,5,'fur')
 # Feet at entity origin: do not reuse the humanoid's y=24 pivot as a flying hitbox offset.
 for bone in r.bones.values():bone['pos'][1]-=17.2
 for b in r.boxes:b['center'][1]-=17.2
 return r
def emit(r,name,cls,size=512):
 im=Image.new('RGBA',(size,size));x=y=2;row=0
 for i,b in enumerate(r.boxes):
  w,h,d=b['size'];rw=math.ceil(2*w+2*d);rh=math.ceil(h+d)
  if x+rw+3>size:x=2;y+=row+3;row=0
  assert y+rh+3<=size;row=max(row,rh);b['uv']=[x,y,rw,rh];colors=[tuple(bytes.fromhex(c))+(255,) for c in PAL[b['material']]]
  for yy in range(rh):
   for xx in range(rw):im.putpixel((x+xx,y+yy),colors[1 if xx%5==0 else 2 if (xx//2+yy//3+i)%9==0 else 0])
  x+=rw+3
 f=lambda n:f'{float(n):.4f}F';lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;',f'public final class {cls} {{public static ModelPart create(){{var mesh=new MeshDefinition();var root=mesh.getRoot();']
 for n,bone in r.bones.items():
  parent=bone['parent'];pos=bone['pos'];pp=r.bones[parent]['pos'] if parent else [0,24,0];c='CubeListBuilder.create()'
  for b in r.boxes:
   if b['bone']!=n:continue
   cx,cy,cz=b['center'];w,h,d=b['size'];u,v,_,_=b['uv'];c+=f'.texOffs({u},{v}).addBox('+','.join(map(f,[cx-w/2-pos[0],pos[1]-cy-h/2,cz-d/2-pos[2],w,h,d]))+')'
  offset=[pos[0]-pp[0],pp[1]-pos[1],pos[2]-pp[2]];lines.append(f'var {n}={(parent or "root")}.addOrReplaceChild("{n}",{c},PartPose.offset('+','.join(map(f,offset))+'));')
 lines.append(f'return LayerDefinition.create(mesh,{size},{size}).bakeRoot();}}private {cls}(){{}}}}');(R/f'src/main/java/org/slavicmyths/client/{cls}.java').write_text('\n'.join(lines)+'\n',encoding='utf-8')
 (A/'textures/entity').mkdir(parents=True,exist_ok=True);im.save(A/f'textures/entity/{name}.png');glow=Image.new('RGBA',(size,size))
 for b in r.boxes:
  if b['material']!='glow':continue
  u,v,w,h=b['uv'];glow.paste(im.crop((u,v,u+w,v+h)),(u,v))
 glow.save(A/f'textures/entity/{name}_glow.png');write(O/f'{name}-rig.json',{'atlas':size,'bones':r.bones,'boxes':r.boxes})
 return im
def held(r):
 elements=[]
 for b in r.boxes:
  if b['bone']!='scythe':continue
  cx,cy,cz=b['center'];w,h,d=b['size'];u,v,_,_=b['uv'];k=.75
  lo=[8+(cx+14-w/2)*k,-5+(cy-h/2)*k,8+(cz+3-d/2)*k];hi=[lo[0]+w*k,lo[1]+h*k,lo[2]+d*k]
  uv={'west':[u,v+d,u+d,v+d+h],'north':[u+d,v+d,u+d+w,v+d+h],'east':[u+d+w,v+d,u+2*d+w,v+d+h],'south':[u+2*d+w,v+d,u+2*d+2*w,v+d+h],'up':[u+d,v,u+d+w,v+d],'down':[u+d+w,v,u+d+2*w,v+d]}
  elements.append({'from':lo,'to':hi,'faces':{face:{'texture':'#surface','uv':[value*16/512 for value in values]} for face,values in uv.items()}})
 write(A/'models/item/karachun_scythe_held.json',{'textures':{'surface':'slavicmyths:entity/karachun'},'elements':elements,'display':{'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[1,1,1]},'thirdperson_lefthand':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[1,1,1]},'firstperson_righthand':{'rotation':[0,-10,0],'translation':[0,1,-2],'scale':[.8,.8,.8]},'firstperson_lefthand':{'rotation':[0,10,0],'translation':[0,1,-2],'scale':[.8,.8,.8]}}})
 write(A/'models/item/karachun_scythe.json',{'parent':'builtin/entity','gui_light':'front'});write(A/'models/item/karachun_scythe_icon.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/karachun_scythe'}})
def icons():
 for name in ['karachun_scythe','rune_of_waning','black_wing_amulet','shard_of_last_night','karachun_trial_totem']:
  im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);black='#242735';edge='#676974';ice='#A9C6EE';violet='#AC79FF'
  if name=='karachun_scythe':
   d.rectangle((23,7,25,30),fill='#44323C');d.line([(24,8),(19,5),(13,5),(7,8),(3,16)],fill=black,width=5);d.line([(23,7),(18,4),(12,4),(6,7),(2,15)],fill=violet,width=1)
   for x,y in [(18,8),(13,8),(8,11),(4,15)]:d.polygon([(x,y),(x+2,y),(x,y+5)],fill=ice)
   for y in [10,17,24,29]:d.rectangle((22,y,26,y+1),fill=edge)
  elif name=='rune_of_waning':
   d.polygon([(10,2),(22,4),(26,25),(21,30),(6,27),(5,8)],fill=black,outline=edge);d.line([(16,6),(23,15),(16,24),(9,15),(16,6)],fill=violet,width=2);d.line((16,4,16,27),fill='#D4B8FF',width=1)
   for x,y in [(5,9),(6,21),(23,5),(24,22)]:d.rectangle((x,y,x+3,y+4),fill=ice)
  elif name=='black_wing_amulet':
   d.arc((3,1,28,18),0,180,fill=edge,width=2)
   for s in [-1,1]:
    for i in range(5):x=16+s*(4+i*2);d.polygon([(16+s*3,13),(x,12+i),(x-s,23+i),(16+s*3,18)],fill=black,outline=edge)
   d.polygon([(16,12),(20,18),(16,27),(12,18)],fill=ice);d.line((16,16,16,23),fill=violet,width=2)
  elif name=='shard_of_last_night':
   d.polygon([(18,1),(25,11),(23,25),(12,31),(7,21),(10,7)],fill=black,outline='#6F49BC');d.polygon([(18,3),(18,26),(12,28),(13,13)],fill='#6F49BC');d.line((18,5,16,26),fill=violet,width=2)
   for x,y in [(8,13),(22,20),(12,5)]:d.polygon([(x,y),(x+3,y+2),(x+1,y+6)],fill=ice)
  else:
   d.rectangle((9,8,23,28),fill=black,outline=edge);d.rectangle((6,27,26,30),fill=edge)
   for x,y in [(8,5),(16,1),(24,5)]:d.polygon([(x,y),(x+2,y+4),(x+1,10),(x-2,8)],fill=ice)
   d.line((16,10,16,25),fill=violet,width=2);d.line([(11,13),(21,21),(16,25),(11,21),(21,13)],fill=ice,width=1)
  (A/'textures/item').mkdir(parents=True,exist_ok=True);im.save(A/f'textures/item/{name}.png')
  if name!='karachun_scythe':write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
def audio():
 names=['ambient_low_wind','enter_trial','voice_whisper','scythe_windup','scythe_hit','dark_frost','long_night_cast','touch_hunger','touch_frost','touch_death','last_hour_mark','last_hour_impact','fear_phantom_spawn','fear_phantom_dispel','yield','raven_call','raven_flap','raven_dive','raven_hit','life_eater','last_breath','black_harvest','wing_activate','waning_stage_up'];sounds={};ff=shutil.which('ffmpeg');assert ff
 for index,name in enumerate(names):
  variants=3 if name in ['raven_call','raven_flap','ambient_low_wind','voice_whisper'] else 1;entries=[]
  for variant in range(variants):
   rnd=random.Random(1920+index*3+variant);rate=22050;duration=2.8 if name=='ambient_low_wind' else 1.7 if name in ['long_night_cast','yield','last_hour_mark'] else .65;count=int(rate*duration);smooth=0;samples=[]
   for i in range(count):
    t=i/rate;u=t/duration;noise=rnd.uniform(-1,1);smooth=smooth*.93+noise*.07;envelope=min(1,t*40)*max(0,1-u)**1.7;freq=82+index*3+variant*7
    low=math.sin(math.tau*freq*t)*.22+math.sin(math.tau*(freq*1.49)*t)*.08
    if name.startswith('raven'):signal=math.sin(math.tau*(280+80*math.sin(t*14))*t)*(.35 if name!='raven_flap' else .08)+smooth*.7;envelope*=.25+.75*max(0,math.sin(t*18))
    elif name in ['touch_frost','waning_stage_up','fear_phantom_dispel']:signal=math.sin(math.tau*(920+index*31)*t)*math.exp(-t*7)*.3+noise*.07
    elif 'scythe' in name or 'harvest' in name or name=='last_breath':signal=smooth*.8+low*math.exp(-t*6)+math.sin(math.tau*610*t)*.04
    else:signal=low+smooth*.55+noise*.02
    samples.append(int(max(-.8,min(.8,signal*envelope))*32767))
   tmp=O/f'{name}-{variant}.wav';dest=A/f'sounds/karachun/{name}_{variant}.ogg';dest.parent.mkdir(parents=True,exist_ok=True)
   with wave.open(str(tmp),'wb') as out:out.setnchannels(1);out.setsampwidth(2);out.setframerate(rate);out.writeframes(struct.pack('<'+'h'*len(samples),*samples))
   subprocess.run([ff,'-hide_banner','-loglevel','error','-y','-i',str(tmp),'-fflags','+bitexact','-flags:a','+bitexact','-c:a','libvorbis','-q:a','3','-map_metadata','-1',str(dest)],check=True);tmp.unlink();entries.append({'name':f'slavicmyths:karachun/{name}_{variant}','attenuation_distance':32})
  sounds['karachun.'+name]={'subtitle':'subtitle.slavicmyths.karachun.'+name,'sounds':entries}
 merge(A/'sounds.json',sounds)
def resources():
 names={'karachun_scythe':('Коса Карачуна','Karachun’s Scythe'),'rune_of_waning':('Руна угасания','Rune of Waning'),'black_wing_amulet':('Чёрное крыло','Black Wing'),'shard_of_last_night':('Осколок последней ночи','Shard of the Last Night'),'karachun_trial_totem':('Тотем призыва Карачуна','Karachun Trial Totem')}
 ru={'boss':'Карачун','boss_phase':'Карачун · фаза %s','success':'Испытание пройдено — Карачун отступает','start':'Карачун услышал зов. Выдержи последнюю ночь!','overworld':'Испытание Карачуна доступно в Верхнем мире','active':'Испытание Карачуна уже идёт','space':'Нужно больше свободного места','received':'Получено: %s','queued':'Награда сохранена. Освободи места: %s','cancel':'Испытание прервано; незавершённый бой закрыт','trust':'Заслужи доверие Яги или угости Баюна','offering':'Подношение: 4 обсидиана и 1 синий лёд','offhand':'Угасание: второстепенная рука временно недоступна','totem_hint':'Многоразовый призыв по твёрдой поверхности. Репутация не нужна для готового тотема. Одно испытание одновременно.','npc_title':'Карачун','npc_offer':'Доверие II Яги или первое угощение Баюна. 4 обсидиана + 1 синий лёд; утраченный тотем восстанавливается.','scythe_hint':'ЛКМ — три тяжёлых удара по дуге; способности: Последний вздох и Чёрная жатва.','scythe_passives':'Жатва душ: каждый третий hit по цели — Иссушение I. Пожиратель жизни: до 2 HP / 5 с. Нежить: +25% урона, +3 HP за убийство.','waning':'Угасание %s · %s','fear':'Страх %s','cooldowns':'Вздох %s с · Жатва %s с · Крыло %s с','bindings':'%s — Последний вздох; %s — Чёрная жатва; %s — Чёрное крыло','reduced':'Сниженные искажения: %s','compatible.waning':'Вставляется в оружие; 20% на +25 Угасания, не чаще раза в секунду по цели.'}
 en={'boss':'Karachun','boss_phase':'Karachun · phase %s','success':'Trial endured — Karachun yields','start':'Karachun answers. Endure the last night!','overworld':'Karachun requires the Overworld','active':'A Karachun trial is already active','space':'More free space is needed','received':'Received: %s','queued':'Rewards queued. Free slots: %s','cancel':'The unfinished trial has been cancelled','trust':'Earn Yaga’s trust or feed Bayun','offering':'Offering: 4 obsidian and 1 blue ice','offhand':'Waning temporarily blocks your offhand','totem_hint':'Reusable surface summon; possession requires no reputation. One trial at a time.','npc_title':'Karachun','npc_offer':'Yaga trust II or Bayun’s first gift. 4 obsidian + 1 blue ice; lost totems can be restored.','scythe_hint':'Attack: three heavy arc swings; Last Breath and Black Harvest abilities.','scythe_passives':'Every third target hit: Wither I. Life Eater: up to 2 HP / 5s. Undead: +25% damage, 3 HP per kill.','waning':'Waning %s · %s','fear':'Fear %s','cooldowns':'Breath %ss · Harvest %ss · Wing %ss','bindings':'%s — Last Breath; %s — Black Harvest; %s — Black Wing','reduced':'Reduced distortions: %s','compatible.waning':'Weapon socket: 20% for +25 Waning, once per target per second.'}
 descriptions={'rune_of_waning':('20% шанс на +25 Угасания при настоящем попадании. Замедление → головокружение → блокировка второй руки; тотем бессмертия работает.','20% chance for +25 Waning on real hits. Slow → dizziness → offhand block; undying totems remain safe.'),'black_wing_amulet':('В слоте ожерелья: два союзных ворона на 20 с, ожидание 150 с. Награда только за первый успех; утраченный амулет не дублируется.','Necklace slot: two allied ravens for 20s, 150s cooldown. First clear only; lost amulets are not duplicated.'),'shard_of_last_night':('Гарантированная награда каждого испытания. Компонент будущего оберега; рецепта пока нет.','Guaranteed each trial. A future ward component; no recipe yet.')}
 attacks=['Погребальный взмах','Обратная жатва','Последний удар','Прикосновение голода','Прикосновение мороза','Прикосновение смерти','Долгая ночь','Первобытный страх','Последний час'];english=['Funeral Sweep','Reverse Reap','Final Stroke','Touch of Hunger','Touch of Frost','Touch of Death','Long Night','Primal Fear','Last Hour']
 for locale,local,idx in [('ru_ru',ru,0),('en_us',en,1)]:
  out={'night192.'+k:v for k,v in local.items()};out.update({'item.slavicmyths.'+n:v[idx] for n,v in names.items()});out.update({'item.slavicmyths.'+n+'.desc':v[idx] for n,v in descriptions.items()});out.update({'entity.slavicmyths.karachun':'Карачун' if idx==0 else 'Karachun','entity.slavicmyths.karachun_raven':'Ворон Карачуна' if idx==0 else 'Karachun Raven','entity.slavicmyths.karachun_blade':'Призрачное лезвие' if idx==0 else 'Spectral Blade','effect.slavicmyths.waning':'Угасание' if idx==0 else 'Waning','effect.slavicmyths.fear':'Страх' if idx==0 else 'Fear','bayun.dialogue.karachun':local['npc_title']})
  for key,value in [('breath','Последний вздох' if idx==0 else 'Last Breath'),('harvest','Чёрная жатва' if idx==0 else 'Black Harvest'),('wing','Чёрное крыло' if idx==0 else 'Black Wing'),('reduced','Снизить искажения' if idx==0 else 'Reduce distortions')]:out['key.slavicmyths.night_'+key]=value
  out['key.categories.slavicmyths.night']='Карачун' if idx==0 else 'Karachun'
  for i,name in enumerate(attacks if idx==0 else english,1):out['night192.attack.'+str(i)]=name
  for name in NightSounds:out['subtitle.slavicmyths.karachun.'+name]=('Карачун: ' if idx==0 else 'Karachun: ')+(name.replace('_',' ') if idx else {'ambient_low_wind':'пустой зимний ветер','enter_trial':'зов испытания','voice_whisper':'хриплый шёпот','scythe_windup':'замах косы','scythe_hit':'удар косы','dark_frost':'тёмный иней','long_night_cast':'наступает долгая ночь','touch_hunger':'прикосновение голода','touch_frost':'прикосновение мороза','touch_death':'прикосновение смерти','last_hour_mark':'отсчёт последнего часа','last_hour_impact':'удар последнего часа','fear_phantom_spawn':'ложный шорох','fear_phantom_dispel':'видение рассыпается','raven_call':'карканье ворона','raven_dive':'ворон пикирует','raven_flap':'хлопки крыльев','raven_hit':'удар клюва','yield':'буря затихает','life_eater':'жизнь перетекает','last_breath':'последний вздох','black_harvest':'чёрная жатва','wing_activate':'зов чёрного крыла','waning_stage_up':'угасание усиливается'}[name])
  out['advancement.slavicmyths.endured_last_night']='Последняя ночь' if idx==0 else 'The Last Night';out['advancement.slavicmyths.endured_last_night.desc']=local['success'];out['tales.entry.karachun_trial.title']=local['boss']
  pages=['Карачун — неоднозначный образ зимы и смерти в фольклоре. Коса, вороны и это испытание — игровая интерпретация, а не установленный исторический набор атрибутов.','Яга или Баюн могут дать многоразовый тотем. Подношение: 4 обсидиана и синий лёд. Готовый тотем работает без проверки репутации.','Угасание замедляет, вызывает краткое головокружение и блокирует только вторую руку. Отойди от тёмного мороза; тотем бессмертия остаётся надёжным.','Страх показывает ложных мобов. Они исчезают от попытки удара и не наносят урон. Настоящие вороны предупреждают о пикировании криком. Долгая ночь локальна.','На 5% здоровья Карачун сдаётся. Каждому участнику — руна и осколок, впервые — Чёрное крыло; коса выпадает независимо. Последний час отмечает точку заранее: покинь круг.'] if idx==0 else ['Karachun is an ambiguous folklore image of winter and death. Scythe, ravens and this trial are game interpretations.','Yaga or Bayun supplies a reusable totem for 4 obsidian and blue ice. Possession requires no reputation.','Waning slows, briefly distorts vision and blocks only offhand use. Leave the dark frost; undying totems remain safe.','Fear creates harmless illusions. Real ravens warn before diving. Long Night is local.','At 5% Karachun yields. Each contributor receives a rune and shard; the first clear grants Black Wing. The scythe is an independent roll. Leave the marked Last Hour circle.']
  for i,page in enumerate(pages):out[f'tales.entry.karachun_trial.text.{i}']=page
  for stage in ['known','rumor','seen','discovered','studied','verified']:out['tales.entry.karachun_trial.'+stage]=' ' .join(pages if stage=='verified' else pages[:2])
  merge(A/f'lang/{locale}.json',out)
 write(R/'src/main/resources/data/curios/tags/item/necklace.json',add_tag(R/'src/main/resources/data/curios/tags/item/necklace.json','slavicmyths:black_wing_amulet'))
 for tag in ['rune_heavy','rune_capacity_3']:write(D/f'tags/item/{tag}.json',add_tag(D/f'tags/item/{tag}.json','slavicmyths:karachun_scythe'))
 for tag in ['weapon','sharp_weapon','sword','durability']:
  path=R/f'src/main/resources/data/minecraft/tags/item/enchantable/{tag}.json';write(path,add_tag(path,'slavicmyths:karachun_scythe'))
 write(D/'tales/karachun_trial.json',{'part':'creatures','chapter':'legends','section':'inhabitants','title':'tales.entry.karachun_trial.title','icon':'slavicmyths:karachun_trial_totem','initial':0,'subjects':['entity/slavicmyths:karachun'],'advancements':['slavicmyths:endured_last_night'],'links':[]})
 write(D/'advancement/endured_last_night.json',{'parent':'slavicmyths:root','display':{'icon':{'id':'slavicmyths:shard_of_last_night'},'title':{'translate':'advancement.slavicmyths.endured_last_night'},'description':{'translate':'advancement.slavicmyths.endured_last_night.desc'},'frame':'challenge','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'event':{'trigger':'minecraft:impossible'}}})
NightSounds=['ambient_low_wind','enter_trial','voice_whisper','scythe_windup','scythe_hit','dark_frost','long_night_cast','touch_hunger','touch_frost','touch_death','last_hour_mark','last_hour_impact','fear_phantom_spawn','fear_phantom_dispel','yield','raven_call','raven_flap','raven_dive','raven_hit','life_eater','last_breath','black_harvest','wing_activate','waning_stage_up']
def main():
 O.mkdir(parents=True,exist_ok=True);r=boss();emit(r,'karachun','KarachunMesh');held(r);bird=raven();emit(bird,'karachun_raven','NightRavenMesh',256);icons();audio();resources();__import__('weapon_patch_assets').main();print('KARACHUN_ASSETS',len(r.bones),len(r.boxes),len(bird.bones),len(bird.boxes))
if __name__=='__main__':main()
```

## tools/rare_weapon_resources_1322.py

```python
"""Four distinct authored pixel silhouettes; additive canonical resource overlay."""
from pathlib import Path
import json
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
NAMES={'tugarin_sword':('Меч Тугарина','Tugarin Sword'), 'likho_staff':('Посох Лихо','Likho Staff'), 'ataman_flail':('Кистень атамана',"Ataman's Flail"), 'victor_spear':('Копьё победителя',"Victor's Spear"), 'ataman_sign':('Знак атамана',"Ataman's Sign")}
HINTS={
 'tugarin_sword':('Оба меча в руках: +40% урона полной атаки; кровотечение до 3 стаков, 6 с, 0,5 HP/стак каждые 2 с. Руны срабатывают только с основной руки. Рецепт выдаёт второй меч в ячейку верстака.', 'Both blades held: +40% charged damage; bleeding up to 3 stacks, 6 s, 0.5 HP/stack every 2 s. Only main-hand runes trigger. The recipe leaves the second blade in the bench.'),
 'likho_staff':('В руках: +1 HP раз в 9 с. ПКМ: морок до 12 блоков, стан 1,25 с, дезориентация 6 с; КД 28 с.', 'Held: +1 HP every 9 s. Use: morok within 12 blocks, 1.25 s stun, 6 s disorientation; 28 s cooldown.'),
 'ataman_flail':('Полные попадания: до 3 стаков Размаха. Следующий удар +35% урона, сильный толчок и давление на щит. Пауза 4 с сбрасывает серию.', 'Charged hits: up to 3 Swing stacks. Next hit: +35% damage, strong push and shield pressure. A 4 s pause resets the sequence.'),
 'victor_spear':('Полный удар дальше 3 блоков от края цели: повторный выпад по той же цели без задержки в течение 1,25 с; КД 8 с.', 'Charged hit beyond 3 blocks from target bounds: repeat thrust at the same target without delay within 1.25 s; 8 s cooldown.')}
def write(path,data):
 p=RES/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def append(tag,ids):
 path=f'data/{tag}.json';p=RES/path;data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
 data['values']=list(dict.fromkeys(data['values']+['slavicmyths:'+i for i in ids]));write(path,data)
def icon(id):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);edge='#201b22';metal='#514650';light='#bcb6bd';red='#a13230';gold='#d5ae52';wood='#503528'
 if id=='tugarin_sword':
  d.line([(7,26),(12,21)],fill=edge,width=5);d.line([(7,26),(12,21)],fill=wood,width=3)
  d.polygon([(10,19),(14,21),(26,9),(28,3),(23,6)],fill=edge)
  d.polygon([(12,19),(14,19),(25,8),(26,5),(23,8)],fill=metal)
  d.line([(15,16),(18,13),(20,13),(23,8)],fill='#de6736');d.line([(9,18),(16,23)],fill=gold,width=2)
  d.rectangle((5,26,8,28),fill=metal)
 elif id=='likho_staff':
  d.line([(5,29),(12,22),(14,14),(20,9)],fill=edge,width=5);d.line([(5,28),(11,22),(13,14),(20,9)],fill=wood,width=3)
  d.polygon([(17,11),(16,5),(20,2),(25,3),(28,7),(26,12),(21,14)],fill=edge)
  d.line([(18,9),(18,5),(21,3),(25,4),(27,7),(25,11),(21,12)],fill='#b6a994',width=2)
  d.polygon([(19,7),(22,5),(25,7),(22,10)],fill=red);d.rectangle((22,6,22,9),fill='#efb17a')
  d.line([(11,21),(14,23)],fill=red,width=2)
 elif id=='ataman_flail':
  d.line([(5,26),(11,15)],fill=edge,width=7);d.line([(5,26),(11,15)],fill=wood,width=4)
  d.line([(6,23),(9,24)],fill=red,width=2);d.line([(8,19),(11,20)],fill=red,width=2)
  d.line([(10,14),(15,8),(20,9),(23,17)],fill=edge,width=4)
  for x,y in [(11,12),(14,9),(18,9),(21,12),(22,15)]:d.rectangle((x,y,x+2,y+2),outline=light)
  d.polygon([(20,18),(26,17),(30,21),(29,27),(25,30),(19,28),(17,23)],fill=edge)
  d.polygon([(20,20),(26,19),(28,22),(27,26),(24,28),(20,26),(19,23)],fill=metal)
  d.line([(21,20),(25,20),(27,23)],fill=light,width=2);d.rectangle((26,16,27,19),fill=metal)
 elif id=='victor_spear':
  d.line([(2,30),(24,8)],fill=edge,width=4);d.line([(3,29),(23,9)],fill=wood,width=2)
  d.polygon([(21,11),(22,6),(29,0),(30,5),(25,12)],fill=edge)
  d.polygon([(23,9),(24,6),(29,2),(28,6),(25,10)],fill=light)
  d.line([(21,10),(24,13)],fill=gold,width=3);d.polygon([(21,13),(18,16),(21,17),(23,14)],fill=red)
  d.line([(6,25),(8,27)],fill=gold,width=2)
 else:
  d.polygon([(9,6),(22,6),(27,12),(25,23),(16,28),(7,23),(5,12)],fill=edge)
  d.polygon([(10,8),(21,8),(25,13),(23,22),(16,25),(9,22),(7,13)],fill=metal)
  d.rectangle((10,12,22,19),outline=gold,width=2);d.line([(12,16),(20,16)],fill=red,width=2)
 return im
def main():
 for id in NAMES:
  path=RES/f'assets/slavicmyths/textures/item/{id}.png';path.parent.mkdir(parents=True,exist_ok=True);icon(id).save(path)
  write(f'assets/slavicmyths/models/item/{id}.json',{'parent':'minecraft:item/generated' if id=='ataman_sign' else 'minecraft:item/handheld','textures':{'layer0':'slavicmyths:item/'+id}})
 weapons=list(HINTS)
 for tag,ids in {'rune_capacity_3':weapons,'rune_staff':['likho_staff'],'rune_heavy':['ataman_flail'],'rune_spear':['victor_spear']}.items():append('slavicmyths/tags/item/'+tag,ids)
 for tag in ('weapon','sharp_weapon','durability'):append('minecraft/tags/item/enchantable/'+tag,weapons)
 recipes={
  'tugarin_sword':['minecraft:diamond_sword','minecraft:diamond_sword','slavicmyths:tugarinova_kozha','slavicmyths:tugarinova_kozha','slavicmyths:tugarinova_kozha','slavicmyths:weapon_wrap'],
  'likho_staff':['slavicmyths:carved_staff','slavicmyths:oko_likha','slavicmyths:spear_shaft','slavicmyths:nit_durnoy_doli','slavicmyths:nit_durnoy_doli'],
  'ataman_flail':['slavicmyths:diamond_flail','slavicmyths:ataman_sign','slavicmyths:flail_chain','slavicmyths:weapon_wrap'],
  'victor_spear':['slavicmyths:diamond_spear','slavicmyths:princely_seal','slavicmyths:druzhinnik_blade_fragment','slavicmyths:silver_fitting','slavicmyths:weapon_wrap']}
 for id,ingredients in recipes.items():
  write(f'data/slavicmyths/recipe/rare_{id}.json',{'type':'slavicmyths:armorer_shapeless','copy_components':True,'ingredients':[{'item':i} for i in ingredients],'result':{'id':'slavicmyths:'+id,'count':1}})
 path='data/slavicmyths/loot_table/entities/ataman.json';data=json.loads((RES/path).read_text());pool={'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:ataman_sign'}]}
 if pool not in data['pools']:data['pools'].append(pool)
 write(path,data)
 for n,locale in enumerate(('ru_ru','en_us')):
  path=f'assets/slavicmyths/lang/{locale}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
  for id,names in NAMES.items():data['item.slavicmyths.'+id]=names[n]
  for id,hint in HINTS.items():data['tooltip.slavicmyths.weapon.'+id]=hint[n]
  for key,names in {'effect.slavicmyths.rare_bleeding':('Кровотечение','Bleeding'),'effect.slavicmyths.disorientation':('Дезориентация','Disorientation'),'rare.slavicmyths.swing':('Размах: %s/3','Swing: %s/3'),'rare.slavicmyths.second':('Второй выпад готов','Second thrust ready')}.items():data[key]=names[n]
  write(path,data)
 for id,color in [('rare_bleeding','#8c2528'),('disorientation','#602b72')]:
  im=Image.new('RGBA',(18,18));d=ImageDraw.Draw(im)
  if id=='rare_bleeding':d.polygon([(9,1),(3,10),(4,15),(9,17),(14,15),(15,10)],fill=color);d.line([(6,10),(6,13),(8,14)],fill='#e3a3a3')
  else:d.ellipse((1,4,16,13),outline='#b28cbf',width=2);d.ellipse((6,5,11,12),fill=color);d.line([(2,1),(15,16)],fill='#a95b67',width=2)
  im.save(RES/f'assets/slavicmyths/textures/mob_effect/{id}.png')
 sheet=Image.new('RGBA',(5*120,135),'#d4c2a2');d=ImageDraw.Draw(sheet)
 for n,id in enumerate(NAMES):sheet.alpha_composite(icon(id).resize((96,96),Image.Resampling.NEAREST),(n*120+12,8));d.text((n*120+4,112),id,fill='#201b22')
 sheet.save(ROOT/'docs/media/rare-weapons-1322.png')
 print('Rare weapon resources generated: 5 items, 4 bench recipes, 2 effects.')
 # Preserve the current combat/model overlay when regenerating historical resources.
 import combat_fix_resources_1344
 combat_fix_resources_1344.apply()
 import unique_armor_resources
 unique_armor_resources.apply()
 import weapon_patch_assets
 weapon_patch_assets.main()
if __name__=='__main__':main()
```

## tools/rewards_models.py

```python
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
root=ROOT/'src/main/resources/assets/slavicmyths/models/item'
p=ROOT/'src/main/java/org/slavicmyths/client/ClientSetup.java'
s=p.read_text(encoding='utf-8').replace('org.slavicmyths.equipment.UniqueEquipment.WARD_SHIELD.get() })','org.slavicmyths.equipment.UniqueEquipment.WARD_SHIELD.get(),org.slavicmyths.rewards.RewardContent.item("gift_ilya_shield") })')
p.write_text(s,encoding='utf-8')
p=root/'gift_ilya_shield.json';d=json.loads(p.read_text())
d['display']={'firstperson_lefthand':{'rotation':[0,180,5],'translation':[2,0,-1],'scale':[1,1,1]},'thirdperson_lefthand':{'rotation':[0,180,0],'translation':[0,3,0],'scale':[.9,.9,.9]}}
d['overrides']=[{'predicate':{'blocking':1},'model':'slavicmyths:item/gift_ilya_shield_blocking'}]
p.write_text(json.dumps(d,indent=2)+'\n')
faces={face:{'uv':[0,0,16,16],'texture':'#front'} for face in ['north','south','east','west','up','down']}
d={'textures':{'front':'slavicmyths:item/gift_ilya_shield','particle':'slavicmyths:item/gift_ilya_shield'},'elements':[{'from':[3,1,7],'to':[13,15,8],'faces':faces},{'from':[4,0,6.8],'to':[12,16,7.2],'faces':faces}]}
d['display']={}
for hand,rotation,translation in [('lefthand',[0,180,-5],[5,2,-3]),('righthand',[0,0,5],[-5,2,-3])]:
 d['display']['firstperson_'+hand]={'rotation':rotation,'translation':translation,'scale':[1.2,1.2,1.2]}
 d['display']['thirdperson_'+hand]={'rotation':rotation,'translation':[0,3,0],'scale':[1,1,1]}
(root/'gift_ilya_shield_blocking.json').write_text(json.dumps(d,indent=2)+'\n')
p=root/'trophy_kalin_sabre.json';d=json.loads(p.read_text());d['parent']='minecraft:item/handheld';p.write_text(json.dumps(d,indent=2)+'\n')
from PIL import Image
for id,palette in [('trophy_idol_chest',['#493524','#806544','#ddd7c7','#e7d4ab']),('trophy_marinka_cloak',['#1e1a24','#39323a','#5c2035','#981e43']),('trophy_kartaus_mantle',['#15161b','#343238','#625a61','#901921'])]:
 im=Image.new('RGBA',(128,128));px=im.load()
 for y in range(128):
  for x in range(128):
   tile=2 if id=='trophy_idol_chest' and x>=40 and y<32 else 3 if id=='trophy_kartaus_mantle' and x>=60 and y>=48 else 0 if y<48 else 1
   px[x,y]=tuple(bytes.fromhex(palette[tile][1:]))+(255,)
 dest=ROOT/'src/main/resources/assets/slavicmyths/textures/models/armor'/(id+'.png');dest.parent.mkdir(parents=True,exist_ok=True);im.save(dest)

from weapon_patch_assets import main as weapon_patch
weapon_patch()
```

## tools/weapon_patch_assets.py

```python
"""Final weapon resource layer. Run after the original resource generators."""
from pathlib import Path
import json
from PIL import Image, ImageDraw

R = Path(__file__).resolve().parents[1]
A = R / 'src/main/resources/assets/slavicmyths'
def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def box(lo, hi, color):
    return {'from': lo, 'to': hi, 'faces': {f: {'texture': '#surface', 'uv': [color, 0, color+1, 1]} for f in ['north','south','east','west','up','down']}}

def main():
    # Scythe: 30-unit dark wooden haft, 18-unit hooked steel blade, 1-unit blade
    # thickness, two iron ferrules. No boss-size chains, teeth or emissive ribbons.
    palette = ['#49392d','#715640','#343840','#646b73','#b5b9ba','#817c94','#bda25d']
    atlas = Image.new('RGBA', (16,16))
    for i, color in enumerate(palette):
        ImageDraw.Draw(atlas).rectangle((i,0,i,15),fill=color)
    atlas.save(A/'textures/item/weapon_scythe_surface.png')
    es = [box([8,-6,7],[9.5,24,8.5],0), box([7.7,5,6.7],[9.8,7,8.8],2),
          box([7.7,21,6.7],[9.8,23,8.8],2), box([2,23,7.25],[9.5,26,8.25],2),
          box([-3,22,7.25],[2,25,8.25],3), box([-7,19,7.25],[-3,23,8.25],3),
          box([-9,15,7.25],[-7,20,8.25],4), box([-10,12,7.25],[-9,17,8.25],4)]
    display = {'gui': {'rotation':[0,0,0],'scale':[.5]*3},
        'ground': {'translation':[0,3,0],'scale':[.35]*3},
        'fixed': {'scale':[.45]*3}, 'head': {'translation':[0,8,0],'scale':[.4]*3}}
    for side, sign in [('righthand',1),('lefthand',-1)]:
        display['firstperson_'+side] = {'rotation':[0,sign*-65,sign*12], 'translation':[sign*3,-2,-3], 'scale':[.52]*3}
        display['thirdperson_'+side] = {'rotation':[0,sign*90,0], 'translation':[0,1,0], 'scale':[.75]*3}
    write(A/'models/item/karachun_scythe_held.json', {'textures':{'surface':'slavicmyths:item/weapon_scythe_surface','particle':'slavicmyths:item/weapon_scythe_surface'},'elements':es,'display':display})
    icon=Image.new('RGBA',(16,16));d=ImageDraw.Draw(icon)
    d.line((12,3,12,15),fill=palette[1],width=1)
    d.line([(12,3),(10,1),(6,1),(3,3),(1,7)],fill=palette[3],width=2)
    d.line([(10,2),(6,2),(3,4),(1,8)],fill=palette[4],width=1)
    for y in [5,11]:d.line((11,y,13,y),fill=palette[2])
    icon.save(A/'textures/item/karachun_scythe.png')
    # Distinct silhouettes specified before construction:
    # Victor: 26-unit oak shaft + narrow 6-unit steel point, brass collar.
    # Likho: crooked 24-unit dark staff + asymmetric 5-unit iron eye socket.
    # Ataman: 12-unit grip + hanging 7-unit chain + 5-unit faceted iron weight.
    # Voivode: 4-unit leather grip + 17-unit curved, single-edged steel blade.
    shapes = {
        'victor_spear': [box([7, -5,7],[8.5,21,8.5],1),box([6.5,19,6.5],[9,21,9],6),box([6.5,21,7.25],[9,25,8.25],3),box([7.25,25,7.25],[8.25,28,8.25],4)],
        'likho_staff': [box([7,-3,7],[9,15,9],0),box([8,14,7],[10,21,9],0),box([7,20,6.5],[11,25,9.5],2),box([8,21,6],[10,23,6.5],5),box([6,24,7],[9,26,9],0)],
        'ataman_flail': [box([7,0,7],[9,12,9],0),box([6.5,10,6.5],[9.5,12,9.5],6),box([8,12,7.5],[9,15,8.5],3),box([9,14,7.5],[10,17,8.5],3),box([10,16,7.5],[11,20,8.5],3),box([9,19,6],[14,24,11],2),box([8.5,20,7],[14.5,23,10],3)],
        'trophy_kalin_sabre': [box([7,0,7],[9,4,9],0),box([5,4,6.5],[11,5.5,9.5],6),box([7,5.5,7.25],[9,14,8.25],3),box([8,13,7.25],[10,19,8.25],3),box([9,18,7.25],[11,22,8.25],4),box([10,21,7.25],[11,24,8.25],4)]}
    for name, elements in shapes.items():
        placement = {'gui':{'rotation':[0,0,-30],'scale':[.65]*3},'ground':{'translation':[0,3,0],'scale':[.4]*3},'fixed':{'scale':[.6]*3},'head':{'translation':[0,7,0],'scale':[.4]*3}}
        for side,sign in [('righthand',1),('lefthand',-1)]:
            placement['firstperson_'+side]={'rotation':[0,sign*-75,sign*15],'translation':[sign*1,-1,-2],'scale':[.6]*3}
            placement['thirdperson_'+side]={'rotation':[0,sign*90,0],'translation':[0,1,0],'scale':[.75]*3}
        write(A/f'models/item/{name}.json',{'textures':{'surface':'slavicmyths:item/weapon_scythe_surface','particle':'slavicmyths:item/weapon_scythe_surface'},'elements':elements,'display':placement})
    # Only placement polish on the existing crossbow family; geometry/materials retained.
    for name in ['serpent_crossbow','three_breath_crossbow']:
        for p in (A/'models/item').glob(name+'*.json'):
            m=json.loads(p.read_text(encoding='utf-8'))
            if not m.get('elements'):continue
            for side,sign in [('righthand',1),('lefthand',-1)]:
                m['display']['firstperson_'+side]={'rotation':[0,sign*-90,sign*15],'translation':[sign*1,0,-1],'scale':[.55]*3}
            m['display']['head']={'rotation':[0,0,0],'translation':[0,6,0],'scale':[.4]*3}
            write(p,m)
    for lang in ['ru_ru','en_us']:
        p=A/f'lang/{lang}.json';m=json.loads(p.read_text(encoding='utf-8'))
        m['night192.scythe_hint'] = ('ЛКМ — тяжёлый взмах по дуге 140°, дальность 3,5 блока. Полная подготовка: 1 секунда.' if lang=='ru_ru' else 'Attack: heavy 140° arc, 3.5-block reach. Full recovery: 1 second.')
        m['night192.scythe_passives'] = ('Каждый третий удар по цели: Иссушение I на 4 с. Лечение до 2 HP раз в 5 с; нежить: +25% урона, +3 HP за убийство.' if lang=='ru_ru' else 'Every third hit on a target: Wither I for 4s. Heal up to 2 HP every 5s; undead: +25% damage, +3 HP per kill.')
        write(p,m)

if __name__=='__main__':main()
```

## tools/verify_weapon_patch.py

```python
"""Production/resource gates; deliberately does not claim gameplay acceptance."""
from pathlib import Path
import csv, hashlib, json, math, zipfile
from PIL import Image
from check_event_subscribers import check as subscribers
from weapon_patch_assets import main as generate

R=Path(__file__).resolve().parents[1]
O=R/'docs/verification/weapon-patch'
J=R/'build/libs/slavicmyths-1.9.7.jar'
checks=0
def need(ok, label):
    global checks
    checks+=1
    if not ok: raise AssertionError(label)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    O.mkdir(parents=True,exist_ok=True)
    log=(R/'.tools/weapon-patch-clean-build.log').read_text(encoding='utf-8',errors='replace')
    need('BUILD SUCCESSFUL' in log,'clean build')
    need('NIGHT_OFFLINE_PASS' in log,'native boss regression')
    assets=R/'src/main/resources'
    before={p.relative_to(assets).as_posix():sha(p) for p in assets.rglob('*') if p.is_file()}
    generate()
    need(before=={p.relative_to(assets).as_posix():sha(p) for p in assets.rglob('*') if p.is_file()},'patch generator reproduces exactly')
    rows=[]
    with zipfile.ZipFile(J) as z:
        names=set(z.namelist())
        need('META-INF/neoforge.mods.toml' in names,'NeoForge production metadata')
        need(not any(n.startswith(('tools/','smoke/')) or 'NightChecks' in n for n in names),'test exclusion')
        for n,h in before.items():
            if n == "META-INF/neoforge.mods.toml":continue
            need(n in names and hashlib.sha256(z.read(n)).hexdigest()==h,'exact packaged resource '+n)
        for n in names:
            if n.endswith('.json'):
                d=json.loads(z.read(n));need(isinstance(d,(dict,list)),'JSON '+n)
                if n.startswith('assets/slavicmyths/models/item/'):
                    rows.append({'model':Path(n).stem,'parent':d.get('parent','geometry'),'cuboids':len(d.get('elements',[])),'contexts':','.join(d.get('display',{})),'acceptance':'STATIC_ONLY; GAME_NOT_RUN'})
                    for e in d.get('elements',[]):
                        need(all(math.isfinite(v) and -16<=v<=32 for k in ['from','to'] for v in e[k]),'model coordinate bounds '+n)
                        need(all(a<=b for a,b in zip(e['from'],e['to'])),'model extents '+n)
            if n.endswith('.png'):
                import io
                im=Image.open(io.BytesIO(z.read(n)));im.verify();need(True,'PNG '+n)
        base='assets/slavicmyths/models/item/'
        held=json.loads(z.read(base+'karachun_scythe_held.json'))
        need(len(held['elements'])==8,'compact scythe')
        need(all(e['to'][2]>e['from'][2] for e in held['elements']),'solid blade thickness')
        for name in ['karachun_scythe_held','victor_spear','likho_staff','ataman_flail','trophy_kalin_sabre','serpent_crossbow','three_breath_crossbow']:
            m=json.loads(z.read(base+name+'.json'))
            for context in ['firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand','gui','ground','fixed','head']:
                need(context in m['display'],'complete contexts '+name+' '+context)
        for lang in ['ru_ru','en_us']:
            m=json.loads(z.read('assets/slavicmyths/lang/'+lang+'.json'))
            for name in ['karachun_scythe','victor_spear','likho_staff','ataman_flail','trophy_kalin_sabre','serpent_crossbow','three_breath_crossbow']:
                need('item.slavicmyths.'+name in m,'localized item '+name)
            need('hit' not in m['night192.scythe_passives'] if lang=='ru_ru' else True,'Russian terminology')
    count=subscribers(J)
    with (O/'ITEM_MODELS.csv').open('w',encoding='utf-8-sig',newline='') as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(sorted(rows,key=lambda r:r['model']))
    result={'status':'PASS','patch':'Патч оружия','version_unchanged':'1.9.7','jar':str(J),'sha256':sha(J),'checks':checks,'item_models_audited':len(rows),'subscribers':count,'client_starts':0,'server_starts':0,'gameplay':'NOT_RUN','visual_acceptance':'REQUIRES_MANUAL_QA'}
    (O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
```

## tools/install_weapon_patch.ps1

```powershell
$ErrorActionPreference='Stop'
$taskRoot=(Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$instance=(Resolve-Path -LiteralPath 'C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing').Path
$mods=(Resolve-Path -LiteralPath (Join-Path $instance '.minecraft/mods')).Path
$report=Join-Path $taskRoot 'docs/verification/weapon-patch'
function Digest([string]$path){(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()}
$gate=Get-Content -LiteralPath (Join-Path $report 'resource-gates.json') -Raw | ConvertFrom-Json
$source=Join-Path $taskRoot 'build/libs/slavicmyths-1.9.7.jar'
if($gate.status -ne 'PASS' -or (Digest $source) -ne $gate.sha256){throw 'Production candidate differs from passed gates'}
if((Get-Content -LiteralPath (Join-Path $taskRoot '.tools/weapon-patch-clean-build.log') -Raw) -notmatch 'BUILD SUCCESSFUL'){throw 'Clean build PASS missing'}
$pack=Get-Content -LiteralPath (Join-Path $instance 'mmc-pack.json') -Raw | ConvertFrom-Json
if(($pack.components | Where-Object uid -eq 'net.minecraft').version -ne '1.21.1' -or ($pack.components | Where-Object uid -eq 'net.neoforged.neoforge').version -ne '21.1.255'){throw 'Wrong target platform'}
$minecraft=@(Get-CimInstance Win32_Process | Where-Object {$_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'net.minecraft.client.main.Main|--gameDir|cpw.mods.bootstraplauncher.BootstrapLauncher|org.multimc|org.polymc'})
if($minecraft.Count -ne 0){throw 'Minecraft process still running: close the game before installing'}
$prior=@(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths-*.jar' -File)
if($prior.Count -eq 0){throw 'Existing installed Slavic Myths JAR not found'}
$target=[IO.Path]::GetFullPath((Join-Path $mods 'slavicmyths-1.9.7.jar'))
$staged=$target+'.new'
foreach($path in @($target,$staged)+@($prior.FullName)){if([IO.Path]::GetDirectoryName([IO.Path]::GetFullPath($path)) -ne $mods){throw 'Unsafe file operation outside selected mods directory'}}
$oldPaths=@($prior.FullName);$oldHashes=@{};foreach($p in $prior){$oldHashes[$p.FullName]=Digest $p.FullName}
$before=@{}
Get-ChildItem -LiteralPath $instance -Recurse -File | Where-Object {$_.FullName -notin $oldPaths} | ForEach-Object {$before[$_.FullName]=Digest $_.FullName}
$backupPath=$null
if(-not ($prior.Count -eq 1 -and $prior[0].FullName -eq $target -and $oldHashes[$target] -eq $gate.sha256)){
 if(Test-Path -LiteralPath $staged){throw 'Existing staging file: preserve it and inspect before installation'}
 $stamp=[TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow,'Russian Standard Time').ToString('yyyyMMdd-HHmmss')
 $backupPath=[IO.Path]::GetFullPath((Join-Path $instance ('.slavicmyths-backups/pre-weapon-patch-'+$stamp)))
 if(-not $backupPath.StartsWith($instance+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Unsafe backup directory'}
 New-Item -ItemType Directory -Path $backupPath | Out-Null
 foreach($p in $prior){$saved=Join-Path $backupPath $p.Name;Copy-Item -LiteralPath $p.FullName -Destination $saved;if((Digest $saved) -ne $oldHashes[$p.FullName]){throw 'Backup checksum mismatch'}}
 Copy-Item -LiteralPath $source -Destination $staged
 if((Digest $staged) -ne $gate.sha256){throw 'Staging checksum mismatch'}
 try {
  foreach($p in $prior){Remove-Item -LiteralPath $p.FullName}
  Move-Item -LiteralPath $staged -Destination $target
  if((Digest $target) -ne $gate.sha256 -or @(Get-ChildItem -LiteralPath $mods -Filter 'slavicmyths-*.jar' -File).Count -ne 1){throw 'Installed checksum/duplicate check failed'}
  foreach($entry in $before.GetEnumerator()){if((Digest $entry.Key) -ne $entry.Value){throw ('Unrelated profile file changed: '+$entry.Key)}}
 }catch{
  if(Test-Path -LiteralPath $target){Remove-Item -LiteralPath $target}
  foreach($p in $prior){Copy-Item -LiteralPath (Join-Path $backupPath $p.Name) -Destination $p.FullName -Force}
  if(Test-Path -LiteralPath $staged){Remove-Item -LiteralPath $staged}
  throw
 }
}
foreach($entry in $before.GetEnumerator()){if((Digest $entry.Key) -ne $entry.Value){throw ('Profile preservation mismatch: '+$entry.Key)}}
$receipt=[ordered]@{patch='Патч оружия';version='1.9.7';status='PASS';instance=$instance;installed_jar=$target;sha256=(Digest $target);source=$source;previous_hashes=$oldHashes;backup=$backupPath;other_files_preserved=$before.Count;configuration_changes=0;client_starts=0;installer_server_starts=0;worlds='NOT_TOUCHED';old_1_16_5='NOT_TOUCHED'}
$receipt | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $report 'polymc-installation.json') -Encoding utf8
$markdown=@"
# PolyMC — Патч оружия поверх 1.9.7

Результат: **PASS**. Профиль: ``$instance``.

Установлен ``$target``. SHA256: ``$($receipt.sha256)``.

Предыдущие JAR сохранены в ``$backupPath`` с проверкой SHA256. Сохранены побайтно $($before.Count) остальных файлов профиля, включая конфигурации и миры. Дубликатов мода нет. Настройки Jade не менялись. Профиль/клиент/сервер установщиком не запускались; старый 1.16.5 не затронут.

Подробная квитанция: [polymc-installation.json](polymc-installation.json).
"@
[IO.File]::WriteAllText((Join-Path $report 'POLYMC_INSTALLATION.md'),$markdown,[Text.UTF8Encoding]::new($false))
$receipt | ConvertTo-Json -Depth 5

$cfg=Join-Path $instance 'instance.cfg'
$cfgBefore=[IO.File]::ReadAllText($cfg)
if(-not $backupPath){$backupPath=Join-Path $instance ('.slavicmyths-backups/weapon-patch-name-'+[DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss'));New-Item -ItemType Directory -Path $backupPath | Out-Null}
if(-not (Test-Path -LiteralPath (Join-Path $backupPath 'instance.cfg'))){Copy-Item -LiteralPath $cfg -Destination (Join-Path $backupPath 'instance.cfg')}
$cfgAfter=if($cfgBefore -match 'Патч оружия'){$cfgBefore}else{[regex]::Replace($cfgBefore,'(?m)^name=(.*?)(\r?)$',{param($m) 'name='+$m.Groups[1].Value+' — Патч оружия'+$m.Groups[2].Value})}
[IO.File]::WriteAllText($cfg,$cfgAfter,[Text.UTF8Encoding]::new($false))
$receipt.configuration_changes=1
$receipt | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $report 'polymc-installation.json') -Encoding utf8

$receipt.backup=$backupPath
$receipt | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $report 'polymc-installation.json') -Encoding utf8
Write-Output ('INSTALLED_WEAPON_PATCH '+$receipt.sha256)
```

## src/main/resources/assets/slavicmyths/models/item/karachun_scythe_held.json

```json
{
  "textures": {
    "surface": "slavicmyths:item/weapon_scythe_surface",
    "particle": "slavicmyths:item/weapon_scythe_surface"
  },
  "elements": [
    {
      "from": [
        8,
        -6,
        7
      ],
      "to": [
        9.5,
        24,
        8.5
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        }
      }
    },
    {
      "from": [
        7.7,
        5,
        6.7
      ],
      "to": [
        9.8,
        7,
        8.8
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        }
      }
    },
    {
      "from": [
        7.7,
        21,
        6.7
      ],
      "to": [
        9.8,
        23,
        8.8
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        }
      }
    },
    {
      "from": [
        2,
        23,
        7.25
      ],
      "to": [
        9.5,
        26,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        }
      }
    },
    {
      "from": [
        -3,
        22,
        7.25
      ],
      "to": [
        2,
        25,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        }
      }
    },
    {
      "from": [
        -7,
        19,
        7.25
      ],
      "to": [
        -3,
        23,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        }
      }
    },
    {
      "from": [
        -9,
        15,
        7.25
      ],
      "to": [
        -7,
        20,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        }
      }
    },
    {
      "from": [
        -10,
        12,
        7.25
      ],
      "to": [
        -9,
        17,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.5,
        0.5,
        0.5
      ]
    },
    "ground": {
      "translation": [
        0,
        3,
        0
      ],
      "scale": [
        0.35,
        0.35,
        0.35
      ]
    },
    "fixed": {
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "head": {
      "translation": [
        0,
        8,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -65,
        12
      ],
      "translation": [
        3,
        -2,
        -3
      ],
      "scale": [
        0.52,
        0.52,
        0.52
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        0,
        90,
        0
      ],
      "translation": [
        0,
        1,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        65,
        -12
      ],
      "translation": [
        -3,
        -2,
        -3
      ],
      "scale": [
        0.52,
        0.52,
        0.52
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        0,
        -90,
        0
      ],
      "translation": [
        0,
        1,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    }
  }
}
```

## src/main/resources/assets/slavicmyths/models/item/ataman_flail.json

```json
{
  "textures": {
    "surface": "slavicmyths:item/weapon_scythe_surface",
    "particle": "slavicmyths:item/weapon_scythe_surface"
  },
  "elements": [
    {
      "from": [
        7,
        0,
        7
      ],
      "to": [
        9,
        12,
        9
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        }
      }
    },
    {
      "from": [
        6.5,
        10,
        6.5
      ],
      "to": [
        9.5,
        12,
        9.5
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        }
      }
    },
    {
      "from": [
        8,
        12,
        7.5
      ],
      "to": [
        9,
        15,
        8.5
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        }
      }
    },
    {
      "from": [
        9,
        14,
        7.5
      ],
      "to": [
        10,
        17,
        8.5
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        }
      }
    },
    {
      "from": [
        10,
        16,
        7.5
      ],
      "to": [
        11,
        20,
        8.5
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        }
      }
    },
    {
      "from": [
        9,
        19,
        6
      ],
      "to": [
        14,
        24,
        11
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        }
      }
    },
    {
      "from": [
        8.5,
        20,
        7
      ],
      "to": [
        14.5,
        23,
        10
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        0,
        0,
        -30
      ],
      "scale": [
        0.65,
        0.65,
        0.65
      ]
    },
    "ground": {
      "translation": [
        0,
        3,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    },
    "fixed": {
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "head": {
      "translation": [
        0,
        7,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -75,
        15
      ],
      "translation": [
        1,
        -1,
        -2
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        0,
        90,
        0
      ],
      "translation": [
        0,
        1,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        75,
        -15
      ],
      "translation": [
        -1,
        -1,
        -2
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        0,
        -90,
        0
      ],
      "translation": [
        0,
        1,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    }
  }
}
```

## src/main/resources/assets/slavicmyths/models/item/likho_staff.json

```json
{
  "textures": {
    "surface": "slavicmyths:item/weapon_scythe_surface",
    "particle": "slavicmyths:item/weapon_scythe_surface"
  },
  "elements": [
    {
      "from": [
        7,
        -3,
        7
      ],
      "to": [
        9,
        15,
        9
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        }
      }
    },
    {
      "from": [
        8,
        14,
        7
      ],
      "to": [
        10,
        21,
        9
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        }
      }
    },
    {
      "from": [
        7,
        20,
        6.5
      ],
      "to": [
        11,
        25,
        9.5
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            2,
            0,
            3,
            1
          ]
        }
      }
    },
    {
      "from": [
        8,
        21,
        6
      ],
      "to": [
        10,
        23,
        6.5
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            5,
            0,
            6,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            5,
            0,
            6,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            5,
            0,
            6,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            5,
            0,
            6,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            5,
            0,
            6,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            5,
            0,
            6,
            1
          ]
        }
      }
    },
    {
      "from": [
        6,
        24,
        7
      ],
      "to": [
        9,
        26,
        9
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        0,
        0,
        -30
      ],
      "scale": [
        0.65,
        0.65,
        0.65
      ]
    },
    "ground": {
      "translation": [
        0,
        3,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    },
    "fixed": {
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "head": {
      "translation": [
        0,
        7,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -75,
        15
      ],
      "translation": [
        1,
        -1,
        -2
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        0,
        90,
        0
      ],
      "translation": [
        0,
        1,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        75,
        -15
      ],
      "translation": [
        -1,
        -1,
        -2
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        0,
        -90,
        0
      ],
      "translation": [
        0,
        1,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    }
  }
}
```

## src/main/resources/assets/slavicmyths/models/item/victor_spear.json

```json
{
  "textures": {
    "surface": "slavicmyths:item/weapon_scythe_surface",
    "particle": "slavicmyths:item/weapon_scythe_surface"
  },
  "elements": [
    {
      "from": [
        7,
        -5,
        7
      ],
      "to": [
        8.5,
        21,
        8.5
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            1,
            0,
            2,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            1,
            0,
            2,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            1,
            0,
            2,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            1,
            0,
            2,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            1,
            0,
            2,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            1,
            0,
            2,
            1
          ]
        }
      }
    },
    {
      "from": [
        6.5,
        19,
        6.5
      ],
      "to": [
        9,
        21,
        9
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        }
      }
    },
    {
      "from": [
        6.5,
        21,
        7.25
      ],
      "to": [
        9,
        25,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        }
      }
    },
    {
      "from": [
        7.25,
        25,
        7.25
      ],
      "to": [
        8.25,
        28,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        0,
        0,
        -30
      ],
      "scale": [
        0.65,
        0.65,
        0.65
      ]
    },
    "ground": {
      "translation": [
        0,
        3,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    },
    "fixed": {
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "head": {
      "translation": [
        0,
        7,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -75,
        15
      ],
      "translation": [
        1,
        -1,
        -2
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        0,
        90,
        0
      ],
      "translation": [
        0,
        1,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        75,
        -15
      ],
      "translation": [
        -1,
        -1,
        -2
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        0,
        -90,
        0
      ],
      "translation": [
        0,
        1,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    }
  }
}
```

## src/main/resources/assets/slavicmyths/models/item/trophy_kalin_sabre.json

```json
{
  "textures": {
    "surface": "slavicmyths:item/weapon_scythe_surface",
    "particle": "slavicmyths:item/weapon_scythe_surface"
  },
  "elements": [
    {
      "from": [
        7,
        0,
        7
      ],
      "to": [
        9,
        4,
        9
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            0,
            0,
            1,
            1
          ]
        }
      }
    },
    {
      "from": [
        5,
        4,
        6.5
      ],
      "to": [
        11,
        5.5,
        9.5
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            6,
            0,
            7,
            1
          ]
        }
      }
    },
    {
      "from": [
        7,
        5.5,
        7.25
      ],
      "to": [
        9,
        14,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        }
      }
    },
    {
      "from": [
        8,
        13,
        7.25
      ],
      "to": [
        10,
        19,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            3,
            0,
            4,
            1
          ]
        }
      }
    },
    {
      "from": [
        9,
        18,
        7.25
      ],
      "to": [
        11,
        22,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        }
      }
    },
    {
      "from": [
        10,
        21,
        7.25
      ],
      "to": [
        11,
        24,
        8.25
      ],
      "faces": {
        "north": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "south": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "east": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "west": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "up": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        },
        "down": {
          "texture": "#surface",
          "uv": [
            4,
            0,
            5,
            1
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        0,
        0,
        -30
      ],
      "scale": [
        0.65,
        0.65,
        0.65
      ]
    },
    "ground": {
      "translation": [
        0,
        3,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    },
    "fixed": {
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "head": {
      "translation": [
        0,
        7,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -75,
        15
      ],
      "translation": [
        1,
        -1,
        -2
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        0,
        90,
        0
      ],
      "translation": [
        0,
        1,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        75,
        -15
      ],
      "translation": [
        -1,
        -1,
        -2
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        0,
        -90,
        0
      ],
      "translation": [
        0,
        1,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    }
  }
}
```

## src/main/resources/assets/slavicmyths/models/item/serpent_crossbow.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/serpent_crossbow",
    "grip": "slavicmyths:item/serpent_crossbow",
    "seam": "slavicmyths:item/serpent_crossbow",
    "ivory": "slavicmyths:item/serpent_crossbow",
    "fire": "slavicmyths:item/serpent_crossbow",
    "mor": "slavicmyths:item/serpent_crossbow",
    "storm": "slavicmyths:item/serpent_crossbow",
    "particle": "slavicmyths:item/serpent_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        10
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front",
  "overrides": [
    {
      "predicate": {
        "pulling": 1
      },
      "model": "slavicmyths:item/serpent_crossbow_pulling_0"
    },
    {
      "predicate": {
        "pulling": 1,
        "pull": 0.58
      },
      "model": "slavicmyths:item/serpent_crossbow_pulling_1"
    },
    {
      "predicate": {
        "pulling": 1,
        "pull": 1
      },
      "model": "slavicmyths:item/serpent_crossbow_pulling_2"
    },
    {
      "predicate": {
        "charged": 1
      },
      "model": "slavicmyths:item/serpent_crossbow_arrow"
    },
    {
      "predicate": {
        "charged": 1,
        "firework": 1
      },
      "model": "slavicmyths:item/serpent_crossbow_firework"
    }
  ]
}
```

## src/main/resources/assets/slavicmyths/models/item/serpent_crossbow_arrow.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/serpent_crossbow",
    "grip": "slavicmyths:item/serpent_crossbow",
    "seam": "slavicmyths:item/serpent_crossbow",
    "ivory": "slavicmyths:item/serpent_crossbow",
    "fire": "slavicmyths:item/serpent_crossbow",
    "mor": "slavicmyths:item/serpent_crossbow",
    "storm": "slavicmyths:item/serpent_crossbow",
    "particle": "slavicmyths:item/serpent_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        10
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    },
    {
      "from": [
        7.6,
        7.2,
        0
      ],
      "to": [
        8.4,
        8,
        11
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front"
}
```

## src/main/resources/assets/slavicmyths/models/item/serpent_crossbow_firework.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/serpent_crossbow",
    "grip": "slavicmyths:item/serpent_crossbow",
    "seam": "slavicmyths:item/serpent_crossbow",
    "ivory": "slavicmyths:item/serpent_crossbow",
    "fire": "slavicmyths:item/serpent_crossbow",
    "mor": "slavicmyths:item/serpent_crossbow",
    "storm": "slavicmyths:item/serpent_crossbow",
    "particle": "slavicmyths:item/serpent_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        10
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    },
    {
      "from": [
        7.6,
        7.2,
        0
      ],
      "to": [
        8.4,
        8,
        11
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front"
}
```

## src/main/resources/assets/slavicmyths/models/item/serpent_crossbow_pulling_0.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/serpent_crossbow",
    "grip": "slavicmyths:item/serpent_crossbow",
    "seam": "slavicmyths:item/serpent_crossbow",
    "ivory": "slavicmyths:item/serpent_crossbow",
    "fire": "slavicmyths:item/serpent_crossbow",
    "mor": "slavicmyths:item/serpent_crossbow",
    "storm": "slavicmyths:item/serpent_crossbow",
    "particle": "slavicmyths:item/serpent_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        7
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front"
}
```

## src/main/resources/assets/slavicmyths/models/item/serpent_crossbow_pulling_1.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/serpent_crossbow",
    "grip": "slavicmyths:item/serpent_crossbow",
    "seam": "slavicmyths:item/serpent_crossbow",
    "ivory": "slavicmyths:item/serpent_crossbow",
    "fire": "slavicmyths:item/serpent_crossbow",
    "mor": "slavicmyths:item/serpent_crossbow",
    "storm": "slavicmyths:item/serpent_crossbow",
    "particle": "slavicmyths:item/serpent_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        8
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front"
}
```

## src/main/resources/assets/slavicmyths/models/item/serpent_crossbow_pulling_2.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/serpent_crossbow",
    "grip": "slavicmyths:item/serpent_crossbow",
    "seam": "slavicmyths:item/serpent_crossbow",
    "ivory": "slavicmyths:item/serpent_crossbow",
    "fire": "slavicmyths:item/serpent_crossbow",
    "mor": "slavicmyths:item/serpent_crossbow",
    "storm": "slavicmyths:item/serpent_crossbow",
    "particle": "slavicmyths:item/serpent_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        9
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front"
}
```

## src/main/resources/assets/slavicmyths/models/item/three_breath_crossbow.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/three_breath_crossbow",
    "grip": "slavicmyths:item/three_breath_crossbow",
    "seam": "slavicmyths:item/three_breath_crossbow",
    "ivory": "slavicmyths:item/three_breath_crossbow",
    "fire": "slavicmyths:item/three_breath_crossbow",
    "mor": "slavicmyths:item/three_breath_crossbow",
    "storm": "slavicmyths:item/three_breath_crossbow",
    "particle": "slavicmyths:item/three_breath_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        10
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    },
    {
      "from": [
        4,
        9,
        5
      ],
      "to": [
        6,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "south": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "east": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "west": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "up": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "down": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        9,
        5
      ],
      "to": [
        9,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "south": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "east": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "west": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "up": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "down": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        }
      }
    },
    {
      "from": [
        10,
        9,
        5
      ],
      "to": [
        12,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "south": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "east": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "west": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "up": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "down": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front",
  "overrides": [
    {
      "predicate": {
        "pulling": 1
      },
      "model": "slavicmyths:item/three_breath_crossbow_pulling_0"
    },
    {
      "predicate": {
        "pulling": 1,
        "pull": 0.58
      },
      "model": "slavicmyths:item/three_breath_crossbow_pulling_1"
    },
    {
      "predicate": {
        "pulling": 1,
        "pull": 1
      },
      "model": "slavicmyths:item/three_breath_crossbow_pulling_2"
    },
    {
      "predicate": {
        "charged": 1
      },
      "model": "slavicmyths:item/three_breath_crossbow_arrow"
    },
    {
      "predicate": {
        "charged": 1,
        "firework": 1
      },
      "model": "slavicmyths:item/three_breath_crossbow_firework"
    }
  ]
}
```

## src/main/resources/assets/slavicmyths/models/item/three_breath_crossbow_arrow.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/three_breath_crossbow",
    "grip": "slavicmyths:item/three_breath_crossbow",
    "seam": "slavicmyths:item/three_breath_crossbow",
    "ivory": "slavicmyths:item/three_breath_crossbow",
    "fire": "slavicmyths:item/three_breath_crossbow",
    "mor": "slavicmyths:item/three_breath_crossbow",
    "storm": "slavicmyths:item/three_breath_crossbow",
    "particle": "slavicmyths:item/three_breath_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        10
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    },
    {
      "from": [
        4,
        9,
        5
      ],
      "to": [
        6,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "south": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "east": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "west": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "up": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "down": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        9,
        5
      ],
      "to": [
        9,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "south": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "east": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "west": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "up": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "down": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        }
      }
    },
    {
      "from": [
        10,
        9,
        5
      ],
      "to": [
        12,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "south": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "east": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "west": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "up": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "down": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        }
      }
    },
    {
      "from": [
        7.6,
        7.2,
        0
      ],
      "to": [
        8.4,
        8,
        11
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front"
}
```

## src/main/resources/assets/slavicmyths/models/item/three_breath_crossbow_firework.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/three_breath_crossbow",
    "grip": "slavicmyths:item/three_breath_crossbow",
    "seam": "slavicmyths:item/three_breath_crossbow",
    "ivory": "slavicmyths:item/three_breath_crossbow",
    "fire": "slavicmyths:item/three_breath_crossbow",
    "mor": "slavicmyths:item/three_breath_crossbow",
    "storm": "slavicmyths:item/three_breath_crossbow",
    "particle": "slavicmyths:item/three_breath_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        10
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    },
    {
      "from": [
        4,
        9,
        5
      ],
      "to": [
        6,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "south": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "east": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "west": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "up": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "down": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        9,
        5
      ],
      "to": [
        9,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "south": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "east": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "west": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "up": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "down": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        }
      }
    },
    {
      "from": [
        10,
        9,
        5
      ],
      "to": [
        12,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "south": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "east": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "west": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "up": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "down": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        }
      }
    },
    {
      "from": [
        7.6,
        7.2,
        0
      ],
      "to": [
        8.4,
        8,
        11
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            0,
            0,
            8,
            8
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front"
}
```

## src/main/resources/assets/slavicmyths/models/item/three_breath_crossbow_pulling_0.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/three_breath_crossbow",
    "grip": "slavicmyths:item/three_breath_crossbow",
    "seam": "slavicmyths:item/three_breath_crossbow",
    "ivory": "slavicmyths:item/three_breath_crossbow",
    "fire": "slavicmyths:item/three_breath_crossbow",
    "mor": "slavicmyths:item/three_breath_crossbow",
    "storm": "slavicmyths:item/three_breath_crossbow",
    "particle": "slavicmyths:item/three_breath_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        7
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    },
    {
      "from": [
        4,
        9,
        5
      ],
      "to": [
        6,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "south": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "east": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "west": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "up": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "down": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        9,
        5
      ],
      "to": [
        9,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "south": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "east": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "west": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "up": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "down": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        }
      }
    },
    {
      "from": [
        10,
        9,
        5
      ],
      "to": [
        12,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "south": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "east": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "west": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "up": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "down": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front"
}
```

## src/main/resources/assets/slavicmyths/models/item/three_breath_crossbow_pulling_1.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/three_breath_crossbow",
    "grip": "slavicmyths:item/three_breath_crossbow",
    "seam": "slavicmyths:item/three_breath_crossbow",
    "ivory": "slavicmyths:item/three_breath_crossbow",
    "fire": "slavicmyths:item/three_breath_crossbow",
    "mor": "slavicmyths:item/three_breath_crossbow",
    "storm": "slavicmyths:item/three_breath_crossbow",
    "particle": "slavicmyths:item/three_breath_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        8
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    },
    {
      "from": [
        4,
        9,
        5
      ],
      "to": [
        6,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "south": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "east": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "west": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "up": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "down": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        9,
        5
      ],
      "to": [
        9,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "south": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "east": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "west": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "up": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "down": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        }
      }
    },
    {
      "from": [
        10,
        9,
        5
      ],
      "to": [
        12,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "south": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "east": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "west": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "up": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "down": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front"
}
```

## src/main/resources/assets/slavicmyths/models/item/three_breath_crossbow_pulling_2.json

```json
{
  "textures": {
    "metal": "slavicmyths:item/three_breath_crossbow",
    "grip": "slavicmyths:item/three_breath_crossbow",
    "seam": "slavicmyths:item/three_breath_crossbow",
    "ivory": "slavicmyths:item/three_breath_crossbow",
    "fire": "slavicmyths:item/three_breath_crossbow",
    "mor": "slavicmyths:item/three_breath_crossbow",
    "storm": "slavicmyths:item/three_breath_crossbow",
    "particle": "slavicmyths:item/three_breath_crossbow"
  },
  "elements": [
    {
      "from": [
        6.5,
        6,
        2
      ],
      "to": [
        9.5,
        9,
        16
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        5,
        10
      ],
      "to": [
        9,
        6,
        15
      ],
      "faces": {
        "north": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "south": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "east": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "west": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "up": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        },
        "down": {
          "texture": "#grip",
          "uv": [
            24,
            0,
            31,
            7
          ]
        }
      }
    },
    {
      "from": [
        -2,
        6,
        3
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -2,
        5,
        1
      ],
      "to": [
        1,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        15,
        5,
        1
      ],
      "to": [
        18,
        8,
        5
      ],
      "faces": {
        "north": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "south": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "east": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "west": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "up": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        },
        "down": {
          "texture": "#metal",
          "uv": [
            0,
            0,
            15,
            15
          ]
        }
      }
    },
    {
      "from": [
        -1,
        6,
        4
      ],
      "to": [
        0,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        16,
        6,
        4
      ],
      "to": [
        17,
        7,
        11
      ],
      "faces": {
        "north": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "south": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "east": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "west": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "up": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        },
        "down": {
          "texture": "#seam",
          "uv": [
            16,
            0,
            23,
            7
          ]
        }
      }
    },
    {
      "from": [
        0,
        6,
        9
      ],
      "to": [
        16,
        6.3,
        10.3
      ],
      "faces": {
        "north": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "south": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "east": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "west": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "up": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        },
        "down": {
          "texture": "#ivory",
          "uv": [
            16,
            8,
            23,
            15
          ]
        }
      }
    },
    {
      "from": [
        4,
        9,
        5
      ],
      "to": [
        6,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "south": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "east": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "west": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "up": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        },
        "down": {
          "texture": "#fire",
          "uv": [
            24,
            8,
            31,
            15
          ]
        }
      }
    },
    {
      "from": [
        7,
        9,
        5
      ],
      "to": [
        9,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "south": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "east": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "west": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "up": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        },
        "down": {
          "texture": "#mor",
          "uv": [
            16,
            16,
            23,
            23
          ]
        }
      }
    },
    {
      "from": [
        10,
        9,
        5
      ],
      "to": [
        12,
        12,
        8
      ],
      "faces": {
        "north": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "south": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "east": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "west": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "up": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        },
        "down": {
          "texture": "#storm",
          "uv": [
            24,
            16,
            31,
            23
          ]
        }
      }
    }
  ],
  "display": {
    "gui": {
      "rotation": [
        28,
        45,
        0
      ],
      "scale": [
        0.75,
        0.75,
        0.75
      ]
    },
    "ground": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.45,
        0.45,
        0.45
      ]
    },
    "fixed": {
      "rotation": [
        0,
        0,
        0
      ],
      "scale": [
        0.6,
        0.6,
        0.6
      ]
    },
    "firstperson_righthand": {
      "rotation": [
        0,
        -90,
        15
      ],
      "translation": [
        1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "firstperson_lefthand": {
      "rotation": [
        0,
        90,
        -15
      ],
      "translation": [
        -1,
        0,
        -1
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_righthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "thirdperson_lefthand": {
      "rotation": [
        -90,
        0,
        0
      ],
      "translation": [
        0,
        2,
        0
      ],
      "scale": [
        0.55,
        0.55,
        0.55
      ]
    },
    "head": {
      "rotation": [
        0,
        0,
        0
      ],
      "translation": [
        0,
        6,
        0
      ],
      "scale": [
        0.4,
        0.4,
        0.4
      ]
    }
  },
  "gui_light": "front"
}
```
