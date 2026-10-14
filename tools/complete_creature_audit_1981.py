"""Continue the existing audit; writes documentation only, never regenerates resources.

Evidence excerpts are reused. Missing lines are fetched from the named file only.
UNVERIFIED is intentional: this script does not interpret Java or certify gameplay.
"""
from pathlib import Path
import csv, json, re, hashlib

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/audit/creatures-1.9.8.1'
INDEX=json.loads((OUT/'registry-source-index.json').read_text(encoding='utf-8'))
E={}
for block in re.split(r'^## ',(OUT/'SOURCE_EVIDENCE.md').read_text(encoding='utf-8'),flags=re.M)[1:]:
    path=block.splitlines()[0]
    E[path]=[(int(m[1]),m[2]) for line in block.splitlines() if (m:=re.match(r'(\d+): (.*)',line))]
CACHE={}
def full(path):
    if path not in CACHE: CACHE[path]=(ROOT/path).read_text(encoding='utf-8').splitlines()
    return CACHE[path]
def path(short): return 'src/main/java/org/slavicmyths/'+short+'.java'
def ref(short,token):
    p=path(short)
    found=[n for n,s in E.get(p,[]) if token in s]
    if not found: found=[i for i,s in enumerate(full(p),1) if token in s]
    if not found: raise ValueError((p,token))
    return p+':'+str(found[0])
def writecsv(name,rows):
    with (OUT/name).open('w',encoding='utf-8-sig',newline='') as f:
        w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)

# Misc entities that are living props/copies are deliberately kept out of unique creatures.
AUX=set('alyosha_arrow carcass doghead_arrow earth_encounter_actor ember_clump finist_magic_arrow flame_vial flying_broom flying_mortar furniture_seat gorynych_breath_cloud gorynych_breath_shot guardian_water_echo guardian_water_prison hot_stone karachun_blade karachun_raven kartaus_banner kartaus_curse_anchor kladovik_spike kladovik_stone kurgan_bolt marinka_bolt marinka_hex_focus marinka_rune marinka_tur_minion mor_bolt mor_flask_projectile mor_zone morozko_ice_bolt morozko_ice_double morozko_icicle player_ice_double poloz_glob poloz_stone_spike rune_visual serpent_projection thrown_net tugarin_stone weapon_projectile earth_servant grey_wolf_support'.split())
NPC=set('baba_yaga domovoy kuzya kot_bayun firebird sivka_burka'.split())
ALLIES=set('ilya_muromets dobrynya_nikitich alyosha_popovich finist'.split())
BOSS=set('zmey_gorynych great_poloz morozko karachun earth_spirit idolishche_poganoe kartaus marinka skim_beast kalin_tsar likho_one_eyed tugarin_zmey'.split())
MINI=set('ataman nightingale cursed_volkolak extinguished_flame_guardian defiled_spring_keeper kurgan_voevoda buried_volkhv unresting_prince kladovik large_podzemnik elder_vodyanoy ovinnik volkolak fire_serpent podvey grey_wolf'.split())
LEVEL={'zmey_gorynych':'VIII','great_poloz':'VIII','morozko':'VII','karachun':'VII','extinguished_flame_guardian':'VII','defiled_spring_keeper':'VII'}
TARGET={'ataman':100,'tugarin_zmey':400,'nightingale':350,'likho_one_eyed':350,'cursed_volkolak':'UNVERIFIED: 700 относится к Оборотню; подтвердить решение','extinguished_flame_guardian':900,'defiled_spring_keeper':800,'zmey_gorynych':2200,'firebird':3000}
STATS={}
def stat(ids,hp,dmg,armor,kb,short,token,tough=0):
    for ident in ids.split(): STATS[ident]=(hp,dmg,armor,tough,kb,ref(short,token))
stat('domovoy',12,2,0,0,'entity/DomovoyEntity','Attributes.MAX_HEALTH')
stat('leshy',100,8,8,.25,'entity/LeshyEntity','Attributes.MAX_HEALTH')
stat('kikimora',36,5,2,0,'entity/KikimoraEntity','Attributes.MAX_HEALTH')
stat('poludnitsa',60,7,4,0,'entity/PoludnitsaEntity','Attributes.MAX_HEALTH')
stat('polevik',44,6,3,0,'entity/PolevikEntity','Attributes.MAX_HEALTH')
stat('bannik',80,7,5,.7,'entity/BannikEntity','Attributes.MAX_HEALTH')
stat('igosha',30,4,0,0,'entity/IgoshaEntity','Attributes.MAX_HEALTH')
stat('bolotnik',48,7,4,.4,'swamp/BolotnikEntity','Attributes.MAX_HEALTH')
stat('rusalka','28; SWAMP spawn 36',3,0,0,'water/RusalkaEntity','Attributes.MAX_HEALTH')
stat('vodyanoy','44; SWAMP spawn 72',5,0,0,'water/VodyanoyEntity','Attributes.MAX_HEALTH')
stat('elder_vodyanoy',150,8,0,.8,'depth/ElderVodyanoy','Attributes.MAX_HEALTH')
stat('grey_wolf',500,13,0,.85,'wolf/GreyWolf','Attributes.MAX_HEALTH')
stat('nightingale',250,'10; phase2 12',0,.65,'bandit/NightingaleEntity','Attributes.MAX_HEALTH')
stat('ovinnik',175,10,10,.55,'hunt/HuntMob','public static AttributeSupplier',2)
stat('volkolak',165,9,7,.25,'hunt/HuntMob','public static AttributeSupplier')
stat('fire_serpent',155,7,5,.35,'hunt/ElementHuntMob','public static AttributeSupplier')
stat('podvey',160,7,4,.75,'hunt/ElementHuntMob','public static AttributeSupplier')
stat('likho_one_eyed',320,11,10,.65,'hunt/BossKind','LIKHO(',2)
stat('tugarin_zmey',350,12,15,.78,'hunt/BossKind','LIKHO(',4)
stat('cursed_volkolak',400,15,7,.6,'volkolak/CursedVolkolak','Attributes.MAX_HEALTH')
stat('baba_yaga',40,'UNVERIFIED',0,1,'yaga/BabaYaga','Attributes.MAX_HEALTH')
for id,hp in [('bandit_fighter',30),('bandit_archer',36),('bandit_heavy',50),('bandit_senior',44),('ataman',150)]:
    stat(id,hp,'2 + equipment/modifiers','0 + equipment',.35 if id=='bandit_heavy' else .3 if id=='ataman' else 0,'bandit/BanditEntity','public static AttributeSupplier')
for id,hp,dam,arm,kb in [('brown_bear',50,7,0,.3),('bear_cub',12,0,0,0),('forest_wolf',30,5,0,0),('boar',36,4,0,0),('stag',26,4,0,0),('doe',22,0,0,0)]:
    stat(id,hp,dam,arm,'UNVERIFIED','entity/WildlifeEntity','Attributes.MAX_HEALTH,k==')
for id,hp,dam,arm,kb in [('upyr',40,6,2,.1),('nav',32,5,0,.2),('kurgan_druzhinnik',60,7,8,.35),('kurgan_voevoda',150,9,12,.5),('buried_volkhv',150,6,6,.2),('unresting_prince',350,11,16,.7)]:
    stat(id,hp,dam,arm,kb,'kurgan/KurganFighter',id+'"')
