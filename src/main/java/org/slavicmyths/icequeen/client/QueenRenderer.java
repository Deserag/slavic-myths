package org.slavicmyths.icequeen.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.icequeen.*;
public final class QueenRenderer extends MobRenderer<IceQueen,QueenModel<IceQueen>>{
 public static final ResourceLocation WHITE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
 public QueenRenderer(EntityRendererProvider.Context c){super(c,new QueenModel<>(),.65F);}
 @Override public ResourceLocation getTextureLocation(IceQueen e){return WHITE;}
 @Override protected void scale(IceQueen e,PoseStack p,float partial){float s=e.phase()==2?1.48F:1;p.scale(s,s,s);}
 public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(QueenContent.BOSS.get(),QueenRenderer::new);for(var k:QueenContent.Kind.values())e.registerEntityRenderer(QueenContent.MINIONS.get(k).get(),c->new QueenMinionRenderer(c,k));}
}
