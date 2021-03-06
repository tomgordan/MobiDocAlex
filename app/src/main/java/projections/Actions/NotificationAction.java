package projections.Actions;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

/**
 * Action for the  Notification operation
 *  in the Call method it return a msg to be sent to the Gui
 */
public class NotificationAction extends Action {

    Actor _actor;

    public NotificationAction(String notificationTxt, String concept, Actor actor) {
        super(ActionType.Notification, notificationTxt, concept);

        _actor=actor;
    }

    @Override
    public  void setOnReceiveConcept(String compositeActionName, String concept)
    {

    }

    @Override
    public Message call() throws Exception {

        if(_actor.equals(Actor.Patient)) {
            Log.i("start building notification", "start building notification");

            msgToSend = actionName;
            int msgType = type.ordinal() + 1;

            Message msg = Message.obtain(null, msgType, 0, 0, 0);
            Bundle bundle = new Bundle();

            bundle.putString("value", msgToSend);

            Log.i("notification action- build msg", "build msg for notification ");

            msg.setData(bundle);
            return msg;
        }
        return null;
    }


}
