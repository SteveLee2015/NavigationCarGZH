package com.novsky.map.util;

import com.novsky.map.main.BDPoint;

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


    public static List<BDPoint> removeDuplicateT(List<BDPoint> list)

    {
        Set set = new LinkedHashSet();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }

    public static List<String> removeDuplicateStr(List<String> list)

    {
        Set set = new LinkedHashSet();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }
}
