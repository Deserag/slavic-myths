"""Scoped deterministic L1 authoring. Default is a dry run; --write writes only
the listed new rewards276 tables. Never imports legacy resource/audio generators.
"""
from pathlib import Path
import copy,json,subprocess,sys
R=Path(__file__).resolve().parents[1];L=R/'src/main/resources/data/slavicmyths/loot_table'
O=R/'docs/implementation/loot276-l1-20261026';BASE='fe3d2752318ed40a00fbfdd79cf050a2b3c99c4b'
CRAFT_I=['rune_strength','rune_speed','rune_resilience','rune_heat']
CRAFT_II=['rune_crushing','rune_wind','rune_blood','rune_fortitude']
FRAGMENTS=['rune_flight_fragment','rune_rare_protection_fragment']
ROLES=['burial','common_cache','great_special','ritual','secret','treasury','warrior']
def baseline(key):return json.loads(subprocess.check_output(['git','show',BASE+':src/main/resources/data/slavicmyths/loot_table/'+key+'.json'],cwd=R))
def uniform(a,b):return a if a==b else {'type':'minecraft:uniform','min':a,'max':b}
def item(id,count=(1,1),weight=1,functions=None):
    d={'type':'minecraft:item','name':id if ':' in id else 'slavicmyths:'+id,'weight':weight};fs=[]
    if count!=(1,1):fs.append({'function':'minecraft:set_count','count':uniform(*count)})
    fs.extend(functions or [])
    if fs:d['functions']=fs
    return d
def enchant(name,low,high,probability=1):
    f={'function':'minecraft:set_enchantments','enchantments':{'minecraft:'+name:uniform(low,high)},'add':False}
    if probability<1:f['conditions']=[{'condition':'minecraft:random_chance','chance':probability}]
    return f
def pool(name,entries,probability=1):
    d={'name':'slavicmyths:rewards276/'+name,'rolls':1,'entries':entries}
    if probability<1:d['conditions']=[{'condition':'minecraft:random_chance','chance':probability}]
    return d
def reference(key):return pool('original_once',[{'type':'minecraft:loot_table','value':'slavicmyths:'+key}])
def rune_entries(tier,family):
    # Explicit craftable whitelist, never a loose tag containing III/story runes.
    out=[item(id,weight=3 if tier==1 else 1) for id in CRAFT_I]
    if tier>0 and not (family=='camp' and tier==1):out += [item(id,weight=1 if tier==1 else 2) for id in CRAFT_II]
    return out
def equipment(tier,family):
    book_p=([.10,.20,.30] if family=='kurgan' else [.08,.16,.25])[tier]
    gear_p=([.12,.22,.20] if family=='kurgan' else [.10,.20,.25])[tier]
    trim_p=([.02,.06,.08][tier] if family=='kurgan' else 0)
    entries=[];used=0
    books=['protection','unbreaking','sharpness','efficiency','power','feather_falling']
    # Scale 6000 permits exactly equal book/gear variants and exact percentage
    # mass for rare armor/scrap; set_enchantments turns BOOK into ENCHANTED_BOOK.
    for id in books:entries.append(item('minecraft:book',weight=round(book_p*6000/len(books)),functions=[enchant(id,1,2 if tier<2 else 3)]))
    gears=['iron_sword','iron_axe','iron_helmet','iron_chestplate','iron_leggings','iron_boots']
    for id in gears:
        damage={'function':'minecraft:set_damage','damage':uniform(.35,.85),'conditions':[{'condition':'minecraft:random_chance','chance':.60}]}
        e='sharpness' if id.endswith('sword') else 'efficiency' if id.endswith('axe') else 'protection'
        entries.append(item('minecraft:'+id,weight=round(gear_p*6000/len(gears)),functions=[damage,enchant(e,1,2,.35)]))
    used=sum(e['weight'] for e in entries)
    if trim_p:
        trims=[['sentry'],['host','raiser'],['shaper','wayfinder']][tier]
        for t in trims:entries.append(item('minecraft:'+t+'_armor_trim_smithing_template',weight=round(trim_p*6000/len(trims))))
        used=sum(e['weight'] for e in entries)
    if family=='kurgan' and tier==2:
        entries.append(item('minecraft:netherite_scrap',weight=900));used+=900
        for slot in ['helmet','chestplate','leggings','boots']:
            entries.append(item('minecraft:diamond_'+slot,weight=45,functions=[enchant('protection',2,2)]));used+=45
    entries.append({'type':'minecraft:empty','weight':6000-used})
    assert sum(x['weight'] for x in entries)==6000 and entries[-1]['weight']>=0
    return pool('equipment_one_result',entries)
