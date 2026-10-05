from pathlib import Path
p=Path('src/main/java/org/slavicmyths/yaga/YagaHutPlan.java');s=p.read_text();s=s.replace('  for(int y=8;y<=12;y++)b(3,y,3,', '''  // Irregular exterior growth and patched eaves keep the cabin visibly weathered.
  for(int y=5;y<=6;y++){b(-6,y,-4,"vine[east=true]");b(-6,y,4,"vine[east=true]");b(6,y,4,"vine[west=true]");b(-2,y,7,"vine[north=true]");}
  b(-6,8,-5,"spruce_stairs[facing=east]");b(-6,8,4,"spruce_stairs[facing=east]");b(0,11,-4,"spruce_slab");
  for(int y=8;y<=12;y++)b(3,y,3,''');p.write_text(s,'utf-8')
p=Path('docs/ARCHITECTURE.md');s=p.read_text().replace('нет force-load/глобального world-tick обхода.','после размещения восстанавливаются neighbor shapes (дверь, ограда, стекло, лестницы),\nдневные грибы получают podzol. Нет force-load/глобального world-tick обхода.');p.write_text(s,'utf-8')
p=Path('tools/yaga_review_093.py');s=p.read_text().replace("elif 'wormwood' in base or 'mushroom' in base:","elif 'vine' in base:cube(x,y,z,[0,0,0],[2,16,16],'#536044')\n   elif 'wormwood' in base or 'mushroom' in base:");p.write_text(s,'utf-8')
