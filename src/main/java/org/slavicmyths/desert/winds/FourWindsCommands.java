package org.slavicmyths.desert.winds;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slavicmyths.commands.CommandTree;
import static org.slavicmyths.desert.winds.FourWindsPlan.*;

public final class FourWindsCommands {
    public static void register(CommandTree tree){
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2))
            .then(Commands.literal("four_winds_palace")
                .then(Commands.literal("generate").executes(c->generate(c.getSource())))
                .then(Commands.literal("search").executes(c->search(c.getSource()))))));
    }
    private static int search(CommandSourceStack s)throws CommandSyntaxException{
        return s.getServer().getCommands().getDispatcher().execute("locate structure slavicmyths:four_winds_palace",s);
    }
    private static int reject(CommandSourceStack s,String reason){s.sendFailure(Component.literal(reason));return 0;}
    private static int generate(CommandSourceStack s){
        var w=s.getLevel();var at=BlockPos.containing(s.getPosition());
        int x=at.getX(),z=at.getZ()-ENTRANCE_Z;
        // The operator stands on the ground landing; never force-load distant chunks.
        var rough=FourWindsBuild.bounds(new BlockPos(x,at.getY(),z));
        if(!FourWindsBuild.loaded(w,rough))return reject(s,"Сначала загрузите всю территорию дворца (47 x 89 блоков).");
        var y=FourWindsBuild.anchor((wx,wz)->w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,wx,wz),
            x,z,w.getMinBuildHeight(),w.getMaxBuildHeight());
        if(y.isEmpty())return reject(s,"Требуется ровная пустынная площадка и свободный воздух над ней.");
        var o=new BlockPos(x,y.getAsInt(),z);var box=FourWindsBuild.bounds(o);
        var corner=o.offset(MIN_X,0,MIN_Z);
        if(!w.getBiome(o).is(Biomes.DESERT)||!w.getBiome(o.offset(0,0,ENTRANCE_Z)).is(Biomes.DESERT))
            return reject(s,"Дворец четырёх ветров строится в пустыне.");
        if(!w.getWorldBorder().isWithinBounds(corner)||!w.getWorldBorder().isWithinBounds(o.offset(MAX_X,0,MAX_Z)))
            return reject(s,"Дворец выходит за границу мира.");
        var records=FourWindsRecords.get(w);
        if(records.overlaps(o)||org.slavicmyths.desert.oasis.LostOasisRecords.get(w).overlaps(corner,W,D)
            ||org.slavicmyths.desert.temple.SunTempleRecords.get(w).overlaps(corner,W,D)||!FourWindsBuild.canPlace(w,o))
            return reject(s,"Площадка пересекается с существующей постройкой или занята блоками.");
        FourWindsBuild.render(w,o,box);records.register(o);
        s.sendSuccess(()->Component.literal("Дворец четырёх ветров построен: 47 x 89 x 59, 13930 блоков. Арена: "+o.above(FLOOR+1).toShortString()+". Босс появится при входе."),true);
        return 1;
    }
    private FourWindsCommands(){}
}
