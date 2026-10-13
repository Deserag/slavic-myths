package org.slavicmyths.client;
/** Original humanoid Zmey: narrow scaled torso, extended snout, fangs, claws and hinged moderate wings.
 * Uses the existing original material atlas: charcoal 19, dark red 16/18, bronze 11, eyes/flame 8.
 * Coordinates are model pixels. Body height 46 px at renderer scale .81 -> 2.33 blocks. */
final class TugarinSerpentGeometry {
    static void build(WorldBossModel m){
        group(m,"root","",0,0,0);group(m,"body","root",0,0,0);group(m,"head","body",0,-20,0);
        box(m,"neck","body",-1.5F,-18,-1.5F,3,4,3,19);
        box(m,"chest","body",-4.5F,-16,-2.5F,9,10,5,19);
        box(m,"belly","body",-3.8F,-7,-2.2F,7.6F,9,4.4F,18);
        box(m,"belt","body",-4.2F,-1,-2.6F,8.4F,2.3F,5.2F,10);
        box(m,"buckle","body",-1,-.7F,-3,2,1.8F,.6F,11);
        box(m,"headCore","head",-2.1F,-3,-2,4.2F,5.5F,4,19);
        box(m,"snout","head",-1.6F,.6F,-5,3.2F,1.4F,3.5F,18);
        box(m,"jaw","head",-1.5F,2,-4.8F,3,1,4.5F,19);
        box(m,"mouthGlow","head",-1,.9F,-5.1F,2,.8F,.12F,8);
        for(int sign:new int[]{-1,1}){
            String side=sign>0?"L":"R";
            box(m,"eye"+side,"head",sign>0?.7F:-1.5F,-.5F,-2.2F,.8F,.55F,.3F,8);
            box(m,"slit"+side,"head",sign>0?1:-1.2F,-.5F,-2.55F,.16F,.55F,.1F,2);
            box(m,"fang"+side,"head",sign>0?1:-1.4F,1.3F,-4.8F,.4F,1.2F,.4F,7);
            group(m,"arm"+side,"body",sign*4.8F,-14,0);
            box(m,"upper"+side,"arm"+side,-1.3F,0,-1.4F,2.6F,7,2.8F,19);
            group(m,"fore"+side,"arm"+side,0,6.5F,0);
            box(m,"forearm"+side,"fore"+side,-1.2F,0,-1.3F,2.4F,6,2.6F,18);
            box(m,"hand"+side,"fore"+side,-1.4F,5.5F,-1.5F,2.8F,2.8F,3,19);
            for(int i=0;i<3;i++)box(m,"claw"+side+i,"fore"+side,-1.2F+i,7.4F,-2,.4F,1.6F,.7F,7);
            group(m,"leg"+side,"root",sign*2.1F,1,0);
            box(m,"pants"+side,"leg"+side,-1.8F,0,-1.8F,3.6F,11,3.6F,16);
            box(m,"boot"+side,"leg"+side,-1.8F,10,-1.9F,3.6F,10,3.8F,10);
            box(m,"foot"+side,"leg"+side,-2,19,-3.5F,4,4,5.5F,19);
            group(m,"wing"+side,"body",sign*3,-14,2);
            // Staggered membranes with three visible support ribs, folded behind shoulders at rest.
            box(m,"wingBase"+side,"wing"+side,sign>0?0:-6,0,0,6,11,.7F,18);
            box(m,"wingOuter"+side,"wing"+side,sign>0?5:-11,2,.1F,6,7,.55F,16);
            for(int i=0;i<3;i++)box(m,"wingRib"+side+i,"wing"+side,sign*(2+i*3)-.25F,-.5F,-.2F,.5F,11-i*2,.8F,19);
            for(int i=0;i<5;i++)box(m,"scale"+side+i,"body",sign>0?2.5F:-4.2F,-15+i*2, -2.7F,1.7F,1.3F,.35F,2);
        }
    }
    private static void group(WorldBossModel m,String n,String parent,float x,float y,float z){m.add(n,parent,x,y,z,null,0,0,0,0);}
    private static void box(WorldBossModel m,String n,String parent,float x,float y,float z,float w,float h,float d,int material){m.add(n,parent,x,y,z,new float[]{0,0,0,w,h,d},material,0,0,0);}
    private TugarinSerpentGeometry(){}
}
