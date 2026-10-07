package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import org.slavicmyths.storage.*;
import org.slavicmyths.storage.Household.Kind;
public final class StorageDisplay implements BlockEntityRenderer<StorageTile>{
 public StorageDisplay(BlockEntityRendererProvider.Context c){}
 private static void draw(ItemStack s,PoseStack p,MultiBufferSource b,int l,int o,float scale,boolean hanging){if(s.isEmpty())return;String id=BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();boolean own=BuiltInRegistries.ITEM.getKey(s.getItem()).getNamespace().equals("slavicmyths");p.pushPose();p.scale(scale,scale,scale);
  if(!hanging&&own&&(KitchenDisplay.FOOD.contains(id)||id.equals("metal_pot")))KitchenDisplay.draw(s,id,p,b,l,o);
  else{boolean tool=s.getItem()instanceof TieredItem||s.getItem()instanceof ShieldItem||id.equals("rolling_pin");boolean food=s.has(net.minecraft.core.component.DataComponents.FOOD);if(!hanging&&(tool||food)){p.translate(0,.04,0);p.mulPose(Axis.XP.rotationDegrees(90));}else if(!hanging)p.translate(0,.45,0);Minecraft.getInstance().getItemRenderer().renderStatic(s,ItemDisplayContext.FIXED,l,o,p,b,Minecraft.getInstance().level,0);}p.popPose();
 }
 public void render(StorageTile t,float partial,PoseStack p,MultiBufferSource b,int l,int o){Kind kind=t.kind;if(kind!=Kind.BASKET&&kind!=Kind.LARGE&&kind!=Kind.CRATE&&kind!=Kind.WALL&&kind!=Kind.SHELF&&kind!=Kind.DRY)return;Direction f=t.getBlockState().getValue(StorageBlock.FACING);p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(f==Direction.NORTH?0:f==Direction.EAST?-90:f==Direction.SOUTH?180:90));
  int shown=0;for(int i=0;i<t.items.size();i++){ItemStack s=t.items.get(i);if(s.isEmpty())continue;double x,y,z;float scale=.43F;
   if(kind==Kind.WALL||kind==Kind.SHELF){x=(i%3-1)*(kind==Kind.WALL?.29:.22);y=kind==Kind.WALL?.5625:i<3?.4375:1.5625;z=kind==Kind.WALL?.31:.1;scale=kind==Kind.WALL?(s.getItem()instanceof BlockItem?.27F:.38F):(s.getItem()instanceof BlockItem?.18F:.29F);}
   else if(kind==Kind.DRY){x=-.25+i*.5;y=1.4;z=0;scale=t.stage(i)==1?.48F:t.stage(i)==2?.4F:.34F;}
   else{if(shown>=(kind==Kind.BASKET?4:6))break;int cols=kind==Kind.BASKET?2:3;x=(shown%cols-(cols-1)/2.0)*.29;z=(shown/cols-.5)*.36;y=kind==Kind.BASKET?.22:.35;scale=kind==Kind.CRATE?.47F:.4F;shown++;}
   p.pushPose();p.translate(x,y,z);draw(s,p,b,l,o,scale,kind==Kind.DRY);p.popPose();
  }p.popPose();
 }
}
