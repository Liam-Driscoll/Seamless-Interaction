from itertools import count
from gdx import gdx
from datetime import datetime
gdx = gdx.gdx()

completion = 0
sleep = 0

def sensorSetUp():
    global list_of_measurements
    global time_between_measurements
    global startingPattern 
    global actions
    actions = []
    startingPattern = [2000, 3500, 4000] # time in (ms)
    list_of_measurements = [0]
    time_between_measurements = 300 #in (ms)
    

    gdx.open_usb()
    #gdx.open_ble("GDX-RB 0K4005K3")

    gdx.select_sensors([1])
    gdx.start(time_between_measurements)


def sensor():
    now = datetime.now()

    raw_measurements = gdx.read()
    measurements = raw_measurements[0]
    list_of_measurements.append(measurements)

    slope = (list_of_measurements[-1] - list_of_measurements[-2])/(time_between_measurements/1000)
    slope_string = str(slope)

    # Each action is assigned a number which is later used to validate its completion
    # the higher the reliability of the classification the higher the assigned action value
    # since higher action values are more disruptive in the validation process
    if slope > 0.75:
        dt_string = now.strftime("%m/%d/%Y %H:%M:%S")  # date and time for data storage
        data = dt_string + ": Inhaling"
        action = -1
        breathAction = " INHALING "

    elif slope < -0.9:   
        dt_string = now.strftime("%m/%d/%Y %H:%M:%S")
        data = dt_string + ": Exhaling"
        action = 0
        breathAction = " EXHALING "
 
    else:
        dt_string = now.strftime("%m/%d/%Y %H:%M:%S")
        data = dt_string + ": Holding"
        action = 1
        breathAction = " HOLDING "

    actions.append(action)
   
    completionResult = completionCheck()
    #print(completionResult, data)
    return completionResult, data, breathAction

def completionCheck():
    global completion
    global sleep

# calculates number of measurements in each time interval
    times = []
    for time in startingPattern:
        times.append(time/time_between_measurements)

# loops through each time interval and the number of measurements recorded in the current interval
    for i in range(len(times)):
        validation = 0
        for j in range(1, int(times[i])+1):
            if i == 0: #INHALE
                try:
                    #starts from end of list to read most recent measurements
                    # skips past other intervals, 
                    # e.g., If the pattern is (Inhale -> Hold -> Exhale)<- Starts reading from here
                    # For Inhale, the Exhale and Hold measurements will be skipped 
                    # so that only Inhale measurements are read
                    validation += actions[-j-(int(times[1]+times[2]))]    
                except:
                    return None
            #Group hold and exhale together?
            elif i == 1: #HOLD
                try:
                    validation += actions[-j-int(times[2])]
                except:
                    return None
            elif i == 2: #EXHALE
                try:    
                    validation += actions[-j]
                except:
                    return None
        #print(str(validation) + "<=" + str(times[0]+3))

        # checks if each section of breathing pattern was completed
        if i == 0: 
            if validation<=times[0]+round(times[0]*0.5):           
                inhaleCompletion = 1
                #print(validation)
            else:
                inhaleCompletion = 0

        if i == 1:
            if validation>=times[1]-round(times[1]*0.5):
                holdCompletion = 1
            else:
                holdCompletion = 0

        if i == 2:
            if validation <= round(times[2]*0.5):
                exhaleCompletion = 1
            else:
                exhaleCompletion = 0
   
# sleeps algorithm for 5 seconds to prevent pattern from changing several times in a row
    if completion == 1:
        sleep = 5000/time_between_measurements

    if sleep > 0:
        sleep -= 1
        completion = 0
        return None
        
    else:
        totalCompletion = inhaleCompletion + holdCompletion + exhaleCompletion
        if totalCompletion == 3:
            completion = 1
            changePattern()
        else:
            completion = 0

    #print(inhaleCompletion, holdCompletion, exhaleCompletion, totalCompletion)
    printing = str(inhaleCompletion) + str(holdCompletion) + str(exhaleCompletion) + str(totalCompletion)

    return completion

# changes the breathing pattern duration once the current pattern is completed
def changePattern():
    for i in range(len(startingPattern)):
        startingPattern[i] += 500
    print(startingPattern)


'''
sensorSetUp()    
for i in range(1,501):
    sensor()
'''

#gdx.stop()
#gdx.close() 