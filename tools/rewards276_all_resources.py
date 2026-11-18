"""Scoped authoring for the owner-delegated overworld reward pass.
No existing tables, coins, trades, story rewards or L1 tables are rewritten.
"""
from pathlib import Path
import copy,json,subprocess,sys,argparse
from loot276_l1_resources import item,pool,enchant,uniform,reference,CRAFT_I,CRAFT_II
R=Path(__file__).resolve().parents[1];D=R/'src/main/resources/data/slavicmyths';L=D/'loot_table';O=R/'docs/implementation/loot276-all-20261026'
BASE='5ffe64764cb10c80385de10cd8eed50836911947';PREFIX='rewards276/overworld/'
III=['rune_counterstrike_iii','rune_hunter_iii','rune_last_stand_iii','rune_depths_iii']
IV=['rune_unbreakable_iv','rune_perun_rage_iv','rune_forest_spirit_iv','rune_mastery_iv']
POLOZ=['poloz_helmet','poloz_chestplate','poloz_leggings','poloz_boots']
def original(key):return json.loads(subprocess.check_output(['git','show',BASE+':src/main/resources/data/slavicmyths/loot_table/'+key+'.json'],cwd=R))
def table(pools):return {'type':'minecraft:chest','pools':pools}
def book(prob=1,levels=(1,3),count=(1,1)):
    out=pool('book',[item('minecraft:book',count,functions=[enchant(e,*levels)]) for e in ['protection','unbreaking','sharpness','efficiency','power','feather_falling']],prob)
    return out
def runes(prob=1,ids=None,rolls=1):
    p=pool('runes',[item(id) for id in (ids or CRAFT_II)],prob);p['rolls']=rolls;return p
def sanctuary_tables():
    out={}
    for family in ['fire','spring']:
        spring=family=='spring'
        # No cheap filler pool. Each normal container helps the existing ritual,
        # plus one modest metal/alchemical ingredient. Main cache stays locked.
        p=[pool('ritual_supply',[item('purified_water_bottle',(1,2))] if spring else [item('sacred_ember')]),
           pool('useful_material',[item('silver_ingot',(1,3),3),item('minecraft:gold_ingot',(1,3),2),item('ancient_rune_fragment',weight=2),item('pure_spring_drop' if spring else 'purifying_ash',weight=3)])]
        if spring:p.append(pool('pool_cleaning',[item('cleansing_powder')]))
        else:p.append(pool('fire_protection',[item('fading_flame_charm')],.35))
        out[PREFIX+family+'_regular']=table(p)
        if not spring:out[PREFIX+'fire_guaranteed']=table([*copy.deepcopy(p),pool('ignition_never_locked',[item('sacred_ember')])])
        weapons=['pool_spear','silver_spear','perunite_sword'] if spring else ['thunder_axe','storm_staff','perunite_sword']
        main=[pool('theme',[item('pure_spring_drop' if spring else 'sacred_ember',(2,4))]),
              pool('purification',[item('purification_stone' if spring else 'purifying_ash')]),
              pool('legacy_cache_component',[item('spring_tear' if spring else 'flame_vial')]),
              pool('rune_fragments',[item('ancient_rune_fragment')]),
              pool('perunite',[item('perunite',(2,3))]),pool('diamonds',[item('minecraft:diamond',(1,3))]),
              pool('silver',[item('silver_ingot',(3,6))]),
              pool('craftable',[*[item(id,weight=1) for id in CRAFT_I],*[item(id,weight=3) for id in CRAFT_II]]),
              runes(.25),book(.65),pool('weapon',[item(id) for id in weapons],.35)]
        out[PREFIX+family+'_main']=table(main)
    return out
def fortress_tables():
    # Six original chests retain their old unique steel/crossbow pool exactly
    # once each. Five authored but previously empty barrels get supplies only.
    ordinary=[reference('chests/serpent_fortress'),pool('fortress_material',[item('perunite',(1,2),2),item('silver_ingot',(2,4),5),item('minecraft:gold_ingot',(2,4),5),item('ancient_rune_fragment',(1,2),3)]),pool('purification',[item('cleansing_powder'),item('purified_water_bottle',(1,2))],.5)]
    main=[reference('chests/serpent_fortress'),pool('poloz_guaranteed',[item(id) for id in POLOZ]),pool('poloz_second',[item(id) for id in POLOZ],.08),
          pool('perunite',[item('perunite',(3,6))]),pool('diamond',[item('minecraft:diamond',(2,4))]),
          pool('steel',[item('ancient_serpent_steel',(2,4))]),runes(ids=CRAFT_II,rolls=2),runes(.18,III),runes(.03,IV),
          book(count=(1,1)),book(.5),pool('weapon',[item('perunite_sword'),item('thunder_axe'),item('storm_staff')],.40),
          pool('likho_weapon',[item('likho_staff')],.12),pool('frost_relic',[item('frost_mirror')],.01)]
    supply=[pool('food',[item('minecraft:cooked_beef',(2,5)),item('karavai',(1,2)),item('smoked_carp',(2,4))]),
            pool('alchemy',[item('cleansing_powder'),item('purified_water_bottle',(1,2)),item('purification_stone')]),
            pool('work',[item('purified_iron_ingot',(1,2)),item('silver_ingot',(1,2)),item('minecraft:gold_ingot',(1,2))],.5)]
    return {PREFIX+'fortress_main':table(main),PREFIX+'fortress_cache':table(ordinary),PREFIX+'fortress_supply':table(supply)}
