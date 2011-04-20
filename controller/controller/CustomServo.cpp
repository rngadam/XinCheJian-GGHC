#include "CustomServo.h"

CustomServo::CustomServo(byte pin, byte min, byte max, byte default_value)
{
	_min = min;
	_max = max;
	_servo.attach(pin);
	set_target(default_value);
}

void CustomServo::set_target(byte pos) 
{
  if(pos >= _min && pos <= _max) {
    _target_pos = pos;
  } else {
    Serial.print("Value exceeds limits: ");
    Serial.println(pos, DEC);
  }
}

void CustomServo::update_pos() 
{
	// http://letsmakerobots.com/node/10326?page=1
	if((_last_update_ms - millis()) >= UPDATE_INTERVAL_MS) {
		// smooth the destination value
		_next_pos = _next_pos * (1.0-FILTRO) + _target_pos * FILTRO;
		// assign new position to the servo
		_servo.write(_next_pos);
		_last_update_ms = millis();
	}
}
