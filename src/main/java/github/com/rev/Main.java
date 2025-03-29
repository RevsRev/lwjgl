package github.com.rev;

import org.lwjgl.opengl.GL43;

import java.util.Map;
import java.util.Optional;

public class Main
{

    public static void main(String[] args) {

//        GlfwManager manager = GlfwManager.instance();
//        manager.addWindowedProgram(new Fractal("Mandelbrot", "fractal/impl/mandelbrot.frag"));
//        manager.run();

//        MandelJulia mandelJulia = MandelJulia.create();
//        mandelJulia.run();

//        GlfwManager manager = GlfwManager.instance();
//        Dynamic dynamic = new Dynamic("Right Drift Demo", "dynamic/impl/right_drift.frag", "dynamic/impl/right_drift_bootstrap.frag");
//        manager.addWindowedProgram(dynamic);
//        manager.run();


        GlfwManager manager = GlfwManager.instance();
        // for stability, we require mu = deltaT / deltaX ^ 2 < 1/4
        float deltaX = 0.01f;
        float deltaT = 0.245f * deltaX * deltaX;
        DynamicV2 dynamicV2 = new DynamicV2(
                "Diffusion V2",
                "dynamic/implV2/v2_diffusion_bootstrap.frag",
                "dynamic/implV2/v2_diffusion_dynamic.frag",
                "dynamic/implV2/v2_diffusion_render.frag",
                Map.of("deltaX", id -> GL43.glUniform1f(id, deltaX),
                        "deltaT", id -> GL43.glUniform1f(id, deltaT)),
                new int[]{GL43.GL_COLOR_ATTACHMENT0},
                new String[]{"screenTexture"}
        );
        manager.addWindowedProgram(dynamicV2);
        manager.run();

//        GlfwManager manager = GlfwManager.instance();
//        DynamicV2 dynamicV2 = new DynamicV2(
//                "Dynamic V2 Test",
//                "dynamic/implV2/v2_trivial_bootstrap.frag",
//                "dynamic/implV2/v2_trivial_dynamic.frag",
//                "dynamic/implV2/v2_trivial_render.frag",
//                Map.of(),
//                new int[]{GL43.GL_COLOR_ATTACHMENT0},
//                new String[]{"screenPosition"}
//        );
//        manager.addWindowedProgram(dynamicV2);
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
//                Optional.empty(),
//                "dynamic/impl/diffusion_render.frag"
//        );
//        Dynamic diffusionWithUnobfiscatedConcentration = new Dynamic("Diffusion Equation demo - unobfiscated concentration",
//                "dynamic/impl/diffusion.frag",
//                "dynamic/impl/diffusion_bootstrap.frag",
//                Map.of(
//                        "deltaT", l -> deltaT,
//                        "deltaX", l -> deltaX
//                ),
//                Optional.empty()
//        );
//        Dynamic diffusionWithUnobfiscatedRConcentration = new Dynamic("Diffusion Equation demo - unobfiscated concentration R component",
//                "dynamic/impl/diffusion.frag",
//                "dynamic/impl/diffusion_bootstrap.frag",
//                Map.of(
//                        "deltaT", l -> deltaT,
//                        "deltaX", l -> deltaX
//                ),
//                Optional.empty(),
//                "dynamic/impl/diffusion_render_unobf_r.frag"
//        );
//        Dynamic diffusionWithUnobfiscatedGConcentration = new Dynamic("Diffusion Equation demo - unobfiscated concentration G component",
//                "dynamic/impl/diffusion.frag",
//                "dynamic/impl/diffusion_bootstrap.frag",
//                Map.of(
//                        "deltaT", l -> deltaT,
//                        "deltaX", l -> deltaX
//                ),
//                Optional.empty(),
//                "dynamic/impl/diffusion_render_unobf_g.frag"
//        );
//        Dynamic diffusionWithUnobfiscatedBConcentration = new Dynamic("Diffusion Equation demo - unobfiscated concentration B component",
//                "dynamic/impl/diffusion.frag",
//                "dynamic/impl/diffusion_bootstrap.frag",
//                Map.of(
//                        "deltaT", l -> deltaT,
//                        "deltaX", l -> deltaX
//                ),
//                Optional.empty(),
//                "dynamic/impl/diffusion_render_unobf_b.frag"
//        );
//        manager.addWindowedProgram(diffusion);
//        manager.addWindowedProgram(diffusionWithUnobfiscatedConcentration);
//        manager.addWindowedProgram(diffusionWithUnobfiscatedRConcentration);
//        manager.addWindowedProgram(diffusionWithUnobfiscatedGConcentration);
//        manager.addWindowedProgram(diffusionWithUnobfiscatedBConcentration);
//        manager.run();

    }

}