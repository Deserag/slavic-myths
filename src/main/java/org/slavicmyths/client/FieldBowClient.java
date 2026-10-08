package org.slavicmyths.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.combat.*;

@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class FieldBowClient {
    public static void properties() {
        for(String id:new String[]{"short_bow","heavy_bow"}) {
            var bow=(FieldBowItem)WeaponCatalog.item(id);
            ItemProperties.register(bow,ResourceLocation.withDefaultNamespace("pull"),(stack,level,user,seed)->user==null||user.getUseItem()!=stack?0:bow.drawPower(stack.getUseDuration(user)-user.getUseItemRemainingTicks()));
            ItemProperties.register(bow,ResourceLocation.withDefaultNamespace("pulling"),(stack,level,user,seed)->user!=null&&user.isUsingItem()&&user.getUseItem()==stack?1:0);
        }
    }
    @SubscribeEvent public static void movement(MovementInputUpdateEvent event) {
        var player=event.getEntity();
        if(player.isUsingItem()&&!player.isPassenger()&&player.getUseItem().getItem() instanceof FieldBowItem bow) {
            float factor=bow.heavy?(player.getTicksUsingItem()>=bow.drawTicks()?.6F:1):2F;
            event.getInput().forwardImpulse*=factor;event.getInput().leftImpulse*=factor;
        }
    }
    private FieldBowClient() { }
}
