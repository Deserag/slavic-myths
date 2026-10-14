import java.util.*;
import java.io.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import org.slavicmyths.rpg.*;
import org.slavicmyths.rpg.tools.*;
import org.slavicmyths.item.ItemState.RuneState;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.codec.ByteBufCodecs;
import io.netty.buffer.Unpooled;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Production rule/detector, real codec and native ABI tests. No world, loader or game process. */
public class ToolRunesOffline {
 static int checks,scenes;
 static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
 static final ToolRuneTrees.Node GROUND=new ToolRuneTrees.Node(ToolRuneTrees.Kind.GROUND,"",false,false,false);
 static ToolRuneTrees.Node log(String family,boolean placed){return new ToolRuneTrees.Node(ToolRuneTrees.Kind.LOG,family,true,placed,false);}
 static ToolRuneTrees.Node leaf(String family,boolean persistent){return new ToolRuneTrees.Node(ToolRuneTrees.Kind.LEAF,family,false,false,persistent);}
 static void tree(Map<BlockPos,ToolRuneTrees.Node> map,BlockPos root,String family,int height,boolean big){
  int width=big?2:1;
  for(int x=0;x<width;x++)for(int z=0;z<width;z++){var p=root.offset(x,0,z);map.put(p.below(),GROUND);for(int y=0;y<height;y++)map.put(p.above(y),log(family,false));}
  for(int x=-1;x<=width;x++)for(int z=-1;z<=width;z++)if(x<0||z<0||x==width||z==width)map.put(root.offset(x,height-1,z),leaf(family,false));
 }
 static List<BlockPos> plan(Map<BlockPos,ToolRuneTrees.Node> map,BlockPos origin,String family,int tier){
  final int[] calls={0};var job=new ToolRuneTrees.Job(p->{calls[0]++;return map.getOrDefault(p,ToolRuneTrees.Node.AIR);},origin,family,tier);
  int ticks=0;
  while(!job.done()&&ticks++<160){int before=calls[0];job.step(ToolRuneRules.READS_PER_TICK);check(calls[0]-before<=ToolRuneRules.READS_PER_TICK,"read budget per actual production detector step");}
  check(job.done(),"finite tree plan");check(job.sampled()<=ToolRuneRules.MAX_READS,"cache/read hard cap");var out=job.result();check(new HashSet<>(out).size()==out.size()&&!out.contains(origin),"origin is not repeated, targets unique");check(out.size()+1<=ToolRuneRules.logs(tier),"total logs includes original");scenes++;return out;
 }
 static ClassNode node(String name)throws Exception{try(var stream=ToolRunesOffline.class.getClassLoader().getResourceAsStream(name+".class")){check(stream!=null,"native class available "+name);var n=new ClassNode();new ClassReader(stream).accept(n,0);return n;}}
 static MethodNode method(ClassNode c,String name,String desc){return c.methods.stream().filter(m->m.name.equals(name)&&m.desc.equals(desc)).findFirst().orElseThrow(()->new AssertionError("native ABI missing "+name+desc));}
 static int call(MethodNode m,String owner,String name){int i=0;for(var n:m.instructions){if(n instanceof MethodInsnNode mi&&mi.owner.equals(owner)&&mi.name.equals(name))return i;i++;}return -1;}
 public static void main(String[] args)throws Exception{
  check(new HashSet<>(ToolRuneRules.IDS).size()==8,"exact eight new IDs");
  for(String id:ToolRuneRules.IDS){
   String family=ToolRuneRules.family(id),other=family+(id.endsWith("_iv")?"_iii":"_iv");
   check(Runes.installationReason(1,3,3,List.of(other),id,true).equals("tool_family_occupied"),"opposite-tier conflict both orders "+id);
   check(Runes.installationReason(1,3,3,List.of(id),id,true).equals("duplicate_rune"),"same rune duplicate "+id);
   check(Runes.installationReason(1,3,3,List.of(),id,false).equals("incompatible_rune"),"wrong tool refused "+id);
   check(Runes.installationReason(1,3,0,List.of(),id,true).equals("socket_full"),"no prepared socket "+id);
   check(Runes.installationReason(2,3,3,List.of(),id,true).equals("unsupported"),"stack installation refused "+id);
   check(ToolRuneRules.tier(List.of(family+"_iii",family+"_iv"),family)==4,"legacy mixed family IV wins");
   if(family.equals("miner"))check(Runes.installationReason(1,3,3,List.of("mastery_iv","depths_iii"),id,true).isEmpty(),"old mining effects coexist with area rune");
  }
  var historical=new RuneState(3,List.of("miner_iii","miner_iv","mastery_iv"));check(RuneState.CODEC.parse(NbtOps.INSTANCE,RuneState.CODEC.encodeStart(NbtOps.INSTANCE,historical).getOrThrow()).getOrThrow().equals(historical),"preserve historical mixed tiers without migration");
  for(int tier:new int[]{3,4}){
   var base=new RuneBase(tier,"perunite");check(RuneBase.CODEC.parse(JsonOps.INSTANCE,RuneBase.CODEC.encodeStart(JsonOps.INSTANCE,base).getOrThrow()).getOrThrow().equals(base),"foundation codec reused");var codec=ByteBufCodecs.fromCodec(RuneBase.CODEC);var b=Unpooled.buffer();try{codec.encode(b,base);check(codec.decode(b).equals(base),"native base wire");}finally{b.release();}
   for(Direction face:Direction.values())for(int seed=0;seed<32;seed++){
    var origin=new BlockPos(-25+seed,64-seed,31-seed);var positions=ToolRuneRules.plane(origin,face,tier);check(positions.size()==(tier==3?9:18)&&new HashSet<>(positions).size()==positions.size(),"six directions exact count");check(Collections.frequency(positions,origin)==1,"center once");
    for(var p:positions){var delta=p.subtract(origin);int depth=-(delta.getX()*face.getStepX()+delta.getY()*face.getStepY()+delta.getZ()*face.getStepZ());check(depth>=0&&depth<(tier==4?2:1),"depth goes inward");switch(face.getAxis()){case X->check(Math.abs(delta.getY())<=1&&Math.abs(delta.getZ())<=1,"YZ plane");case Y->check(Math.abs(delta.getX())<=1&&Math.abs(delta.getZ())<=1,"XZ plane");case Z->check(Math.abs(delta.getX())<=1&&Math.abs(delta.getY())<=1,"XY plane");}}
   }
   var field=ToolRuneRules.field(BlockPos.ZERO,tier);check(field.size()==(tier==3?9:25)&&field.stream().allMatch(p->p.getY()==0),"flat farmer area");check(ToolRuneRules.plane(BlockPos.ZERO,null,tier).equals(List.of(BlockPos.ZERO)),"unknown face is one block");
  }
  for(String name:List.of("oak","birch","spruce","jungle","acacia","dark_oak","mangrove","cherry")){check(ToolRuneRules.logFamily("minecraft:"+name+"_log").equals("minecraft:"+name),"species log "+name);check(ToolRuneRules.logFamily("minecraft:"+name+"_wood").equals("minecraft:"+name),"species wood "+name);check(ToolRuneRules.logFamily("minecraft:stripped_"+name+"_log").isEmpty(),"stripped excluded "+name);check(ToolRuneRules.leafFamily("minecraft:"+name+"_leaves").equals("minecraft:"+name),"matching canopy "+name);}
  check(ToolRuneRules.logFamily("slavicmyths:rowan_log").equals("slavicmyths:rowan")&&!ToolRuneRules.logFamily("minecraft:oak_log").equals(ToolRuneRules.logFamily("slavicmyths:oak_log")),"namespace identity");
  var origin=new BlockPos(0,64,0);
  for(int tier:new int[]{3,4})for(int radius=1;radius<=10;radius++)for(int dy=-3;dy<=3;dy++)check(ToolRuneRules.neighbor(origin,origin.offset(radius,dy,0),tier)==(radius<=ToolRuneRules.radius(tier)&&Math.abs(dy)<=2),"radius/height boundaries");
  for(int tier:new int[]{3,4}){
   var world=new HashMap<BlockPos,ToolRuneTrees.Node>();int radius=ToolRuneRules.radius(tier);tree(world,origin,"minecraft:oak",6,false);tree(world,origin.offset(radius,0,0),"minecraft:oak",6,false);tree(world,origin.offset(0,0,radius),"minecraft:birch",6,false);
   var result=plan(world,origin,"minecraft:oak",tier);check(result.size()==11,"two oaks at radius, no birch");check(result.stream().allMatch(p->world.get(p).family().equals("minecraft:oak")),"only same species logs");
   world.clear();tree(world,origin,"minecraft:oak",6,false);tree(world,origin.offset(radius+1,0,0),"minecraft:oak",6,false);check(plan(world,origin,"minecraft:oak",tier).size()==5,"outside radius");
   world.clear();tree(world,origin,"minecraft:oak",6,false);tree(world,origin.offset(radius,2,0),"minecraft:oak",6,false);check(plan(world,origin,"minecraft:oak",tier).size()==11,"height +2 accepted");
   world.clear();tree(world,origin,"minecraft:oak",6,false);tree(world,origin.offset(radius,3,0),"minecraft:oak",6,false);check(plan(world,origin,"minecraft:oak",tier).size()==5,"height +3 excluded");
   for(String unsafe:List.of("placed","roof","container","persistent_leaves","horizontal_logs","unloaded","no_crown","huge","bare_pillar")){
    world.clear();tree(world,origin,"minecraft:oak",6,false);
    switch(unsafe){
     case "placed"->world.put(origin.above(2),log("minecraft:oak",true));
     case "roof","container"->world.put(origin.offset(1,3,0),new ToolRuneTrees.Node(ToolRuneTrees.Kind.OTHER,"",false,false,false));
     case "persistent_leaves"->world.put(origin.offset(1,5,0),leaf("minecraft:oak",true));
     case "horizontal_logs"->world.put(origin.offset(1,3,0),new ToolRuneTrees.Node(ToolRuneTrees.Kind.LOG,"minecraft:oak",false,false,false));
     case "unloaded"->world.put(origin.offset(2,4,0),ToolRuneTrees.Node.UNKNOWN);
     case "no_crown","bare_pillar"->world.entrySet().removeIf(e->e.getValue().kind()==ToolRuneTrees.Kind.LEAF);
     case "huge"->{world.clear();tree(world,origin,"minecraft:oak",33,false);}
    }
    check(plan(world,origin,"minecraft:oak",tier).isEmpty(),"conservative negative scene "+unsafe);
   }
  }
  var world=new HashMap<BlockPos,ToolRuneTrees.Node>();tree(world,origin,"minecraft:spruce",6,true);tree(world,origin.offset(8,0,0),"minecraft:spruce",6,true);check(plan(world,origin,"minecraft:spruce",4).size()==47,"2x2 nearest stems at R7, not canonical-corner distance");
  for(int height:new int[]{4,6,10,16,24,32}){world.clear();for(int x=-4;x<=4;x+=4)for(int z=-4;z<=4;z+=4)tree(world,origin.offset(x,0,z),"minecraft:oak",height,false);for(int tier:new int[]{3,4}){var result=plan(world,origin,"minecraft:oak",tier);check(result.size()+1<=ToolRuneRules.logs(tier),"dense grove keeps total 48/96 bound");}}
  // A tree-shaped object with unknown historical provenance is indistinguishable: explicitly not a 100% safety proof.
  world.clear();tree(world,origin,"minecraft:oak",6,false);check(plan(world,origin,"minecraft:oak",3).size()==5,"unknown provenance limitation explicit");
  var a=new ToolRuneTrees.Job(p->world.getOrDefault(p,ToolRuneTrees.Node.AIR),origin,"minecraft:oak",3);var b=new ToolRuneTrees.Job(p->ToolRuneTrees.Node.UNKNOWN,origin,"minecraft:oak",4);for(int i=0;i<160&&(!a.done()||!b.done());i++){a.step(32);b.step(32);}check(a.done()&&b.done()&&a.result().size()==5&&b.result().isEmpty(),"independent interleaved operation snapshots");
  var data=new ToolLogOrigins();var marked=new BlockPos(-18,-63,32);data.mark(marked);check(data.blocked(marked)&&!data.blocked(marked.offset(1,0,0)),"placement deny list exact");data.denyChunk(8,-2);check(data.blocked(new BlockPos(8*16,70,-2*16)),"unloaded piston area fails closed without scan");var saved=data.save(new CompoundTag(),null);var restored=ToolLogOrigins.load(saved,null);check(restored.blocked(marked)&&restored.blocked(new BlockPos(8*16+15,70,-2*16+15)),"native provenance NBT roundtrip");
  var future=new CompoundTag();future.putInt("Version",99);future.putString("Opaque","keep");var opaque=ToolLogOrigins.load(future,null);check(opaque.blocked(BlockPos.ZERO)&&opaque.save(new CompoundTag(),null).equals(future),"future provenance passthrough, auto disabled");
  var invalid=new CompoundTag();invalid.putString("Placed","bad");check(ToolLogOrigins.load(invalid,null).disabled(),"invalid provenance fails closed");
  var cap=new ToolLogOrigins();for(int i=0;i<=ToolLogOrigins.MAX;i++)cap.mark(new BlockPos(i,64,0));check(cap.disabled()&&cap.blocked(new BlockPos(-300,64,-300)),"placement cap disables instead of evicting protections");
  var game=node("net/minecraft/server/level/ServerPlayerGameMode");var destroy=method(game,"destroyBlock","(Lnet/minecraft/core/BlockPos;)Z");check(call(destroy,"net/neoforged/neoforge/common/CommonHooks","fireBlockBreak")>=0,"native protected break hook");check(call(destroy,"net/minecraft/world/item/ItemStack","mineBlock")>=0&&call(destroy,"net/minecraft/world/level/block/Block","playerDestroy")>=0,"native durability and loot callbacks");method(game,"removeBlock","(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z");check(game.fields.stream().anyMatch(f->f.name.equals("player")&&(f.access&Opcodes.ACC_FINAL)!=0),"mixin shadow native ABI");
  var stack=node("net/minecraft/world/item/ItemStack");var use=method(stack,"useOn","(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;");check(call(use,"net/neoforged/neoforge/common/CommonHooks","onPlaceItemIntoWorld")>=0,"accepted-use redirect target exists");check(call(use,"net/neoforged/bus/api/IEventBus","post")>=0,"native use event before placement path");
  System.out.println("TOOL_RUNES_OFFLINE_CHECKS="+checks+" DETECTOR_SCENES="+scenes+" GAME_STARTS=0");
 }
}
