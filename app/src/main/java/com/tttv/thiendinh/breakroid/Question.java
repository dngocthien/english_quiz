package com.tttv.thiendinh.breakroid;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Question implements Parcelable {

    private String id, q, a, b, c, d;
    private int t;

    public Question(){}

    public Question(String id, String q, String a, String b, String c, String d, int t) {
        this.id = id;
        this.q = q;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.t = t;
    }

    public Question(Parcel in) {
        id = in.readString();
        q = in.readString();
        a = in.readString();
        b = in.readString();
        c = in.readString();
        d = in.readString();
        t = in.readInt();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(q);
        dest.writeString(a);
        dest.writeString(b);
        dest.writeString(c);
        dest.writeString(d);
        dest.writeInt(t);
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };
}
