package com.dumbdogdiner.stickyapi.bukkit.plugin;

import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class StickyPlugin extends JavaPlugin {
    public abstract LocaleProvider getLocale();

    @Getter
    protected Boolean enabled = false;
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void onLoad();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void onEnable();


}
