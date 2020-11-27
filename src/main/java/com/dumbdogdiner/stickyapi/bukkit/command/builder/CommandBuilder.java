/**
 * Copyright (c) 2020 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.bukkit.command.builder;


import com.dumbdogdiner.stickyapi.StickyAPI;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.bukkit.command.PluginCommand;
import com.dumbdogdiner.stickyapi.bukkit.plugin.StickyPlugin;
import com.dumbdogdiner.stickyapi.bukkit.util.NotificationType;
import com.dumbdogdiner.stickyapi.bukkit.util.SoundUtil;
import com.dumbdogdiner.stickyapi.common.ServerVersion;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.util.ReflectionUtil;
import com.dumbdogdiner.stickyapi.common.util.StringUtil;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.FutureTask;

/**
 * CommandBuilder for avoiding bukkit's terrible command API and making creating
 * new commands as simple as possible
 * 
 * @since 2.0
 */
public class CommandBuilder {
    Boolean synchronous = false;
    Boolean requiresPlayer = false;
    String name;
    Permission permission;
    String description;
    Boolean playSound = false;
    List<String> aliases = new ArrayList<>();
    Long cooldown = 0L;

    Executor executor;
    TabExecutor tabExecutor;

    ErrorHandler errorHandler;

    PluginCommand bukkitCommand;

    HashMap<String, CommandBuilder> subCommands = new HashMap<>();



    @FunctionalInterface
    public interface Executor {
        public ExitCode execute(CommandSender sender, Arguments args, String label, Map<String, String> vars);
    }

    @FunctionalInterface
    public interface TabExecutor {
        public java.util.List<String> tabComplete(CommandSender sender, String commandLabel, Arguments args);
    }

    @FunctionalInterface
    public interface ErrorHandler {
        public void onComplete(ExitCode exitCode, CommandSender sender, Arguments args, Map<String, String> vars);
    }

    /**
     * Create a new [@link CommandBuilder} instance
     * <p>
     * Used to build and register Bukkit commands
     * 
     * @param name The name of the command
     */
    public CommandBuilder(@NotNull String name) {
        this.name = name;
    }

    /**
     * If this command should run asynchronously
     * 
     * @param synchronous if this command should run synchronously
     * @return {@link CommandBuilder}
     */
    public CommandBuilder synchronous(@NotNull Boolean synchronous) {
        this.synchronous = synchronous;
        return this;
    }

    /**
     * Set this command to run asynchronously
     * 
     * @return {@link CommandBuilder}
     */
    public CommandBuilder synchronous() {
        return this.synchronous(true);
    }

