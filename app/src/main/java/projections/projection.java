package projections;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.content.ComponentName;
import android.content.ServiceConnection;

import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import projections.Actions.Action;
import projections.Actions.MonitorAction;
import projections.Actions.compositeAction;


public abstract class projection extends BroadcastReceiver {


    protected ProjectionType Type;

    protected String ProjectionName;

    public Context context;

    protected MonitorAction condAction;
    protected boolean hasAlarm;
    protected compositeAction action;
    protected compositeAction actionToTrigger;
    protected boolean mIsBound = false;
    protected Utils.ExecuteMode mode;

    public enum ProjectionType {
        Cyclic, Monitor
    }

    public enum ProjectionTimeUnit {
        Second, Minute, Hour, Day, Week, Month, None
    }

    protected void receiveData(Intent intent) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:sszzz");
        String concept = intent.getStringExtra("concept");
        String val = String.valueOf(intent.getStringExtra("value"));
        Log.i("geting value ", "get value of : " + val);
        String time = String.valueOf(intent.getStringExtra("time"));

        try {
            Date dateNow = sdf.parse(time);

            if (condAction != null) {
                boolean okToInsert = condAction.isSatisfyVarsConditions(val);
                if (okToInsert)
                    this.condAction.insertData(concept, val, dateNow);

                //check if the value constraints+ time constraints is happening after Receiving
                // the last data
                if (condAction.isNeedToTrigger()) {
                    Log.i("Action On recive", "trigger the conditin trigger ");
                    triggerEvent();
                }

            }

        } catch (ParseException e) {
            Log.e("Action", "error parsing date in onReceive");
        }


    }

    public  String getProjectionName()
    {
        return ProjectionName;
    }

    public  ProjectionType getType()
    {
        return Type;
    }


    public void  setExectuionMode(Utils.ExecuteMode executeMode)
    {
        mode=executeMode;
        if(executeMode.equals(Utils.ExecuteMode.Parallel))
            action.setExecuteMode(mode);
    }

    public void initMonitoringEvents() {
        condAction = new MonitorAction(context);
    }


    public void initTriggerActions(Utils.ExecuteMode mode) {
        actionToTrigger = new compositeAction(context, mode);
    }

    public void addActionToTrigger(Action a)
    {
        if (actionToTrigger!=null)
        {
            actionToTrigger.addAction(a);
        }
    }
    public void addAction(Action a)
    {
        //TODO:
       action.addAction(a);
    }


    public projection(ProjectionType type,String _ProjectionName,Context _context)
    {
        Type=type;
        ProjectionName=_ProjectionName;
        context =_context;
        action=new compositeAction(context, Utils.ExecuteMode.Sequential);
        mode= Utils.ExecuteMode.Sequential;
        hasAlarm=false;
        condAction=null;

    }

    public abstract  void registerToTriggring();


    public boolean Isbound()
    {
        return mIsBound;
    }

	
	public abstract void doAction();


    public   void StopProjection() {



    }
    public void startAlarm()
    {
        return;
    }

    public void defVar(String varName,String concept,var.VarType type)
    {
        if(this.condAction!=null)
            this.condAction.defineVar(varName,concept, type);
    }
    public void addValueConstraint(String varName, var.Operators op, String val)
    {
        if(this.condAction!=null)
            this.condAction.addValueConstraint(varName,op,val);
    }

    public void setTimeConstraint( int daysAgo)
    {
        if(this.condAction!=null)
            this.condAction.setTimeConstraint(daysAgo);

    }
    public void setOpBetweenValueConstraints(String varName,var.OperationBetweenConstraint op)
    {
        if(this.condAction!=null)
            this.condAction.setOpBetweenValueConstraints(varName,op);

    }
    public void setAggregationConstraint(Action.AggregationAction action, Action.AggregationOperators op,int targetVal)
    {
        if(this.condAction!=null)
            this.condAction.setAggregationConstraint(action,op,targetVal);

    }
    public void setOpBetweenVars(var.OperationBetweenConstraint op)
    {
        if(this.condAction!=null)
            this.condAction.setOpBetweenVars(op);

    }

    public   void startProjection() {
        initProjection();
        //TODO : start the compositeActin serivice

        if(hasAlarm)
            this.startAlarm();


    }

    public  abstract void initProjection();

    public void InvokeAction(boolean isReminder) {

        }
    protected void triggerEvent() {
        if(actionToTrigger!=null)
        actionToTrigger.invoke(false);
        else
            Log.i("projection ", "the action to trigger is null");
    }

}
