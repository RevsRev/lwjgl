package github.com.rev;

import java.util.Map;

public final class MandelJulia {

    private final Fractal mandelbrot;
    private final Fractal julia;

    private MandelJulia(final Fractal mandelbrot, final Fractal julia) {
        this.mandelbrot = mandelbrot;
        this.julia = julia;
    }

    public static MandelJulia create() {
        final Fractal mandlebrot = new Fractal("Mandelbrot", "fractal/impl/mandelbrot.frag");
        final Fractal julia = new Fractal(
                "Julia",
                "fractal/impl/julia.frag",
                Map.of(
                        "cR", mandlebrot::getCoordMouseClickX,
                        "cI", mandlebrot::getCoordMouseClickY
                )
        );
        return new MandelJulia(mandlebrot, julia);
    }

    public void run() {
        Thread mandelRunThread = new Thread(mandelbrot::run);

        Thread juliaRunThread = new Thread(() -> {
            while (!mandelbrot.getReady()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            julia.run();
        });

        mandelRunThread.start();
        juliaRunThread.start();

        while (!mandelbrot.getStopped() || !julia.getStopped()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
