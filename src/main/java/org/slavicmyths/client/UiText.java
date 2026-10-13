package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Native-size text with bounded wrapping and explicit overflow, never silent clipping. */
public final class UiText {
    public static String fit(Font font,String text,int width){if(width<font.width("…"))return "";return font.width(text)<=width?text:font.plainSubstrByWidth(text,width-font.width("…"))+"…";}
    public static List<FormattedCharSequence> lines(Font font,Component text,int width,int maxRows){
        var all=font.split(text,Math.max(8,width));int count=Math.min(all.size(),Math.max(1,maxRows));
        var result=new ArrayList<FormattedCharSequence>(all.subList(0,count));
        if(all.size()>count){var value=new StringBuilder();result.get(count-1).accept((index,style,codepoint)->{value.appendCodePoint(codepoint);return true;});result.set(count-1,Component.literal(font.plainSubstrByWidth(value.toString(),Math.max(0,width-font.width("…")))+"…").getVisualOrderText());}
        return result;
    }
    public static void tooltip(GuiGraphics g,Font font,List<Component> text,int x,int y){var lines=new ArrayList<FormattedCharSequence>();for(var value:text)lines.addAll(font.split(value,Math.min(300,Math.max(80,g.guiWidth()-24))));g.renderTooltip(font,lines,x,y);}
    private UiText(){}
}
