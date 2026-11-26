package org.slavicmyths.desert.oasis;
import java.io.*;
import java.util.zip.GZIPInputStream;
public final class LostOasisPlan {
    public static final int W=105,D=117,H=47,CX=52,CZ=57,FLOOR=3,LOJA_FLOOR=27,VERSION=1;
    public static final int AIR=0,SAND=1,STONE=2,LIGHT=3,OLD=4,DARK=5,BRICK=6,GOLD=7,RED=8,PURPLE=9,TEAL=10,WATER=11,WATER2=12,SOIL=13,LEAF=14,LEAF2=15,WOOD=16,WOOD2=17,FIRE=18,FLAME=19,LANTERN=20,IRON=21,CLOTH=22,BOOK=23,WHITE=24,STAIR_S=25,STAIR_N=26,STAIR_E=27,STAIR_W=28,CHEST=29,BED_FOOT=30,BED_HEAD=31,COUNT=32;
    public static final LostOasisPlan INSTANCE=new LostOasisPlan();
    private final byte[] blocks;
    private final boolean[] active=new boolean[W*D];
    private LostOasisPlan(){
        var raw=LostOasisPlan.class.getResourceAsStream("/assets/slavicmyths/lost_oasis/plan_v1.bin.gz");
        if(raw==null)throw new IllegalStateException("Missing Lost Oasis V1 plan");
        try(var in=new DataInputStream(new GZIPInputStream(raw))){
            if(in.readInt()!=0x4f415331||in.readInt()!=W||in.readInt()!=D||in.readInt()!=H||in.readInt()!=COUNT)throw new IOException("Oasis plan header");
            blocks=in.readNBytes(W*D*H);
            if(blocks.length!=W*D*H||in.read()!=-1)throw new IOException("Oasis plan size");
            for(int i=0;i<blocks.length;i++){if(Byte.toUnsignedInt(blocks[i])>=COUNT)throw new IOException("Oasis material");if(blocks[i]!=0)active[i/H]=true;}
        }catch(IOException e){throw new IllegalStateException("Invalid Lost Oasis V1 plan",e);}
    }
    public int at(int x,int y,int z){return x<0||x>=W||z<0||z>=D||y<0||y>=H?AIR:Byte.toUnsignedInt(blocks[(x*D+z)*H+y]);}
    public boolean active(int x,int z){return x>=0&&x<W&&z>=0&&z<D&&active[x*D+z];}
    public static double radius(double x,double z){return Math.hypot(x-CX,(z-CZ)/1.08);}
    public static boolean arena(int x,int y,int z){return y>=FLOOR+1&&y<=FLOOR+16&&radius(x,z)<19;}
    public static boolean encounter(int x,int y,int z){return y>=FLOOR-1&&y<=FLOOR+20&&radius(x,z)<35;}
    public static boolean visitor(int x,int y,int z){return y>=FLOOR&&y<=H&&radius(x,z)<44;}
}
