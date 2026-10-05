package org.slavicmyths.navigation;

import java.util.UUID;

public final class NavigationStatistics {
    public record Statistics(int samples,int minRadius,double meanRadius,int maxRadius,
        double minFraction,double meanFraction,double maxFraction,int inner25,int middle25to50,int outer50to85,int edge85to95,int outside){}
    public static Statistics run(int samples){
        int min=10000,max=0,inner=0,middle=0,outer=0,edge=0,outside=0;double sum=0,minF=1,maxF=0,sumF=0;
        for(int i=0;i<samples;i++){
            double x=(i%51-25)*1500+.5,z=(i%37-18)*1300+.5;
            var area=SearchArea.generate(x,z,SearchArea.Scale.values()[i%4],new UUID(i*312987L, i*898127L),0x71acf8394412L,0,(a,b,r)->true);
            double f=area.distance(x,z)/area.radius();min=Math.min(min,area.radius());max=Math.max(max,area.radius());sum+=area.radius();minF=Math.min(minF,f);maxF=Math.max(maxF,f);sumF+=f;
            if(!area.contains(x,z))outside++;if(f<.25)inner++;else if(f<.5)middle++;else if(f<.85)outer++;else if(f<=.95)edge++;
        }
        return new Statistics(samples,min,sum/samples,max,minF,sumF/samples,maxF,inner,middle,outer,edge,outside);
    }
    private NavigationStatistics(){}
}
