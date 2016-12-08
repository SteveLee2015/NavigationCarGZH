package com.novsky.map.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by llg on 2016/11/28.
 */

public class CollectionUtils {

    /**
     * 去除重复元素
     * @param list
     * @return
     */
    public static List removeDuplicate(List list)

    {
        Set set = new LinkedHashSet();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }
}
