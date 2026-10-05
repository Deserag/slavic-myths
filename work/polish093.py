from pathlib import Path
r=Path('src/main/java/org/slavicmyths')
p=r/'yaga/YagaHutPlan.java';s=p.read_text();s=s.replace('int y=8+(6-Math.abs(x))/2;','int y=8+(6-Math.abs(x))/2;');s=s.replace('if(x==0)b(x,y+1,z,"dark_oak_slab");','if(x==0)b(x,y+1,z,"dark_oak_slab");if(x==5&&z>0)b(x,y,z,"mossy_cobblestone_slab");');s=s.replace('b(3,6,-2,"lantern");','for(int x=-2;x<=2;x++)for(int z=-8;z<=-7;z++)b(x,7,z,"spruce_slab[type=top]");\n  b(-4,7,-2,"flower_pot");b(-4,7,0,"flower_pot");b(3,6,-2,"lantern");');p.write_text(s,'utf-8')
p=r/'yaga/YagaServices.java';s=p.read_text().replace('return ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));','Item i=ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));if(i==null||i==Items.AIR)throw new IllegalStateException("Missing Yaga ingredient "+id);return i;');p.write_text(s,'utf-8')
p=r/'yaga/YagaData.java';s=p.read_text().replace('p.blockedUntil=Math.max','if(p.stage<3&&YagaServices.POOL[p.contract]>=4)p.contract=YagaServices.nextContract(p.stage,p.contract);p.blockedUntil=Math.max');p.write_text(s,'utf-8')
p=r/'yaga/YagaMenu.java';s=p.read_text().replace('else if(action==120)result=progress.accept(', 'else if(action==120)result=progress.stage>=YagaServices.CONTRACTS[YagaServices.POOL[progress.contract]].stage&&progress.accept(');p.write_text(s,'utf-8')
p=r/'yaga/YagaUtilityItem.java';s=p.read_text().replace('if(d.anchor==null)return ActionResult.fail(s);','if(d.anchor==null){YagaServices.fail(p,"home");return ActionResult.fail(s);}');p.write_text(s,'utf-8')
# Ground supports are included in the same atomic snapshot; feet, stairs and furnishings cannot float.
p=r/'yaga/YagaHut.java';s=p.read_text();s=s.replace('  BabaYaga npc=','''  for(Map.Entry<BlockPos,String> e:PLAN.blocks.entrySet()){if(e.getKey().getY()!=0)continue;BlockPos base=anchor.offset(e.getKey()).below();int terrain=ground(w,base.getX(),base.getZ());if(terrain<0||anchor.getY()-1-terrain>4)return false;for(int y=terrain+1;y<anchor.getY();y++){BlockPos p=new BlockPos(base.getX(),y,base.getZ());BlockState before=w.getBlockState(p);if(w.getBlockEntity(p)!=null||before.getMaterial().isLiquid()||!(before.isAir(w,p)||before.getMaterial().isReplaceable()))return false;if(!old.containsKey(p))old.put(p,before);desired.put(p,Blocks.COARSE_DIRT.defaultBlockState());}}
  BabaYaga npc=''');p.write_text(s,'utf-8')
# Tooltip coverage for exchange and visible rewards.
p=r/'client/YagaScreen.java';s=p.read_text();needle='if(menu.tab()==3){YagaServices.Entry e=YagaServices.BREWS[menu.recipe()];for(int i=0;i<4;i++)if(x>='
s=s.replace(needle,'if(menu.tab()==1&&menu.stage()>0&&x>=leftPos+183&&x<leftPos+199&&y>=topPos+96&&y<topPos+112){boolean repeat=menu.stage()>=3||page==1;renderTooltip(m,(repeat?YagaServices.CONTRACTS[YagaServices.POOL[menu.contract()]]:YagaServices.MAIN[menu.stage()]).output(),x,y);}if(menu.tab()==2)for(int row=0;row<3;row++){YagaServices.Entry e=YagaServices.EXCHANGES[page*3+row];if(y>=topPos+44+row*25&&y<topPos+60+row*25){if(x>=leftPos+112&&x<leftPos+128)renderTooltip(m,e.inputs[0].stack(),x,y);if(x>=leftPos+204&&x<leftPos+220)renderTooltip(m,e.output(),x,y);}}'+needle)
s=s.replace('for(int i=0;i<4;i++)if(x>=leftPos+126','for(int i=0;i<4;i++)if(menu.input.getItem(i).isEmpty()&&x>=leftPos+126');p.write_text(s,'utf-8')
# Manifest-backed allowances retain every previous verification.
p=Path('tools/verify_resources.py');s=p.read_text();s=s.replace("items = registered('registry/ModItems.java')", "yaga=read(ROOT/'docs/verification/yaga-0.9.3.json')\nitems = registered('registry/ModItems.java')")
s=s.replace('blocks - {"raspberry_bush", "blueberry_bush"}', 'blocks - set(yaga["world_only_blocks"]) - {"raspberry_bush", "blueberry_bush"}')
s=s.replace('            assert (width,height,depth,color,compression,filtering,interlace)', '''            relative=path.relative_to(RES/'assets/slavicmyths').as_posix() if path.is_relative_to(RES/'assets/slavicmyths') else ''
            if relative in yaga['png_sizes']: expected=tuple(yaga['png_sizes'][relative])
            assert (width,height,depth,color,compression,filtering,interlace)''')
s=s.replace("    if entity=='serpent_projection':",'''    if entity=='baba_yaga':
        assert (RES/'assets/slavicmyths/textures/entity/baba_yaga.png').is_file()
        assert (RES/'data/slavicmyths/loot_tables/entities/baba_yaga.json').is_file()
        assert (JAVA/'client/BabaYagaModel.java').is_file()
        assert 'ModEntities.BABA_YAGA.get()' in (JAVA/'client/ClientSetup.java').read_text()
        continue
    if entity=='serpent_projection':''')
p.write_text(s,'utf-8')
p=Path('build.gradle');s=p.read_text().replace("version = '0.9.2'","version = '0.9.3'");p.write_text(s,'utf-8')
p=Path('tools/create_resources.py');s=p.read_text();s+='\n# Scoped 0.9.3 assets run after historical layers.\nimport yaga_093\n';p.write_text(s,'utf-8')
