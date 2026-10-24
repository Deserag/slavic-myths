package org.slavicmyths.yaga;

import java.util.Random;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slavicmyths.tales.*;

/** Explicit book/login reconciliation; one canonical home, no repeated kills or AI scans. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class YagaDiscovery {
    private static final Map<net.minecraft.server.MinecraftServer,Search> SEARCHES=new HashMap<>();
    private static final net.minecraft.server.level.TicketType<net.minecraft.world.level.ChunkPos> SEARCH_TICKET=net.minecraft.server.level.TicketType.create("slavicmyths_yaga_search",Comparator.comparingLong(net.minecraft.world.level.ChunkPos::toLong),200);
    private static final class Search {
        final net.minecraft.server.level.ServerLevel world;final UUID player;final List<BlockPos> candidates=new ArrayList<>();
        int candidate,chunk;List<net.minecraft.world.level.ChunkPos> chunks=List.of();
        Search(ServerPlayer p){world=p.server.getLevel(Level.OVERWORLD);player=p.getUUID();int attempt=p.getPersistentData().getInt("YagaSearchAttempt");p.getPersistentData().putInt("YagaSearchAttempt",attempt+1);var random=new Random(world.getSeed()^player.getLeastSignificantBits()^((long)attempt*0x9e3779b97f4a7c15L));for(int i=0;i<8;i++){double angle=random.nextDouble()*Math.PI*2;int distance=1100+random.nextInt(1700);candidates.add(new BlockPos(p.getBlockX()+(int)(Math.cos(angle)*distance),0,p.getBlockZ()+(int)(Math.sin(angle)*distance)));}}
    }
    private static void release(Search s){for(var at:s.chunks)s.world.getChunkSource().removeRegionTicket(SEARCH_TICKET,at,0,at);}
    @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){var s=SEARCHES.remove(e.getServer());if(s!=null)release(s);}
    /** A pending explicit book request generates at most one chunk per tick, then uses transactional placement.
     * No player/world traversal; temporary tickets are released after each candidate. */
    @net.neoforged.bus.api.SubscribeEvent public static void step(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){
        var server=e.getServer();var search=SEARCHES.get(server);if(search==null)return;var world=search.world;var data=YagaData.get(world);
        if(data.placed||search.candidate>=search.candidates.size()){
            SEARCHES.remove(server);release(search);var p=server.getPlayerList().getPlayer(search.player);if(p!=null){reconcile(p,false);p.displayClientMessage(net.minecraft.network.chat.Component.translatable(data.placed?"research.yaga.found":"research.chud.unavailable"),false);TaleNetwork.send(p,TaleService.snapshot(p),false,true);}return;
        }
        var target=search.candidates.get(search.candidate);
        if(!world.getWorldBorder().isWithinBounds(target.offset(-12,0,-20))||!world.getWorldBorder().isWithinBounds(target.offset(12,0,12))){search.candidate++;return;}
        if(search.chunks.isEmpty()){
            var chunks=new ArrayList<net.minecraft.world.level.ChunkPos>();for(int x=(target.getX()-10)>>4;x<=(target.getX()+10)>>4;x++)for(int z=(target.getZ()-17)>>4;z<=(target.getZ()+10)>>4;z++)chunks.add(new net.minecraft.world.level.ChunkPos(x,z));search.chunks=chunks;search.chunk=0;
        }
        if(search.chunk<search.chunks.size()){var at=search.chunks.get(search.chunk++);world.getChunkSource().addRegionTicket(SEARCH_TICKET,at,0,at);world.getChunk(at.x,at.z);return;}
        boolean inhabited=false;for(var at:search.chunks)if(world.getChunk(at.x,at.z).getInhabitedTime()>0){inhabited=true;break;}
        if(!inhabited)YagaHut.place(world,target);release(search);search.candidate++;search.chunks=List.of();
    }
    public static boolean eligible(ServerPlayer p){return YagaServices.killed(p,"name_your_misfortune")||YagaServices.killed(p,"steppe_champion")||YagaServices.killed(p,"defeat_nightingale");}
    public static void reconcile(ServerPlayer p,boolean create){
        if(!eligible(p))return;
        TaleService.promote(p,"slavicmyths:yaga_search",1,false);
        var world=p.server.getLevel(Level.OVERWORLD);if(world==null)return;
        var data=YagaData.get(world);
        if(!data.placed&&create&&p.level().dimension()==Level.OVERWORLD){
            long now=world.getGameTime();var memory=p.getPersistentData();
            if(now<memory.getLong("YagaSearchReady"))return;
            memory.putLong("YagaSearchReady",now+1200);
            SEARCHES.computeIfAbsent(p.server,s->new Search(p));
            p.displayClientMessage(net.minecraft.network.chat.Component.translatable("research.yaga.searching"),false);
        }
        if(data.placed&&data.anchor!=null&&data.npc!=null){
            TaleService.hint(p,"slavicmyths:yaga_search",Level.OVERWORLD.location().toString(),data.npc,data.anchor);
            TaleService.promote(p,"slavicmyths:yaga_search",2,false);
        }
    }
    private YagaDiscovery(){}
}
