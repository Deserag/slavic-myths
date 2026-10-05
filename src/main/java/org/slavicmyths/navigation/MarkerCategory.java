package org.slavicmyths.navigation;

public enum MarkerCategory {
    YAGA(0x8c58b3,"Y"), WAYSTONE(0x55d6dc,"W"), KURGAN(0xb5a174,"K"),
    BANDIT(0xa34343,"B"), BOSS(0xd65050,"!"), QUEST(0xe1bd53,"!"),
    SPECIAL_LOCATION(0x8ea962,"S"), EVENT(0xe39849,"E");
    public final int color; public final String symbol;
    MarkerCategory(int color,String symbol){this.color=color;this.symbol=symbol;}
    public String translation(){return "navigation.slavicmyths.category."+name().toLowerCase(java.util.Locale.ROOT);}
}
