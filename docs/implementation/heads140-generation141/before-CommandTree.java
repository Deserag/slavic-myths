package org.slavicmyths.commands;

import java.util.*;
import java.util.function.Predicate;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.*;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Local, composable contributions. Builders are collected, never independently registered.
 * Every executable node retains its original ancestor requirements, including config gates.
 * Canonical and compatibility paths share the same guarded command, never a second action.
 */
public final class CommandTree {
    private final List<LiteralCommandNode<CommandSourceStack>> contributions=new ArrayList<>();
    private final Map<String,CommandNode<CommandSourceStack>> nodes=new LinkedHashMap<>();
    private final Map<String,Spec> specs=new LinkedHashMap<>();
    private final Map<String,String> aliases=new LinkedHashMap<>();
    private boolean installed;
    private record Original(String path,CommandNode<CommandSourceStack> node,Predicate<CommandSourceStack> allowed) {}
    private static final class Spec {
        final CommandNode<CommandSourceStack> prototype;
        final String originalPath;
        Predicate<CommandSourceStack> allowed;
        Spec(CommandNode<CommandSourceStack> n,Predicate<CommandSourceStack> p,String path){prototype=n;allowed=p;originalPath=path;}
    }

    public void add(LiteralArgumentBuilder<CommandSourceStack> builder) {
        if(installed)throw new IllegalStateException("Command contribution after installation");
        contributions.add(builder.build());
    }

    /** Deterministic migration; only confirmed equivalent handlers are folded together. */
    public static String canonical(String old) {
        String p=old.trim();
        if(p.equals("smclass")||p.startsWith("smclass "))p="slavicmyths dev class"+p.substring(7);
        else if(p.startsWith("locate structure slavicmyths:chud_"))
            p=p.endsWith("chud_city")?"slavicmyths dev city search":"slavicmyths dev abandoned search";
        else {
            if(p.equals("sm")||p.startsWith("sm "))p="slavicmyths"+p.substring(2);
            String[] parts=p.split(" ",3);
            if(parts.length>1&&!Set.of("dev","hero","navigation","kalin","kartaus","wolf_decision").contains(parts[1]))
                p="slavicmyths dev "+p.substring(12);
        }
        p=p.replace("slavicmyths wolf_decision","slavicmyths wolf decision")
            .replace("slavicmyths dev debug_171","slavicmyths dev bogatyr")
            .replace("slavicmyths dev navigation clear-owned","slavicmyths dev navigation clear_owned")
            .replace("slavicmyths dev navigation searcharea create-test","slavicmyths dev navigation searcharea create_test")
            .replace("slavicmyths dev locate kurgan","slavicmyths dev kurgan locate")
            .replace("slavicmyths dev generate kurgan","slavicmyths dev kurgan generate")
            .replace("slavicmyths dev generate bandit_camp medium","slavicmyths dev generate bandit_camp fortified")
            .replace("slavicmyths dev city debug_plan","slavicmyths dev city info")
            .replace("slavicmyths dev abandoned inspect","slavicmyths dev abandoned info")
            .replace("slavicmyths dev abandoned debug","slavicmyths dev abandoned validate");
        if(p.startsWith("slavicmyths dev growtree ")&&p.endsWith(" random"))p=p.substring(0,p.length()-7);
        return p;
    }

    private static boolean explicitFold(String path) {
        return path.startsWith("locate structure slavicmyths:chud_")
            ||path.startsWith("slavicmyths locate kurgan ")||path.startsWith("slavicmyths generate kurgan ")
            ||path.startsWith("slavicmyths generate bandit_camp medium")
            ||path.startsWith("slavicmyths city debug_plan")||path.startsWith("slavicmyths abandoned inspect")
            ||path.startsWith("slavicmyths abandoned debug")||path.startsWith("slavicmyths dev growtree ")&&path.endsWith(" random");
    }

