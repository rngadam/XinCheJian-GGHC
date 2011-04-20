
#include <Servo.h>
#include "CustomServo.h"

const byte SIDEWAYS = 'S';
const byte UPDOWN = 'U';
 
const byte SIDEWAYS_MIN_POS = 20;
const byte SIDEWAYS_DEFAULT_POS = 90; 
const byte SIDEWAYS_MAX_POS = 160;
const byte UPDOWN_MIN_POS = 50;
const byte UPDOWN_DEFAULT_POS = 135; 
const byte UPDOWN_MAX_POS = 180;
const byte SIDEWAYS_PIN = 9;
const byte UPDOWN_PIN = 10;

CustomServo servo_sideways(SIDEWAYS_PIN, SIDEWAYS_MIN_POS, SIDEWAYS_MAX_POS, SIDEWAYS_DEFAULT_POS); 
CustomServo servo_updown(UPDOWN_PIN,  UPDOWN_MIN_POS, UPDOWN_MAX_POS, UPDOWN_DEFAULT_POS); 

void setup() 
{ 
  Serial.begin(9600);  
} 

byte readDigitValue() 
{
  while(Serial.available() < 1) {
    delay(10);
  }
  return Serial.read() - '0';
}

byte readDegrees() 
{
  return readDigitValue()*100 + readDigitValue()*10 + readDigitValue();
}

void loop() 
{ 
  if (Serial.available() > 0) {  
    switch(Serial.read()) {
      case 'S':
        servo_sideways.set_target(readDegrees());
        break;
      case 'U':
        servo_sideways.set_target(readDegrees());
        break;
      default:
        Serial.println("Unrecognized value");
        break;
    }
  }  
  servo_sideways.update_pos();
  servo_updown.update_pos();
} 
