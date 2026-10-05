package org.slavicmyths.navigation.client;

import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.navigation.NavigationState;

/** Client-only display target. Return true only when it provides tracked guidance. */
public interface MapIntegrationBridge {
    boolean publish(NavigationState state,ResourceLocation dimension);
    void clear();
    MapIntegrationBridge NONE=new MapIntegrationBridge(){
        public boolean publish(NavigationState state,ResourceLocation dimension){return false;}
        public void clear(){}
    };
}
