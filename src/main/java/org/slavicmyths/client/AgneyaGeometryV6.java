package org.slavicmyths.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** Approved Agneya V6 native Minecraft cuboid source, 16 units per block, Y up. */
public final class AgneyaGeometryV6 {
    public record Box(String part,float x,float y,float z,float w,float h,float d,String material) {}
    private static final List<Box> BOXES=List.of(
        new Box("left_leg",-4.500F,5.000F,-2.600F,3.500F,18.000F,5.000F,"dark_cloth"),
        new Box("left_leg",-4.850F,0.000F,-4.000F,4.200F,7.000F,7.500F,"iron_dark"),
        new Box("left_leg",-4.650F,1.100F,-4.400F,3.800F,2.300F,7.800F,"iron"),
        new Box("right_leg",1.000F,5.000F,-2.600F,3.500F,18.000F,5.000F,"dark_cloth"),
        new Box("right_leg",0.650F,0.000F,-4.000F,4.200F,7.000F,7.500F,"iron_dark"),
        new Box("right_leg",0.850F,1.100F,-4.400F,3.800F,2.300F,7.800F,"iron"),
        new Box("dress",-10.500F,7.000F,-8.600F,21.000F,5.500F,17.200F,"dress_hem"),
        new Box("dress",-7.140F,7.300F,-8.900F,2.100F,4.500F,0.450F,"dress_light"),
        new Box("dress",5.040F,7.300F,-8.900F,2.100F,4.500F,0.450F,"dress_light"),
        new Box("dress",-9.700F,12.000F,-8.000F,19.400F,5.500F,16.000F,"dress_hem"),
        new Box("dress",-6.596F,12.300F,-8.300F,2.100F,4.500F,0.450F,"dress_light"),
        new Box("dress",4.496F,12.300F,-8.300F,2.100F,4.500F,0.450F,"dress_light"),
        new Box("dress",-8.500F,17.000F,-7.200F,17.000F,5.500F,14.400F,"dress"),
        new Box("dress",-5.780F,17.300F,-7.500F,2.100F,4.500F,0.450F,"dress_light"),
        new Box("dress",3.680F,17.300F,-7.500F,2.100F,4.500F,0.450F,"dress_light"),
        new Box("dress",-7.250F,22.000F,-6.150F,14.500F,5.500F,12.300F,"dress_dark"),
        new Box("dress",-6.000F,27.000F,-5.100F,12.000F,5.500F,10.200F,"dress"),
        new Box("dress",-4.750F,32.000F,-4.100F,9.500F,5.500F,8.200F,"dress_dark"),
        new Box("torso",-4.350F,36.000F,-3.900F,8.700F,12.600F,7.800F,"dress_dark"),
        new Box("torso",-4.550F,36.500F,-4.150F,9.100F,2.000F,8.300F,"leather"),
        new Box("torso",-4.500F,44.000F,-4.150F,9.000F,6.100F,8.200F,"dress"),
        new Box("torso",-5.700F,49.000F,2.900F,11.400F,9.800F,1.300F,"dress_dark"),
        new Box("torso",-5.650F,49.300F,-3.650F,11.300F,6.000F,7.300F,"dress"),
        new Box("torso",-5.700F,55.100F,-3.000F,11.400F,3.750F,6.500F,"skin"),
        new Box("torso",-4.850F,58.200F,-2.600F,9.700F,2.300F,5.950F,"skin_light"),
        new Box("torso",-5.000F,54.550F,-6.450F,4.450F,4.300F,3.600F,"skin_light"),
        new Box("torso",0.550F,54.550F,-6.450F,4.450F,4.300F,3.600F,"skin_light"),
        new Box("torso",-4.900F,52.150F,-6.650F,4.350F,3.350F,3.900F,"dress_light"),
        new Box("torso",0.550F,52.150F,-6.650F,4.350F,3.350F,3.900F,"dress_light"),
        new Box("torso",-0.900F,51.400F,-4.830F,1.800F,4.000F,2.100F,"dress"),
        new Box("torso",-2.050F,59.400F,-1.800F,4.100F,4.600F,3.550F,"skin_light"),
        new Box("torso",-2.200F,62.300F,-2.150F,4.400F,1.300F,4.000F,"skin"),
        new Box("torso",-4.650F,37.600F,-4.350F,9.300F,1.750F,8.700F,"leather"),
        new Box("torso",-0.950F,37.750F,-4.590F,1.900F,1.430F,0.480F,"gold"),
        new Box("left_arm",-9.600F,54.150F,-2.800F,4.700F,6.650F,5.300F,"skin_light"),
        new Box("left_arm",-9.350F,47.200F,-2.600F,4.200F,7.600F,4.950F,"skin"),
        new Box("left_arm",-8.950F,48.600F,-3.150F,2.550F,4.150F,0.850F,"skin_light"),
        new Box("left_forearm",-9.100F,43.000F,-2.480F,3.900F,5.200F,4.700F,"skin_shadow"),
        new Box("left_forearm",-9.000F,39.100F,-2.800F,3.750F,5.350F,4.950F,"skin"),
        new Box("left_forearm",-8.900F,41.100F,-3.130F,2.350F,2.700F,0.700F,"skin_light"),
        new Box("left_forearm",-9.160F,38.400F,-3.100F,4.050F,1.550F,5.200F,"leather"),
        new Box("left_hand",-9.050F,35.000F,-3.000F,3.800F,4.000F,4.900F,"skin_light"),
        new Box("right_arm",5.450F,54.150F,-2.800F,4.700F,6.650F,5.300F,"skin_light"),
        new Box("right_arm",5.700F,47.200F,-2.600F,4.200F,7.600F,4.950F,"skin"),
        new Box("right_arm",6.100F,48.600F,-3.150F,2.550F,4.150F,0.850F,"skin_light"),
        new Box("right_forearm",5.950F,43.000F,-2.480F,3.900F,5.200F,4.700F,"skin_shadow"),
        new Box("right_forearm",6.050F,39.100F,-2.800F,3.750F,5.350F,4.950F,"skin"),
        new Box("right_forearm",6.150F,41.100F,-3.130F,2.350F,2.700F,0.700F,"skin_light"),
        new Box("right_forearm",5.890F,38.400F,-3.100F,4.050F,1.550F,5.200F,"leather"),
        new Box("right_hand",6.000F,35.000F,-3.000F,3.800F,4.000F,4.900F,"skin_light"),
        new Box("head",-3.930F,64.100F,-3.900F,7.860F,8.300F,7.550F,"skin"),
        new Box("head",-4.550F,65.800F,-3.500F,0.920F,5.300F,6.750F,"skin"),
        new Box("head",3.630F,65.800F,-3.500F,0.920F,5.300F,6.750F,"skin"),
        new Box("head",-3.750F,65.200F,-4.900F,7.500F,5.700F,1.050F,"skin_light"),
        new Box("head",-3.220F,63.480F,-4.500F,6.440F,2.300F,6.050F,"skin_light"),
        new Box("head",-2.750F,70.800F,-4.450F,5.500F,1.550F,5.750F,"skin_light"),
        new Box("head",-3.060F,67.350F,-5.090F,2.300F,1.330F,0.120F,"eye_dark"),
        new Box("head",-2.960F,67.500F,-5.220F,2.060F,1.030F,0.110F,"eye_amber"),
        new Box("head",-1.860F,67.520F,-5.320F,0.610F,0.990F,0.080F,"eye_dark"),
        new Box("head",-2.740F,68.200F,-5.350F,0.340F,0.260F,0.075F,"eye_glint"),
        new Box("head",-3.080F,69.100F,-5.050F,2.300F,0.350F,0.150F,"hair_dark"),
        new Box("head",0.840F,67.350F,-5.090F,2.300F,1.330F,0.120F,"eye_dark"),
        new Box("head",0.940F,67.500F,-5.220F,2.060F,1.030F,0.110F,"eye_amber"),
        new Box("head",1.250F,67.520F,-5.320F,0.610F,0.990F,0.080F,"eye_dark"),
        new Box("head",1.160F,68.200F,-5.350F,0.340F,0.260F,0.075F,"eye_glint"),
        new Box("head",0.820F,69.100F,-5.050F,2.300F,0.350F,0.150F,"hair_dark"),
        new Box("head",-0.260F,65.840F,-5.130F,0.520F,0.550F,0.120F,"skin_shadow"),
        new Box("head",-0.720F,64.800F,-5.100F,1.440F,0.300F,0.120F,"lip"),
        new Box("head",-5.150F,72.100F,-3.250F,10.300F,2.500F,7.000F,"hair_dark"),
        new Box("head",-5.480F,70.800F,-2.900F,10.960F,1.650F,7.650F,"hair_mid"),
        new Box("head",-5.300F,62.750F,3.400F,1.900F,8.550F,1.950F,"hair_dark"),
        new Box("head",-3.150F,63.300F,3.400F,1.900F,8.000F,1.950F,"hair_mid"),
        new Box("head",-1.000F,62.750F,3.400F,1.900F,8.550F,1.950F,"hair_dark"),
        new Box("head",1.150F,63.300F,3.400F,1.900F,8.000F,1.950F,"hair_mid"),
        new Box("head",3.300F,62.750F,3.400F,1.900F,8.550F,1.950F,"hair_dark"),
        new Box("head",-5.680F,63.400F,-3.850F,1.420F,8.550F,5.850F,"hair_mid"),
        new Box("head",4.260F,63.400F,-3.850F,1.420F,8.550F,5.850F,"hair_mid"),
        new Box("head",-5.680F,64.200F,-4.300F,1.060F,5.400F,1.150F,"hair_light"),
        new Box("head",4.620F,64.200F,-4.300F,1.060F,5.400F,1.150F,"hair_light"),
        new Box("head",-3.850F,70.650F,-4.720F,3.100F,1.400F,0.950F,"hair_mid"),
        new Box("head",0.850F,70.650F,-4.720F,3.000F,1.400F,0.950F,"hair_mid"),
        new Box("head",-4.550F,74.150F,-4.250F,9.100F,2.800F,7.100F,"bone"),
        new Box("head",-3.650F,76.300F,-3.500F,7.300F,1.050F,5.800F,"bone_light"),
        new Box("head",-4.350F,72.650F,-5.950F,1.550F,2.500F,3.550F,"bone"),
        new Box("head",2.800F,72.650F,-5.950F,1.550F,2.500F,3.550F,"bone"),
        new Box("head",-2.800F,74.250F,-5.920F,2.100F,0.680F,1.650F,"bone_light"),
        new Box("head",0.700F,74.250F,-5.920F,2.100F,0.680F,1.650F,"bone_light"),
        new Box("head",-2.850F,72.750F,-5.780F,2.150F,1.450F,0.350F,"socket"),
        new Box("head",0.700F,72.750F,-5.780F,2.150F,1.450F,0.350F,"socket"),
        new Box("head",-2.100F,72.300F,-7.050F,4.200F,2.000F,3.600F,"bone"),
        new Box("head",-1.550F,71.850F,-9.650F,3.100F,1.680F,3.500F,"bone_shadow"),
        new Box("head",-0.880F,72.500F,-9.880F,1.760F,0.620F,0.340F,"socket"),
        new Box("head",-1.550F,70.860F,-7.320F,0.620F,1.100F,0.920F,"bone_light"),
        new Box("head",0.930F,70.860F,-7.320F,0.620F,1.100F,0.920F,"bone_light"),
        new Box("head",-4.050F,76.050F,0.050F,1.950F,2.300F,2.250F,"bone"),
        new Box("head",-3.700F,78.200F,0.350F,1.500F,1.650F,1.550F,"bone_light"),
        new Box("head",2.100F,76.050F,0.050F,1.950F,2.300F,2.250F,"bone"),
        new Box("head",2.200F,78.200F,0.350F,1.500F,1.650F,1.550F,"bone_light"),
        new Box("mace",7.050F,35.700F,-1.600F,1.850F,25.400F,2.100F,"iron_dark"),
        new Box("mace",7.010F,37.000F,-1.720F,1.940F,5.000F,2.300F,"leather"),
        new Box("mace",6.250F,59.000F,-4.250F,5.650F,6.450F,7.450F,"iron"),
        new Box("mace",6.850F,59.700F,-4.520F,4.400F,5.050F,0.680F,"iron_light"),
        new Box("mace",7.420F,65.000F,-1.650F,3.180F,3.500F,2.650F,"iron_light"),
        new Box("mace",7.150F,56.600F,-1.600F,3.700F,2.900F,2.500F,"iron_shadow"),
        new Box("mace",3.800F,61.200F,-1.900F,2.750F,2.000F,2.650F,"iron_light"),
        new Box("mace",11.550F,61.200F,-1.900F,2.750F,2.000F,2.650F,"iron_light"),
        new Box("mace",7.000F,61.600F,-6.600F,4.250F,2.000F,2.600F,"iron_light"),
        new Box("mace",7.000F,61.600F,2.850F,4.250F,2.000F,2.250F,"iron_light"),
        new Box("shield",-17.400F,34.200F,-7.550F,12.000F,21.000F,3.450F,"iron_dark"),
        new Box("shield",-16.750F,35.050F,-8.300F,10.700F,19.300F,1.450F,"iron"),
        new Box("shield",-15.850F,36.150F,-8.630F,8.900F,17.300F,0.690F,"dress_dark"),
        new Box("shield",-12.550F,36.050F,-9.120F,2.150F,17.600F,0.820F,"gold"),
        new Box("shield",-15.800F,43.600F,-9.180F,8.750F,2.100F,0.860F,"gold"),
        new Box("shield",-13.150F,42.200F,-9.880F,3.400F,4.800F,0.950F,"gold_light"),
        new Box("shield",-16.950F,35.100F,-8.500F,1.550F,18.600F,0.720F,"iron_light"),
        new Box("shield",-6.800F,35.100F,-8.500F,1.450F,18.600F,0.720F,"iron_light"),
        new Box("shield",-11.750F,38.600F,-5.550F,4.300F,3.100F,4.300F,"leather"),
        new Box("shield",-11.750F,45.100F,-5.550F,4.300F,2.900F,4.300F,"leather"),
        new Box("shield",-9.450F,39.100F,-3.350F,3.500F,2.100F,2.400F,"iron_shadow"),
        new Box("shield",-9.450F,45.400F,-3.350F,3.500F,1.950F,2.400F,"iron_shadow")
    );
    public static List<Box> build(){return BOXES;}
    public static String json(){
        StringBuilder out=new StringBuilder("{\"model\":\"AgneyaV6\",\"cuboids\":[");
        for(int i=0;i<BOXES.size();i++){
            Box b=BOXES.get(i);
            if(i>0)out.append(',');
            out.append(String.format(Locale.ROOT,
                "{\"part\":\"%s\",\"x\":%.3f,\"y\":%.3f,\"z\":%.3f,\"w\":%.3f,\"h\":%.3f,\"d\":%.3f,\"material\":\"%s\"}",
                b.part(),b.x(),b.y(),b.z(),b.w(),b.h(),b.d(),b.material()));
        }
        return out.append("]}").toString();
    }
    public static void main(String[] args)throws Exception {
        Path dest=Path.of(args.length==0?"agneya_v6_geometry.json":args[0]);
        Files.writeString(dest,json(),StandardCharsets.UTF_8);
        System.out.println("Exported "+BOXES.size()+" Agneya V6 cuboids: "+dest);
    }
    private AgneyaGeometryV6(){}
}
