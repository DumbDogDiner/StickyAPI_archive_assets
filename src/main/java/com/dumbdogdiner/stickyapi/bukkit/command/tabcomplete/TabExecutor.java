package com.dumbdogdiner.stickyapi.bukkit.command.tabcomplete;

import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TabExecutor {
    @NotNull
    List<String> tabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull Arguments arguments);
}
