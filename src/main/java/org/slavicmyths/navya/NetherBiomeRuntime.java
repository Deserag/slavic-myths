package org.slavicmyths.navya;

import java.util.List;
import java.util.ArrayList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/** Runtime registry bridge for Nether sources created before ServerAboutToStartEvent. */
@EventBusSubscriber(modid="slavicmyths")
public final class NetherBiomeRuntime {
    private static volatile List<Holder<Biome>> active=List.of();

    public static void install(List<Holder<Biome>> holders){active=List.copyOf(holders);}
    public static List<Holder<Biome>> holders(){return active;}

    public static boolean isNether(List<Pair<Climate.ParameterPoint,Holder<Biome>>> entries){
        for(var entry:entries){
            ResourceLocation key=entry.getSecond().unwrapKey().map(k->k.location()).orElse(null);
            if(key!=null&&key.getNamespace().equals("minecraft")&&key.getPath().equals("nether_wastes"))return true;
        }
        return false;
    }

    public static Climate.ParameterPoint climate(Holder<Biome> holder){
        if(holder.is(org.slavicmyths.burningislands.IslandsContent.BIOME))return Climate.parameters(.9F,.9F,0,0,0,0,1F);
        if(holder.is(org.slavicmyths.losthalls.HallsContent.BIOME))return org.slavicmyths.losthalls.HallsClimate.POINT;
        if(holder.is(org.slavicmyths.blackcinder.CinderWorldgen.BIOME))return Climate.parameters(.7F,-.4F,0,0,0,0,.175F);
        return Climate.parameters(-.3F,.4F,0,0,0,0,.175F);
    }

    public static Climate.ParameterList<Holder<Biome>> enriched(Climate.ParameterList<Holder<Biome>> source,List<Holder<Biome>> holders){
        var entries=new ArrayList<>(source.values());
        if(!isNether(entries))return source;
        for(var holder:holders){
            if(entries.stream().noneMatch(entry->entry.getSecond().equals(holder)))entries.add(Pair.of(climate(holder),holder));
        }
        return new Climate.ParameterList<>(entries);
    }

    @SubscribeEvent public static void stopped(ServerStoppingEvent event){active=List.of();}
    private NetherBiomeRuntime(){}
}
