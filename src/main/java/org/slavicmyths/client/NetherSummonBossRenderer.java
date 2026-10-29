package org.slavicmyths.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.netherboss.NetherSummonBoss;

/** Native renderer for the original 53/54-cuboid Nether boss models. WIP VFX layers. */
public final class NetherSummonBossRenderer extends MobRenderer<NetherSummonBoss,NetherSummonBossModel> {
    private static final ResourceLocation BRIDE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/morova_bride.png");
    private static final ResourceLocation PRINCE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/trizna_flaming_prince.png");
    public NetherSummonBossRenderer(EntityRendererProvider.Context context,boolean bride){
        super(context,new NetherSummonBossModel(bride),bride?.44F:.7F);
    }
    @Override public ResourceLocation getTextureLocation(NetherSummonBoss boss){return boss.isMorova()?BRIDE:PRINCE;}
}
