package org.slavicmyths.chud;

import java.util.*;

/** Integer cell policy, independent of the game runtime. Retains the original site's priority. */
public final class ChudSitePolicy {
    public record Site(int x,int z) {}
    public static List<Site> candidates(int chunkX,int chunkZ,int originalX,int originalZ) {
        int x=Math.floorDiv(chunkX,256)*256,z=Math.floorDiv(chunkZ,256)*256;
        var sites=new LinkedHashSet<Site>();sites.add(new Site(originalX,originalZ));
        for(int dx:new int[]{0,47,95})for(int dz:new int[]{0,47,95})sites.add(new Site(x+dx,z+dz));
        return List.copyOf(sites);
    }
    private ChudSitePolicy() {}
}
