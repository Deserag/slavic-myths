package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.*;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.slavicmyths.kitchen.*;
import org.slavicmyths.block.KitchenTableBlock;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class KitchenDisplay {
 public static final Set<String> FOOD=Set.of("rye_bread","karavai","baked_turnip","stewed_turnip","stewed_cabbage","oat_porridge","barley_porridge","berry_porridge","vegetable_stew","meat_stew","pea_soup","mushroom_stew_slavic","ukha","bliny","berry_bliny","meat_bliny","apple_bliny","honey_bliny","apple_pie","berry_pie","meat_pie","cabbage_pie");
 private static ModelResourceLocation model(String name){return ModelResourceLocation.standalone(KitchenII.id("table_food/"+name));}
 @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){for(String n:FOOD)e.register(model(n));for(String n:List.of("metal_pot","rolling_pin","dough","formed_dough"))e.register(model(n));}
 public static void draw(ItemStack s,String dedicated,PoseStack p,MultiBufferSource b,int light,int overlay){var mc=Minecraft.getInstance();p.pushPose();if(dedicated!=null){p.translate(0,.5,0);mc.getItemRenderer().render(s,ItemDisplayContext.FIXED,false,p,b,light,overlay,mc.getModelManager().getModel(model(dedicated)));}else{p.translate(0,.03,0);p.mulPose(Axis.XP.rotationDegrees(90));mc.getItemRenderer().renderStatic(s,ItemDisplayContext.FIXED,light,overlay,p,b,mc.level,0);}p.popPose();}
 public static final class Table implements BlockEntityRenderer<TableTile>{public Table(BlockEntityRendererProvider.Context c){}
  public void render(TableTile t,float partial,PoseStack p,MultiBufferSource b,int l,int o){for(int i=0;i<4;i++){ItemStack s=t.food.get(i);if(s.isEmpty())continue;String id=BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();boolean own=BuiltInRegistries.ITEM.getKey(s.getItem()).getNamespace().equals("slavicmyths");p.pushPose();p.translate(i%2==0?.28:.72,1.01,i/2==0?.28:.72);p.scale(.48F,.48F,.48F);draw(s,own&&FOOD.contains(id)?id:null,p,b,l,o);p.popPose();}}
 }
 public static final class Kitchen implements BlockEntityRenderer<KitchenTile>{private long particleTick=Long.MIN_VALUE;public Kitchen(BlockEntityRendererProvider.Context c){}
  public net.minecraft.world.phys.AABB getRenderBoundingBox(KitchenTile t){return new net.minecraft.world.phys.AABB(t.getBlockPos()).inflate(1);}
  public void render(KitchenTile t,float partial,PoseStack p,MultiBufferSource b,int l,int o){Direction facing=t.getBlockState().getValue(KitchenTableBlock.FACING);p.pushPose();p.translate(.5,1.04,.5);p.mulPose(Axis.YP.rotationDegrees(facing==Direction.NORTH?0:facing==Direction.EAST?-90:facing==Direction.SOUTH?180:90));
   if(!t.inventory.getItem(0).isEmpty()){p.pushPose();p.translate(0,.025,t.busy()&&!t.activePot?Math.sin((t.progress+partial)*.12)*.12:0);p.scale(.75F,.75F,.75F);draw(t.inventory.getItem(0),"rolling_pin",p,b,l,o);p.popPose();}
   if(t.busy()&&!t.activePot){p.pushPose();p.translate(0,.005,0);p.scale(.7F,.7F,.7F);draw(new ItemStack(KitchenII.item("dough")),t.progress*2>=t.total?"formed_dough":"dough",p,b,l,o);p.popPose();}
   if(!t.inventory.getItem(1).isEmpty()){p.pushPose();p.translate(1,0,0);p.scale(.9F,.9F,.9F);draw(t.inventory.getItem(1),"metal_pot",p,b,l,o);if(t.servings>0&&!t.dish.isEmpty()){p.translate(0,.3,0);p.scale(.6F,.15F,.6F);draw(t.dish,BuiltInRegistries.ITEM.getKey(t.dish.getItem()).getPath(),p,b,l,o);}p.popPose();}
   p.popPose();long time=t.getLevel().getGameTime();int cadence=t.busy()&&t.activePot?20:80;if((t.busy()&&t.activePot||t.servings>0)&&time%cadence==0&&time!=particleTick){particleTick=time;Direction right=facing.getClockWise();t.getLevel().addParticle(ParticleTypes.SMOKE,t.getBlockPos().getX()+.5+right.getStepX(),t.getBlockPos().getY()+1.45,t.getBlockPos().getZ()+.5+right.getStepZ(),0,.015,0);}
  }
 }
}
