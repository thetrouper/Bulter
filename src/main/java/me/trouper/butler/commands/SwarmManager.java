package me.trouper.butler.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.trouper.butler.modules.SwarmPlusMaster;
import me.trouper.butler.server.Connection;
import me.trouper.butler.utils.MathUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.commands.arguments.SettingArgumentType;
import meteordevelopment.meteorclient.commands.arguments.SettingValueArgumentType;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SwarmManager extends Command {
    public SwarmManager() {
        super("manager", "Sends a message.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("chat")
            .then(argument("command", StringArgumentType.greedyString())
                .executes(context -> {
                    String exec = context.getArgument("command", String.class);
                    if (SwarmPlusMaster.swarmServer == null) {
                        error("SwarmPlusMaster module is disabled. Start a swarm server to send commands to it!");
                        return SINGLE_SUCCESS;
                    }
                    SwarmPlusMaster.swarmServer.broadcast("[CHAT] " + exec);
                    return SINGLE_SUCCESS;
                }))
            )
            .then(literal("list").executes(context -> {
                List<Connection> connections = SwarmPlusMaster.swarmServer.getConnections().stream().toList();
                StringBuilder connectionList = new StringBuilder();
                int pointer = 0;
                for (Connection connection : connections) {
                    pointer++;
                    connectionList.append("\n%s: %s on %s".formatted(pointer, connection.getClientSideName(),connection.getAddress()));
                }
                info("Swarm connections: " + connectionList.toString());
                return SINGLE_SUCCESS;
            }))
            .then(literal("kick").then(argument("target",StringArgumentType.string())
                .executes(context -> {
                    String target = context.getArgument("target", String.class);
                    if (SwarmPlusMaster.swarmServer == null) {
                        error("SwarmPlusMaster module is disabled. Start a swarm server to send commands to it!");
                        return SINGLE_SUCCESS;
                    }
                    //info("Looping %s connections".formatted(SwarmPlusMaster.swarmServer.connectionCount()));

                    for (Connection connection : SwarmPlusMaster.swarmServer.getConnections()) {
                        //info("Looping connections: %s user %s".formatted(connection.getAddress(),connection.getClientSideName()));
                        if (connection.getClientSideName().equals(target)) connection.disconnect();
                        //info("Disconnected %s".formatted(target),"DO YOU NEED AN ARGUMENT AGAIN?????");
                    }
                    return SINGLE_SUCCESS;
                }))
            )
            .then(literal("toggle")
                .then(argument("module", ModuleArgumentType.create())
                    .executes(context -> {
                        if (SwarmPlusMaster.swarmServer == null) {
                            error("SwarmPlusMaster module is disabled. Start a swarm server to send commands to it!");
                            return SINGLE_SUCCESS;
                        }
                        Module m = ModuleArgumentType.get(context);
                        SwarmPlusMaster.swarmServer.broadcast("[METEOR] toggle " + m.name);
                        return SINGLE_SUCCESS;
                    }).then(literal("on")
                        .executes(context -> {
                            if (SwarmPlusMaster.swarmServer == null) {
                                error("SwarmPlusMaster module is disabled. Start a swarm server to send commands to it!");
                                return SINGLE_SUCCESS;
                            }
                            Module m = ModuleArgumentType.get(context);
                            SwarmPlusMaster.swarmServer.broadcast("[METEOR] toggle " + m.name + " on");
                            return SINGLE_SUCCESS;
                        }))
                    .then(literal("off")
                        .executes(context -> {
                            if (SwarmPlusMaster.swarmServer == null) {
                                error("SwarmPlusMaster module is disabled. Start a swarm server to send commands to it!");
                                return SINGLE_SUCCESS;
                            }
                            Module m = ModuleArgumentType.get(context);
                            SwarmPlusMaster.swarmServer.broadcast("[METEOR] toggle " + m.name + " off");
                            return SINGLE_SUCCESS;
                    }))
                )
            )
            .then(literal("settings")
                .then(argument("module", ModuleArgumentType.create())
                    .then(argument("setting", SettingArgumentType.create())
                        .then(argument("value", SettingValueArgumentType.create())
                                .executes(context -> {
                                    if (SwarmPlusMaster.swarmServer == null) {
                                        error("SwarmPlusMaster module is disabled. Start a swarm server to send commands to it!");
                                        return SINGLE_SUCCESS;
                                    }
                                    Module module = ModuleArgumentType.get(context);
                                    Setting<?> setting = SettingArgumentType.get(context);
                                    String value = SettingValueArgumentType.get(context);

                                    SwarmPlusMaster.swarmServer.broadcast("[METEOR] settings %s %s %s".formatted(module.name,setting.name,value));
                                    ModuleArgumentType.get(context).info("Setting %s changed in %s to %s for all swarm members.", module.title, setting.title, value);

                                    return SINGLE_SUCCESS;
                                }))
                    )
                )
            )
            .then(literal("spread")
                .then(argument("radius", IntegerArgumentType.integer(1))
                    .executes(context -> {
                        if (MeteorClient.mc.player == null) {
                            info("How did we get here?");
                            return SINGLE_SUCCESS;
                        }
                        int rad = context.getArgument("radius",Integer.class);
                        int n = SwarmPlusMaster.swarmServer.connectionCount();
                        Point2D.Double[] distribution = MathUtils.distributePoints(MeteorClient.mc.player.getX(),MeteorClient.mc.player.getZ(),rad,n);
                        int index = 0;
                        for (Connection connection : SwarmPlusMaster.swarmServer.getConnections()) {
                            int x = (int) Math.round(distribution[index].x);
                            int z = (int) Math.round(distribution[index].y);
                            connection.sendMessage("[BARITONE] gotoxz %s %s".formatted(x,z));
                            index++;
                        }
                        SwarmManager.this.info("Bots moving to a circle with radius (highlight)%s(default).",rad);
                        return SINGLE_SUCCESS;
                    }))
            )
            .then(literal("here")
                .executes(context -> {
                    int roundX = (int) Math.round(MeteorClient.mc.player.getX());
                    int roundY = (int) Math.round(MeteorClient.mc.player.getY());
                    int roundZ = (int) Math.round(MeteorClient.mc.player.getZ());
                    SwarmPlusMaster.swarmServer.broadcast("[BARITONE] gotoxyz %s %s %s".formatted(
                        roundX,
                        roundY,
                        roundZ
                    ));
                    SwarmManager.this.info("Pathing (highlight)all bots(default) to the host.");
                    return SINGLE_SUCCESS;
                })
                .then(argument("target",StringArgumentType.string())
                    .executes(context -> {
                        String target = StringArgumentType.getString(context,"target");
                        int roundX = (int) Math.round(MeteorClient.mc.player.getX());
                        int roundY = (int) Math.round(MeteorClient.mc.player.getY());
                        int roundZ = (int) Math.round(MeteorClient.mc.player.getZ());
                        for (Connection connection : SwarmPlusMaster.swarmServer.getConnections().stream().toList()) {
                            if (!connection.getClientSideName().equalsIgnoreCase(target)) continue;
                            connection.sendMessage("[BARITONE] gotoxyz %s %s %s".formatted(
                                roundX,
                                roundY,
                                roundZ
                            ));
                            SwarmManager.this.info("Pathing (highlight)%s(default) to the host.",target);
                            return SINGLE_SUCCESS;
                        }
                        SwarmManager.this.error("Could not find a connection with the name (highlight)%s",target);
                        return SINGLE_SUCCESS;
                    }))
            )
            .then(literal("goto")
                .then(argument("y", IntegerArgumentType.integer()).executes(context -> {
                    SwarmPlusMaster.swarmServer.broadcast("[BARITONE] gotoy %s".formatted(
                        IntegerArgumentType.getInteger(context,"y")
                    ));
                    SwarmManager.this.info("Pathing all bots to (highlight)%s",
                        IntegerArgumentType.getInteger(context,"y")
                    );
                    return SINGLE_SUCCESS;
                }))
                .then(argument("x",IntegerArgumentType.integer())
                    .then(argument("z",IntegerArgumentType.integer())
                        .executes(context -> {
                            SwarmPlusMaster.swarmServer.broadcast("[BARITONE] gotoxz %s %s".formatted(
                                    IntegerArgumentType.getInteger(context,"x"),
                                    IntegerArgumentType.getInteger(context,"z")
                                ));
                            SwarmManager.this.info("Pathing all bots to (highlight)%s %s",
                                IntegerArgumentType.getInteger(context,"x"),
                                IntegerArgumentType.getInteger(context,"z"));
                            return SINGLE_SUCCESS;
                        })))
                .then(argument("x",IntegerArgumentType.integer())
                    .then(argument("y",IntegerArgumentType.integer())
                        .then(argument("z",IntegerArgumentType.integer())
                            .executes(context -> {
                                SwarmPlusMaster.swarmServer.broadcast("[BARITONE] gotoxyz %s %s %s".formatted(
                                        IntegerArgumentType.getInteger(context,"x"),
                                        IntegerArgumentType.getInteger(context,"y"),
                                        IntegerArgumentType.getInteger(context,"z")
                                    ));
                                SwarmManager.this.info("Pathing all bots to (highlight)%s %s %s",
                                        IntegerArgumentType.getInteger(context,"x"),
                                        IntegerArgumentType.getInteger(context,"y"),
                                        IntegerArgumentType.getInteger(context,"z")
                                    );
                                return SINGLE_SUCCESS;
                            }))))
            )
            .then(literal("rotate")
                .then(literal("absolute")
                    .then(argument("pitch", DoubleArgumentType.doubleArg(0,360))
                        .then(argument("yaw", DoubleArgumentType.doubleArg(0,360))
                            .executes(context -> {
                                SwarmPlusMaster.swarmServer.broadcast("[LOOK] absolute %s %s".formatted(
                                    context.getArgument("pitch",double.class),
                                    context.getArgument("yaw",double.class)));
                                SwarmManager.this.info("Bots now facing (highlight)%s %s",
                                    context.getArgument("pitch",double.class),
                                    context.getArgument("yaw",double.class));
                                return SINGLE_SUCCESS;
                            }))
                    )
                )
                .then(literal("player")
                    .then(argument("target",PlayerArgumentType.create())
                        .executes(context -> {
                            SwarmPlusMaster.swarmServer.broadcast("[LOOK] player %s".formatted(context.getArgument("target",String.class)));
                            SwarmManager.this.info("Bots now targeting (highlight)%s",context.getArgument("target",String.class));
                            return SINGLE_SUCCESS;
                        }))
                )
            )
            .then(literal("follow")
                .then(argument("player", PlayerArgumentType.create())
                    .executes(context -> {
                        PlayerEntity pe = PlayerArgumentType.get(context);
                        SwarmPlusMaster.swarmServer.broadcast("[BARITONE] follow %s".formatted(pe.getName().getString()));
                        SwarmManager.this.info("Bots now following (highlight)%s(default).",pe.getName().getString());
                        return SINGLE_SUCCESS;
                    }))
            )
            .then(literal("closegame").executes(context -> {
                SwarmPlusMaster.swarmServer.broadcast("[CLOSEGAME]");
                return SINGLE_SUCCESS;
            }))
            .then(literal("stop").executes(context -> {
                SwarmPlusMaster.swarmServer.broadcast("[STOP]");
                return SINGLE_SUCCESS;
            }))
            .then(literal("raw")
                .then(literal("all")
                    .then(argument("packet",StringArgumentType.greedyString())
                        .executes(context -> {
                            SwarmPlusMaster.swarmServer.broadcast(StringArgumentType.getString(context,"packet"));
                            return SINGLE_SUCCESS;
                        })))
                .then(literal("dm")
                    .then(argument("target",StringArgumentType.string())
                        .then(argument("packet",StringArgumentType.greedyString())
                            .executes(context -> {
                                String target = StringArgumentType.getString(context,"target");
                                for (Connection connection : SwarmPlusMaster.swarmServer.getConnections().stream().toList()) {
                                    if (!connection.getClientSideName().equalsIgnoreCase(target)) continue;
                                    connection.sendMessage(StringArgumentType.getString(context,"packet"));
                                    return SINGLE_SUCCESS;
                                }
                                return SINGLE_SUCCESS;
                            })
                        )
                    )
                )
            );
    }

}
