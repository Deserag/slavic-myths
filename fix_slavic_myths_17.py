#!/usr/bin/env python3
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

ITEM_REGISTRATIONS = {
    "MYTAR_SEAL": ('mytar_seal', 'public static final DeferredHolder<Item, Item> MYTAR_SEAL = ITEMS.register("mytar_seal", () -> new Item(properties().rarity(Rarity.RARE).stacksTo(1)));'),
    "MYTAR_CHAIN": ('mytar_chain', 'public static final DeferredHolder<Item, Item> MYTAR_CHAIN = ITEMS.register("mytar_chain", () -> new Item(properties().rarity(Rarity.RARE)));'),
    "MYTAR_SICKLE": ('mytar_sickle', 'public static final DeferredHolder<Item, Item> MYTAR_SICKLE = ITEMS.register("mytar_sickle", () -> new Item(properties().rarity(Rarity.RARE)));'),
    "MYTAR_PURSE": ('mytar_purse', 'public static final DeferredHolder<Item, Item> MYTAR_PURSE = ITEMS.register("mytar_purse", () -> new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.RARE), "belt"));'),
    "BAUK_FANG": ('bauk_fang', 'public static final DeferredHolder<Item, Item> BAUK_FANG = ITEMS.register("bauk_fang", () -> new Item(properties().rarity(Rarity.RARE).stacksTo(1)));'),
    "BAUK_HIDE": ('bauk_hide', 'public static final DeferredHolder<Item, Item> BAUK_HIDE = ITEMS.register("bauk_hide", () -> new Item(properties().rarity(Rarity.RARE)));'),
    "BAUK_CLAW": ('bauk_claw', 'public static final DeferredHolder<Item, Item> BAUK_CLAW = ITEMS.register("bauk_claw", () -> new Item(properties().rarity(Rarity.RARE)));'),
    "BAUK_HEAD_ITEM": ('bauk_head', 'public static final DeferredHolder<Item, Item> BAUK_HEAD_ITEM = ITEMS.register("bauk_head", () -> new BlockItem(ModBlocks.BAUK_HEAD.get(), properties().stacksTo(1).rarity(Rarity.EPIC)));'),
    "BAUK_HIDE_CHARM_ITEM": ('bauk_hide_charm', 'public static final DeferredHolder<Item, Item> BAUK_HIDE_CHARM_ITEM = ITEMS.register("bauk_hide_charm", () -> new org.slavicmyths.item.FolkAccessoryItem(properties().rarity(Rarity.EPIC), "charm"));'),
}

ENTITY_REGISTRATIONS = {
    "POSAD_RESIDENT": ('posad_resident', 'public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.posad.PosadResidentEntity>> POSAD_RESIDENT = ENTITIES.register("posad_resident", () -> EntityType.Builder.of(org.slavicmyths.posad.PosadResidentEntity::new, MobCategory.CREATURE).sized(.6F, 1.95F).fireImmune().clientTrackingRange(10).build("slavicmyths:posad_resident"));'),
    "CURSED_MYTAR": ('cursed_mytar', 'public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.posad.boss.PosadBoss>> CURSED_MYTAR = ENTITIES.register("cursed_mytar", () -> EntityType.Builder.<org.slavicmyths.posad.boss.PosadBoss>of((type, level) -> new org.slavicmyths.posad.boss.PosadBoss(type, level, org.slavicmyths.posad.boss.PosadBoss.Kind.MYTAR), MobCategory.MONSTER).sized(1.2F, 4F).clientTrackingRange(16).fireImmune().build("slavicmyths:cursed_mytar"));'),
    "BAUK": ('bauk', 'public static final DeferredHolder<EntityType<?>, EntityType<org.slavicmyths.posad.boss.PosadBoss>> BAUK = ENTITIES.register("bauk", () -> EntityType.Builder.<org.slavicmyths.posad.boss.PosadBoss>of((type, level) -> new org.slavicmyths.posad.boss.PosadBoss(type, level, org.slavicmyths.posad.boss.PosadBoss.Kind.BAUK), MobCategory.MONSTER).sized(2.5F, 3F).clientTrackingRange(16).fireImmune().build("slavicmyths:bauk"));'),
}

BLOCK_REGISTRATIONS = {
    "BAUK_HEAD": ('bauk_head', 'public static final DeferredHolder<Block, Block> BAUK_HEAD = BLOCKS.register("bauk_head", () -> new org.slavicmyths.posad.boss.BaukHeadBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(3F).sound(SoundType.BONE_BLOCK).noOcclusion()));'),
}

