import java.util.*;
import org.slavicmyths.client.ClassTreeLayout;
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
  for(int[] resolution:new int[][]{{1920,1080},{2560,1440}})for(int scale:new int[]{2,3,4}){
   int width=(int)Math.ceil(resolution[0]/(double)scale),height=(int)Math.ceil(resolution[1]/(double)scale),panel=Math.min(900,width-16),left=(width-panel)/2,right=left+panel-Math.min(292,Math.max(202,panel*2/5)),top=74,bottom=height-72;
   check(right-left-14>=230,"Tree viewport");check(bottom-top>=124,"Tree height");check(right+8+panel-(right-left)-16<=width,"Detail bounds");
   int preview=Math.max(88,panel/5),card=Math.max(142,panel/4),detail=left+preview+12+card+12,h=Math.min(66,(height-112)/3);
   check(left+panel-detail-10>=180,"Choice detail width");check(top+2*(h+5)+h<=height-16,"Choice cards");check(top+25+2*53+22<height,"Bindings bounds");check(height-57+47<height,"Loadout bounds");
  }
  System.out.println("{\"offline_rule_and_layout_checks\":"+checks+",\"skills\":"+ClassDefinitions.SKILLS.size()+",\"trees\":3,\"resolution_scale_pairs\":6,\"minecraft_launches\":0}");
 }
}