for id,hp,dam,arm,kb,short,token,tough in [
 ('ilya_muromets',280,12,0,.5,'bogatyr/BogatyrContent','e.put(ILYA',0),('idolishche_poganoe',900,20,0,1,'bogatyr/BogatyrContent','e.put(IDOL',0),
 ('dobrynya_nikitich',230,9,0,.35,'marinka/MarinkaContent','e.put(DOBRYNYA',0),('marinka',600,5,0,.65,'marinka/MarinkaContent','e.put(MARINKA',0),
 ('alyosha_popovich',175,'UNVERIFIED',6,.12,'skim/SkimContent','e.put(ALYOSHA',0),('skim_beast',750,'UNVERIFIED',8,.85,'skim/SkimContent','e.put(SKIM',2),
 ('finist',170,'UNVERIFIED',12,.12,'finist/FinistContent','e.put(FINIST',0),('kartaus',650,'UNVERIFIED',10,.7,'finist/FinistContent','e.put(KARTAUS',2),
 ('kalin_tsar','1024 attribute; own ceiling UNVERIFIED','UNVERIFIED',16,.8,'kalin/KalinContent','e.put(KALIN',4),('kalin_war_horse',300,'UNVERIFIED',16,1,'kalin/KalinContent','e.put(HORSE',0),
 ('kalin_common_soldier',28,5,4,0,'kalin/KalinContent','e.put(COMMON',0),('kalin_elite_soldier',65,9,12,0,'kalin/KalinContent','e.put(ELITE',0),
 ('kot_bayun',100,'UNVERIFIED',0,1,'bayun/BayunContent','Attributes.MAX_HEALTH',0),('nochnitsa',50,6,0,.05,'nochnitsa/NochnitsaContent','Attributes.MAX_HEALTH',0),
 ('pesigolovets','40 hunter;28 scout;50 leader',6,0,0,'pesigolovtsy/DogheadRules','HUNTER(40',0),
 ('podzemnik','28; variant1 40','5; variant1 6.5',0,.1,'underground/UndergroundMobs','e.put(PODZEMNIK',0),('kladovik',200,6,0,.3,'underground/UndergroundMobs','e.put(PODZEMNIK',0),('large_podzemnik',300,10,0,.75,'underground/UndergroundMobs','e.put(PODZEMNIK',0),
 ('earth_spirit',800,'rawForDiamond(8)',0,1,'earth/EarthRules','HEALTH=800',0),('earth_servant',18,'rawForIron(1.5)',0,0,'earth/EarthRules','HEALTH=800',0),
 ('extinguished_flame_guardian',500,5,7,.8,'guardians/GuardianRules','FIRE_HP=500',0),('defiled_spring_keeper',450,3,4,.8,'guardians/GuardianRules','FIRE_HP=500',0),
 ('guardian_water_echo',25,2,0,0,'guardians/GuardianRules','FIRE_HP=500',0),('guardian_water_prison',3,'UNVERIFIED',0,0,'guardians/WaterPrison','MAX_HEALTH,3',0),
 ('zmey_gorynych','1024 attribute;3600 logical','UNVERIFIED',12,1,'gorynych/Gorynych','Attributes.MAX_HEALTH',6),('firebird','1024 attribute;3000 getMaxHealth','UNVERIFIED',0,'UNVERIFIED','firebird/FirebirdEntity','getMaxHealth()',0),
 ('great_poloz','3000 requested attribute; own ceiling UNVERIFIED',12,0,1,'poloz/GreatPoloz','Attributes.MAX_HEALTH',0),('karachun','1024 attribute;1200 default own ceiling',12,10,.9,'karachun/Karachun','Attributes.MAX_HEALTH',0),
]: stat(id,hp,dam,arm,kb,short,token,tough)
for id,hp in [('pike',8),('carp',6),('crayfish',4)]:stat(id,hp,'UNVERIFIED',0,0,'registry/ModEntities','event.put('+id.upper())

def klass(row):
    d=row['declaration']
    if 'wildlife(' in d:return 'org.slavicmyths.entity.WildlifeEntity'
    m=re.search(r'of\(([\w.]+)::new',d) or re.search(r'new ([\w.]+)\(',d)
    if not m:return 'UNVERIFIED'
    name=m[1]
    if '.' in name:return name
    package='entity' if row['source'].endswith('/registry/ModEntities.java') else row['source'].split('/')[-2]
    return 'org.slavicmyths.'+package+'.'+name

rows=[]
animals=set('brown_bear bear_cub forest_wolf boar stag doe pike carp crayfish kalin_war_horse sivka_burka grey_wolf'.split())
humans=set('bandit_fighter bandit_archer bandit_heavy bandit_senior ataman nightingale kalin_tsar kalin_common_soldier kalin_elite_soldier'.split())|ALLIES
undead=set('upyr nav kurgan_druzhinnik kurgan_voevoda buried_volkhv unresting_prince'.split())
water=set('rusalka vodyanoy elder_vodyanoy bolotnik defiled_spring_keeper'.split())
fire=set('ovinnik fire_serpent firebird extinguished_flame_guardian'.split())
for r in INDEX:
    id=r['id'].split(':')[1];cl=klass(r);short=cl.removeprefix('org.slavicmyths.').replace('.','/')
    p=path(short);es=E.get(p,[])
    st=STATS.get(id,('UNVERIFIED',)*6)
    # Source snippets, not a claim that a quoted expression has been interpreted.
    defense=' | '.join(f'{p}:{n} {s}' for n,s in es if ('hurt(' in s and ('return false' in s or '*=' in s)) or 'canFreeze' in s or 'causeFallDamage' in s)
    types=[]
    if id in animals:types.append('животное')
    if id in humans:types.append('человек')
    if id in undead:types+=['нежить','нечисть']
    if id in water:types+=['дух','водный']
    if id in fire:types+=['огненный']
    if id in {'leshy','kikimora','poludnitsa','polevik','bannik','igosha','domovoy','kuzya','earth_spirit','podvey','nochnitsa','likho_one_eyed','ovinnik'}:types+=['дух']
    if id in {'kikimora','nochnitsa','likho_one_eyed','volkolak','cursed_volkolak','marinka','baba_yaga'}:types+=['нечисть']
    group='auxiliary' if id in AUX else 'NPC' if id in NPC else 'ally' if id in ALLIES else 'boss' if id in BOSS else 'mini_boss' if id in MINI else 'ordinary'
    rows.append(dict(name=r['name'],entity_type=r['id'],implementation=cl,group=group,danger_level=LEVEL.get(id,'0' if id in {'pike','carp','crayfish','bear_cub','doe'} else 'UNVERIFIED'),types=';'.join(types) or 'UNVERIFIED',types_status='аналитическая классификация; не runtime tag',current_max_health=st[0],target_hp=TARGET.get(id,'UNVERIFIED'),current_base_damage=st[1],proposed_base_damage={'VII':24,'VIII':26}.get(LEVEL.get(id),'UNVERIFIED'),armor=st[2],armor_toughness=st[3],knockback_resistance=st[4],resistances_immunities=defense or ('fireImmune registration' if 'fireImmune()' in r['declaration'] else 'UNVERIFIED'),spawn_conditions='UNVERIFIED',spawn_frequency='UNVERIFIED',progression_role={'NPC':'взаимодействие/награды','ally':'сюжетный союзник','boss':'босс отдельной встречи','mini_boss':'боевой рубеж/награды','ordinary':'локальная фауна/противник','auxiliary':'снаряд/область/копия/призыв/транспорт/prop'}[group],attribute_source=st[5],registry_source=r['source']+':'+str(r['line']),notes='Состояния см. FORMS.md; числовое предложение не изменяет production'))

ATT=[]
COEFF={'A1':.65,'A2':.80,'A3':.70,'A4':1.,'A5':1.15,'A6':1.35,'A7':1.50}
def atk(id,name,short,token,damage,reach='UNVERIFIED',windup='UNVERIFIED',cd='UNVERIFIED',effects='нет дополнительных в указанной ветке',multi='1',cat='A4',form='normal',note=''):
    base={'VII':24,'VIII':26}.get(LEVEL.get(id))
    target=round(base*COEFF[cat],2) if base is not None and cat in COEFF else 'UNVERIFIED'
    ATT.append(dict(entity_type='slavicmyths:'+id,form=form,attack=name,handler=short.split('/')[-1]+': '+token,source=ref(short,token),current_direct_damage=damage,range_blocks=reach,windup_ticks=windup,cooldown_ticks=cd,effects_and_duration_ticks=effects,hits=multi,proposed_category=cat,coefficient=COEFF.get(cat,'N/A'),target_base_damage=base or 'UNVERIFIED',target_direct_damage=target,target_unit='за действие; распределение между попаданиями UNVERIFIED' if multi!='1' else 'за попадание',status='SOURCE_VERIFIED; proposal not approved',notes=note))

for id,dam in [('leshy',8),('kikimora','6+4'),('poludnitsa',7),('polevik',6),('bannik',7),('igosha',4)]:
    token='strikeTarget(' if id not in {'poludnitsa'} else 'player.hurt('
    atk(id,'персональный ближний удар/комбо','entity/'+{'leshy':'LeshyEntity','kikimora':'KikimoraEntity','poludnitsa':'PoludnitsaEntity','polevik':'PolevikEntity','bannik':'BannikEntity','igosha':'IgoshaEntity'}[id],token,dam,multi='2' if id=='kikimora' else '1')
