/**
 *  ****************  Presence Central  ****************
 *
 *  Credits: I have to credit Brian Gudauskas (@bridaus -Reliable Presence) & Eric Roberts (@baldeagle072 - Everyones Presence) for stealing some of their code for multiple presence sensor determinations
 *
 *
 *  Design Usage:
 *  This is the 'Child' app for presence automation
 *
 *
 *  Copyright 2017 Andrew Parker
 *  
 *  This SmartApp is free!
 *  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://www.paypal.me/smartcobra
 *  
 *
 *  I'm very happy for you to use this app without a donation, but if you find it useful then it would be nice to get a 'shout out' on the forum! -  @Cobra
 *  Have an idea to make this app better?  - Please let me know :)
 *
 *  Website: http://securendpoint.com/smartthings
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @Cobra
 *
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  Last Update: 22/10/2017
 *
 *  Changes:
 *
 * 
 *
 *  
 *  
 *  V1.0.0 - POC
 *
 */

 
definition(
    name: "Presence_Central_Child",
    namespace: "Cobra",
    author: "Andrew Parker",
    description: "Child App for Presence Automation",
     category: "Fun & Social",

   
    
    parent: "Cobra:Presence Central",
    iconUrl: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/presence.png",
    iconX2Url: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/presence.png",
    iconX3Url: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/presence.png"
    )
    
    
    preferences {
    
    page name: "mainPage", title: "", install: false, uninstall: true, nextPage: "actionPage"
    page name: "actionPage", title: "", install: false, uninstall: true, nextPage: "finalPage"
    page name: "finalPage", title: "", install: true, uninstall: true
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    initialize()
}
def initialize() {
 log.info "Initialised with settings: ${settings}"
      setAppVersion()
      logCheck()
      state.appgo = true
      state.timer1 = true


// Basic Subscriptions    

	subscribe(enableSwitch, "switch", switchEnable)
    
    
// Trigger subscriptions

		if(trigger == "Single Presence Sensor"){
     	LOGDEBUG( "Trigger is $trigger")
		subscribe(presenceSensor1, "presence", singlePresenceHandler) 
    }
		else if(trigger == "Group 1 \r\n(Anyone arrives or leaves = changed presence)"){
		LOGDEBUG( "Trigger is $trigger")
        setPresence1()
		subscribe(presenceSensor2, "presence", group1Handler) 
        
    }    
		else if(trigger == "Group 2 \r\n('Present' if anyone is at home)"){
		LOGDEBUG( "Trigger is $trigger")
        setPresence2()
		subscribe(presenceSensor3, "presence", group2Handler) 
        
    }


}






// main page *************************************************************************
def mainPage() {
    dynamicPage(name: "mainPage") {
      
        section {
        paragraph image: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/presence.png",
                  title: "Message Control Child",
                  required: false,
                  "This child app allows you to define different actions upon arrival or departure of one or more presence sensors"
                  }
     section() {
   
        paragraph image: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/cobra3.png",
                         "Child Version: $state.appversion - Copyright © 2017 Cobra"
    }             
      section() {
        	basicInputs()
          	  	}
        
  }
}




// action page *************************************************************************
def actionPage() {
    dynamicPage(name: "actionPage") {

 section() {
 
		triggerInput()
		presenceActions()
        outputActions()
        
	}
}
}


// name page *************************************************************************
def finalPage() {
       dynamicPage(name: "finalPage") {
       
            section("Automation name") {
                label title: "Enter a name for this message automation", required: false
            }
             section("Modes") {
           		mode title: "Set for specific mode(s)", required: false
            }
             section("Logging") {
            input "debugMode", "bool", title: "Enable logging", required: true, defaultValue: false
  	        }
      }  
    }




def basicInputs(){
	input "enableSwitch", "capability.switch", title: "Select a switch Enable/Disable this automation (Optional)", required: false, multiple: false 
	input "fromTime", "time", title: "Allow actions from", required: true
    input "toTime", "time", title: "Allow actions until", required: true 
    input "days", "enum", title: "Select Days of the Week", required: true, multiple: true, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
   
}

def triggerInput() {
   input "trigger", "enum", title: "How to trigger actions?",required: true, submitOnChange: true, options: ["Single Presence Sensor", "Group 1 \r\n(Anyone arrives or leaves = changed presence)", "Group 2 \r\n('Present' if anyone is at home)"]
  
}

