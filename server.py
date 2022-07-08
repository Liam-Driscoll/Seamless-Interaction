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
                participant = data
                createHRFile(data)
                createBRFile(data)
            try:
                if "." in data:
                    writeHR(data)
                if "ing" in data:
                    writeBR(data)
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

def createHRFile(participant):
    try:
        file = open(participant+"_HR.txt", "x")
    except (FileExistsError):
        print("HR file exists")

def createBRFile(participant):
    try:
        file = open(participant+"_BR.txt", "x")
    except(FileExistsError):
        print("BR file exists")

def writeHR(heartRate):
    file = open(participant+"_HR.txt", "a")
    file.write(heartRate+",\n")

def writeBR(breathRate):
    file = open(participant+"_BR.txt", "a")
    file.write(breathRate+",\n")

async def main():
    async with websockets.serve(hello, "206.87.9.211", 8765):
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