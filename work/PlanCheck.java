import org.slavicmyths.kurgan.KurganPlan;
public class PlanCheck {public static void main(String[] args){for(int k=0;k<3;k++)for(int s=0;s<200;s++){try{KurganPlan p=KurganPlan.create(k,s);if(!p.validate().isEmpty())throw new AssertionError(p.validate());}catch(Exception e){throw new RuntimeException("tier="+k+" seed="+s,e);}}System.out.println("600 plans validated");}}