atk('leshy','корни/исчезновение','entity/LeshyEntity','if(roots&&attackable',0,20,16,280,'Slowness40; vanish16',cat='N/A')
atk('bannik','пар','entity/BannikEntity','if(steam)',4,5,effects='Blindness40',cat='A3')
atk('hot_stone','попадание; producer UNVERIFIED','entity/HotStoneEntity','onHitEntity',4,effects='fire40',cat='A2',note='BannikEntity не создаёт этот снаряд в проверенном scheduler; не приписывать Баннику доступную атаку')
for id,dam in [('rusalka',3),('vodyanoy',5),('bolotnik',7)]:atk(id,'vanilla MeleeAttackGoal','water/WaterSpirit','MeleeAttackGoal',dam,note='Mob.doHurtTarget + difficulty/equipment; итог после брони не равен raw')
atk('bolotnik','замедление после удара','swamp/BolotnikEntity','doHurtTarget',0,cd=120,effects='Slowness60',cat='N/A',note='не отдельное повторное hurt; proc предыдущей атаки')
for id,dam in [('brown_bear',7),('forest_wolf',5),('boar',4),('stag',4)]:atk(id,'strike','entity/WildlifeEntity','private void strike',dam,note='множитель атаки/формы отдельно')
atk('brown_bear','тяжёлый удар','entity/WildlifeEntity','attackStyle()==1','7*1.35; mother8*1.35',cat='A5')
atk('boar','разгон','entity/WildlifeEntity','strike(target,1.35F','4*1.35',cat='A6')
for id in 'bandit_fighter bandit_heavy bandit_senior ataman'.split():atk(id,'оружие/тяжёлый удар','bandit/BanditEntity','try {hit=doHurtTarget','2+equipment+heavy modifier',note='armor и weapon различаются при генерации лагеря; финальное значение UNVERIFIED')
atk('bandit_archer','стрела','bandit/BanditEntity','setBaseDamage','см. source; vanilla velocity formula',cat='A2')
for id,base in [('ovinnik',10),('volkolak',9)]:
    atk(id,'MELEE' if id=='ovinnik' else 'CLAW','hunt/HuntMob','case MELEE:case CLAW',base,2.7 if id=='ovinnik' else 2.4,8 if id=='ovinnik' else 5,22 if id=='ovinnik' else 18)