def material_entries(tier,family):
    if family=='kurgan':
        values=[[("minecraft:iron_ingot",600,(2,4)),("minecraft:gold_ingot",220,(1,2)),("silver_ingot",150,(1,2)),("minecraft:diamond",30,(1,1))],
                [("minecraft:iron_ingot",400,(3,6)),("minecraft:gold_ingot",300,(2,4)),("silver_ingot",220,(1,3)),("minecraft:diamond",80,(1,2))],
                [("minecraft:gold_ingot",500,(3,6)),("silver_ingot",350,(2,4)),("minecraft:diamond",150,(1,2))]][tier]
    else:
        values=[[("minecraft:iron_ingot",650,(1,3)),("minecraft:gold_ingot",150,(1,1)),("silver_ingot",180,(1,1)),("minecraft:diamond",20,(1,1))],
                [("minecraft:iron_ingot",500,(2,4)),("minecraft:gold_ingot",220,(1,2)),("silver_ingot",220,(1,2)),("minecraft:diamond",60,(1,1))],
                [("minecraft:gold_ingot",380,(1,3)),("silver_ingot",500,(1,2)),("minecraft:diamond",120,(1,2))]][tier]
    assert sum(w for id,w,c in values)==1000
    return [item(id,c,w) for id,w,c in values]
def main_pools(tier,family,large_rune_chance):
    rune_p=([.08,.25,.45] if family=='kurgan' else [.05,.20,large_rune_chance])[tier]
    perun_p=([0,.12,.30] if family=='kurgan' else [0,.08,.25])[tier]
    frag_p=([.04,.10,.18] if family=='kurgan' else [.03,.08,.12])[tier]
    p=[pool('materials_one',material_entries(tier,family)),pool('craftable_rune',rune_entries(tier,family),rune_p)]
    if perun_p:p.append(pool('perunite',[item('perunite',(1,2))],perun_p))
    p.append(pool('fragment',[item(id) for id in FRAGMENTS],frag_p));p.append(equipment(tier,family))
    if family=='camp':
        p.append(pool('supplies',[item('organic_fertilizer',(1,3),3),item('minecraft:cooked_beef',(2,4),3),item('minecraft:torch',(4,8),3)]+([item('minecraft:golden_apple',weight=1)] if tier>0 else []),[.30,.45,.50][tier]))
    return p
def tables():
    # Optional cache=35%, with independent rune pools20% and6%; nightingale18%.
    # Whole-site probability of no existing rune = .82*(.65+.35*.8*.94).
    old_no_rune=.82*(.65+.35*.8*.94)
    large_rune_chance=1-.65/old_no_rune
    out={}
    for tier in range(3):
        out[f'rewards276/kurgan_{tier+1}_main']={'type':'minecraft:chest','pools':main_pools(tier,'kurgan',large_rune_chance)}
    # Preserve all seven legacy resource tables; route NEW tierIII assignments to
    # these scoped copies, retaining exact weights/counts with gold replacing iron.
    for role in ROLES:
        d=baseline('kurgan/'+role)
        for p in d['pools']:
            for e in p['entries']:
                if e.get('name')=='minecraft:iron_nugget':e['name']='minecraft:gold_nugget'
                elif e.get('name')=='minecraft:iron_ingot':e['name']='minecraft:gold_ingot'
        out['rewards276/kurgan_3_'+role]=d
    for tier in range(3):
        base='chests/bandit_'+('small' if tier==0 else 'medium') if tier<2 else 'chests/stronghold_ataman'
        out[f'rewards276/camp_{tier+1}_main']={'type':'minecraft:chest','pools':[reference(base),*main_pools(tier,'camp',large_rune_chance)]}
    # Existing cache probability/coin/material/gear pools remain unchanged.
    # Substitute only ambiguous legacy whole runes with known craftable tierI.
    d=baseline('chests/stronghold_cache')
    for p in d['pools']:
        names=[e.get('name','') for e in p['entries']]
        if any(n.startswith('slavicmyths:rune_') for n in names):
            p['entries']=[item(id) for id in CRAFT_I]
    out['rewards276/camp_3_cache']=d
    return out,dict(large_existing_rune_probability=1-old_no_rune,large_supplemental_rune_probability=large_rune_chance,large_target_rune_probability=1-old_no_rune*(1-large_rune_chance),capacity='kurgan main max5 nonempty stacks; ordinary roles and camp containers unchanged')
def generate(write=False):
    out,receipt=tables();O.mkdir(parents=True,exist_ok=True)
    (O/'profiles.json').write_text(json.dumps(dict(baseline=BASE,**receipt,tables=out),ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    if write:
        for key,d in out.items():
            p=L/(key+'.json');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    print(json.dumps(dict(mode='WRITE_SCOPED_NEW_TABLES' if write else 'DRY_RUN',table_count=len(out),**receipt)))
    return out
if __name__=='__main__':generate('--write' in sys.argv)
