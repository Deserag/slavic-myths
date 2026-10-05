from pathlib import Path
import re
files=[Path('src/main/java/org/slavicmyths/wood')/name for name in ['RowanLeaves.java','HangingLeaves.java']]
files += [Path('src/main/java/org/slavicmyths')/n for n in ['artifact/SkatertBlock.java','depth/PoolStoneBlock.java','water/FishingNetBlock.java','water/ReedBlock.java','yaga/YagaLegBlock.java','yaga/YagaCauldronBlock.java','rpg/RpgBlock.java','kurgan/BurialCoffinBlock.java','furniture/FurnitureBlock.java','furniture/WardrobeBlock.java']]
for p in files:
 s=p.read_text(encoding='utf-8');old=s
 s=re.sub(r'^import net\.minecraft\.block\.material\.Material;\s*','',s,flags=re.M)
 s=re.sub(r'Properties\.of\((?:net\.minecraft\.block\.material\.)?Material\.([A-Z_]+)\)',r'Properties.of().mapColor(net.minecraft.world.level.material.MapColor.\1)',s)
 s=s.replace('MapColor.PLANT','MapColor.PLANT').replace('Properties.copy(', 'Properties.ofFullCopy(')
 s=s.replace('java.util.Random','net.minecraft.util.RandomSource').replace('Random r','RandomSource r')
 s=re.sub(r'(\w+)\.getBlockTicks\(\)\.scheduleTick\(',r'\1.scheduleTick(',s)
 s=s.replace('.inventory.add(','.getInventory().add(')
 s=s.replace('net.minecraft.particles.ItemParticleData','net.minecraft.core.particles.ItemParticleOption').replace('net.minecraft.particles.ParticleTypes','net.minecraft.core.particles.ParticleTypes')
 s=s.replace('net.minecraft.state.properties.','net.minecraft.world.level.block.state.properties.')
 if 'newBlockEntity(' in s:
  s=re.sub(r'(extends (?:Block|HorizontalDirectionalBlock))\s*\{',r'\1 implements EntityBlock {',s,count=1)
  if 'import net.minecraft.world.level.block.*;' not in s:s=s.replace('\n','\nimport net.minecraft.world.level.block.EntityBlock;\n',1)
  # Kept only as an internal conditional factory predicate; no obsolete override.
  s=s.replace('@Override public boolean hasTileEntity','public boolean hasTileEntity')
 m=re.search(r'(?:@Override\s+)?public InteractionResult use\(([^)]+)\)\s*\{',s)
 if m:
  start=m.end();i=start;depth=1
  while depth:
   if s[i]=='{':depth+=1
   elif s[i]=='}':depth-=1
   i+=1
  args=[a.strip().split()[-1] for a in m.group(1).split(',')];assert len(args)==6,(p,args)
  state,world,pos,player,hand,hit=args
  parameters=[a.strip() for a in m.group(1).split(',')]
  plain_parameters=','.join(parameters[:4]+parameters[5:])
  empty_call=','.join([player+'.getItemInHand(InteractionHand.MAIN_HAND)',state,world,pos,player,'InteractionHand.MAIN_HAND',hit])
  wrapper='@Override public InteractionResult useWithoutItem('+plain_parameters+'){return useItemOn('+empty_call+').result();}\n'
  body=s[start:i-1].replace('InteractionResult.', 'net.minecraft.world.ItemInteractionResult.')
  body=body.replace('ItemInteractionResult.PASS','ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION')
  body=body.replace('super.use(', 'super.useItemOn(interactionStack,')
  method='@Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack interactionStack,'+m.group(1)+'){'+body+'}'
  s=s[:m.start()]+wrapper+method+s[i:]
 if s!=old:p.write_text(s,encoding='utf-8')