    /** Context gates only reject sources that the original handlers could not serve. */
    public static boolean playerOnly(String p) {
        return p.startsWith("slavicmyths hero")||p.startsWith("slavicmyths wolf ")
            ||p.startsWith("slavicmyths kalin")||p.startsWith("slavicmyths kartaus")
            ||p.equals("slavicmyths navigation")
            ||p.startsWith("slavicmyths dev navigation ")&&!p.endsWith(" stats")
            ||p.startsWith("slavicmyths dev boss ")||p.startsWith("slavicmyths dev hunt ")
            ||p.startsWith("slavicmyths dev tp_")||p.startsWith("slavicmyths dev spawn_nightingale")
            ||p.startsWith("slavicmyths dev growtree ")||p.startsWith("slavicmyths dev rusalka_preview ")
            ||p.startsWith("slavicmyths dev bogatyr ")||p.startsWith("slavicmyths dev encounter ")
            ||p.startsWith("slavicmyths dev witch_yard ")||p.startsWith("slavicmyths dev skim_valley ")
            ||p.startsWith("slavicmyths dev sivka ")||p.startsWith("slavicmyths dev test grey_wolf")
            ||p.startsWith("slavicmyths dev test mor_block ")||p.startsWith("slavicmyths dev debug military_quests")
            ||p.startsWith("slavicmyths dev city tp ")||p.startsWith("slavicmyths dev abandoned tp ")
            ||p.startsWith("slavicmyths dev depths ")&&!p.endsWith(" status")
            ||p.equals("slavicmyths dev yaga generate")||p.equals("slavicmyths dev yaga status")
            ||p.equals("slavicmyths dev swamp")
            ||p.equals("slavicmyths dev clear_kurgan_curse");
    }

    private static void flatten(CommandNode<CommandSourceStack> n,String path,Predicate<CommandSourceStack> ancestors,List<Original> out) {
        Predicate<CommandSourceStack> allowed=ancestors.and(n.getRequirement());
        out.add(new Original(path,n,allowed));
        for(var child:n.getChildren())flatten(child,path+" "+child.getName(),allowed,out);
    }