for name,token,dam,r,w,c,cat,multi,eff in [('SLAM','case SLAM:', '8; blocking3',4,18,120,'A6','1','knockback'),('ASH','case ASH:',4,6,12,140,'A3','1','Weakness80'),('TRAIL','for(Trail t:trail)','2; rain1.7','AABB1.2x.7x1.2',10,160,'A1','every20','периодическая зона'),('EMBER','case EMBER:','UNVERIFIED','UNVERIFIED',10,100,'A2','1','огненный снаряд')]:atk('ovinnik',name,'hunt/HuntMob',token,dam,r,w,c,eff,multi,cat)
atk('volkolak','POUNCE','hunt/HuntMob','if(m==Move.POUNCE',8,2.6,10,90,'knockback.5','1','A6')
atk('volkolak','COMBO','hunt/HuntMob','case COMBO:','4+4+7',2.7,8,110,'knockback.7','3 at8/14/20','A5',note='invulnerableTime сокращается до10')
for id in ['fire_serpent','podvey']:atk(id,'MELEE_SNAP','hunt/ElementHuntMob','if(s==State.MELEE_SNAP)',7,'2.4/2.5','7/8','UNVERIFIED')
for name,token,dam,r,w,c,cat,multi,eff in [('FIRE_DASH','if(s==State.FIRE_DASH)',8,2.2,16,'ElementRules.cooldown','A6','1 per UUID','fire60 rain adjusted'),('STAR_DIVE','if(s==State.STAR_DIVE)',10,3.25,24,'ElementRules.cooldown','A6','1','fire40 rain adjusted'),('trail','for(Trace tr:traces)',2,'AABB1.4',0,20,'A1','every20','rain adjusted'),('ember fallback','EmberClump e=','UNVERIFIED','UNVERIFIED',0,100,'A2','1','не выбираемый основной cast')]:atk('fire_serpent',name,'hunt/ElementHuntMob',token,dam,r,w,c,eff,multi,cat)
for name,token,dam,r,w,cat,multi,eff in [('VORTEX','private void vortex()',6,'pull7; hit1.5',20,'A3','every30 within60','pull/projectile deflection'),('GUST','if(s==State.GUST_CHARGE)',4,8,14,'A3','1','push; shield .55'),('BROKEN_PATH_DASH','if(s==State.BROKEN_PATH_DASH)',6,2.1,10,'A6','1','Weakness60 Slowness40')]:atk('podvey',name,'hunt/ElementHuntMob',token,dam,r,w,'ElementRules.cooldown',eff,multi,cat)
for name,token,dam,r,w,c,cat,multi,eff in [('SWEEP','case SWEEP:',10,4,14,50,'A3','1','knockback.55'),('GRAB','case GRAB:',4,3.5,18,180,'A5','1','ILL_FATE160; hold20'),('throw','if(toss&&p.isAlive())',6,5,20,'part ofGRAB','A5','1','slow_falling60'),('GAZE','case GAZE:',2,10,26,200,'A1','1','ILL_FATE160 Weakness80'),('GROUND','case GROUND:for',6,1.75,24,150,'A3','1 per UUID across3 zones','Slowness50'),('LEAP','if(m==Move.LEAP&&time>18)',8,3.5,18,160,'A6','1','landing/knockback'),('CHASE','if(m==Move.CHASE)',10,4,0,220,'A5','3 at25/50/75','phase3')]:atk('likho_one_eyed',name,'hunt/WorldBoss',token,dam,r,w,c,eff,multi,cat,note='phase3 cooldown*0.85')
for name,token,dam,r,w,c,cat,multi,eff in [('PUNCH','case PUNCH ->',12,3.5,10,'26; phase2 20','A4','1','knockback.65'),('SWEEP','case SWEEP ->',16,4,14,200,'A3','1','knockback.65'),('SPIN','if(move==Move.SPIN)',16,5.2,16,180,'A5','3 at16/28/40','knockback.8'),('BLADE_COMBO','if(move==Move.BLADE_COMBO)','5+5+8',3.5,10,260,'A5','3 at10/22/34','native hurt window'),('CHARGE','if(move==Move.CHARGE)',14,2.1,10,240,'A6','1 per UUID','Slowness14'),('WING_DIVE','if(move==Move.WING_DIVE)',16,2.4,8,280,'A6','1 per UUID','low glide/dive'),('BREATH','if(move==Move.BREATH)',6,8,18,240,'A5','3 at18/30/42','fire40'),('SERPENT_LUNGE','if(move==Move.SERPENT_LUNGE',14,5,8,150,'A6','1 per UUID','Slowness45'),('SMOKE_BURST','case SMOKE_BURST ->',8,5,14,340,'A3','1','Slowness35 fire40')]:atk('tugarin_zmey',name,'hunt/TugarinCombat',token,dam,r,w,c,eff,multi,cat,note='персональная текущая базовая12; новая персональная база UNVERIFIED')
for name,token,dam,r,w,c,cat,multi in [('CLAW_L/R','else if(m==Move.CLAW_L',15,3.1,'10/12',18,'A4','1'),('DOUBLE','else if(m==Move.DOUBLE','13+13',3.1,10,52,'A5','2 at10/20'),('TRIPLE','else if(m==Move.DOUBLE','13+13+18',3.1,10,85,'A5','3 at10/20/30'),('BITE','else if(m==Move.BITE',17,3.05,13,25,'A5','1'),('POUNCE','else if(m==Move.POUNCE)',20,2.8,16,90,'A6','1 landed'),('FLANK','else if(m==Move.FLANK)', 'UNVERIFIED','UNVERIFIED','UNVERIFIED',55,'A6','1'),('GRAB','private void capture',16,1.65,'UNVERIFIED',260,'A5','UNVERIFIED')]:atk('cursed_volkolak',name,'volkolak/CursedVolkolak',token,dam,r,w,c,'bleeding on actual strike; phase3 +2',multi,cat,note='strike damage + ATTACK_DAMAGE-15; capture shield branch fixed16')
for name,dam,token,w,multi,cat in [('BITE',13,'case BITE->13',12,'1','A4'),('DOUBLE_CLAW',11,'case BITE->13',14,'2 at14/33','A5'),('POUNCE',17,'case BITE->13',27,'1','A6'),('FLANK_DASH',15,'case BITE->13',27,'1','A6')]:atk('grey_wolf',name,'wolf/GreyWolf',token,dam,'WolfTimeline/strike gate',w,'UNVERIFIED','WolfBleeding; pounce knockback.65',multi,cat,note='персональная база13; новая база UNVERIFIED')
for id,short,dam in [('extinguished_flame_guardian','FireGuardian',5),('defiled_spring_keeper','SpringKeeper',3)]:atk(id,'MELEE','guardians/GuardianEntity','meleeTick()',dam,'UNVERIFIED',11,'recovery9')
atk('extinguished_flame_guardian','PULSE','guardians/FireGuardian','if(s==GuardianRules.PULSE)','8/5/2','3/6/10',30,'280; final224',cat='A3')
atk('extinguished_flame_guardian','SLAM','guardians/FireGuardian','if(s==GuardianRules.SLAM)',4,'3 cracks length6 width1',20,180,'knockback.25',cat='A6')
atk('extinguished_flame_guardian','LOCKDOWN','guardians/FireGuardian','if(s==GuardianRules.LOCKDOWN)',2,'arena unsafe strips','24; final18',500,'3 waves active40','3 pulses/wave every15','A1')
atk('defiled_spring_keeper','WHIP','guardians/SpringKeeper','if(s==GuardianRules.WHIP)',3,6,16,'140; desperation110','knockback.12','2 at16/26','A5')
atk('defiled_spring_keeper','WHIRL','guardians/SpringKeeper','if(s==GuardianRules.WHIRL)',2,2.5,24,280,'pull.065; active80','4 every20','A1')
atk('defiled_spring_keeper','PRISON','guardians/SpringKeeper','if(s==GuardianRules.PRISON)',0,'arena safe prison',20,400,'capture max80; 3 escape actions',cat='N/A')
atk('defiled_spring_keeper','FLOOD','guardians/SpringKeeper','if(s==GuardianRules.FLOOD)',0,'2 arena regions',24,340,'motion*.82; active140; second delay50',cat='N/A')
atk('defiled_spring_keeper','HEAL','guardians/SpringKeeper','if(s==GuardianRules.HEAL)',0,'within2 of source',40,200,'self heal5 every20 for200',cat='N/A')
atk('guardian_water_echo','water projectile','guardians/WaterEcho','tickCount%40==0',2,6,0,40)
for name,token,dam,r,w,c,cat,multi,eff in [('predator bite/grab','if(headAge[h]==warning',11,'sqrt120',20,'rest100','A4','1','grab immunity240'),('grab ticks','if(grabAge%20==0)',3,15,20,'part of grab','A1','3 at20/40/60','max60; escape3 hits'),('mor ray','if(h<2&&headAction[h]==2',4,22,36,'rest400','A5','6 every10 over60','Weakness100'),('fire cone','if(h<2&&headAction[h]==2',4,22,36,'rest480','A5','6 every10 over60','fire60'),('tail','if(bodyAge==32)',12,11,32,'rest300 ground160 air','A3','1','rear dot<.35 knockback.8'),('spikes','if(bodyAge==32)',10,1.5,32,'body rest300','A3','up to5 marks; native i-frame','vertical effects5'),('mor rain','if(bodyAge==32)',6,'shot explosion AABB4',32,'body rest160','A3','7 shots+clouds','мор'),('fire bombardment','if(action==4&&bodyAge>=32',8,'shot explosion AABB4',32,'body rest160','A5','5 shots32/44/56/68/80+clouds','fire60'),('wind','if(bodyAge==32)',6,20,32,'body rest160','A3','1+cloud','push.7/up.3'),('dive','if(bodyAge==32)',12,12,32,'body rest160','A6','1','landing')]:atk('zmey_gorynych',name,'gorynych/Gorynych',token,dam,r,w,c,eff,multi,cat,note='head/body budget2; cooldown rest starts after action')
for typ,dam in [('mor',6),('fire',8)]:atk('zmey_gorynych',typ+' shot','gorynych/BreathShot','if(hit.getType()!=HitResult.Type.MISS)',dam,'AABB4x4x4',24,'head rest400/480','fire60 for fire; creates cloud','1+cloud','A2')
for typ,dam in [('mor',3),('fire',3),('wind',2)]:atk('zmey_gorynych',typ+' cloud','gorynych/BreathCloud','if(tickCount%20==0)',dam,3,0,'part of parent cast','Weakness80/fire40/wind push; life240','every20 while cloud active','A1')
atk('firebird','retaliation','firebird/FirebirdEntity','if(state()==6)',20,3,'11 warning+8 attack',60,'firebird_retaliation damage type',cat='A7',note='сюжетная персональная атака; новую базу не выводить из MAX_HEALTH')
atk('kot_bayun','claws','bayun/BayunEntity','if(!intercepted)','max(1000,maxHP+absorption+100)','UNVERIFIED','UNVERIFIED','UNVERIFIED','lethal narrative response',cat='A7',note='не переносить обычную A7 формулу без отдельного сюжетного решения')
for name,token,dam,r,w,c,cat,multi,eff in [('staff','if(a==1&&age==10)','FrostRules.MELEE',3.5,10,40,'A4','1','cold8'),('bolt','if(a==2&&age==13','FrostRules.BOLT',20,13,50,'A2','1','cold10'),('spikes','if(a==4&&age==5)',7,'spike hitbox',23,'283/244','A3','3..5 spike actors','cold12 Weakness60 Slowness60'),('breath','if(a==5&&age>=16',4,9,16,'423/369','A5','3 at16/30/44','cold15'),('wave','if(a==6&&age==24)',5,7,24,482,'A3','1 per UUID','cold8 knockback.7'),('blizzard','if(a==3&&age==50)',0,12,50,850,'N/A','aura every4','storm240 cold accumulation'),('final storm','if(a==8&&age==48)',0,12,48,'once phase3','N/A','aura every4','enhanced160 cold*1.5')]:atk('morozko',name,'morozko/Morozko',token,dam,r,w,c,eff,multi,cat)
for name,token,dam,r,w,c,cat,eff in [('scythe','if(a<=3&&age==',12,4,'10/14','NightConfig sweepCooldown','A4','waning4 fear6 knockback.6'),('hunger','if(a==4&&age==16',0,'projectile',16,'NightConfig hungerCooldown','N/A','Hunger240 amp3 fear6'),('frost','if(a==5&&age==20)',6,8,20,'NightConfig frostCooldown','A3','waning20 fear4 Weakness80'),('death','if(a==6&&age==16)',8,3.5,16,'NightConfig deathCooldown','A5','waning10 fear10 Wither100 amp1'),('last hour','if(marker!=null)',16,3,'NightConfig hourWindup','NightConfig hourCooldown','A7','waning10 fear10'),('long night','if(a==7&&age==24)',0,'arena',24,'NightConfig nightCooldown','N/A','aura*1.4 fear .6; duration config'),('fear','if(a==8&&age==24)',0,'arena',24,'NightConfig fearCooldown','N/A','fear+30')]:atk('karachun',name,'karachun/Karachun',token,dam,r,w,c,eff,cat=cat)
atk('karachun_raven','dive','karachun/NightRaven','else if(state()==2)','NightConfig ravenDamage','collision+.25','UNVERIFIED','UNVERIFIED','fear8')
for name,token,dam,r,w,cat,multi,eff in [('head strike','if(skill==1&&attackAge>=30',12,'body inflate1',30,'A4','1 per UUID','phase*1/1.15/1.25'),('emerge','if(skill==2&&attackAge==100)','14; water12','inflate4',100,'A6','1','phase multiplier'),('tail','private void tailSweep','min(12,11*phase); secondary5*phase','tail9; wave6','UNVERIFIED','A3','1 UUID across both areas','push'),('funnel','if(skill==5&&attackAge>=80',8,7,80,'A1','3 at80/100/120','pull .15'),('rain','public boolean rainDamage','budget12 per volley','UNVERIFIED','UNVERIFIED','A3','multi; budget12','budget charged before hurt')]:atk('great_poloz',name,'poloz/GreatPoloz',token,dam,r,w,'UNVERIFIED',eff,multi,cat)
for name,token,dam,r,w,cat,multi in [('fist','case FIST_SLAM',20,4.8,8,'A4','1'),('stomp','case STOMP_PULSE','BogatyrRules.stompRaw(ring)','5/10/15',4,'A3','1 UUID across3 rings'),('grab','case GRAB_HOLD',18,6,8,'A5','1+landing10'),('breath','case BREATH_ACTIVE',20,11,10,'A5','1+2 delayed1'),('charge','case CHARGE_RUNNING',22,'collision.4',0,'A6','1 UUID; Ilya shield32')]:atk('idolishche_poganoe',name,'bogatyr/IdolishcheEntity',token,dam,r,w,'UNVERIFIED','rage*1.1 except breath; breath Slowness60 Weakness80 Confusion50',multi,cat)
for name,token,dam,r,w,cat,multi in [('melee','case MELEE', 'rawForDiamond(8)','surface4.4',12,'A4','1'),('pillar','private void impact','rawForDiamond(13); splash rawForDiamond(8)',3.5,'UNVERIFIED','A6','direct+splash excludes same UUID'),('volley','kind()==PILLAR?EarthRules.PILLAR_RAW','rawForDiamond(5)','actor collision','UNVERIFIED','A2','4..6; shared8tick gate'),('tremor','kind()==TREMOR?.35','rawForDiamond(10.5)','wave radius','UNVERIFIED','A3','1 UUID/wave'),('spikes','private void spikeTick','rawForDiamond(9)','length13 side1',1,'A3','1 UUID across3 segments')]:atk('earth_spirit',name,'earth/EarthSpirit' if name=='melee' else 'earth/EarthActor',token,dam,r,w,'EarthRules.Action cooldown','native armor calibration; tremor Imbalance13',multi,cat)
atk('earth_servant','melee','earth/EarthServant','age(0)>=9','rawForIron(1.5)',2.6,9,32)
for id,short,entries in [
 ('skim_beast','skim/SkimBeastEntity',[('CLAWS','case CLAWS','raw(2.5)+raw(2.5)+raw(6.1)',3.8,11,'3 at11/19/30','A5'),('LEAP','case LEAP','raw(3..7)',2.2,29,'1','A6'),('WHISTLE','case WHISTLE','raw(1)',18,22,'1','A1'),('ROAR','case ROAR','raw(6.1)/raw(3)',7,28,'1','A3'),('FLANK','case FLANK','raw(6.1)',3.8,24,'1','A6'),('GRIP','case GRIP','raw(3)+raw(2)+raw(2)+raw(4)',2.5,16,'4 at21/31/41/45','A5')]),
 ('kartaus','finist/KartausEntity',[('SWORD','case SWORD','raw(6)',3.3,13,'1','A4'),('WAVE','private void wave','raw(6.5); phase3 raw(8.125)',18,19,'1 UUID','A3'),('EYE','case EYE','raw(7); phase3 raw(8.75)',2.5,42,'1','A7'),('CHAINS','case CHAINS','raw(2)+3*raw(1)',2.5,22,'initial+3 every20','A1')]),
 ('ilya_muromets','bogatyr/IlyaEntity',[('mace','case MACE_WINDUP','12; Idol10 joint15',3.3,19,'1','A4'),('shockwave','case SHOCKWAVE_CAST','8; Idol5',4.5,29,'1','A3')]),
 ('dobrynya_nikitich','marinka/DobrynyaEntity',[('COMBO','case COMBO','9+10+15',2.6,6,'3 at6/13/23','A5'),('THRUST','case THRUST',21,3.6,8,'1','A5'),('PARRY','case PARRY',13,3.6,10,'1','A5'),('SWEEP','case SWEEP',15,3.25,9,'1 UUID across9/15','A3'),('BULL_CHARGE','case BULL_CHARGE',12,3,14,'1','A6')]),
 ('alyosha_popovich','skim/AlyoshaEntity',[('SABER','case SABER','2+2+2.4+2.6',2.5,6,'4 at6/11/16/19','A5'),('FEINT','case FEINT',4,2.5,14,'1','A5'),('COUNTER','case COUNTER',4,2.5,8,'1','A5')]),
 ('finist','finist/FinistEntity',[('SWORD','FalconAction.SWORD&&frame(8)',5,2.7,8,'1','A4'),('DASH/MORPH','private void leapTick','8/7',2.7,'20/31','1','A6'),('falcon dive','else if(bird()==2)','3; Kartaus/Kalin2',20,16,'1','A2')]),
 ('kalin_tsar','kalin/KalinEntity',[('SWORD','FalconAction.SWORD&&frame(12)','diamondRaw(7)','4.5 mounted/3.6 foot',12,'1','A4'),('WAVE','FalconAction.WAVE&&','diamondRaw(3)','6 mounted/5 foot',15,'3 at15/26/37','A5'),('INTERCEPT','FalconAction.INTERCEPT&&parried&&frame(14)','diamondRaw(5)',4,14,'1','A5'),('BANNER stomp','FalconAction.BANNER&&frame(25)','diamondRaw(4)',4,25,'1','A3'),('DASH','FalconAction.DASH&&t>=','diamondRaw(6 mounted/5 foot)','collision+.7','6/8','1 UUID','A6')]),
]:
    for name,token,dam,r,w,multi,cat in entries:atk(id,name,short,token,dam,r,w,'UNVERIFIED','см. обработчик; длительность UNVERIFIED',multi,cat,note='raw(net) — нелинейная калибровка по броне, не прямой net HP')
