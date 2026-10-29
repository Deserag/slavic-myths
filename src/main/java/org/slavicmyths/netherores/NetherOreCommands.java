package org.slavicmyths.netherores;
import net.minecraft.commands.Commands;import net.minecraft.network.chat.Component;import net.minecraft.world.level.Level;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;
/** OP diagnostics: one already loaded chunk, no worldgen or forced chunk loads. */
@EventBusSubscriber(modid="slavicmyths")
public final class NetherOreCommands {
 @SubscribeEvent public static void register(net.neoforged.neoforge.event.RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("slavicmyths_ores_count").requires(s->s.hasPermission(2)).executes(c->{var s=c.getSource();var w=s.getLevel();if(!w.dimension().equals(Level.NETHER)){s.sendFailure(Component.translatable("netherores.count.nether"));return 0;}var at=net.minecraft.core.BlockPos.containing(s.getPosition());var chunk=w.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4);if(chunk==null){s.sendFailure(Component.translatable("netherores.count.unloaded"));return 0;}
  int[] counts=new int[5];var ids=java.util.List.of("livingstone_ore","alatyrite_ore","runnik_ore","infernal_ore","sulfur_deposit");for(var section:chunk.getSections()){if(section.hasOnlyAir())continue;for(int x=0;x<16;x++)for(int y=0;y<16;y++)for(int z=0;z<16;z++){var block=section.getBlockState(x,y,z).getBlock();for(int i=0;i<5;i++)if(block==NetherOres.BLOCKS.get(ids.get(i)).get()){counts[i]++;break;}}}
  s.sendSuccess(()->Component.translatable("netherores.count.result",chunk.getPos().x,chunk.getPos().z,java.util.Arrays.toString(counts)),false);return java.util.Arrays.stream(counts).sum();}));}
 private NetherOreCommands(){}
}
