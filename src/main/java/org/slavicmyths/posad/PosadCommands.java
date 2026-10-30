package org.slavicmyths.posad;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * OP placement and read-only layout. Manual commands are independent of natural
 * biome/rarity gates; authored templates retain their original geometry and loot.
 */
public final class PosadCommands {
    private record Template(String name, String id, PosadGeometry.Slot slot, int x, int z) {}
    private static final Template[] TEMPLATES = {
        new Template("Вечевая палата", "veche_hall_2_2_2", PosadGeometry.Slot.VECHE_HALL, 42, 12),
        new Template("Общинный дом", "communal_house_2_2_3", PosadGeometry.Slot.COMMUNAL_HOUSE, 13, 15),
        new Template("Дружинный двор", "druzhinny_dvor_2_2_4", PosadGeometry.Slot.DRUZHINNY_DVOR, 12, 75),
        new Template("Кузнечный двор", "kuznechny_dvor_2_2_5", PosadGeometry.Slot.KUZNECHNY_DVOR, 12, 44),
        new Template("Меновые ряды", "menovye_ryady_2_2_6", PosadGeometry.Slot.MENOVYE_RYADY, 75, 75)
    };

    public static void register(org.slavicmyths.commands.CommandTree tree) {
        var generate=Commands.literal("generate").requires(source->source.hasPermission(2));
        generate.then(Commands.literal("nether_posad").executes(c->start(c.getSource())));
        String[][] names={{"veche_hall","veche_hall_2_2_2"},{"communal_house","communal_house_2_2_3"},{"druzhinny_dvor","druzhinny_dvor_2_2_4"},{"kuznechny_dvor","kuznechny_dvor_2_2_5"},{"zeleynaya","zeleynaya_2_2_7"},{"granary","granary_2_2_8"},{"menovye_ryady","menovye_ryady_2_2_6"}};
        for(var name:names)generate.then(Commands.literal(name[0]).executes(c->building(c.getSource(),name[1],org.slavicmyths.worldgen.ManualTerrain.ground(c.getSource()))));
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").then(generate)));
        var posad = Commands.literal("posad").requires(source -> source.hasPermission(2));
        posad.then(Commands.literal("info").executes(context -> info(context.getSource())));
        posad.then(Commands.literal("layout")
            .then(Commands.argument("turns", IntegerArgumentType.integer(0, 3))
                .then(Commands.argument("anchorX", IntegerArgumentType.integer())
                    .then(Commands.argument("anchorZ", IntegerArgumentType.integer())
                        .executes(context -> layout(context.getSource(), IntegerArgumentType.getInteger(context, "turns"), IntegerArgumentType.getInteger(context, "anchorX"), IntegerArgumentType.getInteger(context, "anchorZ")))))));
        posad.then(Commands.literal("templates")
            .then(Commands.argument("anchorX", IntegerArgumentType.integer())
                .then(Commands.argument("deckY", IntegerArgumentType.integer())
                    .then(Commands.argument("anchorZ", IntegerArgumentType.integer())
                        .executes(context -> templates(context.getSource(), IntegerArgumentType.getInteger(context, "anchorX"), IntegerArgumentType.getInteger(context, "deckY"), IntegerArgumentType.getInteger(context, "anchorZ")))))));
        posad.then(Commands.literal("status").requires(source -> source.hasPermission(2)).executes(context -> PosadManualJobs.status(context.getSource(), false)));
        posad.then(Commands.literal("cancel").requires(source -> source.hasPermission(2)).executes(context -> PosadManualJobs.status(context.getSource(), true)));
        var createPosition=Commands.argument("origin",net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos())
            .executes(c -> create(c));
        posad.then(Commands.literal("create").then(Commands.argument("turns",IntegerArgumentType.integer(0,3)).then(createPosition)));
        for(String id : new String[]{"veche_hall_2_2_2","communal_house_2_2_3","druzhinny_dvor_2_2_4","kuznechny_dvor_2_2_5","zeleynaya_2_2_7","granary_2_2_8","menovye_ryady_2_2_6"}) {
            var building=Commands.literal(id).executes(c -> building(c.getSource(),id,net.minecraft.core.BlockPos.containing(c.getSource().getPosition())));
            building.then(Commands.argument("pos",net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos()).executes(c -> building(c.getSource(),id,net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos(c,"pos"))));
            posad.then(Commands.literal("building").then(building));
        }
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").then(posad)));
    }

    public static int start(CommandSourceStack source){
        var at=org.slavicmyths.worldgen.ManualTerrain.ground(source).offset(-56,-1,12);
        return PosadManualJobs.create(source,0,at.getX(),at.getY(),at.getZ());
    }
    private static int info(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Пекельный посад: фиксированный план 113x113, схема новых городов " + PosadGeometry.SCHEMA_VERSION + ", несколько зданий, мебель и башенный переход."), false);
        source.sendSuccess(() -> Component.literal("Естественная генерация пока заблокирована до native QA, performance, save/reload и anti-dupe. Старые города никогда не перестраиваются."), false);
        source.sendSuccess(() -> Component.literal("Ручное создание с OP 2 в любом мире: /slavicmyths dev posad create <turns> <anchorX> <deckY> <anchorZ>. Чанки подготавливаются постепенно. status/cancel — для управления."), false);
        return 1;
    }

