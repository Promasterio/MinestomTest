package me.promasterio.com.util.camera;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.PlayerAbilitiesPacket;
import net.minestom.server.network.packet.server.play.PlayerRotationPacket;
import net.minestom.server.tag.Tag;

import java.util.concurrent.locks.LockSupport;

public class PlayerCamera {

    private final Player player;
    private volatile double yawOffset;
    private volatile double pitchOffset;

    public float zoomScale = 1f;

    public PlayerCamera(Player player) {
        this.player = player;
    }

    // Requires -Dminestom.faster-socket-writes=true
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    public void lerpCamera(float yawDelta, float pitchDelta, double timeSeconds) {

        int frames = Math.max(1, (int) (this.getPPS() * timeSeconds));
        long periodNs = (long) ((timeSeconds * 1_000_000_000.0) / frames);

        float yawStep = yawDelta / frames;
        float pitchStep = pitchDelta / frames;

        PlayerRotationPacket packet = new PlayerRotationPacket(yawStep, true, pitchStep, true);

        startThread("Recoil-" + player.getUsername(), () -> {
            for (int i = 0; i < frames; i++) {
                player.sendPacket(packet);
                yawOffset += yawStep;
                pitchOffset += pitchStep;
                LockSupport.parkNanos(periodNs);
            }
        });
    }

    public void setZoom(float scale) {
        PlayerAbilitiesPacket packet = new PlayerAbilitiesPacket((byte) 0x0, player.getFlyingSpeed(), getZoomScope(scale));
        player.sendPacket(packet);
        zoomScale = scale;
    }

    public float getZoom() {
        return zoomScale;
    }

    public float getZoomScope(float scale) {
        return scale / (20f - 10f * scale);
    }

    public double getYawOffset() { return yawOffset; }
    public double getPitchOffset() { return pitchOffset; }

    private int getPPS() {
        return Math.max(1, (int) (20 + 124 * Math.pow(0.993, player.getLatency())));
    }

    private static void startThread(String name, Runnable runnable) { Thread.ofVirtual().name(name).start(runnable); }
}
