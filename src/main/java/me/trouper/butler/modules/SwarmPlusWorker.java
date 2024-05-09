package me.trouper.butler.modules;

import me.trouper.butler.Addon;
import me.trouper.butler.server.Address;
import me.trouper.butler.server.Client;
import me.trouper.butler.server.Response;
import me.trouper.butler.utils.MathUtils;
import me.trouper.butler.utils.Text;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Objects;

public class SwarmPlusWorker extends Module {

    private Client client = null;

    private final SettingGroup general = settings.getDefaultGroup();

    public SwarmPlusWorker() {
        super(Addon.CATEGORY, "swarm-plus-worker", "(Worker/Client) Control multiple instances of meteor through one account, but better.");
    }

    private final Setting<String> ip = general.add(new StringSetting.Builder()
        .name("address")
        .defaultValue("localhost")
        .build()
    );

    private final Setting<Integer> port = general.add(new IntSetting.Builder()
        .name("port")
        .defaultValue(25561)
        .max(65535)
        .min(0)
        .build()
    );

    private final Setting<Boolean> verbose = general.add(new BoolSetting.Builder()
        .name("verbose")
        .defaultValue(false)
        .build()
    );

    @Override
    public void onActivate() {
        client = new Client(new Address(ip.get(),port.get())) {
            @Override
            protected void info(String str, Object... args) {
                super.info(str, args);
                String full = str.formatted(args);
                if (verbose.get()) SwarmPlusWorker.this.info(full);
                String packetType = Text.getPacketType(full);
                String packetArgs = Text.getPacketArgs(full);
                if (packetType == null) packetType = "ERR";
                switch (packetType) {
                    case "CHAT" -> {
                        if (packetArgs == null) {
                            SwarmPlusWorker.this.error("Chat message returned null. (highlight)%s",full);
                            return;
                        }
                        SwarmPlusWorker.this.info("Received chat command. Sending (highlight)%s".formatted(packetArgs));
                        if (MinecraftClient.getInstance().player == null) return;
                        ChatUtils.sendPlayerMsg(packetArgs);
                    }
                    case "METEOR" -> {
                        SwarmPlusWorker.this.info("Received meteor command (highlight)%s(default).".formatted(packetArgs));
                        if (MinecraftClient.getInstance().player == null) return;
                        ChatUtils.sendPlayerMsg(Config.get().prefix.get() + packetArgs);
                    }
                    case "LOOK" -> {
                        String[] largs = packetArgs.split(" ");
                        switch (largs[0]) {
                            case "player" -> {
                                String target = largs[1];
                                try {
                                    for (Entity entity : mc.player.clientWorld.getEntities()) {
                                        if (!(entity instanceof PlayerEntity)) continue;
                                        if (!entity.getName().getString().equalsIgnoreCase(target)) continue;
                                        Vec3d vec = entity.getEyePos().subtract(mc.player.getEyePos()).normalize();
                                        float[] rot = MathUtils.toPolar(vec.x,vec.y,vec.z);
                                        mc.player.setPitch(rot[0]);
                                        mc.player.setYaw(rot[1]);
                                        return;
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    this.sendToServer("An error occurred whilst trying to rotate to %s!".formatted(target));
                                }
                            }
                            case "absolute" -> {
                                float pitch = Float.parseFloat(largs[1]);
                                float yaw = Float.parseFloat(largs[2]);
                                mc.player.setPitch(pitch);
                                mc.player.setYaw(yaw);
                            }
                        }
                    }
                    case "STOP" -> {
                        SwarmPlusWorker.this.info("Received Stop Command");
                        ChatUtils.sendPlayerMsg("#stop");
                    }
                    case "BARITONE" -> {
                        String[] bargs = packetArgs.split(" ");
                        if (verbose.get()) SwarmPlusWorker.this.info("Received command addressed to baritone. Full: > (highlight)%s(default) < Arguments: > (highlight)%s(default) <".formatted(full, Arrays.stream(bargs).toList().toString()));
                        switch (bargs[0]) {
                            case "gotoxyz" -> {
                                int x = Integer.parseInt(bargs[1]);
                                int y = Integer.parseInt(bargs[2]);
                                int z = Integer.parseInt(bargs[3]);
                                if (verbose.get()) SwarmPlusWorker.this.info("Received baritone command (highlight)gotoxyz %s %s %s".formatted(x,y,z));
                                ChatUtils.sendPlayerMsg("#goto %s %s %s".formatted(x,y,z));
                            }
                            case "gotoxz" -> {
                                int x = Integer.parseInt(bargs[1]);
                                int z = Integer.parseInt(bargs[2]);
                                if (verbose.get()) SwarmPlusWorker.this.info("Received baritone command (highlight)gotoxz %s %s".formatted(x,z));
                                ChatUtils.sendPlayerMsg("#goto %s %s".formatted(x,z));
                            }
                            case "gotoy" -> {
                                int y = Integer.parseInt(bargs[1]);
                                if (verbose.get()) SwarmPlusWorker.this.info("Received baritone command (highlight)gotoy %s".formatted(y));
                                ChatUtils.sendPlayerMsg("#goto %s".formatted(y));
                            }
                            case "follow" -> {
                                String player = bargs[1];
                                if (verbose.get()) SwarmPlusWorker.this.info("Received baritone command (highlight)follow %s".formatted(player));
                                ChatUtils.sendPlayerMsg("#follow player %s".formatted(player));
                                SwarmPlusWorker.this.info("Following (highlight)%s",player);
                            }
                        }
                    }
                    case "LEAVE" -> {
                        SwarmPlusWorker.this.info("Quit Server call from host!");
                        MeteorClient.mc.disconnect();
                    }
                    case "CLOSEGAME" -> {
                        SwarmPlusWorker.this.info("Close game call from host!");
                        System.exit(0);
                    }
                    default -> {
                        SwarmPlusWorker.this.error("An error occurred when receiving a packet from the host. (highlight)%s",full);
                    }
                }
            }

            @Override
            protected void error(String str, Object... args) {
                super.error(str, args);
                if (verbose.get()) SwarmPlusWorker.this.info("Error: " + str.formatted(args));
            }
        };

        client.sendToServer(new Response(Response.Method.TO_SERVER, Response.Type.HANDSHAKE,mc.getSession().getUsername()));

    }

    @Override
    public void onDeactivate() {
        client.disconnect();
    }

}
