package org.slavicmyths.kurgan;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

public final class KurganDebug {
    private static final Set<Block> NATURAL=new HashSet<>(Arrays.asList(Blocks.STONE,Blocks.GRANITE,Blocks.DIORITE,Blocks.ANDESITE,Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.GRASS_BLOCK,Blocks.PODZOL,Blocks.GRAVEL,Blocks.SAND,Blocks.SANDSTONE,Blocks.CLAY,Blocks.SNOW,Blocks.SNOW_BLOCK,Blocks.COAL_ORE,Blocks.IRON_ORE,Blocks.GOLD_ORE,Blocks.REDSTONE_ORE,Blocks.LAPIS_ORE,Blocks.DIAMOND_ORE,Blocks.EMERALD_ORE));
    public static int generate(CommandSource source,int tier,long seed){ServerWorld world=source.getLevel();if(!world.dimension().equals(World.OVERWORLD)){source.sendFailure(new TranslationTextComponent("swamp.command.overworld"));return 0;}
        KurganPlan plan;try{plan=KurganPlan.create(tier,seed);}catch(IllegalArgumentException invalid){source.sendFailure(new TranslationTextComponent("kurgan.command.invalid_site"));return 0;}
        BlockPos from=new BlockPos(source.getPosition());
        for(int[] offset:new int[][]{{192,0},{-192,0},{0,192},{0,-192}}){int x=from.getX()+offset[0],z=from.getZ()+offset[1];int y=world.getHeight(Heightmap.Type.OCEAN_FLOOR,x,z)-1;BlockPos origin=new BlockPos(x,y,z);
            if(!KurganStructure.terrainValid(world.getChunkSource().getGenerator(),plan,origin)||!safe(world,origin,plan))continue;
            UUID id=UUID.nameUUIDFromBytes((world.dimension().location()+":"+origin.asLong()+":"+seed).getBytes(java.nio.charset.StandardCharsets.UTF_8));KurganDungeonPiece piece=new KurganDungeonPiece(plan,origin,id);MutableBoundingBox b=piece.getBoundingBox();
            // Preflight has completed for the entire footprint before the first block mutation.
            BurialRecords.get(world).register(new KurganInstance(id,origin,plan));
            for(int cx=b.x0>>4;cx<=b.x1>>4;cx++)for(int cz=b.z0>>4;cz<=b.z1>>4;cz++){
                world.getChunk(cx,cz);MutableBoundingBox clip=new MutableBoundingBox(cx*16,0,cz*16,cx*16+15,255,cz*16+15);
                piece.postProcess(world,world.structureFeatureManager(),world.getChunkSource().getGenerator(),new Random(seed),clip,new ChunkPos(cx,cz),origin);
            }
            BlockPos p=piece.arrival();source.sendSuccess(new TranslationTextComponent("kurgan.command.generated",id.toString(),p.getX(),p.getY(),p.getZ(),plan.floors,plan.rooms.size()),true);return 1;
        }
        source.sendFailure(new TranslationTextComponent("kurgan.command.invalid_site"));return 0;
    }
    private static boolean safe(ServerWorld world,BlockPos origin,KurganPlan plan){KurganPlan.Box b=plan.bounds();BlockPos low=origin.offset(b.x0,b.y0,b.z0),high=origin.offset(b.x1,b.y1,b.z1);if(!world.getWorldBorder().isWithinBounds(low)||!world.getWorldBorder().isWithinBounds(high))return false;
        for(KurganInstance i:BurialRecords.get(world).instances.values()){KurganPlan.Box q=i.plan.bounds();if(new KurganPlan.Box(low.getX(),low.getY(),low.getZ(),high.getX(),high.getY(),high.getZ()).intersects(new KurganPlan.Box(i.origin.getX()+q.x0,i.origin.getY()+q.y0,i.origin.getZ()+q.z0,i.origin.getX()+q.x1,i.origin.getY()+q.y1,i.origin.getZ()+q.z1)))return false;}
        for(int dx=-plan.radius;dx<=plan.radius;dx+=2)for(int dz=-plan.radius;dz<=plan.radius;dz+=2){int ground=world.getHeight(Heightmap.Type.OCEAN_FLOOR,origin.getX()+dx,origin.getZ()+dz)-1;if(Math.abs(ground-origin.getY())>4||!world.getFluidState(new BlockPos(origin.getX()+dx,ground+1,origin.getZ()+dz)).isEmpty())return false;}
        BlockPos.Mutable pos=new BlockPos.Mutable();
        for(int x=low.getX();x<=high.getX();x++)for(int z=low.getZ();z<=high.getZ();z++)for(int y=low.getY();y<=high.getY();y++){
            pos.set(x,y,z);BlockState s=world.getBlockState(pos);if(s.hasTileEntity())return false;if(s.isAir()||NATURAL.contains(s.getBlock())||s.is(BlockTags.LEAVES)||s.is(BlockTags.LOGS)||s.getMaterial().isReplaceable())continue;return false;
        }return true;
    }
    public static int info(CommandSource source){BlockPos pos=new BlockPos(source.getPosition());BurialRecords data=BurialRecords.get(source.getLevel());KurganInstance i=data.at(pos);if(i==null){double best=96*96;for(KurganInstance candidate:data.instances.values()){double d=candidate.origin.distSqr(pos);if(d<best){best=d;i=candidate;}}}if(i==null){source.sendFailure(new TranslationTextComponent("kurgan.command.not_found"));return 0;}
        source.sendSuccess(new TranslationTextComponent("kurgan.command.info",i.id.toString(),new TranslationTextComponent("kurgan.kind."+i.plan.tier),i.origin.toShortString(),i.plan.floors,i.plan.seed,i.disturbance),false);
        KurganPlan.Room room=i.room(pos);KurganEncounterState encounter=room==null?null:i.encounters.get(room.id);source.sendSuccess(new TranslationTextComponent("kurgan.command.encounter_info",room==null?-1:room.id,encounter!=null&&encounter.triggered,encounter!=null&&encounter.cleared,encounter==null?0:encounter.alive.size(),i.sealOpened,i.bossDefeated),false);return 1;
    }
    public static int clear(CommandSource source,ServerPlayerEntity player){KurganCurse.clear(player);source.sendSuccess(new TranslationTextComponent("kurgan.command.cleared",player.getName()),true);return 1;}
}
