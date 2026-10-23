package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

/** One bounded model bank shared by visible whips. No entities, ticks or packets stored globally. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class WhipClient {
 private static final int PHASES=64,SWAY=16;
 private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
 @SubscribeEvent public static void additional(ModelEvent.RegisterAdditional e){e.register(ModelResourceLocation.standalone(id("item/poloz_whip_pose_0")));}
 @SubscribeEvent public static void bake(ModelEvent.ModifyBakingResult e){
  var key=ModelResourceLocation.inventory(id("poloz_whip"));var root=e.getModels().get(key);
  var source=e.getModels().get(ModelResourceLocation.standalone(id("item/poloz_whip_pose_0")));
  if(root==null||source==null||source.isCustomRenderer())return;
  var quads=source.getQuads(null,null,RandomSource.create(0));
  if(quads.size()!=216||quads.stream().anyMatch(q->q.getVertices().length!=32))return; // Resource-pack fallback.
  var bank=new ArrayList<List<BakedQuad>>(PHASES+1+SWAY);
  for(int i=0;i<=PHASES;i++)bank.add(deform(quads,i/(float)PHASES,0));
  for(int i=0;i<SWAY;i++)bank.add(deform(quads,0,(float)Math.sin(i*Math.PI*2/SWAY)));
  e.getModels().put(key,new Bound(root,source,List.copyOf(bank),null,null,root,new LinkedHashMap<>()));
 }
 private static boolean free(BakedQuad q){for(int i=0;i<4;i++)if(Float.intBitsToFloat(q.getVertices()[i*8+1])*16>5.0001)return true;return false;}
 private static List<BakedQuad> deform(List<BakedQuad> source,float phase,float sway){
  var out=new ArrayList<BakedQuad>(source.size());
  for(var q:source){
   int[] v=q.getVertices().clone();
   for(int i=0;i<4;i++){
    int o=i*8;double authoredY=Float.intBitsToFloat(v[o+1])*16;var p=WhipMotion.point(Float.intBitsToFloat(v[o])*16,authoredY,Float.intBitsToFloat(v[o+2])*16,phase,sway);
    v[o]=Float.floatToRawIntBits((float)(p.x()/16));v[o+1]=Float.floatToRawIntBits((float)(p.y()/16));v[o+2]=Float.floatToRawIntBits((float)(p.z()/16));
   }
   var a=new org.joml.Vector3f(Float.intBitsToFloat(v[8])-Float.intBitsToFloat(v[0]),Float.intBitsToFloat(v[9])-Float.intBitsToFloat(v[1]),Float.intBitsToFloat(v[10])-Float.intBitsToFloat(v[2]));
   var b=new org.joml.Vector3f(Float.intBitsToFloat(v[16])-Float.intBitsToFloat(v[0]),Float.intBitsToFloat(v[17])-Float.intBitsToFloat(v[1]),Float.intBitsToFloat(v[18])-Float.intBitsToFloat(v[2]));
   a.cross(b);Direction face=q.getDirection();
   if(a.lengthSquared()>1e-10F){a.normalize();face=Direction.getNearest(a.x,a.y,a.z);int normal=((byte)(a.x*127)&255)|(((byte)(a.y*127)&255)<<8)|(((byte)(a.z*127)&255)<<16);for(int i=0;i<4;i++)v[i*8+7]=normal;}
   out.add(new BakedQuad(v,q.getTintIndex(),face,q.getSprite(),q.isShade(),q.hasAmbientOcclusion()));
  }
  return List.copyOf(out);
 }
 private static final class Bound extends BakedModelWrapper<BakedModel> {
  private final BakedModel held,icon;private final List<List<BakedQuad>> bank;private final Map<PoseKey,List<BakedQuad>> aimed;
  private final LivingEntity owner;private final ItemStack stack;private List<BakedQuad> active;
  Bound(BakedModel root,BakedModel held,List<List<BakedQuad>> bank,LivingEntity owner,ItemStack stack,BakedModel icon,Map<PoseKey,List<BakedQuad>> aimed){super(root);this.held=held;this.bank=bank;this.owner=owner;this.stack=stack;this.icon=icon;this.aimed=aimed;}
  @Override public ItemOverrides getOverrides(){return new ItemOverrides(){
   @Override public BakedModel resolve(BakedModel base,ItemStack item,ClientLevel level,LivingEntity entity,int seed){
    BakedModel resolved=originalModel.getOverrides().resolve(originalModel,item,level,entity,seed);
    return new Bound(resolved==null?originalModel:resolved,held,bank,entity,item,icon,aimed);
   }
  };}
  @Override public BakedModel applyTransform(ItemDisplayContext context,PoseStack pose,boolean left){
   active=null;
   if(context==ItemDisplayContext.GUI)return icon.applyTransform(context,pose,left);
   boolean hand=context.firstPerson()||context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
   if(!hand||owner==null||stack==null||owner.isRemoved())return originalModel.applyTransform(context,pose,left);
   InteractionHand physical=(owner.getMainArm()==HumanoidArm.LEFT)==left?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
   if(!ItemStack.isSameItemSameComponents(owner.getItemInHand(physical),stack))return originalModel.applyTransform(context,pose,left);
   var transform=held.getTransforms().getTransform(context);
   if(context.firstPerson()){
    // A centered grip at vanilla's low wrist hides the complete lash below the FOV.
    // Keep the grip intact, raise its camera anchor and point the free lash forwards.
    pose.translate(0,.35,0);
    pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(left?15:-15));
    pose.scale(.65F,.65F,.65F);
   }else{
    cancelDisplayOffset(pose,transform,left);
    held.applyTransform(context,pose,left);
   }
   centerHandle(pose);
   float partial=Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
   float swing=owner.swinging&&owner.swingingArm==physical?owner.getAttackAnim(partial):0;
   int index=swing>0?Math.clamp(Math.round(swing*PHASES),0,PHASES):PHASES+1;
   if(swing==0&&owner.walkAnimation.speed(partial)>.05F)index+=Math.floorMod((int)((owner.tickCount+partial)*.6F),SWAY);
   active=bank.get(index);
   if(swing>0){
    if(context.firstPerson())aimFreeLashDirection(index,new org.joml.Matrix3f(pose.last().normal()).invert().transform(new org.joml.Vector3f(0,0,-1)).normalize());
    else aimFreeLash(context,left,index);
   }
   return this;
  }
  private void aimFreeLash(ItemDisplayContext context,boolean left,int index){
   if(!(owner instanceof net.minecraft.client.player.AbstractClientPlayer clientPlayer))return;
   var renderer=Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(clientPlayer);
   if(!(renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer player))return;
   var arm=left?player.getModel().leftArm:player.getModel().rightArm;
   var nativeHand=new PoseStack();arm.translateAndRotate(nativeHand);
   nativeHand.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));nativeHand.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
   held.getTransforms().getTransform(context).apply(left,nativeHand);
   var desired=new org.joml.Matrix3f(nativeHand.last().normal()).invert().transform(new org.joml.Vector3f(0,0,-1)).normalize();
   aimFreeLashDirection(index,desired);
  }
  private void aimFreeLashDirection(int index,org.joml.Vector3f desired){
   float phase=index/(float)PHASES;var end=WhipMotion.point(14.564620284687606,23.526787076386654,8,phase,0);
   var direction=new org.joml.Vector3f((float)(end.x()-8),(float)(end.y()-5),(float)(end.z()-8)).normalize();
   float extension=(float)Math.pow(Math.sin(Math.PI*phase),2);
   var rotation=new org.joml.Quaternionf().rotationTo(direction,desired);new org.joml.Quaternionf().slerp(rotation,extension,rotation);
   var key=new PoseKey(index,Math.round(rotation.x*64),Math.round(rotation.y*64),Math.round(rotation.z*64),Math.round(rotation.w*64));
   var cached=aimed.get(key);
   if(cached==null){
    var quantized=new org.joml.Quaternionf(key.x/64F,key.y/64F,key.z/64F,key.w/64F).normalize();var source=held.getQuads(null,null,RandomSource.create(0));var output=new ArrayList<BakedQuad>(active.size());
    for(int n=0;n<active.size();n++){
     var quad=active.get(n);if(!free(source.get(n))){output.add(quad);continue;}int[] vertices=quad.getVertices().clone();
     for(int i=0;i<4;i++){int o=i*8;var v=new org.joml.Vector3f(Float.intBitsToFloat(vertices[o])-.5F,Float.intBitsToFloat(vertices[o+1])-5F/16,Float.intBitsToFloat(vertices[o+2])-.5F).rotate(quantized);vertices[o]=Float.floatToRawIntBits(v.x+.5F);vertices[o+1]=Float.floatToRawIntBits(v.y+5F/16);vertices[o+2]=Float.floatToRawIntBits(v.z+.5F);int packed=vertices[o+7];var normal=new org.joml.Vector3f((byte)packed/127F,(byte)(packed>>8)/127F,(byte)(packed>>16)/127F).rotate(quantized);vertices[o+7]=((byte)(normal.x*127)&255)|(((byte)(normal.y*127)&255)<<8)|(((byte)(normal.z*127)&255)<<16);}
     var normal=quantized.transform(new org.joml.Vector3f(quad.getDirection().getStepX(),quad.getDirection().getStepY(),quad.getDirection().getStepZ()));output.add(new BakedQuad(vertices,quad.getTintIndex(),Direction.getNearest(normal.x,normal.y,normal.z),quad.getSprite(),quad.isShade(),quad.hasAmbientOcclusion()));
    }
    cached=List.copyOf(output);if(aimed.size()>=128)aimed.remove(aimed.keySet().iterator().next());aimed.put(key,cached);
   }
   active=cached;
  }
  @Override public List<BakedModel> getRenderPasses(ItemStack item,boolean fabulous){return List.of(this);}
  @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random){return active==null?originalModel.getQuads(state,side,random):side==null?active:List.of();}
  @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random,net.neoforged.neoforge.client.model.data.ModelData data,net.minecraft.client.renderer.RenderType type){return active==null?originalModel.getQuads(state,side,random,data,type):side==null?active:List.of();}
 }
 private record PoseKey(int phase,int x,int y,int z,int w){}
 /** Native ItemTransform mirrors its translation X for the physical left hand. */
 public static void cancelDisplayOffset(PoseStack pose,ItemTransform transform,boolean left){pose.translate(left?transform.translation.x:-transform.translation.x,-transform.translation.y,-transform.translation.z);}
 /** Actual authored grip is the centre of the unchanged 7,-4,7 .. 9,5,9 handle. */
 public static void centerHandle(PoseStack pose){pose.translate(0,7.5/16,0);}
 private WhipClient(){}
}
