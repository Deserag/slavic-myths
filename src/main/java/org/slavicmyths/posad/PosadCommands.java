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
        posad.then(Commands.literal("templates")
            .then(Commands.argument("anchorX", IntegerArgumentType.integer())
                .then(Commands.argument("deckY", IntegerArgumentType.integer())
                    .then(Commands.argument("anchorZ", IntegerArgumentType.integer())
                        .executes(context -> templates(context.getSource(), IntegerArgumentType.getInteger(context, "anchorX"), IntegerArgumentType.getInteger(context, "deckY"), IntegerArgumentType.getInteger(context, "anchorZ")))))));
        tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").then(posad)));
    }

    private static int info(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Пекельный посад: фиксированный план 113x113, схема новых городов " + PosadGeometry.SCHEMA_VERSION + ", четыре утверждённых шаблона (3598/1992/2440/1901 блоков)."), false);
        source.sendSuccess(() -> Component.literal("StructureSet натуральной генерации пока заблокирован: не пройдены atomic worldgen, performance, save/reload и антидюп. Старые города не перестраиваются."), false);
        source.sendSuccess(() -> Component.literal("Для тестового мира: /slavicmyths dev posad templates <anchorX> <deckY> <anchorZ>. Команды /place template исполняются отдельно с правами OP."), false);
        return 1;
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
