package org.slavicmyths.desert.winds;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.InflaterInputStream;

/** Approved Python V2, with immutable block coordinates relative to the arena center. */
public final class FourWindsPlan {
    public static final int MIN_X=-23, MAX_X=23, MIN_Z=-65, MAX_Z=23;
    public static final int W=47, D=89, H=59, FLOOR=35, ENTRANCE_Z=-57, COUNT=13930;
    public static final int AIR=0, CHAIN=1, CUT=2, CYAN=3, PLANKS=4, GLASS=5,
        COPPER=6, GOLD=7, RED=8, STONE=9, LIGHT=10, SMOOTH=11, STAIR=12, LOG=13;
    public static final String SHA256="d4d42cc599f44777a1f509eb4bb4158f040876b15003ea8537e4721b2f3a631b";
    private static final byte[] DATA=load();
    private static byte[] load(){
        try(var raw=FourWindsPlan.class.getResourceAsStream("/assets/slavicmyths/structures/four_winds_v2.zlib")){
            if(raw==null)throw new IOException("Missing Four Winds V2 geometry");
            try(var in=new InflaterInputStream(raw)){
                byte[] data=in.readNBytes(W*D*H+1);
                if(data.length!=W*D*H||!SHA256.equals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data))))
                    throw new IOException("Four Winds V2 geometry checksum mismatch");
                int count=0;
                for(byte b:data){if(b<0||b>LOG)throw new IOException("Invalid Four Winds block palette");if(b!=AIR)count++;}
                if(count!=COUNT)throw new IOException("Invalid Four Winds V2 block count: "+count);
                return data;
            }
        }catch(IOException|NoSuchAlgorithmException e){throw new IllegalStateException("Cannot load Four Winds V2",e);}
    }
    public static int at(int x,int y,int z){
        if(x<MIN_X||x>MAX_X||z<MIN_Z||z>MAX_Z||y<0||y>=H)return AIR;
        return DATA[(y*D+z-MIN_Z)*W+x-MIN_X];
    }
    public static boolean landing(int x,int z){return at(x,0,z)!=AIR;}
    public static boolean encounter(double x,double y,double z){return x*x+z*z<=21*21&&y>=FLOOR+.5&&y<=FLOOR+18;}
    private FourWindsPlan(){}
}
