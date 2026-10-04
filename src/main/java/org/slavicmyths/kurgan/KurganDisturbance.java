package org.slavicmyths.kurgan;

import java.util.UUID;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="slavicmyths")
public final class KurganDisturbance {
    public static void burial(PlayerEntity player,UUID id,BlockPos pos){if(!(player instanceof ServerPlayerEntity)||player.isSpectator())return;BurialRecords d=BurialRecords.get((ServerWorld)player.level);KurganInstance i=d.instances.get(id);if(i!=null)trigger((ServerPlayerEntity)player,d,i,pos,"loot:");}
    private static void trigger(ServerPlayerEntity player,BurialRecords data,KurganInstance i,BlockPos pos,String source){KurganPlan.Room room=i.room(pos);if(room==null||source.equals("zone:")&&room.loot.isEmpty()&&!room.hall())return;if(!i.record(source+room.id,room.disturbance))return;
        for(int band=1;band<=3;band++)if(i.disturbance>=band*25&&(i.thresholds&(1<<band))==0){i.thresholds|=1<<band;player.level.playSound(null,pos,SoundEvents.AMBIENT_CAVE,SoundCategory.AMBIENT,.35F,.7F);}
        data.setDirty();if(i.disturbance>=75&&i.plan.tier>0&&!i.bossDefeated)KurganCurse.apply(player,i.id,i.plan.tier==2);
    }
    private static void prop(PlayerEntity p,BlockPos pos){if(!(p instanceof ServerPlayerEntity)||p.isSpectator())return;net.minecraft.block.Block b=p.level.getBlockState(pos).getBlock();if(b!=KurganBlocks.get("burial_stone_slab")&&b!=KurganBlocks.get("kurgan_stone_altar")&&b!=KurganBlocks.get("kurgan_ritual_bowl"))return;BurialRecords data=BurialRecords.get((ServerWorld)p.level);KurganInstance i=data.at(pos);if(i!=null)trigger((ServerPlayerEntity)p,data,i,pos,"zone:");}
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickBlock e){prop(e.getPlayer(),e.getPos());}
    @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent e){prop(e.getPlayer(),e.getPos());net.minecraft.block.BlockState s=e.getState();if(s.getBlock() instanceof BurialCoffinBlock){BlockPos foot=BurialCoffinBlock.foot(s,e.getPos());net.minecraft.tileentity.TileEntity t=e.getWorld().getBlockEntity(foot);if(t instanceof BurialCoffinTile){BurialCoffinTile b=(BurialCoffinTile)t;if(b.barrow!=null)burial(e.getPlayer(),b.barrow,foot);}}}
}
