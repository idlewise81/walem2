import time
import requests
import json
import firebase
firebase_url = 'https://moblert-b1226.firebaseio.com'




def post_alert(waterlevel):
  Timestamp= int(round(time.time() * 1000))
  time_hhmmss = time.strftime('%I:%M:%p %b %d, %Y')

  data15 ={'email':'wls_1@gmail.com','msgtype':'fa','edate':Timestamp,'level':'Water Level reach 15 meters.',
           'time_str':'as of '+str(time_hhmmss)+'. Keep an eye for possible flooding.',
           'detail': 'Flooding is possible. You should make some plans, '+
           'think about what you would have to do in a flood '+
           'and keep an eye on the situation.',
           'name':'Tumana ALERT LEVEL 1','profilePic':'15','image':'15',
                  'timeStamp':Timestamp}  

  data16 ={'email':'wls_1@gmail.com','msgtype':'fa','edate':Timestamp,'level':'Water Level reach 16 meters.',
           'time_str':'as of '+str(time_hhmmss)+'. Prepare to evacuate!',
           'detail': 'Flooding is possible. You should make some plans, '+
           'think about what you would have to do in a flood '+
           'and keep an eye on the situation.',
           'name':'Tumana ALERT LEVEL 2','profilePic':'15','image':'15',
		              'timeStamp':Timestamp}
  data17 ={'email':'wls_1@gmail.com','msgtype':'fa','edate':Timestamp,'level':'Water Level reach 17 meters.',
            'time_str':'as of '+str(time_hhmmss)+'. People should evacuate to designated centers.',
            'detail': 'Flooding of homes and businesses is most likely.'+
            'You should take action to make sure your family are safe, '+
            'and try to reduce the impact of flood (such as moving) '+
            'important items to a safe place',
           'name':'Tumana ALERT LEVEL 3','profilePic':'16','image':'16',
		              'timeStamp':Timestamp}	
  data18 ={'email':'wls_1@gmail.com','msgtype':'fa','edate':Timestamp,'level':'Water Level reach '+str(waterlevel)+' meters.',
            'time_str':'as of '+str(time_hhmmss)+'. Forced Evacuaton!',
            'detail': 'Flooding of homes and businesses is most likely.'+
            'You should take action to make sure your family are safe, '+
            'and try to reduce the impact of flood (such as moving) '+
            'important items to a safe place',
           'name':'Tumana ALERT LEVEL 4','profilePic':'16','image':'16',
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
  WLS_loc = 'Tumana'
  water_level =waterlevel
    
  #insert record
  data = {'name':WLS_loc,'date':date_mmddyyyy,'time':hour,'water_level':water_level}
  res = firebase.put(firebase_url+'/current_level/'+WLS_loc,water_level)
 # result = firebase.push(firebase_url+'/location.json',data)#requests.post(firebase_url +'/location.json', data=json.dumps(data))
  print ('')
  print ('')
  return


# -----------------------
# Main Script
# -----------------------
#res=firebase.get(firebase_url+'/feed')
#print(res)

try:

  while True:

    userInput = input("Input Water level:")

    try:
      val = int(userInput)
      post_water_reading(userInput)
      post_alert(val)
    except ValueError:
     print("That's not an number")

    time.sleep(1)

except KeyboardInterrupt:
  print("Program terminated.")
