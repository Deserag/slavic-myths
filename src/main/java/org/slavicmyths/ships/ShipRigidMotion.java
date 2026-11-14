package org.slavicmyths.ships;

/**
 * Continuous server-authoritative motion of a rigid ship, independent of any
 * Minecraft world/chunk block placement.
 *
 * Units: position in blocks, linear velocity in blocks/tick, yaw/heading in
 * degrees, angular velocity in degrees/tick. The vehicle is not snapped to
 * integer cells or cardinal bearings. Client renderer can interpolate between
 * the previous and current poses without guessing where the hull blocks are.
 *
 * This is the motion kernel for a future assembled-contraption vessel. It must
 * NEVER be used to move a still-world-block hull independently of its collision
 * or cargo: conversion to a moving palette is a separate atomic transaction.
 */
public final class ShipRigidMotion {
    // Top speed is shared by all player-controlled ships; 0.18 blocks/tick
    // made even fully rigged vessels feel slower than a player sprinting.
    // 0.30 blocks/tick is six blocks/second before water drag.
    public static final double MAX_FORWARD = 0.30D, MAX_REVERSE = 0.10D;
    public static final double MAX_IV_FORWARD = 0.72D;
    public static final double ACCELERATION = 0.018D, COAST_DECELERATION = 0.002D, LINEAR_DRAG = 0.985D;
    public static final double MAX_ANGULAR = 2.2D, ANGULAR_ACCELERATION = 0.20D;
    public static final double ANGULAR_DRAG = 0.80D;
    private static final double EPS = 1E-8D;

    public record Pose(double x, double z, double yaw) {
        public Pose {
            if (!Double.isFinite(x) || !Double.isFinite(z) || !Double.isFinite(yaw))
                throw new IllegalArgumentException("Ship pose must be finite");
        }
    }