for id,short,entries in [
 ('kladovik','underground/Kladovik',[('BURROW','case BURROW',9,1.6,80,270,'A6'),('SPIKES','case SPIKES',6,.85,26,250,'A3'),('COIN_WAVE','case COIN_WAVE',1,4,20,320,'A1')]),
 ('large_podzemnik','underground/LargePodzemnik',[('HEAVY','case HEAVY','EncounterRules.HEAVY_DAMAGE',3.1,'38; rage32',320,'A5'),('TREMOR','case TREMOR',3,6,24,350,'A3'),('CHARGE','case CHARGE',8,1.7,19,280,'A6')])]:
    for name,token,dam,r,w,c,cat in entries:atk(id,name,short,token,dam,r,w,c,'см.source; knockback/slow',cat=cat)
atk('nochnitsa','claw','nochnitsa/NochnitsaEntity','Event.HIT',6,2.15,'NochnitsaRules.Clock','UNVERIFIED')
for name,token,dam,w,c in [('root','MorRules.release(s,t)',4,24,240),('burst','if(s==2',3,20,360)]:atk('leshy','mor '+name,'mor/MorLeshy',token,dam,'9 select/5 burst',w,c,'root hold30; burst kb1.1',cat='A1' if name=='root' else 'A3',form='mor')
atk('poludnitsa','mor flash','mor/MorPoludnitsa','if(s==3', '2; noon3',6,20,400,'Blindness30',cat='A1',form='mor')
atk('poludnitsa','mor dash combo','mor/MorPoludnitsa','if(s==4)',3,2.5,19,220,'i-frame shortened for followup','2 at19/25','A5','mor')
atk('kikimora','mor jump strike','mor/MorKikimora','if(s==5)',4,1.75,32,260,cat='A6',form='mor')
atk('kikimora','mor thread','mor/MorKikimora','if(s==6',2,'select6',16,320,'hold20',cat='A1',form='mor')
atk('fire_serpent','mor bolts','mor/MorSerpent','if(s==7','3; clone2','select14',22,180,'i-frame shortened','3 at22/27/32','A5','mor')
atk('fire_serpent','mor dive','mor/MorSerpent','if(s==8)','6; clone3','bbox+.3',18,240,'collision','1','A6','mor')
for id,dam in [('likho_one_eyed',14),('ovinnik',12)]:
    atk(id,'mor sweep','mor/HigherMor','if(skill==11',dam,4,22,'rest32/phase22',cat='A3',form='mor')
    for name,dam,r,multi in [('ground',1,3,'every20 after exposure60'),('cloud',2,5,'every20 immediately')]:atk(id,'mor '+name,'mor/MorZone','time%20==0',dam,r,'cloud24/phase20; groundautomatic','ground60; Ovinnik phase30/cloud240','Slowness30; Ovinnik cloud Blindness30',multi,'A1','mor')
atk('likho_one_eyed','mor beam','mor/HigherMor','private void beam()',12,18,30,'190; phase130',cat='A2',form='mor')
atk('ovinnik','mor ember volley','mor/HigherMor','if(skill==13','UNVERIFIED',18,24,'190; phase130','EmberClump hit handler','3 at24/29/34','A5','mor')

# Additional verified pools and attribute corrections (do not infer capped values from requested attributes).
patches={
 'morozko':(1000,10,8,0,.8,'morozko/FrostRules','BOSS_HP=1000'),
 'morozko_ice_double':(50,10,8,0,.8,'morozko/FrostRules','BOSS_HP=1000'),
 'player_ice_double':(15,1,0,0,0,'morozko/FrostRules','BOSS_HP=1000'),
 'sivka_burka':(80,'N/A: no registered attack damage',0,0,.3,'sivka/SivkaContent','Attributes.MAX_HEALTH'),
 'kuzya':(20,'N/A: no registered attack damage',0,0,1,'kuzya/KuzyaContent','Attributes.MAX_HEALTH'),
 'kalin_tsar':('1024 attribute;1800 getMaxHealth','N/A: fixed diamondRaw attacks',16,4,.8,'kalin/KalinEntity','getMaxHealth()'),
 'great_poloz':('1024 capped attribute;3000 getMaxHealth',12,0,0,1,'poloz/GreatPoloz','getMaxHealth()'),
 'firebird':('1024 attribute;3000 getMaxHealth/getAttributeValue','N/A: fixed retaliation20',0,0,.6,'firebird/FirebirdContent','Attributes.MAX_HEALTH'),
}
for r in rows:
    id=r['entity_type'].split(':')[1]
    if id in patches:
        hp,damage,armor,tough,kb,short,token=patches[id]
        for k,v in zip(['current_max_health','current_base_damage','armor','armor_toughness','knockback_resistance'],[hp,damage,armor,tough,kb]):r[k]=v
        r['attribute_source']+='; '+ref(short,token)
    if id in animals and id in {'brown_bear','forest_wolf','boar','bear_cub','stag','doe'}:
        r['armor']={'brown_bear':3,'forest_wolf':1,'boar':2}.get(id,0);r['knockback_resistance']=0
        r['attribute_source']+='; '+ref('entity/WildlifeEntity','Attributes.ARMOR,k==')
    if id in {'rusalka','vodyanoy'}:r['attribute_source']+='; '+ref('water/WaterSpirit','double hp=')
    if id=='tugarin_zmey':r['attribute_source']+='; '+ref('hunt/WorldBoss','public static AttributeSupplier')
    if id=='likho_one_eyed':r['attribute_source']+='; '+ref('hunt/WorldBoss','public static AttributeSupplier')
    if id=='ataman':r['attribute_source']+='; '+ref('bandit/LargeCampPiece','setBaseValue(150)')
    if id in {'alyosha_popovich','finist','kartaus','kot_bayun','skim_beast','zmey_gorynych','kalin_war_horse','baba_yaga'}:r['current_base_damage']='N/A: no ATTACK_DAMAGE registered in cited factory; fixed/source-formula handlers in ATTACKS'
    if id in {'pike','carp','crayfish'}:r['current_base_damage']='N/A: no attack attribute in fish registration; predation navigation is not hurt'
    # Record all existing spawn evidence for this registry symbol. Probability expressions
    # remain per-attempt source evidence, not an invented mobs/hour measurement.
    sym=id.upper(); found=[]
    for p,lines in E.items():
        if any(t in p for t in ['Spawn','Population','Encounter','Piece','Content']):
            found.extend(f'{p}:{n} {s}' for n,s in lines if (sym+'.get()' in s or '"'+id+'"' in s) and any(t in s.lower() for t in ['spawn','create(','spawnerdata','register(','population']))
    if found:r['spawn_conditions']=' | '.join(found);r['spawn_frequency']='UNVERIFIED: use cited per-attempt/structure conditions; not mobs/hour'
    if id in {'bolotnik','vodyanoy','rusalka'}:
        r['spawn_frequency']={'bolotnik':'1/4 placement roll','vodyanoy':'1/8 placement roll','rusalka':'1/10 placement roll'}[id]
        r['spawn_conditions']=ref('water/WaterSpawns','ModEntities.'+sym+'.get()')+' '+{'bolotnik':'SWAMP; non-peaceful; night; Mob.checkMobSpawnRules','vodyanoy':'Overworld; water at pos and above','rusalka':'Overworld; night; water at pos'}[id]

