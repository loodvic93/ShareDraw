package umlv.fr.sharedraw.actions;

import android.os.Parcelable;

public interface Action extends Parcelable {
    String getAuthor();
    String getMessage();
    long getTimestamp();
    int getIdMessage();
}
