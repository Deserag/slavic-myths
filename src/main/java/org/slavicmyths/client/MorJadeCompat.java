package org.slavicmyths.client;

import java.lang.reflect.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.LandSpiritEntity;

/** Optional Jade 15 callback, isolated from common code and without a compile/runtime dependency.
 * Only removes the health row of a concealed serpent; no configuration is modified.
 */
public final class MorJadeCompat {
    private static boolean warned;
    public static void install(){
        if(!net.neoforged.fml.ModList.get().isLoaded("jade"))return;
        try {
            ClassLoader loader=MorJadeCompat.class.getClassLoader();
            Class<?> callback=Class.forName("snownee.jade.api.callback.JadeTooltipCollectedCallback",false,loader);
            Method target=Class.forName("snownee.jade.api.Accessor",false,loader).getMethod("getTarget");
            Method tooltip=Class.forName("snownee.jade.api.ui.IBoxElement",false,loader).getMethod("getTooltip");
            Method remove=Class.forName("snownee.jade.api.ITooltip",false,loader).getMethod("remove",ResourceLocation.class);
            ResourceLocation health=(ResourceLocation)Class.forName("snownee.jade.api.JadeIds",false,loader).getField("MC_ENTITY_HEALTH").get(null);
            Object proxy=Proxy.newProxyInstance(loader,new Class<?>[]{callback},(self,method,args)->{
                if(method.getDeclaringClass()==Object.class)return switch(method.getName()){case "hashCode"->System.identityHashCode(self);case "equals"->self==args[0];default->"SlavicMythsMorHealthMask";};
                try {if(args!=null&&args.length==2&&target.invoke(args[1]) instanceof LandSpiritEntity mob&&mob.morConcealed())remove.invoke(tooltip.invoke(args[0]),health);}catch(ReflectiveOperationException failure){warn(failure);}return null;
            });
            Class<?> registration=Class.forName("snownee.jade.impl.WailaClientRegistration",false,loader);
            Object instance=registration.getMethod("instance").invoke(null);
            registration.getMethod("addTooltipCollectedCallback",int.class,callback).invoke(instance,10000,proxy);
        }catch(ReflectiveOperationException|LinkageError failure){warn(failure);}
    }
    private static void warn(Throwable failure){if(!warned){warned=true;com.mojang.logging.LogUtils.getLogger().warn("Unable to mask concealed mor serpent health in optional Jade",failure);}}
    private MorJadeCompat(){}
}
