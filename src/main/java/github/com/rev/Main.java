package github.com.rev;

public class Main
{

    public static void main(String[] args) {

//        Fractal mandelbrot = new Fractal("Mandelbrot", "fractal/impl/mandelbrot/mandelbrot.frag");
//        mandelbrot.run();

        Fractal julia = new Fractal("Julia", "fractal/impl/julia.frag");
        julia.run();

//        Dynamic dynamic = new Dynamic("Right Drift Demo", "dynamic/impl/right_drift.frag", "dynamic/impl/right_drift_bootstrap.frag");
//        dynamic.run();

//        // for stability, we require mu = deltaT / deltaX ^ 2 < 1/4
//        float deltaX = 0.01f;
//        float deltaT = 0.245f * deltaX * deltaX;
//
//        Dynamic diffusion = new Dynamic("Diffusion Equation demo",
//                "dynamic/impl/diffusion.frag",
//                "dynamic/impl/diffusion_bootstrap.frag",
//                Map.of(
//                        "deltaT", l -> deltaT,
//                        "deltaX", l -> deltaX
//                ),
//                Optional.empty()
//        );
//        diffusion.run();

    }

}