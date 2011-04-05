#include <NewSoftSerial.h>

// parameters: rx, tx
NewSoftSerial xbee1(2, 4); 
NewSoftSerial xbee2(3, 5);

char buffer[64];

boolean checkOK(NewSoftSerial& xbee) {
  if(readUntil(xbee, '\r', buffer) != 3)
    return false;
  if(buffer[0] != 'O' && buffer[1] != 'K')
    return false;
  return true;
}

int readUntil(NewSoftSerial& xbee, char endchar, char* buffer) {
  char c = 0;
  int i = 0;
  while(c != endchar) {
    if(xbee.available()) {
      c = xbee.read();
      buffer[i++] = c;
    }
  }
  buffer[i] = '\0';
  return i;
}

boolean enterCommandMode(NewSoftSerial& xbee)
{
  xbee.begin(9600);  
  delay(1000);
  xbee.print("+++");
  delay(1000);
  if(!checkOK(xbee))
    return false;
  return true;
}
boolean XBeeSetup(NewSoftSerial& xbee, char* nodeIdentifier) {

  if(!enterCommandMode(xbee))
    return false;
  
  xbee.print("ATNI");
  xbee.print(nodeIdentifier);
  xbee.print("\r");
  if(!checkOK(xbee))
    return false;
  
  xbee.print("ATVR\r"); // firmware version
  readUntil(xbee, '\r', buffer);
  Serial.print("ATVR Firmware version: ");
  Serial.println(buffer);

  xbee.print("ATHV\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATHV Hardware version: ");
  Serial.println(buffer);
  
  xbee.print("AT%V\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("AT%V Supply voltage: ");
  Serial.println(buffer);

  xbee.print("ATMY\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATMY Network address: ");
  Serial.println(buffer);
  
  if(!exitCommandMode())
    return false;
  return true;
}

boolean exitCommandMode(NewSoftSerial& xbee) 
{
  xbee.print("ATCN\r");
  if(!checkOK(xbee))
    return false;  
  return true;
}

boolean setDestinationNode(NewSoftSerial& xbee, char* nodeIdentifier) 
{
  if(!enterCommandMode(xbee))
    return false;
   xbee.print("ATDN");
   xbee.print(nodeIdentifier);
   xbee.print("\r");
   if(!checkOK(xbee))
     return false;
   return true;
}

void setup()  
{
  Serial.begin(9600);
  Serial.println("Welcome to the Xbee testbench!");
  
  if(!XBeeSetup(xbee1, "xbee1")) {
    Serial.println("Setup failed XBEE1");
  } 
  if(!XBeeSetup(xbee2, "xbee2")) {
    Serial.println("Setup failed XBEE2");
  } 
  Serial.println("Setting destination nodes!");
  if(!setDestinationNode(xbee1, "xbee2")) {
    Serial.println("Error setting destination node for xbee1 to xbee2");
  }
  if(!setDestinationNode(xbee2, "xbee1")) {
    Serial.println("Error setting destination node for xbee2 to xbee1");
  }
  
  Serial.println("Init completed!");
}

boolean nodeDiscover(NewSoftSerial& xbee) 
{
  if(!enterCommandMode(xbee))
    return false;
   xbee.print("ATND");
   xbee.print("\r");
   do {
     readUntil(xbee, '\r', buffer);
     Serial.println(buffer);
   } while(buffer[0] != '\r');
   return true;
}

boolean toggle = false;

void loop()
{
  nodeDiscover(xbee1);
  nodeDiscover(xbee2);
}
/*void loop()                 
{  
  // 1 second cycles:
  // 1) read whatever xbee2 has 
  // 2) write a message on xbee1
  if ((millis() / 1000) % 2 == 0)
  {
    if(!toggle) {
      toggle = true;
      Serial.print("?");
    }
    if (xbee2.available()) {
      Serial.print(xbee2.read());
    }
  } else {
    if(toggle) { // single shot
      Serial.print(">");
      xbee1.println("hello world!");
      toggle = false;
    }
  }}*/
