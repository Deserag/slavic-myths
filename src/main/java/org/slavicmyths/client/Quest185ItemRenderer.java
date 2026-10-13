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
