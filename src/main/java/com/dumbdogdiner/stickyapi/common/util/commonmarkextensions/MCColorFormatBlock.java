/**
 * Copyright (c) 2020 DumbDogDiner <a href="dumbdogdiner.com">&lt;dumbdogdiner.com&gt;</a>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.common.util.commonmarkextensions;

import com.dumbdogdiner.stickyapi.annotation.Untested;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.commonmark.node.CustomBlock;

@Untested
public class MCColorFormatBlock extends CustomBlock {
    @Getter
    private ChatColor type;

    public MCColorFormatBlock(ChatColor type) {
        this.type = type;
    }
}
