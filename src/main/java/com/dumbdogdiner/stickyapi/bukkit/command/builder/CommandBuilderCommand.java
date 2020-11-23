package com.dumbdogdiner.stickyapi.bukkit.command.builder;

import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.bukkit.command.PluginCommand;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//This makes it package local right?? I hope??
class CommandBuilderCommand extends PluginCommand {
    public CommandBuilderCommand(@NotNull String name, @Nullable List<String> aliases, @NotNull Plugin owner) {
        super(name, aliases, owner);
    }

    @Override
    public ExitCode execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) {
        return null;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException, CommandException {
        return null;
    }

    public void enableSounds(){
        playSounds = true;
    }
}
