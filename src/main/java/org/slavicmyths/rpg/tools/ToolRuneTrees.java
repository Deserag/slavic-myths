package org.slavicmyths.rpg.tools;

import java.util.*;
import java.util.function.Function;
import net.minecraft.core.BlockPos;

/** Conservative straight 1x1/2x2 trunk detector, incrementally budgeted snapshot reads. */
public final class ToolRuneTrees {
 public enum Kind {AIR,GROUND,LOG,LEAF,OTHER,UNKNOWN}
 public record Node(Kind kind,String family,boolean vertical,boolean placed,boolean persistent){
  public static final Node AIR=new Node(Kind.AIR,"",false,false,false),UNKNOWN=new Node(Kind.UNKNOWN,"",false,false,false);
 }
 public record Tree(BlockPos base,List<BlockPos> roots,List<BlockPos> logs){}
 private static final class Pause extends RuntimeException {private Pause(){super(null,null,false,false);}}
 private static final Pause PAUSE=new Pause();
 public static final class Job {
  private final Function<BlockPos,Node> world;
  private final BlockPos origin;private final String family;private final int tier;
  private final Map<BlockPos,Node> cache=new HashMap<>();
  private final LinkedHashSet<BlockPos> output=new LinkedHashSet<>(),seenBases=new LinkedHashSet<>();
  private List<BlockPos> candidates=List.of(),originalRoots=List.of();private BlockPos base;private int next,reads,inspected;
  private boolean done;
  public Job(Function<BlockPos,Node> world,BlockPos origin,String family,int tier){this.world=world;this.origin=origin.immutable();this.family=family;this.tier=tier;}
  private Node read(BlockPos pos){Node n=cache.get(pos);if(n!=null)return n;if(cache.size()>=ToolRuneRules.MAX_READS){done=true;throw PAUSE;}if(reads<=0)throw PAUSE;reads--;n=world.apply(pos);if(n==null)n=Node.UNKNOWN;cache.put(pos.immutable(),n);return n;}
  private boolean log(Node n){return n.kind==Kind.LOG&&n.vertical&&!n.placed&&n.family.equals(family);}
  private Tree inspect(BlockPos start){
   if(!log(read(start)))return null;
   BlockPos root=start;int down=0;
   while(log(read(root.below()))&&down++<ToolRuneRules.MAX_HEIGHT)root=root.below();
   if(down>ToolRuneRules.MAX_HEIGHT||read(root.below()).kind!=Kind.GROUND)return null;
   var columns=new ArrayList<BlockPos>();
   for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var p=root.offset(x,0,z);if(log(read(p))&&read(p.below()).kind==Kind.GROUND)columns.add(p);}
   if(columns.size()!=1&&columns.size()!=4)return null;
   int minX=columns.stream().mapToInt(BlockPos::getX).min().orElseThrow(),maxX=columns.stream().mapToInt(BlockPos::getX).max().orElseThrow();
   int minZ=columns.stream().mapToInt(BlockPos::getZ).min().orElseThrow(),maxZ=columns.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
   if(columns.size()==4&&(maxX-minX!=1||maxZ-minZ!=1))return null;
   var footprint=new HashSet<>(columns);var logs=new ArrayList<BlockPos>();int top=0,lowest=999;
   for(var c:columns){int height=0;for(;height<ToolRuneRules.MAX_HEIGHT;height++){var p=c.above(height);var n=read(p);if(n.kind!=Kind.LOG)break;if(!log(n))return null;logs.add(p);}if(height<4||read(c.above(height)).kind==Kind.LOG)return null;top=Math.max(top,height);lowest=Math.min(lowest,height);}
   if(top-lowest>2)return null;
   var logSet=new HashSet<>(logs);int leaves=0;
   for(int y=0;y<=top+1;y++)for(int x=minX-2;x<=maxX+2;x++)for(int z=minZ-2;z<=maxZ+2;z++){
    var p=new BlockPos(x,root.getY()+y,z);if(footprint.contains(new BlockPos(x,root.getY(),z))&&y<top&&logSet.contains(p))continue;
    var n=read(p);
    if(n.kind==Kind.UNKNOWN||n.kind==Kind.LOG||n.kind==Kind.OTHER||n.kind==Kind.GROUND&&y>1||n.kind==Kind.LEAF&&n.persistent)return null;
    if(n.kind==Kind.LEAF&&n.family.equals(family)&&y>=lowest-3)leaves++;
   }
   if(leaves<4)return null;
   logs.sort(Comparator.<BlockPos>comparingInt(p->p.getY()).reversed().thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));
   return new Tree(new BlockPos(minX,root.getY(),minZ),List.copyOf(columns),List.copyOf(logs));
  }
  private void accept(Tree t){seenBases.add(t.base);if(output.size()+1+(int)t.logs.stream().filter(p->!p.equals(origin)&&!output.contains(p)).count()>ToolRuneRules.logs(tier))return;for(var p:t.logs)if(!p.equals(origin))output.add(p);}
  private long distance(BlockPos p){return originalRoots.stream().mapToLong(r->{long x=p.getX()-r.getX(),z=p.getZ()-r.getZ();return x*x+z*z;}).min().orElse(Long.MAX_VALUE);}
  private void candidates(){var unique=new LinkedHashSet<BlockPos>();int r=ToolRuneRules.radius(tier);for(var root:originalRoots)for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)if(x*x+z*z<=r*r)for(int y=-2;y<=2;y++)unique.add(root.offset(x,y,z));var a=new ArrayList<>(unique);a.sort(Comparator.<BlockPos>comparingLong(this::distance).thenComparingInt(p->Math.abs(p.getY()-base.getY())).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));candidates=List.copyOf(a);}
  public boolean step(int budget){
   if(done)return true;reads=Math.min(Math.max(0,budget),ToolRuneRules.READS_PER_TICK);
   try{
    if(base==null){var tree=inspect(origin);if(tree==null||tree.logs.size()>ToolRuneRules.logs(tier)){done=true;return true;}base=tree.base;originalRoots=tree.roots;accept(tree);candidates();}
    while(next<candidates.size()&&inspected<ToolRuneRules.MAX_TREES&&output.size()+1<ToolRuneRules.logs(tier)){
     var p=candidates.get(next);if(log(read(p))&&read(p.below()).kind==Kind.GROUND){var tree=inspect(p);if(tree!=null&&!seenBases.contains(tree.base)){inspected++;if(ToolRuneRules.neighborTrees(originalRoots,tree.roots,tier))accept(tree);}}
     next++;
    }
    done=true;
   }catch(Pause ignored){}return done;
  }
  public List<BlockPos> result(){return done?List.copyOf(output):List.of();}
  public int sampled(){return cache.size();}
  public boolean done(){return done;}
 }
 private ToolRuneTrees(){}
}
