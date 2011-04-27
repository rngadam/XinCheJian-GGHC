http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1204645075

Re: XBee ZNet 2.5 (formerly Series 2)
Reply #11 - 10.03.2008 at 04:52:47   rappa wrote on 06.03.2008 at 16:18:49:
According to these guys http://six.media.mit.edu:8080/6/development/daughterboards/xbee-daughterboard  It seems that you could flash your XBee in an Arduino if you connect the following pins:

RTS => 3.3 V
CTR => GND

but I haven't tried yet.

I tested this method using a Freeduino USB board (Arduino Diecimila compatible) and the XBee shield, and it works.
The signals are:

RTS (pin 16) => 3.3V (pin 1)
DTR (pin 9) => GND (pin 10)

The pins are for the XBee module, not the Arduino board.

There is no CTR signal in RS-232.  The XBEE/USB jumpers on the XBee shield must be in USB position, and you need to remove the ATMEGA168 (I didn't try with the ATMEGA168 inserted, but I think it makes total sense to remove it).

I used X-CTU 5.0.2.2 and I updated the firmware on my XBee modules (series 1) to 10A5.

Just don't forget to put the RTS and DTR signals back to normal after programming, as that configuration interferes with the normal use of the XBee modules.

Hope this helps.