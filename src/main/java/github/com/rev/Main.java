package github.com.rev;

public class Main
{

    public static void main(String[] args) {

//        Mandelbrot mandelbrot = new Mandelbrot();
//        mandelbrot.run();

        Dynamic dynamic = new Dynamic("dynamic/impl/right_drift.frag");
        dynamic.run();

    }

}