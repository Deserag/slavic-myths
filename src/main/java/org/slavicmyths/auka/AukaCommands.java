package org.slavicmyths.auka;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.commands.CommandTree;

/** No-OP accept/decline choice and OP-only test spawn, integrated with the canonical command tree. */
public final class AukaCommands {
    public static void register(CommandTree tree) {
        var decision=Commands.literal("auka_decision")
            .then(Commands.argument("boss",UuidArgument.uuid())
                .then(Commands.argument("token",UuidArgument.uuid())
                    .then(Commands.argument("choice",StringArgumentType.word())
                        .executes(ctx->{
                            ServerPlayer player=ctx.getSource().getPlayerOrException();
                            String choice=StringArgumentType.getString(ctx,"choice");
                            if(!choice.equals("accept")&&!choice.equals("decline"))return 0;
                            var boss=player.serverLevel().getEntitiesOfClass(AukaBoss.class,
                                player.getBoundingBox().inflate(14),
                                a->a.getUUID().equals(UuidArgument.getUuid(ctx,"boss"))).stream().findFirst().orElse(null);
                            if(boss==null)return 0;
                            return boss.decide(player,UuidArgument.getUuid(ctx,"token"),choice.equals("accept"))?1:0;
                        }))));
        var test=Commands.literal("auka").executes(ctx->spawn(ctx.getSource().getPlayerOrException()));
        test.then(Commands.literal("spawn").executes(ctx->spawn(ctx.getSource().getPlayerOrException())));
        tree.add(Commands.literal("slavicmyths").then(decision)
            .then(Commands.literal("dev").requires(s->s.hasPermission(2))
                .then(Commands.literal("test").then(test))));
    }
    private static int spawn(ServerPlayer player){
        if(player.level().dimension()!=Level.OVERWORLD)return 0;
        var boss=AukaContent.AUKA.get().create(player.serverLevel());
        if(boss==null)return 0;
        Vec3 dir=player.getLookAngle().multiply(1,0,1).normalize();
        Vec3 position=player.position().add(dir.scale(6));
        boss.moveTo(position.x,position.y,position.z,player.getYRot()+180,0);
        if(!player.serverLevel().noCollision(boss))return 0;
        return player.serverLevel().addFreshEntity(boss)?1:0;
    }
    private AukaCommands(){}
}
