package me.promasterio.com;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;

import java.util.concurrent.atomic.AtomicReference;

public class ServerInfoBossBar {

    private final EventNode node = EventNode.all("serverInfo");
    private final AtomicReference<TickMonitor> LAST_TICK = new AtomicReference<>();
    private final BossBar[] bossBar = new BossBar[1];

    public ServerInfoBossBar() {
        MinecraftServer.getGlobalEventHandler().addChild(node);
        MinecraftServer.getGlobalEventHandler().addListener(ServerTickMonitorEvent.class, event -> LAST_TICK.set(event.getTickMonitor()));
        var benchmarkManager = MinecraftServer.getBenchmarkManager();

        bossBar[0] = BossBar.bossBar(Component.text("Initializing..."), 0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);

        long[] lastUsedRam = {-1L};
        double[] lastTickTime = {-1.0};
        long maxRam = Runtime.getRuntime().maxMemory() / (1024 * 1024);

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (MinecraftServer.getConnectionManager().getOnlinePlayerCount() == 0) return;

            long usedRam = benchmarkManager.getUsedMemory() / (1024 * 1024);
            float ramProgress = (float) Math.min(Math.max((double) usedRam / maxRam, 0.0), 1.0);

            TickMonitor tickMonitor = LAST_TICK.get();
            if (tickMonitor == null) return;
            double tickTime = tickMonitor.getTickTime();

            bossBar[0].progress(ramProgress);

            if (usedRam != lastUsedRam[0] || tickTime != lastTickTime[0]) {
                lastUsedRam[0] = usedRam;
                lastTickTime[0] = tickTime;

                Component text = Component.text()
                        .append(Component.text("RAM: ", NamedTextColor.GRAY))
                        .append(Component.text(usedRam + "/" + maxRam + " MB", NamedTextColor.WHITE))
                        .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("TICK: ", NamedTextColor.GRAY))
                        .append(Component.text(MathUtils.round(tickTime, 2) + " ms", NamedTextColor.WHITE))
                        .build();

                bossBar[0] = bossBar[0].name(text);
            }

            Audiences.players().showBossBar(bossBar[0]);

        }).repeat(1, TimeUnit.SERVER_TICK).schedule();
    }
}
