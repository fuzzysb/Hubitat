/**
 *
 *  Author: Stuart Buchanan, Based on original work by Steve The Geek (Steve Tremblay) with thanks.
 *
 *	Updates:
 *	Date: 2017-04-26  v1.0 Adapted to Hubitat.
 */
import groovy.json.JsonSlurper

metadata {
	definition (name: "NanoLeaf Aurora", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Light"
		capability "Switch Level"
		capability "Switch"
		capability "Color Control"
        capability "Polling"
        capability "Refresh"
        capability "Actuator"
		
		command "createAuthToken"
        command "previousScene"
		command "nextScene"
		command "setScene1"
		command "setScene2"
		command "setScene3"
        command "setScene4"
        command "setPartyScene1"
        command "setPartyScene2"
        command "setPartyScene3"
        command "setPartyScene4"
        
        attribute "apiKey", "String"
		attribute "scene", "String"
		attribute "scenesList", "String"
	}

	simulator {
	}
    
   	preferences {
    		input name: "scene1", type: "text", title: "Favorite Scene 1", description: "Enter a Scene name", required: false
        	input name: "scene2", type: "text", title: "Favorite Scene 2", description: "Enter a Scene name", required: false
        	input name: "scene3", type: "text", title: "Favorite Scene 3", description: "Enter a Scene name", required: false
            input name: "scene4", type: "text", title: "Favorite Scene 4", description: "Enter a Scene name", required: false
            input name: "partyScene1", type: "text", title: "Favorite Party Scene 1", description: "Enter a Scene name", required: false
            input name: "partyScene2", type: "text", title: "Favorite Party Scene 2", description: "Enter a Scene name", required: false
            input name: "partyScene3", type: "text", title: "Favorite Party Scene 3", description: "Enter a Scene name", required: false
            input name: "partyScene4", type: "text", title: "Favorite Party Scene 4", description: "Enter a Scene name", required: false
	}
}

def parse(resp) {
    	
    	if(resp.data) {
      		def auroraOn = resp.data.state.on.value
      
      		if(auroraOn && device.currentValue("switch") == "off") {
        		log.debug("Aurora has been switched on outside of Hubitat")
      			sendEvent(name: "switch", value: "on", isStateChange: true)
      		}
      		
		if(!auroraOn && device.currentValue("switch") == "on") {
        		log.debug("Aurora has been switched off outside of Hubitat")
      	 		sendEvent(name: "switch", value: "off", isStateChange: true)
      		}
      
      	def currentScene = resp.data.effects.select
      		if(currentScene != device.currentValue("scene")) {
         	log.debug("Scene was changed outside of Hubitat")
         	sendEvent(name: "scene", value: currentScene, isStateChange: true)
      	}

      	def currentBrightness = resp.data.state.brightness.value
      	def deviceBrightness = "${device.currentValue("level")}"
      	if(currentBrightness != device.currentValue("level")) {
         	log.debug("Brightness was changed outside of Hubitat")
         	sendEvent(name: "level", value: currentBrightness, isStateChange: true)
      	}
      
      	def effectsList = resp.data.effects.effectsList
      		if(effectsList.toString() != device.currentValue("scenesList").toString()) {
         	log.debug("List of effects was changed in the Aurora App")
         	sendEvent(name: "scenesList", value: effectsList, isStateChange: true)
      	}

    	} else {
      		log.debug("Response from PUT, do nothing")
    	}
}

def poll() {
    	log.debug("polled")
    	refresh()
}

def refresh() {
	return createGetRequest("");
}

def off() {
	sendEvent(name: "switch", value: "off", isStateChange: true)
	return createPutRequest("state", "{\"on\" : false}")
} 

def on() {
	sendEvent(name: "switch", value: "on", isStateChange: true)
	return createPutRequest("state", "{\"on\" : true}")
}

def previousScene() {
  	def sceneListString = device.currentValue("scenesList").replaceAll(", ", ",")
  	def sceneList = sceneListString.substring(1, sceneListString.length()-1).tokenize(',')
  	def currentSelectedScene = device.currentValue("scene");
  	def index = sceneList.indexOf(currentSelectedScene)
    	log.debug(index)
  
  	if(index == -1) {
    		index = 1;
  	}
  	
	index--
  	if(index == -1) {
     		index = sceneList.size -1
  	}
	
	changeScene(sceneList[index])
}

def nextScene() {
  	def sceneListString = device.currentValue("scenesList").replaceAll(", ", ",")
  	def sceneList = sceneListString.substring(1, sceneListString.length()-1).tokenize(',')
  	def currentSelectedScene = device.currentValue("scene");
  	def index = sceneList.indexOf(currentSelectedScene)
  
  	index++
    	if(index == sceneList.size) {
     		index = 0
  	}
  	
	changeScene(sceneList[index])
}

def changeScene(String scene) {
    	sendEvent(name: "scene", value: scene, isStateChange: true)
    	return createPutRequest("effects", "{\"select\" : \"${scene}\"}")
}

