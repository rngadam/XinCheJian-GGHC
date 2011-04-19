import processing.serial.*;

import cc.arduino.*;

Arduino arduino;

void setup() {
  size(512, 200);
  println(Arduino.list());
  arduino = new Arduino(this, Arduino.list()[0], 57600);
  arduino.pinMode(9, Arduino.SERVO);
  arduino.pinMode(10, Arduino.SERVO);  
}

void draw() {
  background(constrain(mouseX / 2, 0, 180));
  int value = constrain(mouseX / 2, 0, 180);
  arduino.analogWrite(9, value);
  arduino.analogWrite(10, value); 
  delay(1000);
  println(value);
}
