/**
 * Copyright (c) 2020 DumbDogDiner <a href="dumbdogdiner.com">&lt;dumbdogdiner.com&gt;</a>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.bukkit.command;

import com.dumbdogdiner.stickyapi.bukkit.command.builder.CommandBuilder;
import com.dumbdogdiner.stickyapi.bukkit.util.NotificationType;
import com.dumbdogdiner.stickyapi.bukkit.util.SoundUtil;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickyapi.common.util.ReflectionUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.dumbdogdiner.stickyapi.bukkit.command.ExitCode.EXIT_COOLDOWN;

/**
 * Represents a {@link org.bukkit.command.Command} belonging to a plugin
 * <p>
 * Cloned from bukkit to prevent reflective calls
 */
// Fuck you reflection, and fuck you Java for changing it so much!!!
    //I'ma fuggin rewrite bits of this so its not garbage
public abstract class PluginCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {
    private final Plugin owningPlugin;
    private TabCompleter completer;
    protected List<Permission> commandPermissions;
    protected boolean playSounds = false;
    protected Map<CommandSender, Long> cooldownSenders = new HashMap<>();
    protected static long COOLDOWN_TIME = 0L;
    //fixme point to the internal messages yml plz;
    private LocaleProvider rootLocaleProvider = new LocaleProvider(null);
    @Setter
    @Getter
    CommandBuilder.ErrorHandler errorHandler;


    public class StatusInfo {
        @Getter
        private final TreeMap<String, String> variables;
        @Getter
        private final ExitCode exitCode;

        public StatusInfo(TreeMap<String, String> variables, ExitCode exitCode) {
            this.variables = variables;
            variables.put("plugin", owningPlugin.getName());
            this.exitCode = exitCode;
        }
    }

    public class SilentErrorHandler implements CommandBuilder.ErrorHandler{

        @Override
        public void apply(ExitCode exitCode, CommandSender sender, Arguments args, TreeMap<String, String> vars) {
            vars.put("plugin", owningPlugin.getName());
            switch(exitCode){
                case EXIT_PERMISSION_DENIED:
                    sender.sendMessage(rootLocaleProvider.translate("no-permission", vars));
                    break;
                case EXIT_MUST_BE_PLAYER:
                    sender.sendMessage(rootLocaleProvider.translate("must-be-player", vars));
                    break;
                case EXIT_COOLDOWN:
                    // could be wrong i guess?? fixme
                    float timeToWait = Math.max(0f,cooldownSenders.getOrDefault(sender, (long) Bukkit.getWorlds().get(1).getFullTime()) - Bukkit.getWorlds().get(1).getFullTime() / 20.0f);
                    vars.put("cooldown", String.format("%.1f", timeToWait));
                    sender.sendMessage(rootLocaleProvider.translate("cooldown", vars));
            }

        }
    }

    public class NoisyErrorHandler extends SilentErrorHandler implements CommandBuilder.ErrorHandler{

        @Override
        public void apply(ExitCode exitCode, CommandSender sender, Arguments args, TreeMap<String, String> vars) {
            super.apply(exitCode, sender, args, vars);
            playSound(sender, exitCode.getType());
        }
    }

    public PluginCommand(@NotNull String name, @Nullable List<String> aliases, @NotNull Plugin owner) {
        super(name);
        if(aliases != null)
            setAliases(aliases);
        this.owningPlugin = owner;

    }

    abstract public ExitCode execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args);

    /**
     * Executes the command, returning its success, is final so as to encourage use of StickyAPI arguments, etc
     * <p>
     * Returns true if the command was successful, otherwise false
     * 
     * @param sender       Source object which is executing this command
     * @param commandLabel The alias of the command used
     * @param args         All arguments passed to the command, split via ' '
     * @return {@link java.lang.Boolean}
     */
    @Override
    final public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!owningPlugin.isEnabled()) {
            throw new CommandException("Cannot execute command '" + commandLabel + "' in plugin "
                    + owningPlugin.getDescription().getFullName() + " - plugin is disabled.");
        }
        Arguments arguments = new Arguments(Arrays.asList(args));
ExitCode code;
        try {
            code = checkCooldown(sender) ? EXIT_COOLDOWN : execute(sender,commandLabel,arguments);
            switch(code){
                case EXIT_ERROR_SILENT:
                case EXIT_SUCCESS:
                    playSound(sender, NotificationType.SUCCESS);
                    return true;
                case EXIT_INFO:
                    playSound(sender, NotificationType.INFO);
                    return true;
                case EXIT_INVALID_SYNTAX:
                    if (usageMessage.length() > 0) {
                        for (String line : usageMessage.replace("<command>", commandLabel).split("\n")) {
                            sender.sendMessage(line);
                        }
                    }
                default:
                    playSound(sender, NotificationType.ERROR);
                    onError(sender, commandLabel, arguments, code);
                    return false;
            }
        } catch (Throwable ex) {
            throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin "
                    + owningPlugin.getDescription().getFullName(), ex);
        }
    }

    private boolean checkCooldown(CommandSender sender) {
        if(!cooldownSenders.containsKey(sender)){
            Bukkit.getScheduler().runTaskLaterAsynchronously(owningPlugin, () -> {
               cooldownSenders.remove(sender);
            }, COOLDOWN_TIME);
            cooldownSenders.put(sender, Bukkit.getCurrentTick() + COOLDOWN_TIME);
            return false;
        }
        return true;
    }


    /**
     * Gets the owner of this PluginCommand
     *
     * @return Plugin that owns this command
     */
    @Override
    @NotNull
    public Plugin getPlugin() {
        return owningPlugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String [] args) throws IllegalArgumentException, CommandException;

    /**
     * <p>
     * Stub method to perform validation for tabcomplete
     * <p>
     * If it is not present or returns null, will delegate to the current command
     * executor if it implements {@link TabCompleter}. If a non-null list has not
     * been found, will default to standard player name completion in
     * {@link Command#tabComplete(CommandSender, String, String[])}.
     * <p>
     * @throws IllegalArgumentException if sender, alias, or args is null
     */

    public void validateTabComplete(@NotNull CommandSender sender, @NotNull String alias,
            @NotNull String[] args) throws IllegalArgumentException {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(", ").append(owningPlugin.getDescription().getFullName()).append(')');
        return stringBuilder.toString();
    }

    /**
     * Register the command with a {@link org.bukkit.plugin.Plugin}
     *
     * @param plugin to register with
     */
    public final void register(@NotNull Plugin plugin) {
        CommandMap cmap = ReflectionUtil.getProtectedValue(plugin.getServer(), "commandMap");
        assert cmap != null;
        cmap.register(plugin.getName(), this);
    }

    private void playSound(CommandSender sender, NotificationType type) {
        if (this.playSounds)
            SoundUtil.send(sender, type);
    }
    void enableSounds(){
        playSounds = true;
    }
}