def presenceActions(){
		if (trigger) {
		state.selection1 = trigger
    
	if(state.selection1 == "Single Presence Sensor"){
	input "presenceSensor1", "capability.presenceSensor", title: "Select presence sensor to trigger action", required: false, multiple: false 
    }
    
	else if(state.selection1 == "Group 1 \r\n(Anyone arrives or leaves = changed presence)"){
	input "presenceSensor2", "capability.presenceSensor", title: "Select presence sensors to trigger action", multiple: true, required: false
  	}
    
	else if(state.selection1 == "Group 2 \r\n('Present' if anyone is at home)"){
	input "presenceSensor3", "capability.presenceSensor", title: "Select presence sensors to trigger action", multiple: true, required: false
    }
    
    
 }
}
    





def outputActions(){
input "presenceAction", "enum", title: "What action to take?",required: true, submitOnChange: true, options: ["Control A Switch", "Speak A Message", "Send A Message", "Change Mode", "Run a Routine"]

if (presenceAction) {
    state.selection2 = presenceAction
    
    if(state.selection2 == "Control A Switch"){
     input "switch1", "capability.switch", title: "Select switch(s) to turn on/off", required: false, multiple: true 
     input "presenceSensor1Action1", "bool", title: "\r\n \r\n On = Switch On when someone arrives (Off when they leave) \r\n Off = Switch Off when someone arrives (On when they leave) ", required: true, defaultValue: true  
    }
    
    
   else if(state.selection2 == "Speak A Message"){ 
   input "speaker", "capability.musicPlayer", title: "Choose speaker(s)", required: false, multiple: true
	input "volume1", "number", title: "Normal Speaker volume", description: "0-100%", defaultValue: "100",  required: true
    input "message1", "text", title: "Message to play when sensor arrives",  required: false
	input "message2", "text", title: "Message to play when sensor leaves",  required: false
    input "msgDelay", "number", title: "Minutes delay between messages (Enter 0 for no delay)", defaultValue: '0', description: "Minutes", required: true
	input "volume2", "number", title: "Quiet Time Speaker volume", description: "0-100%", defaultValue: "0",  required: true
    input "fromTime2", "time", title: "Quiet Time Start", required: false
    input "toTime2", "time", title: "Quiet Time End", required: false
	}
    
    
     else if(state.selection2 == "Send A Message"){
     input "message1", "text", title: "Message to send when sensor arrives",  required: false
	 input "message2", "text", title: "Message to send when sensor leaves",  required: false
     input("recipients", "contact", title: "Send notifications to") {
     input(name: "sms", type: "phone", title: "Send A Text To", description: null, required: false)
     input(name: "pushNotification", type: "bool", title: "Send a push notification", description: null, defaultValue: true)
     
     }
     }
    
    else if(state.selection2 == "Change Mode"){
    input "newMode1", "mode", title: "Change to this mode when someone arrives"
    input "newMode2", "mode", title: "Change to this mode when someone leaves"
    
    }
    
     else if(state.selection2 == "Run a Routine"){
      def actions = location.helloHome?.getPhrases()*.label
            if (actions) {
            input "routine1", "enum", title: "Select a routine to execute when someone arrives", options: actions
            input "routine2", "enum", title: "Select a routine to execute when someone leaves", options: actions
                    }
            }
    
    
    
    
    
    
	}
}

// ************************ Handlers ****************************************
 

def singlePresenceHandler(evt){
state.privatePresence = evt.value
if (state.privatePresence == "present"){
arrivalAction()
}
if (state.privatePresence == "not present"){
departureAction()
}
}


// Group 1  ======================================================================
def group1Handler(evt) {

    if (evt.value == "present") {
        if (state.privatePresence1 != "present") {
            state.privatePresence1 = "present"
            state.privatePresence = "present"
           log.debug("A sensor arrived so setting group to '$state.privatePresence'")
           arrivalAction ()
            }
    } else if (evt.value == "not present") {
        if (state.privatePresence1 != "not present") {
            state.privatePresence1 = "not present"
            state.privatePresence = "not present"
            log.debug("A sensor left so setting group to '$state.privatePresence'")
            departureAction ()
        }
    }
}


// end group 1 ========================================================


// Group 2 ============================================================

def group2Handler(evt) {
	setPresence2()
}


// end group 2 ========================================================

// end handlers *************************************************************



// ************************* Actions ****************************************


// Arrival Actions - Check OK to run
def arrivalAction(){
LOGDEBUG("Calling Arrival Action")
checkTime()
checkDay()
if (state.timeOK == true && state.dayCheck == true){
decideActionArrival()
	}
}


