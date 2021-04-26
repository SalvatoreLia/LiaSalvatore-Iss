package demoWithRobot

import it.unibo.actor0.ActorBasicKotlin
import it.unibo.actor0.ApplMessage
import it.unibo.actor0.sysUtil
import it.unibo.robotService.ApplMsgs
import it.unibo.robotService.BasicStepRobotActor
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import kotlin.system.exitProcess

class BoundaryWalkRobotActorCaller(name: String ) : ActorBasicKotlin( name ) {

    private val robot = BasicStepRobotActor("stepRobot", ownerActor=this, scope, "localhost")
    private var tback = "350"
    private var count = 0

    protected fun doMoves() {
        robot.registerActor(this)
        //robot.send(ApplMsgs.stepRobot_w("main", "800"))
        // robot.send(ApplMsgs.stepRobot_l("main" ))
        //robot.send(ApplMsgs.stepRobot_r("main" ))
        robot.send(ApplMsgs.stepRobot_step("main", "350"))

/*
        robot.send(ApplMsgs.stepRobot_l("main" ))
        robot.send(ApplMsgs.stepRobot_l("main" ))
        robot.send(ApplMsgs.stepRobot_step("main", "350"))
        robot.send(ApplMsgs.stepRobot_l("main" ))
        robot.send(ApplMsgs.stepRobot_l("main" ))
*/
    }

    override suspend fun handleInput(msg: ApplMessage) {
        if(count>=4)
            return
        println("$name | handleInput $msg")
        if (msg.msgId == "start") {
            doMoves()
        }
        if (msg.msgId == "stepAnswer") {
            val answerJson = JSONObject(msg.msgContent)
            if (answerJson.has("stepDone")) {
                robot.send(ApplMsgs.stepRobot_step("main", "350"))
            }else if (answerJson.has("stepFail")){
                tback = answerJson.getString("stepFail")
                println("$name | handleInput stepFail turn left $tback")
                robot.send(ApplMsgs.stepRobot_l("main"))
            }
        }
        if (msg.msgId == "endmove") {
            val answerJson = JSONObject(msg.msgContent)  //.replace("@", ","))
            if (answerJson.has("endmove")) {//&& answerJson.getString("endmove") == "notallowed"){
                val endmove = answerJson.getString("endmove")
                val move = answerJson.getString("move")
                println("endmove=${endmove} move=$move")
                /*if(move.equals("moveForward") && endmove.equals("notallowed")){
                    robot.send(ApplMsgs.stepRobot_step("main", tback))
                }*/
                if(move.equals("turnLeft") && endmove.equals("notallowed")){
                    println("LEFT NOT ALLOWED, faccio LEFT")
                    robot.send(ApplMsgs.stepRobot_l("main"))

                }
                else {
                    println("TBACK $tback")
                    robot.send(ApplMsgs.stepRobot_step("main", tback))
                }
                if(move.equals("turnLeft") && endmove.equals("true")){
                    count++
                }
            }
        }

    }
}

fun main( ) {
    println("BEGINS CPU=${sysUtil.cpus} ${sysUtil.curThread()}")
    runBlocking {
        val rc = BoundaryWalkRobotActorCaller("rc")

        rc.send(ApplMsgs.startAny("main"))
        println("ENDS runBlocking ${sysUtil.curThread()}")
    }
    println("ENDS main ${sysUtil.curThread()}")
}
