"""0.3 additions: original cuboid UV textures, synthetic Vorbis and additive data."""
from pathlib import Path
import json, math, random, sys
from generate_textures import png

def generate(root):
    res = root/'src/main/resources'
    def js(path, obj):
        p = res/path; p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    labels = {
        'item.slavicmyths.leshy_heart': ('Сердцевина Лешего','Leshy Heartwood'),
        'item.slavicmyths.domovoy_spawn_egg': ('Яйцо призыва Домового','Domovoy Spawn Egg'),
        'item.slavicmyths.leshy_spawn_egg': ('Яйцо призыва Лешего','Leshy Spawn Egg'),
        'entity.slavicmyths.domovoy': ('Домовой','Domovoy'),
        'entity.slavicmyths.leshy': ('Леший','Leshy'),
        'advancements.slavicmyths.root.title': ('Славянские мифы','Slavic Myths'),
        'advancements.slavicmyths.root.description': ('Откройте следы древних сил. Начните с бересты или диких растений.','Discover traces of old powers. Begin with birch bark or wild plants.'),
    }
    advances = [
        ('meet_domovoy','root','domovoy_spawn_egg','domovoy',('Кто здесь хозяин?','Who Lives Here?'),('Нажмите ПКМ пустой рукой по Домовому. Ищите его редко возле деревень днём.','Right-click a Domovoy with an empty hand. Rarely found near villages in daylight.')),
        ('meet_leshy','root','leshy_spawn_egg','leshy',('Не один в этом лесу','Not Alone in the Woods'),('Нажмите ПКМ пустой рукой по Лешему. Не подходите вплотную ночью и не бейте его.','Right-click a Leshy with an empty hand. Avoid approaching closely at night or attacking.')),
        ('leshy_heart','meet_leshy','leshy_heart',None,('Сердце леса','Heart of the Forest'),('Получите Сердцевину Лешего: редкий трофей после победы (шанс 35%).','Obtain Leshy Heartwood: a rare trophy after defeating a Leshy (35% chance).')),
    ]
    for aid,parent,icon,entity,title,desc in advances:
        labels['advancements.slavicmyths.'+aid+'.title']=title
        labels['advancements.slavicmyths.'+aid+'.description']=desc
        criterion = {'trigger':'minecraft:player_interacted_with_entity','conditions':{'entity':{'type':'slavicmyths:'+entity}}} if entity else {'trigger':'minecraft:inventory_changed','conditions':{'items':[{'item':'slavicmyths:leshy_heart'}]}}
        js(Path('data/slavicmyths/advancements')/(aid+'.json'),{'parent':'slavicmyths:'+parent,'display':{'icon':{'item':'slavicmyths:'+icon},'title':{'translate':'advancements.slavicmyths.'+aid+'.title'},'description':{'translate':'advancements.slavicmyths.'+aid+'.description'},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'encounter' if entity else 'obtained':criterion}})
    rootpath=res/'data/slavicmyths/advancements/root.json'
    advancement=json.loads(rootpath.read_text(encoding='utf-8'))
    advancement['criteria']={'first_join':{'trigger':'minecraft:tick'}}
    js(Path('data/slavicmyths/advancements/root.json'),advancement)
    for entity in ('domovoy','leshy'):
        js(Path('assets/slavicmyths/models/item')/(entity+'_spawn_egg.json'),{'parent':'minecraft:item/template_spawn_egg'})
    js(Path('assets/slavicmyths/models/item/leshy_heart.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/leshy_heart'}})
    js(Path('data/slavicmyths/loot_tables/entities/domovoy.json'),{'type':'minecraft:entity','pools':[]})
    js(Path('data/slavicmyths/loot_tables/entities/leshy.json'),{'type':'minecraft:entity','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'},{'condition':'minecraft:random_chance','chance':0.35}],'entries':[{'type':'minecraft:item','name':'slavicmyths:leshy_heart'}]}]})
    soundnames=['domovoy_ambient','domovoy_hurt','domovoy_death','leshy_ambient','leshy_hurt','leshy_death','leshy_angry']
    subtitles=[('Домовой ворчит','Domovoy murmurs'),('Домовой вскрикивает','Domovoy hurts'),('Домовой затихает','Domovoy dies'),('Леший шелестит','Leshy rustles'),('Кора трещит','Bark cracks'),('Леший затихает','Leshy dies'),('Леший рычит','Leshy growls')]
    sounds={}
    for name,text in zip(soundnames,subtitles):
        labels['subtitles.slavicmyths.'+name]=text
        sounds[name]={'subtitle':'subtitles.slavicmyths.'+name,'sounds':[{'name':'slavicmyths:'+name,'volume':0.8}]}
    js(Path('assets/slavicmyths/sounds.json'),sounds)
    for lang,index in [('ru_ru',0),('en_us',1)]:
        path=Path('assets/slavicmyths/lang')/(lang+'.json')
        data=json.loads((res/path).read_text(encoding='utf-8'))
        data.update({key:value[index] for key,value in labels.items()});js(path,data)

    # Cuboid UV regions match the original Java models, not a reskin of a vanilla mob.
    for kind in ('domovoy','leshy'):
        rng=random.Random(300 if kind=='domovoy' else 301)
        pixels=[[(0,0,0,255) for x in range(64)] for y in range(64)]
        def fill(x,y,w,h,color,noise=8):
            for py in range(y,y+h):
                for px in range(x,x+w):
                    delta=rng.randint(-noise,noise)
                    pixels[py][px]=tuple(max(0,min(255,c+delta)) for c in color)+(255,)
        if kind=='domovoy':
            fill(0,0,30,15,(176,139,103));fill(0,16,26,13,(149,126,82))
            fill(32,0,16,9,(182,171,148));fill(48,0,8,4,(181,136,97))
            fill(32,16,12,11,(149,126,82));fill(0,32,14,9,(79,57,40))
            fill(0,44,32,10,(105,65,44));fill(0,25,26,2,(111,45,38),2)
            # Front head UV is u=depth, v=depth; eyes under brows.
            fill(8,9,2,1,(78,65,50),0);fill(12,9,2,1,(78,65,50),0)
            fill(8,10,2,1,(33,39,30),0);fill(12,10,2,1,(33,39,30),0)
            for x in range(34,40,2):fill(x,3,1,5,(151,141,121),4)
        else:
            fill(0,0,64,64,(70,63,45),12)
            for x in range(0,64,4):fill(x,0,1,64,(43,44,32),6)
            for x,y in [(5,19),(13,26),(26,24),(3,40),(32,9),(43,5)]:fill(x,y,3,5,(69,87,42),10)
            fill(6,8,6,3,(32,35,24),2)
            fill(7,9,1,1,(223,231,124),0);fill(10,9,1,1,(223,231,124),0)
        png(res/f'assets/slavicmyths/textures/entity/{kind}.png',pixels,64,64)
    rng=random.Random(302)
    heart=[[(0,0,0,0) for _ in range(16)] for _ in range(16)]
    for y in range(2,14):
        for x in range(3,13):
            if abs(x-7.5)/5+abs(y-7.5)/7<1.35:
                d=rng.randint(-12,12);heart[y][x]=(102+d,79+d,45+d,255)
    for y in range(4,12):
        x=7+(y%3==0);heart[y][x]=(158,191,82,255)
        if y in (5,8,10):heart[y][x+1]=(213,228,135,255)
    png(res/'assets/slavicmyths/textures/item/leshy_heart.png',heart,16,16)

    # libsndfile encodes real Vorbis; no renamed WAV files or borrowed recordings.
    sys.path.insert(0,str(root/'.tools/audio-libs'))
    import numpy as np
    import soundfile as sf
    rate=22050
    for index,name in enumerate(soundnames):
        duration=[0.65,0.28,0.75,1.1,0.4,1.2,0.85][index]
        t=np.arange(int(duration*rate))/rate
        rng=np.random.default_rng(index+300)
        noise=rng.normal(0,1,len(t)); low=np.convolve(noise,np.ones(35)/35,mode='same')
        envelope=np.sin(np.pi*t/duration)**1.5
        if name.startswith('domovoy'):
            frequency=110+12*np.sin(t*19)+(35 if 'hurt' in name else 0)
            phase=np.cumsum(frequency)*2*np.pi/rate
            samples=(0.35*np.sin(phase)+0.1*np.sin(phase*2.1)+0.25*low)*envelope*(0.65+0.35*np.sin(t*32)**2)
        else:
            phase=2*np.pi*(55*t+6*np.sin(t*5))
            samples=(0.22*np.sin(phase)+0.55*low)*envelope
            for when in (0.08,0.23,0.41):
                samples+=0.08*np.sin(2*np.pi*430*t)*np.exp(-np.maximum(0,t-when)*65)*(t>=when)
        samples=np.clip(samples,-0.8,0.8).astype('float32')
        path=res/f'assets/slavicmyths/sounds/{name}.ogg';path.parent.mkdir(parents=True,exist_ok=True)
        # Vorbis writes a fresh stream serial on each encode. Preserve an existing
        # validated recording so regeneration does not churn bytes or user audio edits.
        if not path.exists(): sf.write(path,samples,rate,format='OGG',subtype='VORBIS')
        decoded,sr=sf.read(path)
        assert sr==rate and len(decoded)>0 and np.isfinite(decoded).all()

if __name__=='__main__': generate(Path(__file__).resolve().parents[1])
