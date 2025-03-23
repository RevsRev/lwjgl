package github.com.rev;

import java.util.Map;
import java.util.Optional;

public class Main
{

    public static void main(String[] args) {

//        Mandelbrot mandelbrot = new Mandelbrot();
//        mandelbrot.run();

//        Dynamic dynamic = new Dynamic("Right Drift Demo", "dynamic/impl/right_drift.frag", "dynamic/impl/right_drift_bootstrap.frag");
//        dynamic.run();

        float diffusionRate = 0.1f;
        long sleepMillis = 100;

        // for stability, we require mu = deltaT / deltaX ^ 2 < 1/4
        float deltaX = 0.01f;
        float deltaT = 0.245f * deltaX * deltaX;

        Dynamic diffusion = new Dynamic("Diffusion Equation demo",
                "dynamic/impl/diffusion.frag",
                "dynamic/impl/diffusion_bootstrap.frag",
                Map.of(
                        "deltaT", l -> deltaT,
                        "deltaX", l -> deltaX
                ),
                Optional.empty()
//                Optional.of(
//                        () -> {
//                            try {
//                                Thread.sleep(sleepMillis);
//                            } catch (InterruptedException e) {
//                                System.out.println("Thread interrupted while sleeping");
//                            }
//                        }
//                )
        );
        diffusion.run();

    }

}