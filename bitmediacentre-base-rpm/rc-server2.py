#from evdev import UInput, ecodes as e
import os,re
import netifaces, socket, subprocess

#Determine broadcast IP to use:
#Prefer the first result from 'nmcli' command:
preferred_iface=None
try: 
  preferred_iface=subprocess.check_output("nmcli d show |grep GENERAL.DEVICE|head -n+1|awk '{print $2}'",shell=True).decode().rstrip()
except:
  pass 

if preferred_iface == None:
  for x in netifaces.interfaces():
    y=netifaces.ifaddresses(x)
    print (x+": "+str(y))
    if socket.AF_INET in y and 'broadcast' in y[socket.AF_INET][0]:
      broadcast_ip = y[socket.AF_INET][0]['broadcast']
else:
  broadcast_ip = netifaces.ifaddresses(preferred_iface)[socket.AF_INET][0]['broadcast']

UDP_PORT = 10000

sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP
sock.bind((broadcast_ip, UDP_PORT))

#ui = UInput()

lines=open('/proc/bus/input/devices').readlines()
theid=-1
foundit=False
for l in lines:
  if l.startswith('N: Name="FakeKeyboard"'):
    foundit=True
    continue
  if foundit and l.startswith('H: '):
    theid=re.match('^H: Handlers=.*?event(\d+)', l).group(1)
    break

assert foundit == True

the_device_path='/dev/input/event'+str(theid)
print ('the device path is: '+str(the_device_path))

def call_evemu_event(a_key_code,a_value):
    os.system('/bin/evemu-event {} --type EV_KEY --code {} --value {} --sync'.format(the_device_path, a_key_code, a_value))

def key_down(a_key_code):
    call_evemu_event(a_key_code, 1)

def key_up(a_key_code):
    call_evemu_event(a_key_code, 0)

while True:
    data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
    data = data.decode()
    print ("received message: " + data)
    if "+" in data:
      keycode1, keycode2 = data.split("+")
      key_down(keycode1)
      key_down(keycode2)
      key_up(keycode1)
      key_up(keycode2)
    else:
      keycode = int(data)  
      key_down(keycode)
      key_up(keycode)

#???:
#ui.close()
