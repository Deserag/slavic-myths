package org.slavicmyths.navigation;

import java.util.Random;
import java.util.UUID;

/** Public approximate geometry only. No target position, server seed or private salt. */
public record SearchArea(double centerX,double centerZ,int radius,State state) {
    public enum State { ASSIGNED, APPROACHING, INSIDE_SEARCH_AREA, TARGET_DISCOVERED, COMPLETED, EXPIRED }
    public enum Scale {
        MINI(180,300), RARE(240,380), WORLD(320,550), UNIQUE(450,700);
        final int min,max;
        Scale(int min,int max){this.min=min;this.max=max;}
    }
    public SearchArea {
        if(!Double.isFinite(centerX)||!Double.isFinite(centerZ)||Math.abs(centerX)>30_001_000||
           Math.abs(centerZ)>30_001_000||radius<180||radius>700||state==null)
            throw new IllegalArgumentException("Invalid public search geometry");
    }
    public double distance(double x,double z){return Math.hypot(x-centerX,z-centerZ);}
    public boolean contains(double x,double z){return distance(x,z)<=radius;}
    public double boundaryDistance(double x,double z){return Math.max(0,distance(x,z)-radius);}
    public SearchArea withState(State next){return new SearchArea(centerX,centerZ,radius,next);}
    @FunctionalInterface public interface Quality {
        /** Noise/biome sampling only; must not load chunks. */
        boolean plausible(double x,double z,int radius);
    }
    /** Server only call site: bounded candidates, no centered fallback on failure. */
    public static SearchArea generate(double targetX,double targetZ,Scale scale,UUID instance,
                                      long privateSalt,int revision,Quality quality) {
        long seed=privateSalt^instance.getMostSignificantBits()^Long.rotateLeft(instance.getLeastSignificantBits(),27)
            ^(revision*0x9e3779b97f4a7c15L);
        Random random=new Random(seed);
        int radius=scale.min+random.nextInt(scale.max-scale.min+1);
        for(int attempt=0;attempt<32;attempt++){
            double bucket=random.nextDouble();
            double fraction=bucket<.10?.30+random.nextDouble()*.15:
                bucket<.85?.50+random.nextDouble()*.35:.85+random.nextDouble()*.10;
            double angle=random.nextDouble()*Math.PI*2;
            double x=targetX-Math.cos(angle)*fraction*radius,z=targetZ-Math.sin(angle)*fraction*radius;
            if(Math.abs(x)>29_999_984||Math.abs(z)>29_999_984)continue;
            SearchArea area=new SearchArea(x,z,radius,State.ASSIGNED);
            if(area.contains(targetX,targetZ)&&quality.plausible(x,z,radius))return area;
        }
        return null; // Real terrain can reject every candidate: never leak exact anchor as fallback.
    }
}
