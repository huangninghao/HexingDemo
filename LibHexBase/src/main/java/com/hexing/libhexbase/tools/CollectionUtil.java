package com.hexing.libhexbase.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author by HEC271
 *         on 2018/1/26.
 */

public class CollectionUtil {
    /**
     * 列表是否为空
     *
     * @param list 集合
     * @return bool
     */
    public static boolean isEmpty(Collection list) {
        return list == null || list.size() == 0;
    }

    /**
     * 去除list重复数据
     *
     * @param list 集合
     */
    @SuppressWarnings("unchecked")
    public static void removeDuplicate(ArrayList<String> list) {
        HashSet h = new HashSet(list);
        list.clear();
        list.addAll(h);
        Collections.reverse(list);
    }
}
