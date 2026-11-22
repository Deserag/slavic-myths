"""Rebuild marine resources from vanilla pixels; never change their alpha masks or UV layouts."""
import io,json,zipfile,pathlib
from PIL import Image
from ocean519_recolor import colorize,find_vanilla_jar,SOURCES
ROOT=pathlib.Path('src/main/resources/assets/slavicmyths')
navy=(45,116,145); turquoise=(47,187,186); bronze=(187,151,85)
jobs={}
for name,base in [('tide_helmet','diamond_helmet'),('abyss_chestplate','diamond_chestplate'),('storm_leggings','diamond_leggings'),('waterwalk_boots','diamond_boots'),('pharaohka_trident','trident'),('zhelva_shield','turtle_scute')]:
    jobs[f'item/marine/{name}']=(f'item/{base}',navy)
for layer in (1,2): jobs[f'armor/marine/marine_armor_layer_{layer}']=(f'models/armor/diamond_layer_{layer}',navy)
for name,tint in [('pharaohka_trident',turquoise),('storm_harpoon',navy)]: jobs[f'entity/marine/{name}']=('entity/trident',tint)
jobs['entity/marine/zhelva_shield']=('entity/shield_base_nopattern',turquoise)
for name,base,tint in [('abyss_staff','blaze_rod',navy),('storm_harpoon','trident',navy),('storm_anchor','fishing_rod',navy),('storm_anchor_cast','fishing_rod_cast',navy),('tide_ring','gold_nugget',turquoise),('abyss_necklace','nautilus_shell',navy),('storm_bracelet','iron_nugget',navy),('tailwind_feather','feather',turquoise),('tsar_pike_belt','lead',bronze),('zhelva_amulet','turtle_scute',turquoise),('marine_compass','compass_16',navy),('depths_lantern','lantern',turquoise),('breathing_bowl','bowl',turquoise)]:
    jobs[f'item/ocean/{name}']=(f'item/{base}',tint)
for name,(base,tint) in SOURCES.items(): jobs[f'item/ocean519/{name}']=(f'item/{base}',tint)
for name,base,tint in [('abyss_elixir','potion',navy),('storm_tincture','potion',bronze),('tsar_pike_meat','cod',turquoise),('rune_tide','amethyst_shard',turquoise),('rune_abyss','amethyst_shard',navy),('rune_storm','amethyst_shard',bronze)]:
    jobs[f'item/marine524/{name}']=(f'item/{base}',tint)
with zipfile.ZipFile(find_vanilla_jar()) as z:
    for out,(base,tint) in jobs.items():
        source=Image.open(io.BytesIO(z.read(f'assets/minecraft/textures/{base}.png'))).convert('RGBA')
        result=colorize(source,tint)
        if out=='entity/marine/zhelva_shield':
            for y in range(2,22):
                for x in range(2,14):
                    q=((x-7.5)/5.5)**2+((y-11.5)/9.5)**2
                    if 0.72<q<1.15 and source.getpixel((x,y))[3]:
                        r,g,b,a=result.getpixel((x,y)); result.putpixel((x,y),(min(255,r+80),min(255,g+25),max(0,b-35),a))
        assert source.size==result.size and source.getchannel('A').tobytes()==result.getchannel('A').tobytes(),out
        path=ROOT/'textures'/f'{out}.png';path.parent.mkdir(parents=True,exist_ok=True);result.save(path)
for name,icon in [('pharaohka_trident','marine/pharaohka_trident'),('storm_harpoon','ocean/storm_harpoon')]:
    flat={'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{icon}'}}
    model={'loader':'neoforge:separate_transforms','base':{'parent':'minecraft:item/trident_in_hand','textures':{'particle':f'slavicmyths:item/{icon}'}},'perspectives':{p:flat for p in ['gui','ground','fixed']},'overrides':[{'predicate':{'throwing':1},'model':f'slavicmyths:item/{name}_throwing'}]}
    (ROOT/'models/item'/f'{name}.json').write_text(json.dumps(model,indent=2)+'\n',encoding='utf-8')
print(f'PASS: {len(jobs)} vanilla-derived textures, identical source alpha and dimensions')
import marine_armor_design
