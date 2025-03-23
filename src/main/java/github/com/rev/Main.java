package github.com.rev;

import java.util.Map;

public class Main
{

    public static void main(String[] args) {

//        GlfwManager manager = GlfwManager.instance();
//        manager.addWindowedProgram(new Fractal("Mandelbrot", "fractal/impl/mandelbrot.frag"));
//        manager.run();

        MandelJulia mandelJulia = MandelJulia.create();
        mandelJulia.run();

//        Fractal mandelbrot = new Fractal("Mandelbrot", "fractal/impl/mandelbrot/mandelbrot.frag");
//        mandelbrot.run();

//        final Fractal mandelbrot = new Fractal("Mandelbrot", "fractal/impl/mandelbrot.frag");
//        mandelbrot.run();

//        final Fractal julia = new Fractal(
//                "Julia",
//                "fractal/impl/julia.frag",
//                Map.of(
//                        "cR", () -> -0.79,
//                        "cI", () -> 0.15
//                )
//        );
//        julia.run();

//        MandelJulia mandelJulia = MandelJulia.create();
//        mandelJulia.run();

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