package umlv.fr.sharedraw;

import android.os.Parcel;
import android.os.Parcelable;

public class Dashboard implements Parcelable {
    private final String name;
    private final String color;

    public Dashboard(String name, String color) {
        this.name = name;
        this.color = color;
    }

    private Dashboard(Parcel in) {
        name = in.readString();
        color = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Dashboard createFromParcel(Parcel in) {
            return new Dashboard(in);
        }

        @Override
        public Object[] newArray(int size) {
            return null;
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dashboard dashboard = (Dashboard) o;

        return name != null ? name.equals(dashboard.name) : dashboard.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(color);
    }
}