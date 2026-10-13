package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Immutable generation-v1 plan. Coordinates are local; every saved start retains its inputs. */
public final class ChudPlan {
    public static final int GENERATION_VERSION = 1;
    public enum Role { SQUARE, RESIDENTIAL, FORGE, STORE, COUNCIL, GATE, ENTRANCE }
    public record Hall(String id, Role role, int x, int y, int z, int width, int depth, int height, boolean major) {
        public BlockPos point() { return new BlockPos(x, y + 1, z - depth / 2 + 7); }
    }
    public record Room(String id, int variant, int x, int y, int z, int width, int depth) {}
    public record Connector(String from, String to, List<BlockPos> floors, int width, int headroom) {}
    public record MineGate(String id,BlockPos floor,Direction facing,int width,int height,int depth) {}
    public final long seed;
    public final BlockPos anchor;
    public final int variant, rotation, entranceFloor, entranceBend;
    public final List<Hall> halls;
    public final List<Room> homes;
    public final List<Connector> connectors;
    public final Map<Role, BlockPos> points;

    public ChudPlan(long seed, BlockPos anchor, int variant, int rotation, int entranceFloor) {
        this(seed,anchor,variant,rotation,entranceFloor,1);
    }
    public ChudPlan(long seed, BlockPos anchor, int variant, int rotation, int entranceFloor,int entranceBend) {
        if (variant < 0 || variant > 2 || rotation < 0 || rotation > 3) throw new IllegalArgumentException("plan variant/rotation");
        this.seed = seed; this.anchor = anchor.immutable(); this.variant = variant; this.rotation = rotation; this.entranceFloor = entranceFloor;
        if(Math.abs(entranceBend)!=1)throw new IllegalArgumentException("entrance bend");this.entranceBend=entranceBend;
        var hs = new ArrayList<Hall>();
        hs.add(new Hall("square", Role.SQUARE, 0, 0, 0, variant==0?50:58, variant==0?46:54, variant==0?28:34, true));
        hs.add(new Hall("residential", Role.RESIDENTIAL, 76, 14, 38, variant==0?58:64, variant==0?50:54, variant==0?35:42, true));
        hs.add(new Hall("forge", Role.FORGE, -72, -14, 36, variant==0?58:60, 54, variant==0?28:34, true));
        hs.add(variant == 0 ? new Hall("store", Role.STORE, -72, -14, 79, 26, 30, 18, false)
                : new Hall("store", Role.STORE, -66, -24, -55, 46, 42, 24, true));
        hs.add(variant < 2 ? new Hall("council", Role.COUNCIL, 0, 0, 47, 24, 28, 14, false)
                : new Hall("council", Role.COUNCIL, 74, 14, -58, 46, 40, 29, true));
        hs.add(new Hall("gate", Role.GATE, -109, -40, -100, 28, 30, 34, false));
        halls = List.copyOf(hs);
        var rs = new ArrayList<Room>();
        int tiers = variant == 0 ? 2 : 3;
        // Separate circulation bands at z=-1,+11,+23. Roofs never intersect the next floor.
        for (int tier = 0; tier < tiers; tier++) for (int row = 0; row < 2; row++) for (int col = 0; col < (variant == 2 ? 3 : 2); col++) {
            int v = Math.floorMod(col + row * 2 + tier + (int)seed, 4);
            if(v==2&&tier<tiers-1)v=0; // Two-storey family occupies the top terrace, with room for its roof.
            rs.add(new Room("home_" + rs.size(), v, 76 - 23 + col * 16, 14 + tier * 12, 38 - 17 + row * 23, 9 + (v == 3 ? 1 : 0), 10));
        }
        homes = List.copyOf(rs);
        var ps = new EnumMap<Role, BlockPos>(Role.class);
        for (var h : halls) ps.put(h.role(), h.point());
        ps.put(Role.ENTRANCE, new BlockPos(124, entranceFloor + 1, 38));
        ps.put(Role.RESIDENTIAL, new BlockPos(74, 15, 38));
        ps.put(Role.COUNCIL,new BlockPos(hs.get(4).x(),hs.get(4).y()+1,hs.get(4).z()-4));
        ps.put(Role.GATE, new BlockPos(hs.get(5).x(), hs.get(5).y() + 1, hs.get(5).z() + 4));
        points = Collections.unmodifiableMap(ps);
        var cs = new ArrayList<Connector>();
        link(cs, "entrance", "residential", new BlockPos(124, entranceFloor, 38), new BlockPos(76, 14, 38), 5);
        link(cs, "square", "residential", new BlockPos(-8, 0, -8), new BlockPos(76, 14, 38), 5);
        link(cs, "square", "forge", new BlockPos(-8, 0, -8), new BlockPos(-72, -14, 36), 5);
        var store = hs.get(3); var council = hs.get(4); var gate = hs.get(5);
        link(cs, "forge", "store", new BlockPos(-90, -14, 36), new BlockPos(store.x(), store.y(), store.z()), 5);
        link(cs, "square", "council", new BlockPos(-8, 0, -8), new BlockPos(council.x(), council.y(), council.z()-council.depth()/2+5), 5);
        link(cs, "store", "gate", new BlockPos(store.x(), store.y(), store.z()), new BlockPos(gate.x(), gate.y(), gate.z() + 4), 5);
        if (variant == 0) link(cs, "residential", "forge", new BlockPos(76, 14, 66), new BlockPos(-72, -14, 66), 3);
        else if (variant == 1) link(cs, "square", "store", new BlockPos(-8, 0, -8), new BlockPos(store.x(), store.y(), store.z()), 3);
        else {
            link(cs, "residential", "council", new BlockPos(104, 14, 38), new BlockPos(104, 14, -58), 3);
            link(cs, "council", "council", new BlockPos(104,14,-58),new BlockPos(74,14,-58),3);
            link(cs, "store", "council", new BlockPos(-66, -24, -55), new BlockPos(74, 14, -58), 3);
        }
        connectors = List.copyOf(cs);
        validate();
    }
    private void link(List<Connector> result, String from, String to, BlockPos a, BlockPos b, int width) {
        var path = new ArrayList<BlockPos>();
        int dx=b.getX()-a.getX(), dz=b.getZ()-a.getZ(), length=Math.abs(dx)+Math.abs(dz), change=b.getY()-a.getY();
        for (int i=0;i<=length;i++) {
            int z=a.getZ()+Integer.signum(dz)*Math.min(i,Math.abs(dz));
            int x=a.getX()+Integer.signum(dx)*Math.max(0,i-Math.abs(dz));
            // Spaced one-block risers; long horizontal stretches act as landings.
            int y=a.getY()+Integer.signum(change)*(int)((long)Math.abs(change)*i/Math.max(1,length));
            path.add(new BlockPos(x,y,z));
        }
        if(from.equals("entrance")&&a.getY()-b.getY()>24){
            path.clear();int turn=38+28*entranceBend;int[][] corners={{124,38},{124,turn},{110,turn},{110,38},{76,38}};int total=104,index=0;
            for(int s=0;s<corners.length-1;s++){int ax=corners[s][0],az=corners[s][1],bx=corners[s+1][0],bz=corners[s+1][1];int run=Math.abs(bx-ax)+Math.abs(bz-az);for(int k=0;k<run;k++){int py=a.getY()-(int)((long)(a.getY()-b.getY())*index/total);path.add(new BlockPos(ax+Integer.signum(bx-ax)*k,py,az+Integer.signum(bz-az)*k));index++;}}
            path.add(b);length=total;
        }
        if(from.equals("square")&&to.equals("residential")){
            path.clear();int[][] corners={{a.getX(),a.getZ()},{a.getX(),-26},{36,-26},{36,38},{b.getX(),b.getZ()}};
            for(int s=0;s<corners.length-1;s++){int ax=corners[s][0],az=corners[s][1],bx=corners[s+1][0],bz=corners[s+1][1];int run=Math.abs(bx-ax)+Math.abs(bz-az);for(int k=0;k<run;k++)path.add(new BlockPos(ax+Integer.signum(bx-ax)*k,a.getY(),az+Integer.signum(bz-az)*k));}
            path.add(b);length=path.size()-1;
        }
        if(length<Math.abs(change))throw new IllegalArgumentException("stair slope");
        var source=halls.stream().filter(h->h.id().equals(from)).findFirst();var target=halls.stream().filter(h->h.id().equals(to)).findFirst();
        int start=0,end=length;
        if(source.isPresent())while(start<length&&inside(source.get(),path.get(start)))start++;
        if(target.isPresent())while(end>start&&inside(target.get(),path.get(end)))end--;
        if(end-start>=Math.abs(change))for(int i=0;i<path.size();i++){var pos=path.get(i);int y=a.getY()+Integer.signum(change)*(int)((long)Math.abs(change)*Math.max(0,Math.min(end-start,i-start))/Math.max(1,end-start));path.set(i,new BlockPos(pos.getX(),y,pos.getZ()));}
        result.add(new Connector(from,to,List.copyOf(path),width,5));
    }
    private static boolean inside(Hall h,BlockPos p){return Math.abs(p.getX()-h.x())<=h.width()/2+3&&Math.abs(p.getZ()-h.z())<=h.depth()/2+3;}
    public Rotation blockRotation() { return Rotation.values()[rotation]; }
    public BlockPos world(BlockPos local) {
        int x=local.getX(),z=local.getZ();
        return switch(rotation) {
            case 1 -> anchor.offset(-z,local.getY(),x);
            case 2 -> anchor.offset(-x,local.getY(),-z);
            case 3 -> anchor.offset(z,local.getY(),-x);
            default -> anchor.offset(x,local.getY(),z);
        };
    }
    public String cityId() { return "chud-v1-" + anchor.getX() + "-" + anchor.getZ(); }
    public MineGate mineGate(){return new MineGate(cityId()+":mine_gate",world(new BlockPos(-109,-40,-91)),blockRotation().rotate(Direction.SOUTH),5,5,8);}
    public String layout() { return new String[]{"LINEAR","FAN","RING"}[variant]; }
    public int majorHalls() { return 3+variant; }
    public BoundingBox bounds() {
        // Native piece fits eight chunks; the separate distance envelope includes its 12-block margin.
        return new BoundingBox(anchor.getX()-139,anchor.getY()-44,anchor.getZ()-139,anchor.getX()+139,anchor.getY()+73,anchor.getZ()+139);
    }
    public void validate() {
        if (homes.size()<6 || majorHalls()!=halls.stream().filter(Hall::major).count()) throw new IllegalStateException("hall/home invariant");
        for(Role role:Role.values()) if(!points.containsKey(role)) throw new IllegalStateException("missing role "+role);
        if(halls.stream().map(Hall::role).distinct().count()!=6)throw new IllegalStateException("duplicate unique role");
        var visited=new HashSet<String>(); visited.add("entrance");
        boolean changed=true; while(changed){changed=false;for(var c:connectors)if(visited.contains(c.from())||visited.contains(c.to())){changed|=visited.add(c.from());changed|=visited.add(c.to());}}
        if(!visited.containsAll(List.of("square","residential","forge","store","council","gate")))throw new IllegalStateException("disconnected halls");
    }
    public CompoundTag save() {
        var n=new CompoundTag();n.putInt("GenerationVersion",GENERATION_VERSION);n.putLong("Seed",seed);n.putLong("Anchor",anchor.asLong());n.putInt("Variant",variant);n.putInt("Rotation",rotation);n.putInt("EntranceFloor",entranceFloor);n.putInt("EntranceBend",entranceBend);var gate=mineGate();n.putString("MineGateId",gate.id());n.putLong("MineGateFloor",gate.floor().asLong());n.putString("MineGateFacing",gate.facing().getName());return n;
    }
    public static ChudPlan load(CompoundTag n) {
        if(n.getInt("GenerationVersion")!=GENERATION_VERSION)throw new IllegalArgumentException("unsupported Chud generation version");
        var plan=new ChudPlan(n.getLong("Seed"),BlockPos.of(n.getLong("Anchor")),n.getInt("Variant"),n.getInt("Rotation"),n.getInt("EntranceFloor"),n.contains("EntranceBend")?n.getInt("EntranceBend"):1);
        if(n.contains("MineGateFloor")&&(n.getLong("MineGateFloor")!=plan.mineGate().floor().asLong()||!n.getString("MineGateFacing").equals(plan.mineGate().facing().getName())))throw new IllegalArgumentException("saved mine gate differs from generation-v1 geometry");return plan;
    }
    public static long boundaryDistanceSquared(BoundingBox a, BoundingBox b) {
        long dx=Math.max(0L,Math.max((long)a.minX()-b.maxX(),(long)b.minX()-a.maxX()));
        long dz=Math.max(0L,Math.max((long)a.minZ()-b.maxZ(),(long)b.minZ()-a.maxZ()));
        return dx*dx+dz*dz;
    }
}
