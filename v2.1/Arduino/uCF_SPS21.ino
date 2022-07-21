/*
  uCF_SPS2.ino
  To manually control towards front-panel and towards USB remote control the uCF·SPS module, 
  a Syringe Pumping System developped by 
  the Applied Electrochemistry Research Group and
  the Open Source Hardware Group on the University of Huelva
  
  Programmed by Juan Daniel Mozo Llamazares, José Ingacio Otero Bueno and Angel García Barrios
  February, 2014
  
  Version #2.1 programmed by JDMozo - January, 2021
  To includes the DRV8255 stepper motor driver and flow control by valve-box Jigsaw-FIA
  Fixes some comunication issues to optimize the Java console synchronizarion 
    (all messages have the same structure)
 */

// pin assignation (constants).
// give it a name:
const int ONOFFbutton = A1;            // button Run/Stop
const int ledONOFF = A2;               // Red LED Start flag
const int ledLOAD = 2;                 // Yellow LED LOAD flag / R-L motion pin
const int Step = 3;                    // motor clock pin (one step)
const int pinHalf = 4;                 // full/half step pin
const int LOADbutton = 5;              // LOAD button
const int limitDISP = 6;               // Dispense limit switch
const int limitLOAD = 7;                 // Load limit switch
const int Buzz = 8;                    // buzzer
const int boardLED = 13;               // Arduino ON BOARD LED
const int Speed = A0;                  // analogical potential read pin (speed)
const int V1 = 9;                      // valve 1
const int V2 = 10;                     // valve 2
const int V3 = 11;                     // valve 3
const int V4 = 12;                     // valve 4
const int V5 = 13;                     // valve 5 (injection valve)


// other constants
const int DEMO = LOW;                  // Demo mode (blink pin 13 LED instead motor runs)
const long debounceDelay = 50;         // stores Debounce time in millisec

// variables
unsigned long previousTime = 0;        // stores previous steptime in microsec
unsigned long currentTime = 0;         // stores actual time in microsec
unsigned long interval = 500;          // stores interval-between-steps in microsecs
unsigned long previousTimeVolt = 0;    // stores voltmeter' previous refresh time in microsec
long lastDebounceTime = 0;             // stores last LED turnover time
int ledState = LOW;                    // stores led's state
int runMotor = LOW;                    // stores motor's state (HIGH = run)
int runLOAD = LOW;                     // stores running direction (HIGH = LOAD = LEFT)
int buttonState = LOW;                 // stores limit switches state
int buttonState1 = LOW;                // stores ONOFF button state
int previousButtonState1 = LOW;        // stores ONOFF button previous state (to Debounce)
int buttonState2 = LOW;                // stores LOAD button state
int previousButtonState2 = LOW;        // stores LOAD button previous state (to Debounce)
int volt = 0;                          // stores analog speed setting (0 - 1024)
char serialData = 0;                   // stores data read from USB (one byte 0 - 255)
int remote = LOW;                      // stores active control mode (to speed's question)

/* USB command set: 
 * (input) for remote actuation
 *    fn      == Freq (n = millisec 0 to 1023)     replace manual panel control 
 *    g       == Go run                            start motor to difuse
 *    l       == Load                              start motor to withdraw
 *    s       == Stop                              stop motor
 *    r       == Remote                            enable remote control (disable speed reading)
 *    m       == Manual                            disable remote control (enable speed reading)
 *    u       == Up                                increases speed
 *    d       == Down                              decreases speed
 *    wn      == Valves                            set all valves at once (n = 0 to 31)
 * (output) for sync with Java GUI
 *    vn      == Freq (n = millisec 0 to 1023)     send freq value
 *    an      == Button ONOFF (n = 0, 1)           console button can act as panic button
 *    bn      == Button LOAD (n = 0, 1)            console button acting overight GUI
 *    xn      == Limit switch (n= 0 to 4294967295) DISPENSE Limitswitch detection (n = actual time)
 *    yn      == Limit switch (n= 0 to 4294967295) LOAD Limitswitch detection (n = actual time)
*/

