
#include <Servo.h> 
 
Servo servo_sideways; 
Servo servo_updown; 

void setup() 
{ 
  servo_sideways.attach(9); 
  servo_updown.attach(10); 
} 
 
 
void loop() 
{ 
  for(int pos = 20; pos < 160; pos += 1) 
  {                                   
    servo_sideways.write(pos);        
    delay(15);                       
  } 
  servo_sideways.write(90);
  
  for(int pos = 50; pos < 180; pos += 1) 
  {                                   
    servo_updown.write(pos);        
    delay(15);                       
  } 
  servo_updown.write(105);
} 
