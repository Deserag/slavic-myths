"""Reproducible source inventory, not a live registry or world/encounter measurement."""
from pathlib import Path
import re,json,csv,subprocess
R=Path(__file__).resolve().parents[1];JAVA=R/'src/main/java/org/slavicmyths';RES=R/'src/main/resources/data/slavicmyths'
OUT=R/'docs/implementation/encounters271-20261025'
def read(p):return p.read_text(encoding='utf-8')
def inventory():
    rows={};dynamic=[]
    for p in JAVA.rglob('*.java'):
        s=read(p)
        for m in re.finditer(r'(?:ModEntities\.)?ENTITIES\.register\(\s*("[^"]+"|\w+)',s):
            token=m.group(1);statement=s[m.start():s.find(';',m.end())]
            if 'EntityType' not in statement and 'EntityType' not in s[max(0,m.start()-250):m.start()]:continue
            if not token.startswith('"'):dynamic.append((p.relative_to(JAVA).as_posix(),token));continue
            category=re.search(r'MobCategory\.(\w+)',statement)
            rows[token.strip('"')]=(category.group(1),p.relative_to(JAVA).as_posix())
    assert set(dynamic)=={('husbandry/Husbandry.java','id'),('industry/ChudPeople.java','name'),('navya/NavyaContent.java','name'),('registry/ModEntities.java','id')},dynamic
    helpers=[('husbandry/Husbandry.java',r'=animal\("([^"]+)"','CREATURE'),('industry/ChudPeople.java',r'=type\("([^"]+)"','CREATURE'),('registry/ModEntities.java',r'= wildlife\("([^"]+)"','CREATURE')]
    for path,pattern,cat in helpers:
        names=re.findall(pattern,read(JAVA/path));assert names,path
        for name in names:rows[name]=(cat,path)
    for name in re.findall(r'Kind\.\w+,"([^"]+)"',read(JAVA/'navya/NavyaContent.java').split('static {')[0]):rows[name]=('AMBIENT' if name=='navya_wisp' else 'MONSTER','navya/NavyaContent.java')
    return rows

def biomes():
    entries={}
    def add(sp,selector,source):
        if sp['type'].startswith('slavicmyths:'):entries.setdefault(sp['type'].split(':')[1],[]).append({'selector':selector,'weight':sp['weight'],'min':sp['minCount'],'max':sp['maxCount'],'source':source})
    for p in (RES/'neoforge/biome_modifier').glob('*.json'):
        d=json.loads(read(p))
        if d.get('type')!='neoforge:add_spawns':continue
        values=d['spawners'];values=[values] if isinstance(values,dict) else values
        for sp in values:add(sp,d['biomes'],p.relative_to(R).as_posix())
    for p in (RES/'worldgen/biome').glob('*.json'):
        for group,values in json.loads(read(p)).get('spawners',{}).items():
            for sp in values:add(sp,'slavicmyths:'+p.stem+'/'+group,p.relative_to(R).as_posix())
    for name,tag,config,field,count in [('nochnitsa','nochnitsa_surface','nochnitsa/NochnitsaConfig.java','spawnWeight',1),('pesigolovets','pesigolovtsy_wilderness','pesigolovtsy/DogheadConfig.java','weight',3)]:
        weight=int(re.search(r'\b'+field+r'\s*=\s*(\d+)',read(JAVA/config)).group(1))
        add({'type':'slavicmyths:'+name,'weight':weight,'minCount':count,'maxCount':count if count==1 else 6},'#slavicmyths:'+tag,'runtime configurable default; '+config)
    return entries

POLICY={}
def group(ids,channel,path,conditions,rng='none in this gate',limits='vanilla category cap/collision; see source'):
    assert (JAVA/path).is_file(),path
    for name in ids.split():assert name not in POLICY,name;POLICY[name]=(channel,path,conditions,rng,limits)

