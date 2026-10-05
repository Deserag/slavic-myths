package org.slavicmyths.kurgan;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class KurganDisturbance {
    public static void burial(Player player,UUID id,BlockPos pos){if(!(player instanceof ServerPlayer)||player.isSpectator())return;BurialRecords d=BurialRecords.get((ServerLevel)player.level());KurganInstance i=d.instances.get(id);if(i!=null)trigger((ServerPlayer)player,d,i,pos,"loot:");}
    private static void trigger(ServerPlayer player,BurialRecords data,KurganInstance i,BlockPos pos,String source){KurganPlan.Room room=i.room(pos);if(room==null||source.equals("zone:")&&room.loot.isEmpty()&&!room.hall())return;if(!i.record(source+room.id,room.disturbance))return;
        for(int band=1;band<=3;band++)if(i.disturbance>=band*25&&(i.thresholds&(1<<band))==0){i.thresholds|=1<<band;player.level().playSound(null,pos,SoundEvents.AMBIENT_CAVE.value(),SoundSource.AMBIENT,.35F,.7F);}
        data.setDirty();if(i.disturbance>=75&&i.plan.tier>0&&!i.bossDefeated)KurganCurse.apply(player,i.id,i.plan.tier==2);
    }
    private static void prop(Player p,BlockPos pos){if(!(p instanceof ServerPlayer)||p.isSpectator())return;net.minecraft.world.level.block.Block b=p.level().getBlockState(pos).getBlock();if(b!=KurganBlocks.get("burial_stone_slab")&&b!=KurganBlocks.get("kurgan_stone_altar")&&b!=KurganBlocks.get("kurgan_ritual_bowl"))return;BurialRecords data=BurialRecords.get((ServerLevel)p.level());KurganInstance i=data.at(pos);if(i!=null)trigger((ServerPlayer)p,data,i,pos,"zone:");}
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickBlock e){prop(e.getEntity(),e.getPos());}
    @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent e){prop(e.getPlayer(),e.getPos());net.minecraft.world.level.block.state.BlockState s=e.getState();if(s.getBlock() instanceof BurialCoffinBlock){BlockPos foot=BurialCoffinBlock.foot(s,e.getPos());net.minecraft.world.level.block.entity.BlockEntity t=e.getLevel().getBlockEntity(foot);if(t instanceof BurialCoffinTile){BurialCoffinTile b=(BurialCoffinTile)t;if(b.barrow!=null)burial(e.getPlayer(),b.barrow,foot);}}}
}
