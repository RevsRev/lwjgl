package github.com.rev;

public class Main
{

    public static void main(String[] args) {

//        Mandelbrot mandelbrot = new Mandelbrot();
//        mandelbrot.run();

        Dynamic dynamic = new Dynamic("Right Drift Demo", "dynamic/impl/right_drift.frag", "dynamic/impl/right_drift_bootstrap.frag");
        dynamic.run();

    }

}