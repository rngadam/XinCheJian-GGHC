#include <NewSoftSerial.h>

NewSoftSerial bluetooth(8, 7);

void setup() 
{ 
  Serial.begin(115200);  
  bluetooth.begin(115200);
} 

void loop()
{
  if(bluetooth.available()) {
    Serial.println(bluetooth.read());
  }  
}
