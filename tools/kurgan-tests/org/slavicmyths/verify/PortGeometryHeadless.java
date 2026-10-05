package org.slavicmyths.verify;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.slavicmyths.client.FolkModelGeometry;

/** CPU-only rendering into a collector; never initializes a window or Minecraft client. */
public final class PortGeometryHeadless {
    private static final class Vertices implements VertexConsumer {
        int count, alpha;
        float minX=Float.POSITIVE_INFINITY, maxX=Float.NEGATIVE_INFINITY;
        float minY=Float.POSITIVE_INFINITY, maxY=Float.NEGATIVE_INFINITY;
        float minZ=Float.POSITIVE_INFINITY, maxZ=Float.NEGATIVE_INFINITY;
        float minU=Float.POSITIVE_INFINITY, minV=Float.POSITIVE_INFINITY;
        public VertexConsumer addVertex(float x,float y,float z) {
            count++;minX=Math.min(minX,x);maxX=Math.max(maxX,x);
            minY=Math.min(minY,y);maxY=Math.max(maxY,y);minZ=Math.min(minZ,z);maxZ=Math.max(maxZ,z);return this;
        }
        public VertexConsumer setColor(int r,int g,int b,int a) { alpha=a;return this; }
        public VertexConsumer setUv(float u,float v) { minU=Math.min(minU,u);minV=Math.min(minV,v);return this; }
        public VertexConsumer setUv1(int u,int v) { return this; }
        public VertexConsumer setUv2(int u,int v) { return this; }
        public VertexConsumer setNormal(float x,float y,float z) {
            if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z))throw new AssertionError("Invalid normal");return this;
        }
    }
    private static void near(float actual,float expected) {
        if(Math.abs(actual-expected)>.00001F)throw new AssertionError(actual+" != "+expected);
    }
    private static void rejects(Runnable action) {
        try { action.run();throw new AssertionError("Invalid hierarchy accepted"); }
        catch(IllegalArgumentException expected) { }
    }
    public static void main(String[] args) {
        var geometry=new FolkModelGeometry();
        var root=geometry.part(512,512);root.setPos(16,0,0);
        var child=geometry.part(512,512,64,128);child.setPos(0,16,0);
        geometry.box(child,-1.2F,0,-2.3F,2.4F,5.6F,4.6F);
        geometry.attach(root,child);
        if(root.getAllParts().count()!=2)throw new AssertionError("Lost child");
        var out=new Vertices();root.render(new PoseStack(),out,0,0,0xaa4080c0);
        if(out.count!=24||out.alpha!=170)throw new AssertionError("Lost faces/color");
        near(out.minX,.925F);near(out.maxX,1.075F);near(out.minY,1);near(out.maxY,1.35F);
        near(out.minZ,-2.3F/16);near(out.maxZ,2.3F/16);near(out.minU,64F/512);near(out.minV,128F/512);
        rejects(()->geometry.attach(child,root));rejects(()->geometry.attach(root,child));
        rejects(()->geometry.attach(root,new FolkModelGeometry().part(64,64)));
        if((FolkModelGeometry.withOpacity(0xaa4080c0,.5F)>>>24)!=85)throw new AssertionError("Changed opacity");
        child.visible=false;var hidden=new Vertices();root.render(new PoseStack(),hidden,0,0);
        if(hidden.count!=0)throw new AssertionError("Visibility ignored");
        System.out.println("PASS: real ModelPart CPU vertices, fractional boxes, hierarchy/pivots, UV atlas, opacity and visibility. In-game visuals remain MANUAL.");
    }
}
