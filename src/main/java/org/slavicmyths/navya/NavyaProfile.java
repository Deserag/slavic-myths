package org.slavicmyths.navya;

/** Continuous bounded geology, sampled identically by worldgen and offline QA. */
public final class NavyaProfile {
    public record Column(int lower,int middle,int upper,double pillar) { }
    private static long mix(long x){x=(x^(x>>>30))*0xbf58476d1ce4e5b9L;x=(x^(x>>>27))*0x94d049bb133111ebL;return x^(x>>>31);}
    private static double value(long seed,int x,int z){return (mix(seed+x*341873128712L+z*132897987541L)>>>11)*0x1.0p-52-1;}
    private static double smooth(double x){return x*x*(3-2*x);}
    public static double noise(long seed,int x,int z,int scale){
        int gx=Math.floorDiv(x,scale),gz=Math.floorDiv(z,scale);double fx=smooth(Math.floorMod(x,scale)/(double)scale),fz=smooth(Math.floorMod(z,scale)/(double)scale);
        double a=value(seed,gx,gz)*(1-fx)+value(seed,gx+1,gz)*fx,b=value(seed,gx,gz+1)*(1-fx)+value(seed,gx+1,gz+1)*fx;return a*(1-fz)+b*fz;
    }
    public static Column at(long seed,int x,int z){
        int lower=(int)Math.round(39+6*noise(seed+17,x,z,96));
        double raw=102+12*noise(seed+31,x,z,128)+6*noise(seed+43,x,z,48);
        // Local valleys join the middle shelf to the cavern floor instead of
        // sealing the lower zone under a continuous, unreachable stone plate.
        int middle=(int)Math.round(Math.max(lower+1,raw-30+5*noise(seed+59,x,z,80)-Math.max(0,noise(seed+71,x,z,64)-.1)*45));
        int upper=(int)Math.round(Math.max(middle+3,raw-Math.max(0,noise(seed+89,x,z,80)+.1)*35));
        return new Column(lower,middle,upper,noise(seed+101,x,z,48));
    }
    public static boolean solid(Column column,int y){
        return y<=column.lower || y>=column.upper-6 && y<=column.upper || y>=column.middle-4 && y<=column.middle
                || y<=column.upper && column.pillar>.61+.06*Math.sin(y*.08);
    }
    private NavyaProfile() { }
}
