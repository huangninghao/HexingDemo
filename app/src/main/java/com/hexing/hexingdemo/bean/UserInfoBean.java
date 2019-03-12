package com.hexing.hexingdemo.bean;

import android.annotation.SuppressLint;


/**
 * @author caibinglong
 *         date 2018/4/4.
 *         desc desc
 */

@SuppressLint("ParcelCreator")
public class UserInfoBean {
    public String name;
    public String password;

    private UserInfoBean(Builder builder) {
        name = builder.name;
        password = builder.password;
    }

    public UserInfoBean() {
    }


    @Override
    public String toString() {
        return super.toString();
    }


    public static final class Builder {
        private String name;
        private String password;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder password(String val) {
            password = val;
            return this;
        }

        public UserInfoBean build() {
            return new UserInfoBean(this);
        }
    }
}
