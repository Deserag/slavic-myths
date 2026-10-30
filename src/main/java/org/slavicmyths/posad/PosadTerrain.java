package org.slavicmyths.posad;

import java.util.ArrayList;
import java.util.List;

/** Bounded, world-independent terrain contract shared by native and explicit placement. */
public final class PosadTerrain {
    public static final int MAX_SUPPORT_DEPTH=48, MAX_BRIDGE=48, SUPPORT_STEP=8;
    public enum Material { AIR, NATURAL, LAVA, PROTECTED }
    public interface Columns { Material at(int worldX,int worldY,int worldZ); }
    public record Support(int x,int z,int depth) {}
    public record Plan(int anchorX,int deckY,int anchorZ,int turns,int bridgeLength,List<Support> supports) {
        public Plan { supports=List.copyOf(supports); }
        public PosadGeometry.Cell world(int x,int z) {
            // The same quarter-turn transform extends outside CITY for the shore approach.
            return switch(Math.floorMod(turns,4)) {
                case 0 -> new PosadGeometry.Cell(anchorX+x,anchorZ+z);
                case 1 -> new PosadGeometry.Cell(anchorX+112-z,anchorZ+x);
                case 2 -> new PosadGeometry.Cell(anchorX+112-x,anchorZ+112-z);
                default -> new PosadGeometry.Cell(anchorX+z,anchorZ+112-x);
            };
        }
    }
    public record Decision(Plan plan,String reason,int inspected) { public boolean accepted(){return plan!=null;} }
    private static final class Probe {
        final Columns columns; int reads;
        Probe(Columns columns){this.columns=columns;}
        Material get(int x,int y,int z){reads++;return columns.at(x,y,z);}
        boolean clear(int x,int y,int z,int height) {
            for(int dy=1;dy<=height;dy++)if(get(x,y+dy,z)!=Material.AIR)return false;
            return true;
        }
        int footing(int x,int y,int z) {
            for(int d=3;d<=MAX_SUPPORT_DEPTH;d++) {
                var m=get(x,y-d,z);
                if(m==Material.NATURAL)return d;
                if(m==Material.PROTECTED)return -1;
            }
            return -1;
        }
    }
    /** Full footprint inspection, never a sparse sample advertised as full safety. No writes/loads. */
    public static Decision inspect(Columns columns,int ax,int deck,int az,int firstTurns) {
        var p=new Probe(columns);
        for(int z=0;z<113;z++)for(int x=0;x<113;x++) {
            if(!p.clear(ax+x,deck,az+z,17))return new Decision(null,"HEADROOM",p.reads);
            for(int dy=0;dy>=-2;dy--) {
                var m=p.get(ax+x,deck+dy,az+z);
                if(m==Material.PROTECTED)return new Decision(null,"PROTECTED_PLATFORM",p.reads);
            }
        }
        var baseSupports=new ArrayList<Support>();
        for(int z=0;z<113;z+=SUPPORT_STEP)for(int x=0;x<113;x+=SUPPORT_STEP) {
            int d=p.footing(ax+x,deck,az+z);
            if(d<0)return new Decision(null,"NO_FOOTING",p.reads);
            baseSupports.add(new Support(x,z,d));
        }
        // Choose the shortest verified bank, seeded orientation only breaks equal-length ties.
        for(int length=1;length<=MAX_BRIDGE;length++)for(int r=0;r<4;r++) {
            int turns=Math.floorMod(firstTurns+r,4);
            var transform=new Plan(ax,deck,az,turns,length,List.of());
            boolean bank=true;
            for(int lane=53;lane<=59;lane++) {
                var pos=transform.world(lane,112+length);
                if(p.get(pos.x(),deck-1,pos.z())!=Material.NATURAL||!p.clear(pos.x(),deck,pos.z(),3)) {bank=false;break;}
            }
            if(!bank)continue;
            var supports=new ArrayList<Support>();
            // Rotate the foundation support coordinates back into this plan's local frame.
            for(var s:baseSupports) {
                var local=PosadGeometry.rotate(new PosadGeometry.Cell(s.x(),s.z()),-turns);
                supports.add(new Support(local.x(),local.z(),s.depth()));
            }
            boolean valid=true;
            for(int t=1;t<=length&&valid;t++)for(int lane=52;lane<=60;lane++) {
                var pos=transform.world(lane,112+t);
                if(!p.clear(pos.x(),deck,pos.z(),3)){valid=false;break;}
                for(int dy=0;dy>=-2;dy--)if(p.get(pos.x(),deck+dy,pos.z())==Material.PROTECTED){valid=false;break;}
                if(!valid)break;
                if((t==length||t%SUPPORT_STEP==0)&&(lane==52||lane==60)) {
                    int d=p.footing(pos.x(),deck,pos.z());
                    if(d<0){valid=false;break;}
                    supports.add(new Support(lane,112+t,d));
                }
            }
            if(valid)return new Decision(new Plan(ax,deck,az,turns,length,supports),"ACCEPTED",p.reads);
        }
        return new Decision(null,"NO_REACHABLE_SHORE",p.reads);
    }
    private PosadTerrain() {}
}
