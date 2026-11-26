package org.slavicmyths.desert.indrik;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slavicmyths.commands.CommandTree;
import static org.slavicmyths.desert.indrik.IndrikLairPlan.*;

public final class IndrikLairCommands {
    public static void register(CommandTree tree){
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2))
            .then(Commands.literal("indrik_lair")
                .then(Commands.literal("generate").executes(c->generate(c.getSource())))
                .then(Commands.literal("search").executes(c->search(c.getSource()))))));
    }
    private static int search(CommandSourceStack s)throws CommandSyntaxException{
        var known=IndrikLairRecords.get(s.getLevel()).nearest(BlockPos.containing(s.getPosition()));
        if(known.isPresent())s.sendSuccess(()->Component.literal("Ближайшее зарегистрированное логово: "+known.get().offset(0,0,ENTRANCE_Z).toShortString()),false);
        // Native locate also finds unvisited structures; registered sites include manually generated ones.
        try{return s.getServer().getCommands().getDispatcher().execute("locate structure slavicmyths:indrik_lair",s);}
        catch(CommandSyntaxException e){if(known.isPresent())return 1;throw e;}
    }
    private static int reject(CommandSourceStack s,String reason){s.sendFailure(Component.literal(reason));return 0;}
    private static int generate(CommandSourceStack s){
        var w=s.getLevel();var at=BlockPos.containing(s.getPosition());
        int x=at.getX(),z=at.getZ()-ENTRANCE_Z;
        if(!IndrikLairBuild.loaded(w,IndrikLairBuild.bounds(new BlockPos(x,at.getY(),z))))
            return reject(s,"Сначала загрузите территорию логова (33 x 36 блоков).");
        var y=anchor((wx,wz)->w.getHeight(Heightmap.Types.WORLD_SURFACE,wx,wz),
            (wx,wz)->w.getHeight(Heightmap.Types.OCEAN_FLOOR,wx,wz),x,z,w.getMinBuildHeight(),w.getMaxBuildHeight());
        if(y.isEmpty())return reject(s,"Нужна сухая площадка без обрыва: перепад рельефа слишком велик или мало места по высоте.");
        var o=new BlockPos(x,y.getAsInt(),z);var box=IndrikLairBuild.bounds(o);
        for(int dx=-12;dx<=12;dx+=12)for(int dz=-12;dz<=12;dz+=12)
            if(!w.getBiome(o.offset(dx,0,dz)).is(Biomes.DESERT))return reject(s,"Логово строится в пустыне.");
        var corner=o.offset(MIN_X,0,MIN_Z);
        if(!w.getWorldBorder().isWithinBounds(corner)||!w.getWorldBorder().isWithinBounds(o.offset(MAX_X,0,MAX_Z)))
            return reject(s,"Логово выходит за границу мира.");
        var records=IndrikLairRecords.get(w);
        if(records.overlaps(o)||org.slavicmyths.desert.oasis.LostOasisRecords.get(w).overlaps(corner,33,36)
            ||org.slavicmyths.desert.temple.SunTempleRecords.get(w).overlaps(corner,33,36)
            ||!IndrikLairBuild.canPlace(w,o))return reject(s,"Площадка занята постройкой, водой или защищёнными блоками.");
        int changed=IndrikLairBuild.render(w,o,box);records.register(o);
        s.sendSuccess(()->Component.literal("Логово Индрика-змея построено. Изменено блоков: "+changed+
            " / "+cells().size()+". Вход: "+o.offset(0,0,ENTRANCE_Z).toShortString()+
            ". Арена: "+o.offset(0,FLOOR+1,ARENA_Z).toShortString()+". Босс появится при входе."),true);
        return 1;
    }
    private IndrikLairCommands(){}
}
