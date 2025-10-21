package br.puc.robobattle.ui;

public class SpriteAnimator {
    private final int frames;
    private final double fps;
    private double t = 0.0;

    public SpriteAnimator(int frames, double fps) {
        this.frames = frames;
        this.fps = fps;
    }

    public void update(double deltaSeconds) { t += deltaSeconds; }

    public int currentIndex() {
        int idx = (int) Math.floor(t * fps) % frames;
        return Math.max(0, idx);
    }

    public void reset() { t = 0.0; }
}
