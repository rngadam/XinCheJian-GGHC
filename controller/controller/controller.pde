
#include <Servo.h> 
 
Servo servo_sideways; 
Servo servo_updown; 

const byte SIDEWAYS = 'S';
const byte UPDOWN = 'U';
 
const byte SIDEWAYS_MIN_POS = 20;
const byte SIDEWAYS_DEFAULT_POS = 90; 
const byte SIDEWAYS_MAX_POS = 160;
const byte UPDOWN_MIN_POS = 50;
const byte UPDOWN_DEFAULT_POS = 135; 
const byte UPDOWN_MAX_POS = 180;

void setup() 
{ 
  Serial.begin(9600);
  servo_sideways.attach(9); 
  servo_updown.attach(10); 
  servo_sideways.write(SIDEWAYS_DEFAULT_POS);
  servo_updown.write(UPDOWN_DEFAULT_POS);  
} 

void writeServoLimits(Servo& servo, byte min_value, byte max_value, byte value) 
{
  if(value >= min_value && value <= max_value) {
    servo.write(value);
  } else {
    Serial.print("Value exceeds limits: ");
    Serial.print(value, DEC);
  }
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

byte value;
byte inByte;

void loop() 
{ 
  if (Serial.available() > 0) {
    // get incoming byte:
    byte value = Serial.read();
  
    switch(value) {
      case 'S':
        value = readDegrees();
        writeServoLimits(servo_sideways, SIDEWAYS_MIN_POS, SIDEWAYS_MAX_POS, value);
        break;
      case 'U':
        value = readDegrees();
        writeServoLimits(servo_updown, UPDOWN_MIN_POS, UPDOWN_MAX_POS, value);
        break;
      default:
        Serial.println("Unrecognized value");
        break;
    }
    
  }  
} 
