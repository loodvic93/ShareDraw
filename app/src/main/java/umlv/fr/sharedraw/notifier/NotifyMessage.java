package umlv.fr.sharedraw.notifier;

import umlv.fr.sharedraw.actions.Say;

public interface NotifyMessage {
    void notifyMessageReceive(Say say);
}
