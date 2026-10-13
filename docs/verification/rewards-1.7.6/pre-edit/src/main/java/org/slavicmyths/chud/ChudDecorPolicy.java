package org.slavicmyths.chud;

/** Stable three-block masonry patches and eight-block floor lights, independent of runtime. */
public final class ChudDecorPolicy {
    public static int patch(long seed,int x,int y,int z,int family) {
        long n=seed^Math.floorDiv(x,3)*341873128712L^Math.floorDiv(z,3)*132897987541L^Math.floorDiv(y,4)*42317861L^family*73428767L;
        n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;n=(n^(n>>>27))*0x94d049bb133111ebL;
        return Math.floorMod(n^(n>>>31),100);
    }
    public static int material(int patch,boolean ruined) {
        return patch<(ruined?38:64)?0:patch<(ruined?63:80)?1:patch<(ruined?85:94)?2:3;
    }
    public static boolean floorLight(int x,int z) { return Math.floorMod(x,8)==4&&Math.floorMod(z,8)==4; }
    private ChudDecorPolicy() {}
}
