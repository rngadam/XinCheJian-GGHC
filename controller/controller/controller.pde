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

// bluetooth tx -> arduino rx
// arduino tx -> bluetooth rx 
NewSoftSerial bluetooth(4, -1);

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
  while(!serial.available()) {
    delay(50);
  }

  byte r = serial.read();
    
  Serial.print("Got digit: ");
  Serial.println((char) r);

  if(r < '0' || r > '9')
    return -1;

  return r - '0';
}

const byte PADDING1 = 0;
const byte MSB_DIGIT = 1;
const byte MIDDLE_DIGIT = 2;
const byte LSB_DIGIT = 3;
const byte PADDING2 = 4;
const byte CHECKSUM = 5;
const byte EXPECTED_DIGITS = 6;

byte readDegreesNewSoftSerial(NewSoftSerial& serial) 
{
  byte values[EXPECTED_DIGITS];
  for(int i=0; i<EXPECTED_DIGITS; i++) {
    values[i] = readDigitValueNewSoftSerial(serial);
    if(values[i] == (byte)-1 && i != PADDING1 && i != PADDING2) 
      return 0;
  }
  int value = values[MSB_DIGIT]*100 + values[MIDDLE_DIGIT]*10 + values[LSB_DIGIT];
  
  if(values[CHECKSUM] == (value % 9)) {
    return value;
  } else {
    Serial.print("Expected checksum: ");
    Serial.print(value % 9, DEC);
    Serial.print(" got checksum: ");
    Serial.println(values[CHECKSUM], DEC);
    return 0;
  }
}

void checkNewSoftSerial(NewSoftSerial& serial)
{
  if (serial.available() > 0) {  
    switch(serial.read()) {
      case SIDEWAYS:
        Serial.println("incoming sideways command");
        servo_sideways.set_target(readDegreesNewSoftSerial(serial));
        break;
      case UPDOWN:
        Serial.println("incoming up/down command");      
        servo_updo  wn.set_target(readDegreesNewSoftSerial(serial));
        break;
      default:
        serial.println("Unrecognized value");
        serial.flush();
        break;
    }
  }
}
  
void loop() 
{ 
  checkNewSoftSerial(bluetooth);
  servo_sideways.update_pos();
  servo_updown.update_pos();
  Serial.flush();
} 