ATTRIBUTE_REGISTRATIONS = {
    "POSAD_RESIDENT": 'event.put(POSAD_RESIDENT.get(), org.slavicmyths.posad.PosadResidentEntity.attributes().build());',
    "CURSED_MYTAR": 'event.put(CURSED_MYTAR.get(), org.slavicmyths.posad.boss.PosadBoss.attributes(org.slavicmyths.posad.boss.PosadBoss.Kind.MYTAR).build());',
    "BAUK": 'event.put(BAUK.get(), org.slavicmyths.posad.boss.PosadBoss.attributes(org.slavicmyths.posad.boss.PosadBoss.Kind.BAUK).build());',
}

BAUK_HEAD_BLOCK = '''package org.slavicmyths.posad.boss;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BaukHeadBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BaukHeadBlock> CODEC = simpleCodec(BaukHeadBlock::new);
    public static final BooleanProperty JAW_OPEN = BooleanProperty.create("jaw_open");

    public BaukHeadBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(JAW_OPEN, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, JAW_OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos front = context.getClickedPos().relative(facing);
        if (!context.getLevel().hasChunkAt(front) || !context.getLevel().getBlockState(front).isAir()) return null;
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, world, pos, oldState, moved);
        if (!world.isClientSide) world.scheduleTick(pos, this, 45);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, state.setValue(JAW_OPEN, !state.getValue(JAW_OPEN)), 3);
        level.scheduleTick(pos, this, 35 + random.nextInt(35));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> Block.box(1, 0, 1, 15, 15, 32);
            case EAST -> Block.box(1, 0, 1, 32, 15, 15);
            case WEST -> Block.box(-16, 0, 1, 15, 15, 15);
            default -> Block.box(1, 0, -16, 15, 15, 15);
        };
    }
}
'''


def line_separator(source: str) -> str:
    return '\r\n' if '\r\n' in source else '\n'


def add_fields(source: str, registrar: str, registry_type: str, registrations: dict[str, tuple[str, str]]) -> tuple[str, list[str]]:
    missing = []
    for field, (registry_id, declaration) in registrations.items():
        if re.search(r'\b' + re.escape(field) + r'\s*=', source):
            continue
        if re.search(r'\b' + registrar + r'\.register\s*\(\s*"' + re.escape(registry_id) + r'"', source):
            raise ValueError(f'{registry_id}: registry ID already used under a different field')
        missing.append((field, declaration))
    if not missing:
        return source, []
    pattern = r'\bDeferredRegister\.create\s*\(\s*Registries\.' + registry_type + r'\s*,\s*SlavicMyths\.MOD_ID\s*\)\s*;'
    anchor = re.search(pattern, source)
    if not anchor:
        raise ValueError(f'Cannot find expected {registrar} registry declaration')
    newline = line_separator(source)
    insertion = ''.join(newline + '    ' + declaration for _, declaration in missing)
    result = source[:anchor.end()] + insertion + source[anchor.end():]
    return result, [name for name, _ in missing]


def add_attributes(source: str) -> tuple[str, list[str]]:
    missing = [(name, declaration) for name, declaration in ATTRIBUTE_REGISTRATIONS.items() if not re.search(r'\bevent\.put\s*\(\s*' + name + r'\.get\s*\(', source)]
    if not missing:
        return source, []
    anchor = re.search(r'\bpublic\s+static\s+void\s+attributes\s*\(\s*EntityAttributeCreationEvent\s+\w+\s*\)\s*\{', source)
    if not anchor:
        raise ValueError('Cannot locate ModEntities.attributes(EntityAttributeCreationEvent)')
    newline = line_separator(source)
    insertion = ''.join(newline + '        ' + declaration for _, declaration in missing)
    return source[:anchor.end()] + insertion + source[anchor.end():], [name for name, _ in missing]


def match_brace(source: str, opening: int) -> int:
    depth = 0
    for i in range(opening, len(source)):
        if source[i] == '{':
            depth += 1
        elif source[i] == '}':
            depth -= 1
            if depth == 0:
                return i
    raise ValueError('Unbalanced method braces')


