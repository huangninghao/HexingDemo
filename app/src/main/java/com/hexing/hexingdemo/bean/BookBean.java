package com.hexing.hexingdemo.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author caibinglong
 *         date 2018/6/14.
 *         desc desc
 */

public class BookBean implements Parcelable {
    private String name;
    private String price;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.price);
    }

    public BookBean() {
    }

    protected BookBean(Parcel in) {
        this.name = in.readString();
        this.price = in.readString();
    }

    public static final Parcelable.Creator<BookBean> CREATOR = new Parcelable.Creator<BookBean>() {
        @Override
        public BookBean createFromParcel(Parcel source) {
            return new BookBean(source);
        }

        @Override
        public BookBean[] newArray(int size) {
            return new BookBean[size];
        }
    };
}
