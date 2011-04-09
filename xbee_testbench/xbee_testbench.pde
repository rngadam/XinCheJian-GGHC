#include <NewSoftSerial.h>

/*

You need at least one device acting as a coordination

Can do this by connection Xbee to USB

http://www.digi.com/xctu

See:

http://electronics.stackexchange.com/questions/2668/using-x-ctu-to-setup-xbee-modem
http://www.arduino.cc/en/Main/ArduinoXbeeShield

Page 32

64-Bit Addressing (Transparent)
To address a node by its 64-bit address, the destination address must be set to match the 64-bit 
address of the remote. In the AT firmware, the DH and DL commands set the destination 64-bit 
address. In the API firmware, the destination 64-bit address is set in the ZigBee Transmit Request 
frame. 
To send a packet to an RF module using its 64-bit Address (Transparent Mode)
Set the DH (Destination Address High) and DL (Destination Address Low) parameters of the 
source node to match the 64-bit Address (SH (Serial Number High) and SL (Serial Number Low) 
parameters) of the destination node
Since the ZigBee protocol relies on the 16-bit network address for routing, the 64-bit address 
must be converted into a 16-bit network address prior to transmitting data. If a module does not 
know the 16-bit network address for a given 64-bit address, it will transmit a broadcast network 
address Discovery command. The module with a matching 64-bit address will transmit its 16-bit 
network address back. Once the network address is discovered, the data will be transmitted.
The modules maintain a table that can store up to seven 64-bit addresses and their corresponding 16-bit addresses
*/

// parameters: rx, tx
NewSoftSerial xbee1(2, 4); 
NewSoftSerial xbee2(3, 5);

char buffer[64];

boolean checkOK(NewSoftSerial& xbee) {
  readUntil(xbee, '\r', buffer);
  if(buffer[0] != 'O' && buffer[1] != 'K') {
    Serial.print("Expected OK, got: ");
    Serial.println(buffer);
    return false;
  }
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
  delay(1000);
  xbee.print("+++");
  delay(1000);
  if(!checkOK(xbee)) 
    return false;

  return true;
}

void enterCommandModeChecked(NewSoftSerial& xbee) 
{
  if(!enterCommandMode(xbee)) {
    Serial.println("Failed entering command mode");
  }
}

void exitCommandModeChecked(NewSoftSerial& xbee)
{
  if(!exitCommandMode(xbee)) {
    Serial.println("Failed exiting command mode");    
  }
}

boolean XBeeSetupNetworkDevice(NewSoftSerial& xbee, char* nodeIdentifier)
{  
  Serial.println("Setting up NI");
  xbee.print("ATNI");
  xbee.print(nodeIdentifier);
  xbee.print("\r");
  if(!checkOK(xbee))
    return false;

  Serial.println("Setting up PAN");
  xbee.print("ATPAN");
  xbee.print(0xFFFF);
  xbee.print("\r");
  if(!checkOK(xbee))
    return false;
  
  return true;
}

