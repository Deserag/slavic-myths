package org.slavicmyths.bogatyr;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slavicmyths.bogatyr.BogatyrRules.State;
public final class BogatyrCommands {
    public static void register(org.slavicmyths.commands.CommandTree tree){
        var debug=Commands.literal("debug_171").requires(s->s.hasPermission(2)&&BogatyrConfig.DEBUG.get())
            .then(Commands.literal("courtyard").executes(c->BogatyrArena.create(c.getSource().getPlayerOrException())?1:0))
            .then(Commands.literal("spawn_ilya").executes(c->BogatyrLedger.call(c.getSource().getPlayerOrException())?1:0))
            .then(Commands.literal("spawn_idolishche").executes(c->spawn(c.getSource().getPlayerOrException(),true)))
            .then(Commands.literal("test_hero_retreat").executes(c->{var h=BogatyrLedger.hero(c.getSource().getPlayerOrException());if(h==null)return 0;h.setHealth(42);return 1;}))
            .then(Commands.literal("boss_phase").then(Commands.argument("phase",IntegerArgumentType.integer(2,2)).executes(c->{var b=near(c.getSource().getPlayerOrException());if(b==null)return 0;b.setHealth(315);return 1;})))
            .then(Commands.literal("attack").then(Commands.argument("state",com.mojang.brigadier.arguments.StringArgumentType.word()).suggests((c,s)->net.minecraft.commands.SharedSuggestionProvider.suggest(java.util.Arrays.stream(Attack.values()).map(Attack::getSerializedName),s)).executes(c->{var b=near(c.getSource().getPlayerOrException());if(b==null||!b.debug)return 0;Attack a;try{a=Attack.valueOf(com.mojang.brigadier.arguments.StringArgumentType.getString(c,"state").toUpperCase(java.util.Locale.ROOT));}catch(IllegalArgumentException ex){return 0;}b.setTarget(c.getSource().getPlayerOrException());b.face(c.getSource().getPlayerOrException());b.begin(a.state,a.ticks);return 1;})))
            .then(Commands.literal("status").executes(c->{var b=near(c.getSource().getPlayerOrException());c.getSource().sendSuccess(()->Component.literal(b==null?"No Idolishche within 72":"HP="+b.getHealth()+" state="+b.state()+" phase="+(b.rage()?2:1)+" feeds="+b.feeds+" debug="+b.debug+" "+b.lastDamageReport),false);return 1;}))
            .then(Commands.literal("progression").then(Commands.literal("get").executes(c->{var p=c.getSource().getPlayerOrException();var data=BogatyrLedger.get(p.serverLevel()).victories.get(p.getUUID());c.getSource().sendSuccess(()->Component.literal(data==null?"No victory":data.toString()),false);return 1;})).then(Commands.literal("clear_test").executes(c->{var p=c.getSource().getPlayerOrException();var d=BogatyrLedger.get(p.serverLevel());d.victories.remove(p.getUUID());d.setDirty();return 1;})));
        tree.add(Commands.literal("slavicmyths").then(debug).then(Commands.literal("hero").then(Commands.literal("call").executes(c->BogatyrLedger.call(c.getSource().getPlayerOrException())?1:0))).then(Commands.literal("encounter").requires(s->s.hasPermission(2)).then(Commands.literal("courtyard").executes(c->BogatyrArena.create(c.getSource().getPlayerOrException())?1:0)).then(Commands.literal("idolishche").executes(c->spawn(c.getSource().getPlayerOrException(),false)))));
    }
    public enum Attack implements net.minecraft.util.StringRepresentable {FIST(State.FIST_WINDUP,22),STOMP(State.STOMP_TELEGRAPH,32),GRAB(State.GRAB_START,25),SUMMON(State.FEED_SUMMON,50),BREATH(State.BREATH_WINDUP,25),CHARGE(State.CHARGE_WINDUP,28);final State state;final int ticks;Attack(State s,int t){state=s;ticks=t;}public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static IdolishcheEntity near(ServerPlayer p){return p.level().getEntitiesOfClass(IdolishcheEntity.class,p.getBoundingBox().inflate(72),b->b.isAlive()).stream().findFirst().orElse(null);}
    public static int spawn(ServerPlayer p,boolean debug){if(near(p)!=null)return 0;var w=p.serverLevel();var b=BogatyrContent.IDOL.get().create(w);if(b==null)return 0;var at=BogatyrLedger.safe(w,p.position().add(p.getLookAngle().multiply(12,0,12)),4.2,13,6);if(at==null){p.sendSystemMessage(Component.translatable("gui.slavicmyths.171.boss_no_space"));return 0;}b.moveTo(at.x,at.y,at.z,p.getYRot()+180,0);if(!w.noCollision(b)||!w.addFreshEntity(b))return 0;b.start(p,debug);return 1;}
    private BogatyrCommands(){}
}
