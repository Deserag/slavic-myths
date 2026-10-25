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
    private static Location lastLocation;
    private static boolean lastSidebar=true,lastFullscreen,lastTree;
    private static String lastQuery="",lastDeed="",lastGroup="";
    private static int lastNavOffset,lastTextOffset;
    private static final Set<String> lastExpanded=new HashSet<>();
    private final Set<String> expanded=new HashSet<>(lastExpanded);
    private boolean sidebar=lastSidebar,fullscreen=lastFullscreen,tree=lastTree,fitTree=true;
    private String selectedDeed="";
    private final TaleViewport viewport=new TaleViewport();
    private double pressX,pressY;
    private boolean moved;
    private Component deedContent;
    private final UiText.Layout deedLayout=new UiText.Layout();
    public static void clear(){snapshot=new CompoundTag();discoveryReady=0;lastLocation=null;lastExpanded.clear();lastSidebar=true;lastFullscreen=lastTree=false;lastQuery=lastDeed=lastGroup="";lastNavOffset=lastTextOffset=0;}
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
    public TaleScreen(){super(Component.translatable("tales.title"));load(true,true);if(lastLocation!=null){part=lastLocation.part;chapter=lastLocation.chapter;section=lastLocation.section;selected=lastLocation.entry;page=lastLocation.page;}query=lastQuery;selectedDeed=lastDeed;deedGroup=lastGroup;navOffset=lastNavOffset;textOffset=lastTextOffset;}
    public static TaleScreen cleansing(){var screen=new TaleScreen();screen.part="crafts";screen.chapter="mor";screen.section="cleansing";screen.selected="slavicmyths:cleansing_knowledge";return screen;}
    public static void receive(TaleNetwork.Snapshot payload){CompoundTag delta=payload.data();snapshot=org.slavicmyths.tales.TaleSnapshotMerge.merge(snapshot,delta,payload.full());
        var mc=Minecraft.getInstance();if(!payload.open()&&!payload.full()&&delta.contains("Entries")&&mc.level!=null&&mc.level.getGameTime()>=discoveryReady){sound("book.discovery");discoveryReady=mc.level.getGameTime()+40;}if(payload.open()){mc.setScreen(new TaleScreen());sound("book.open");}else if(mc.screen instanceof TaleScreen screen){screen.load(payload.full()||delta.contains("Catalog"),payload.full()||delta.contains("Advancements")||delta.getBoolean("AdvancementReset"));screen.init();}}
    private static void sound(String cue){Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(BayunContent.sound(cue),1,.38F));}
    /** Only changed snapshot sections are decoded; progress deltas do not rebuild the catalog/tree. */
    private void load(boolean catalogChanged,boolean advancementsChanged){
        pageContent=null;deedContent=null;
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
    private boolean portrait(){var e=current();return sidebar&&!part.equals("deeds")&&e!=null&&stage(e)>=2&&h>=320&&navWidth>=140;}
    private int rows(){return Math.max(1,(h-87-(portrait()?85:0))/18);}
    private void button(int x,int y,int width,int height,Component text,boolean active,Runnable action){var b=FolkUi.button(x,y,width,height,text,false,v->action.run());b.active=active;addRenderableWidget(b);}
    @Override protected void init(){
        boolean searchFocused=search!=null&&search.isFocused();
        clearWidgets();navigation.clear();w=fullscreen?width:Math.min(1000,width-12);h=fullscreen?height:height-12;left=(width-w)/2;top=(height-h)/2;
        navWidth=sidebar?Math.max(90,Math.min(210,w/3)):0;
        button(left+12,top+4,20,18,Component.literal("←"),!history.isEmpty(),()->{forward.push(location());restore(history.pop());});
        button(left+34,top+4,20,18,Component.literal("→"),!forward.isEmpty(),()->{history.push(location());restore(forward.pop());});
        button(left+w-70,top+4,20,18,Component.literal("☰"),true,()->{sidebar=!sidebar;init();});
        button(left+w-47,top+4,20,18,Component.literal("↔"),true,()->{fullscreen=!fullscreen;fitTree=true;init();});
        button(left+w-24,top+4,20,18,Component.literal("×"),true,this::onClose);
        search=new EditBox(font,left+12,top+29,Math.max(40,w-112),17,Component.translatable("tales.search"));search.setMaxLength(64);search.setHint(Component.translatable("tales.search"));search.setValue(query);search.setResponder(v->{query=v;navOffset=0;positionCache=null;fitTree=true;rebuildNavigation();});addRenderableWidget(search);if(searchFocused)setFocused(search);
        button(left+w-96,top+28,22,19,Component.literal("×"),true,()->{search.setValue("");search.setFocused(true);});
        button(left+w-71,top+28,58,19,Component.translatable("tales.tab.quests"),true,Quest185Screen::request);
        rebuildNavigation();
        int rx=contentX(),rw=contentWidth();
        if(snapshot.getInt("Rewards176")>0)button(left+w-94,top+4,20,18,Component.literal("!"),true,()->PacketDistributor.sendToServer(new TaleNetwork.Read("__rewards176")));
        if(part.equals("deeds")){
            button(rx,top+h-25,Math.min(72,rw/3),19,Component.translatable(tree?"tales.view.list":"tales.view.tree"),true,()->{tree=!tree;fitTree=true;init();});
            if(tree){
                button(rx+76,top+h-25,20,19,Component.literal("−"),true,()->viewport.zoom(viewport.zoom()-.1F,rw,treeHeight()));
                button(rx+99,top+h-25,20,19,Component.literal("+"),true,()->viewport.zoom(viewport.zoom()+.1F,rw,treeHeight()));
                button(rx+122,top+h-25,44,19,Component.translatable("tales.fit"),true,()->fitTree=true);
                if(!selectedDeed.isEmpty())button(rx+170,top+h-25,Math.max(25,Math.min(90,rw-170)),19,Component.translatable("tales.center"),true,()->{var at=positions().get(selectedDeed);if(at!=null){viewport.zoom(1,rw,treeHeight());viewport.center(at,rw,treeHeight());}});
            }
            return;
        }
        Entry e=current();if(e!=null){page=Math.clamp(page,1,Math.max(1,stage(e)));int y=top+h-25;button(rx,y,28,19,Component.literal("‹"),page>1,()->{page--;textOffset=0;sound("book.page");init();});button(left+w-42,y,28,19,Component.literal("›"),page<stage(e),()->{page++;textOffset=0;sound("book.page");init();});
            if(e.id.equals("slavicmyths:yaga_search")&&!snapshot.getCompound("Hints").contains(e.id)&&stage(e)>0)button(rx,top+h-49,Math.min(rw,160),20,Component.translatable("guides.find_yaga"),!snapshot.getCompound("YagaSearch").getString("Status").equals("SEARCHING"),()->PacketDistributor.sendToServer(new TaleNetwork.Read("__find_yaga")));
            if(snapshot.getCompound("Hints").contains(e.id)){
                if(e.id.startsWith("slavicmyths:guide_")){
                    var q=snapshot.getCompound("GuideQuests").getCompound(e.id.substring("slavicmyths:guide_".length()));int cell=Math.max(24,(rw-8)/3);
                    button(rx,top+h-49,cell,20,Component.translatable("tales.map"),true,()->{map=!map;textOffset=0;});
                    if(q.hasUUID("Marker")){var marker=q.getUUID("Marker");button(rx+cell+4,top+h-49,cell,20,Component.translatable("guides.track"),true,()->org.slavicmyths.navigation.NavigationNetwork.request(org.slavicmyths.navigation.NavigationNetwork.Action.TRACK,marker,0,true));button(rx+(cell+4)*2,top+h-49,cell,20,Component.translatable("guides.untrack"),true,()->org.slavicmyths.navigation.NavigationNetwork.request(org.slavicmyths.navigation.NavigationNetwork.Action.STOP,marker,0,false));}
                }else{button(rx,top+h-49,Math.min(rw-35,120),20,Component.translatable("tales.map"),true,()->{map=!map;textOffset=0;});button(left+w-47,top+h-49,33,20,Component.literal("×"+mapZoom),true,()->{mapZoom=mapZoom>=2?1:mapZoom+.5F;init();});}
            }
            else if(stage(e)>=3&&!e.recipes.isEmpty()){button(rx,top+h-49,Math.min(rw,150),20,Component.translatable("tales.recipe.open"),true,()->{recipeIndex=recipeIndex+1>=e.recipes.size()?-1:recipeIndex+1;textOffset=0;});}
            else if(!e.links.isEmpty()){String link=e.links.get(0);Entry target=entriesById.get(link);if(target!=null)button(rx,top+h-49,Math.min(rw,150),20,Component.translatable(target.title),true,()->select(target));}
        }
    }
    private final List<AbstractWidget> navigation=new ArrayList<>();
    private record NavRow(Component label,boolean selected,Runnable action){}
    private void rebuildNavigation(){
        for(var widget:navigation)removeWidget(widget);navigation.clear();
        if(!sidebar)return;
        var rows=new ArrayList<NavRow>();
        for(String category:PARTS){
            if(!query.isBlank()){
                boolean matches=category.equals("deeds")?nodes.values().stream().anyMatch(n->UiSearch.matches(query,component(n.getString("Title")).getString()+" "+component(n.getString("Description")).getString())):entries.stream().anyMatch(e->e.part.equals(category)&&UiSearch.matches(query,Component.translatable(e.title).getString()+" "+heading("chapter",e.chapter).getString()));
                if(!matches)continue;
            }
            boolean open=category.equals(part)&&expanded.contains(category)||!query.isBlank();
            rows.add(new NavRow(Component.literal(open?"▾ ":"▸ ").append(heading("part",category)),part.equals(category),()->{positionCache=null;fitTree=true;if(!part.equals(category)){expanded.add(category);choosePart(category);}else{if(!expanded.add(category))expanded.remove(category);init();}}));
            if(!open)continue;
            if(category.equals("deeds")){
                var categories=new TreeSet<String>();for(String id:nodes.keySet())categories.add(group(id));
                for(String branch:categories){
                    boolean branchOpen=branch.equals(deedGroup)||!query.isBlank();
                    rows.add(new NavRow(Component.literal(branchOpen?"  ▾ ":"  ▸ ").append(heading("group",branch)),branch.equals(deedGroup),()->{deedGroup=branch.equals(deedGroup)?"":branch;navOffset=0;positionCache=null;fitTree=true;init();}));
                    if(!branchOpen)continue;
                    for(var node:nodes.values()){
                        String id=node.getString("Id");if(!group(id).equals(branch))continue;Component label=component(node.getString("Title"));
                        if(!query.isBlank()&&!UiSearch.matches(query,label.getString()+" "+component(node.getString("Description")).getString()))continue;
                        rows.add(new NavRow(Component.literal(node.getBoolean("Done")?"    ● ":"    ○ ").append(label),selectedDeed.equals(id),()->{part="deeds";selectedDeed=id;deedContent=null;textOffset=0;init();}));
                    }
                }
            }else for(Entry entry:entries){
                if(!entry.part.equals(category)||entry.id.equals("slavicmyths:cleansing_knowledge")&&stage(entry)==0)continue;
                Component label=Component.translatable(entry.title);
                if(!query.isBlank()&&!UiSearch.matches(query,label.getString()+" "+heading("chapter",entry.chapter).getString()))continue;
                rows.add(new NavRow(Component.literal(stage(entry)>=2?"  ● ":"  ○ ").append(label),selected.equals(entry.id),()->select(entry)));
            }
        }
        navOffset=Math.clamp(navOffset,0,Math.max(0,rows.size()-rows()));
        for(int i=0;i<rows()&&navOffset+i<rows.size();i++){
            var row=rows.get(navOffset+i);
            var widget=FolkUi.button(left+12,top+53+i*18,navWidth-4,17,row.label,row.selected,v->row.action.run());
            widget.setTooltip(Tooltip.create(row.label));navigation.add(widget);addRenderableWidget(widget);
        }
    }
    private int contentX(){return left+(sidebar?navWidth+20:14);}
    private int contentWidth(){return Math.max(30,left+w-14-contentX());}
    private int treeHeight(){return Math.max(24,h-155);}
    private int textTop(){return top+99;}
    private int textBottom(){return top+h-55;}
    private List<FormattedCharSequence> textLines(){
        var language=net.minecraft.locale.Language.getInstance();
        if(pageContent==null||!contentEntry.equals(selected)||contentPage!=page||contentRecipe!=recipeIndex||contentLanguage!=language){
            pageContent=pageText();contentEntry=selected;contentPage=page;contentRecipe=recipeIndex;contentLanguage=language;
        }
        return pageLayout.split(font,pageContent,contentWidth()-6);
    }
    /** Build recipe/quest text once per page or snapshot, never once per rendered frame. */
    private Component pageText(){
        Entry e=current();
        if(e==null)return Component.translatable(part.equals("quests")?"tales.quest.none":"tales.locked");
        if(stage(e)==0)return Component.translatable("tales.locked");
        if(recipeIndex>=0&&recipeIndex<e.recipes.size())return TaleRecipeText.display(snapshot.getCompound("Recipes").getString(e.recipes.get(recipeIndex)));
        String suffix=switch(page){case 2->"discovered";case 3->"studied";case 4->"verified";default->"rumor";};
        Component content=Component.translatable("tales.entry."+e.id.split(":",2)[1]+"."+suffix);
        if(e.id.equals("slavicmyths:yaga_search")){
            var search=snapshot.getCompound("YagaSearch");String status=search.getString("Status");
            var text=Component.translatable("yaga.search.status."+(status.isEmpty()?"AVAILABLE":status));
            if(status.equals("UNAVAILABLE"))text.append("\n").append(Component.translatable("yaga.search.reason."+search.getString("Reason")));
            return text.append("\n\n").append(content);
        }
        if(e.id.startsWith("slavicmyths:guide_")){
            var q=snapshot.getCompound("GuideQuests").getCompound(e.id.substring("slavicmyths:guide_".length()));
            return Component.translatable("guides.status."+(q.getString("Status").isEmpty()?"NONE":q.getString("Status"))).append("\n\n").append(Component.translatable("guides."+(q.getString("Giver").equals("bayun")?"bayun":"yaga")+".explain")).append("\n\n").append(content);
        }
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
    @Override public void render(GuiGraphics g,int mx,int my,float delta){renderBackground(g,mx,my,delta);FolkUi.frame(g,left,top,w,h);
        if(sidebar)g.fill(left+navWidth+15,top+50,left+navWidth+17,top+h-7,0xff806447);
        g.drawString(font,font.plainSubstrByWidth(title.getString(),Math.max(20,w-140)),left+59,top+9,FolkUi.GOLD,false);
        int rx=contentX(),rw=contentWidth();
        if(part.equals("deeds"))renderDeeds(g,rx,rw,mx,my);else{
            Entry e=current();Component subtitle=e==null?heading("part",part):Component.translatable(e.title);FolkUi.text(g,font,subtitle,rx,top+54,rw,FolkUi.RED,2,subtitleLayout);
            if(e!=null){Component state=Component.translatable("tales.stage."+Math.max(1,page));if(snapshot.getCompound("Entries").getCompound(e.id).getBoolean("Defeated"))state=state.copy().append(" · ").append(Component.translatable("tales.defeated"));FolkUi.text(g,font,state,rx,top+78,rw,FolkUi.GREEN,2,stateLayout);if(map)renderMap(g,e,rx,rw);else{List<FormattedCharSequence> lines=textLines();int count=Math.max(1,(textBottom()-textTop())/12);textOffset=Math.clamp(textOffset,0,Math.max(0,lines.size()-count));g.enableScissor(rx,textTop(),rx+rw,textBottom());for(int i=0;i<count&&textOffset+i<lines.size();i++)g.drawString(font,lines.get(textOffset+i),rx,textTop()+i*12,FolkUi.INK,false);g.disableScissor();if(lines.size()>count){int track=Math.max(1,textBottom()-textTop()),thumb=Math.max(8,track*count/lines.size()),at=(track-thumb)*textOffset/Math.max(1,lines.size()-count);g.fill(rx+rw-2,textTop()+at,rx+rw,textTop()+at+thumb,FolkUi.RED);}}
                g.drawCenteredString(font,Component.translatable("tales.page",page,Math.max(1,stage(e))),rx+rw/2,top+h-18,FolkUi.INK);
            }else{FolkUi.text(g,font,heading("part",part),rx,top+99,rw,FolkUi.INK,4);if(part.equals("quests")){var q=snapshot.getCompound("Quests");FolkUi.text(g,font,Component.translatable(q.getString("YagaActive").isEmpty()?"tales.quest.none":q.getString("YagaActive")),rx,top+130,rw,FolkUi.INK,5);FolkUi.text(g,font,Component.translatable("tales.quest.progress",q.getInt("YagaStage")),rx,top+165,rw,FolkUi.GREEN,3);}}
        }
        if(portrait())illustrate(g,mx,my);if(sidebar)g.drawString(font,countLabel,left+12,top+h-20,FolkUi.INK,false);for(var widget:renderables)widget.render(g,mx,my,delta);if(sidebar&&navigation.isEmpty())g.drawWordWrap(font,Component.translatable("tales.search_empty"),left+34,top+104,navWidth-20,FolkUi.INK);

    }
    private void releaseIllustration(){illustration=null;illustrated="";}
    @Override public void removed(){
        lastLocation=location();lastQuery=query;lastDeed=selectedDeed;lastGroup=deedGroup;lastNavOffset=navOffset;lastTextOffset=textOffset;lastSidebar=sidebar;lastFullscreen=fullscreen;lastTree=tree;lastExpanded.clear();lastExpanded.addAll(expanded);
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
    private Component deedDescription(CompoundTag node){
        if(deedContent!=null)return deedContent;
        var text=component(node.getString("Title")).copy().append("\n\n").append(component(node.getString("Description"))).append("\n\n").append(Component.translatable(node.getBoolean("Done")?"tales.done":node.getBoolean("Available")?"tales.available":"tales.unearned"));
        var parent=nodes.get(node.getString("Parent"));if(parent!=null)text.append("\n").append(Component.translatable("tales.requires")).append(": ").append(component(parent.getString("Title")));
        deedContent=text;return text;
    }
    private void renderDeeds(GuiGraphics g,int x,int width,int mx,int my){
        int y=top+54,right=x+width,bottom=y+treeHeight();
        if(tree){
            var positions=positions();
            if(fitTree){viewport.fit(positions,width,treeHeight());fitTree=false;}
            g.enableScissor(x,y,right,bottom);
            for(var item:positions.entrySet()){
                var node=nodes.get(item.getKey());var at=item.getValue();var parent=positions.get(node.getString("Parent"));
                if(parent!=null)UiLine.draw(viewport.x(x,parent[0]+32),viewport.y(y,parent[1]+16),viewport.x(x,at[0]),viewport.y(y,at[1]+16),x,y,right,bottom,(a,b,c,d)->g.fill(a,b,c,d,FolkUi.GOLD));
            }
            for(var item:positions.entrySet()){
                var node=nodes.get(item.getKey());int nx=viewport.x(x,item.getValue()[0]),ny=viewport.y(y,item.getValue()[1]),size=viewport.size();
                if(!UiLine.intersects(nx,ny,size,size,x,y,right,bottom))continue;
                int color=selectedDeed.equals(item.getKey())?FolkUi.RED:node.getBoolean("Done")?FolkUi.GREEN:node.getBoolean("Available")?0xffa87c3e:0xff5a5651;
                if(size>=5)FolkUi.panel(g,nx,ny,size,size,color);else g.fill(nx,ny,nx+size,ny+size,color);
                // Icons share the node's scale; text is always drawn at native integer coordinates.
                if(size>=12){float scale=Math.min(1,(size-4)/16F);g.pose().pushPose();g.pose().translate(nx+(size-16*scale)/2,ny+(size-16*scale)/2,0);g.pose().scale(scale,scale,1);g.renderItem(icon(node.getString("Icon")),0,0);g.pose().popPose();}
            }
            g.disableScissor();
            String hover=mx>=x&&mx<right&&my>=y&&my<bottom?viewport.hit(positions,x,y,mx,my):"";
            if(!hover.isEmpty())UiText.tooltip(g,font,List.of(component(nodes.get(hover).getString("Title"))),mx,my);
        }
        var selectedNode=nodes.get(selectedDeed);
        if(selectedNode!=null){
            int textY=tree?bottom+4:y;int end=top+h-29;
            var lines=deedLayout.split(font,deedDescription(selectedNode),width-6);int count=Math.max(1,(end-textY)/11);
            textOffset=Math.clamp(textOffset,0,Math.max(0,lines.size()-count));g.enableScissor(x,textY,right,end);
            for(int i=0;i<count&&textOffset+i<lines.size();i++)g.drawString(font,lines.get(textOffset+i),x,textY+i*11,FolkUi.INK,false);
            g.disableScissor();
        }else if(!tree)FolkUi.text(g,font,Component.translatable("tales.deed.choose"),x,y,width,FolkUi.INK,3,subtitleLayout);
    }
    private static void line(GuiGraphics g,int x1,int y1,int x2,int y2,int color){
        UiLine.draw(x1,y1,x2,y2,0,0,g.guiWidth(),g.guiHeight(),(ax,ay,bx,by)->g.fill(ax,ay,bx,by,color));
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(search!=null&&search.isMouseOver(x,y))return super.mouseScrolled(x,y,horizontal,vertical);
        if(sidebar&&x<contentX()){navOffset=Math.max(0,navOffset+(vertical>0?-1:1));rebuildNavigation();}
        else if(part.equals("deeds")&&tree&&y<top+54+treeHeight()){
            if(hasControlDown())viewport.zoom(viewport.zoom()+(float)vertical*.1F,contentWidth(),treeHeight());else viewport.pan(horizontal*24,vertical*24);
        }else textOffset=Math.max(0,textOffset+(vertical>0?-3:3));return true;
    }
    @Override public boolean mouseClicked(double x,double y,int button){
        if(super.mouseClicked(x,y,button))return true;
        if(button==0&&part.equals("deeds")&&tree&&x>=contentX()&&x<left+w-14&&y>=top+54&&y<top+54+treeHeight()){drag=true;moved=false;pressX=dragX=x;pressY=dragY=y;return true;}return false;
    }
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy){
        if(drag){if(!moved&&Math.hypot(x-pressX,y-pressY)>=4)moved=true;if(moved){viewport.pan(x-dragX,y-dragY);dragX=x;dragY=y;}return true;}return super.mouseDragged(x,y,button,dx,dy);
    }
    @Override public boolean mouseReleased(double x,double y,int button){
        if(drag){drag=false;if(!moved){String hit=viewport.hit(positions(),contentX(),top+54,x,y);if(!hit.isEmpty()){selectedDeed=hit;deedContent=null;textOffset=0;init();}}return true;}return super.mouseReleased(x,y,button);
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(search!=null&&search.isFocused())return super.keyPressed(key,scan,modifiers);if(key==259&&!history.isEmpty()){restore(history.pop());return true;}if(key==262||key==263){Entry e=current();if(e!=null){page=Math.clamp(page+(key==262?1:-1),1,Math.max(1,stage(e)));textOffset=0;init();return true;}}return super.keyPressed(key,scan,modifiers);}
    @Override public boolean isPauseScreen(){return false;}
}
