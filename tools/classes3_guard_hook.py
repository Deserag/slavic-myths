from pathlib import Path
p=Path('src/main/java/org/slavicmyths/military/GuardBehavior.java')
s=p.read_text(encoding='utf-8')
s=s.replace('private LivingEntity enemy;','private LivingEntity enemy,beforeBanner;private boolean bannerControlled;')
s=s.replace('if(now>=search){search=now+40;var hurt=', 'if(now>=search){search=now+40;var priority=org.slavicmyths.rpg.classes.ClassUltimateRuntime.priority(v);if(priority!=null){if(!bannerControlled)beforeBanner=enemy;enemy=priority;bannerControlled=true;}else{if(bannerControlled){enemy=beforeBanner!=null&&beforeBanner.isAlive()?beforeBanner:null;beforeBanner=null;bannerControlled=false;}var hurt=')
s=s.replace('.orElse(null);}\n  if(enemy!=null)', '.orElse(null);}}\n  if(enemy!=null)')
s=s.replace('enemy=null;}\n}', 'enemy=null;beforeBanner=null;bannerControlled=false;}\n}')
p.write_text(s,encoding='utf-8')
