import asyncio
import websockets
'''
async def hello():
    uri = "ws://206.87.9.89:8765"
    async with websockets.connect(uri) as websocket:
        while True:
            name = input("What's your name? ")

            await websocket.send(name)
            print(f">>> {name}")

            greeting = await websocket.recv()
            print(f"<<< {greeting}")

if __name__ == "__main__":
    asyncio.run(hello())
'''


















#from sensor import sensor, sensorSetUp
from updatedClosedLoopAlgorithm import sensorSetUp, sensor, completionCheck, changePattern
run = True
async def send_data():
    #uri = "ws://localhost:8765"
    #uri = "ws://172.20.10.4:8765" #hotspot
    uri = "ws://142.231.70.68:8765" #ubcsecure
    

    async with websockets.connect(uri) as websocket:
        '''
        if run == True:
            participant = await websocket.recv()
        if "Participant" in participant: 
            fileName = participant + "_BR.txt"
            file = open(fileName, "w")
            run = FALSE
        try:
            file = open(fileName, "a")
            file.write(sensor_data[1])
        except:
            pass
'''
        await websocket.send(str(sensor_data[0])) # data must be string, bytes etc.
        await websocket.send(str(sensor_data[1]))
        #print(f">>> {sensor_data}")

if __name__ == "__main__":
    sensorSetUp()
    while True:
        sensor_data = sensor()
        print(str(sensor_data[0]), " ", str(sensor_data[1]))
        asyncio.run(send_data())

