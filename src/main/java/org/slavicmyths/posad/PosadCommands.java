package org.slavicmyths.posad;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * Admin-only template command planner for disposable Nether QA worlds.
 * Never loads chunks, places blocks, grants rewards or upgrades an existing city.
 * Actual isolated placement uses vanilla /place template with exact approved R1 NBT.
 */
public final class PosadCommands {
    private record Template(String name, String id, PosadGeometry.Slot slot, int x, int z) {}
    private static final Template[] TEMPLATES = {
        new Template("Вечевая палата", "veche_hall_2_2_2", PosadGeometry.Slot.VECHE_HALL, 42, 12),
        new Template("Общинный дом", "communal_house_2_2_3", PosadGeometry.Slot.COMMUNAL_HOUSE, 13, 15),
        new Template("Дружинный двор", "druzhinny_dvor_2_2_4", PosadGeometry.Slot.DRUZHINNY_DVOR, 12, 75),
        new Template("Кузнечный двор", "kuznechny_dvor_2_2_5", PosadGeometry.Slot.KUZNECHNY_DVOR, 12, 44)
    };

    public static void register(org.slavicmyths.commands.CommandTree tree) {
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
        posad.then(Commands.literal("status").requires(source -> source.hasPermission(4)).executes(context -> PosadManualJobs.status(context.getSource(), false)));
        posad.then(Commands.literal("cancel").requires(source -> source.hasPermission(4)).executes(context -> PosadManualJobs.status(context.getSource(), true)));
        var confirmation = Commands.literal("confirm_disposable_world").executes(context -> PosadManualJobs.create(context.getSource(), IntegerArgumentType.getInteger(context, "turns"), IntegerArgumentType.getInteger(context, "anchorX"), IntegerArgumentType.getInteger(context, "deckY"), IntegerArgumentType.getInteger(context, "anchorZ")));
        var createZ = Commands.argument("anchorZ", IntegerArgumentType.integer()).then(confirmation);
        var createY = Commands.argument("deckY", IntegerArgumentType.integer()).then(createZ);
        var createX = Commands.argument("anchorX", IntegerArgumentType.integer()).then(createY);
        var createTurns = Commands.argument("turns", IntegerArgumentType.integer(0, 3)).then(createX);
        posad.then(Commands.literal("create").requires(source -> source.hasPermission(4)).then(createTurns));
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").then(posad)));
    }

    private static int info(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Пекельный посад: фиксированный план 113x113, схема новых городов " + PosadGeometry.SCHEMA_VERSION + ", четыре утверждённых шаблона (3598/1992/2440/1901 блоков)."), false);
        source.sendSuccess(() -> Component.literal("Естественная генерация пока заблокирована до native QA, performance, save/reload и anti-dupe. Старые города никогда не перестраиваются."), false);
        source.sendSuccess(() -> Component.literal("Только мир posad-qa-* и OP 4: /slavicmyths dev posad create <turns> <anchorX> <deckY> <anchorZ> confirm_disposable_world. Чанки должны быть заранее загружены. status/cancel — для управления."), false);
        return 1;
    }

    /**
     * Read-only map for a rotated settlement. This command does not place structures
     * or trigger chunk generation and works for planning before an isolated QA run.
     */
    private static int layout(CommandSourceStack source, int turns, int anchorX, int anchorZ) {
        if (!source.getLevel().dimension().equals(Level.NETHER)) {
            source.sendFailure(Component.literal("Пекельный посад: план доступен только в Незере; блоки не изменены."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Пекельный посад 113x113, поворот " + (turns * 90) + "°, anchor NW (" + anchorX + "," + anchorZ + "). Только схема, без генерации."), false);
        for (Template template : TEMPLATES) {
            PosadGeometry.Rect rect = template.slot().world(anchorX, anchorZ, turns);
            source.sendSuccess(() -> Component.literal(template.name() + ": X" + rect.minX() + ".." + rect.maxX() + " Z" + rect.minZ() + ".." + rect.maxZ()), false);
        }
        source.sendSuccess(() -> Component.literal("Для проверки размещения используйте штатный native PosadPiece; /place template без preflight может разрушить чужие блоки."), false);
        return TEMPLATES.length;
    }

    private static int templates(CommandSourceStack source, int anchorX, int deckY, int anchorZ) {
        if (!source.getLevel().dimension().equals(Level.NETHER)) {
            source.sendFailure(Component.literal("Шаблоны Пекельного посада доступны только в Незере; мир не изменён."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Только disposable QA-мир! Команды ниже НЕ проверяют чужие постройки и не предназначены для достройки старых городов."), false);
        for (Template t : TEMPLATES) {
            int x = Math.addExact(anchorX, t.x());
            int z = Math.addExact(anchorZ, t.z());
            String command = "/place template slavicmyths:posad/" + t.id() + " " + x + " " + deckY + " " + z;
            source.sendSuccess(() -> Component.literal(t.name() + " [слот " + t.slot().local() + "]: " + command), false);
        }
        source.sendSuccess(() -> Component.literal("Городская ориентация для команд: только 0°; для 90/180/270 используйте native PosadPiece и тесты, не поворачивайте отдельные дома независимо."), false);
        return TEMPLATES.length;
    }

    private PosadCommands() {}
}
