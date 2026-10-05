from pathlib import Path
r=Path('src/main/java/org/slavicmyths')
(r/'yaga/YagaLegBlock.java').write_text('''package org.slavicmyths.yaga;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.state.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
/** Native scaled skin surfaces and separate joints/toes; never recolored log columns. */
public final class YagaLegBlock extends Block {
 public static final IntegerProperty PART=IntegerProperty.create("part",0,4);public static final DirectionProperty FACING=net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;
 public YagaLegBlock(){super(AbstractBlock.Properties.of(Material.STONE).strength(-1,3600000).noOcclusion().noDrops());registerDefaultState(stateDefinition.any().setValue(PART,0).setValue(FACING,Direction.NORTH));}
 @Override protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> b){b.add(PART,FACING);}
 @Override public VoxelShape getShape(BlockState s,IBlockReader w,BlockPos p,ISelectionContext c){int part=s.getValue(PART);if(part==0)return box(3,0,3,13,16,13);if(part==2)return box(5,0,5,11,16,11);boolean east=s.getValue(FACING).getAxis()==Direction.Axis.X;int a=part==1?3:6,h=part==1?10:4;return east?box(0,0,a,16,h,16-a):box(a,0,0,16-a,h,16);}
}
''','utf-8')
p=r/'registry/ModBlocks.java';s=p.read_text();i=s.rfind('}');s=s[:i]+' public static final RegistryObject<Block> YAGA_CHICKEN_LEG=BLOCKS.register("yaga_chicken_leg",org.slavicmyths.yaga.YagaLegBlock::new);\n'+s[i:];p.write_text(s,'utf-8')
p=r/'yaga/YagaHutPlan.java';s=p.read_text();a=s.index('  for(int x:new int[]{-3,3}){');b=s.index('\n  for(int x=-2;',a);s=s[:a]+'''  for(int x:new int[]{-3,3}){b(x,2,1,"slavicmyths:yaga_chicken_leg[part=0]");b(x,2,0,"slavicmyths:yaga_chicken_leg[part=1]");b(x,1,0,"slavicmyths:yaga_chicken_leg[part=2]");b(x,0,0,"slavicmyths:yaga_chicken_leg[part=2]");for(int dx=-1;dx<=1;dx++){b(x+dx,0,-1,"slavicmyths:yaga_chicken_leg[part=3]");b(x+dx,0,-2,"slavicmyths:yaga_chicken_leg[part=3]");b(x+dx,0,-3,"slavicmyths:yaga_chicken_leg[part=4]");}b(x,0,1,"slavicmyths:yaga_chicken_leg[part=3,facing=south]");b(x,0,2,"slavicmyths:yaga_chicken_leg[part=4,facing=south]");}'''+s[b:];p.write_text(s,'utf-8')
p=Path('tools/kurgan-tests/org/slavicmyths/verify/YagaHeadless.java');s=p.read_text().replace('equals("minecraft:polished_blackstone_slab")','contains("part=4")').replace('contains("stairs"),"straight pillar leg"','contains("part=1"),"straight pillar leg"');p.write_text(s,'utf-8')
p=Path('tools/yaga_093.py');s=p.read_text();s=s.replace("'#ae9b54'","'#ba9348'");s=s.replace("'yaga_bone_charm']","'yaga_bone_charm','yaga_chicken_leg']",1).replace("'yaga_binding':'#834031'}","'yaga_binding':'#834031','yaga_scales':'#9d8e6e','yaga_claw':'#373832'}")
s=s.replace(" if id=='yaga_liquid':", " if id=='yaga_scales':\n  for yy in range(0,64,8):\n   for xx in range(-8,64,16):\n    off=8 if yy%16 else 0;d.rectangle((xx+off,yy,xx+off+14,yy+6),outline='#6b6554');d.line((xx+off+2,yy+1,xx+off+12,yy+1),fill='#b6a47c',width=1)\n if id=='yaga_liquid':")
marker='# 320x236 carved wooden frame';idx=s.index(marker);s=s[:idx]+'''# Native anatomy models: thigh, knee, tarsus, long toe and curved dark claw.
leg_shapes=[([3,0,3],[13,16,13]),([3,0,0],[13,10,16]),([5,0,5],[11,16,11]),([6,0,0],[10,4,16]),([6,0,0],[10,4,12])]
for part,(lo,hi) in enumerate(leg_shapes):
 elements=[cub(lo,hi,'scales' if part<4 else 'claw')]
 if part==1:elements.append(cub([4,8,5],[12,14,15],'scales'))
 if part==4:elements.extend([cub([6,0,-0],[10,3,5],'claw'),cub([7,0,0],[9,2,3],'claw')])
 out(A/f'models/block/yaga_leg_{part}.json',{'textures':{'particle':'slavicmyths:block/yaga_scales','scales':'slavicmyths:block/yaga_scales','claw':'slavicmyths:block/yaga_claw'},'elements':elements})
out(A/'blockstates/yaga_chicken_leg.json',{'variants':{f'part={part},facing={face}':{'model':f'slavicmyths:block/yaga_leg_{part}','y':rot} for part in range(5) for face,rot in [('north',0),('east',90),('south',180),('west',270)]}})
out(D/'loot_tables/blocks/yaga_chicken_leg.json',{'type':'minecraft:block','pools':[]})
'''+s[idx:];s=s.replace("['Костяной подвес','Bone charm']])", "['Костяной подвес','Bone charm'],['Куриная лапа избушки','Hut chicken leg']])");p.write_text(s,'utf-8')
