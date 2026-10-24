package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.tales.TaleNetwork;
import org.slavicmyths.bayun.BayunContent;

/** Responsive parchment reader. Font remains native size at high GUI scale. */
public final class TaleScreen extends Screen {
    private static CompoundTag snapshot=new CompoundTag();
    private static long discoveryReady;
    public static void clear(){snapshot=new CompoundTag();discoveryReady=0;}
    private static final String[] PARTS={"tales","creatures","people","places","crafts","nature","quests","deeds"};
    private record Entry(String id,String part,String chapter,String section,String title,String icon,String entity,int initial,List<String> links,List<String> recipes){}
    private record Location(String part,String chapter,String section,String entry,int page){}
    private final Map<String,Location> saved=new HashMap<>();private final Deque<Location> history=new ArrayDeque<>(),forward=new ArrayDeque<>();
    private List<Entry> entries=List.of();
    private final Map<String,Entry> entriesById=new HashMap<>();
    private final Map<String,CompoundTag> nodes=new TreeMap<>();
    private final Map<String,Component> components=new HashMap<>();
    private final Map<String,ItemStack> icons=new HashMap<>();
    private final Map<String,String> groups=new HashMap<>();
    private final UiText.Layout pageLayout=new UiText.Layout(),subtitleLayout=new UiText.Layout(),stateLayout=new UiText.Layout();
    private Component pageContent,countLabel;
    private String contentEntry="";
    private int contentPage,contentRecipe;
    private net.minecraft.locale.Language contentLanguage;
    private String part="tales",chapter="",section="",selected="",deedGroup="";private int page=1,left,top,w,h,navWidth,navOffset,textOffset,panX,panY;private float zoom=1;
    private EditBox search;private String query="";private boolean map,drag;private double dragX,dragY;
    private net.minecraft.world.entity.LivingEntity illustration;private String illustrated="";private float mapZoom=1;private int recipeIndex=-1;
    private Map<String,int[]> positionCache;private String positionKey="";
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){g.fill(0,0,width,height,0x88100D0B);}
    @Override protected void renderBlurredBackground(float delta){}
    public TaleScreen(){super(Component.translatable("tales.title"));load(true,true);}
    public static TaleScreen cleansing(){var screen=new TaleScreen();screen.part="crafts";screen.chapter="mor";screen.section="cleansing";screen.selected="slavicmyths:cleansing_knowledge";return screen;}
    public static void receive(TaleNetwork.Snapshot payload){CompoundTag delta=payload.data();if(payload.full())snapshot=delta;else{if(delta.getBoolean("EntriesReset"))snapshot.put("Entries",new CompoundTag());if(delta.getBoolean("AdvancementReset"))snapshot.put("Advancements",new ListTag());for(String key:delta.getAllKeys()){if(key.equals("Entries")){CompoundTag target=snapshot.getCompound("Entries");for(String id:delta.getCompound(key).getAllKeys())target.put(id,delta.getCompound(key).get(id).copy());snapshot.put(key,target);}else if(key.equals("Advancements")){Map<String,CompoundTag> all=new TreeMap<>();for(Tag raw:snapshot.getList(key,10)){CompoundTag n=(CompoundTag)raw;all.put(n.getString("Id"),n);}for(Tag raw:delta.getList(key,10)){CompoundTag n=(CompoundTag)raw;all.put(n.getString("Id"),n);}ListTag list=new ListTag();all.values().forEach(list::add);snapshot.put(key,list);}else snapshot.put(key,delta.get(key).copy());}}
        var mc=Minecraft.getInstance();if(!payload.open()&&!payload.full()&&delta.contains("Entries")&&mc.level!=null&&mc.level.getGameTime()>=discoveryReady){sound("book.discovery");discoveryReady=mc.level.getGameTime()+40;}if(payload.open()){mc.setScreen(new TaleScreen());sound("book.open");}else if(mc.screen instanceof TaleScreen screen){screen.load(payload.full()||delta.contains("Catalog"),payload.full()||delta.contains("Advancements")||delta.getBoolean("AdvancementReset"));screen.init();}}
    private static void sound(String cue){Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(BayunContent.sound(cue),1,.38F));}
    /** Only changed snapshot sections are decoded; progress deltas do not rebuild the catalog/tree. */
    private void load(boolean catalogChanged,boolean advancementsChanged){
        pageContent=null;
        if(catalogChanged){
            List<Entry> list=new ArrayList<>();entriesById.clear();
            for(Tag raw:snapshot.getList("Catalog",10)){
                CompoundTag n=(CompoundTag)raw;
                List<String> links=new ArrayList<>(),recipes=new ArrayList<>();
                for(Tag r:n.getList("Links",8))links.add(r.getAsString());
                for(Tag r:n.getList("Recipes",8))recipes.add(r.getAsString());
                Entry entry=new Entry(n.getString("Id"),n.getString("Part"),n.getString("Chapter"),n.getString("Section"),n.getString("Title"),n.getString("Icon"),n.getString("Entity"),n.getInt("Initial"),List.copyOf(links),List.copyOf(recipes));
                list.add(entry);entriesById.put(entry.id,entry);
            }
            entries=List.copyOf(list);
        }
        if(advancementsChanged){
            positionCache=null;groups.clear();components.clear();nodes.clear();
            for(Tag raw:snapshot.getList("Advancements",10)){
                CompoundTag n=(CompoundTag)raw;nodes.put(n.getString("Id"),n);
            }
        }
        if(catalogChanged||advancementsChanged)icons.clear();
        int unlocked=0;var progress=snapshot.getCompound("Entries");
        for(Entry entry:entries)if(progress.getCompound(entry.id).getInt("Stage")>=2)unlocked++;
        countLabel=Component.translatable("tales.count_short",unlocked,entries.size());
    }
    private Entry current(){return entriesById.get(selected);}
    private int stage(Entry e){return Math.max(e.initial,Math.clamp(snapshot.getCompound("Entries").getCompound(e.id).getInt("Stage"),0,4));}
    private Location location(){return new Location(part,chapter,section,selected,page);}
    private void restore(Location l){releaseIllustration();part=l.part;chapter=l.chapter;section=l.section;selected=l.entry;page=l.page;textOffset=navOffset=0;map=false;recipeIndex=-1;init();}
    private void remember(){forward.clear();history.push(location());if(history.size()>64)history.removeLast();}
    private void choosePart(String p){query="";deedGroup="";panX=panY=0;saved.put(part,location());remember();restore(saved.getOrDefault(p,new Location(p,"","","",1)));}
    private void select(Entry e){releaseIllustration();remember();part=e.part;chapter=e.chapter;section=e.section;selected=e.id;page=1;textOffset=0;map=false;recipeIndex=-1;PacketDistributor.sendToServer(new TaleNetwork.Read(e.id));sound("book.page");init();}
    private Component heading(String type,String key){return Component.translatable("tales."+type+"."+key);}
    private boolean portrait(){var e=current();return e!=null&&stage(e)>=2&&h>=280&&navWidth>=100;}
    private int rows(){return Math.max(1,(h-127-(portrait()?85:0))/34);}
    private void button(int x,int y,int width,int height,Component text,boolean active,Runnable action){var b=FolkUi.button(x,y,width,height,text,false,v->action.run());b.active=active;addRenderableWidget(b);}
    @Override protected void init(){
        clearWidgets();navigation.clear();w=Math.min(900,width-12);h=height-12;left=(width-w)/2;top=(height-h)/2;navWidth=Math.max(96,Math.min(220,w/3));
        for(int i=0;i<PARTS.length;i++){String target=PARTS[i];int tw=(w-64)/4;var tab=FolkUi.button(left+32+(i%4)*tw,top+29+(i/4)*21,tw-3,19,heading("tab",target),target.equals(part),v->choosePart(target));tab.setTooltip(Tooltip.create(heading("part",target)));addRenderableWidget(tab);}
        if(snapshot.getInt("Rewards176")>0)button(left+w-170,top+4,140,18,Component.translatable("reward176.claim",snapshot.getInt("Rewards176")),true,()->PacketDistributor.sendToServer(new TaleNetwork.Read("__rewards176")));
        button(left+82,top+4,Math.max(80,Math.min(130,w-270)),18,org.slavicmyths.quest185.Quest185Story.text("chapter"),true,Quest185Screen::request);
        button(left+w-25,top+4,20,18,Component.literal("×"),true,this::onClose);
        button(left+32,top+4,22,18,Component.literal("←"),!history.isEmpty()||!chapter.isEmpty(),()->{if(!history.isEmpty()){forward.push(location());restore(history.pop());}else if(!section.isEmpty()){section="";selected="";init();}else{chapter="";init();}});
        button(left+57,top+4,22,18,Component.literal("→"),!forward.isEmpty(),()->{history.push(location());restore(forward.pop());});
        search=new EditBox(font,left+34,top+76,w-75,17,Component.translatable("tales.search"));search.setMaxLength(64);search.setHint(Component.translatable("tales.search"));search.setValue(query);search.setResponder(v->{query=v;navOffset=0;panX=panY=0;positionCache=null;rebuildNavigation();});addRenderableWidget(search);button(left+w-35,top+74,22,20,Component.literal("×"),true,()->{search.setValue("");search.setFocused(true);});
        rebuildNavigation();
        int rx=left+navWidth+39,rw=w-navWidth-54;
        if(part.equals("deeds")){button(rx,top+h-25,32,19,Component.literal("−"),true,()->zoom=Math.max(.4F,zoom-.2F));button(rx+36,top+h-25,32,19,Component.literal("+"),true,()->zoom=Math.min(1.6F,zoom+.2F));button(rx+72,top+h-25,Math.min(100,rw-72),19,Component.translatable("tales.center"),true,()->{panX=panY=0;zoom=1;});return;}
        Entry e=current();if(e!=null){page=Math.clamp(page,1,Math.max(1,stage(e)));int y=top+h-25;button(rx,y,28,19,Component.literal("‹"),page>1,()->{page--;textOffset=0;sound("book.page");init();});button(left+w-42,y,28,19,Component.literal("›"),page<stage(e),()->{page++;textOffset=0;sound("book.page");init();});
            if(snapshot.getCompound("Hints").contains(e.id)){button(rx,top+h-49,Math.min(rw-35,120),20,Component.translatable("tales.map"),true,()->{map=!map;textOffset=0;});button(left+w-47,top+h-49,33,20,Component.literal("×"+mapZoom),true,()->{mapZoom=mapZoom>=2?1:mapZoom+.5F;init();});}
            else if(stage(e)>=3&&!e.recipes.isEmpty()){button(rx,top+h-49,Math.min(rw,150),20,Component.translatable("tales.recipe.open"),true,()->{recipeIndex=recipeIndex+1>=e.recipes.size()?-1:recipeIndex+1;textOffset=0;});}
            else if(!e.links.isEmpty()){String link=e.links.get(0);Entry target=entriesById.get(link);if(target!=null)button(rx,top+h-49,Math.min(rw,150),20,Component.translatable(target.title),true,()->select(target));}
        }
    }
    private final List<AbstractWidget> navigation=new ArrayList<>();
    private void rebuildNavigation(){for(var b:navigation)removeWidget(b);navigation.clear();List<Entry> filtered=entries.stream().filter(e->!e.id.equals("slavicmyths:cleansing_knowledge")||stage(e)>0).filter(e->query.isBlank()?e.part.equals(part):UiSearch.matches(query,Component.translatable(e.title).getString()+" "+heading("part",e.part).getString()+" "+heading("chapter",e.chapter).getString()+" "+heading("section",e.section).getString())).toList();List<String> keys;
        if(part.equals("deeds"))keys=nodes.values().stream().filter(n->query.isBlank()||UiSearch.matches(query,component(n.getString("Title")).getString()+" "+component(n.getString("Description")).getString())).map(n->group(n.getString("Id"))).distinct().sorted().toList();
        else keys=filtered.stream().map(Entry::id).toList();
        navOffset=Math.clamp(navOffset,0,Math.max(0,keys.size()-rows()));
        for(int i=0;i<rows()&&navOffset+i<keys.size();i++){String key=keys.get(navOffset+i);Entry entry=entriesById.get(key);Component label=part.equals("deeds")?heading("group",key):Component.translatable(entry.title);var button=FolkUi.button(left+33,top+102+i*34,navWidth-18,32,label,selected.equals(key)||deedGroup.equals(key),v->{if(part.equals("deeds")){deedGroup=key;panX=panY=0;positionCache=null;}else select(entry);});button.setTooltip(Tooltip.create(entry==null?label:label.copy().append("\n").append(heading("part",entry.part)).append(" · ").append(heading("chapter",entry.chapter)).append(" · ").append(heading("section",entry.section))));navigation.add(button);addRenderableWidget(button);}
    }
    private int textTop(){return top+146;}
    private int textBottom(){return top+h-55;}
    private List<FormattedCharSequence> textLines(){
        var language=net.minecraft.locale.Language.getInstance();
        if(pageContent==null||!contentEntry.equals(selected)||contentPage!=page||contentRecipe!=recipeIndex||contentLanguage!=language){
            pageContent=pageText();contentEntry=selected;contentPage=page;contentRecipe=recipeIndex;contentLanguage=language;
        }
        return pageLayout.split(font,pageContent,Math.max(40,w-navWidth-60));
    }
    /** Build recipe/quest text once per page or snapshot, never once per rendered frame. */
    private Component pageText(){
        Entry e=current();
        if(e==null)return Component.translatable(part.equals("quests")?"tales.quest.none":"tales.locked");
        if(stage(e)==0)return Component.translatable("tales.locked");
        if(recipeIndex>=0&&recipeIndex<e.recipes.size())return TaleRecipeText.display(snapshot.getCompound("Recipes").getString(e.recipes.get(recipeIndex)));
        String suffix=switch(page){case 2->"discovered";case 3->"studied";case 4->"verified";default->"rumor";};
        Component content=Component.translatable("tales.entry."+e.id.split(":",2)[1]+"."+suffix);
        if(e.id.startsWith("slavicmyths:chud_research_")){
            String giver=e.id.endsWith("_yaga")?"yaga":"bayun";
            content=Component.translatable("research.chud.status."+snapshot.getCompound("Research").getInt(giver)).append("\n\n").append(content);
        }else if(part.equals("quests")){
            var q=snapshot.getCompound("Quests");
            content=Component.translatable(q.getString("YagaActive").isEmpty()?"tales.quest.none":q.getString("YagaActive")).append("\n\n").append(Component.translatable("tales.quest.progress",q.getInt("YagaStage"))).append("\n\n").append(content);
        }
        return content;
    }
    private ItemStack icon(String id){
        return icons.computeIfAbsent(id,key->{var location=ResourceLocation.tryParse(key);return location==null?ItemStack.EMPTY:new ItemStack(BuiltInRegistries.ITEM.get(location));});
    }
    @Override public void render(GuiGraphics g,int mx,int my,float delta){renderBackground(g,mx,my,delta);FolkUi.frame(g,left,top,w,h);int divider=left+navWidth+29;g.fill(divider,top+99,divider+5,top+h-7,0xff806447);g.drawString(font,font.plainSubstrByWidth(title.getString(),Math.max(40,w-265)),left+85,top+9,FolkUi.GOLD,false);int rx=divider+10,rw=left+w-14-rx;
        if(part.equals("deeds"))renderDeeds(g,rx,rw,mx,my);else{
            Entry e=current();Component subtitle=e==null?heading("part",part):Component.translatable(e.title);FolkUi.text(g,font,subtitle,rx,top+102,rw,FolkUi.RED,2,subtitleLayout);
            if(e!=null){Component state=Component.translatable("tales.stage."+Math.max(1,page));if(snapshot.getCompound("Entries").getCompound(e.id).getBoolean("Defeated"))state=state.copy().append(" · ").append(Component.translatable("tales.defeated"));FolkUi.text(g,font,state,rx,top+124,rw,FolkUi.GREEN,2,stateLayout);if(map)renderMap(g,e,rx,rw);else{List<FormattedCharSequence> lines=textLines();int count=Math.max(1,(textBottom()-textTop())/12);textOffset=Math.clamp(textOffset,0,Math.max(0,lines.size()-count));g.enableScissor(rx,textTop(),rx+rw,textBottom());for(int i=0;i<count&&textOffset+i<lines.size();i++)g.drawString(font,lines.get(textOffset+i),rx,textTop()+i*12,FolkUi.INK,false);g.disableScissor();if(lines.size()>count){int track=Math.max(1,textBottom()-textTop()),thumb=Math.max(8,track*count/lines.size()),at=(track-thumb)*textOffset/Math.max(1,lines.size()-count);g.fill(rx+rw-2,textTop()+at,rx+rw,textTop()+at+thumb,FolkUi.RED);}}
                g.drawCenteredString(font,Component.translatable("tales.page",page,Math.max(1,stage(e))),rx+rw/2,top+h-18,FolkUi.INK);
            }else{FolkUi.text(g,font,heading("part",part),rx,top+146,rw,FolkUi.INK,4);if(part.equals("quests")){var q=snapshot.getCompound("Quests");FolkUi.text(g,font,Component.translatable(q.getString("YagaActive").isEmpty()?"tales.quest.none":q.getString("YagaActive")),rx,top+178,rw,FolkUi.INK,5);FolkUi.text(g,font,Component.translatable("tales.quest.progress",q.getInt("YagaStage")),rx,top+215,rw,FolkUi.GREEN,3);}}
        }
        if(portrait())illustrate(g,mx,my);g.drawString(font,countLabel,left+34,top+h-20,FolkUi.INK,false);for(var widget:renderables)widget.render(g,mx,my,delta);if(navigation.isEmpty())g.drawWordWrap(font,Component.translatable("tales.search_empty"),left+34,top+104,navWidth-20,FolkUi.INK);

    }
    private void releaseIllustration(){illustration=null;illustrated="";}
    @Override public void removed(){
        releaseIllustration();icons.clear();components.clear();pageContent=null;
        super.removed();
    }
    private void illustrate(GuiGraphics g,int mx,int my){
        var e=current();int x=left+39,y=top+h-103,width=navWidth-28;
        FolkUi.panel(g,x,y,width,78,0xffd9c49a);
        if(!illustrated.equals(e.entity)||(illustration!=null&&illustration.level()!=minecraft.level)){
            releaseIllustration();illustrated=e.entity;
            if(!e.entity.isEmpty()&&minecraft.level!=null){
                var id=ResourceLocation.tryParse(e.entity);
                var entity=id==null?null:BuiltInRegistries.ENTITY_TYPE.get(id).create(minecraft.level);
                illustration=entity instanceof net.minecraft.world.entity.LivingEntity living?living:null;
            }
        }
        if(illustration!=null){
            net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsMouse(g,x+3,y+3,x+width-3,y+75,Math.max(12,Math.min(36,(int)(65/Math.max(1,illustration.getBbHeight())))),0,mx,my,illustration);
            return;
        }
        if(e.id.equals("slavicmyths:cleansing_knowledge")){
            g.blit(ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/item/cleansing_knowledge.png"),x+width/2-16,y+22,32,32,0,0,16,16,16,16);return;
        }
        var itemIcon=icon(e.icon);
        if(!itemIcon.isEmpty()){
            g.pose().pushPose();g.pose().translate(x+width/2-16,y+22,0);g.pose().scale(2,2,1);
            g.renderItem(itemIcon,0,0);g.pose().popPose();
        }
    }
    private void renderMap(GuiGraphics g,Entry e,int x,int width){var hint=snapshot.getCompound("Hints").getCompound(e.id);String error=hint.getBoolean("Stale")?"tales.stale":!hint.getString("Dimension").equals(snapshot.getString("Dimension"))?"tales.other_dimension":"";int y=textTop();g.enableScissor(x,y,x+width,textBottom());if(!error.isEmpty()){FolkUi.text(g,font,Component.translatable(error),x,y,width,FolkUi.RED,8);g.disableScissor();return;}int size=Math.max(40,Math.min(width,Math.max(40,textBottom()-y-65))),cx=x+size/2,cy=y+size/2,r=(int)(size/3*mapZoom);g.enableScissor(x,y,x+size,y+size);g.fill(x,y,x+size,y+size,0xffd9c49a);for(int i=0;i<48;i++){double a=i*Math.PI/24,b=(i+1)*Math.PI/24;if(i%2==0)line(g,cx+(int)(Math.cos(a)*r),cy+(int)(Math.sin(a)*r),cx+(int)(Math.cos(b)*r),cy+(int)(Math.sin(b)*r),FolkUi.RED);}var player=minecraft.player;if(player!=null){double dx=(player.getX()-hint.getDouble("X"))/hint.getInt("Radius"),dz=(player.getZ()-hint.getDouble("Z"))/hint.getInt("Radius");int px=cx+(int)Math.clamp(dx*r,-size/2+4,size/2-4),py=cy+(int)Math.clamp(dz*r,-size/2+4,size/2-4);g.fill(px-2,py-2,px+3,py+3,FolkUi.GREEN);}g.disableScissor();g.drawString(font,Component.translatable("tales.north"),x+2,y+2,FolkUi.INK,false);FolkUi.text(g,font,Component.translatable("tales.schematic"),x,y+size+3,width,FolkUi.INK,2);FolkUi.text(g,font,Component.translatable(hint.getString("Source")).append(" · ").append(Component.translatable("tales.direction."+hint.getInt("Direction"))).append(" · ").append(Component.translatable("tales.distance."+hint.getInt("Distance"))),x,y+size+25,width,FolkUi.GREEN,3);g.disableScissor();}
    private String group(String id){return groups.computeIfAbsent(id,this::findGroup);}
    private String findGroup(String id){String at=id;Set<String> seen=new HashSet<>();while(!at.isEmpty()&&seen.add(at)){if(at.equals("slavicmyths:first_neighbors"))return "peaceful";if(at.equals("slavicmyths:first_structure"))return "exploration";if(at.equals("slavicmyths:meet_bandits"))return "combat";if(at.equals("slavicmyths:tales_come_alive"))return "stories";if(at.contains(":group_"))return at.split(":group_",2)[1];CompoundTag n=nodes.get(at);if(n==null)break;at=n.getString("Parent");}return "tales";}
    private Component component(String json){return components.computeIfAbsent(json,value->{try{var decoded=Component.Serializer.fromJson(value,minecraft.level.registryAccess());return decoded==null?Component.translatable("tales.locked"):decoded;}catch(RuntimeException failure){return Component.translatable("tales.locked");}});}
    private Map<String,int[]> positions(){String key=deedGroup+"|"+query;if(positionCache!=null&&key.equals(positionKey))return positionCache;var visible=new LinkedHashMap<String,String>();for(var n:nodes.values()){String id=n.getString("Id");if(!deedGroup.isEmpty()&&!group(id).equals(deedGroup))continue;if(!query.isBlank()&&!UiSearch.matches(query,component(n.getString("Title")).getString()+" "+component(n.getString("Description")).getString()))continue;String at=id;var seen=new HashSet<String>();while(nodes.containsKey(at)&&seen.add(at)){var parent=nodes.get(at);if(!deedGroup.isEmpty()&&!group(at).equals(deedGroup))break;visible.put(at,parent.getString("Parent"));at=parent.getString("Parent");}}positionKey=key;positionCache=TaleTreeLayout.create(visible);return positionCache;}
    private void renderDeeds(GuiGraphics g,int x,int width,int mx,int my){
        int y=top+102,bottom=top+h-30,right=x+width;
        g.enableScissor(x,y,right,bottom);
        Map<String,int[]> positions=positions();
        UiLine.Rectangles edges=(x1,y1,x2,y2)->g.fill(x1,y1,x2,y2,FolkUi.GOLD);
        for(var item:positions.entrySet()){
            var n=nodes.get(item.getKey());int[] p=item.getValue(),parent=positions.get(n.getString("Parent"));
            if(parent!=null)UiLine.draw(x+panX+(int)((parent[0]+32)*zoom),y+panY+(int)((parent[1]+16)*zoom),x+panX+(int)(p[0]*zoom),y+panY+(int)((p[1]+16)*zoom),x,y,right,bottom,edges);
        }
        CompoundTag hover=null;
        for(var item:positions.entrySet()){
            var n=nodes.get(item.getKey());int[] p=item.getValue();
            int nx=x+panX+(int)(p[0]*zoom),ny=y+panY+(int)(p[1]*zoom),size=Math.max(32,(int)(32*zoom));
            if(!UiLine.intersects(nx,ny,size,size,x,y,right,bottom))continue;
            int color=n.getBoolean("Done")?0xff385b32:n.getBoolean("Available")?0xffa87c3e:0xff5a5651;
            FolkUi.panel(g,nx,ny,size,size,color);
            var itemIcon=icon(n.getString("Icon"));if(!itemIcon.isEmpty())g.renderItem(itemIcon,nx+8,ny+8);
            if(mx>=x&&mx<right&&my>=y&&my<bottom&&mx>=nx&&mx<nx+size&&my>=ny&&my<ny+size)hover=n;
        }
        g.disableScissor();
        if(hover!=null)UiText.tooltip(g,font,List.of(component(hover.getString("Title")),component(hover.getString("Description")),Component.translatable(hover.getBoolean("Done")?"tales.done":hover.getBoolean("Available")?"tales.available":"tales.unearned")),mx,my);
    }
    private static void line(GuiGraphics g,int x1,int y1,int x2,int y2,int color){
        UiLine.draw(x1,y1,x2,y2,0,0,g.guiWidth(),g.guiHeight(),(ax,ay,bx,by)->g.fill(ax,ay,bx,by,color));
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(search!=null&&search.isMouseOver(x,y))return super.mouseScrolled(x,y,horizontal,vertical);if(x<left+navWidth+30){navOffset=Math.max(0,navOffset+(vertical>0?-1:1));rebuildNavigation();}else if(part.equals("deeds")){if(hasControlDown())zoom=Math.clamp(zoom+(float)vertical*.1F,.4F,1.6F);else{panY+=(int)(vertical*24);panX+=(int)(horizontal*24);}}else textOffset=Math.max(0,textOffset+(vertical>0?-3:3));return true;}
    @Override public boolean mouseClicked(double x,double y,int button){if(button==0&&part.equals("deeds")&&x>left+navWidth+40&&x<left+w-14&&y>top+102&&y<top+h-30){drag=true;dragX=x;dragY=y;return true;}return super.mouseClicked(x,y,button);}
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){if(drag){panX+=(int)(x-dragX);panY+=(int)(y-dragY);dragX=x;dragY=y;return true;}return super.mouseDragged(x,y,button,dx,dy);}
    @Override public boolean mouseReleased(double x,double y,int button){drag=false;return super.mouseReleased(x,y,button);}
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(search!=null&&search.isFocused())return super.keyPressed(key,scan,modifiers);if(key==259&&!history.isEmpty()){restore(history.pop());return true;}if(key==262||key==263){Entry e=current();if(e!=null){page=Math.clamp(page+(key==262?1:-1),1,Math.max(1,stage(e)));textOffset=0;init();return true;}}return super.keyPressed(key,scan,modifiers);}
    @Override public boolean isPauseScreen(){return false;}
}