group('brown_bear bear_cub forest_wolf boar stag doe','NATURAL','world/WildlifeSpawns.java','Overworld; biome entries; brightness>7; dirt/moss; Mob spawn rules')
group('goose duck domestic_goat','NATURAL','husbandry/Husbandry.java','Overworld; biome entries; dirt; brightness>8; Mob spawn rules')
group('domovoy','NATURAL','world/SpiritSpawns.java','village; brightness>8; ON_GROUND','1/8','CREATURE cap; home becomes persistent')
group('leshy','NATURAL','world/SpiritSpawns.java','forest entries; not Peaceful; sky; vanilla darkness','1/8')
group('kikimora','NATURAL','world/LandEncounters.java','Overworld; not Peaceful; night; brightness<8','1/12','same class excluded within32; MONSTER cap')
group('poludnitsa','NATURAL','world/LandEncounters.java','Overworld; not Peaceful; time5000..7500; no rain; sky','1/16','same class excluded within48; MONSTER cap')
group('polevik','NATURAL','world/LandEncounters.java','Overworld; not Peaceful; sky','1/16')
group('igosha','NATURAL','world/LandEncounters.java','Overworld; not Peaceful; night; brightness<5','1/40','same class excluded within48; MONSTER cap')
group('ovinnik','NATURAL','hunt/HuntSpawns.java','Overworld; night; not Peaceful; actual1.15x2.45 body; open biome OR hay/campfire/smoker within8','removed by earlier package','same species64; MONSTER cap')
group('volkolak','NATURAL','hunt/HuntSpawns.java','Overworld; night; not Peaceful; forest/taiga; actual1.05x2.25 body','removed by earlier package','same species72; MONSTER cap')
group('fire_serpent','NATURAL','hunt/HuntSpawns.java','Overworld; night; not Peaceful; open OR forest; sky; actual1.25x1.55 body','removed by earlier package','same species96; MONSTER cap')
group('podvey','NATURAL','hunt/HuntSpawns.java','Overworld; night OR rain; not Peaceful; open biome; sky; actual1.1x2.55 body','removed by earlier package','same species96; MONSTER cap')
group('vodyanoy','NATURAL','water/WaterSpawns.java','Overworld; IN_WATER; water at feet and above','1/8')
group('rusalka','NATURAL','water/WaterSpawns.java','Overworld; night; IN_WATER','1/10')
group('bolotnik','NATURAL','water/WaterSpawns.java','swamp; night; not Peaceful; ON_GROUND; Mob rules','1/4')
group('pike carp crayfish','NATURAL','water/WaterSpawns.java','Overworld water; above also water; crayfish solid floor; fish water below')
group('nochnitsa','NATURAL_CUSTOM_MODIFIER','nochnitsa/NochnitsaContent.java','Overworld; night; sky; configured biome tag; vanilla monster rules','vanilla darkness roll','same species48; config NochnitsaConfig.spawnWeight; MONSTER cap')
group('pesigolovets','NATURAL_CUSTOM_MODIFIER','pesigolovtsy/DogheadContent.java','Overworld wilderness; >96from worldspawn; outside village; near heightmap; largest body','day1/40 plus vanilla monster darkness','pack3..6; group finalize rejects pack within96; config DogheadConfig.weight')
group('navka rootling bone_crawler navya_wisp','NATURAL','navya/NavyaSpawns.java','navya_grove Nether; air2; Y33..145; profile strata; ground Mob rules except flying','wisp1/8; other kinds no extra RNG','MONSTER cap; AMBIENT cap for wisp; not Peaceful except wisp')
group('sivka_burka','NATURAL_AND_FIRST_OPPORTUNITY','sivka/SivkaExploration.java','day Overworld; grasslands; actual safe body; existing placement reused; first-opportunity survival only','q(d)=2%..15%; guaranteed pending d40; daily distance>=1000','per-player durable one opportunity; existing horse128 suppresses new; search12columns/server/5s')
group('grey_wolf','EVENT_LOADED_TERRAIN','wolf/WolfEncounter.java','Overworld forest tag; center chunk; safe arena35% columns radius14; exclusions96','35% deterministic regions512','homes768 apart; quota3..12; saved regions; retry600ticks; transient cache8192 evicts')
group('firebird','EVENT_LOADED_TERRAIN','firebird/FirebirdEncounter.java','Overworld survival observer; time13000..22999; safe natural perch; visible aerial spawn32..64away','no start lottery;16candidate columns/100ticks; next visit+2..4days','one world ledger UUID; planned2..3 drops; unloaded UUID not immediately replaced')
group('kot_bayun','FEATURE_AND_EXPLICIT_BOOK_CREATION','bayun/BayunFeature.java','natural bayun_nook biome feature;13x13site; height delta<=4; natural floor; separate on-demand book path','placed_feature rarity1/128 before safety','persistent NPC/home; structure placement repair deferred #275')
group('bannik','FEATURE','world/HomesteadFeature.java','bathhouse generation; barn does NOT spawn guaranteed Ovinnik','feature rarity; not a mob natural weight','persistent home; generation availability #275')
group('elder_vodyanoy','STRUCTURE_RITUAL_ENCOUNTER','depth/PoolStoneTile.java','deep pool stone ritual activation; not independent biome spawn','ritual/site gates','pool encounter identity; generation #275')
group('likho_one_eyed tugarin_zmey','NATURAL_ANCHOR_EVENT','hunt/BossAnchors.java','Overworld biome anchors; Likho night; loaded player distance64/80; clear safe location','deterministic regional anchors; natural Likho Mor12% from day2 >160spawn','saved ledger active UUID;7day respawn; ordinary gameplay unchanged')
group('upyr nav kurgan_druzhinnik kurgan_voevoda buried_volkhv unresting_prince','STRUCTURE_ENCOUNTER','kurgan/KurganEncounters.java','kurgan instance/roster; not independent biome spawn','roster from plan','saved instance spawn/death ledger; generation #275')
group('podzemnik kladovik large_podzemnik','STRUCTURE_ENCOUNTER','underground/EncounterPopulation.java','abandoned/Chud underground chamber plan; safe floor/arena; loaded room','room module variants .25/.4/.6; not natural weight','deterministic UUID and consumed records; generation #275')
group('bandit_fighter bandit_archer bandit_heavy bandit_senior ataman nightingale','STRUCTURE_ENCOUNTER','bandit/CampPiece.java','camp/large camp/woodland structure population; alternate sources in related plans','plan roles; no independent natural biome channel','persistent camp slots; natural structure frequency #275')
group('baba_yaga kuzya','STRUCTURE_NPC','yaga/YagaHut.java','one Yaga hut; Kuzya household NPC; explicit creation via book','no mob spawn lottery','saved world NPC/home; #272/#275; no independent natural weight')
group('chud_resident chud_artisan chud_guard','STRUCTURE_NPC','chud/ChudPiece.java','Chud structure plan residents/artisans/guards','plan roles','structure population records; generation #275')
group('grove_guardian','STRUCTURE_ENCOUNTER','navya/NavyaHeartPiece.java','Nether navya_heart structure encounter','heart structure placement; not biome mob weight','persistent guardian; generation #275/#257')
group('cursed_volkolak','STORY_ENCOUNTER','volkolak/VolkolakStory.java','Grey Wolf story/assigned quest; different from natural volkolak','story gates; not natural RNG','saved encounter; story access depends on Wolf')
group('skim_beast alyosha_popovich','STORY_ENCOUNTER_ALLY','skim/SkimEvents.java','Skim valley/hero story and calls','story gates','separate from ordinary natural mobs; site dependency #275')
group('ilya_muromets idolishche_poganoe','STORY_ENCOUNTER_ALLY','bogatyr/BogatyrLedger.java','Ilya introduction/Idolishche arena and hero calls','story gates','personal ledger; story locations #275')
group('dobrynya_nikitich marinka marinka_tur_minion','STORY_ENCOUNTER_ALLY','marinka/MarinkaEvents.java','Dobrynya introduction/Marinka story; tur summoned by encounter','story gates','personal/world encounter ledgers')
group('finist kartaus','STORY_ENCOUNTER_ALLY','finist/KartausEncounter.java','Finist introduction/help; Kartaus encounter started through story','story gates','encounter identity; not biome natural spawn')
group('kalin_tsar kalin_common_soldier kalin_elite_soldier kalin_war_horse','STORY_ENCOUNTER','kalin/KalinEncounter.java','Kalin campaign battlefield encounter and soldiers','story gates','saved encounter slots; no independent natural weight')
group('morozko morozko_ice_double player_ice_double','RITUAL_TRIAL_AND_SUMMONED','morozko/FrostEvents.java','trial totem/ritual; doubles spawned by trial or ability','trial gates','FrostLedger; not missing natural mobs')
group('karachun karachun_raven','RITUAL_TRIAL_AND_SUMMONED','karachun/NightEvents.java','Night trial ritual; raven companion/projectile support','trial gates','NightLedger; not independent natural weight')
group('great_poloz','STORY_ENCOUNTER','poloz/PolozEvent.java','Poloz ritual/core campaign','ritual gates','separate boss encounter ledger')
group('earth_spirit earth_servant earth_encounter_actor','STRUCTURE_RITUAL_ENCOUNTER','earth/EarthArena.java','earth seal activation, arena actor and summons','ritual/site gates','structure/arena; generation #275')
group('extinguished_flame_guardian defiled_spring_keeper guardian_water_echo','STRUCTURE_RITUAL_ENCOUNTER','guardians/GuardianLedger.java','sanctuary guardian activation/echo','ritual/site gates','SanctuaryRecords/GuardianLedger; #273/#275')
group('zmey_gorynych','STRUCTURE_RITUAL_ENCOUNTER','gorynych/GorynychLedger.java','serpent hill ritual/arena','ritual/site gates','GorynychLedger; #275')
group('grey_wolf_support','SUMMONED_ALLY','quest185/WolfSupport.java','earned Grey Wolf ally support','story gates','temporary ally; not natural grey_wolf')