// Departure Actions - Check OK to run
def departureAction(){
LOGDEBUG("Calling Departure Action")
checkTime()
checkDay()
if (state.timeOK == true && state.dayCheck == true){
decideActionDeparture()
	}
}





// Decide which action to call
def decideActionArrival() {
LOGDEBUG("Deciding on correct Arrival Action")

 if(state.selection2 == "Control A Switch"){
  LOGDEBUG("Decided to: 'Control A Switch' ")
  
  def actionType1 = presenceSensor1Action1
 LOGDEBUG("actionType1 = $actionType1") 
  if (actionType1 == true){
   LOGDEBUG("Switching on...")
  switch1.on()
  }
  else  if (actionType1 == false){
  LOGDEBUG("Switching off...")
  switch1.off()
  }
  
  
 }
else if(state.selection2 == "Speak A Message"){
  LOGDEBUG("Decided to: 'Speak A Message' ")
  state.msg1 = message1
	speakNow()
 }
 else if(state.selection2 == "Send A Message"){
 LOGDEBUG("Decided to: 'Send A Message' ")
 def msg = message1
  sendMessage(msg)
 }

 else if(state.selection2 == "Change Mode"){
   LOGDEBUG("Decided to: 'Change Mode'")
 if(newMode1){
 changeMode1()
 }
 }

 else if(state.selection2 == "Run a Routine"){
 LOGDEBUG("Decided to: 'Run a Routine' ") 
 state.routineGo = routine1
 LOGDEBUG("Running routine: $state.routineGo")
 location.helloHome?.execute(state.routineGo)
 
 
 
 }


}

def decideActionDeparture() {
LOGDEBUG("Deciding on correct Departure Action")

 if(state.selection2 == "Control A Switch"){
 LOGDEBUG("Decided to: 'Control A Switch' ")
 
  def actionType1 = presenceSensor1Action1
  LOGDEBUG("actionType1 = $actionType1") 
  if (actionType1 == false){
  LOGDEBUG("Switching on...")
  switch1.on()
  }
  else  if (actionType1 == true){
  LOGDEBUG("Switching off...")
  switch1.off()
  }
 }
else if(state.selection2 == "Speak A Message"){
  LOGDEBUG("Decided to: 'Speak A Message' ")
  state.msg1 = message2
	speakNow()
 }
 else if(state.selection2 == "Send A Message"){
 LOGDEBUG("Decided to: 'Send A Message' ")
   def msg = message2
  sendMessage(msg)
 }

 else if(state.selection2 == "Change Mode"){
  LOGDEBUG("Decided to: 'Change Mode'")
 if(newMode2){
 changeMode2()
 }
 }

 else if(state.selection2 == "Run a Routine"){
 LOGDEBUG("Decided to: 'Run a Routine' ")
   state.routineGo = routine2
 LOGDEBUG("Running routine: $state.routineGo")
 location.helloHome?.execute(state.routineGo)
 
 }
}






// Group 1 Actions ======================================

def setPresence1(){
	def presentCounter1 = 0
    
    presenceSensor2.each {
    	if (it.currentValue("presence") == "present") {
        	presentCounter1++
        }
    }
    
    log.debug("presentCounter1: ${presentCounter1}")
    
    if (presentCounter1 > 0) {
    	if (state.privatePresence1 != "present") {
    		state.privatePresence1 = "present"
            state.privatePresence = "present"
            log.debug("A sensor arrived so setting group to '$state.privatePresence'")
        }
    } else {
    	if (state.privatePresence1 != "not present") {
    		state.privatePresence1 = "not present"
            state.privatePresence = "not present"
            log.debug("A sensor left so setting group to '$state.privatePresence'")
        }
    }
}

// end group 1 actions ==================================

// Group 2 Actions ======================================

def setPresence2(){
def	presentCounter2 = 0
        presenceSensor3.each {
    	if (it.currentValue("presence") == "present") {
        	presentCounter2++
        }
    }
    
    log.debug("Number of sensors present: ${presentCounter2}")
    
    if (presentCounter2 > 0) {
    	if (state.privatePresence2 != "present") {
            state.privatePresence2 = "present"
            state.privatePresence = "present"
            log.debug("Arrived - At least one sensor arrived - set group to '$state.privatePresence'")
             arrivalAction ()
        }
    } else {
    	if (state.privatePresence2 != "not present") {
            state.privatePresence2 = "not present"
            state.privatePresence = "not present"
            log.debug("Departed - Last sensor left - set group to '$state.privatePresence'")
             departureAction ()
        }
    }
}

