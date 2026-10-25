package org.slavicmyths.chud;

import java.util.*;
import java.util.concurrent.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.slavicmyths.tales.*;

/** Existing tale journal: reuse a completed saved site or assign after actual queued construction. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ChudResearch {
    private static final Set<UUID> PENDING=ConcurrentHashMap.newKeySet();
    @net.neoforged.bus.api.SubscribeEvent public static void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent e){PENDING.clear();}
    private static CompoundTag state(ServerPlayer p){return TaleData.compound(TaleData.get(p.serverLevel()).player(p.getUUID()),"ChudResearch");}
    public static boolean offer(ServerPlayer p,String giver){
        if(p.level().dimension()!=Level.OVERWORLD||!TaleData.get(p.serverLevel()).compatible())return false;
        var root=state(p);var q=root.getCompound(giver);
        if(q.contains("Target")&&!q.getBoolean("CreatedSite")&&!q.getBoolean("Visited")&&!q.getBoolean("Claimed")){q.putLong("LegacyTarget",q.getLong("Target"));q.remove("Target");TaleData.get(p.serverLevel()).setDirty();}
        if(q.contains("Target")){
            if(!q.getBoolean("Visited")){guide(p,giver,q);return true;}
            if(ChudResearchProgress.claim(q)){TaleData.get(p.serverLevel()).setDirty();org.slavicmyths.yaga.YagaServices.give(p,new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.ANCIENT_SIGN.get()));p.displayClientMessage(Component.translatable("research.chud.reward"),false);sync(p);return true;}
        }
        if(!PENDING.add(p.getUUID()))return false;
        var world=p.serverLevel();UUID player=p.getUUID();var excluded=new HashSet<Long>();for(String key:root.getAllKeys()){var old=root.getCompound(key);if(old.contains("Target"))excluded.add(old.getLong("Target"));}for(long visited:root.getLongArray("Visited"))excluded.add(visited);
        var known=org.slavicmyths.worldgen.ManualStructureRecords.get(world).entries().stream().filter(e->e.family().equals("chud_city")&&!excluded.contains(e.arrival().asLong())).min(Comparator.comparingDouble(e->e.arrival().distSqr(p.blockPosition())));
        if(known.isPresent()){var e=known.get();assign(p,giver,new ChudCreationJobs.Result(e.arrival(),new ChunkPos(e.bounds().getCenter()),e.bounds(),1));PENDING.remove(player);return true;}
        int accepted=ChudCreationJobs.create(p.createCommandSourceStack(),"city",result->{try{var online=world.getServer().getPlayerList().getPlayer(player);if(online==null||online.serverLevel()!=world)return;if(result==null){online.displayClientMessage(Component.translatable("research.chud.unavailable"),false);return;}assign(online,giver,result);}finally{PENDING.remove(player);}});
        if(accepted==0)PENDING.remove(player);return accepted!=0;
    }
    private static void assign(ServerPlayer p,String giver,ChudCreationJobs.Result result){
        var assigned=new CompoundTag();assigned.putBoolean("CreatedSite",true);assigned.putLong("Target",result.entrance().asLong());assigned.putLong("Owner",result.owner().toLong());assigned.putString("Dimension",p.level().dimension().location().toString());var b=result.bounds();assigned.putIntArray("Bounds",new int[]{b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()});assigned.putInt("StartPieces",result.pieces());state(p).put(giver,assigned);TaleData.get(p.serverLevel()).setDirty();TaleService.promote(p,"slavicmyths:chud_research_"+giver,1,false);guide(p,giver,assigned);
    }
    private static void sync(ServerPlayer p){var delta=new CompoundTag();delta.put("Hints",TaleService.hints(p));delta.put("Research",TaleService.research(p));TaleNetwork.send(p,delta,false,false);}
    private static void guide(ServerPlayer p,String giver,CompoundTag q){
        var target=BlockPos.of(q.getLong("Target"));TaleService.hint(p,"slavicmyths:chud_research_"+giver,q.getString("Dimension"),UUID.nameUUIDFromBytes(("chud:"+q.getLong("Owner")).getBytes(java.nio.charset.StandardCharsets.UTF_8)),target);
        var hint=TaleData.get(p.serverLevel()).player(p.getUUID()).getCompound("Hints").getCompound("slavicmyths:chud_research_"+giver);hint.remove("Target");hint.putString("Source","entity.slavicmyths."+(giver.equals("yaga")?"baba_yaga":"kot_bayun"));hint.putLong("Expires",Long.MAX_VALUE);TaleData.get(p.serverLevel()).setDirty();
        p.displayClientMessage(Component.translatable("research.chud.assigned"),false);sync(p);
    }
    /** Called by the existing loaded-structure visit observer, never a second polling loop. */
    public static void visited(ServerPlayer p,StructureStart start){
        var id=p.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(start.getStructure());if(id==null||!id.getPath().equals("chud_city"))return;
        visitOwner(p,start.getChunkPos().toLong());
    }
    /** Large-city entrances can lie eight chunks away from their start. Validate the
     * native reference of the already-loaded player chunk, not a forced start chunk. */
    public static void visitedReference(ServerPlayer p,net.minecraft.world.level.levelgen.structure.Structure structure,long owner){
        var id=p.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(structure);if(id==null||!id.toString().equals("slavicmyths:chud_city"))return;
        var pos=p.blockPosition();var chunk=p.serverLevel().getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4);var references=chunk==null?null:chunk.getAllReferences().get(structure);if(references==null||!references.contains(owner))return;
        var root=state(p);for(String giver:List.of("yaga","bayun")){var q=root.getCompound(giver);if(!q.contains("Target")||q.getLong("Owner")!=owner||!q.getString("Dimension").equals(p.level().dimension().location().toString()))continue;
            int[] b=q.getIntArray("Bounds");var target=BlockPos.of(q.getLong("Target"));boolean inside=b.length==6?pos.getX()>=b[0]&&pos.getY()>=b[1]&&pos.getZ()>=b[2]&&pos.getX()<=b[3]&&pos.getY()<=b[4]&&pos.getZ()<=b[5]:pos.distSqr(target)<=8*8;
            if(inside){visitOwner(p,owner);return;}}
    }
    private static void visitOwner(ServerPlayer p,long owner){
        var root=state(p);var visited=new HashSet<Long>();for(long value:root.getLongArray("Visited"))visited.add(value);
        boolean changed=false;
        for(String giver:List.of("yaga","bayun")){var q=root.getCompound(giver);if(ChudResearchProgress.visit(q,p.level().dimension().location().toString(),owner)){changed=true;visited.add(q.getLong("Target"));TaleService.promote(p,"slavicmyths:chud_research_"+giver,3,false);p.displayClientMessage(Component.translatable("research.chud.visited"),false);}}
        var starts=new HashSet<Long>();for(long value:root.getLongArray("VisitedStarts"))starts.add(value);starts.add(owner);root.putLongArray("VisitedStarts",starts.stream().mapToLong(Long::longValue).toArray());
        root.putLongArray("Visited",visited.stream().mapToLong(Long::longValue).toArray());TaleData.get(p.serverLevel()).setDirty();
        if(changed)sync(p);
    }
    private ChudResearch(){}
}
