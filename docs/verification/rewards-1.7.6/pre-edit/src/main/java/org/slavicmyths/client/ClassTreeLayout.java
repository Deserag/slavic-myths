package org.slavicmyths.client;

import java.util.*;
import org.slavicmyths.rpg.classes.ClassDefinitions;

/** Tidy prerequisite tree: leaf intervals reserve space, parents sit over their children. */
public final class ClassTreeLayout {
    public record Node(String id,int x,int y,String parent) { }
    public record Tree(List<Node> nodes,int width,int height) { }
    public static final int SIZE=44,STEP_X=58,STEP_Y=74;
    public static Tree create(String base) {
        var children=new LinkedHashMap<String,List<String>>();children.put(base,new ArrayList<>());
        for(var s:ClassDefinitions.SKILLS.values())if(s.base().equals(base)){children.computeIfAbsent(s.prerequisite().isEmpty()?base:s.prerequisite(),k->new ArrayList<>()).add(s.id());children.computeIfAbsent(s.id(),k->new ArrayList<>());}
        var nodes=new ArrayList<Node>();int[] leaf={0};place(base,"",0,children,nodes,leaf);
        int depth=nodes.stream().mapToInt(Node::y).max().orElse(0);
        return new Tree(List.copyOf(nodes),Math.max(SIZE,leaf[0]*STEP_X),depth+SIZE);
    }
    private static int place(String id,String parent,int depth,Map<String,List<String>> children,List<Node> nodes,int[] leaf) {
        var list=children.getOrDefault(id,List.of());int x;
        if(list.isEmpty())x=leaf[0]++*STEP_X;
        else {int first=0,last=0;for(int i=0;i<list.size();i++){int child=place(list.get(i),id,depth+1,children,nodes,leaf);if(i==0)first=child;last=child;}x=(first+last)/2;}
        nodes.add(new Node(id,x,depth*STEP_Y,parent));return x;
    }
    private ClassTreeLayout() { }
}