for id,base,moves in [
 ('upyr',6,[('CLAW',6,2.7,5,16,'A4'),('BITE',8,2.3,10,140,'A5'),('LEAP',6,2.7,10,90,'A6')]),
 ('nav',5,[('TOUCH',5,2.5,5,18,'A4')]),
 ('kurgan_druzhinnik',7,[('SWORD',7,2.7,6,18,'A4'),('BASH',4.2,2.7,10,100,'A6'),('RIPOSTE',7,2.7,5,50,'A5')]),
 ('kurgan_voevoda',9,[('AXE',9,2.7,6,16,'A4'),('SWEEP',11,3.5,16,125,'A3'),('CHARGE',5.4,2.7,12,90,'A6')]),
 ('buried_volkhv',6,[('BOLT',6,20,8,22,'A2'),('SEAL',8,3,28,115,'A3')]),
 ('unresting_prince',11,[('COMBO',11,3.3,6,16,'A5'),('HEAVY',15,3.8,18,110,'A5'),('GRAB',8,3,12,200,'A5'),('RUSH',11,2.7,10,80,'A6')])]:
    for name,dam,r,w,c,cat in moves:
        token='case '+name+':' if name in {'BITE','TOUCH','BOLT','SEAL','SWEEP','HEAVY','GRAB'} else 'private void release('
        atk(id,name,'kurgan/KurganCreature',token,dam,r,w,c,'TOUCH slow40; BITE heal4 marker100; GRAB heal12; bash shield cooldown60','3' if name=='COMBO' else '1',cat,note='reachable per choose; timings '+ref('kurgan/KurganFighter','public enum Move'))
for id,name in [('nav','SHIFT'),('buried_volkhv','CLONES'),('buried_volkhv','SUMMON'),('unresting_prince','SUMMON')]:atk(id,name,'kurgan/KurganCreature','case '+name+':',0,'local safe reposition/summon','KurganFighter.Move','KurganFighter.Move','CLONES decoy60; SUMMON2..3/PRINCE3',cat='N/A')
for name,dam,r,w,c,cat in [('THRUST',6,2.4,10,30,'A4'),('SWEEP',4,1.8,13,80,'A3'),('KNIFE',4,1.75,9,20,'A4'),('CHOP',8,2.1,21,50,'A5'),('CLEAVE',6,2,19,120,'A3')]:atk('pesigolovets',name,'pesigolovtsy/DogheadEntity','private void impact(',dam,r,w,c,'SWEEP knockback.25','up to2 victims' if name in {'SWEEP','CLEAVE'} else '1',cat,form='role: hunter/scout/leader',note='cooldown from marker; sources '+ref('pesigolovtsy/DogheadRules','public enum Clip'))
atk('pesigolovets','BOW','pesigolovtsy/DogheadEntity','private void fire','UNVERIFIED: arrow base/speed; source',18,22,44,cat='A2',form='SCOUT')
for name,dam,token,w,c,cat in [('MELEE','5; miner6.5','case MELEE',6,'duration25/28+1','A4'),('MINER_HEAVY',10,'case MINER_HEAVY',20,160,'A5'),('LUNGE',5,'case LUNGE',12,200,'A6')]:atk('podzemnik',name,'underground/Podzemnik',token,dam,2.4,w,c,'heavy MiningFatigue50',cat=cat,form='normal/miner')
for name,token,dam,r,w,cat in [('QUICK','if(a==QUICK', '10; phase2 12',2.2,10,'A4'),('COMBO','if(a==COMBO','4+4+7',2.35,8,'A5'),('INHALE','if(a==INHALE',14,10,16,'A3'),('WHIRLWIND','if(a==WHIRLWIND',12,6,16,'A3'),('WAVE','if(a==WAVE','14; phase2 airWhistle source',12,16,'A3'),('BREAK_AWAY','if(a==BREAK_AWAY)',6,5,8,'A3'),('AIR_DIVE','if(a==AIR_DIVE)',16,2.2,12,'A6'),('LEAP_ATTACK','if(a==LEAP_ATTACK)',12,2,12,'A6'),('DASH','if(a==DASH||a==ONSLAUGHT)',12,3,8,'A6'),('ONSLAUGHT','if(a==DASH||a==ONSLAUGHT)','UNVERIFIED',3,8,'A5')]:atk('nightingale',name,'bandit/NightingaleEntity',token,dam,r,w,'UNVERIFIED','WindAttack push/control; melee knockback.35','3 at8/20/32' if name=='COMBO' else '1',cat)
for name,token,dam,r,w,c,cat in [('FIRE','case FIRE','side10; central17.5','select15',11,140,'A2'),('TRACE','case TRACE',11.2,'rune box2.5',11,220,'A3'),('LURE','case LURE',7.32,13,22,420,'A1'),('RAGE rune','kind()==1?17.5F',17.5,'rune box2.5','UNVERIFIED','UNVERIFIED','A3'),('EMBER rune','kind()==2)',1,'rune box2.5',0,20,'A1')]:atk('marinka',name,'marinka/WitchRune' if 'rune' in name else 'marinka/MarinkaEntity',token,dam,r,w,c,'phase3 timing*.85; TRACE Weakness80 Slowness40; lure pull.15','2..3 shots' if name=='FIRE' else 'every20' if name=='EMBER rune' else '1',cat,note='FIRE impact '+ref('marinka/WitchBolt','if(hit!=null)'))
for name,w,c,eff in [('SHADOW',13,700,'same EntityType falseBody clones'),('VEIL',8,600,'barrier180'),('BLINK',10,240,'safe teleport; max3 per700'),('CURSE',25,999999,'Dobrynya bull700 via HexAnchor'),('HERD','UNVERIFIED',540,'TurMinion summon'),('BIRD_CHANGE',9,540,'magpie form/flight')]:atk('marinka',name,'marinka/MarinkaEntity','case '+name,0,'local encounter',w,c,eff,cat='N/A')
atk('marinka_tur_minion','charge','marinka/TurMinion','age()==14',5,1.7,14,30)
atk('marinka_bolt','reflected projectile','marinka/WitchBolt','if(hit!=null)',13,'collision',0,'part of cast','Marinka deflected',cat='A2')
for id,damage in [('kalin_common_soldier',5),('kalin_elite_soldier',9)]:atk(id,'vanilla melee','kalin/KalinSoldier','MeleeAttackGoal',damage,'vanilla reach','vanilla','vanilla',note='attribute + equipment; source is goal registration')
for name,dam,r in [('heavy',9,4),('push',5,4),('area',3,6),('wave',3,10.5),('dash','UNVERIFIED','UNVERIFIED'),('pull',0,10)]:atk('elder_vodyanoy',name,'depth/ElderVodyanoy','hitArea(action==4',dam,r,'26/32/40; source','UNVERIFIED','pull requires water; wave1 UUID',cat='N/A' if name=='pull' else 'A3' if name in {'area','wave'} else 'A6' if name=='dash' else 'A4')
atk('vodyanoy','underwater drag proc','water/VodyanoyEntity','public boolean doHurtTarget',0,'melee',0,100,'vertical velocity-.14',cat='N/A')
atk('grey_wolf_support','support pounce','quest185/WolfSupport','if(now>=leapAt',8,4,0,800,'Poloz threat80',cat='A6')
for id in ['leshy','poludnitsa','kikimora','fire_serpent']:
    atk(id,'mor fallback melee','mor/MorController','if(d<=2.5',{'leshy':'8*1.2','poludnitsa':'7*1.12','kikimora':'5*1.05','fire_serpent':'7*1.2'}[id],2.5,0,30,'vanilla Mob.doHurtTarget modifiers',form='mor')
