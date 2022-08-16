from itertools import count
from tracemalloc import start
from gdx import gdx
from datetime import datetime
gdx = gdx.gdx()
import os
completion = 0
sleep = 0

def sensorSetUp():
    global list_of_measurements
    global time_between_measurements
    global startingPattern
    global pattern 
    global goalPattern
    global actions
    actions = []
    startingPattern = [2000, 2000] # time in (ms)
    pattern = [2000, 2000] # time in (ms)
    goalPattern = [6000, 6000]
    list_of_measurements = [0]
    time_between_measurements = 300 #in (ms)
    

    #gdx.open_usb()
    gdx.open_ble("GDX-RB 0K4005K3")

    gdx.select_sensors([1,2])
    gdx.start(time_between_measurements)


def sensor():
    now = datetime.now()

    raw_measurements = gdx.read()
    force = round(raw_measurements[0], 2)
    respiration_rate = round(raw_measurements[1], 2)
    list_of_measurements.append(force)
    resetPattern()
    decreasePattern()
    slope = (list_of_measurements[-1] - list_of_measurements[-2])/(time_between_measurements/1000)
    slope_string = str(slope)

    # Each action is assigned a number which is later used to validate its completion
    # the higher the reliability of the classification the higher the assigned action value
    # since higher action values are more disruptive in the validation process
    if slope > 0.5:
        date = now.strftime("%m/%d/%Y,%H:%M:%S")  # date and time for data storage
        action = 1
        breathAction = "INHALING"

    elif slope < -0.5:   
        date = now.strftime("%m/%d/%Y,%H:%M:%S")
        action = -1
        breathAction = "EXHALING"
 
    else:
        date = now.strftime("%m/%d/%Y,%H:%M:%S")
        action = 0
        breathAction = "HOLDING"

    actions.append(action)
   
    # 1 if the pattern was completed, 0 if it was not completed
    completionResult = completionCheck()

    comma = ","

    # results to be stored in the csv file
    message = [date, str(completionResult), breathAction, str(force), str(respiration_rate)]
    results = comma.join(message)

    return completionResult, results

def completionCheck():
    global completion
    global sleep

# calculates number of measurements in each time interval
    times = []
    for time in pattern:
        # -100 adds a buffer so that patterns can be completed by following vibration
        # before you would have to extend inhales/exhales to complete pattern
        times.append((time-150)/time_between_measurements)

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
                    validation += actions[-j-(int(times[1]))]    
                except:
                    return None

            elif i == 1: #EXHALE
                try:    
                    validation += actions[-j]
                except:
                    return None
        #print(str(validation) + "<=" + str(times[0]+3))

        # checks if each section of breathing pattern was completed
        # by comparing validation sum to expected sum (with a buffer)
        if i == 0: 
            if validation>=times[0]-round(times[0]*0.2):           
                inhaleCompletion = 1
            else:
                inhaleCompletion = 0

        if i == 1:
            if validation<=times[1]*-1+round(times[1]*0.2):
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
        totalCompletion = inhaleCompletion + exhaleCompletion
        if totalCompletion == 2:
            completion = 1
            increasePattern()
        else:
            completion = 0

    #print(inhaleCompletion, holdCompletion, exhaleCompletion, totalCompletion)
    printing = str(inhaleCompletion) + str(exhaleCompletion) + str(totalCompletion)
    
    return completion

# changes the breathing pattern duration once the current pattern is completed
def increasePattern():
    global pattern, startingPattern
    if (pattern[0] < goalPattern[0]):
        for i in range(len(pattern)):
            pattern[i] += 250
    print(pattern)

def decreasePattern():
    global pattern, startingPattern
    file_path = "decrease.txt"

    # check if size of file is 0
    if os.stat(file_path).st_size != 0:
        startingPattern = [2000, 2000] # time in (ms)
        f = open(file_path, 'r+')
        f.truncate(0) # clear file 

        if (pattern[0] > startingPattern[0]):
            for i in range(len(pattern)):
                pattern[i] -= 250
            print(f"PATTERN DECREASED TO: {pattern}")


def resetPattern():
    global pattern, startingPattern
    file_path = "reset.txt"

    # check if size of file is 0
    if os.stat(file_path).st_size != 0:
        startingPattern = [2000, 2000] # time in (ms)
        f = open(file_path, 'r+')
        f.truncate(0) # clear file 
        pattern = startingPattern
        print(f"PATTERN RESET TO: {pattern}")


#gdx.stop()  #stops sensor
#gdx.close() #closes sensor