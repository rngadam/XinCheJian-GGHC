void setup()
{
  Serial.begin(9600);
  Serial.println("Starting!");
}

void loop()
{
  Serial.println("One time around the loop");
  delay(1000);
}