# Non-damaging combat actions are explicit instead of invented direct damage.
for id,short,token,name in [('fire_serpent','mor/MorSerpent','if(s==9)','mor clone summon'),('fire_serpent','hunt/ElementHuntMob','if(s==State.DECOY_CAST)','decoy cast'),('morozko','morozko/Morozko','if(a==7&&age==7)','summon ice doubles'),('karachun','karachun/Karachun','private void ravens()','summon ravens'),('earth_spirit','earth/EarthSpirit','case SUMMON','summon servants'),('volkolak','hunt/HuntMob','case HOWL:','HOWL'),('grey_wolf','wolf/GreyWolf','if(a==Move.HOWL_SUMMON)','HOWL_SUMMON')]:atk(id,name,short,token,0,'local','UNVERIFIED','UNVERIFIED','summon/buff; no direct HP in this handler',cat='N/A')

atk('morozko_ice_double','staff','morozko/IceDouble','distanceToSqr(t)<16',10,3.5,10,70,'cold8')
atk('morozko_ice_double','bolt','morozko/IceDouble','distanceToSqr(t)<=400',8,20,13,80,'cold10',cat='A2')
atk('player_ice_double','mirror melee','morozko/PlayerDouble','public void mirrorHit','snapshotDamage*(.2+scale^2*.8)*enchant; crit*1.5',3,'event mirrored accepted player hit; life>=13','ceil20/attackSpeed','FrostEvents.proc',note='персональная копия; новое фиксированное число из A4 не выводить')
atk('player_ice_double','mirror projectile','morozko/PlayerDouble','public void mirrorHit','rangedBase + native arrow formula',24,'UNVERIFIED','ceil20/attackSpeed','snapshot projectile',cat='A2')
atk('idolishche_poganoe','throw landing','bogatyr/BogatyrEvents','public static void landing',10,'landing',0,'part of GRAB','fall multiplier0',cat='A5')
atk('idolishche_poganoe','poison aftermath','bogatyr/IdolishcheEntity','for(var id:new ArrayList<>(poisonAt',1,'victim UUID',40,'part of BREATH','indirectMagic','2 every40','A1')
atk('poludnitsa','HEAT_PRESSURE','entity/PoludnitsaEntity','if(heat){particles',0,6,24,240,'Weakness60',cat='N/A')
atk('vodyanoy','reputation pressure','water/VodyanoyEntity','public void pressure',0,'interaction',0,200,'rep-20; provoke at<=-20',cat='N/A')
atk('polevik','GRASS_SNARE','entity/PolevikEntity','if(opened)',0,7,16,240,'Slowness40',cat='N/A')
atk('nochnitsa','SCREAM','nochnitsa/NochnitsaEntity','Event.SCREAM',0,32,30,600,'NochnitsaHorde alert; no direct hurt in clock',cat='N/A')
atk('baba_yaga','expulsion reaction','yaga/BabaYaga','public boolean hurt',0,'attacker',0,'YagaData progress attack gate','knockback1.6 Slowness100; close menu',cat='N/A')
# Resolve values discovered during dependency checks; never label an unresolved
# expression as a measured hit or silently merge alternate forms.
for a in ATT:
    id=a['entity_type'].split(':')[1];name=a['attack']
    if id=='cursed_volkolak' and name=='FLANK':a.update(current_direct_damage='16 + ATTACK_DAMAGE-15 + phase3?2:0',range_blocks=2.4,windup_ticks=24)
    if id=='cursed_volkolak' and name=='GRAB':a.update(current_direct_damage='held bite24 + ATTACK_DAMAGE-15 + phase3?2:0; shield branch16',windup_ticks='capture22..25; bite28',effects_and_duration_ticks='WolfBleeding + BadBlood on actual bite; held max20',hits='1 held bite; shield branch alternate',source=ref('volkolak/CursedVolkolak','else if(m==Move.GRAB)'))
    if id=='elder_vodyanoy' and name=='dash':a.update(current_direct_damage=6,range_blocks=3.5,windup_ticks='32 preparation +7 dash ticks',source=ref('depth/ElderVodyanoy','if(dashTicks>0)'))
    if id=='nightingale' and name=='ONSLAUGHT':a.update(current_direct_damage='4+4+10*WindAttack.exposure',range_blocks='melee3.2; wave10',windup_ticks=22,hits='3 at22/34/46',source=ref('bandit/NightingaleEntity','if(a==ONSLAUGHT&&(t==22'))
    if id=='pesigolovets' and name=='BOW':a.update(current_direct_damage='arrow base2.7; speed1.6 -> native velocity formula',source=a['source']+'; '+ref('pesigolovtsy/DogheadArrow','setBaseDamage(2.7)'))
    if id=='bannik' and name.startswith('персональный'):a.update(current_direct_damage=8,range_blocks=2.6,windup_ticks=14,cooldown_ticks='active1+recovery18+cooldown40; lowHP34',source=ref('entity/BannikEntity','else strikeTarget(8'))
    if id=='bannik' and name=='пар':a.update(windup_ticks=24,cooldown_ticks=240)
    if id=='leshy' and name.startswith('персональный'):a.update(range_blocks=3,windup_ticks=18,cooldown_ticks='active1+recovery20+cooldown35')
    if id=='igosha' and name.startswith('персональный'):a.update(range_blocks=2.2,windup_ticks=12,cooldown_ticks='active1+recovery24+cooldown40',effects_and_duration_ticks='vanish12')
    if id=='polevik' and name.startswith('персональный'):a.update(range_blocks=2.3,windup_ticks=12,cooldown_ticks='active10+recovery20+cooldown35',proposed_category='A6',coefficient=1.35)
    if id=='poludnitsa' and name.startswith('персональный'):a.update(range_blocks=3,windup_ticks=20,cooldown_ticks='active1+recovery16+cooldown30')
    if id=='kikimora' and name.startswith('персональный'):a.update(range_blocks=2.3,windup_ticks='ambush14/close12',cooldown_ticks='active28+recovery24+cooldown40',effects_and_duration_ticks='first hit Slowness30',proposed_category='A5',coefficient=1.15)
    if id=='nochnitsa' and name=='claw':a.update(windup_ticks=10,cooldown_ticks=24)
    if id=='grey_wolf' and name in {'BITE','DOUBLE_CLAW','POUNCE','FLANK_DASH'}:a.update(cooldown_ticks='max44,duration+14+random0..7; FLANK minimum280',notes=a['notes']+'; '+ref('wolf/GreyWolf','nextAttack=now+Math.max'))
    if id=='elder_vodyanoy':a['cooldown_ticks']='recovery60; phase3 30; additional action duration';a['windup_ticks']=40 if name=='wave' else 26 if name=='heavy' else 32
    if id in {'ovinnik','fire_serpent'} and name in {'EMBER','ember fallback','mor ember volley'}:a.update(current_direct_damage='5; rain4.25' if id=='ovinnik' else 'ElementRules.fireDamage(4,rain)',range_blocks='splash1.5; lifetime60; flight path',source=a['source']+'; '+ref('hunt/EmberClump','private float damage'))
    if id=='karachun_raven':a.update(current_direct_damage='3 default; NightConfig ravenDamage override',cooldown_ticks='100 default; NightConfig ravenCooldown',notes=ref('karachun/NightConfig','Map.entry("ravenDamage"'))
    # All field-level uncertainties are explicit, and proposals remain proposals.
    a['status']='HANDLER_VERIFIED; FIELDS_UNVERIFIED' if any('UNVERIFIED' in str(v) for k,v in a.items() if k!='status') else 'SOURCE_VERIFIED; runtime NOT_RUN'

