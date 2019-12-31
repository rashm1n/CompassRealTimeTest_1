package com.fyp.ble.compassrealtimetest_1;

public class KalmanFilter {

    private double R,Q,A,B,C,cov,x;

    public KalmanFilter(double R, double Q) {
        this.A = 1.0;
        this.B = 0.0;
        this.C = 1.0;
        this.R = R;
        this.Q = Q;
        this.cov = Double.NaN;
        this.x = Double.NaN;
    }

    public KalmanFilter(double r, double q, double a, double b, double c, double cov, double x) {
        R = r;
        Q = q;
        A = a;
        B = b;
        C = c;
        this.cov = cov;
        this.x = x;
    }

    public double filter(double measurement){
        int u =0;
        if (Double.isNaN(this.x)){

            this.x = (1/this.C)*measurement;
            this.cov = (1/this.C)*this.Q*(1/this.C);
        }else {
            double predX = (this.A * this.x) + (this.B*u);
            double predCov = ((this.A*this.cov) * this.A) + this.R;

            //Kalman Gain

            double K = predCov * this.C * (1/((this.C * predCov * this.C) + this.Q ));

            //correction

            this.x = predX + K * (measurement-(this.C*predX));
            this.cov = predCov - (K * this.C*predCov);
        }
        return this.x;
    }

    public double last_measurement(){
        return this.x;
    }

    public void set_measurementNoice(double n){
        this.Q = n;
    }

    public void set_processNoice(double n){
        this.R = n;
    }


}
