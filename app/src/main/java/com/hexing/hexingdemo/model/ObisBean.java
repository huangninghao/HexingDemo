package com.hexing.hexingdemo.model;

import java.util.List;

/**
 * @author caibinglong
 *         date 2019/3/8.
 *         desc desc
 */

public class ObisBean {
    public String name;
    public String classid;
    public String functionid;
    public String obis;
    public List<Attribute> attribute;

    public static class Attribute {
        public String name;
        public String id;
        public String accessright;
        public List<Item> item;

        public static class Item {
            public String name;
            public String format;
            public String remark;
        }
    }
}