// end group 2 actions ==================================

// Mode Actions  ======================================

def changeMode1() {
    LOGDEBUG( "changeMode1, location.mode = $location.mode, newMode1 = $newMode1, location.modes = $location.modes")

    if (location.mode != newMode1) {
        if (location.modes?.find{it.name == newMode1}) {
            setLocationMode(newMode1)
        }  else {
            LOGDEBUG( "Tried to change to undefined mode '${newMode1}'")
        }
    }
}
def changeMode2() {
    LOGDEBUG( "changeMode2, location.mode = $location.mode, newMode2 = $newMode2, location.modes = $location.modes")

    if (location.mode != newMode2) {
        if (location.modes?.find{it.name == newMode2}) {
            setLocationMode(newMode2)
        }  else {
            LOGDEBUG( "Tried to change to undefined mode '${newMode2}'")
        }
    }
}

// end mode actions =================================


// Message Actions ==================================


def sendMessage(msg) {
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sms) {
            sendSms(sms, msg)
        }
        if (pushNotification) {
            sendPush(msg)
        }
    }
}

// end message actions ===============================

// Speaking Actions ==================================

def speakNow(){
LOGDEBUG("speakNow called...")
checkVolume()

    if ( state.timer1 == true){
	LOGDEBUG("Speaking now - Message: '$state.msg1'")
	speaker.speak(state.msg1)
   	startTimerSpeak()  
 } 
	else if ( state.timer1 == false){
	LOGDEBUG("NOT Speaking now - Too close to last message so I have to wait a while before I can speak again...")
 }
}

def startTimerSpeak(){
state.timer1 = false
state.timeDelay = 60 * msgDelay
LOGDEBUG("Waiting for $msgDelay minutes before resetting timer to allow further messages")
runIn(state.timeDelay, resetTimerSpeak)
}

def resetTimerSpeak() {
state.timer1 = true
LOGDEBUG( "Timer reset - Messages allowed again...")
}


// end speaking actions ==============================

// end Actions ****************************************************************************



// Check time allowed to run... *******************************

def checkTime(){

def between = timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
    if (between) {
    state.timeOK = true
   LOGDEBUG("Time is ok so can continue...")
    
}
else if (!between) {
state.timeOK = false
LOGDEBUG("Time is NOT ok so cannot continue...")
	}
}

def checkDay(){

 def df = new java.text.SimpleDateFormat("EEEE")
    
    df.setTimeZone(location.timeZone)
    def day = df.format(new Date())
    def dayCheck1 = days.contains(day)
    if (dayCheck1) {

  state.dayCheck = true
LOGDEBUG( " Day ok so can continue...")
 }       
 else {
LOGDEBUG( " Not today!")
 state.dayCheck = false
 }
 }

// Check volume levels ****************************************

def checkVolume(){
def timecheck = fromTime2
if (timecheck != null){
def between2 = timeOfDayIsBetween(fromTime2, toTime2, new Date(), location.timeZone)
    if (between2) {
    
    state.volume = volume2
   speaker.setLevel(state.volume)
    
   LOGDEBUG("Quiet Time = Yes - Setting Quiet time volume")
    
}
else if (!between2) {
state.volume = volume1
LOGDEBUG("Quiet Time = No - Setting Normal time volume")

speaker.setLevel(state.volume)
 
	}
}
else if (timecheck == null){

state.volume = volume1
speaker.setLevel(state.volume)

	}
 
}

// Enable Switch  **********************************************

def switchEnable(evt){
state.sEnable = evt.value
LOGDEBUG("$enableSwitch = $state.sEnable")
if(state.sEnable == 'on'){
state.appgo = true
}
else if(state.sEnable == 'off'){
state.appgo = false
}
}
// end enable switch ********************************************



// Define debug action  *****************************************
def logCheck(){
state.checkLog = debugMode
if(state.checkLog == true){
log.info "All Logging Enabled"
}
else if(state.checkLog == false){
log.info "Further Logging Disabled"
}

}
def LOGDEBUG(txt){
    try {
    	if (settings.debugMode) { log.debug("${app.label.replace(" ","_").toUpperCase()}  (Childapp Version: ${state.appversion}) - ${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
// end debug action ********************************************



// App Version   ***********************************************
def setAppVersion(){
    state.appversion = "1.0.0"
}
// end app version *********************************************