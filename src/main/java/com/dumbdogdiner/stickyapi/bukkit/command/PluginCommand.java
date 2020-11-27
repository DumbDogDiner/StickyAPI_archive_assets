/**
 * Copyright (c) 2020 DumbDogDiner <a href="dumbdogdiner.com">&lt;dumbdogdiner.com&gt;</a>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.bukkit.command;

import com.dumbdogdiner.stickyapi.bukkit.command.builder.CommandBuilder;
import com.dumbdogdiner.stickyapi.bukkit.plugin.StickyPlugin;
import com.dumbdogdiner.stickyapi.bukkit.util.NotificationType;
import com.dumbdogdiner.stickyapi.bukkit.util.SoundUtil;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;
import com.dumbdogdiner.stickyapi.common.util.ReflectionUtil;
import com.dumbdogdiner.stickyapi.common.util.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.dumbdogdiner.stickyapi.bukkit.command.ExitCode.*;

/**
 * Represents a {@link org.bukkit.command.Command} belonging to a plugin
 * <p>
 * Cloned from bukkit to prevent reflective calls
 */
// Fuck you reflection, and fuck you Java for changing it so much!!!
//I'ma fuggin rewrite bits of this so its not garbage
public abstract class PluginCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {
    private final StickyPlugin owningPlugin;
    @Getter
    protected List<Permission> commandPermissions = new ArrayList<>();
    protected boolean playSounds = false;
    @Getter
    private final LocaleProvider locale;
    @Setter
    @Getter
    CommandBuilder.ErrorHandler errorHandler;
    protected long COOLDOWN_TIME;
    protected CooldownManager cooldowns;
    @Getter
    protected Permission basePermission;
    protected boolean requiresPlayer = false;



    /**
     * A StickyPluginCommand
     * @param name the name of the command
     * @param aliases aliases of the command
     * @param owner the owning plugin
     */
    public PluginCommand(@NotNull String name, @Nullable List<String> aliases, @NotNull StickyPlugin owner) {
        this(name, aliases, owner, new Permission((owner.getName() + '.' +  name).toLowerCase()));
    }

    /**
     * A StickyPluginCommand
     * @param name the name of the command
     * @param aliases aliases of the command
     * @param owner the owning plugin
     * @param basePermission the permission to execute the command
     */
    public PluginCommand(@NotNull String name, @Nullable List<String> aliases, @NotNull StickyPlugin owner, @NotNull Permission basePermission) {
        super(name);
        if(aliases != null)
            setAliases(aliases);
        this.owningPlugin = owner;
        this.basePermission = basePermission;
        setPermission(basePermission.getName());
        this.locale = owner.getLocale();
        commandPermissions.add(0, basePermission);
        cooldowns = new CooldownManager(COOLDOWN_TIME);
    }

    abstract public ExitCode execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull Arguments args, @NotNull Map<String, String> variables);

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

        Map<String, String> variables = locale.newVariables();
        variables.put("command", getName());
        variables.put("sender", sender.getName());
        variables.put("player", sender.getName());
        variables.put("uuid", (sender instanceof Player) ? ((Player) sender).getUniqueId().toString() : "");
        Arguments arguments = new Arguments(Arrays.asList(args));

        long cooldown = cooldowns.getSenderRemainingCooldown(sender);
        if(cooldown > 0L){
            variables.put("cooldown", TimeUtil.durationString(COOLDOWN_TIME));
            variables.put("cooldown-remaining", TimeUtil.durationString(cooldown));
            onError(sender,commandLabel, arguments, EXIT_COOLDOWN, variables);
            return true;
        }

        if(requiresPlayer && sender instanceof ConsoleCommandSender){
            onError(sender, commandLabel, arguments, EXIT_MUST_BE_PLAYER, variables);
            return true;
        }

        if (sender instanceof Player && !sender.hasPermission(basePermission)) {
            onError(sender, commandLabel, arguments, EXIT_PERMISSION_DENIED, variables);
            return true;
        }

        try {
            ExitCode code = execute(sender, commandLabel, arguments, variables);
            onError(sender, commandLabel, arguments, code, variables);
            return true;
        } catch (Throwable ex) {
            throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin "
                    + owningPlugin.getDescription().getFullName(), ex);

        }
    }

    protected void onError(CommandSender sender, String commandLabel, Arguments arguments, ExitCode code, Map<String, String> vars) {
        playSound(sender, code);
        switch(code) {
            case EXIT_ERROR_SILENT:
            case EXIT_SUCCESS:
            case EXIT_INFO:
                return;
            case EXIT_PERMISSION_DENIED:
                sender.sendMessage(locale.translate("permission-denied", vars));
                return;
            case EXIT_BAD_SENDER:
                sender.sendMessage(locale.translate("bad-sender", vars));
                return;
            case EXIT_MUST_BE_PLAYER:
                sender.sendMessage(locale.translate("must-be-player", vars));
                return;
            case EXIT_COOLDOWN:
                sender.sendMessage(locale.translate("cooldown", vars));
                return;
            case EXIT_INVALID_SYNTAX:
                if (usageMessage.length() > 0) {
                    for (String line : usageMessage.replace("<command>", commandLabel).split("\n")) {
                        sender.sendMessage(line);
                    }
                }
                return;
            case EXIT_ERROR:
            default:
                sender.sendMessage(locale.translate("generic-error", vars));
        }
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

    private void playSound(CommandSender sender, ExitCode code) {
        playSound(sender, code.getType());
    }

    private void playSound(CommandSender sender, NotificationType type) {
        if (this.playSounds)
            SoundUtil.send(sender, type);
    }
    void enableSounds(){
        playSounds = true;
    }
}
