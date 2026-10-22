package org.slavicmyths.commands;

import java.util.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

/** Reads the freshly composed tree; no hidden or inaccessible commands in help/completion. */
final class CommandHelp {
    static void attach(CommandNode<CommandSourceStack> root,List<String> canonical) {
        root.addChild(Commands.literal("help").executes(c->show(c.getSource(),root,canonical,""))
            .then(Commands.argument("category",StringArgumentType.greedyString())
                .suggests((c,b)->SharedSuggestionProvider.suggest(categories(c.getSource(),root,canonical),b))
                .executes(c->show(c.getSource(),root,canonical,StringArgumentType.getString(c,"category")))).build());
    }

    private static List<String> categories(CommandSourceStack source,CommandNode<CommandSourceStack> root,List<String> canonical) {
        return canonical.stream().filter(p->p.startsWith("slavicmyths ")&&!p.contains("<"))
            .map(p->p.substring(12)).filter(p->{var n=resolve(root,p,source);return n!=null&&visible(n,"slavicmyths "+p,source,canonical);}).toList();
    }

    private static CommandNode<CommandSourceStack> resolve(CommandNode<CommandSourceStack> root,String category,CommandSourceStack source) {
        var node=root;
        if(!category.isBlank())for(String token:category.trim().split(" +")) {
            node=node.getChild(token);if(node==null||node instanceof ArgumentCommandNode||!node.canUse(source))return null;
        }
        return node;
    }

    private static int show(CommandSourceStack source,CommandNode<CommandSourceStack> root,List<String> canonical,String category) {
        var node=resolve(root,category,source);
        String prefix="slavicmyths"+(category.isBlank()?"":" "+category.trim());
        if(node==null||!canonical.contains(prefix)&&node!=root||node!=null&&!visible(node,prefix,source,canonical)) {
            source.sendFailure(Component.translatable("command.slavicmyths.help.unknown"));return 0;
        }
        source.sendSuccess(()->Component.translatable("command.slavicmyths.help.title","/"+prefix),false);
        if(node.getCommand()!=null&&(!CommandTree.playerOnly(prefix)||source.getEntity() instanceof net.minecraft.server.level.ServerPlayer))source.sendSuccess(()->Component.translatable("command.slavicmyths.help.usage","/"+prefix),false);
        int count=0;
        for(var child:node.getChildren()) {
            String path=prefix+" "+child.getName();
            if(child.getName().equals("help")||!canonical.contains(path)||!visible(child,path,source,canonical))continue;
            if(child instanceof ArgumentCommandNode&&!child.getChildren().isEmpty()) {
                count+=argumentUsages(source,child,path,"/"+prefix+" "+child.getUsageText(),canonical);
                continue;
            }
            String usage="/"+prefix+" "+child.getUsageText()+(child.getChildren().isEmpty()?"":" …");
            var summary=summary(child.getName());
            source.sendSuccess(()->summary.isEmpty()?Component.translatable("command.slavicmyths.help.usage",usage):Component.translatable("command.slavicmyths.help.entry",usage,Component.translatable("command.slavicmyths.help.summary."+summary)),false);count++;
        }
        source.sendSuccess(()->Component.translatable("command.slavicmyths.help.more",category.isBlank()?"navigation":category.trim()),false);
        return Math.max(1,count);
    }
    private static int argumentUsages(CommandSourceStack source,CommandNode<CommandSourceStack> node,String path,String syntax,List<String> canonical) {
        if(!visible(node,path,source,canonical))return 0;
        int count=0;
        if(node.getCommand()!=null&&(!CommandTree.playerOnly(path)||source.getEntity() instanceof net.minecraft.server.level.ServerPlayer)) {
            source.sendSuccess(()->Component.translatable("command.slavicmyths.help.usage",syntax),false);count++;
        }
        for(var child:node.getChildren())if(canonical.contains(path+" "+child.getName()))
            count+=argumentUsages(source,child,path+" "+child.getName(),syntax+" "+child.getUsageText(),canonical);
        return count;
    }
    private static boolean visible(CommandNode<CommandSourceStack> node,String path,CommandSourceStack source,List<String> canonical) {
        if(!node.canUse(source)||!canonical.contains(path))return false;
        if(node.getCommand()!=null&&(!CommandTree.playerOnly(path)||source.getEntity() instanceof net.minecraft.server.level.ServerPlayer))return true;
        for(var child:node.getChildren())if(visible(child,path+" "+child.getName(),source,canonical))return true;
        return false;
    }
    private static String summary(String name) {
        return switch(name) {
            case "dev","hero","navigation","kalin","kartaus","wolf","city","abandoned","class","test","debug","generate","generation","search","locate","place","status","info","validate","tp","clear_owned","quest185","boss","hunt","yaga","growtree" -> name;
            default -> "";
        };
    }
    private CommandHelp(){}
}
