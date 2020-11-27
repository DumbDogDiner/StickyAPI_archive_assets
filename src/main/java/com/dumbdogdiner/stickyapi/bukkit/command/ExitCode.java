/**
 * Copyright (c) 2020 DumbDogDiner <a href="dumbdogdiner.com">&lt;dumbdogdiner.com&gt;</a>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.bukkit.command;

import com.dumbdogdiner.stickyapi.bukkit.util.NotificationType;
import lombok.Getter;

/**
 * Enum based exit codes for StickyAPI command classes.
 */
public enum ExitCode {
    /**
     * If the command executed successfully
     */
    EXIT_SUCCESS(NotificationType.SUCCESS),
    /**
     * If the command executed successfully, but is just giving the sender
     * information such as the plugin version
     */
    EXIT_INFO(NotificationType.INFO),
    /**
     * If the command did not execute successfully due to an unexpected error
     */
    EXIT_ERROR(NotificationType.ERROR),
    /**
     * If the sender provided invalid syntax
     */
    EXIT_INVALID_SYNTAX(NotificationType.ERROR),
    /**
     * If the sender did not have permission to execute the command
     */
    EXIT_PERMISSION_DENIED(NotificationType.ERROR),
    /**
     * If the sender is of an invalid type (prefer EXIT_MUST_BE_PLAYER when
     * possible)
     */
    EXIT_BAD_SENDER(NotificationType.ERROR),
    /**
     * If the sender specifically must be a player
     */
    EXIT_MUST_BE_PLAYER(NotificationType.ERROR),
    /**
     * If the sender provided valid syntax but is not in a valid state (e.g. running
     * a sell command while empty-handed)
     */
    EXIT_INVALID_STATE(NotificationType.ERROR),
    /**
     * If the command has a cooldown period and the sender is performing the command
     * too fast
     */
    EXIT_COOLDOWN(NotificationType.QUIET),
    /**
     * If the command did not execute successfully, but the error was expected and
     * handled cleanly
     */
    EXIT_EXPECTED_ERROR(NotificationType.ERROR),
    /**
     * If the command failed, but the command will handle the error message itself.
     * Although there is no difference between EXIT_SUCCESS and EXIT_ERROR_SILENT,
     * prefer using this exit code when possible for clearer code
     */
    EXIT_ERROR_SILENT(NotificationType.ERROR);

    @Getter
    private final NotificationType type;
    ExitCode(NotificationType type){
        this.type = type;
    }



}
