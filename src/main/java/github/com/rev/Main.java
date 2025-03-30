package github.com.rev;

import github.com.rev.gl.uniform.UniformArray;
import github.com.rev.gl.uniform.UniformPrimative;
import github.com.rev.terminal.Terminal;
import org.lwjgl.opengl.GL43;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Main
{

    public static void main(String[] args) {

        Terminal t = new Terminal();

        addMandelbrotJulia(t);
        addSierpinski(t);
        addSierpinskiTriangle(t);
        addBurningShip(t);
        addDiffusionV1Options(t);
        addDiffusionV2(t);
        addWave(t);

        t.start();

    }

    private static void addMandelbrotJulia(final Terminal t) {
        final Fractal mandlebrot = new Fractal("Mandelbrot", "fractal/impl/mandelbrot.frag");
        final Fractal julia = new Fractal(
                "Julia",
                "fractal/impl/julia.frag",
                Map.of(
                        "cR", mandlebrot::getCoordMouseClickX,
                        "cI", mandlebrot::getCoordMouseClickY
                )
        );
        t.addOption("mandelbrot", Set.of(mandlebrot, julia));
    }

    private static void addSierpinski(final Terminal t) {
        final Fractal sierpinskiCarpet = new Fractal(
                "Sierpinski Carpet",
                "fractal/impl/sierpinski_carpet.frag",
                Collections.emptyMap(),
                zoom -> Math.max(6, (int)(6 + Math.log(zoom) / Math.log(3))));
        sierpinskiCarpet.setCoordOriginX(0.5);
        sierpinskiCarpet.setCoordOriginY(0.5);
        sierpinskiCarpet.setCoordXWidth(0.5);
        sierpinskiCarpet.setCoordYWidth(0.5);
        sierpinskiCarpet.setBackgroundColor(Color.WHITE);
        sierpinskiCarpet.setSetColor(Color.BLACK);
        t.addOption("sierpinski", Set.of(sierpinskiCarpet));
    }

    private static void addSierpinskiTriangle(final Terminal t) {
        final Fractal sierpinskiCarpet = new Fractal(
                "Sierpinski Triangle",
                "fractal/impl/sierpinski_triangle.frag",
                Collections.emptyMap(),
                zoom -> Math.max(9, (int)(9 + Math.log(zoom) / Math.log(2))));
        sierpinskiCarpet.setCoordOriginX(0.5);
        sierpinskiCarpet.setCoordOriginY(0.5);
        sierpinskiCarpet.setCoordXWidth(0.5);
        sierpinskiCarpet.setCoordYWidth(0.5);
        sierpinskiCarpet.setBackgroundColor(Color.WHITE);
        sierpinskiCarpet.setSetColor(Color.BLACK);
        t.addOption("sierpinskiT", Set.of(sierpinskiCarpet));
    }

    private static void addBurningShip(final Terminal t) {
        final Fractal burningShip = new Fractal(
                "Burning Ship",
                "fractal/impl/burning_ship.frag");
        t.addOption("burning_ship", Set.of(burningShip));
    }

    private static void addDiffusionV1Options(final Terminal t) {
        // for stability, we require mu = deltaT / deltaX ^ 2 < 1/4
        float deltaX = 0.01f;
        float deltaT = 0.245f * deltaX * deltaX;

        final Dynamic diffusion = new Dynamic("Diffusion Equation demo",
                "dynamic/impl/diffusion.frag",
                "dynamic/impl/diffusion_bootstrap.frag",
                Map.of(
                        "deltaT", l -> deltaT,
                        "deltaX", l -> deltaX
                ),
                Optional.empty(),
                "dynamic/impl/diffusion_render.frag"
        );
        final Dynamic diffusionWithUnobfiscatedConcentration = new Dynamic("Diffusion Equation demo - unobfiscated concentration",
                "dynamic/impl/diffusion.frag",
                "dynamic/impl/diffusion_bootstrap.frag",
                Map.of(
                        "deltaT", l -> deltaT,
                        "deltaX", l -> deltaX
                ),
                Optional.empty()
        );
        final Dynamic diffusionWithUnobfiscatedRConcentration = new Dynamic("Diffusion Equation demo - unobfiscated concentration R component",
                "dynamic/impl/diffusion.frag",
                "dynamic/impl/diffusion_bootstrap.frag",
                Map.of(
                        "deltaT", l -> deltaT,
                        "deltaX", l -> deltaX
                ),
                Optional.empty(),
                "dynamic/impl/diffusion_render_unobf_r.frag"
        );
        final Dynamic diffusionWithUnobfiscatedGConcentration = new Dynamic("Diffusion Equation demo - unobfiscated concentration G component",
                "dynamic/impl/diffusion.frag",
                "dynamic/impl/diffusion_bootstrap.frag",
                Map.of(
                        "deltaT", l -> deltaT,
                        "deltaX", l -> deltaX
                ),
                Optional.empty(),
                "dynamic/impl/diffusion_render_unobf_g.frag"
        );
        final Dynamic diffusionWithUnobfiscatedBConcentration = new Dynamic("Diffusion Equation demo - unobfiscated concentration B component",
                "dynamic/impl/diffusion.frag",
                "dynamic/impl/diffusion_bootstrap.frag",
                Map.of(
                        "deltaT", l -> deltaT,
                        "deltaX", l -> deltaX
                ),
                Optional.empty(),
                "dynamic/impl/diffusion_render_unobf_b.frag"
        );
        t.addOption("diffusionV1", Set.of(diffusion));
        t.addOption("diffusionV1extended", Set.of(
                diffusion,
                diffusionWithUnobfiscatedConcentration,
                diffusionWithUnobfiscatedRConcentration,
                diffusionWithUnobfiscatedGConcentration,
                diffusionWithUnobfiscatedBConcentration
                ));
    }

    private static void addDiffusionV2(final Terminal t) {
        // for stability, we require mu = deltaT / deltaX ^ 2 < 1/4
        float deltaX = 0.01f;
        float deltaT = 0.245f * deltaX * deltaX;
        final DynamicV2 diffusionV2 = new DynamicV2(
                "Diffusion V2",
                "dynamic/implV2/v2_diffusion_bootstrap.frag",
                "dynamic/implV2/v2_diffusion_dynamic.frag",
                "dynamic/implV2/v2_diffusion_render.frag",
                Set.of(
                        new UniformPrimative("deltaX", true, id -> GL43.glUniform1f(id, deltaX)),
                        new UniformPrimative("deltaT", true, id -> GL43.glUniform1f(id, deltaT))
                ),
                new String[]{"screenTexture"}
        );
        t.addOption("diffusionV2", Set.of(diffusionV2));
    }

    private static void addWave(final Terminal t) {
        // for stability, we require mu = deltaT / deltaX ^ 2 < 1/4
        float deltaX = 0.01f;
        float deltaT = 0.245f * deltaX * deltaX;
        float mu = deltaT / (deltaX * deltaX);

        Float[][] offsets = new Float[][] {
                new Float[] {-deltaX, deltaX},
                new Float[] {0.0f, deltaX},
                new Float[] {deltaX, deltaX},
                new Float[] {-deltaX, 0.0f},
                new Float[] {0.0f, 0.0f},
                new Float[] {deltaX, 0.0f},
                new Float[] {-deltaX, -deltaX},
                new Float[] {0.0f, -deltaX},
                new Float[] {deltaX, -deltaX}
        };

        Float[] kernel = new Float[] {
                mu / 6, 2 * mu / 3, mu / 6,
                2 * mu / 3, -10 / 3f * mu, 2 * mu / 3,
                mu / 6, 2 * mu / 3, mu / 6
        };

        final DynamicV2 wave = new DynamicV2(
                "Wave Equation",
                "dynamic/implV2/wave_bootstrap.frag",
                "dynamic/implV2/wave_dynamic.frag",
                "dynamic/implV2/wave_render.frag",
                Set.of(
                        new UniformPrimative("deltaX", true, id -> GL43.glUniform1f(id, deltaX)),
                        new UniformPrimative("deltaT", true, id -> GL43.glUniform1f(id, deltaT)),
                        new UniformArray<>("offsets", true, (f, id) -> GL43.glUniform2f(id, f[0], f[1]), offsets),
                        new UniformArray<>("kernel", true, (f, id) -> GL43.glUniform1f(id, f), kernel)
                ),
                new String[]{"inputPosition", "inputVelocity"}
        );
        t.addOption("wave", Set.of(wave));
    }

}