def add_traits_overload(source: str) -> tuple[str, list[str]]:
    if re.search(r'\bPosadResidentTraits\s+of\s*\(\s*UUID\s+\w+\s*,\s*PosadPopulationPlan\.Role\s+\w+\s*\)', source):
        return source, []
    signature = re.search(r'\bpublic\s+static\s+PosadResidentTraits\s+of\s*\(\s*PosadPopulationPlan\.WorldMember\s+member\s*\)\s*\{', source)
    if not signature:
        raise ValueError('Cannot locate PosadResidentTraits.of(WorldMember member)')
    opening = source.find('{', signature.start())
    closing = match_brace(source, opening)
    old_body = source[opening + 1:closing]
    new_body, count = re.subn(r'\bUUID\s+id\s*=\s*member\.identity\s*\(\s*\)\s*;', '', old_body, count=1)
    if count != 1:
        raise ValueError('Cannot convert traits generator: expected UUID id = member.identity()')
    if not re.search(r'member\.member\s*\(\s*\)\.role\s*\(\s*\)', new_body):
        raise ValueError('Cannot convert traits generator: expected member.member().role()')
    new_body = re.sub(r'member\.member\s*\(\s*\)\.role\s*\(\s*\)', 'role', new_body)
    new_body = re.sub(r'member\.identity\s*\(\s*\)', 'id', new_body)
    newline = line_separator(source)
    indent = re.search(r'(?m)^[ \t]*(?=public\s+static\s+PosadResidentTraits\s+of\s*\()', source[0:signature.start()])
    pad = '    '
    replacement = ('public static PosadResidentTraits of(PosadPopulationPlan.WorldMember member) {' + newline + pad * 2 + 'return of(member.identity(), member.member().role());' + newline + pad + '}' + newline + newline + pad + 'public static PosadResidentTraits of(UUID id, PosadPopulationPlan.Role role) {' + new_body + '}')
    return source[:signature.start()] + replacement + source[closing + 1:], ['of(UUID, Role)']


def read_source(path: Path) -> str:
    if not path.is_file():
        raise FileNotFoundError(f'Missing project file: {path}')
    return path.read_bytes().decode('utf-8')


def main() -> int:
    parser = argparse.ArgumentParser(description='Preserve full existing Slavic Myths classes while repairing 17 unresolved symbols.')
    parser.add_argument('--root', type=Path, default=Path.cwd(), help='Root of current local slavic-myths worktree')
    parser.add_argument('--dry-run', action='store_true', help='Validate only, without modifying Java files')
    args = parser.parse_args()
    java_root = args.root / 'src' / 'main' / 'java' / 'org' / 'slavicmyths'
    paths = {
        'ModBlocks.java': java_root / 'registry' / 'ModBlocks.java',
        'ModItems.java': java_root / 'registry' / 'ModItems.java',
        'ModEntities.java': java_root / 'registry' / 'ModEntities.java',
        'PosadResidentTraits.java': java_root / 'posad' / 'PosadResidentTraits.java',
    }
    updates: dict[Path, tuple[str, str]] = {}
    try:
        for filename, path in paths.items():
            original = read_source(path)
            if filename == 'ModBlocks.java':
                updated, found = add_fields(original, 'BLOCKS', 'BLOCK', BLOCK_REGISTRATIONS)
            elif filename == 'ModItems.java':
                updated, found = add_fields(original, 'ITEMS', 'ITEM', ITEM_REGISTRATIONS)
            elif filename == 'ModEntities.java':
                updated, found = add_fields(original, 'ENTITIES', 'ENTITY_TYPE', ENTITY_REGISTRATIONS)
                updated, attr = add_attributes(updated)
                found += ['attributes.' + name for name in attr]
            else:
                updated, found = add_traits_overload(original)
            print(filename + ': ' + (', '.join(found) if found else 'already up to date'))
            if original != updated:
                updates[path] = (original, updated)
        head_path = java_root / 'posad' / 'boss' / 'BaukHeadBlock.java'
        write_head = not head_path.exists()
        print('BaukHeadBlock.java: ' + ('create full block implementation' if write_head else 'already exists'))
        if args.dry_run:
            print('Dry run passed; nothing written.')
            return 0
        for path, (original, updated) in updates.items():
            backup = path.with_suffix(path.suffix + '.before_17_fix.bak')
            if not backup.exists():
                backup.write_bytes(original.encode('utf-8'))
            path.write_bytes(updated.encode('utf-8'))
        if write_head:
            head_path.parent.mkdir(parents=True, exist_ok=True)
            head_path.write_text(BAUK_HEAD_BLOCK, encoding='utf-8')
        print('Updated ' + str(len(updates) + int(write_head)) + ' complete Java files. Run .\\gradlew.bat compileJava --no-daemon')
        return 0
    except (OSError, ValueError) as exc:
        print('ERROR: ' + str(exc), file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())
