package me.trouper.butler;

import com.mojang.logging.LogUtils;
import me.trouper.butler.commands.SwarmManager;
import me.trouper.butler.modules.SwarmPlusMaster;
import me.trouper.butler.modules.SwarmPlusWorker;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Butler");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Butler Addon for Meteor");

        // Modules
        Modules.get().add(new SwarmPlusMaster());
        Modules.get().add(new SwarmPlusWorker());

        // Commands
        Commands.add(new SwarmManager());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "me.trouper.addon";
    }
}
