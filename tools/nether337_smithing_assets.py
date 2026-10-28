"""Exact documented crafting grids and derived diamond-form assets, not original art."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,zipfile
from nether324_assets import R,A,D,write
NAMES={
 'nether_hardening_template':('Шаблон адской закалки','Nether Hardening Template'),
 'zhara_plate':('Жаровая пластина','Zhara Plate'),
 'four_faces_crown_core':('Венец четырёх ликов','Four Faces Crown Core'),
 'infernal_armor_heart':('Сердцевина пекельного доспеха','Infernal Armor Heart'),
 'navya_armor_weave':('Плетение Навьей чащи','Navya Grove Weave'),
 'blazing_step_seal':('Печать пылающей поступи','Blazing Step Seal'),
 'pekelnik_ember':('Тлеющий уголёк Пекельника','Pekelnik Ember'),
 'navya_fork_core':('Сердцевина Навьей остроги','Navya Fork Core'),
 'ashen_blade_core':('Сердцевина пепельного клинка','Ashen Blade Core'),
 'shadow_knife_core':('Сердцевина теневого ножа','Shadow Knife Core'),
 'volcanic_flail_core':('Сердцевина вулканического кистеня','Volcanic Flail Core'),
 'infernal_bow_core':('Сердцевина адского лука','Infernal Bow Core')}
FORMS={'sword':('Жаровой меч','Zhara Sword'),'axe':('Жаровой топор','Zhara Axe'),'spear':('Жаровое копьё','Zhara Spear'),'sulitsa':('Жаровая сулица','Zhara Sulitsa'),'throwing_knife':('Жаровой метательный нож','Zhara Throwing Knife'),'flail':('Жаровой кистень','Zhara Flail')}
def ember_pool():return {'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':.65}],'entries':[{'type':'minecraft:item','name':'slavicmyths:pekelnik_ember','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':2}}]}]}
def apply_ember(data):
 data['pools']=[p for p in data['pools'] if 'slavicmyths:pekelnik_ember' not in json.dumps(p)];data['pools'].append(ember_pool());return data
def recipe(name,pattern,key,count=1):
 write(D/'recipe'/Path(name+'.json'),{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:{'item':v if ':' in v else 'slavicmyths:'+v} for k,v in key.items()},'result':{'id':'slavicmyths:'+name,'count':count}})
 first=next(iter(key.values()));first=first if ':' in first else 'slavicmyths:'+first
 unlock(name,first)
def unlock(name,first):write(D/'advancement/recipes/nether_smithing'/Path(name+'.json'),{'parent':'minecraft:recipes/root','criteria':{'material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':first}]}}},'requirements':[['material']],'rewards':{'recipes':['slavicmyths:'+name]}})
def icon(name,i):
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);dark='#33282b';gold='#d8ad5c';hot='#f49a32';bone='#d6d2b0';cold='#55c9be'
 if i==0:
  d.rectangle((4,1,11,14),fill=dark,outline='#736a63');d.rectangle((6,2,9,4),fill=bone);d.line([(5,7),(8,11),(10,7)],fill=hot,width=2)
 elif i==1:
  d.polygon([(4,2),(11,2),(14,6),(12,12),(7,14),(2,10),(2,5)],fill=dark,outline='#79514a');d.line([(4,4),(7,7),(4,11)],fill=hot);d.line([(10,4),(8,8),(11,11)],fill='#df5327')
 elif i==2:
  d.arc((1,3,14,14),180,360,fill=bone,width=2);d.rectangle((2,10,13,12),fill='#6d6564');d.line([(8,4),(8,11)],fill='#f15e35',width=2)
  for x,c in zip([2,5,10,13],['#65d4dd','#ee584e','#80c15b','#b980df']):d.rectangle((x-1,6,x,8),fill=c)
 elif i==3:
  d.polygon([(8,1),(12,5),(11,10),(8,14),(4,10),(3,5)],fill='#e45128',outline=gold)
  for y in [4,8,11]:d.line([(2,y),(5,y+1),(11,y+1),(14,y)],fill=gold,width=2)
 elif i==4:
  d.ellipse((1,2,14,14),outline='#79593a',width=2);d.arc((3,1,12,12),20,300,fill='#4b3f2e',width=2);d.polygon([(8,4),(11,8),(8,11),(5,8)],fill=bone);d.point((8,6),fill=cold)
 elif i==5:
  d.ellipse((1,1,14,14),fill=dark,outline='#9e5d3d');d.line([(7,4),(6,9),(10,11)],fill=hot,width=2);d.rectangle((9,4,10,7),fill=hot)
 elif i==6:
  d.polygon([(5,2),(11,3),(13,9),(9,14),(3,12),(2,6)],fill='#302d30',outline='#686064');d.point((6,5),fill=hot);d.rectangle((9,8,10,9),fill='#ed5c2d');d.point((5,11),fill='#aaa098')
 elif i==7:
  d.line([(4,2),(4,8),(8,10),(12,8),(12,2)],fill=bone,width=2);d.line([(8,9),(8,14)],fill=cold,width=2);d.arc((2,8,13,15),0,180,fill='#69543e')
 elif i==8:
  d.polygon([(8,1),(10,5),(10,11),(6,11),(6,5)],fill='#4d4543',outline=gold);d.line([(8,3),(8,10)],fill=hot);d.line([(3,11),(12,11)],fill=gold,width=2);d.rectangle((7,12,8,14),fill='#78513c')
 elif i==9:
  d.polygon([(8,1),(13,8),(8,14),(3,8)],fill='#252637',outline=cold)
  for x,y,c in [(7,4,'#6178ec'),(9,6,'#48c8e7'),(7,8,'#bb66df'),(9,10,'#e16bad'),(7,11,'#cfb0e8')]:d.point((x,y),fill=c)
 elif i==10:
  d.ellipse((5,1,10,5),outline=gold);d.ellipse((2,5,13,14),fill='#373032',outline='#8b6250');d.line([(6,6),(8,9),(5,12)],fill=hot);d.line([(10,7),(9,12)],fill='#e6532e')
 else:
  d.arc((1,1,13,14),-80,80,fill='#97653d',width=2);d.line([(7,1),(11,4)],fill=bone,width=2);d.line([(11,11),(7,14)],fill=bone,width=2);d.line([(7,1),(7,14)],fill='#b6b2a1');d.rectangle((10,6,13,9),fill=gold);d.point((11,7),fill=hot)
 im.save(A/'textures/item'/Path(name+'.png'))
def recolor(im):
 im=im.convert('RGBA');p=im.load()
 for y in range(im.height):
  for x in range(im.width):
   r,g,b,a=p[x,y]
   if a and g>r*1.13 and b>r*1.12:
    lum=(r+g+b)//3;vein=(x*3+y*5)%13<3
    p[x,y]=(min(255,lum+85),max(55,lum-15),29,a) if vein else (max(30,lum//3),max(22,lum//5),max(21,lum//6),a)
 return im
def main():
 for i,name in enumerate(NAMES):icon(name,i);write(A/'models/item'/Path(name+'.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
 recipe('nether_hardening_template',['QNQ','PIP','PSP'],{'Q':'minecraft:quartz','N':'minecraft:nether_brick','P':'minecraft:paper','I':'roasted_infernal_shard','S':'sulfur'},4)
 recipe('zhara_plate',['LIL','IAI',' G '],{'L':'roasted_livingstone','I':'roasted_infernal_shard','A':'alatyr_ingot','G':'minecraft:gold_ingot'})
 recipe('four_faces_crown_core',['LHL','TAT','QRQ'],{'L':'lightvein_shard','H':'posten_heart','T':'lost_whisper_thread','A':'alatyr_ingot','Q':'minecraft:quartz','R':'roasted_runnik'})
 recipe('infernal_armor_heart',['CHC','MAM','IRI'],{'C':'magma_core','H':'magma_lord_heart','M':'magma_clot','A':'alatyr_ingot','I':'roasted_infernal_shard','R':'magma_rune_fragment'})
 recipe('navya_armor_weave',['LHL','BAB','ITI'],{'L':'living_root','H':'grove_core','B':'guardian_branch','A':'alatyr_ingot','I':'roasted_livingstone','T':'bone_thorn'})
 recipe('blazing_step_seal',['ESE','IAI','MLM'],{'E':'pekelnik_ember','S':'zharenitsa_heat_seal','I':'roasted_infernal_shard','A':'alatyr_ingot','M':'minecraft:magma_cream','L':'roasted_livingstone'})
 recipe('navya_fork_core',['LBL','TRT','IAQ'],{'L':'living_root','B':'guardian_branch','T':'bone_thorn','R':'roasted_runnik','I':'roasted_livingstone','A':'alatyr_ingot','Q':'minecraft:quartz'})
 recipe('ashen_blade_core',['BSB','EAE','III'],{'B':'minecraft:blaze_rod','S':'zharenitsa_heat_seal','E':'pekelnik_ember','A':'alatyr_ingot','I':'roasted_infernal_shard'})
 recipe('shadow_knife_core',['TLT','LRL','QAQ'],{'T':'lost_whisper_thread','L':'lightvein_shard','R':'roasted_runnik','A':'alatyr_ingot','Q':'minecraft:quartz'})
 recipe('volcanic_flail_core',['CKC','IAI','MRM'],{'C':'crab_claw','K':'magma_core','I':'roasted_infernal_shard','A':'alatyr_ingot','M':'magma_clot','R':'magma_rune_fragment'})
 recipe('infernal_bow_core',['SBS','RAR','DLD'],{'S':'salamander_hide','B':'minecraft:blaze_rod','R':'magma_rune_fragment','A':'alatyr_ingot','D':'minecraft:diamond','L':'roasted_alatyrite'})
 # Reuse exact existing diamond geometry and hand transforms; retain all existing IDs/assets.
 atlas=A/'textures/item/weapons87_materials.png';recolor(Image.open(atlas)).save(A/'textures/item/nethersmithing_materials.png')
 jars=list((R/'build/moddev/artifacts').glob('*client-extra*jar'));assert jars,'Need installed Minecraft texture resources'
 with zipfile.ZipFile(jars[0]) as vanilla:
  import io
  for form in FORMS:
   name='zhara_'+form;source='diamond_'+form
   if form in ['sword','axe']:
    im=Image.open(io.BytesIO(vanilla.read('assets/minecraft/textures/item/'+source+'.png')));write(A/'models/item'/Path(name+'.json'),{'parent':'minecraft:item/handheld','textures':{'layer0':'slavicmyths:item/'+name}})
   else:
    im=Image.open(A/'textures/item'/Path(source+'.png'))
    for suffix in ['', '_held','_icon']+(['_handle87','_ball87'] if form=='flail' else []):
     p=A/'models/item'/Path(source+suffix+'.json');s=p.read_text(encoding='utf-8').replace(source,name).replace('slavicmyths:item/weapons87_materials','slavicmyths:item/nethersmithing_materials');write(A/'models/item'/Path(name+suffix+'.json'),json.loads(s))
   recolor(im).save(A/'textures/item'/Path(name+'.png'))
   base='minecraft:'+source if form in ['sword','axe'] else 'slavicmyths:'+source
   write(D/'recipe'/Path(name+'.json'),{'type':'minecraft:smithing_transform','template':{'item':'slavicmyths:nether_hardening_template'},'base':{'item':base},'addition':{'item':'slavicmyths:zhara_plate'},'result':{'id':'slavicmyths:'+name,'count':1}});unlock(name,base)
 p=D/'loot_table/entities/pekelnik.json';write(p,apply_ember(json.loads(p.read_text(encoding='utf-8'))))
 for index,code in enumerate(['ru_ru','en_us']):
  p=A/'lang'/Path(code+'.json');lang=json.loads(p.read_text(encoding='utf-8'))
  for name,values in NAMES.items():lang['item.slavicmyths.'+name]=values[index];lang['nethersmithing.material.'+name]=('Кузнечный материал; часть сложного снаряжения ещё в разработке.' if index==0 else 'Smithing material; some advanced equipment is still in development.')
  for form,values in FORMS.items():lang['item.slavicmyths.zhara_'+form]=values[index]
  for key,ru,en in [('applies','Алмазное оружие соответствующей формы','Matching diamond weapons'),('ingredients','Жаровая пластина','Zhara Plate'),('upgrade','Адская закалка','Nether Hardening'),('base','Поместите соответствующее алмазное оружие','Place the matching diamond weapon'),('addition','Поместите Жаровую пластину','Place a Zhara Plate')]:lang['nethersmithing.'+key]=ru if index==0 else en
  write(p,lang)
 p=A/'creative_catalog.tsv';s=p.read_text(encoding='utf-8');known={v.split('\t')[0] for v in s.splitlines()};s+=''.join(n+'\tresources\tnether_smithing\n' for n in NAMES if n not in known);s+=''.join('zhara_'+n+'\tweapons\tnether_smithing\n' for n in FORMS if 'zhara_'+n not in known);p.write_text(s,encoding='utf-8')
 print('Smithing material grids + six diamond-form upgrades generated; five special/armor ART_BLOCKED, not substituted.')
if __name__=='__main__':main()
