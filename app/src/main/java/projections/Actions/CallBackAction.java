package projections.Actions;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

/**
 * Action for the callback operation
 *  in the Call method it return a msg to be sent to the Gui
 */
public class CallBackAction extends Action {
    public CallBackAction(String name, String concept) {
        super(ActionType.CallBack, name, concept);
    }



    @Override
    public  void setOnReceiveConcept(String compositeActionName, String concept)
    {

    }
    @Override
    public Message call() throws Exception {

        Log.i("start building CallBack", "start building callback");

        msgToSend = actionName;
        int msgType = type.ordinal() + 1;

        Message msg = Message.obtain(null, msgType, 0, 0, 0);
        Bundle bundle = new Bundle();

        bundle.putString("value", msgToSend);

        bundle.putString("concept", getConcept());

        Log.i("callback action- build msg", "build msg for callback ");

        msg.setData(bundle);
        return msg;
    }
}

