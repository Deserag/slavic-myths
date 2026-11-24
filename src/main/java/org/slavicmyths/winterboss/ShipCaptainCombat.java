package org.slavicmyths.winterboss;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.random.RandomGenerator;

/** Server-side, world-independent attack timeline for the one-phase ship captain.
 * Entity integration consumes the events; damage and projectile creation are not performed here.
 */
public final class ShipCaptainCombat {
    public enum Attack {
        AXE_STRIKE(0, 3.5, 1, 26, 50, "axe_windup"),
        DOUBLE_STRIKE(0, 3.5, 2, 38, 120, "double_windup"),
        AXE_THROW(5, 25, 1, 32, 100, "axe_windup"),
        AXE_SPIN(0, 5, 2, 60, 300, "spin_start"),
        ICE_SLAM(0, 7, 0, 48, 360, "slam_charge"),
        AXE_VOLLEY(8, 20, 1, 48, 400, "volley_charge"),
        GHOST_GRASP(5, 12, 0, 36, 240, "grasp_charge"),
        FROST_DASH(6, 14, 0, 38, 200, "dash_charge");

        public final double minRange, maxRange;
        public final int requiredAxes, duration, cooldown;
        public final String windupSound;
        Attack(double minRange, double maxRange, int requiredAxes, int duration, int cooldown, String windupSound) {
            this.minRange = minRange;
            this.maxRange = maxRange;
            this.requiredAxes = requiredAxes;
            this.duration = duration;
            this.cooldown = cooldown;
            this.windupSound = windupSound;
        }
    }

    public enum Kind { SOUND, MELEE, DOUBLE_MELEE, THROW, SPIN_HIT, SLAM, VOLLEY, GRASP, DASH_START, DASH_END, COMPLETE }
    public record Event(Kind kind, String soundKey) {}

    private final EnumMap<Attack, Integer> cooldowns = new EnumMap<>(Attack.class);
    private Attack active;
    private int frame;
    private int recovery;
    private Attack last;

    public Attack active() { return active; }
    public int frame() { return frame; }
    public int cooldown(Attack attack) { return cooldowns.getOrDefault(attack, 0); }
    public boolean ready() { return active == null && recovery == 0; }

    public boolean canStart(Attack attack, double distance, int axesInHand, boolean lineOfSight) {
        return ready() && lineOfSight && cooldown(attack) == 0
            && axesInHand >= attack.requiredAxes && Double.isFinite(distance)
            && distance >= attack.minRange && distance <= attack.maxRange;
    }

    public Attack choose(double distance, int axesInHand, boolean lineOfSight, RandomGenerator random) {
        List<Attack> eligible = new ArrayList<>();
        for (Attack attack : Attack.values()) {
            if (canStart(attack, distance, axesInHand, lineOfSight)) eligible.add(attack);
        }
        if (eligible.size() > 1) eligible.remove(last);
        return eligible.isEmpty() ? null : eligible.get(random.nextInt(eligible.size()));
    }

    public List<Event> start(Attack attack, double distance, int axesInHand, boolean lineOfSight) {
        if (attack == null || !canStart(attack, distance, axesInHand, lineOfSight)) return List.of();
        active = attack;
        frame = 0;
        cooldowns.put(attack, attack.cooldown);
        return List.of(event(Kind.SOUND, attack.windupSound));
    }

    /** Advance once per server tick, including ticks without a target. */
    public List<Event> tick() {
        cooldowns.replaceAll((attack, remaining) -> Math.max(0, remaining - 1));
        if (recovery > 0) recovery--;
        if (active == null) return List.of();
        frame++;
        List<Event> events = new ArrayList<>(2);
        switch (active) {
            case AXE_STRIKE -> {
                if (frame == 10) events.add(event(Kind.SOUND, "axe_swing"));
                if (frame == 13) events.add(event(Kind.MELEE, "axe_hit"));
            }
            case DOUBLE_STRIKE -> {
                if (frame == 18) events.add(event(Kind.DOUBLE_MELEE, "double_hit"));
            }
            case AXE_THROW -> {
                if (frame == 14) events.add(event(Kind.THROW, "throw_release"));
            }
            case AXE_SPIN -> {
                // Two full rotations over 40 ticks; hit opportunities spaced by 10 ticks.
                if (frame == 12 || frame == 22 || frame == 32 || frame == 42)
                    events.add(event(Kind.SPIN_HIT, "spin_sweep"));
            }
            case ICE_SLAM -> {
                if (frame == 25) events.add(event(Kind.SLAM, "slam_impact"));
            }
            case AXE_VOLLEY -> {
                if (frame == 20) events.add(event(Kind.VOLLEY, "volley_release"));
            }
            case GHOST_GRASP -> {
                if (frame == 18) events.add(event(Kind.GRASP, "grasp_release"));
            }
            case FROST_DASH -> {
                if (frame == 14) events.add(event(Kind.DASH_START, "dash_release"));
                if (frame == 24) events.add(new Event(Kind.DASH_END, null));
            }
        }
        if (frame >= active.duration) {
            last = active;
            active = null;
            frame = 0;
            recovery = 10;
            events.add(new Event(Kind.COMPLETE, null));
        }
        return List.copyOf(events);
    }

    public void cancel() {
        if (active != null) last = active;
        active = null;
        frame = 0;
        recovery = 10;
    }

    private static Event event(Kind kind, String cue) {
        return new Event(kind, "ship_captain." + cue);
    }
}
