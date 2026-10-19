package org.slavicmyths.earth;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
@EventBusSubscriber(modid="slavicmyths")
public final class EarthCommands {
    public static void register(org.slavicmyths.commands.CommandTree tree){tree.add(Commands.literal("sm").then(Commands.literal("earth_spirit").requires(s->s.hasPermission(2)).then(Commands.literal("status").executes(c->{var w=c.getSource().getLevel();var boss=w.getEntitiesOfClass(EarthSpirit.class,new net.minecraft.world.phys.AABB(c.getSource().getPosition(),c.getSource().getPosition()).inflate(40)).stream().findFirst().orElse(null);c.getSource().sendSuccess(()->Component.literal(boss==null?"No Earth Spirit within 40 blocks":"HP="+boss.getHealth()+" phase="+boss.phase()+" action="+boss.action()+" arena="+boss.arenaId()+" lastHit=["+boss.lastDamageReport+"]"),false);return 1;})).then(Commands.literal("spawn_debug").executes(c->{var w=c.getSource().getLevel();var b=EarthContent.BOSS.get().create(w);if(b==null)return 0;var p=c.getSource().getPosition();b.moveTo(p.x,p.y,p.z,0,0);if(!w.noCollision(b)){c.getSource().sendFailure(Component.literal("Earth Spirit needs 3.1 x 6.35 blocks of free space."));return 0;}w.addFreshEntity(b);c.getSource().sendSuccess(()->Component.literal("Earth Spirit spawned. Victory awards normal loot and the rune."),false);return 1;}))));}
    private EarthCommands(){}
}
