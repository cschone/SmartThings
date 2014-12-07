/**
 *  Sump Pump Power Monitor
 *
 *  Copyright 2014 Chad Schone
 *
 *  Based on https://github.com/sudarkoff/smarttings/blob/develop/BetterLaundryMonitor.groovy
 *  by sudarkoff
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
definition(
    name: "Sump Pump Power Monitor",
    namespace: "skismatik",
    author: "Chad Schone",
    description: "Use an power meter to monitor sump pump activity.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartthings-device-icons/alarm/water/wet.png",
    iconX2Url: "https://s3.amazonaws.com/smartthings-device-icons/alarm/water/wet@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartthings-device-icons/alarm/water/wet@3x.png")


preferences {
  section ("When this device starts drawing power") {
    input "meter", "capability.powerMeter", multiple: false, required: true
  }

  section (title: "Notification method") {
    input "sendPushMessage", "bool", title: "Send a push notification?"
  }
  
  section (title: "Notification method") {
    input "phone", "phone", title: "Send a text message to:", required: false
  }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(meter, "power", handler)
}

def handler(evt) {
	def latestPower = meter.currentValue("power")
    log.trace "Sump Pump Power Monitor: ${latestPower}W"
    
    if (!state.cycleOn && latestPower > 2) {
    	state.cycleOn = true
        tate.cycleStart = now()
        log.trace "Cycle started."
        
        // If the sump pump starts drawing power, send notification.
        def message = "Check your sump pump!"
    	send()
  	} else if (state.cycleOn && latestPower == 0) {
    	state.cycleOn = false
    	state.cycleEnd = now()
    	duration = state.cycleEnd - state.cycleStart
    	log.trace "Cycle ended after ${duration} minutes."
    }
}

private send() {
	def msg = "Your sump pump is running!"
    if (sendPushMessage) {
        sendPush(msg)
    }

    if (phone) {
        sendSms(phone, msg)
    }

    log.debug msg
}