package org.slavicmyths.client;

import java.util.Map;

/** One integer screen transform for drawing, hit testing, fitting and centering book nodes. */
public final class TaleViewport {
    private float zoom = 1;
    private double panX, panY;
    public float zoom(){return zoom;}
    public int x(int origin,int logical){return origin+(int)Math.round(panX+logical*zoom);}
    public int y(int origin,int logical){return origin+(int)Math.round(panY+logical*zoom);}
    public int size(){return Math.max(1,Math.round(32*zoom));}
    public void pan(double x,double y){panX+=x;panY+=y;}
    public void zoom(float value,int width,int height){
        float next=Math.clamp(value,.005F,2F),ratio=next/zoom;
        panX=width/2.0-(width/2.0-panX)*ratio;panY=height/2.0-(height/2.0-panY)*ratio;zoom=next;
    }
    public void center(int[] at,int width,int height){panX=width/2.0-(at[0]+16)*zoom;panY=height/2.0-(at[1]+16)*zoom;}
    public void fit(Map<String,int[]> nodes,int width,int height){
        if(nodes.isEmpty()){zoom=1;panX=panY=0;return;}
        int minX=Integer.MAX_VALUE,minY=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,maxY=Integer.MIN_VALUE;
        for(var at:nodes.values()){minX=Math.min(minX,at[0]);minY=Math.min(minY,at[1]);maxX=Math.max(maxX,at[0]+32);maxY=Math.max(maxY,at[1]+32);}
        zoom=Math.clamp(Math.min((width-20F)/Math.max(32,maxX-minX),(height-20F)/Math.max(32,maxY-minY)),.005F,1F);
        panX=(width-(maxX-minX)*zoom)/2.0-minX*zoom;panY=(height-(maxY-minY)*zoom)/2.0-minY*zoom;
    }
    public String hit(Map<String,int[]> nodes,int left,int top,double mx,double my){
        for(var e:nodes.entrySet()){int x=x(left,e.getValue()[0]),y=y(top,e.getValue()[1]);if(mx>=x&&mx<x+size()&&my>=y&&my<y+size())return e.getKey();}
        return "";
    }
}