    /** World-space pose of the same rigid body. Water travel currently locks
     * height, pitch and roll; an air controller can own all six coordinates. */
    public record SpatialPose(double x, double y, double z,
                              double yaw, double pitch, double roll) {
        public SpatialPose {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                    || !Double.isFinite(yaw) || !Double.isFinite(pitch)
                    || !Double.isFinite(roll))
                throw new IllegalArgumentException("Ship spatial pose must be finite");
        }
    }

    public record Frame(Pose before, Pose after, double speed, double angularVelocity,
                        boolean blocked) {}

    /** Signed throttle and rudder, independently normalized to [-1,+1]. */
    public record Control(double throttle, double rudder) {
        public Control {
            if (!Double.isFinite(throttle) || !Double.isFinite(rudder))
                throw new IllegalArgumentException("Non-finite ship control");
            throttle = clamp(throttle, -1D, 1D);
            rudder = clamp(rudder, -1D, 1D);
        }
        public static final Control IDLE = new Control(0, 0);
    }

    /**
     * Server world collision interface. isClear MUST sweep the full 7x11
     * rotated hull, not just the tiny vanilla Boat entity AABB. Return false
     * if a solid block, unloaded chunk or unvalidated BlockEntity is touched.
     */
    @FunctionalInterface public interface HullClearance {
        boolean isClear(Pose pose);
    }

    private Pose pose;
    private Pose previous;
    private final double hullRadius;
    private final double forwardLimit;
    private double speed;
    private double angularVelocity;

    public ShipRigidMotion(Pose start) { this(start, 6.5D); }

    public ShipRigidMotion(Pose start, double hullRadius) {
        this(start, hullRadius, MAX_FORWARD);
    }

    /** Optional per-vessel cap; the existing ships retain the original 0.30. */
    public ShipRigidMotion(Pose start, double hullRadius, double forwardLimit) {
        if (!Double.isFinite(forwardLimit) || forwardLimit < .05D
                || forwardLimit > MAX_IV_FORWARD)
            throw new IllegalArgumentException("Invalid per-vessel speed cap");
        if (!Double.isFinite(hullRadius) || hullRadius < 1D || hullRadius > 100D)
            throw new IllegalArgumentException("Invalid ship collision radius");
        pose = start;
        previous = start;
        this.hullRadius = hullRadius;
        this.forwardLimit = forwardLimit;
    }

    public Pose pose() { return pose; }
    public Pose previous() { return previous; }
    public double speed() { return speed; }
    public double angularVelocity() { return angularVelocity; }

    public Frame advance(Control control, HullClearance collision) {
        if (collision == null) throw new IllegalArgumentException("Missing full-hull collision");
        Pose from = pose;
        previous = from;
        double desiredSpeed = control.throttle >= 0
                ? control.throttle * forwardLimit : control.throttle * MAX_REVERSE;
        // Unrigged rowing hulls still retain some momentum after helm release.
        // Acceleration is distinct from passive coasting drag, so small low-
        // power ships do not stop dead the moment a captain dismounts.
        speed = approach(speed, desiredSpeed,
                control.throttle() == 0D ? COAST_DECELERATION : ACCELERATION);
        speed *= LINEAR_DRAG;
        if (Math.abs(speed) < 1E-5D) speed = 0D;
        // A ship may rotate nearly on the spot, but does so with rudder inertia.
        double desiredAngular = control.rudder * MAX_ANGULAR;
        angularVelocity = approach(angularVelocity, desiredAngular, ANGULAR_ACCELERATION);
        angularVelocity *= control.rudder == 0D ? ANGULAR_DRAG : 0.96D;
        if (Math.abs(angularVelocity) < 1E-5D) angularVelocity = 0D;

        double deltaYaw = angularVelocity;
        double yawRadians = Math.toRadians(from.yaw() + deltaYaw * .5D);
        double dx = -Math.sin(yawRadians) * speed;
        double dz = Math.cos(yawRadians) * speed;
        // Do not sweep hundreds of hull cells every tick while fully stopped.
        // This only skips a mathematically unchanged pose; every actual
        // translation or rotation still receives the complete collision sweep.
        if (Math.abs(dx) <= EPS && Math.abs(dz) <= EPS
                && Math.abs(deltaYaw) <= EPS)
            return new Frame(from, pose, speed, angularVelocity, false);

        // Substeps protect against tunneling of bow/corners on rotation.
        // Even low speeds can sweep a distant bow through a solid obstacle.
        double bowArc = Math.toRadians(Math.abs(deltaYaw)) * hullRadius;
        int steps = Math.max(1, (int)Math.ceil((Math.hypot(dx, dz) + bowArc) / .20D));
        Pose candidate = from;
        for (int n = 1; n <= steps; n++) {
            double fraction = (double)n / steps;
            candidate = new Pose(from.x() + dx * fraction,
                    from.z() + dz * fraction, wrapDegrees(from.yaw() + deltaYaw * fraction));
            if (!collision.isClear(candidate)) {
                // No partial penetration. Both movement and rotation stop on
                // collision; no world blocks or cargo have been mutated.
                speed = 0D;
                angularVelocity = 0D;
                return new Frame(from, pose, speed, angularVelocity, true);
            }
        }
        pose = candidate;
        return new Frame(from, pose, speed, angularVelocity, false);
    }

    public Pose interpolate(double partialTick) {
        double t = clamp(partialTick, 0D, 1D);
        double turn = wrapDegrees(pose.yaw() - previous.yaw());
        return new Pose(previous.x() + (pose.x() - previous.x()) * t,
                previous.z() + (pose.z() - previous.z()) * t,
                wrapDegrees(previous.yaw() + turn * t));
    }

    public void restore(Pose saved, double savedSpeed, double savedTurn) {
        if (!Double.isFinite(savedSpeed) || !Double.isFinite(savedTurn))
            throw new IllegalArgumentException("Non-finite saved ship velocity");
        pose = saved;
        previous = saved;
        speed = clamp(savedSpeed, -MAX_REVERSE, forwardLimit);
        angularVelocity = clamp(savedTurn, -MAX_ANGULAR, MAX_ANGULAR);
    }

    private static double approach(double current, double target, double change) {
        if (current < target) return Math.min(target, current + change);
        return Math.max(target, current - change);
    }

    public static double wrapDegrees(double angle) {
        double value = angle % 360D;
        if (value >= 180D) value -= 360D;
        if (value < -180D) value += 360D;
        return value == 0D ? 0D : value;
    }

    private static double clamp(double n, double lo, double hi) {
        return Math.max(lo, Math.min(hi, n));
    }

    private ShipRigidMotion() { this(new Pose(0D, 0D, 0D)); }
}
