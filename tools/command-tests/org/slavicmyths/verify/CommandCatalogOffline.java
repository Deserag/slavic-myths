package org.slavicmyths.verify;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.file.*;
import java.util.function.Predicate;
import com.mojang.brigadier.*;
import com.mojang.brigadier.tree.*;
import com.mojang.brigadier.arguments.*;
import net.minecraft.commands.*;
import net.minecraft.server.level.ServerPlayer;
import org.slavicmyths.commands.*;

/** Real production builders/Brigadier on a plain JVM. No server, client, world,
 * entity constructor or gameplay callback. Explicit spies test parsing only. */
public final class CommandCatalogOffline {
 private static final class EmptySubscriber {}
 private static int checks;
 private static CommandTree tree;
 private static ServerPlayer identity;
 private static final com.google.gson.Gson JSON=new com.google.gson.GsonBuilder().setPrettyPrinting().create();
 private static final Path OUT=Path.of("docs/implementation/commands-74");
 private record Original(String path,CommandNode<CommandSourceStack> node,Predicate<CommandSourceStack> allowed){}
 private static CommandSourceStack source(int op,boolean player) {
  return new CommandSourceStack(CommandSource.NULL,net.minecraft.world.phys.Vec3.ZERO,net.minecraft.world.phys.Vec2.ZERO,null,op,"Offline",net.minecraft.network.chat.Component.literal("Offline"),null,player?identity:null);
 }
 private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}

 public static void main(String[] args)throws Exception {
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
  net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  // Unconstructed type token for instanceof only: no player/world methods are called.
  var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);
  identity=(ServerPlayer)((sun.misc.Unsafe)f.get(null)).allocateInstance(ServerPlayer.class);
  org.slavicmyths.village.VillageRoles.init(net.neoforged.bus.api.BusBuilder.builder().build());
  var spec=org.slavicmyths.bogatyr.BogatyrConfig.SPEC;
  var config=com.electronwill.nightconfig.core.CommentedConfig.inMemory();spec.correct(config);
  var loaded=Class.forName("net.neoforged.fml.config.LoadedConfig").getDeclaredConstructors()[0];loaded.setAccessible(true);
  spec.acceptConfig((net.neoforged.fml.config.IConfigSpec.ILoadedConfig)loaded.newInstance(config,null,null));
  var d=new CommandDispatcher<CommandSourceStack>();tree=SlavicCommands.contributions();
  var field=CommandTree.class.getDeclaredField("contributions");field.setAccessible(true);
  @SuppressWarnings("unchecked") var roots=(List<LiteralCommandNode<CommandSourceStack>>)field.get(tree);
  var originals=new ArrayList<Original>();for(var root:roots)originals(root,root.getName(),s->true,originals);
  Files.createDirectories(OUT);
  var originalsJson=new ArrayList<Map<String,Object>>();
  var argumentPaths=new HashSet<String>();for(var old:originals)if(old.node() instanceof ArgumentCommandNode)argumentPaths.add(old.path());
  for(var old:originals) {
   String full="",syntax="";for(String part:old.path().split(" ")){full=full.isEmpty()?part:full+" "+part;syntax+=(syntax.isEmpty()?"":" ")+(argumentPaths.contains(full)?"<"+part+">":part);}
   var row=new LinkedHashMap<String,Object>();row.put("path",syntax);row.put("canonical",CommandTree.canonical(old.path()));row.put("executable",old.node().getCommand()!=null);
   for(boolean player:new boolean[]{false,true}){var levels=new ArrayList<Integer>();for(int op=0;op<=4;op++)if(old.allowed().test(source(op,player)))levels.add(op);row.put(player?"player_permissions":"console_permissions",levels);}
   if(old.node().getCommand()!=null)row.put("handler_class",old.node().getCommand().getClass().getNestHost().getName());
   originalsJson.add(row);
  }
  Files.writeString(OUT.resolve("contributions.json"),JSON.toJson(originalsJson)+"\n");
  tree.install(d);
  var rows=new ArrayList<Map<String,Object>>();dump(d.getRoot(),"",s->true,rows);
  Files.createDirectories(OUT);Files.writeString(OUT.resolve("tree-after.json"),JSON.toJson(rows)+"\n");
  Files.writeString(OUT.resolve("aliases.json"),JSON.toJson(tree.aliases())+"\n");
  check(d.getRoot().getChildren().stream().map(CommandNode::getName).collect(java.util.stream.Collectors.toSet()).equals(Set.of("slavicmyths","sm","smclass","locate")),"Only canonical/compatibility roots");
  check(d.getRoot().getChild("sm").getRedirect()==d.getRoot().getChild("slavicmyths"),"One sm redirect");
  check(!d.getRoot().getChild("locate").canUse(source(0,true)),"Compatibility locate root retains OP2");
  int leaves=0;
  for(var old:originals)if(old.node().getCommand()!=null) {
   leaves++;String path=CommandTree.canonical(old.path());var after=tree.nodes().get(path);
   check(after!=null&&after.getCommand()!=null,"No executable disappears: "+old.path());
   for(boolean player:new boolean[]{false,true})for(int op=0;op<=4;op++) {
    var src=source(op,player);boolean was=old.allowed().test(src),now=after.canUse(src);
    check(!now||was,"No escalation: "+old.path()+" OP"+op+" player="+player);
    if(was&&(player||!CommandTree.playerOnly(path)))check(now,"Valid original context: "+old.path());
    if(path.startsWith("slavicmyths dev ")&&op<2)check(!now,"Admin blocked OP"+op+": "+path);
   }
  }
  for(var alias:tree.aliases().entrySet()) {
   var n=tree.nodes().get(alias.getKey());var target=tree.nodes().get(alias.getValue());
   check(n.getRedirect()!=null||n.getCommand()==target.getCommand(),"Alias shares guarded callback: "+alias.getKey());
   for(int op=0;op<=4;op++)for(boolean player:new boolean[]{false,true}) {
    var src=source(op,player);check(!n.canUse(src)||target.canUse(src),"Alias cannot bypass: "+alias.getKey());
   }
  }
  var ide=net.minecraft.SharedConstants.class.getField("IS_RUNNING_IN_IDE");boolean priorIde=ide.getBoolean(null);ide.setBoolean(null,false);
  var production=new CommandDispatcher<CommandSourceStack>();var productionTree=SlavicCommands.contributions();productionTree.install(production);
  check(!productionTree.nodes().containsKey("slavicmyths dev sivka advance_time ticks"),"IDE-only command absent in production mode");
  check(production.getRoot().getChild("slavicmyths")!=d.getRoot().getChild("slavicmyths"),"Reload gets fresh independent root");
  ide.setBoolean(null,priorIde);
  var src=source(2,true);
  org.slavicmyths.bogatyr.BogatyrConfig.DEBUG.set(false);
  check(!tree.nodes().get("slavicmyths dev bogatyr status").canUse(src),"Debug config gate closed");
  org.slavicmyths.bogatyr.BogatyrConfig.DEBUG.set(true);
  check(tree.nodes().get("slavicmyths dev bogatyr status").canUse(src),"Debug config gate restored");
  var names=d.getCompletionSuggestions(d.parse("slavicmyths ",source(0,true))).join().getList().stream().map(x->x.getText()).toList();
  check(names.containsAll(List.of("help","hero","navigation","kalin","kartaus","wolf"))&&!d.getRoot().getChild("slavicmyths").getChild("dev").canUse(source(0,true)),"Root completion plus server-tree permission filter");
  check(d.getCompletionSuggestions(d.parse("slavicmyths dev ",src)).join().getList().stream().anyMatch(x->x.getText().equals("class")),"OP sees class");
  check(!d.getCompletionSuggestions(d.parse("slavicmyths dev class Offline choose ",src)).join().getList().isEmpty(),"Class ID suggestions retained");
  check(d.getCompletionSuggestions(d.parse("slavicmyths help ",source(0,true))).join().getList().stream().noneMatch(x->x.getText().startsWith("dev")),"Help filters admin");
  check(d.parse("slavicmyths dev class Offline xp 0",src).getReader().canRead(),"Bad XP bound rejected");
  check(d.parse("slavicmyths dev class Offline level 99999",src).getReader().canRead(),"Bad level bound rejected");
  check(d.parse("slavicmyths dev navigation marker not-a-uuid",src).getReader().canRead(),"Bad UUID rejected");
  check(d.execute("slavicmyths help",source(0,true))>0,"Actual help runs without world");
  check(d.execute("slavicmyths help dev city",source(2,true))>0,"Actual admin category help");
  check(d.execute("slavicmyths help dev city",source(0,true))==0,"Inaccessible help category denied");
  // No gameplay execution. All callback replacements are confined to this disposable tree.
  var hits=new AtomicInteger();var spies=new IdentityHashMap<Command<CommandSourceStack>,Command<CommandSourceStack>>();
  var commandField=CommandNode.class.getDeclaredField("command");commandField.setAccessible(true);
  for(var node:tree.nodes().values())if(node.getCommand()!=null)commandField.set(node,spies.computeIfAbsent(node.getCommand(),key->c->{hits.incrementAndGet();return 1;}));
  for(String call:List.of("slavicmyths navigation","sm navigation","slavicmyths dev city info","slavicmyths city debug_plan",
    "slavicmyths debug chud_city info","slavicmyths dev abandoned inspect","sm dev abandoned info","smclass Offline state",
    "slavicmyths dev class Offline state","slavicmyths dev navigation clear-owned","slavicmyths dev navigation clear_owned",
    "slavicmyths locate kurgan small","slavicmyths dev kurgan locate small","locate structure slavicmyths:chud_city","slavicmyths dev city search")) {
   int before=hits.get();check(d.execute(call,src)==1&&hits.get()==before+1,"One parsed action: "+call);
  }
  var originalNodes=new HashMap<String,CommandNode<CommandSourceStack>>();for(var old:originals)originalNodes.put(old.path(),old.node());
  for(var old:originals)if(old.node().getCommand()!=null) {
   var call=new StringBuilder();String prefix="";
   for(String part:old.path().split(" ")) {
    prefix=prefix.isEmpty()?part:prefix+" "+part;var node=originalNodes.get(prefix);String token=part;
    if(node instanceof ArgumentCommandNode<CommandSourceStack,?> arg) {
     if(arg.getType() instanceof IntegerArgumentType v)token=Integer.toString(Math.clamp(0,v.getMinimum(),v.getMaximum()));
     else if(arg.getType() instanceof LongArgumentType v)token=Long.toString(Math.clamp(0L,v.getMinimum(),v.getMaximum()));
     else token=arg.getExamples().iterator().next();
    }
    if(!call.isEmpty())call.append(' ');call.append(token);
   }
   int before=hits.get();check(d.execute(call.toString(),src)==1&&hits.get()==before+1,"Every old executable parses once: "+call);
  }
  check(d.execute("slavicmyths dev clear_kurgan_curse Offline",source(2,false))==1,"Explicit player argument retained for console");
  boolean rejected=false;try{d.execute("smclass Offline state",source(0,true));}catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){rejected=true;}
  check(rejected,"Old root cannot elevate player");
  // Exercise the same native bus registration validation used by automatic subscribers.
  var registrationBus=net.neoforged.bus.api.BusBuilder.builder().build();
  boolean rejectsEmpty=false;try{registrationBus.register(EmptySubscriber.class);}catch(IllegalArgumentException expected){rejectsEmpty=true;}
  check(rejectsEmpty,"Native bus rejects empty subscriber (reported loading regression)");
  int automatic=0;
  try(var files=Files.walk(Path.of("src/main/java"))){for(var path:files.filter(p->p.toString().endsWith(".java")).toList()){
   String source=Files.readString(path);boolean annotated=java.util.regex.Pattern.compile("@(?:[\\w.]+\\.)?EventBusSubscriber\\b").matcher(source).find();
   if(!annotated)continue;
   check(java.util.regex.Pattern.compile("@(?:[\\w.]+\\.)?SubscribeEvent\\b").matcher(source).find(),"Empty automatic subscriber: "+path);
   if(source.contains("org.slavicmyths.commands.CommandTree tree)")||path.endsWith("SlavicCommands.java")){
    String name=Path.of("src/main/java").relativize(path).toString().replace('\\','.').replace('/','.').replaceAll("\\.java$", "");
    registrationBus.register(Class.forName(name));automatic++;check(true,"Native bus registration "+name);
   }
  }}
  check(automatic==7,"Central registrar plus six contributors with remaining non-command events");
  var report=new LinkedHashMap<String,Object>();report.put("status","PASS_OFFLINE");report.put("checks",checks);
  report.put("original_executables_in_contributions",leaves);report.put("tree_nodes",rows.size());report.put("compatibility_nodes",tree.aliases().size());
  report.put("tests","Real builders/Brigadier OP0-4, player/console identity, callback identity, bounds/UUID, class/help completion, one-action parsing spies");
  report.put("not_run","Server event bus; real player selectors/world-backed suggestions; gameplay/text display; 0 clients/servers/GameTests");
  Files.writeString(OUT.resolve("checks.json"),JSON.toJson(report)+"\n");System.out.println("COMMANDS_OFFLINE PASS checks="+checks+" nodes="+rows.size());
 }
 private static void originals(CommandNode<CommandSourceStack> n,String path,Predicate<CommandSourceStack> ancestors,List<Original> rows) {
  var allowed=ancestors.and(n.getRequirement());rows.add(new Original(path,n,allowed));
  for(var child:n.getChildren())originals(child,path+" "+child.getName(),allowed,rows);
 }
 private static void dump(CommandNode<CommandSourceStack> n,String path,Predicate<CommandSourceStack> ancestors,List<Map<String,Object>> out) {
  var allowed=ancestors.and(n.getRequirement());
  if(!path.isEmpty()) {
   var row=new LinkedHashMap<String,Object>();row.put("path",path);row.put("executable",n.getCommand()!=null);row.put("argument",n instanceof ArgumentCommandNode);row.put("examples",n.getExamples());
   if(n instanceof ArgumentCommandNode<CommandSourceStack,?> arg) {
    row.put("argument_type",arg.getType().getClass().getSimpleName());row.put("suggestions",arg.getCustomSuggestions()!=null);
    if(arg.getType() instanceof IntegerArgumentType v){row.put("min",v.getMinimum());row.put("max",v.getMaximum());}
    if(arg.getType() instanceof LongArgumentType v){row.put("min",v.getMinimum());row.put("max",v.getMaximum());}
   }
   for(boolean player:new boolean[]{false,true}) {
    var levels=new ArrayList<Integer>();for(int level=0;level<=4;level++)if(allowed.test(source(level,player)))levels.add(level);
    row.put(player?"player_permissions":"console_permissions",levels);
   }
   String raw=path.replaceAll("[<>]","");if(n.getCommand()!=null)row.put("requires_player_for_execution",CommandTree.playerOnly(tree.aliases().getOrDefault(raw,raw)));String origin=tree.handlerClass(raw);if(!origin.isEmpty())row.put("handler_class",origin);
   if(tree.aliases().containsKey(raw))row.put("alias",tree.aliases().get(raw));
   if(n.getRedirect()!=null)row.put("redirect",n.getRedirect().getName());out.add(row);
  }
  for(var child:n.getChildren())dump(child,path.isEmpty()?child.getUsageText():path+" "+child.getUsageText(),allowed,out);
 }
}