def swamp_selectors():
    data=json.loads((O/'template-inventory.json').read_text(encoding='utf8'));out={}
    for full,v in data.items():
        if not full.startswith('swamp/'):continue
        name=full.split('/',1)[1]
        if not name.startswith(('v097_','ruin_')):continue
        loot=[x for x in v['contents'] if (x.get('marker') or '').startswith('loot:')]
        if not loot:continue
        if name.startswith('v097_cluster_'):kind='great';preferred='swamp_great_shrine'
        elif name.startswith('v097_shrine_'):kind='ritual';preferred='swamp_ritual'
        elif name.startswith('ruin_'):kind='ruins';preferred='swamp_ruins'
        elif name.startswith('v097_watchtower_'):kind='watch';preferred='swamp_watchtower'
        elif name.startswith(('v097_fisher_','v097_landing_')):kind='fisher';preferred='swamp_fisher'
        elif name.startswith('v097_abandoned_'):kind='home';preferred='swamp_hidden'
        else:continue # boardwalk supplies already match its small peaceful role
        picked=min((x for x in loot if x['marker']=='loot:'+preferred),key=lambda x:tuple(x['pos']))
        out[name]=dict(kind=kind,base='chests/'+preferred,pos=picked['pos'])
    return out
def swamp_minor_tables():
    out={};selectors=swamp_selectors()
    for kind in ['home','fisher','watch','ruins','ritual','great']:
        base=next(v['base'] for v in selectors.values() if v['kind']==kind)
        pools=[reference(base)]
        if kind in ['home','fisher','watch']:
            pools.append(pool('useful_supply',[item('organic_fertilizer',(1,3)),item('smoked_carp',(2,4)),item('minecraft:cooked_beef',(2,4))]))
            if kind=='fisher':pools.append(pool('working_net',[item('fishing_net')],.10))
            if kind=='watch':pools.append(book(.06,levels=(1,2)))
        else:
            tier={'ruins':0,'ritual':1,'great':2}[kind]
            pools += [pool('water_alchemy',[item('purified_water_bottle',(1,2)),item('purification_stone'),item('pearl_fragment',(1,2)),item('silver_ingot',(1,3))]),
                      runes([.15,.18,.25][tier],CRAFT_I if tier==0 else CRAFT_I+CRAFT_II),
                      book([.10,.15,.25][tier],levels=(1,2)),pool('perunite',[item('perunite',(1,2))],[.04,.06,.10][tier])]
            if tier==2:pools.append(pool('water_weapon',[item('pool_spear'),item('silver_spear')],.20))
        out[PREFIX+'swamp_'+kind+'_main']=table(pools)
    out[PREFIX+'ancient_shrine']=table([reference('chests/ancient_shrine'),pool('ritual_use',[item('ritual_charcoal',(2,4),4),item('purifying_ash',weight=2),item('thunder_stone'),item('warding_charm')],.35),runes(.08,CRAFT_I),book(.05,levels=(1,1))])
    # Exact original dynamic reward distribution, now a normal loot reference
    # only for a newly created second chest. Starter/dynamic coins unchanged.
    old=[pool('old_gold',[item('minecraft:gold_ingot',(2,4)),item('minecraft:gold_nugget',(5,9))]),
         pool('old_amethyst',[item('minecraft:amethyst_shard',(2,4))]),pool('old_iron',[item('minecraft:iron_ingot',(1,2))]),pool('old_coin',[item('ancient_coin')],.3)]
    out[PREFIX+'depths_main']=table([*old,runes(.20,CRAFT_I),book(.15,levels=(1,2)),pool('silver',[item('silver_ingot',(1,2))],.25),pool('perunite',[item('perunite',(1,2))],.08)])
    return out
def all_tables():return {**sanctuary_tables(),**fortress_tables(),**swamp_minor_tables()}
def generate(group,write=False):
    groups={'sanctuary':sanctuary_tables,'fortress':fortress_tables,'swamp':swamp_minor_tables,'all':all_tables};out=groups[group]()
    if write:
        for key,d in out.items():
            p=L/(key+'.json');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    (O/'world-profiles.json').write_text(json.dumps(all_tables(),ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    (O/'swamp-selectors.json').write_text(json.dumps(swamp_selectors(),indent=2)+'\n',encoding='utf8')
    print('Scoped rewards276 group='+group+' tables='+str(len(out))+' write='+str(write))
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--group',choices=['sanctuary','fortress','swamp','all'],default='all');p.add_argument('--write',action='store_true');a=p.parse_args();generate(a.group,a.write)
