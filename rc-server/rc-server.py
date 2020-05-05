from evdev import UInput, ecodes as e
import netifaces, socket

#Determine broadcast IP to use:
for x in netifaces.interfaces():
  y=netifaces.ifaddresses(x)
  if socket.AF_INET in y and 'broadcast' in y[socket.AF_INET][0]:
    broadcast_ip = y[socket.AF_INET][0]['broadcast']

UDP_PORT = 10000

sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP
sock.bind((broadcast_ip, UDP_PORT))

ui = UInput()

while True:
    data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
    data = data.decode()
    print ("received message: " + data)
    if "+" in data:
      keycode1, keycode2 = data.split("+")
      ui.write(e.EV_KEY, int(keycode1), 1)  # key down
      ui.syn()
      ui.write(e.EV_KEY, int(keycode2), 1)  # key down
      ui.syn()
      ui.write(e.EV_KEY, int(keycode1), 0)  # key up
      ui.syn()
      ui.write(e.EV_KEY, int(keycode2), 0)  # key up
      ui.syn()
    else:
      keycode = int(data)  
      ui.write(e.EV_KEY, keycode, 1)  # key down
      ui.write(e.EV_KEY, keycode, 0)  # key up
      ui.syn()

#???:
ui.close()