    public void install(CommandDispatcher<CommandSourceStack> dispatcher) {
        if(installed)throw new IllegalStateException("Command tree installed twice");
        installed=true;
        var originals=new ArrayList<Original>();
        for(var root:contributions)flatten(root,root.getName(),s->true,originals);
        for(var old:originals) {
            if(Set.of("slavicmyths","sm","locate","locate structure").contains(old.path()))continue;
            String path=canonical(old.path());
            Predicate<CommandSourceStack> allowed=old.allowed();
            if(path.startsWith("slavicmyths dev"))allowed=allowed.and(s->s.hasPermission(2));
            if(old.node().getCommand()!=null&&old.node().getChildren().isEmpty()&&playerOnly(path))allowed=allowed.and(s->s.getEntity() instanceof ServerPlayer);
            var prior=specs.get(path);
            if(prior==null)specs.put(path,new Spec(old.node(),allowed,old.path()));
            else {
                // Same structural parents compose; executable collisions require an explicit migration.
                if(prior.prototype.getCommand()!=null&&old.node().getCommand()!=null&&
                    (prior.originalPath.equals(old.path())||!explicitFold(prior.originalPath)&&!explicitFold(old.path())))
                    throw new IllegalStateException("Duplicate canonical action: "+path);
                prior.allowed=prior.allowed.or(allowed);
            }
        }
        nodes.put("slavicmyths",Commands.literal("slavicmyths").build());
        nodes.put("slavicmyths dev",Commands.literal("dev").requires(s->s.hasPermission(2)).build());
        nodes.put("locate",Commands.literal("locate").requires(s->s.hasPermission(2)).build());
        for(var entry:specs.entrySet()) {
            String path=entry.getKey();Spec spec=entry.getValue();
            if(path.equals("slavicmyths dev"))continue;
            var builder=spec.prototype instanceof LiteralCommandNode<CommandSourceStack>
                ? Commands.literal(path.substring(path.lastIndexOf(' ')+1)).requires(spec.allowed)
                : spec.prototype.createBuilder().requires(spec.allowed);
            Command<CommandSourceStack> original=spec.prototype.getCommand();
            if(original!=null)builder.executes(context->{
                if(!spec.allowed.test(context.getSource()))throw new SimpleCommandExceptionType(Component.translatable("command.slavicmyths.denied")).create();
                if(playerOnly(path)&&!(context.getSource().getEntity() instanceof ServerPlayer))
                    throw new SimpleCommandExceptionType(Component.translatable("command.slavicmyths.player_required")).create();
                return original.run(context);
            });
            nodes.put(path,builder.build());
        }
        // Nodes made by inserting categories (dev/class/wolf) do not need dummy executors.
        for(String path:new ArrayList<>(nodes.keySet()))attach(path);
        for(var old:originals) {
            String path=old.path();String target=canonical(path);
            if(!path.equals(target)&&nodes.containsKey(target)&&!Set.of("sm","locate","locate structure").contains(path)) {
                if(path.startsWith("sm "))path="slavicmyths"+path.substring(2);
                addCompatibility(path,target,old.node());
                // Preserve old tokens under the administrative prefix too.
                String prefixed=old.path().startsWith("slavicmyths ")?"slavicmyths dev "+old.path().substring(12):"";
                if(!old.path().startsWith("slavicmyths dev ")&&!prefixed.isEmpty()) {
                    // Only internal spelling/duplicate migrations need this second alias.
                    if(canonical(prefixed).equals(target)&&!prefixed.equals(target))addCompatibility(prefixed,target,old.node());
                }
            }
        }
        var city=nodes.get("slavicmyths dev city");
        if(city!=null) {
            addRedirect("slavicmyths debug chud_city",city);
            addRedirect("slavicmyths dev debug chud_city",city);
        }
        CommandHelp.attach(nodes.get("slavicmyths"),nodes.keySet().stream().filter(p->!aliases.containsKey(p)).toList());
        // Exactly one real registration of our canonical root. The remaining roots are compatibility only.
        dispatcher.getRoot().addChild(nodes.get("slavicmyths"));
        dispatcher.register(Commands.literal("sm").redirect(nodes.get("slavicmyths")));
        if(nodes.containsKey("smclass"))dispatcher.getRoot().addChild(nodes.get("smclass"));
        if(nodes.containsKey("locate"))dispatcher.getRoot().addChild(nodes.get("locate"));
    }

    private void attach(String path) {
        int space=path.lastIndexOf(' ');if(space<0)return;
        String parent=path.substring(0,space);
        if(!nodes.containsKey(parent)) {
            nodes.put(parent,Commands.literal(parent.substring(parent.lastIndexOf(' ')+1)).build());attach(parent);
        }
        var child=nodes.get(path);var current=nodes.get(parent).getChild(child.getName());
        if(current!=null&&current!=child)throw new IllegalStateException("Conflicting command node: "+path);
        nodes.get(parent).addChild(child);
    }

    private void addCompatibility(String path,String target,CommandNode<CommandSourceStack> prototype) {
        if(path.equals(target)||nodes.containsKey(path))return;
        var destination=nodes.get(target);
        var builder=prototype.createBuilder().requires(destination.getRequirement());
        if(destination.getCommand()!=null)builder.executes(destination.getCommand());
        nodes.put(path,builder.build());aliases.put(path,target);attach(path);
    }

    private void addRedirect(String path,CommandNode<CommandSourceStack> target) {
        if(nodes.containsKey(path))throw new IllegalStateException("Alias collision: "+path);
        nodes.put(path,Commands.literal(path.substring(path.lastIndexOf(' ')+1)).requires(s->s.hasPermission(2)&&target.canUse(s)).redirect(target).build());
        aliases.put(path,"slavicmyths dev city");attach(path);
    }

    public String handlerClass(String path) {
        Spec spec=specs.get(aliases.getOrDefault(path,path));
        return spec==null||spec.prototype.getCommand()==null?"":spec.prototype.getCommand().getClass().getNestHost().getName();
    }
    public Map<String,String> aliases(){return Collections.unmodifiableMap(aliases);}
    public Map<String,CommandNode<CommandSourceStack>> nodes(){return Collections.unmodifiableMap(nodes);}
}
