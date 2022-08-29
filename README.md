# Overview
"Seamless Interaction” is a research project exploring open (no feedback) and closed (feedback) loop breathing guidance.   

Breathing guidance is a popular way for users to relax, meditate, and be generally mindful of their breath. Breathing guidance could be needed at any time, therefore making a smartwatch, which is always-available and body-worn, well-suited for directing breathing exercises. Current breathing guidance tools are one-size-fits-all and do not allow for any customization based on the user. However, in many situations, such as after a workout or during a stressful event, users may want to be provided with breathing guidance that is customized to their current ability. 

 

# Smartwatch Set Up 

## Basic requirements

- Android Studio installed on the computer  

  - Tutorial on how to install [Android Studio](https://www.youtube.com/watch?v=0zx_eFyHRU0)  

- Wear OS app installed on the phone 

## Connecting phone and watch

- Follow instructions found at this [link](https://support.google.com/wearos/answer/6056630?hl=en&co=GENIE.Platform%3DAndroid)  

## Required settings 

- Phone 

  - In the Wear OS app, under “Advanced settings”, ensure that the “Debugging over Bluetooth” option is enabled 

- Watch 

  - Select the middle side button to open the app selection screen, open the “Settings” app, scroll to the bottom and select “Developer options”, ensure that both the “ADB debugging” and “Debug over Bluetooth” options are enabled 

  - Open the “Settings” app, select “Connectivity”, select “Bluetooth”, and ensure that “Bluetooth” is enabled and that the phone is connected 

- Android Studio 

  - You will need to use “adb” commands to install the app on the watch 

  - To use the “adb” commands you must be in the correct directory, so make sure to navigate to that directory in the command line shell or set the path of “adb” into Environment Variables before running the commands 

  - adb is found in “platform-tools” and the default directory for that is: ```C:\Users\User\AppData\Local\Android\Sdk\platform-tools```

 

## Install application on watch

 - Enter the adb commands 

- The adb commands in the order they should be executed are
```
adb forward tcp:4444 localabstract:/adb-hub 

adb connect 127.0.0.1:4444 
```
> Note: “4444” is the port number and can be any number from 0 to 65536 

> Note: 127.0.0.1 is the localhost IP address 

- After you run the commands, the watch should appear in the list of available devices in Android Studio 

- You will then be able to install the app on the watch 

# Sensor Set Up 

## Basic information on how to use sensor 

- Tutorial [video](https://www.youtube.com/watch?v=-2Qgvu1Ygmw ) (watch until ~1:00) 

- Helpful [resource](https://github.com/VernierST/godirect-examples/tree/main/python) for Python programming with sensor  

- README for sensor functions is found in ```gdx/readme.md```

- Sensor lights 

  - Checkmark light (located in bottom left corner):  

    - Shines GREEN if there is an adequate amount of force being applied 

    - Shines RED if there is too much force being applied 

  - Bluetooth light (located above power button): 

    - Blinks RED when Bluetooth is on 

    - Blinks GREEN when Bluetooth is connected 

  - Charging light (located below power button): 

    - Shines ORANGE when charging 

- Press the power button to turn on the sensor’s Bluetooth functionality (Bluetooth light should begin blinking red) 

# Server Set Up 

- Change the IP address located in “server.py” within the main() function to the IP address of the network you are connected to 

> Note: You don’t need to change the port, but if you do make sure the same port is used for the server and the clients (watch and sensor)	 

- To start the server, run the “server.py” file 

## Connect to Server 
### Watch

- Change the IP address in “MainActivity2.java” in the “Seamless Interaction” project to match the IP address of the network you are connected to 

> Note: You don’t need to change the port, but if you do make sure the same port is used for the server and the clients (watch and sensor) 

- Once the IP address is changed, install the application onto the watch  

- The app should automatically connect to the server any time it is opened if the server is currently running, but if it is not connecting to the server try reinstalling the app onto the watch (will need to run adb commands)

### Sensor

- Change the IP address in “client.py” to match the IP address of the network you are connected to 

> Note: You don’t need to change the port, but if you do make sure the same port is used for the server and the clients (watch and sensor) 

- Once the IP address is changed, the sensor’s Bluetooth light is blinking red, and the server is running, run the “client.py” file 

- The sensor will connect to the server if the server is currently running

> Note: Data will begin to be collected once both the watch and sensor are connected to the server

## Data Storage 

- Data is stored in a separate csv file for each participant (i.e., Participant 1 and Participant 2 will both have their own separate csv files) 

- The saved csv files are located in the same directory as the “server.py” file 