// the setup routine runs once when you press reset:
void setup() {                
  // initialize the digital pins as input or output.
  pinMode(ledONOFF, OUTPUT);  
  pinMode(ledLOAD, OUTPUT);  
  pinMode(Step, OUTPUT);
  pinMode(pinHalf, OUTPUT);
  pinMode(Buzz, OUTPUT);
  pinMode(boardLED, OUTPUT);
  pinMode(ONOFFbutton, INPUT);
  pinMode(LOADbutton, INPUT);
  pinMode(limitDISP, INPUT);
  pinMode(limitLOAD, INPUT);
  pinMode(V1, OUTPUT);
  pinMode(V2, OUTPUT);
  pinMode(V3, OUTPUT);
  pinMode(V4, OUTPUT);
  pinMode(V5, OUTPUT);
    
  // initialize the serial port
  Serial.begin(9600);
}

// the loop routine runs over and over again forever:
void loop() {
  // first read the actual time                     --------------------------------------------------------
  currentTime = micros();
  
  // check if data is waiting on serial port        --------------------------------------------------------
  while (Serial.available() > 0) {
    serialData = Serial.read();
    switch (serialData) {
      case 's':                                      // stop
        runMotor = LOW;
        runLOAD = LOW;
        Serial.print("Ok, ");                        // Handshaking Ok
        Serial.print(serialData);
        Serial.println(millis(),DEC);                // to sinc with computer app
        break;
      case 'm':                                      // manual mode
        remote = LOW;
        Serial.print("Ok, ");                        // Handshaking Ok
        Serial.println(serialData);
        break;
      case 'r':                                      // remote mode
        remote = HIGH;
        Serial.print("Ok, ");                        // Handshaking Ok
        Serial.println(serialData);
        break;
      case 'l':                                      // load
        runMotor = HIGH;
        runLOAD = HIGH;
        Serial.print("Ok, ");                        // Handshaking Ok
        Serial.println(serialData);
        break;
      case 'g':                                      // go run
        runMotor = HIGH;
        runLOAD = LOW;
        Serial.print("Ok, ");                        // Handshaking Ok
        Serial.print(serialData);
        Serial.println(millis(),DEC);                // to sinc with computer app
        break;
      case 'u':                                      // up volt (speed)
        volt += 5;
        if (volt > 1023) {volt = 1023;}              // upper limit 
        Serial.print("Ok, ");                        // Handshaking Ok
        Serial.print(serialData);
        Serial.println(volt, DEC);
        break;
      case 'd':                                      // down volt (speed)
        volt += -5;
        if (volt < 0) {volt = 0;}                    // lower limit 
        Serial.print("Ok, ");                        // Handshaking Ok
        Serial.print(serialData);
        Serial.println(volt, DEC);
        break;
      case 'f':                                      // frequency (speed)
        volt = Serial.parseInt();                    // read the message completely
        volt = constrain(volt, 0, 1023);             // avoid out-of-range values
        Serial.print("Ok, ");                        // Handshaking Ok
        Serial.print(serialData);
        Serial.println(volt, DEC);
        break;
      case 'w':                                      // sets valves state
        int valveState = Serial.parseInt();          // read all valves state
        valveState = constrain(valveState, 0, 31);   // avoid out-of-range values
        for (int i = 0; i <= 4; i++) {
          if (bitRead(valveState, i) == 1){          // if bit = 1
            digitalWrite(i + 9, HIGH);}
          else {                                     // if bit = 0
            digitalWrite(i + 9, LOW);}}
        Serial.print("Ok, ");                        // Handshaking Ok
        Serial.print(serialData);
        Serial.println(valveState);        
        break;
    }
    int serialDatatmp = Serial.read();               // empty serial port (CR)
  }
  
  // check control panel knobs                       -------------------------------------------------------
  // check ONOFF button setting ---------------------
  int reading = digitalRead(ONOFFbutton);
  if (reading != previousButtonState1) {             // with Debounce
    lastDebounceTime = millis(); }
  if ((millis() - lastDebounceTime) > debounceDelay) {
    if (reading != buttonState1) {
      buttonState1 = reading;  
      if (buttonState1 == HIGH) {                    // changes the runMotor value if button is pushed
        runMotor = !runMotor; } 
      if (runLOAD == HIGH && runMotor == LOW) {      // if loading and run stop all
        runLOAD = LOW; }
      Serial.print("Ok, a");                         // Handshaking Ok (to sync with Java GUI)
      Serial.println(runMotor); }}       
  previousButtonState1 = reading;
 
  // check LOAD button setting ----------------------
  reading = digitalRead(LOADbutton);
  if (reading != previousButtonState2) {             // with Debounce
    lastDebounceTime = millis(); }
  if ((millis() - lastDebounceTime) > debounceDelay) {
    if (reading != buttonState2) {
      buttonState2 = reading;  
      if (buttonState2 == HIGH) {                    // changes the runLOAD value if button is pushed
        runLOAD = !runLOAD; }
      if (runLOAD == HIGH) {                         // check if goes LOAD and start the motor too
        runMotor = HIGH; }
      Serial.print("Ok, b");                         // Handshaking Ok (to sync with Java GUI)
      Serial.println(runLOAD);}}        
  previousButtonState2 = reading; 
  
  // check the limit switches                        -------------------------------------------------------
  // check the DISPENSE limit switch setting --------
  buttonState = digitalRead(limitDISP);
  // motor must be running and dispensing to check the limit switch
  if (buttonState == HIGH && runMotor == HIGH && runLOAD == LOW) {       
    tone(Buzz,250,500);                              // audio alarm
    runMotor = LOW;                                  // if limit switch is pressed stop the motion
    Serial.print("Ok, x");
    Serial.println(millis(),DEC);}                   // to sync with computer app

  // check the LOAD limit switch setting ------------
  buttonState = digitalRead(limitLOAD);
  // motor must be running and loading to check the limit switch
  if (buttonState == HIGH && runMotor == HIGH && runLOAD == HIGH) {      
    tone(Buzz,523,250);                             // audio alarm
    runLOAD = LOW;                                  // if limit switch is pressed stops and load
    runMotor = LOW; 
    Serial.print("Ok, y");                          
    Serial.println(millis(),DEC);}                  // to sinc with computer app

  digitalWrite(ledONOFF, runMotor);                 // write RUN setting on LED
  digitalWrite(ledLOAD, runLOAD);                   // write LOAD setting on LED and set direction
  digitalWrite(pinHalf, !runLOAD);                  // sets HALF step to dispense and FULL to load
  
  // calculate motor rotation speed                 --------------------------------------------------------
  if (remote == LOW) {                              // manual mode
    volt = analogRead(Speed);                       // read potentiometer value each 500 millisec
    // check if refresh time is passed ( 500 ms)
    if (currentTime - previousTimeVolt >= 500000L) {
      previousTimeVolt = currentTime;
      Serial.print("Ok, v");                        // write in serial USB to PC synchronization
      Serial.println(volt, DEC);}}    
    
  if (DEMO == LOW) { 
    interval = (1024 - volt) * 250L;                // calculates motor steps interval in microsec 
    if (runLOAD == HIGH) interval = 3500L; }        // full speed to load 
  else {                                            // in DEMO mode calculates bigger intervals (LED)
    interval = (1024 - volt) * 1000L; 
    if (runLOAD == HIGH) interval = 25000L; }

  // check if elapsed time is bigger than calculated interval to motor step out ----------------------------
  if (currentTime - previousTime >= interval && runMotor == HIGH) {
    previousTime = currentTime;                     // stores actual time (reset time
    if (DEMO == HIGH) {
      ledState = !ledState;                         // turn the LED OFF and ON every interval
      digitalWrite(boardLED, ledState); }
    else {
      digitalWrite(Step,HIGH);                      // set a pulse on Step (Motor goes on)
      delay(1);
      digitalWrite(Step,LOW); }
  }
}
