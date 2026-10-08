package org.slavicmyths.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slavicmyths.rpg.classes.ClassHudConfig;

/** Mods > Slavic Myths settings; a single local option, shared with the HUD. */
public final class ClassSettingsScreen extends Screen {
 private final Screen parent;
 public ClassSettingsScreen(Screen parent){super(Component.translatable("classes.slavicmyths.settings"));this.parent=parent;}
 @Override protected void init(){int x=(width-260)/2,y=height/2;addRenderableWidget(new ClassTheme.WoodButton(x,y-8,260,24,Component.translatable("classes.slavicmyths.show_hud").append(": ").append(Component.translatable(ClassHudConfig.SHOW.get()?"options.on":"options.off")),b->{ClassHudConfig.SHOW.set(!ClassHudConfig.SHOW.get());ClassHudConfig.SPEC.save();rebuildWidgets();}));addRenderableWidget(new ClassTheme.WoodButton(x+70,y+50,120,24,Component.translatable("gui.done"),b->onClose()));}
 @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){g.fill(0,0,width,height,0xCC100D0B);}
 @Override protected void renderBlurredBackground(float delta){}
 @Override public void render(GuiGraphics g,int mx,int my,float delta){ClassTheme.panel(g,(width-284)/2,height/2-56,284,144);g.drawCenteredString(font,title,width/2,height/2-40,ClassTheme.TEXT);g.drawWordWrap(font,Component.translatable("classes.slavicmyths.hud_settings_hint"),(width-250)/2,height/2+22,250,ClassTheme.MUTED);super.render(g,mx,my,delta);}
 @Override public void onClose(){minecraft.setScreen(parent);}
}
