package org.slavicmyths.dungeon;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slavicmyths.commands.CommandTree;

public final class DungeonCommands {
    public static void register(CommandTree tree){
        var desert=Commands.literal("desert")
            .executes(c->generate(c.getSource(),4,c.getSource().getLevel().random.nextLong()))
            .then(Commands.argument("floors",IntegerArgumentType.integer(3,5))
                .executes(c->generate(c.getSource(),IntegerArgumentType.getInteger(c,"floors"),c.getSource().getLevel().random.nextLong()))
                .then(Commands.argument("seed",LongArgumentType.longArg())
                    .executes(c->generate(c.getSource(),IntegerArgumentType.getInteger(c,"floors"),LongArgumentType.getLong(c,"seed")))));
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2))
            .then(Commands.literal("dungeon").then(Commands.literal("generate").then(desert)))));
    }
    private static int generate(CommandSourceStack source,int floors,long seed){
        ServerLevel world=source.getLevel();
        BlockPos at=BlockPos.containing(source.getPosition());
        try{
            DungeonPlan plan=DungeonPlan.create(seed,floors);
            int surface=world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,at.getX(),at.getZ());
            int y=plan.anchorY(surface,world.getMinBuildHeight(),world.getMaxBuildHeight());
            var entrance=plan.entrance();
            BlockPos origin=new BlockPos(at.getX()-entrance.x(),y,at.getZ()-entrance.z());
            BlockPos well=new BlockPos(at.getX(),Math.max(surface,y+plan.top()+4),at.getZ());
            int expected=plan.expectedBosses();
            if(!DungeonBuild.start(world,origin,plan,surface,spawned->source.sendSuccess(()->Component.literal("Подземелье построено. Боссы: "+spawned+"/"+expected+(spawned<expected?" (проверьте сложность и свободное место на аренах)":"")),true))){
                source.sendFailure(Component.literal("Dungeon generation already running or site conflicts with world bounds."));return 0;
            }
            source.sendSuccess(()->Component.literal("Dungeon geometry queued: "+floors+" floors, seed "+seed+"; "+(y<surface?"underground":"above ground")+"; well "+well.toShortString()+"; origin "+origin.toShortString()),true);
            return 1;
        }catch(IllegalArgumentException|IllegalStateException ex){source.sendFailure(Component.literal(ex.getMessage()));return 0;}
    }
    private DungeonCommands(){}
}
