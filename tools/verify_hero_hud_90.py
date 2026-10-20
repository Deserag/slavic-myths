"""#90: execute production selection/layout/drawing on JVM and validate the production artifact.
CPU previews replay actual draw calls with the bundled vanilla bitmap glyphs, not Minecraft screenshots.
"""
from pathlib import Path
from io import BytesIO
import json,math,hashlib,subprocess,tomllib,zipfile
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/hero-hud-90-v3';JDK=Path('C:/Program Files/Java/jdk-21.0.12/bin')

def font(z):
 glyphs={};advance={32:4}
 data=json.loads(z.read('assets/minecraft/font/include/default.json'))
 for p in data['providers']:
  if p['type']!='bitmap':continue
  namespace,path=p['file'].split(':');im=Image.open(BytesIO(z.read(f'assets/{namespace}/textures/{path}'))).convert('RGBA')
  cellw=im.width//len(p['chars'][0]);cellh=im.height//len(p['chars']);height=p.get('height',8);factor=height/cellh
  for y,row in enumerate(p['chars']):
   for x,c in enumerate(row):
    if c=='\0' or ord(c) in glyphs:continue
    tile=im.crop((x*cellw,y*cellh,(x+1)*cellw,(y+1)*cellh));bbox=tile.getchannel('A').getbbox();width=bbox[2] if bbox else 0
    advance[ord(c)]=int(.5+width*factor)+1
    glyphs[ord(c)]=(tile.resize((round(cellw*factor),height),Image.Resampling.NEAREST),7-p['ascent'])
 return glyphs,advance

