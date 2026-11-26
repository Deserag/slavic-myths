package org.slavicmyths.desert.temple;

import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slavicmyths.commands.CommandTree;

public final class SunTempleCommands {
    public static void register(CommandTree tree){
        org.slavicmyths.desert.oasis.LostOasisCommands.register(tree);
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2))
            .then(Commands.literal("sun_temple").then(Commands.literal("generate").executes(c->generate(c.getSource()))))));
    }
    private static int generate(CommandSourceStack s){
        var w=s.getLevel();BlockPos at=BlockPos.containing(s.getPosition());
        int surface=w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,at.getX(),at.getZ());
        int y=SunTempleBuild.anchor(surface,w.getMinBuildHeight(),w.getMaxBuildHeight());
        BlockPos origin=new BlockPos(at.getX()-SunTemplePlan.CX,y,at.getZ());
        if(!SunTempleBuild.start(w,origin,spawned->s.sendSuccess(()->Component.literal("Храм Солнца построен. Хала: "+(spawned?"появилась":"ожидает игрока (проверьте сложность)")),true))){
            s.sendFailure(Component.literal("Генерация уже идёт, храм пересекается с существующим храмом или выходит за границы мира."));return 0;
        }
        s.sendSuccess(()->Component.literal("Начато строительство Храма Солнца: "+origin.toShortString()+". Рельеф в пределах 81 x 91 блоков будет заменён."),true);
        return 1;
    }
    private SunTempleCommands(){}
}
