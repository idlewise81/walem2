#!/usr/bin/python
import time
import requests
import json
import firebase

import RPi.GPIO as GPIO

firebase_url = 'https://moblert-b1226.firebaseio.com'

def post_alert(waterlevel):
  Timestamp= int(round(time.time() * 1000))
  time_hhmmss = time.strftime('%I:%M:%p %b %d, %Y')

  data15 ={'email':'wls_1@gmail.com','msgtype':'fa','edate':Timestamp,'level':'Water Level reach 15 meters.',
           'time_str':'as of '+str(time_hhmmss)+'. Keep an eye for possible flooding.',
           'detail': 'Flooding is possible. You should make some plans, '+
           'think about what you would have to do in a flood '+
           'and keep an eye on the situation.',
           'name':'Calumpang ALERT LEVEL 1','profilePic':'15','image':'15',
                  'timeStamp':Timestamp}  

  data16 ={'email':'wls_1@gmail.com','msgtype':'fa','edate':Timestamp,'level':'Water Level reach 16 meters.',
           'time_str':'as of '+str(time_hhmmss)+'. Prepare to evacuate!',
           'detail': 'Flooding is possible. You should make some plans, '+
           'think about what you would have to do in a flood '+
           'and keep an eye on the situation.',
           'name':'Calumpang ALERT LEVEL 2','profilePic':'15','image':'15',
                  'timeStamp':Timestamp}
  data17 ={'email':'wls_1@gmail.com','msgtype':'fa','edate':Timestamp,'level':'Water Level reach 17 meters.',
            'time_str':'as of '+str(time_hhmmss)+'. People should evacuate to designated centers.',
            'detail': 'Flooding of homes and businesses is most likely.'+
            'You should take action to make sure your family are safe, '+
            'and try to reduce the impact of flood (such as moving) '+
            'important items to a safe place',
           'name':'Calumpang ALERT LEVEL 3','profilePic':'16','image':'16',
                  'timeStamp':Timestamp}  
  data18 ={'email':'wls_1@gmail.com','msgtype':'fa','edate':Timestamp,'level':'Water Level reach '+str(waterlevel)+' meters.',
            'time_str':'as of '+str(time_hhmmss)+'. Forced Evacuaton!',
            'detail': 'Flooding of homes and businesses is most likely.'+
            'You should take action to make sure your family are safe, '+
            'and try to reduce the impact of flood (such as moving) '+
            'important items to a safe place',
           'name':'Calumpang ALERT LEVEL 4','profilePic':'16','image':'16',
                  'timeStamp':Timestamp}     
                                                  
  if waterlevel==15:
     result = firebase.push(firebase_url+'/feed.json',data15)
  if waterlevel==16:
     result = firebase.push(firebase_url+'/feed.json',data16)
  if waterlevel==17:
     result = firebase.push(firebase_url+'/feed.json',data17)      
  if waterlevel==18:
     result = firebase.push(firebase_url+'/feed.json',data18)        
  print ('')
  print ('')                    
  return 

def post_water_reading(waterlevel):
  #current time and date
  #time_hhmmss = time.strftime('%H:%M:%S')
  hour = time.strftime('%H')
  date_mmddyyyy = time.strftime('%d/%m/%Y')
    
  #current location name
  WLS_loc = 'Calumpang'
  water_level =waterlevel
    
  #insert record
  data = {'name':WLS_loc,'date':date_mmddyyyy,'time':hour,'water_level':water_level}
  res = firebase.put(firebase_url+'/current_level/'+WLS_loc,water_level)
 # result = firebase.push(firebase_url+'/location.json',data)#requests.post(firebase_url +'/location.json', data=json.dumps(data))
  print ('')
  print ('')
  return

def measure():
  # This function measures a distance
  GPIO.output(GPIO_TRIGGER, True)
  time.sleep(0.00001)
  GPIO.output(GPIO_TRIGGER, False)
  start = time.time()

  while GPIO.input(GPIO_ECHO)==0:
    start = time.time()

  while GPIO.input(GPIO_ECHO)==1:
    stop = time.time()

  elapsed = stop-start
  distance = (elapsed * 34300)/2

  return distance

def measure_average():
  # This function takes 3 measurements and
  # returns the average.
  distance1=measure()
  time.sleep(0.1)
  distance2=measure()
  time.sleep(0.1)
  distance3=measure()
  distance = distance1 + distance2 + distance3
  distance = distance / 3
  return distance

# -----------------------
# Main Script
# -----------------------

# Use BCM GPIO references
# instead of physical pin numbers
GPIO.setmode(GPIO.BCM)

# Define GPIO to use on Pi
GPIO_TRIGGER = 23
GPIO_ECHO    = 24

print "Ultrasonic Measurement"

# Set pins as output and input
GPIO.setup(GPIO_TRIGGER,GPIO.OUT)  # Trigger
GPIO.setup(GPIO_ECHO,GPIO.IN)      # Echo

# Set trigger to False (Low)
GPIO.output(GPIO_TRIGGER, False)

# Wrap main content in a try block so we can
# catch the user pressing CTRL-C and run the
# GPIO cleanup function. This will also prevent
# the user seeing lots of unnecessary error
# messages.
try:

  while True:

    distance = measure()#_average()
    if (distance>-1 and distance<400):
      post_alert(distance)
      distance = int(round(distance))
      post_water_reading(distance)
    print "Distance : %.1f" % distance
    time.sleep(4)

except KeyboardInterrupt:
  # User pressed CTRL-C
  # Reset GPIO settings
  GPIO.cleanup()