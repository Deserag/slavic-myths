from pathlib import Path
p=Path('src/main/java/org/slavicmyths/kurgan/KurganDungeonPiece.java');s=p.read_text(encoding='utf-8-sig')
s=s.replace('earthwork.place(w,origin,clip);','earthwork.place(w,origin,clip,plan);')
a=s.index('int patch=Math.floorMod',s.index('private void mound'))
b=s.index('\n        }',a)
s=s[:a]+'''int slope=q>.2?2:1;
            Block surface=KurganSurface.block(w,origin,origin.offset(x,top,z),plan.seed,r,plan.tier,slope,true);
            Block side=KurganSurface.block(w,origin,origin.offset(x,top,z),plan.seed,r,plan.tier,slope,false);
            for(int y=ground;y<=top;y++)put(w,clip,x,y,z,(y==top?surface:y>=top-2?side:Blocks.DIRT).defaultBlockState());''' +s[b:]
p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/kurgan/KurganEarthwork.java');s=p.read_text(encoding='utf-8-sig')
s=s.replace('public void place(WorldGenLevel world,BlockPos origin,BoundingBox clip){','public void place(WorldGenLevel world,BlockPos origin,BoundingBox clip){place(world,origin,clip,null);}\n    public void place(WorldGenLevel world,BlockPos origin,BoundingBox clip,KurganPlan plan){')
s=s.replace('int index=x*depth+z;\n                int actualFloor', '''int index=x*depth+z;
                int skin=tops[index];for(int[] d:new int[][]{{-1,0},{1,0},{0,-1},{0,1}}){int nx=x+d[0],nz=z+d[1];if(nx>=0&&nx<width&&nz>=0&&nz<depth)skin=Math.min(skin,tops[nx*depth+nz]);}
                int actualFloor''')
s=s.replace('(y>tops[index]?Blocks.AIR:y==tops[index]?Blocks.GRASS_BLOCK:y<tops[index]-4?Blocks.STONE:Blocks.DIRT)', '(y>tops[index]?Blocks.AIR:plan!=null&&y>=skin?KurganSurface.block(world,origin,pos,plan.seed,plan.radius,plan.tier,tops[index]-skin,y==tops[index]):y==tops[index]?Blocks.GRASS_BLOCK:y<tops[index]-4?Blocks.STONE:Blocks.DIRT)')
p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/worldgen/ManualStructureJobs.java');s=p.read_text(encoding='utf-8-sig')
s=s.replace('if(!old.equals(next)){world.setBlock(pos,next,2);changed++;}', '''if(kurgan!=null&&voxelY<=top[column]){int skin=top[column];for(int neighbor:new int[]{column-depth,column+depth,column-1,column+1})if(neighbor>=0&&neighbor<top.length&&Math.abs(x(neighbor)-x)+Math.abs(z(neighbor)-z)==1)skin=Math.min(skin,top[neighbor]);if(voxelY>=skin)next=KurganSurface.block(world,anchor,pos,kurgan.seed,kurgan.radius,tier,top[column]-skin,voxelY==top[column]).defaultBlockState();}if(!old.equals(next)){world.setBlock(pos,next,2);changed++;}''')
p.write_text(s,encoding='utf-8')
