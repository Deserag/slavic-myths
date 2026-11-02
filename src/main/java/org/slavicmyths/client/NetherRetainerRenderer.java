package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import org.slavicmyths.netherboss.NetherRetainer;
/** Separate authored shadow/armored retainer atlases; never the default green zombie skin. */
public final class NetherRetainerRenderer extends ZombieRenderer {
 public NetherRetainerRenderer(EntityRendererProvider.Context context){super(context);}
 @Override public ResourceLocation getTextureLocation(Zombie entity){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(entity instanceof NetherRetainer retainer&&retainer.shadow()?"morova_shadow":"trizna_retainer")+".png");}
 @Override protected RenderType getRenderType(Zombie entity,boolean visible,boolean translucent,boolean glowing){return RenderType.entityTranslucent(getTextureLocation(entity));}
}
