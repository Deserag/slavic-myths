package org.slavicmyths.client;

/** Shared by renderer and pointer handling; no Minecraft dependency. */
public final class ClassTreeViewport {
 public record Rect(int x,int y,int width,int height){
  public int right(){return x+width;} public int bottom(){return y+height;}
  public boolean contains(double px,double py){return px>=x&&px<right()&&py>=y&&py<bottom();}
  public boolean overlaps(Rect other){return x<other.right()&&right()>other.x&&y<other.bottom()&&bottom()>other.y;}
 }
 public static Rect canvas(int width,int height){return new Rect(8,59,width-16,Math.max(24,height-105));}
 public static Rect slot(int width,int height,int index){int size=30,gap=7,start=(width-(5*size+4*gap))/2;return new Rect(start+index*(size+gap),height-39,size,size);}
 private Rect area=new Rect(0,0,1,1);private double scale=1,offsetX,offsetY;private boolean initialized;
 public Rect area(){return area;} public double scale(){return scale;}
 public double screenX(double x){return area.x+offsetX+x*scale;} public double screenY(double y){return area.y+offsetY+y*scale;}
 public double worldX(double x){return (x-area.x-offsetX)/scale;} public double worldY(double y){return (y-area.y-offsetY)/scale;}
 public void resize(Rect next){if(initialized){double cx=worldX(area.x+area.width/2.0),cy=worldY(area.y+area.height/2.0);area=next;offsetX=area.width/2.0-cx*scale;offsetY=area.height/2.0-cy*scale;}else area=next;}
 public void open(ClassTreeLayout.Tree tree){if(!initialized){fit(tree);scale=Math.max(.65,scale);center(tree);initialized=true;}}
 public void reset(){initialized=false;}
 private void center(ClassTreeLayout.Tree tree){offsetX=(area.width-tree.width()*scale)/2;offsetY=(area.height-tree.height()*scale)/2;}
 public void fit(ClassTreeLayout.Tree tree){scale=Math.max(.08,Math.min(1.5,Math.min((area.width-16.0)/tree.width(),(area.height-16.0)/tree.height())));center(tree);initialized=true;}
 public void pan(double dx,double dy){offsetX+=dx;offsetY+=dy;}
 public void zoom(double px,double py,double amount){double x=worldX(px),y=worldY(py);scale=Math.clamp(scale*Math.pow(1.15,amount),.08,2.5);offsetX=px-area.x-x*scale;offsetY=py-area.y-y*scale;}
 public Rect node(ClassTreeLayout.Node node){return new Rect((int)Math.round(screenX(node.x())),(int)Math.round(screenY(node.y())),Math.max(3,(int)Math.round(ClassTreeLayout.SIZE*scale)),Math.max(3,(int)Math.round(ClassTreeLayout.SIZE*scale)));}
 public String hit(ClassTreeLayout.Tree tree,double x,double y){if(!area.contains(x,y))return "";for(var node:tree.nodes())if(!node.parent().isEmpty()&&node(node).contains(x,y))return node.id();return "";}
 /** Pick a corner that avoids the selected node and covers the fewest actual nodes. */
 public Rect card(ClassTreeLayout.Tree tree,String selected,int wantedWidth,int wantedHeight){
  int w=Math.min(wantedWidth,area.width-8),h=Math.min(wantedHeight,area.bottom()-12);
  for(var node:tree.nodes())if(node.id().equals(selected)){var focus=node(node);int free=Math.max(focus.x()-area.x-8,area.right()-focus.right()-8);if(free>=120)w=Math.min(w,free);}
  Rect best=null;int bestScore=Integer.MAX_VALUE;
  for(int y:new int[]{8,Math.max(8,area.bottom()-h-4)})for(int x:new int[]{area.x+4,area.right()-w-4}){var candidate=new Rect(x,y,w,h);int score=0;for(var node:tree.nodes())if(candidate.overlaps(node(node)))score+=node.id().equals(selected)?10000:1;if(score<bestScore){best=candidate;bestScore=score;}}
  return best;
 }
}
