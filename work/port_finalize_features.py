from pathlib import Path
import re
p=Path('src/main/java/org/slavicmyths/client/ClientSetup.java');s=p.read_text(encoding='utf-8');lines=s.splitlines(keepends=True);menu=[];keep=[]
for l in lines:
 if 'net.minecraft.client.gui.screens.MenuScreens.register(' in l:menu.append(l.replace('net.minecraft.client.gui.screens.MenuScreens.register','event.register'))
 else:keep.append(l)
s=''.join(keep);s=s.replace('    private ClientSetup()', '    @SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event){\n'+''.join(menu)+'    }\n    private ClientSetup()').replace('new net.minecraft.client.renderer.entity.SpriteRenderer<>(manager, net.minecraft.client.Minecraft.getInstance().getItemRenderer())','new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(manager)');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/wood/TreeShape.java');s=p.read_text(encoding='utf-8').replace('private final Random random;','private final java.util.function.IntUnaryOperator nextInt;\n private final java.util.function.DoubleSupplier nextDouble;\n private final java.util.function.BooleanSupplier nextBoolean;')
s=s.replace('public TreeShape(String species,Random r){random=r;variant=r.nextInt(3);','public TreeShape(String species,Random r){this(species,r::nextInt,r::nextDouble,r::nextBoolean);}\n public TreeShape(String species,java.util.function.IntUnaryOperator integers,java.util.function.DoubleSupplier doubles,java.util.function.BooleanSupplier booleans){nextInt=integers;nextDouble=doubles;nextBoolean=booleans;variant=nextInt.applyAsInt(3);')
s=s.replace('random.nextInt(','nextInt.applyAsInt(').replace('r.nextInt(','nextInt.applyAsInt(').replace('random.nextDouble()','nextDouble.getAsDouble()').replace('random.nextBoolean()','nextBoolean.getAsBoolean()');p.write_text(s,encoding='utf-8')
for p in Path('src/main/java/org/slavicmyths').rglob('*Feature.java'):
 s=p.read_text(encoding='utf-8');old=s
 if 'extends Feature<NoneFeatureConfiguration>' not in s:continue
 s=s.replace('import net.minecraft.world.gen.*;','import net.minecraft.world.level.chunk.ChunkGenerator;').replace('import net.minecraft.world.gen.feature.*;','import net.minecraft.world.level.levelgen.feature.*;').replace('import java.util.Random;','import net.minecraft.util.RandomSource;')
 if 'import net.minecraft.util.RandomSource;' not in s:s=s.replace('\n','\nimport net.minecraft.util.RandomSource;\n',1)
 s=re.sub(r'\bRandom\b','RandomSource',s)
 pat=re.compile(r'public boolean place\(WorldGenLevel (\w+),\s*ChunkGenerator (\w+),\s*RandomSource (\w+),\s*BlockPos (\w+),\s*NoneFeatureConfiguration (\w+)\)\s*\{')
 def context(m):
  w,g,r,o,c=m.groups();return 'public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){WorldGenLevel '+w+'=context.level();ChunkGenerator '+g+'=context.chunkGenerator();RandomSource '+r+'=context.random();BlockPos '+o+'=context.origin();NoneFeatureConfiguration '+c+'=context.config();'
 s=pat.sub(context,s).replace('Blocks.GRASS)','Blocks.SHORT_GRASS)').replace('new TreeShape(species,r)','new TreeShape(species,r::nextInt,r::nextDouble,r::nextBoolean)')
 if s!=old:p.write_text(s,encoding='utf-8')