    /**
     * Read-only map for a rotated settlement. This command does not place structures
     * or trigger chunk generation and works for planning before an isolated QA run.
     */
    private static int layout(CommandSourceStack source, int turns, int anchorX, int anchorZ) {
        source.sendSuccess(() -> Component.literal("Пекельный посад 113x113, поворот " + (turns * 90) + "°, anchor NW (" + anchorX + "," + anchorZ + "). Только схема, без генерации."), false);
        for (Template template : TEMPLATES) {
            PosadGeometry.Rect rect = template.slot().world(anchorX, anchorZ, turns);
            source.sendSuccess(() -> Component.literal(template.name() + ": X" + rect.minX() + ".." + rect.maxX() + " Z" + rect.minZ() + ".." + rect.maxZ()), false);
        }
        source.sendSuccess(() -> Component.literal("Для проверки размещения используйте штатный native PosadPiece; /place template без preflight может разрушить чужие блоки."), false);
        return TEMPLATES.length;
    }

    private static int templates(CommandSourceStack source, int anchorX, int deckY, int anchorZ) {
        source.sendSuccess(() -> Component.literal("Ручные команды зданий с проверкой занятого места. Они не достраивают старые города."), false);
        for (Template t : TEMPLATES) {
            int x = Math.addExact(anchorX, t.x());
            int z = Math.addExact(anchorZ, t.z());
            String command = "/slavicmyths dev posad building " + t.id() + " " + x + " " + deckY + " " + z;
            source.sendSuccess(() -> Component.literal(t.name() + " [слот " + t.slot().local() + "]: " + command), false);
        }
        source.sendSuccess(() -> Component.literal("Городская ориентация для команд: только 0°; для 90/180/270 используйте native PosadPiece и тесты, не поворачивайте отдельные дома независимо."), false);
        return TEMPLATES.length;
    }

    private static int create(com.mojang.brigadier.context.CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var p=net.minecraft.commands.arguments.coordinates.BlockPosArgument.getBlockPos(c,"origin");
        return PosadManualJobs.create(c.getSource(),IntegerArgumentType.getInteger(c,"turns"),p.getX(),p.getY(),p.getZ());
    }
    public static int building(CommandSourceStack source,String id,net.minecraft.core.BlockPos pos) {
        if(!source.hasPermission(2))return 0;
        var world=source.getLevel();
        var key=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","posad/"+id);
        var template=world.getStructureManager().get(key);
        if(template.isEmpty()){source.sendFailure(Component.literal("Шаблон не найден: "+key));return 0;}
        var size=template.get().getSize();
        if((long)size.getX()*size.getY()*size.getZ()>32768)throw new IllegalArgumentException("Template exceeds bounded placement size");
        var end=pos.offset(size.getX()-1,size.getY()-1,size.getZ()-1);
        if(pos.getY()<world.getMinBuildHeight()||end.getY()>=world.getMaxBuildHeight()||!world.getWorldBorder().isWithinBounds(pos)||!world.getWorldBorder().isWithinBounds(end)){source.sendFailure(Component.literal("Постройка выходит за высоту или границу мира: "+pos.toShortString()));return 0;}
        for(int cx=pos.getX()>>4;cx<=end.getX()>>4;cx++)for(int cz=pos.getZ()>>4;cz<=end.getZ()>>4;cz++)
            if(world.getChunkSource().getChunkNow(cx,cz)==null){source.sendFailure(Component.literal("Чанк не загружен: "+cx+","+cz+". Подойдите ближе к площадке."));return 0;}
        var bounds=new net.minecraft.world.level.levelgen.structure.BoundingBox(pos.getX(),pos.getY(),pos.getZ(),end.getX(),end.getY(),end.getZ());
        if(!org.slavicmyths.worldgen.ManualStructureRecords.get(world).overlapping(bounds).isEmpty()){source.sendFailure(Component.literal("Область уже занята постройкой."));return 0;}
        for(var at:net.minecraft.core.BlockPos.betweenClosed(pos,end)) {
            var old=world.getBlockState(at);
            if(old.hasBlockEntity()||old.is(net.minecraft.world.level.block.Blocks.BEDROCK)||org.slavicmyths.rpg.tools.ToolLogOrigins.get(world).blocked(at)||!(org.slavicmyths.worldgen.ManualStructureJobs.natural(old))) {
                source.sendFailure(Component.literal("Площадка занята: "+at.toShortString()+" "+old));return 0;
            }
        }
        java.util.Map<net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState> foundation;
        try{foundation=org.slavicmyths.worldgen.ManualTerrain.foundation(world,pos,end);}catch(IllegalArgumentException failure){source.sendFailure(Component.literal(failure.getMessage()));return 0;}
        var record=org.slavicmyths.worldgen.ManualStructureRecords.get(world);
        var token=java.util.UUID.randomUUID();
        record.add(token,"posad_building/"+id,0,pos,bounds);
        world.getDataStorage().save();
        foundation.forEach((at,state)->world.setBlock(at,state,2));
        // NBT stores only non-air authored cells. Clear the fully checked envelope
        // so natural cave stone cannot remain inside doors/rooms.
        for(var at:net.minecraft.core.BlockPos.betweenClosed(pos,end))if(!world.getBlockState(at).isAir())world.setBlock(at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
        boolean placed=template.get().placeInWorld(world,pos,pos,new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings(),world.random,2);
        if(!placed){source.sendFailure(Component.literal("Шаблон не разместился: "+key+"; резерв сохранён."));return 0;}
        // Template barrels are authored empty: assign persistent single-use loot after placement.
        String table=id.startsWith("menovye_ryady")?"chests/posad_barrel_market":"chests/posad_barrel_supplies";
        for(var at:net.minecraft.core.BlockPos.betweenClosed(pos,end)) {
            if(world.getBlockState(at).is(net.minecraft.world.level.block.Blocks.BARREL) && world.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity barrel) {
                barrel.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",table)),at.asLong()^pos.asLong());
                barrel.setChanged();
            }
        }
        source.sendSuccess(()->Component.literal("Построено: "+id+" в "+pos.toShortString()),false);
        return 1;
    }
    private PosadCommands() {}
}