    /**
     * Set the cooldown for this command
     * 
     * @param cooldown in milliseconds
     * @return {@link CommandBuilder}
     */
    public CommandBuilder cooldown(@NotNull Long cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    /**
     * If this command requires the sender to be an instance of
     * {@link org.bukkit.entity.Player}
     * 
     * @param requiresPlayer If this command should require a player as the executor
     * @return {@link CommandBuilder}
     */
    public CommandBuilder requiresPlayer(@NotNull Boolean requiresPlayer) {
        this.requiresPlayer = requiresPlayer;
        return this;
    }

    /**
     * If this command requires the sender to be an instance of
     * {@link org.bukkit.entity.Player}
     * 
     * @return {@link CommandBuilder}
     */
    public CommandBuilder requiresPlayer() {
        return this.requiresPlayer(true);
    }

    /**
     * If this command should play a sound upon exiting
     * 
     * @param playSound If this command should play a sound upon exiting
     * @return {@link CommandBuilder}
     */
    public CommandBuilder playSound(@NotNull Boolean playSound) {
        this.playSound = playSound;
        return this;
    }

    /**
     * If this command should play a sound upon exiting
     * 
     * @return {@link CommandBuilder}
     */
    public CommandBuilder playSound() {
        return this.playSound(true);
    }

    /**
     * Set the permission of the command
     * 
     * @param permission to set
     * @return {@link CommandBuilder}
     */
    public CommandBuilder permission(@NotNull String permission) {
        this.permission = new Permission(permission);
        return this;
    }

    /**
     * Set the permission of the command
     *
     * @param permission to set
     * @return {@link CommandBuilder}
     */
    public CommandBuilder permission(@NotNull Permission permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Set the description of the command
     * 
     * @param description to set
     * @return {@link CommandBuilder}
     */
    public CommandBuilder description(@NotNull String description) {
        this.description = description;
        return this;
    }

    /**
     * Add an alias to this command.
     * 
     * @param alias to add
     * @return {@link CommandBuilder}
     */
    public CommandBuilder alias(@NotNull String... alias) {
        this.aliases.addAll(Arrays.asList(alias));
        return this;
    }

    /**
     * Set the aliases of the command
     * 
     * @param aliases to set
     * @return {@link CommandBuilder}
     */
    public CommandBuilder aliases(@NotNull List<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    /**
     * Add a subcommand to a command
     * 
     * @param builder the sub command
     * @return {@link CommandBuilder}
     */
    public CommandBuilder subCommand(@NotNull CommandBuilder builder) {
        builder.synchronous = this.synchronous;
        this.subCommands.put(builder.name, builder);
        return this;
    }

    /**
     * Set the executor of the command
     * 
     * @param executor to set
     * @return {@link CommandBuilder}
     */
    public CommandBuilder onExecute(@NotNull Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Set the tab complete executor of the command
     * 
     * @param executor to set
     * @return {@link CommandBuilder}
     */
    public CommandBuilder onTabComplete(@NotNull TabExecutor executor) {
        this.tabExecutor = executor;
        return this;
    }

    /**
     * Set the error handler of the command
     * 
     * @param handler to set
     * @return {@link CommandBuilder}
     */
    public CommandBuilder onError(@NotNull ErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }

    private void performAsynchronousExecution(CommandSender sender, PluginCommand command, String label,
            List<String> args) {
        StickyAPI.getPool().execute(new FutureTask<Void>(() -> {
            command.execute(sender, label, args.toArray(new String[]));
            return null;
        }));
    }

    /**
     *
     * @param sender the commandsender
     * @param command the bukkit command
     * @param args the args provided to the main command
     * @return if a subcommand was found and executed
     */
    private boolean executeSubCommandIfExists(CommandSender sender, PluginCommand command, List<String> args){
        if (args.size() > 0 && subCommands.containsKey(args.get(0))) {
            CommandBuilder subCommand = subCommands.get(args.get(0));
            String subLabel = args.get(0);
            List<String> subArgs = args.subList(1, args.size());
            if(synchronous != subCommand.synchronous){
                if(subCommand.synchronous){
                    subCommand.performAsynchronousExecution(sender, command, subLabel, subArgs);
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(command.getPlugin(), () ->
                            errorHandler.onsubCommand.performExecution(sender, command, subLabel, subArgs), 1L);
                }
            }
        }
    }

    /**
     * Execute this command.
     * @return the exit code from command
     */
    private ExitCode performExecution(CommandSender sender, String label, Arguments args, Map<String, String> variables) {
        return executor.execute(sender, args, label, variables);
    }

    /**
     * Generate a permission for a given command
     * @param plugin The plugin that owns the command
     * @param name The name of the command
     * @return the permission of format &lt;PluginName&gt;.&lt;CommandName&gt;
     */
    public static Permission getBasePermissionName(@NotNull StickyPlugin plugin, @NotNull String name){
        return new Permission((plugin.getName() + '.' +  name).toLowerCase());
    }

    /**
     * Build the command!
     * 
     * @param plugin to build it for
     * @return {@link org.bukkit.command.Command}
     */
    public org.bukkit.command.Command build(@NotNull StickyPlugin plugin) {

        if(permission == null){
           permission = getBasePermissionName(plugin, name);
        }
        PluginCommand command = new PluginCommand(name, aliases, plugin, permission, playSound) {

            @Override
            public ExitCode execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args, @NotNull Map<String, String> variables) {
                if(executeSubCommandIfExists(sender, this, args.getRawArgs())){
                    return null;
                } else {
                    return performExecution(sender, this, alias, args, variables);
                }
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException, CommandException {
                return null;
            }

            @Override
            protected void onError(CommandSender sender, String commandLabel, Arguments arguments, ExitCode code, Map<String, String> vars){

            }
        };

        if (this.synchronous == null) {
            this.synchronous = false;
        }

        // Execute the command by creating a new CommandExecutor and passing the
        // arguments to our executor
        command.setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label,
                    String[] args) {
                performExecution(sender, command, label, Arrays.asList(args));
                return true;
            }
        });

        command.setTabCompleter(new TabCompleter() {
            @Override
            public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
                if (tabExecutor == null) {
                    if (args.length == 0) {
                        return ImmutableList.of();
                    }

                    String lastWord = args[args.length - 1];

                    Player senderPlayer = sender instanceof Player ? (Player) sender : null;

                    ArrayList<String> matchedPlayers = new ArrayList<String>();
                    for (Player player : sender.getServer().getOnlinePlayers()) {
                        String name = player.getName();
                        if ((senderPlayer == null || senderPlayer.canSee(player))
                                && StringUtil.startsWithIgnoreCase(name, lastWord)) {
                            matchedPlayers.add(name);
                        }
                    }

                    Collections.sort(matchedPlayers, String.CASE_INSENSITIVE_ORDER);
                    return matchedPlayers;
                } else {
                    return tabExecutor.tabComplete(sender, alias, new Arguments(Arrays.asList(args)));
                }
            }
        });

        command.setDescription(this.description);

        if (this.aliases != null)
            command.setAliases(this.aliases);

        command.setPermission(this.permission);
        this.bukkitCommand = command;
        return command;
    }

    /**
     * Register the command with a {@link org.bukkit.plugin.Plugin}
     * 
     * @param plugin to register with
     */
    public void register(@NotNull Plugin plugin) {

        // If the server is running paper, we don't need to do reflection, which is
        // good.
        if (ServerVersion.isPaper()) {
            plugin.getServer().getCommandMap().register(plugin.getName(), this.build(plugin));
            return;
        }
        // However, if it's not running paper, we need to use reflection, which is
        // really annoying
        ((CommandMap) ReflectionUtil.getProtectedValue(plugin.getServer(), "commandMap")).register(plugin.getName(),
                this.build(plugin));
    }

    private void _playSound(CommandSender sender, NotificationType type) {
        if (!this.playSound)
            return;
        SoundUtil.send(sender, type);
    }
}
