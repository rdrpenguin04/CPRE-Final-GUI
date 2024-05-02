package com.waffl;

public interface RobotMovementListener {
    void robotStartedMovement();
    void robotMoved(double dist);
    void robotTurned(double angle);
}
