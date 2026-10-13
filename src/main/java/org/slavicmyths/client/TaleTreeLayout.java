package org.slavicmyths.client;

import java.util.*;

/** Forest layout: each subtree reserves a leaf interval; parents align with their children. */
public final class TaleTreeLayout {
    public static Map<String,int[]> create(Map<String,String> parents){
        var children=new LinkedHashMap<String,List<String>>();
        parents.keySet().forEach(id->children.put(id,new ArrayList<>()));
        var roots=new ArrayList<String>();
        parents.forEach((id,parent)->{if(!id.equals(parent)&&children.containsKey(parent))children.get(parent).add(id);else roots.add(id);});
        var result=new LinkedHashMap<String,int[]>();var seen=new HashSet<String>();int[] row={0};
        for(String root:roots){place(root,0,children,result,seen,row);row[0]++;}
        for(String id:parents.keySet())if(!seen.contains(id)){place(id,0,children,result,seen,row);row[0]++;}
        return result;
    }
    private static int place(String id,int depth,Map<String,List<String>> children,Map<String,int[]> out,Set<String> seen,int[] row){
        if(!seen.add(id))return row[0]*48;
        int first=-1,last=-1;
        for(String child:children.get(id))if(!seen.contains(child)){int y=place(child,depth+1,children,out,seen,row);if(first<0)first=y;last=y;}
        int y=first<0?row[0]++*48:(first+last)/2;
        out.put(id,new int[]{depth*64,y});return y;
    }
    private TaleTreeLayout(){}
}
