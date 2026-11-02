package org.slavicmyths.posad;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/** A+ residence: production geometry is loaded ONLY from the approved proposal and overlay. */
public final class PosadnikTerem {
    public static final int MIN_X=60, MAX_X=72, MIN_Z=78, MAX_Z=95, HEIGHT=18, BLOCK_COUNT=1337;
    private static final Map<Long,BlockState> VOXELS=load();
    private static long key(int x,int y,int z){return ((long)x<<32)|((long)y<<16)|z;}

    private static JsonObject json(String filename){
        String path="/data/slavicmyths/posad/"+filename;
        try(InputStream stream=PosadnikTerem.class.getResourceAsStream(path)){
            if(stream==null)throw new IllegalStateException("Missing Posadnik resource "+path);
            return JsonParser.parseString(new String(stream.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject();
        }catch(IOException failure){throw new IllegalStateException("Unreadable Posadnik resource "+path,failure);}
    }
    private static <T extends Comparable<T>> BlockState apply(BlockState state,Property<T> prop,String value){
        return state.setValue(prop,prop.getValue(value).orElseThrow(()->new IllegalArgumentException("Unknown Posadnik value "+prop.getName()+"="+value)));
    }
    private static BlockState parse(String raw){
        int bracket=raw.indexOf('[');
        String id=bracket<0?raw:raw.substring(0,bracket);
        var block=BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id)).orElseThrow(()->new IllegalStateException("Unregistered Posadnik block "+id));
        BlockState result=block.defaultBlockState();
        if(bracket<0)return result;
        if(!raw.endsWith("]"))throw new IllegalArgumentException("Invalid Posadnik blockstate "+raw);
        for(String property:raw.substring(bracket+1,raw.length()-1).split(",")){
            String[] pair=property.split("=",2);
            if(pair.length!=2)throw new IllegalArgumentException("Invalid Posadnik property "+raw);
            Property<?> prop=block.getStateDefinition().getProperty(pair[0]);
            if(prop==null)throw new IllegalArgumentException("Unknown Posadnik property "+pair[0]+" for "+id);
            result=apply(result,prop,pair[1]);
        }
        return result;
    }
    private static BlockState[] palette(JsonArray array){
        BlockState[] states=new BlockState[array.size()];
        for(int i=0;i<array.size();i++)states[i]=parse(array.get(i).getAsString());
        return states;
    }
    private static int[] position(JsonArray value,BlockState[] palette){
        if(value.size()!=4)throw new IllegalArgumentException("Malformed Posadnik voxel");
        int x=value.get(0).getAsInt(),y=value.get(1).getAsInt(),z=value.get(2).getAsInt(),id=value.get(3).getAsInt();
        if(x<MIN_X||x>MAX_X||z<MIN_Z||z>MAX_Z||y<0||y>=HEIGHT||id<0||id>=palette.length)throw new IllegalArgumentException("Posadnik voxel out of bounds "+value);
        return new int[]{x,y,z,id};
    }
    private static Map<Long,BlockState> load(){
        JsonObject base=json("posadnik_terem_A_proposal.json");
        JsonObject delta=json("posadnik_terem_Aplus_delta.json");
        if(base.get("block_count").getAsInt()!=1259||delta.get("baseline_blocks").getAsInt()!=1259||delta.get("expected_merged_blocks").getAsInt()!=BLOCK_COUNT)throw new IllegalStateException("Unexpected Posadnik design revision");
        JsonArray rows=base.getAsJsonArray("blocks"),edits=delta.getAsJsonArray("placements");
        if(rows.size()!=1259||edits.size()!=80)throw new IllegalStateException("Posadnik source count drift");
        BlockState[] first=palette(base.getAsJsonArray("palette")),second=palette(delta.getAsJsonArray("palette"));
        Map<Long,BlockState> result=new HashMap<>();
        for(var elem:rows){int[] q=position(elem.getAsJsonArray(),first);if(result.putIfAbsent(key(q[0],q[1],q[2]),first[q[3]])!=null)throw new IllegalStateException("Duplicate baseline voxel");}
        int replaced=0;
        HashSet<Long> touched=new HashSet<>();
        for(var elem:edits){
            int[] q=position(elem.getAsJsonArray(),second);long k=key(q[0],q[1],q[2]);
            if(!touched.add(k))throw new IllegalStateException("Duplicate Posadnik delta voxel");
            if(result.put(k,second[q[3]])!=null)replaced++;
        }
        if(replaced!=2||result.size()!=BLOCK_COUNT)throw new IllegalStateException("Posadnik expected 1337 blocks and 2 replacements");
        return Map.copyOf(result);
    }
    public static BlockState voxel(int x,int y,int z){
        if(x<MIN_X||x>MAX_X||y<0||y>=HEIGHT||z<MIN_Z||z>MAX_Z)return null;
        return VOXELS.get(key(x,y,z));
    }
    public static int count(){return VOXELS.size();}
    private PosadnikTerem(){}
}
