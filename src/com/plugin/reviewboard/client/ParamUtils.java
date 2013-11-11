package com.plugin.reviewboard.client;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Say it.
 *
 * @author george
 * @since 11/8/13 4:19 PM
 */

public class ParamUtils {
    public static <T> String join(List<T> list, Function<T, String> func) {
        if (null == list || list.isEmpty()) {
            return "";
        }
        List<String> values = Lists.transform(list, func);
        return Joiner.on(",").join(values);
    }
}
