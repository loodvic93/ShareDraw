package umlv.fr.sharedraw.notifier;

import umlv.fr.sharedraw.drawer.tools.Brush;

public interface NotifyDraw {
    void notifyOnDraw(Brush brush);

    void notifyNewDraw(Brush brush);
}
