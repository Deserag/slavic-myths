package org.slavicmyths.mor;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
@EventBusSubscriber(modid="slavicmyths")
public final class MorBlockCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("sm").requires(s->s.hasPermission(2)).then(Commands.literal("test").then(Commands.literal("mor_block").then(Commands.argument("id",StringArgumentType.word()).suggests((c,b)->{for(String id:MorBlocks.IDS)b.suggest(id);b.suggest("reset");return b.buildFuture();}).executes(c->{
            var player=c.getSource().getPlayerOrException();var hit=player.pick(8,0,false);
            if(!(hit instanceof BlockHitResult target)||hit.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK)return 0;
            String id=StringArgumentType.getString(c,"id");var level=player.serverLevel();var pos=target.getBlockPos();
            if(id.equals("reset")) {if(MorBlocks.index(level.getBlockState(pos))<0)return 0;level.setBlock(pos,MorBlocks.prototype(level.getBlockState(pos)),3);return 1;}
            if(!MorBlocks.BLOCKS.containsKey(id)||!level.getBlockState(pos).is(MorBlocks.BASE[java.util.Arrays.asList(MorBlocks.IDS).indexOf(id)]))return 0;
            var result=MorBlocks.convert(level,pos,player);c.getSource().sendSuccess(()->Component.literal(result.name()),false);return result==MorBlocks.Result.CONVERTED?1:0;
        })))));
    }
}
