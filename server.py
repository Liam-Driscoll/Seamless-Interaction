import asyncio
from re import L
import websockets
import updatedClosedLoopAlgorithm

connected = set()

async def hello(websocket):
    connected.add(websocket)
    try:
        while True:
            try:
                data = await websocket.recv()
            except websockets.ConnectionClosed:
                #print(f"Terminated")
                break

            if "Participant" in data:
                global participant
                global watch_data
                global sensor_data
                participant = data
                createFile(data)
            try:
                if "P" in data:
                    watch_data = data
                    if watch_data is not None and sensor_data is not None:
                        writeData(sensor_data,watch_data)
                if "ING" in data:
                    sensor_data = data
                    if watch_data is not None and sensor_data is not None:
                        writeData(sensor_data,watch_data)
                # resets vibration pattern once guidance is finished
                if "reset" in data:
                    print("Pattern is being reset...")
                    try:
                        f = open("reset.txt", 'w')
                        f.write("reset")
                    except Exception as e:
                        print(e)
            except:
                pass

            #print(f"< {data}")

            #await websocket.send(greeting)
            for connection in connected:
                # only sends data to other clients (doesn't send to itself)
                if connection != websocket:
                    #only send data to watch if pattern is completed 
                    if  data == "1":
                        print("Pattern complete!")
                        await connection.send(data)
            #print(f"> {greeting}")
    finally:
        connected.remove(websocket)

def createFile(participant):
    try:
        file = open(participant+".txt", "x")
    except (FileExistsError):
        print("HR file exists")

def writeData(sensor, watch):
    file = open(participant+".txt", "a")
    file.write(sensor+","+watch+"\n")

async def main():
    async with websockets.serve(hello, "142.231.70.68", 8765):
        await asyncio.Future()  # run forever

if __name__ == "__main__":
    print("Server Opened")
    asyncio.run(main())

















'''
if "Participant" in data:
                global participant
                participant = data
                createHRFile(data)
                createBRFile(data)
            try:
                if "PDT" in data:
                    writeHR(data)
                if "/" in data:
                    writeBR(data)
            except:
                pass

async def receive_data(websocket):
    while True:
        data = await websocket.recv()
        await websocket.send(data)
        # if data is ??: append to breathing_data
        # else append to heart_rate_data
        print(f"<<< {data}")


async def main():
    async with websockets.serve(receive_data, "206.87.9.89", 8765):
        await asyncio.Future()  # run forever

if __name__ == "__main__":
    asyncio.run(main())
'''