def previews(captures,glyphs,advance):
 sheet=Image.new('RGBA',(1920,1110),'#21272b');draw=ImageDraw.Draw(sheet)
 draw.text((8,3),'#90 actual production draw calls + vanilla glyphs. OFFLINE, not Minecraft.',fill='white')
 for capture in captures:
  n=capture['count'];w,h=capture['width'],capture['height'];scale=capture['gui_scale'];tile=Image.new('RGBA',(w,h),'#3c4348');d=ImageDraw.Draw(tile)
  d.text((6,2),f'{n} companions / GUI scale {scale} fixture',fill='white')
  # Replay at unscaled coordinates first, then apply the same .75 pose scale.
  layer=Image.new('RGBA',(math.ceil(w/.75),math.ceil(h/.75)))
  for op in capture['ops']:
   value=op['color'] & 0xffffffff;rgb=((value>>16)&255,(value>>8)&255,value&255)
   if op['op']=='fill':
    if op['r']>op['x'] and op['b']>op['y']:ImageDraw.Draw(layer).rectangle((op['x'],op['y'],op['r']-1,op['b']-1),fill=(*rgb,(value>>24)&255))
   else:
    x=op['x'];y=op['y']
    for c in op['text']:
     if ord(c) in glyphs:
      glyph,dy=glyphs[ord(c)];mask=glyph.getchannel('A');ink=Image.new('RGBA',glyph.size,(*rgb,255));ink.putalpha(mask)
      shadow=Image.new('RGBA',glyph.size,tuple(v//4 for v in rgb)+(255,));shadow.putalpha(mask)
      layer.alpha_composite(shadow,(x+1,y+dy+1));layer.alpha_composite(ink,(x,y+dy))
     else:assert c==' ',repr(c)
     x+=advance.get(ord(c),6)
  tile.alpha_composite(layer.resize((w,h),Image.Resampling.NEAREST))
  # Legend identifies reserved areas; it does not pretend to render an actual game scene.
  d=ImageDraw.Draw(tile);d.text((max(6,w-245),h-16),'Bottom area reserved for vanilla bars',fill='#b7c1c7')
  sheet.alpha_composite(tile.resize((640,360),Image.Resampling.NEAREST),((n-1)*640,(scale-2)*360+30))
 sheet.save(O/'hud-1-3-scales-2-4.png')

def main():
 O.mkdir(parents=True,exist_ok=True);out=R/'.tools/hero-hud90';out.mkdir(parents=True,exist_ok=True)
 vanilla=R/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar'
 with zipfile.ZipFile(vanilla) as z:glyphs,advance=font(z)
 metrics=out/'advances.tsv';metrics.write_text(''.join(f'{k}\t{v}\n' for k,v in advance.items()),encoding='utf-8')
 subprocess.run([str(JDK/'javac.exe'),'-encoding','UTF-8','-d',str(out),str(R/'src/main/java/org/slavicmyths/client/HeroHudView.java'),str(R/'tools/hero-hud-tests/HeroHudChecks.java')],check=True)
 result=json.loads(subprocess.check_output([str(JDK/'java.exe'),'-cp',str(out),'HeroHudChecks',str(metrics)],text=True,encoding='utf-8'))
 assert result['cases']==32 and result['result']=='HERO_HUD_OFFLINE PASS'
 (O/'production-draw-trace.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');previews(result['previews'],glyphs,advance)
 src=(R/'src/main/java/org/slavicmyths/client/HeroHud.java').read_text(encoding='utf-8-sig')
 for key in ('mc.player == null','mc.level == null','mc.screen != null','mc.options.hideGui','BogatyrConfig.HUD.get()','entitiesForRendering()','64 * 64','syncedOwner()','HeroHudView.select','HeroHudView.draw','Dist.CLIENT'):
  assert key in src,key
 for key in ('Math.min(2','getEntitiesOfClass','sendToServer','lastStand','recoveryEnd','restSince','KalinRetreated'):assert key not in src,key
 calls=(R/'src/main/java/org/slavicmyths/skim/HeroCalls.java').read_text(encoding='utf-8')
 assert 'count(p)>=(polozParty?3:2)' in calls and 'PolozEvent.active(w)&&!id.equals("finist")' in calls
 # Existing entity state/ownership and server lifetime logic must remain unchanged.
 protected=['src/main/java/org/slavicmyths/bogatyr/IlyaEntity.java','src/main/java/org/slavicmyths/marinka/DobrynyaEntity.java','src/main/java/org/slavicmyths/skim/AlyoshaEntity.java','src/main/java/org/slavicmyths/finist/FinistEntity.java','src/main/java/org/slavicmyths/kalin/KalinParty.java','src/main/java/org/slavicmyths/skim/HeroCalls.java']
 for p in protected:assert subprocess.check_output(['git','show','9120c67b:'+p],cwd=R).decode().replace('\r\n','\n')==(R/p).read_text(encoding='utf-8'),p
 config=(R/'src/main/java/org/slavicmyths/bogatyr/HeroHudConfig.java').read_text(encoding='utf-8-sig');assert 'net.minecraft.client' not in config
 langs={l:json.loads((R/f'src/main/resources/assets/slavicmyths/lang/{l}.json').read_text(encoding='utf-8')) for l in ('ru_ru','en_us')}
 for key in ('hero_hud.slavicmyths.active','hero_hud.slavicmyths.retreating','hero_hud.slavicmyths.cursed','gui.slavicmyths.174.recovery'):assert all(key in values for values in langs.values()),key
 props=dict(l.split('=',1) for l in (R/'gradle.properties').read_text().splitlines() if '=' in l);jar=R/f"build/libs/slavicmyths-{props['mod_version']}.jar";resources=classes=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  metadata=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']==props['mod_version'] for m in metadata['mods'])
  assert not any('HeroHudChecks' in n or n.startswith(('tools/','smoke/')) or '/verify/' in n for n in z.namelist())
  for p in (R/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   key=p.relative_to(R/'src/main/resources').as_posix()
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if key!='META-INF/neoforge.mods.toml':assert z.read(key)==p.read_bytes(),key
   resources+=1
  for p in (R/'build/classes/java/main').rglob('*.class'):
   assert z.read(p.relative_to(R/'build/classes/java/main').as_posix())==p.read_bytes();assert int.from_bytes(p.read_bytes()[6:8],'big')==65;classes+=1
 log=(R/'.tools/hero-hud90-v3-build.log').read_text(errors='replace');assert 'BUILD SUCCESSFUL' in log
 report=dict(issue=90,status='IN_PROGRESS / OWNER_PENDING',version=props['mod_version'],jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),resources=resources,java21_classes=classes,offline_cases=result['cases'],selection='UUID dedup, owner/alive/range boundary',render='actual production Canvas calls and vanilla bitmap glyphs; OFFLINE, no Minecraft/GL',build='PASS',runtime='NOT_RUN',minecraft_launches=0,polymc='POLYMC_NOT_INSTALLED',owner_acceptance=False,lifetime_cooldown='NOT_ADDED: #78 authoritative synchronization absent')
 (O/'checks.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,ensure_ascii=False))
if __name__=='__main__':main()
