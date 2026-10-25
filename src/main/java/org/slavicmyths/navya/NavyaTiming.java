package org.slavicmyths.navya;
import org.slavicmyths.navya.NavyaContent.Kind;
import org.slavicmyths.navya.NavyaMob.Action;
/** One timing contract for server damage and client poses. */
public final class NavyaTiming {
    public static int windup(Kind kind,Action action) { return switch(action){case MELEE->kind==Kind.GUARDIAN?26:12;case SHOT->20;case ROOTS->28;case DASH->16;case THROW->48;case SUMMON->40;case PHASE->60;default->0;}; }
    public static int duration(Kind kind,Action action) { return windup(kind,action)+(action==Action.PHASE?20:action==Action.THROW?28:18); }
    private NavyaTiming() { }
}
