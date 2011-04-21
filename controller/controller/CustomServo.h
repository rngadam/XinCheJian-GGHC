#ifndef CUSTOM_SERVO_H
#define CUSTOM_SERVO_H

#include <Servo.h>
#include "WProgram.h"

const byte UPDATE_INTERVAL_MS = 15;
const byte ARRAY_SIZE = 12;
const float percentage[ARRAY_SIZE] = {0.01, 0.02, 0.04, 0.08, 0.16, 0.19, 0.19, 0.16, 0.08, 0.04, 0.02, 0.01}; // = 50% * 2 = 100%

class CustomServo
{
public:
  CustomServo(byte pin, byte min_value, byte max_value, byte default_value);
  void set_target(byte pos);
  void update_pos();
  void setup();
private:
  byte _min;
  byte _max;
  byte _target_pos;
  byte _next_pos;
  byte _last_update_ms;
  byte _initial_pos;
  byte _array_ptr;
  byte _default_value;
  byte _pin;
  Servo _servo;
};
#endif
