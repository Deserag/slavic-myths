import java.util.*;
import org.slavicmyths.client.ClassTreeLayout;
import org.slavicmyths.client.ClassTreeViewport;
import org.slavicmyths.rpg.classes.*;

/** Offline rules/layout checks: does not load Minecraft or touch a world. */
public final class ClassUiChecks {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  for(var skill:ClassDefinitions.SKILLS.values()){
   for(int rank=0;rank<skill.maxRank();rank++){
    check(ClassLearningRules.blocker(skill.base(),skill,rank,1,0,false,0,true).isEmpty(),"Creative "+skill.id());
    check(ClassLearningRules.blocker("invalid",skill,rank,50,3,true,100,true).equals("wrong_class"),"Identity "+skill.id());
    for(int level:new int[]{1,skill.level()+rank*2-1,skill.level()+rank*2,50})for(int prerequisite:new int[]{0,3})for(boolean branch:new boolean[]{false,true})for(int points:new int[]{0,1}){
     String expected=level<skill.level()+rank*2?"level_required":!skill.prerequisite().isEmpty()&&prerequisite<skill.prerequisiteRank()?"prerequisite":!skill.branch().isEmpty()&&!branch?"branch_required":points<1?"no_points":"";
     check(ClassLearningRules.blocker(skill.base(),skill,rank,level,prerequisite,branch,points,false).equals(expected),"Survival "+skill.id());
    }
   }
   check(ClassLearningRules.blocker(skill.base(),skill,skill.maxRank(),50,3,true,100,true).equals("max_rank"),"Cap "+skill.id());
  }
  for(String id:ClassDefinitions.SKILLS.keySet())check(java.nio.file.Files.isRegularFile(java.nio.file.Path.of("src/main/resources/assets/slavicmyths/textures/gui/classes/"+id+".png")),"Actual skill icon "+id);
  for(var branch:ClassDefinitions.BRANCHES.values())check(java.nio.file.Files.isRegularFile(java.nio.file.Path.of("src/main/resources/assets/slavicmyths/textures/gui/classes/"+branch.enhancedSkill()+".png")),"Actual branch enhanced skill icon "+branch.id());
  check(ClassLearningRules.blocker("",null,0,1,0,false,0,true).equals("wrong_class"),"Unknown skill");
  for(int points:new int[]{0,1,100}){check(ClassLearningRules.pointsAfterLearning(points,true)==points,"Creative cost");if(points>0)check(ClassLearningRules.pointsAfterLearning(points,false)==points-1,"Survival cost");}
  for(String base:ClassDefinitions.BASES){
   var tree=ClassTreeLayout.create(base);var nodes=new HashMap<String,ClassTreeLayout.Node>();for(var node:tree.nodes())check(nodes.put(node.id(),node)==null,"Duplicate node");
   check(tree.nodes().size()==1+ClassDefinitions.SKILLS.values().stream().filter(s->s.base().equals(base)).count(),"Complete tree");
   check(tree.nodes().stream().filter(n->n.parent().equals(base)).count()>=2,"Two root lines");
   for(var node:tree.nodes()){
    check(node.x()>=0&&node.x()+44<=tree.width()&&node.y()+44<=tree.height(),"Tree bounds");
    if(!node.id().equals(base)){var s=ClassDefinitions.SKILLS.get(node.id());check(node.parent().equals(s.prerequisite().isEmpty()?base:s.prerequisite()),"Actual prerequisite");check(nodes.get(node.parent()).y()+ClassTreeLayout.STEP_Y==node.y(),"Depth");}
    for(var other:tree.nodes())if(other!=node)check(Math.abs(node.x()-other.x())>=44||Math.abs(node.y()-other.y())>=54,"Node/rank overlap");
   }
  }
  for(int[] resolution:new int[][]{{1280,720},{1649,928}})for(int guiScale:new int[]{2,3,4}){
   int width=(int)Math.ceil(resolution[0]/(double)guiScale),height=(int)Math.ceil(resolution[1]/(double)guiScale);
   var canvas=ClassTreeViewport.canvas(width,height);check(canvas.width()==width-16,"Full available width, no sidebar");check(canvas.bottom()==height-46,"Only compact quickbar below canvas");
   for(int index=0;index<5;index++){var slot=ClassTreeViewport.slot(width,height,index);check(slot.x()>=0&&slot.right()<=width&&slot.bottom()<=height,"Slot bounds");check(!slot.overlaps(canvas),"No slot/canvas overlap");for(int other=index+1;other<5;other++)check(!slot.overlaps(ClassTreeViewport.slot(width,height,other)),"Distinct slots");}
   for(String base:ClassDefinitions.BASES){var tree=ClassTreeLayout.create(base);var camera=new ClassTreeViewport();camera.resize(canvas);camera.open(tree);
    check(camera.scale()>=.65,"Initial readable nodes; pan exposes full graph");
    camera.fit(tree);for(var node:tree.nodes()){var r=camera.node(node);check(r.x()>=canvas.x()&&r.right()<=canvas.right()&&r.y()>=canvas.y()&&r.bottom()<=canvas.bottom(),"Fit entire graph "+node.id());if(!node.parent().isEmpty())check(camera.hit(tree,r.x()+r.width()/2.0,r.y()+r.height()/2.0).equals(node.id()),"Renderer/hitbox share exact transform "+node.id());}
    double px=canvas.x()+canvas.width()*.37,py=canvas.y()+canvas.height()*.63,worldX=camera.worldX(px),worldY=camera.worldY(py);camera.zoom(px,py,3);
    check(Math.abs(camera.worldX(px)-worldX)<1e-8&&Math.abs(camera.worldY(py)-worldY)<1e-8,"Pointer anchored zoom");
    double sx=camera.screenX(20),sy=camera.screenY(30);camera.pan(47,-23);check(Math.abs(camera.screenX(20)-sx-47)<1e-8&&Math.abs(camera.screenY(30)-sy+23)<1e-8,"Pan is screen-pixel accurate");
    sx=camera.screenX(20);sy=camera.screenY(30);camera.resize(canvas);camera.open(tree);check(Math.abs(camera.screenX(20)-sx)<1e-8&&Math.abs(camera.screenY(30)-sy)<1e-8,"Network/selection refresh preserves camera");
    for(var node:tree.nodes())if(!node.parent().isEmpty()){var overlay=camera.card(tree,node.id(),Math.min(280,width-80),Math.min(220,height-24));check(overlay.x()>=0&&overlay.right()<=width&&overlay.y()>=0&&overlay.bottom()<=canvas.bottom(),"Transient card bounds");check(overlay.height()-56>=50,"Scrollable card has readable body");var focus=camera.node(node);int free=Math.max(focus.x()-canvas.x()-8,canvas.right()-focus.right()-8);if(free>=120)check(!overlay.overlaps(focus),"Card avoids selected node when side space exists");}
    check(camera.hit(tree,canvas.x()-1,canvas.y()).isEmpty(),"No clicks outside scissor");
    camera.pan(10000,-10000);camera.fit(tree);for(var node:tree.nodes()){var r=camera.node(node);check(r.x()>=canvas.x()&&r.bottom()<=canvas.bottom(),"Fit restores lost canvas");}
   }
  }
  if(args.length>0&&args[0].equals("--nodes"))for(String base:ClassDefinitions.BASES)for(var node:ClassTreeLayout.create(base).nodes())System.out.println("NODE\t"+base+"\t"+node.id()+"\t"+node.x()+"\t"+node.y()+"\t"+node.parent());
  System.out.println("{\"offline_rule_and_layout_checks\":"+checks+",\"skills\":"+ClassDefinitions.SKILLS.size()+",\"trees\":3,\"resolution_scale_pairs\":6,\"minecraft_launches\":0}");
 }
}