boolean XBeeInfo(NewSoftSerial& xbee) 
{
  xbee.print("ATVR\r"); // firmware version
  readUntil(xbee, '\r', buffer);
  Serial.print("ATVR Firmware: ");
  Serial.println(buffer);

  xbee.print("ATHV\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATHV Hardware: ");
  Serial.println(buffer);
  
  xbee.print("AT%V\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("AT%V Supply voltage: ");
  Serial.println(buffer);

  xbee.print("ATMY\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATMY Network addr: ");
  Serial.println(buffer);
 
  xbee.print("ATPAN\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATPAN PAN: ");
  Serial.println(buffer);
  
  xbee.print("ATDB\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATDB Last RF packet strength: ");
  Serial.println(buffer);

  xbee.print("ATOP\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATOP Operating PAN: ");
  Serial.println(buffer);
 
  xbee.print("ATCH\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATCH Operating Channel: ");
  Serial.println(buffer);

  xbee.print("ATSH\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATSH High: ");
  Serial.println(buffer);

  xbee.print("ATSL\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATSL Low: ");
  Serial.println(buffer);

  xbee.print("ATDH\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATDH Destination High: ");
  Serial.println(buffer);

  xbee.print("ATDL\r");
  readUntil(xbee, '\r', buffer);
  Serial.print("ATDH Destination Low: ");
  Serial.println(buffer);  
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

boolean SetXBeeValue(NewSoftSerial& xbee, char* type, char* value) 
{
  Serial.print("Setting up ");
  Serial.println(type);
  xbee.print("AT");
  xbee.print(type);
  xbee.print(value);
  xbee.print("\r");
  if(!checkOK(xbee))
    return false;  
  return true;
}

void setup()  
{
  Serial.begin(9600);
  Serial.println("Welcome to the Xbee testbench!");
  
  xbee1.begin(9600);  
  xbee2.begin(9600);  
  
  /*Serial.println("Setup of XBEE1");
  enterCommandModeChecked(xbee1);
  if(!XBeeSetup(xbee1, "xbee1")) {
    Serial.println("Setup failed XBEE1");
  } 
  exitCommandModeChecked(xbee1);
  
  Serial.println("Setup of XBEE2");
  enterCommandModeChecked(xbee2);
  if(!XBeeSetup(xbee2, "xbee2")) {
    Serial.println("Setup failed XBEE2");
  } 
  exitCommandModeChecked(xbee2);*/
  
  enterCommandModeChecked(xbee1);
  //SetXBeeValue(xbee1, "DH", "13A200");
  //SetXBeeValue(xbee1, "DL", "406F215B");
  SetXBeeValue(xbee1, "DH", "0");
  SetXBeeValue(xbee1, "DL", "FFFF");
  exitCommandModeChecked(xbee1);

  enterCommandModeChecked(xbee2);
  //SetXBeeValue(xbee2, "DH", "13A200");
  //SetXBeeValue(xbee2, "DL", "406CBAFC");
  SetXBeeValue(xbee2, "DH", "0");
  SetXBeeValue(xbee2, "DL", "FFFF");
  exitCommandModeChecked(xbee2);
  
  /*Serial.println("Setting destination nodes!");
  if(!setDestinationNode(xbee1, "xbee2")) {
    Serial.println("Error setting destination node for xbee1 to xbee2");
  }
  if(!setDestinationNode(xbee2, "xbee1")) {
    Serial.println("Error setting destination node for xbee2 to xbee1");
  }*/
  
  /*if(!nodeDiscover(xbee1)) {
    Serial.println("xbee1 unable to do node discovery");
  }
  if(!nodeDiscover(xbee2)) {
    Serial.println("xbee2 unable to do node discovery");
  }*/
  
  Serial.println("Init completed!");

  Serial.println(">>>>> Info of XBEE1");
  enterCommandModeChecked(xbee1);
  XBeeInfo(xbee1);
  exitCommandModeChecked(xbee1);

  Serial.println(">>>>>  Info of XBEE2");
  enterCommandModeChecked(xbee2);
  XBeeInfo(xbee2);
  exitCommandModeChecked(xbee2);
  Serial.println(".");
}

boolean nodeDiscover(NewSoftSerial& xbee) 
{
  Serial.println("Doing node discovery");
  if(!enterCommandMode(xbee))
    return false;
   xbee.print("ATND");
   xbee.print("\r");
   do {
     Serial.print("D: ");
     readUntil(xbee, '\r', buffer);
     Serial.println(buffer);
   } while(buffer[0] != '\r');
   
   return exitCommandMode(xbee);
}

boolean toggle = false;

void loop()                 
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
     Serial.print(xbee2.read());
     if (xbee2.available()) {
      Serial.print(xbee2.read());
    }
  } else {
    if(toggle) { // single shot

      Serial.print(">");
      xbee1.println("hello world!");
      toggle = false;
    }
  }}
