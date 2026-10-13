package org.slavicmyths.kurgan;
import net.minecraft.world.level.ChunkPos;
import org.slavicmyths.worldgen.StructureCoverageService;
import org.slavicmyths.worldgen.StructureCoverageService.Rejection;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.network.chat.Component;

import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;

public final class KurganDebug {
    private static final Set<Block> NATURAL=new HashSet<>(Arrays.asList(Blocks.STONE,Blocks.GRANITE,Blocks.DIORITE,Blocks.ANDESITE,Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.GRASS_BLOCK,Blocks.PODZOL,Blocks.GRAVEL,Blocks.SAND,Blocks.SANDSTONE,Blocks.CLAY,Blocks.SNOW,Blocks.SNOW_BLOCK,Blocks.COAL_ORE,Blocks.IRON_ORE,Blocks.GOLD_ORE,Blocks.REDSTONE_ORE,Blocks.LAPIS_ORE,Blocks.DIAMOND_ORE,Blocks.EMERALD_ORE));
    public static int generate(CommandSourceStack source,int tier,long seed){return org.slavicmyths.worldgen.ManualStructureJobs.start(source,"kurgan",tier,seed);}
    public static int info(CommandSourceStack source){BlockPos pos=BlockPos.containing(source.getPosition());BurialRecords data=BurialRecords.get(source.getLevel());KurganInstance i=data.at(pos);if(i==null){double best=96*96;for(KurganInstance candidate:data.instances.values()){double d=candidate.origin.distSqr(pos);if(d<best){best=d;i=candidate;}}}if(i==null){source.sendFailure(Component.translatable("kurgan.command.not_found"));return 0;}
        KurganInstance selected=i;source.sendSuccess(()->Component.translatable("kurgan.command.info",selected.id.toString(),Component.translatable("kurgan.kind."+selected.plan.tier),selected.origin.toShortString(),selected.plan.floors,selected.plan.seed,selected.disturbance),false);
        if(selected.plan.formatVersion==2)source.sendSuccess(()->Component.literal("layout=v2 "+KurganLayout.metrics(selected.plan)),false);
        KurganPlan.Room room=i.room(pos);KurganEncounterState encounter=room==null?null:i.encounters.get(room.id);source.sendSuccess(()->Component.translatable("kurgan.command.encounter_info",room==null?-1:room.id,encounter!=null&&encounter.triggered,encounter!=null&&encounter.cleared,encounter==null?0:encounter.alive.size(),selected.sealOpened,selected.bossDefeated),false);return 1;
    }
    public static int clear(CommandSourceStack source,ServerPlayer player){KurganCurse.clear(player);source.sendSuccess(()->Component.translatable("kurgan.command.cleared",player.getName()),true);return 1;}
}
