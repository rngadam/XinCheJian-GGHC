#include "CustomServo.h"

CustomServo::CustomServo(byte pin, byte min_value, byte max_value, byte default_value)
{
  _min = min_value;
  _max = max_value;
  _next_pos = default_value;
  _target_pos = default_value;
  _default_value = default_value;
  _pin = pin;
}

void CustomServo::setup() 
{
  _servo = Servo();
  Serial.print("Attaching servo to pin ");
  Serial.print(_pin);
  Serial.print(" and writing: ");
  Serial.println(_default_value);
  _servo.attach(_pin);
  _servo.write(_default_value);  
}

void exceeds(byte pos, byte limit) {
  Serial.print(pos, DEC);
  Serial.print(" value exceeds limits: ");
  Serial.println(limit, DEC);
}

void CustomServo::set_target(byte pos) 
{
  if(pos < _min) {
    exceeds(pos, _min);
  } else if(pos > _max) {
    exceeds(pos, _max);    
  } else {
    _initial_pos = _next_pos;
    _target_pos = pos;
    _array_ptr = 0;
  } 
}

void CustomServo::update_pos() 
{
  // if we've reached destination, nothing left to do
  if(_next_pos == _target_pos)
    return;
    
  // is it time to update yet?
  if((millis() - _last_update_ms) >= UPDATE_INTERVAL_MS) {
    // we've reached final deceleration position, put it exactly to target pos
    if(_array_ptr == ARRAY_SIZE) {
      _next_pos = _target_pos;
    } else {
      // we add a positive or negative value to _next_pos
      // if we're accelerating up to 50% of the distance, the delta is increasingly larger (larger acceleration)
      // if we're decelerating from remaining 50% of the distance, the delta is increasingly smaller (smaller acceleration)
      _next_pos = _next_pos + ((_target_pos - _initial_pos) * percentage[_array_ptr++]);
    }
    
    // assign new position to the servo
    Serial.print("Next intermediate position: ");
    Serial.print(_next_pos, DEC);  
    Serial.print(" to reach: ");
    Serial.println(_target_pos, DEC);    
    
    _servo.write(_next_pos);

    Serial.print("Last write: ");
    Serial.println(_servo.read(), DEC);

    _last_update_ms = millis();
  }
}

