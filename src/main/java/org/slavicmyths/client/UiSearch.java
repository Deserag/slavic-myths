package org.slavicmyths.client;

import java.util.Locale;

/** Shared literal word search: case, whitespace and Russian ё do not affect matches. */
public final class UiSearch {
    public static String normalize(String text){return text.toLowerCase(Locale.ROOT).replace('ё','е').strip();}
    public static boolean matches(String query,String text){
        String value=normalize(text);
        for(String word:normalize(query).split("\\s+"))if(!value.contains(word))return false;
        return true;
    }
    private UiSearch(){}
}
