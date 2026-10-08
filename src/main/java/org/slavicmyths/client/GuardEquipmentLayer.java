package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import org.slavicmyths.military.*;
import org.slavicmyths.registry.ModItems;
/** Role-owned rendering equipment: never materialized into NPC inventory or death loot. */
public final class GuardEquipmentLayer extends RenderLayer<Villager,VillagerModel<Villager>> {
 private final ModelPart parts;private final net.minecraft.client.renderer.ItemInHandRenderer items;
 public GuardEquipmentLayer(RenderLayerParent<Villager,VillagerModel<Villager>> parent,EntityRendererProvider.Context ctx){super(parent);items=ctx.getItemInHandRenderer();parts=bakedParts();}
 private static ModelPart bakedParts(){var mesh=new MeshDefinition();var root=mesh.getRoot();root.addOrReplaceChild("right",CubeListBuilder.create().texOffs(0,32).addBox(-3,-2,-2,4,12,4),PartPose.offset(-5,2,0));root.addOrReplaceChild("left",CubeListBuilder.create().texOffs(0,32).mirror().addBox(-1,-2,-2,4,12,4),PartPose.offset(5,2,0));root.addOrReplaceChild("helmet",CubeListBuilder.create().texOffs(32,0).addBox(-4.4F,-11,-4.4F,8.8F,2,8.8F).texOffs(32,15).addBox(-3.4F,-12.5F,-3.4F,6.8F,1.5F,6.8F).texOffs(64,0).addBox(-1.8F,-14,-1.8F,3.6F,1.5F,3.6F).texOffs(32,25).addBox(-4.4F,-9,3.8F,8.8F,6,1),PartPose.ZERO);return LayerDefinition.create(mesh,128,128).bakeRoot();}
 public void render(PoseStack pose,MultiBufferSource buffers,int light,Villager v,float walk,float speed,float partial,float age,float yaw,float pitch){if(!Military.guard(v)||v.isInvisible())return;int kind=Math.max(0,v.getData(Military.ARCHETYPE));var vertices=buffers.getBuffer(RenderType.entityCutoutNoCull(org.slavicmyths.village.VillageRoles.id("textures/entity/guard_equipment.png")));int overlay=LivingEntityRenderer.getOverlayCoords(v,0);
  if(v.getItemBySlot(EquipmentSlot.HEAD).isEmpty()){pose.pushPose();getParentModel().getHead().translateAndRotate(pose);parts.getChild("helmet").render(pose,vertices,light,overlay);pose.popPose();}
  var right=parts.getChild("right");var left=parts.getChild("left");float hit=v.getAttackAnim(partial);right.xRot=kind==3?-1.25F:-.55F-(float)Math.sin(hit*Math.PI)*1.2F;right.yRot=kind==3?-.35F:0;left.xRot=kind==3?-1.2F:v.getData(Military.ACTION)==2?-1.1F:-.7F;left.yRot=kind==3?.35F:0;
  pose.pushPose();getParentModel().root().getChild("body").translateAndRotate(pose);right.render(pose,vertices,light,overlay);left.render(pose,vertices,light,overlay);
  held(v,new ItemStack(GuardBehavior.weapon(kind)),right,false,pose,buffers,light);
  if(GuardBehavior.shield(kind))held(v,new ItemStack(ModItems.RETAINER_SHIELD.get()),left,true,pose,buffers,light);pose.popPose();
 }
 private void held(Villager v,ItemStack stack,ModelPart arm,boolean left,PoseStack pose,MultiBufferSource buffers,int light){pose.pushPose();arm.translateAndRotate(pose);pose.translate(left?.0625:-.0625,.625,-.125);pose.mulPose(Axis.XP.rotationDegrees(-90));pose.mulPose(Axis.YP.rotationDegrees(180));items.renderItem(v,stack,left?ItemDisplayContext.THIRD_PERSON_LEFT_HAND:ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,left,pose,buffers,light);pose.popPose();}
}
