
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
  for(int pos = 0; pos < 180; pos += 1)  // goes from 0 degrees to 180 degrees 
  {                                  // in steps of 1 degree 
    servo_sideways.write(pos);              // tell servo to go to position in variable 'pos' 
    servo_updown.write(pos/2+45);              // tell servo to go to position in variable 'pos' 
    delay(15);                       // waits 15ms for the servo to reach the position 
  } 
  for(int pos = 180; pos>=1; pos-=1)     // goes from 180 degrees to 0 degrees 
  {                                
    servo_sideways.write(pos);              // tell servo to go to position in variable 'pos' 
    servo_updown.write(pos/2+45);              // tell servo to go to position in variable 'pos' 
    delay(15);                       // waits 15ms for the servo to reach the position 
  } 
} 
