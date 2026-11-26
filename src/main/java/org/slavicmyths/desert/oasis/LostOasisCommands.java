package org.slavicmyths.desert.oasis;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slavicmyths.commands.CommandTree;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
public final class LostOasisCommands {
    public static void register(CommandTree tree){
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2))
          .then(Commands.literal("lost_oasis").then(Commands.literal("generate").executes(c->generate(c.getSource())))
          .then(Commands.literal("search").executes(c->search(c.getSource()))))));
    }
    private static int search(CommandSourceStack s)throws CommandSyntaxException{
        return s.getServer().getCommands().getDispatcher().execute("locate structure slavicmyths:lost_oasis",s);
    }
    private static int generate(CommandSourceStack s){
        var w=s.getLevel();BlockPos at=BlockPos.containing(s.getPosition());
        int surface=w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,at.getX(),at.getZ());
        int y=LostOasisBuild.anchor(surface,w.getMinBuildHeight(),w.getMaxBuildHeight());
        BlockPos o=new BlockPos(at.getX()-LostOasisPlan.CX,y,at.getZ());
        if(!LostOasisBuild.start(w,o,spawned->s.sendSuccess(()->Component.literal("Затерянный оазис построен. Ламия: "+(spawned?"в царской ложе":"ожидает загрузки территории и игрока")),true))){
            s.sendFailure(Component.literal("Генерация уже идёт, территория зарезервирована или выходит за границы мира."));return 0;
        }
        s.sendSuccess(()->Component.literal("Территория зарезервирована. Начата адаптация и строительство оазиса: "+o.toShortString()+" (105 x 117 блоков)."),true);return 1;
    }
    private LostOasisCommands(){}
}
