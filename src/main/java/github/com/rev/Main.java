package github.com.rev;

import java.util.Map;
import java.util.Optional;

public class Main
{

    public static void main(String[] args) {

//        GlfwManager manager = GlfwManager.instance();
//        manager.addWindowedProgram(new Fractal("Mandelbrot", "fractal/impl/mandelbrot.frag"));
//        manager.run();

        MandelJulia mandelJulia = MandelJulia.create();
        mandelJulia.run();

//        GlfwManager manager = GlfwManager.instance();
//        Dynamic dynamic = new Dynamic("Right Drift Demo", "dynamic/impl/right_drift.frag", "dynamic/impl/right_drift_bootstrap.frag");
//        manager.addWindowedProgram(dynamic);
//        manager.run();


//        GlfwManager manager = GlfwManager.instance();
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
//        manager.addWindowedProgram(diffusion);
//        manager.run();

    }

}