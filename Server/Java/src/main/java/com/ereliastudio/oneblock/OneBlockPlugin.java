package com.ereliastudio.oneblock;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public final class OneBlockPlugin extends JavaPlugin {
    public OneBlockPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    public void setup() {
        getEntityStoreRegistry().registerSystem(new OneBlockBreakSystem());
    }
}