def createAuthToken(){
    createAuthTokenRequest()
}

def setScene1() {
	sendEvent(name: "scene1", value: "${scene1}")
    	changeScene("${scene1}")
}    

def setScene2() {
	sendEvent(name: "scene2", value: "${scene2}")
    	changeScene("${scene2}")
} 

def setScene3() {
	sendEvent(name: "scene3", value: "${scene3}")
	changeScene("${scene3}")
} 

def setScene4() {
	sendEvent(name: "scene4", value: "${scene4}")
	changeScene("${scene4}")
}

def setPartyScene1() {
	sendEvent(name: "partyScene1", value: "${partyScene1}")
	changeScene("${partyScene1}")
}

def setPartyScene2() {
	sendEvent(name: "partyScene2", value: "${partyScene2}")
	changeScene("${partyScene2}")
} 

def setPartyScene3() {
	sendEvent(name: "partyScene3", value: "${partyScene3}")
	changeScene("${partyScene3}")
} 

def setPartyScene4() {
	sendEvent(name: "partyScene4", value: "${partyScene4}")
	changeScene("${partyScene4}")
} 

def setLevel(value) {
    sendEvent(name: "level", value: "${value}", isStateChange: true)
	   return createPutRequest("state", "{\"brightness\" : ${value}}")
}

def setColor(value) {
    	sendEvent(name: "scene", value: "--", isStateChange: true)
    	sendEvent(name: "color", value: value.hex, isStateChange: true)
    	return createPutRequest("state", "{\"hue\" : ${(value.hue*360/100).toInteger()}, \"sat\" : ${value.saturation.toInteger()}}")
}

// gets the address of the hub
private getCallBackAddress() {
    	return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private createPutRequest(String url, String body) {

	    log.debug("/api/v1/${device.currentValue("apiKey").toString()}/${url}")
        log.debug("body : ${body}")
    
    	def result
		try {
            log.debug("Beginning API PUT: http://${getHostAddress()}/api/v1/${device.currentValue("apiKey").toString()}/${url}, ${body}")
            def params = [
				uri: "http://${getHostAddress()}",
        		path: "/api/v1/${device.currentValue("apiKey").toString()}/${url}",
       			requestContentType: "application/json",
        		headers: apiRequestHeaders(),
                body: body
    		]
			
            httpPut(params) {response ->
			logResponse(response)
			result = response
			}
		}
    	catch (groovyx.net.http.HttpResponseException e) {
			logResponse(e.response)
			result = e.response
        }
	

        return result;
}

private createAuthTokenRequest() {

	    log.debug("/api/v1/new")   
    	def result
		try {
            log.debug("Beginning API POST: http://${getHostAddress()}/api/v1/new")
            def params = [
				uri: "http://${getHostAddress()}",
        		path: "/api/v1/new",
        		headers: apiRequestHeaders(),
    		]
			
            httpPost(params) {response ->
			parseToken(response)
			result = response
			}
		}
    	catch (groovyx.net.http.HttpResponseException e) {
			logResponse(e.response)
			result = e.response
        }
	

        return result;
}

Map apiRequestHeaders() {
    return [ "Host": getHostAddress(),
   			"Content-Type": "application/json"
	]
}

private logResponse(response) {
	log.info("Status: ${response.status}")
	log.info("Body: ${response.data}")
}

private parseToken(response) {
	log.info("Status: ${response.status}")
	log.info("Body: ${response.data}")
    if(response.data){
        log.info("RetrievedToken: ${response.data.auth_token.value}")
        def newToken = response.data.auth_token.value
      		if(newToken.toString() != device.currentValue("apiKey").toString()) {
                log.debug("New apiKey was requested it's Value is: ${newToken}")
         	sendEvent(name: "apiKey", value: newToken.toString(), isStateChange: true)
      	}
    }
}

private createGetRequest(String url) {

	def result
		try {
            log.debug("Beginning API Get: http://${getHostAddress()}/api/v1/${device.currentValue("apiKey").toString()}/${url}")
            def params = [
				uri: "http://${getHostAddress()}",
        		path: "/api/v1/${device.currentValue("apiKey").toString()}/${url}",
        		headers: apiRequestHeaders()
    		]
			
            httpGet(params) {response ->
			parse(response)
			result = response
			}
		}
    	catch (groovyx.net.http.HttpResponseException e) {
			logResponse(e.response)
			result = e.response
        }
	

        return result;
}

// gets the address of the device
private getHostAddress() {
    	def ip = getDataValue("ip")
    	def port = getDataValue("port")

    	if (!ip || !port) {
        	def parts = device.deviceNetworkId.split(":")
        	if (parts.length == 2) {
            		ip = parts[0]
            		port = parts[1]
        	} else {
            		log.warn "Can't figure out ip and port for device: ${device.id}"
        	}
	}

    	log.debug "Using IP: " + convertHexToIP(ip) + " and port: " + convertHexToInt(port) + " for device: ${device.id}"
    	return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
    	return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    	return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}