def main():
    rows=inventory();bio=biomes();unknown=[];result=[]
    for name,(cat,reg) in sorted(rows.items()):
        policy=POLICY.get(name)
        if policy is None and cat=='MISC':policy=('ITEM_PROJECTILE_EFFECT_OR_VEHICLE',reg,'created by owning item/entity/ability; no natural channel','not applicable','not a missing naturally spawning creature')
        if policy is None:unknown.append(name);continue
        channel,path,conditions,rng,limits=policy
        result.append({'ID':'slavicmyths:'+name,'CATEGORY':cat,'REGISTRATION':reg,'CHANNEL':channel,'GATE_SOURCE':path,'BIOME_RAW_ENTRIES':json.dumps(bio.get(name,[]),ensure_ascii=False),'CONDITIONS':conditions,'EXTRA_RNG':rng,'LIMITS_PERSISTENCE':limits,'EXPECTED_FREQUENCY':'NOT_MEASURED; raw weights/conditional probabilities are not encounter rates','RUNTIME':'NOT_RUN'})
    assert not unknown,('Unclassified entity IDs',unknown)
    assert set(POLICY)<=set(rows),('Stale policy IDs',set(POLICY)-set(rows))
    assert set(bio)<=set(rows),('Biome spawn references missing registration',set(bio)-set(rows))
    OUT.mkdir(parents=True,exist_ok=True)
    with (OUT/'all-entities.tsv').open('w',encoding='utf-8',newline='') as f:
        w=csv.DictWriter(f,fieldnames=list(result[0]),delimiter='\t');w.writeheader();w.writerows(result)
    summary={'source_entities':len(rows),'categories':{cat:sum(v[0]==cat for v in rows.values()) for cat in sorted({v[0] for v in rows.values()})},'not_live_registry':True,'runtime_launches':0}
    (OUT/'inventory-summary.json').write_text(json.dumps(summary,indent=2)+'\n')
    tags={p.stem:json.loads(read(p)) for p in (RES/'tags/worldgen/biome').glob('*.json')}
    (OUT/'biome-tags.json').write_text(json.dumps(tags,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('PASS inventory coverage:',summary)
if __name__=='__main__':main()
