#Perform a rolling XOR on a file
import sys

if len (sys.argv) == 3:
 (progname, file_in, file_out ) = sys.argv
else:
 print len (sys.argv)
 print 'Usage: {0} file_in file_out'.format (sys.argv[0])
 exit (1)
 
#Random xor key
xor_key = "\xa3\x45\x23\x06\xf4\x21\x42\x81\x72\x11\x92\x29"
 
#Open files
f_i = open(file_in, 'rb')
f_o = open(file_out, 'wb')
data = f_i.read()
f_i.close()

#Rolling XOR
for i in range(len(data)):
  out = ord(data[i]) ^ ord(xor_key[i%len(xor_key)])
  f_o.write(chr(out))
  
#Close
f_o.close()

  