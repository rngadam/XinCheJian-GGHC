
#ifndef CustomServo_h
#define CustomServo_h

#include <Servo.h
#include "WProgram.h"

// the filter to be aplied
// this value will be multiplied by "d0" and added to "d0_sh"
const float FILTRO = 0.05; // 0.01 to 1.0
const byte UPDATE_INTERVAL_MS = 25;

class CustomServo
{
public:
  CustomServo(byte pin, byte min, byte max, byte default_value);
  void set_target(byte pos);
  void update_pos();
private:
  byte _min;
  byte _max;
  byte _target_pos;
  byte _next_pos;
  int _last_update_ms;
  Servo _servo;
};
#endif

