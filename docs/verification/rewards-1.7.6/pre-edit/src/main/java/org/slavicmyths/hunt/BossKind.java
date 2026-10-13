package org.slavicmyths.hunt;
public enum BossKind {
 LIKHO("likho_one_eyed",320,.275,10,2,.65,44,140),TUGARIN("tugarin_zmey",350,.285,15,4,.78,48,170);
 public final String id;public final double hp,speed,armor,toughness,resistance,range;public final int xp;
 BossKind(String id,double hp,double speed,double armor,double tough,double resistance,double range,int xp){this.id=id;this.hp=hp;this.speed=speed;this.armor=armor;toughness=tough;this.resistance=resistance;this.range=range;this.xp=xp;}
 public static BossKind read(String id){return TUGARIN.id.equals(id)?TUGARIN:LIKHO;}
}
