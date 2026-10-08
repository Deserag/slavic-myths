package org.slavicmyths.rpg.classes;

/** Shared server/client eligibility; Creative skips costs, never class identity or rank caps. */
public final class ClassLearningRules {
    public static String blocker(String base,ClassDefinitions.Skill skill,int rank,int level,int prerequisiteRank,boolean branch,int points,boolean creative) {
        if(skill==null||!skill.base().equals(base))return "wrong_class";
        if(rank>=skill.maxRank())return "max_rank";
        if(creative)return "";
        if(level<skill.level()+rank*2)return "level_required";
        if(!skill.prerequisite().isEmpty()&&prerequisiteRank<skill.prerequisiteRank())return "prerequisite";
        if(!skill.branch().isEmpty()&&!branch)return "branch_required";
        return points<1?"no_points":"";
    }
    public static int pointsAfterLearning(int points,boolean creative){return creative?points:points-1;}
    private ClassLearningRules() { }
}
