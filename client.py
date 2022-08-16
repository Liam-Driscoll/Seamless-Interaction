import asyncio
import websockets


from updatedClosedLoopAlgorithm import sensorSetUp, sensor
run = True
async def send_data():
    #uri = "ws://localhost:8765"  #localhost
    uri = "ws://206.87.9.22:8765" #ubcsecure
    

    async with websockets.connect(uri) as websocket:
        await websocket.send(str(sensor_data[0])) # data must be string, bytes etc.
        await websocket.send(str(sensor_data[1]))

if __name__ == "__main__":
    sensorSetUp()
    while True:
        sensor_data = sensor()
        print(str(sensor_data[0]), " ", str(sensor_data[1]))
        asyncio.run(send_data())

