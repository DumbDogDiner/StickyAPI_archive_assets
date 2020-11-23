/**
 * Copyright (c) 2020 DumbDogDiner <a href="dumbdogdiner.com">&lt;dumbdogdiner.com&gt;</a>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.common.util.commonmarkextensions;

import com.dumbdogdiner.stickyapi.annotation.Untested;
import org.commonmark.Extension;
import org.commonmark.parser.Parser;
@Untested
public class MCColorFormatExtension implements Parser.ParserExtension {
    private MCColorFormatExtension(){}

    public static Extension create(){
        return new MCColorFormatExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new MCColorFormatBlockParser.Factory());
    }
}
