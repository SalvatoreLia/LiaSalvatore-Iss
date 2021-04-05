package it.unibo.robotWithActorJava;

import it.unibo.supports2021.ActorBasicJava;
import it.unibo.supports2021.IssWsHttpJavaSupport;
import org.json.JSONObject;

import java.util.Random;

public class BoundaryWalkerActor extends ActorBasicJava {
    final String forwardMsg   = "{\"robotmove\":\"moveForward\", \"time\": 350}";
    final String backwardMsg  = "{\"robotmove\":\"moveBackward\", \"time\": 350}";
    final String turnLeftMsg  = "{\"robotmove\":\"turnLeft\", \"time\": 300}";
    final String turnRightMsg = "{\"robotmove\":\"turnRight\", \"time\": 300}";
    final String haltMsg      = "{\"robotmove\":\"alarm\", \"time\": 100}";

    private boolean firstTime=true;
    private boolean wait = true;

    private enum State {start, walking, obstacle, end, stop };
    private IssWsHttpJavaSupport support;
    private State curState       =  State.start ;
    private int stepNum          = 1;
    private RobotMovesInfo moves = new RobotMovesInfo(true);

    public BoundaryWalkerActor(String name, IssWsHttpJavaSupport support) {
        super(name);
        this.support = support;
    }
/*
//Removed since we want use just the fsm, without any 'external' code
    public void reset(){
        System.out.println("RobotBoundaryLogic | FINAL MAP:"  );
        moves.showRobotMovesRepresentation();
        stepNum        = 1;
        curState       =  State.start;
        moves.getMovesRepresentationAndClean();
        moves.showRobotMovesRepresentation();
    }
*/

    protected void fsm(String move, String endmove){
        System.out.println( myname + " | fsm state=" + curState + " stepNum=" + stepNum + " move=" + move + " endmove=" + endmove);
        wait=true;
        /*Random random = new Random();
        switch (curState){
            case start:
                funzione(random.nextInt(4));
                curState = State.walking;
                break;
            case walking:
                if(endmove.equals("false")){
                    curState = State.obstacle;
                    turnDen(moves.getJourney());
                }else {
                    funzione(random.nextInt(4));
                }
                break;
            case obstacle:
                turnDen(moves.getJourney());
                break;

        }*/
        switch( curState ) {
            case start: {
                moves.showRobotMovesRepresentation();
                doStep();
                curState = State.walking;
                break;
            }
            case stop: {
                System.out.println("STOP SISTEMA");
                break;
            }
            case walking: {
                if (move.equals("moveForward") && endmove.equals("true")) {
                    //curState = State.walk;
                    moves.updateMovesRep("w");
                    doStep();
                 } else if (move.equals("moveForward") && endmove.equals("false")) {
                    curState = State.obstacle;
                    turnLeft();
                } else {System.out.println("IGNORE answer of turnLeft");
                }
                break;
            }//walk

            case obstacle :
                moves.updateMovesRep("l");
                turnDen(moves.getJourney());

                /*if( move.equals("turnLeft") && endmove.equals("true")) {
                    if( stepNum < 4) {
                        stepNum++;

                        System.out.println("MOSSE" + moves.getJourney());
                        moves.showRobotMovesRepresentation();
                        curState = State.walking;
                        doStep();
                    }else{  //at home again
                        curState = State.end;
                        turnLeft(); //to force state transition
                    }
                }*/ break;

            case end : {
                if( move.equals("turnLeft") ) {
                    System.out.println("BOUNDARY WALK END");
                    moves.showRobotMovesRepresentation();
                    turnRight();    //to compensate last turnLeft
                }else{
                    //reset();
                    //stepNum        = 1;
                    //curState       =  State.start;
                    //moves.getMovesRepresentationAndClean();
                    moves.showRobotMovesRepresentation();
                    System.exit(0);
                }
                break;
            }
            default: {
                System.out.println("error - curState = " + curState);
            }
        }
    }

    private void turnDen(String move){
        String mosse = "" ;
        if(firstTime){
            mosse = move;
            firstTime=false;
        }
        for(int i=mosse.length()-1; i>=0; i--){
            System.out.println("TORNO INDIETRO " + mosse.charAt(i));
            switch (mosse.charAt(i)){
                case ('l'):
                    turnRight();
                    break;
                case('r'):
                    turnLeft();
                    break;
                case('w'):
                    goBack();
                    break;
                case('s'):
                    doStep();
                    break;
                default:
                    break;
            }
        }
        curState = State.end;
    }

    private char funzione(int n){
        char ris= 'a';
        switch (n){
            case(0):
                moves.updateMovesRep("w");
                doStep();
                ris = 'w';
                break;
            case(1):
                moves.updateMovesRep("s");
                goBack();
                ris = 's';
                break;
            case(2):
                moves.updateMovesRep("l");
                turnLeft();
                ris = 'l';
                break;
            case(3):
                moves.updateMovesRep("r");
                turnRight();
                ris = 'r';
                break;
            default:
                break;
        }
        return ris;
    }

    @Override
    protected void handleInput(String msg ) {     //called when a msg is in the queue
        //System.out.println( name + " | input=" + msgJsonStr);
        if( msg.equals("startApp"))  fsm("","");
        else msgDriven( new JSONObject(msg) );
    }

    protected void msgDriven( JSONObject infoJson){
        if( infoJson.has("endmove") )        fsm(infoJson.getString("move"), infoJson.getString("endmove"));
        else if( infoJson.has("sonarName") ) handleSonar(infoJson);
        else if( infoJson.has("collision") ) handleCollision(infoJson);
        else if( infoJson.has("robotcmd") )  handleRobotCmd(infoJson);
    }

    protected void handleSonar( JSONObject sonarinfo ){
        //curState = State.stop;
        if(wait){
            delay(2000);
            wait=false;
        }
        //curState = State.walking;
        //doStep();
        String sonarname = (String)  sonarinfo.get("sonarName");
        int distance     = (Integer) sonarinfo.get("distance");
        System.out.println("RobotApplication | handleSonar:" + sonarname + " distance=" + distance);
    }
    protected void handleCollision( JSONObject collisioninfo ){
        //we should handle a collision  when there are moving obstacles
        //in this case we could have a collision even if the robot does not move
        //String move   = (String) collisioninfo.get("move");
        //System.out.println("RobotApplication | handleCollision move=" + move  );
    }
  
    protected void handleRobotCmd( JSONObject robotCmd ){
        String cmd = (String)  robotCmd.get("robotcmd");
        if (cmd.equals("STOP")){
            curState = State.stop;
        } else if(cmd.equals("RESUME")){
            curState = State.walking;
            doStep();
        }
        System.out.println("===================================================="    );
        System.out.println("RobotApplication | handleRobotCmd cmd=" + cmd  );
        System.out.println("===================================================="    );
    }

    //------------------------------------------------
    protected void doStep(){
        support.forward( forwardMsg);
        delay(1000); //to avoid too-rapid movement
    }
    protected void turnLeft(){
        support.forward( turnLeftMsg );
        delay(500); //to avoid too-rapid movement
    }
    protected void turnRight(){
        support.forward( turnRightMsg );
        delay(500); //to avoid too-rapid movement
    }
    protected void goBack(){
        support.forward( backwardMsg);
        delay(1000); //to avoid too-rapid movement
    }
}
