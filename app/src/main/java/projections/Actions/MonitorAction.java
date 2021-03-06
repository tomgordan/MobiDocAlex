package projections.Actions;


import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Vector;

import projections.monitoringObjects.DataCollection;
import projections.monitoringObjects.valueConstraint;
import projections.var;

import static ch.lambdaj.Lambda.avg;
import static ch.lambdaj.Lambda.count;
import static ch.lambdaj.Lambda.sum;

public class MonitorAction extends  Action {


    protected String ansVal;

    protected String msgToSend;

    public Messenger MessengerToMonitoringService = null;

    public int aggregationTargetVal;
    protected boolean mIsBound = false;
    //=============================
    protected Vector<var> vars;
    protected DataCollection data;

    protected int count;
    protected var.OperationBetweenConstraint betweenVars;
    protected AggregationOperators aggregationOperator;

    private Vector<String> conceptsToMonitor;
    //=============================
    public Intent serviceIntent = null;
    protected AggregationAction aggregationAction;

    protected boolean isReminder;


    public MonitorAction(Context _context) {
        super(ActionType.Trigger, "", "", _context);
        count = 0;

        ansVal = "";
        conceptsToMonitor=new Vector<String>();
        data = new DataCollection( 1);
        vars = new Vector<var>();
        aggregationAction = null;
        aggregationTargetVal = 0;
        aggregationOperator = null;
        betweenVars = var.OperationBetweenConstraint.Or;

        serviceIntent = new Intent(_context, projections.monitoringObjects.MonitoringDBservice.class);
        isReminder=false;

        //===========================


    }
    @Override
    public  void setOnReceiveConcept(String compositeActionName, String concept)
    {

    }

    protected ServiceConnection mconnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MessengerToMonitoringService = new Messenger(service);
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MessengerToMonitoringService = null;

            mIsBound = false;
        }
    };

    public void defineVar(String name, String concept, var.VarType type) {
        var v = null;
        switch (type) {
            case Int:
                v = new var<Integer>(name, concept, type);
                break;
            case String:
                v = new var<String>(name, concept, type);
                break;

            case Char:
                break;
            case Double:
                v = new var<Double>(name, concept, type);
                break;
            case Null:
                break;
        }
        vars.add(v);


        //TODO: SUBSCRIBE in the projection for this concept
        //add the concept to the monitoring comcepts
        addConceptToMonitor(concept);
    }
    public void setOpBetweenValueConstraints(String varName, var.OperationBetweenConstraint op) {
        var v = getVar(varName);
        v.setOpBetweenValueConstraints(op);
    }

    public void setAggregationAction(AggregationAction action, AggregationOperators op, int targetVal) {
        aggregationAction = action;
        aggregationOperator = op;
        aggregationTargetVal = targetVal;
    }
    public void insertData(String concept,String val,Date dateNow)
    {
        data.insertItem(concept, val, dateNow);
    }
    public var getVar(String name) {
        for (int i = 0; i < vars.size(); i++) {
            var v = vars.get(i);
            if (v.getName().equals(name))
                return v;
        }
        return null;

    }

    public Vector<String>getConceptsToMonitor()
    {
        return conceptsToMonitor;
    }
    public void printDataCollections() {
        System.out.println("printong data");
        System.out.println("--------------------------------------");
        for (int i = 0; i < data.getDataItems().size(); i++)
            System.out.println("data in " + i + " is " + data.getDataItems().get(i).getVal());
    }

    public boolean isSatisfyVarsConditions(String val) {
        if (betweenVars.equals(var.OperationBetweenConstraint.And)) {
            boolean ans = true;
            for (var v : vars) {
                ans = ans && v.isSatisfyVar(val);
            }
            return ans;
        } else {
            boolean ans = false;
            for (var v : vars) {
                ans = ans || v.isSatisfyVar(val);
            }
            return ans;
        }

    }

    public boolean isSatisfyAggregationConstraint(Iterable data) {

        int ans = AggregationFunc(data);
        System.out.println("the func  is : " + ans);
        return (ans >= aggregationTargetVal);

    }

    public int AggregationFunc(Iterable data) {
        switch (aggregationAction) {
            case Sum:
                return sum(data).intValue();

            case Avg:
                return avg(data).intValue();

            case Count:

                return count(data).size();

        }
        return -1;
    }

    public boolean isNeedToTrigger() {
        boolean ans = false;
        if (vars.size() > 0 || data.hasValueConstraints()) {
            Iterable it = data.getDataValues();

            boolean needToTrigger = isSatisfyAggregationConstraint(it);
            if (needToTrigger) {
                ans = true;
                Log.i("Action", "the Conditions  is happend -> apply the triggering");
            }
        }
        return ans;
    }


    public void addValueConstraint(String varName, var.Operators op, String val) {
        var v = getVar(varName);
        String concept=v.getConcept();
        valueConstraint valc = new valueConstraint(concept, op, val);
        data.addValueConstraint(concept,valc);

        v.addValueConstraint(concept, op, val);

    }

    public void setTimeConstraint(int daysAgo) {
        data.setTimeConstraint(daysAgo);

    }



    public void startMonitoring() {

        if (!mIsBound) {

            context.bindService(serviceIntent, mconnection, Context.BIND_AUTO_CREATE);
            this.mIsBound = true;

        }
    }

    public void setAggregationConstraint(AggregationAction action, AggregationOperators op, int targetVal) {
        if (vars.size() > 0)
            setAggregationAction(action, op, targetVal);
    }

    //By default the Operation between Vars is OR
    public void setOpBetweenVars(var.OperationBetweenConstraint op) {
        betweenVars = op;
    }


    /*=============================================
       //TODO: NOT SURE IF NEEDED - NO ONE CALL FOR THIS FUNCTION
     *===========================================*/
    public void SubscribeConcept(String concept) {

        IntentFilter intentFilter = new IntentFilter(concept);

       //todo: context.registerReceiver(this, intentFilter);
        startMonitoring();


    }

    public void UnSubscribeConcept(String concept) {
       // context.unregisterReceiver(this);

        ///STOP MONITORING SERVICE
        if (mIsBound) {
            mconnection = null;
            mIsBound = false;
            context.stopService(serviceIntent);
            Toast.makeText(context, "service succefully stopped", Toast.LENGTH_LONG).show();
        }
    }


    public void addConceptToMonitor( String conceptId)
    {
        if(!conceptsToMonitor.contains(conceptId))
       conceptsToMonitor.add(conceptId);
      // SubscribeConcept(conceptId);
    }
    //TODO: when ok, need to triggring the projecction


    @Override
    public Message call() throws Exception {
        return null;
    }
}
