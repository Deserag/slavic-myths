package org.slavicmyths.progression;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.slavicmyths.chud.*;

/** Event-only progression. Visual parents never gate an award; no login historical import. */
public final class AdvancementJourney {
    public static void award(ServerPlayer p,String id){AdvancementMilestones.award(p,id);}
    public static void conversation(ServerPlayer p){award(p,"tales_come_alive");}

    /** Exact completed fermentation fact, including when its starter is offline. */
    public static boolean beerReady(ServerLevel w,UUID owner){
        if(owner==null)return false;
        var data=org.slavicmyths.tales.TaleData.get(w);if(!data.compatible())return false;
        data.player(owner).putBoolean("JourneyBeerPending",true);data.setDirty();
        var p=w.getServer().getPlayerList().getPlayer(owner);if(p!=null)deliverBeer(p);return true;
    }
    public static void deliverBeer(ServerPlayer p){
        if(!AdvancementMilestones.eligible(p))return;
        var data=org.slavicmyths.tales.TaleData.get(p.serverLevel());if(!data.compatible())return;var n=data.player(p.getUUID());
        if(!n.getBoolean("JourneyBeerPending"))return;
        award(p,"self_brewed_beer");
        if(Knowledge.knows(p,"self_brewed_beer")){n.remove("JourneyBeerPending");data.setDirty();}
    }
    public static BlockPos plantingPosition(UseOnContext c){if(!(c.getPlayer() instanceof ServerPlayer)||!(c.getItemInHand().getItem() instanceof net.minecraft.world.item.BlockItem seed)||!(seed.getBlock() instanceof CropBlock)||!BuiltInRegistries.BLOCK.getKey(seed.getBlock()).getNamespace().equals("slavicmyths"))return null;return new BlockPlaceContext(c).getClickedPos().immutable();}
    private static String cropKey(ServerPlayer p,BlockPos pos){return p.level().dimension().location()+"/"+pos.asLong();}
    private static CompoundTag crops(ServerPlayer p){var root=org.slavicmyths.rpg.PathData.data(p);if(!root.contains("JourneyCrops",Tag.TAG_COMPOUND))root.put("JourneyCrops",new CompoundTag());return root.getCompound("JourneyCrops");}
    public static void planted(UseOnContext c,BlockPos pos,BlockState before,InteractionResult result){
        if(pos==null||!(c.getPlayer() instanceof ServerPlayer p)||!AdvancementMilestones.eligible(p)||!result.consumesAction()||!p.serverLevel().hasChunkAt(pos))return;
        var after=p.serverLevel().getBlockState(pos);
        if(after.equals(before)||!(after.getBlock() instanceof CropBlock crop)||crop.getAge(after)!=0||!BuiltInRegistries.BLOCK.getKey(crop).getNamespace().equals("slavicmyths"))return;
        var records=crops(p);if(records.size()>=128&&!records.contains(cropKey(p,pos)))records.remove(records.getAllKeys().stream().sorted().findFirst().orElseThrow());
        records.putString(cropKey(p,pos),BuiltInRegistries.BLOCK.getKey(crop).toString());
    }
    public static void harvested(ServerPlayer p,BlockPos pos,BlockState before,boolean removed){
        if(!removed||!(before.getBlock() instanceof CropBlock))return;
        var records=crops(p);String key=cropKey(p,pos),planted=records.getString(key);records.remove(key);
        if(before.getBlock() instanceof CropBlock crop&&crop.isMaxAge(before)&&planted.equals(BuiltInRegistries.BLOCK.getKey(crop).toString()))award(p,"own_harvest");
    }
    /** Called by the book's existing 1 Hz moved-position loaded-start sampler. */
    public static void structure(ServerPlayer p,StructureStart start){
        if(!AdvancementMilestones.eligible(p)||!(start.getStructure() instanceof ChudStructure||start.getStructure() instanceof AbandonedStructure))return;
        for(var piece:start.getPieces()){
            if(piece instanceof ChudPiece city){halls(p,city.plan);if(city.abandoned!=null)maze(p,city.abandoned);}
            else if(piece instanceof AbandonedPiece abandoned)maze(p,abandoned.plan);
        }
    }
    public static BlockPos local(BlockPos world,BlockPos anchor,int rotation){
        int x=world.getX()-anchor.getX(),y=world.getY()-anchor.getY(),z=world.getZ()-anchor.getZ();
        var at=JourneyZones.local(x,y,z,rotation);return new BlockPos(at[0],at[1],at[2]);
    }
    private static void halls(ServerPlayer p,ChudPlan plan){
        if(Knowledge.knows(p,"chud_halls"))return;
        BlockPos at=local(p.blockPosition(),plan.anchor,plan.rotation);int mask=0,required=0;
        for(int i=0;i<plan.halls.size();i++){var h=plan.halls.get(i);if(!h.major())continue;required|=1<<i;
            int dx=at.getX()-h.x(),dz=at.getZ()-h.z();
            if(ChudGeometry.cavernQ(plan,h,dx,dz)<.8&&at.getY()>h.y()&&at.getY()<=ChudGeometry.cavernTop(plan,h,dx,dz)-2)mask|=1<<i;
        }
        if(mask==0)return;
        var root=org.slavicmyths.rpg.PathData.data(p);if(!root.contains("JourneyChudHalls",Tag.TAG_COMPOUND))root.put("JourneyChudHalls",new CompoundTag());var visited=root.getCompound("JourneyChudHalls");
        String key=p.level().dimension().location()+"/"+plan.cityId()+"/"+plan.seed;
        if(visited.size()>=8&&!visited.contains(key))visited.remove(visited.getAllKeys().stream().sorted().findFirst().orElseThrow()); // bounded exploration ledger
        int done=visited.getInt(key)|mask;visited.putInt(key,done);
        if((done&required)==required)award(p,"chud_halls");
    }
    private static void maze(ServerPlayer p,AbandonedPlan plan){
        if(Knowledge.knows(p,"chud_labyrinth"))return;
        var at=local(p.blockPosition(),plan.anchor,plan.rotation);
        for(var edge:plan.mazeEdges){var a=plan.mazeNodes.get(edge.from());var b=plan.mazeNodes.get(edge.to());int radius=edge.extra()?1:2;
            if(JourneyZones.passage(at.getX(),at.getY(),at.getZ(),a.getX(),a.getY(),a.getZ(),b.getX(),b.getY(),b.getZ(),radius)){award(p,"chud_labyrinth");return;}
        }
    }
    private AdvancementJourney(){}
}
