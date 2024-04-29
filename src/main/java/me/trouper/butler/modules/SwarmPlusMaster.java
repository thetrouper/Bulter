package me.trouper.butler.modules;

import me.trouper.butler.Addon;
import me.trouper.butler.server.Address;
import me.trouper.butler.server.Server;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.net.Socket;

public class SwarmPlusMaster extends Module {

    public static Server swarmServer = null;

    private final SettingGroup general = settings.getDefaultGroup();

    public SwarmPlusMaster() {
        super(Addon.CATEGORY, "swarm-plus-host", "(Host/Master) Control multiple instances of meteor through one account, but better.");
    }

    private final Setting<String> ip = general.add(new StringSetting.Builder()
        .name("address")
        .defaultValue("localhost")
        .build());

    private final Setting<Integer> port = general.add(new IntSetting.Builder()
        .name("port")
        .defaultValue(25561)
        .max(65535)
        .min(0)
        .build());

    private final Setting<Boolean> verbose = general.add(new BoolSetting.Builder()
        .name("verbose")
        .defaultValue(false)
        .build()
    );

    @Override
    public void onActivate() {
        swarmServer = new Server(new Address(ip.get(),port.get())) {

            @Override
            public void onHandShake(Address address, String clientSideName) {
                super.onHandShake(address, clientSideName);
                SwarmPlusMaster.this.info("%s connected on %s".formatted(clientSideName,address));
            }

            @Override
            public synchronized boolean removeConnection(Address address) {
                SwarmPlusMaster.this.info("Client disconnecting: " + address);
                return super.removeConnection(address);
            }

            @Override
            protected void info(String str, Object... args) {
                super.info(str, args);
                if (verbose.get()) SwarmPlusMaster.this.info(str.formatted(args));
            }

            @Override
            protected void error(String str, Object... args) {
                super.error(str, args);
                if (verbose.get()) SwarmPlusMaster.this.info("Error: " + str.formatted(args));
            }
        };
        info("Started a new server on IP %s with port %s".formatted(ip,port));
    }

    @Override
    public void onDeactivate() {
        swarmServer.disconnect();
        info("Closed the swarm server.","THERE YOU GO METEOR HERE IS YOUR ARG NOW DON'T CRASH");
    }

}
