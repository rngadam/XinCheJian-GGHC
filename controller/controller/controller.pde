#include <PWMServo.h>
#include "CustomServo.h"
#include <NewSoftSerial.h>

const byte SIDEWAYS = 'S';
const byte UPDOWN = 'U';
 
const byte SIDEWAYS_MIN_POS = 20;
const byte SIDEWAYS_DEFAULT_POS = 90; 
const byte SIDEWAYS_MAX_POS = 160;
const byte UPDOWN_MIN_POS = 55;
const byte UPDOWN_DEFAULT_POS = 135; 
const byte UPDOWN_MAX_POS = 180;
const byte SIDEWAYS_PIN = 9;
const byte UPDOWN_PIN = 10;

CustomServo servo_sideways = CustomServo(SIDEWAYS_PIN, SIDEWAYS_MIN_POS, SIDEWAYS_MAX_POS, SIDEWAYS_DEFAULT_POS);
CustomServo servo_updown = CustomServo(UPDOWN_PIN,  UPDOWN_MIN_POS, UPDOWN_MAX_POS, UPDOWN_DEFAULT_POS);

NewSoftSerial bluetooth(4, 5);

void setup() 
{ 
  Serial.begin(115200);  
  bluetooth.begin(115200);
  servo_sideways.setup();
  servo_sideways.disable_filtering();
  servo_updown.setup();
  servo_updown.disable_filtering();
} 

/*********************************
USB Serial port logic
*********************************/
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

void checkSerial()
{
  if (Serial.available() > 0) {  
    switch(Serial.read()) {
      case SIDEWAYS:
        servo_sideways.set_target(readDegrees());
        break;
      case UPDOWN:
        servo_updown.set_target(readDegrees());
        break;
      default:
        Serial.println("Unrecognized value");
        break;
    }
  }
}

/*********************************
NewSoftSerial Serial port logic
*********************************/
byte readDigitValueNewSoftSerial(NewSoftSerial& serial) 
{
  while(serial.available() < 1) {
    delay(10);
  }
  return serial.read() - '0';
}

byte readDegreesNewSoftSerial(NewSoftSerial& serial) 
{
  byte value = readDigitValueNewSoftSerial(serial)*100 + readDigitValueNewSoftSerial(serial)*10 + readDigitValueNewSoftSerial(serial);
  if(readDigitValueNewSoftSerial(serial) ==  value % 9) {
    return value;
  } else {
    return 0;
  }
}

void checkNewSoftSerial(NewSoftSerial& serial)
{
  if (serial.available() > 0) {  
    switch(serial.read()) {
      case SIDEWAYS:
        servo_sideways.set_target(readDegreesNewSoftSerial(serial));
        break;
      case UPDOWN:
        servo_updown.set_target(readDegreesNewSoftSerial(serial));
        break;
      default:
        serial.println("Unrecognized value");
        break;
    }
  }
}
  
void loop() 
{ 
  checkSerial();
  checkNewSoftSerial(bluetooth);
  
  servo_sideways.update_pos();
  servo_updown.update_pos();
} 
