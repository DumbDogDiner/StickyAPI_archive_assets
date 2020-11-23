/**
 * Copyright (c) 2020 DumbDogDiner <a href="dumbdogdiner.com">&lt;dumbdogdiner.com&gt;</a>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.bukkit.command.builder;

import com.dumbdogdiner.stickyapi.StickyAPI;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.bukkit.command.PluginCommand;
import com.dumbdogdiner.stickyapi.bukkit.util.NotificationType;
import com.dumbdogdiner.stickyapi.bukkit.util.SoundUtil;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickyapi.common.util.ReflectionUtil;
import com.dumbdogdiner.stickyapi.common.util.StringUtil;
import com.google.common.collect.ImmutableList;
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

    // Before you ask, yes, this should be it's own object
    // Before you ask, it's not because that's too much work for poor zachy, and I
    // personally don't like the amount of code dupe it introduces
    // so i'll stick with this.


    // THEREFORE I will make it abstract and use inheritance uwu

    // and while im at it i'll make an interface that it ends up producing or something
    Boolean subCommand = false;

    protected Boolean synchronous = false;
    protected Boolean requiresPlayer = false;
    protected String name;
    Permission permission;
    String description;
    Boolean playSound = false;
    List<String> aliases = new ArrayList<>();
    Long cooldown = 0L;

    Executor executor;
    TabExecutor tabExecutor = new TabExecutor() {
        @Override
        public List<String> apply(CommandSender sender, String commandLabel, Arguments arguments) {
            String [] args = (String[]) arguments.getRawArgs().toArray();
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

            matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
            return matchedPlayers;
        }
    };

    ErrorHandler errorHandler;

    Command bukkitCommand;

    HashMap<String, CommandBuilder> subCommands = new HashMap<>();


    //Fixme should have a scheduled task thing that adds uuids or senders to a list, and removes them after a time ellapses.
    // Hmm...
    HashMap<CommandSender, Long> cooldownSenders = new HashMap<>();

    @FunctionalInterface
    public interface Executor {
        public ExitCode apply(CommandSender sender, Arguments args, TreeMap<String, String> vars);
    }

    @FunctionalInterface
    public interface TabExecutor {
        public java.util.List<String> apply(CommandSender sender, String commandLabel, Arguments args);
    }

    @FunctionalInterface
    public interface ErrorHandler {
        public void apply(ExitCode exitCode, CommandSender sender, Arguments args, TreeMap<String, String> vars);
    }

    /**
     * Create a new [@link CommandBuilder} instance
     * <p>
     * Used to build and register Bukkit commands
     * 
     * @param name
     */
    public CommandBuilder(@NotNull String name) {
        this.name = name;
    }

    /**
     * If this command should run asynchronously
     * 
     * @param synchronous
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
    public CommandBuilder cooldown(Long cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    /**
     * If this command requires the sender to be an instance of
     * {@link org.bukkit.entity.Player}
     * 
     * @param requiresPlayer If a player is required
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
     * @param playSound if sound should be enabled
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
        this.aliases.addAll(aliases);
        return this;
    }

    /**
     * Add a subcommand to a command
     * 
     * @param builder the sub command
     * @return {@link CommandBuilder}
     */
    public CommandBuilder subCommand(SubComandBuilder builder) {
        builder.synchronous = this.synchronous;
        //builder.subCommand = true;
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

    private void performAsynchronousExecution(CommandSender sender, org.bukkit.command.Command command, String label,
            List<String> args) {
        StickyAPI.getPool().execute(new FutureTask<Void>(() -> {
            performExecution(sender, command, label, args);
            return null;
        }));
    }

    /**
     * Execute this command. Checks for existing sub-commands, and runs the error
     * handler if anything goes wrong.
     */
    private void performExecution(CommandSender sender, org.bukkit.command.Command command, String label,
            List<String> args) {
        // look for subcommands
        if (args.size() > 0 && subCommands.containsKey(args.get(0))) {
            CommandBuilder subCommand = subCommands.get(args.get(0));
            if (!synchronous && subCommand.synchronous) {
                throw new RuntimeException("Attempted to asynchronously execute a synchronous sub-command!");
            }

            // We can't modify List, so we need to make a clone of it, because java is
            // special.
            List<String> subArgs = args.subList(1, args.size());

            // spawn async command from sync
            if (synchronous && !subCommand.synchronous) {
                subCommand.performAsynchronousExecution(sender, command, label, subArgs);
            }

            subCommand.performExecution(sender, command, label, subArgs);
            return;
        }

        ExitCode exitCode;
        Arguments a = new Arguments(args);
        var variables = new TreeMap<String, String>();
        variables.put("command", command.getName());
        variables.put("sender", sender.getName());
        variables.put("player", sender.getName());
        variables.put("uuid", (sender instanceof Player) ? ((Player) sender).getUniqueId().toString() : "");
        variables.put("cooldown", cooldown.toString());
        variables.put("cooldown_remaining",
                cooldownSenders.containsKey(sender)
                        ? String.valueOf(cooldown - (System.currentTimeMillis() - cooldownSenders.get(sender)))
                        : "0");
        try {
            if (cooldownSenders.containsKey(sender)
                    && ((System.currentTimeMillis() - cooldownSenders.get(sender)) < cooldown)) {
                exitCode = ExitCode.EXIT_COOLDOWN;
            } else {

                // Add our sender and their command execution time to our hashmap of coolness
                cooldownSenders.put(sender, System.currentTimeMillis());

                // If the user does not have permission to execute the sub command, don't let
                // them execute and return permission denied
                if (this.permission != null && !sender.hasPermission(this.permission)) {
                    exitCode = ExitCode.EXIT_PERMISSION_DENIED;
                } else if (this.requiresPlayer && !(sender instanceof Player)) {
                    exitCode = ExitCode.EXIT_MUST_BE_PLAYER;
                } else {
                    exitCode = executor.apply(sender, a, variables);
                }
            }
        } catch (Exception e) {
            exitCode = ExitCode.EXIT_ERROR;
            e.printStackTrace();
        }

        // run the error handler - something made a fucky wucky uwu
        if (exitCode != ExitCode.EXIT_SUCCESS) {
            if (exitCode == ExitCode.EXIT_INFO) {
                _playSound(sender, NotificationType.INFO);
                return;
            }
            errorHandler.apply(exitCode, sender, a, variables);
            _playSound(sender, NotificationType.ERROR);
        } else {
            _playSound(sender, NotificationType.SUCCESS);
        }
    }

    /**
     * Build the command!
     * 
     * @param plugin to build it for
     * @return {@link org.bukkit.command.Command}
     */
    public org.bukkit.command.Command build(@NotNull Plugin plugin, LocaleProvider locale) {
        if (this.synchronous == null) {
            this.synchronous = false;
        }

        PluginCommand command = new PluginCommand(this.name, aliases,  plugin, locale){
            {
                playSounds = playSound;
            }
            @Override
            public ExitCode execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args) {
                return null;
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException, CommandException {
                return tabExecutor.apply(sender, alias, new Arguments(Arrays.asList(args)));
            }
        };



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



        command.setDescription(this.description);

        if (this.aliases != null)
            command.setAliases(this.aliases);

        command.setPermission(this.permission);
        this.bukkitCommand = command;
        return command;
    }


}
