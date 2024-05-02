package com.waffl;

public interface RobotScanListener {
    void ir(double angle, double dist);
    void ping(double angle, double dist);
}