writecsv('CREATURES.csv',rows)
writecsv('ATTACKS.csv',ATT)
summary={'entity_types':len(rows),'creatures':sum(r['group']!='auxiliary' for r in rows),'auxiliary':sum(r['group']=='auxiliary' for r in rows),'attack_rows':len(ATT),'groups':{g:sum(r['group']==g for r in rows) for g in sorted({r['group'] for r in rows})},'unverified_creature_fields':sum('UNVERIFIED' in str(v) for r in rows for v in r.values()),'unverified_attack_fields':sum('UNVERIFIED' in str(v) for r in ATT for v in r.values())}
(OUT/'completion-summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
# Machine-readable outstanding work. Counting cells, including status strings,
# is not a measure of how many independent facts remain to be verified.
pending=[]
for table,data in [('CREATURES',rows),('ATTACKS',ATT)]:
    for row in data:
        for field,value in row.items():
            if field in {'status','notes','resistances_immunities','spawn_conditions'}:continue
            if 'UNVERIFIED' in str(value):pending.append(dict(table=table,entity_type=row['entity_type'],attack=row.get('attack',''),field=field,value=value))
writecsv('UNVERIFIED.csv',pending)
def cited(short,token):return '`'+ref(short,token)+'`'
forms=f'''# Формы существ — продолжение 1.9.8.1

Форма не равна EntityType. Номера состояний ниже подтверждены исходниками;
восстановление после настоящего игрового перезахода NOT_RUN.

## Лихо

Обычное `slavicmyths:likho_one_eyed` — `WorldBoss`, 320 HP, базовая атака11,
броня10/стойкость2/KB .65: {cited('hunt/BossKind','LIKHO(')},
{cited('hunt/WorldBoss','public static AttributeSupplier')}.
Фазы обычного Лиха: >65%, ≤65%, ≤30% ({cited('hunt/BossRules','public static int phase')}).
Моровое Лихо использует тот же ID; kind5 в {cited('mor/MorController','public int kind()')}.
Постоянный `mor_health` ADD_MULTIPLIED_TOTAL +1 даёт640 HP, `mor_damage` +.05
даёт11.55 атрибутного урона; спецатаки HigherMor имеют отдельные literal12/14.
Вторая моровая фаза при50%: {cited('mor/HigherRules','phase2')},
{cited('mor/HigherMor','if(!phase&&')}. Название «проклятое Лихо» не задаёт
другой EntityType или третью доказанную форму. Цель350 относится к обычному;
целевые HP морового/«проклятого» — UNVERIFIED.

## Волколаки и Оборотень

`slavicmyths:volkolak` — VolkolakEntity → HuntMob,165 HP/base9,
отдельная ритуальная охота ({cited('hunt/HuntMob','public static AttributeSupplier')}).
`slavicmyths:cursed_volkolak` — CursedVolkolak,400 HP/base15/armor7/KB .6,
самостоятельный ID ({cited('volkolak/CursedVolkolak','Attributes.MAX_HEALTH')}).
Его ru_ru имя из существующего индекса: «Проклятый Оборотень». Это подтверждает
соответствие локализации, но не автоматическую замену всех употреблений слова
«Оборотень» в старых ТЗ. Утвердить, что цель700 относится именно к этому ID.
Фазы на250/100 HP; UI знаменатель400 — отдельные места будущего изменения:
{cited('volkolak/CursedVolkolak','int next=getHealth()')},
{cited('volkolak/CursedVolkolak','bossbar.setVisible')}.
UPRIGHT меняет габариты/скорость; это поза того же моба. Escape сюжетного экземпляра
перехватывает смертельный урон, оставляет1 HP и удаляет актёра после сцены;
не распространять это на независимый summon/egg:
{cited('volkolak/VolkolakEvents','public static void lethal')},
{cited('volkolak/CursedVolkolak','public void escape()')}.

## Все формы Мора

| kind | Исходное существо | HP normal → mor | Атрибут ATTACK_DAMAGE |
|---|---|---|---|
|1|Леший|100→250|8→9.6|
|2|Полудница|60→120|7→7.84|
|3|Кикимора|36→72|5→5.25|
|4|Огненный змей|155→387.5|7→8.4|
|5|Лихо|320→640|11→11.55|
|6|Овинник|175→350|10→10.5|

Источник всех множителей: {cited('mor/MorController','private void attributes(')};
базы: CREATURES.csv. Это атрибуты, а не множитель каждого literal спецудара.
State0 обычный,1 трансформация50 тиков,2 заражённый. Natural сразу2 и полное HP;
склянка начинает1 и затем сохраняет долю здоровья:
{cited('mor/MorController','public boolean apply(')},
{cited('mor/MorRules','public static float health')},
{cited('mor/MorFlaskProjectile','protected void onHit(')}.
Склянка радиус3, доступны kinds1..6; protected/name/friend/owner/passenger/clone
отсекаются. Natural только kinds1..4: Overworld с7-го дня, >160 от world spawn,
NATURAL/SPAWNER, roll<4 из100, нет другого заражённого/ожидающего в48×24×48:
{cited('mor/MorEvents','public static void spawn')},
{cited('mor/MorRules','public static boolean natural')}.
Таким образом моровые Лихо/Овинник не входят в этот natural обработчик.
Клоны морового змея — тот же FIRE_SERPENT ID с MOR_CLONE и parent UUID;
HP30, максимум2, lifetime1800; summon один раз при≤50%, safe placement/retry20.
{cited('mor/MorController','void spawnClones()')},
{cited('mor/MorController','private boolean cloneTick()')}.
Это не обычный `serpent_projection` (отдельная краткая приманка).
NBT MorSnapshot/HigherSnapshot + постоянные modifiers; повторное добавление
защищено hasModifier(id). Порядок load важен: subclasses HuntMob/ElementHuntMob/
WorldBoss повторно устанавливают base attributes после общего load. Это риск
изменения баз, не доказанное удвоение modifiers:
{cited('mor/MorController','public void load(')},
{cited('hunt/ElementHuntMob','public void readAdditionalSaveData')}.

## Хранители

Оба ID сохраняются после очищения; общий GuardianEntity/GuardianRules.
SEALED/IDLE, пробуждение60, бой, несмертельный предел5%, PURIFY60, CLEANSED;
порог огненного25 HP, водного22.5 HP:
{cited('guardians/GuardianRules','public static float threshold')},
{cited('guardians/GuardianEntity','public static AttributeSupplier')}.
Огненный: lockdown доступен≤75%; LastEmbers≤25%, warning18 вместо24,
pulse rest224 вместо280. Водный: desperation≤30%,2 WaterEcho по25 HP,
whip cooldown110 вместо140; лечение запрещеноStandalone и двумя закрытыми вентилями.
{cited('guardians/FireGuardian','public boolean lastEmbers()')},
{cited('guardians/SpringKeeper','public boolean desperation()')},
{cited('guardians/SpringKeeper','EchoesCalled')}.
Структурная/Standalone привязка — вариант встречи, не новый ID;
очищение и выдача фрагментов не равны убийству.

## Горыныч

PATROL0 → GROUND1 → TRANSFORM2 при1800 → AIR3; EXHAUSTED4 при180;
CLEANSING5 с Clean bitmask трёх голов → PURIFIED6 после160.
TRANSFORM120, крылья на40; air цикл400 с наземными200 и воздушными200.
3600 логических HP проецируются в1024 служебных; головы multipart не имеют
самостоятельной полосы HP или registry ID.
{cited('gorynych/GorynychRules','HP=3600')},
{cited('gorynych/Gorynych','private void flight()')},
{cited('gorynych/Gorynych','public boolean hurt(')}.
NBT сущности GorynychHP/state/clean/wings/age/node и SavedData HP/state/clean/wings
должны мигрировать согласованно; configure может заменить entity NBT данными ledger.
После очищения HP снова3600. Самостоятельная встреча не даёт сюжетных наград.

## Прочие состояния, не новые существа

- Wildlife: медведица120 HP/armor6/damage8 и оленёнок12/damage0 того же
  WildlifeEntity ID: {cited('entity/WildlifeEntity','entityData.set(MOTHER,true)')},
  {cited('entity/WildlifeEntity','public void fawn(')}.
- SWAMP Русалка36 и Водяной72 вместо28/44: {cited('water/WaterSpirit','double hp=')}.
- Подземник шахтёр40/base6.5 вместо28/5, Variant NBT:
  {cited('underground/Podzemnik','public void variant(')}.
- Песиголовец HUNTER40/SCOUT28/LEADER50, один ID, роль сохраняется:
  {cited('pesigolovtsy/DogheadRules','HUNTER(40')},
  {cited('pesigolovtsy/DogheadEntity','public void readAdditionalSaveData')}.
- Полудница calm/transform/hostile: FORM state, не EntityType:
  {cited('entity/PoludnitsaEntity','public void provoke(')}.
- Тугарин PHASE_ONE/TRANSFORMING/PHASE_TWO на50%, incoming*.15/*.5:
  {cited('hunt/TugarinCombat','void startTransformationIfNeeded()')},
  {cited('hunt/WorldBoss','public boolean hurt(')}.
- Маринка falseBody/veil/bird и Добрыня bull: состояния актёров; HexAnchor,
  TurMinion — auxiliary: {cited('marinka/MarinkaEntity','case SHADOW')},
  {cited('marinka/MarinkaEntity','case CURSE')}.
- Финист morph/curse/falcon actor: тот же союзник;
  {cited('finist/FinistEntity','private void leapTick()')}.
- Морозко/Карачун trial phases и yield5%, IceDouble/PlayerDouble/NightRaven
  — вспомогательные зарегистрированные сущности; Морозко1000, Карачун1200 default
  configurable, Калин1800, Полоз3000 getMaxHealth. Высокие HP нельзя выводить из
  одного MAX_HEALTH в регистрации.

Полнота остальных комбинаций NBT/экипировки/событий — UNVERIFIED;
этот документ не является игровым roundtrip тестом.
'''
(OUT/'FORMS.md').write_text(forms,encoding='utf-8')
print(json.dumps(summary,ensure_ascii=False))

