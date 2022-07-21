/*
  Remote_control.ino
  To manually control towards front-panel and towards USB remote control the uCF·SPS module, 
  a Syringe Pumping System developped by the 
  Applied Electrochemistry Research Group on the University of Huelva
  
  Programmed by Juan Daniel Mozo Llamazares, José Ingacio Otero Bueno and Angel García Barrios
  February, 2014
 */

// asignacion de pines (constantes).
// give it a name:
const int botonONOFF = 2;              // boton marcha/paro
const int ledONOFF = 3;                // led rojo indicador de marcha
const int ledLOAD = 4;                 // led amarillo indicador de carga / linea de sentido de avance
const int Paso = 5;                    // linea de avance del motor (un paso)
const int pinHalf = 6;                 // linea de paso entero / medio paso
const int botonLOAD = 7;               // boton Carga
const int FinVaciar = 8;               // fin de recorrido de vaciado
const int FinLOAD = 9;                 // fin de recorrido de carga
const int Buzz = 10;                   // zumbador
const int ledPRUEBA = 13;              // led ON BOARD de Arduino
const int Veloc = A0;                  // puerto analogico de lectura de potencial (velocidad)

// otras constantes
const int Prueba = LOW;                // Modo demo (activa el pin 13 en lugar del motor)
const long debounceDelay = 50;         // guarda el tiempo de Debounce en milisec

// variables
unsigned long previousTime = 0;        // guarda el tiempo del pulso anterior en microsec
unsigned long currentTime = 0;         // guarda el tiempo actual en microsec
unsigned long interval = 500;          // guarda el intervalo entre pulsos en microsegundos
unsigned long previousTimeVolt = 0;    // guarda el tiempo del refresco de voltimetro anterior en microsec
long lastDebounceTime = 0;             // guarda el tiempo del ultimo cambio de LED
int ledState = LOW;                    // guarda el estado de los led
int runMotor = LOW;                    // guarda el estado del motor (HIGH = en marcha)
int runLOAD = LOW;                     // guarda el sentido de marcha (HIGH = Llenar = Hacia la izq)
int botonState = LOW;                  // guarda el estado de los fines de carrera
int botonState1 = LOW;                 // guarda el estado del boton ONOFF
int previousBotonState1 = LOW;         // guarda el estado anterior de los botones (para Debouncing)
int botonState2 = LOW;                 // guarda el estado del boton LOAD
int previousBotonState2 = LOW;         // guarda el estado anterior de los botones (para Debouncing)
int volt = 0;                          // guarda el valor leido del control de velocidad (0 - 1024)
char serialData = 0;                   // guarda el dato leido del puerto USB (un byte 0 - 255)
int remote = LOW;                      // guarda el tipo de control activo (para la gestion de la velocidad)

/* Conjunto de ordenes esperadas por USB
fn,n    == Freq (hl = millisec 0 to 1024)    sustituye al control de panel (higher and lower byte required)
g       == Go run                            pone en marcha el motor para vaciar
l       == Load                              pone en marcha el motor para llenar
s       == Stop                              detiene el motor
r       == Remote                            activa el control remoto (anula la lectura de velocidad)
m       == Manual                            desactiva el control remoto (activa la lectura de velocidad)
u       == Up                                aumenta la velocidad
d       == Down                              disminuye la velocidad
*/

// the setup routine runs once when you press reset:
void setup() {                
  // initialize the digital pin as an output.
  pinMode(ledONOFF, OUTPUT);  
  pinMode(ledLOAD, OUTPUT);  
  pinMode(Paso, OUTPUT);
  pinMode(pinHalf, OUTPUT);
  pinMode(Buzz, OUTPUT);
  pinMode(ledPRUEBA, OUTPUT);
  pinMode(botonONOFF, INPUT);
  pinMode(botonLOAD, INPUT);
  pinMode(FinVaciar, INPUT);
  pinMode(FinLOAD, INPUT);
  
  // initialize the serial port
  Serial.begin(9600);
}

// the loop routine runs over and over again forever:
void loop() {
  // primero lee el tiempo actual                   --------------------------------------------------------
  currentTime = micros();
  
  // check if data is waiting on serial port        --------------------------------------------------------
  while (Serial.available() > 0) {
    serialData = Serial.read();
    switch (serialData) {
      case 's':                                     // stop
        runMotor = LOW;
        runLOAD = LOW;
        Serial.print("Ok, ");                       // Handshaking Ok
        Serial.println(serialData);
        break;
      case 'm':                                     // manual mode
        remote = LOW;
        Serial.print("Ok, ");                       // Handshaking Ok
        Serial.println(serialData);
        break;
      case 'r':                                     // remote mode
        remote = HIGH;
        Serial.print("Ok, ");                       // Handshaking Ok
        Serial.println(serialData);
        break;
      case 'l':                                     // load
        runMotor = HIGH;
        runLOAD = HIGH;
        Serial.print("Ok, ");                       // Handshaking Ok
        Serial.println(serialData);
        break;
      case 'g':                                     // go run
        runMotor = HIGH;
        runLOAD = LOW;
        Serial.print("Ok, ");                       // Handshaking Ok
        Serial.println(serialData);
        break;
      case 'u':                                     // up volt (speed)
        volt += 5;
        Serial.print("Ok, ");                       // Handshaking Ok
        Serial.println(serialData);
        break;
      case 'd':                                     // down volt (speed)
        volt += -5;
        Serial.print("Ok, ");                       // Handshaking Ok
        Serial.println(serialData);
        break;
      case 'f':                                     // frequency (speed)
        int HByteRead = Serial.parseInt();          // termina de leer el mensaje
        int LByteRead = Serial.parseInt(); 
        LByteRead = constrain(LByteRead, 0, 255);   // asegura el rango de valores válidos
        HByteRead = constrain(HByteRead, 0, 3); 
        volt = LByteRead + (HByteRead * 256);       // compone los dos Bytes para calcular el voltaje
        
        Serial.print("Ok, ");                       // Handshaking Ok
        Serial.print(serialData);
        Serial.print(HByteRead,DEC);
        Serial.print(',');
        Serial.print(LByteRead,DEC);
        Serial.print(','); 
        Serial.println(volt,DEC);
        break;
    }
    int serialDatatmp = Serial.read();              // lee final de mensaje CR
  }
  
  // comprueba los actuadores del panel de control  --------------------------------------------------------
  // lee el estado del boton ONOFF------------------
  int reading = digitalRead(botonONOFF);
  if (reading != previousBotonState1) {             // con Debounce
    lastDebounceTime = millis(); }
  if ((millis() - lastDebounceTime) > debounceDelay) {
    if (reading != botonState1) {
      botonState1 = reading;  
      if (botonState1 == HIGH) {                    // cambia el valor de runMotor si se pulsa el boton
        runMotor = !runMotor; } 
      if (runLOAD == HIGH && runMotor == LOW) {     // comprueba si esta cargando y para tambien
        runLOAD = LOW; }}}
  previousBotonState1 = reading; 
  // lee el estado del boton LOAD-------------------
  reading = digitalRead(botonLOAD);
  if (reading != previousBotonState2) {             // con Debounce
    lastDebounceTime = millis(); }
  if ((millis() - lastDebounceTime) > debounceDelay) {
    if (reading != botonState2) {
      botonState2 = reading;  
      if (botonState2 == HIGH) {                    // cambia el valor de runLOAD si se pulsa el boton
        runLOAD = ! runLOAD; }
      if (runLOAD == HIGH) {                        // comprueba si esta cargando y enciende el motor tambien
        runMotor = HIGH; }}}
  previousBotonState2 = reading; 
  
  // comprueba los fines de carrera                 ---------------------------------------------------------
  // lee el estado del fin de carrera de VACIADO----
  botonState = digitalRead(FinVaciar);
  // el motor debe estar funcionando y vaciando para comprobar el fin de carrera
  if (botonState == HIGH && runMotor == HIGH && runLOAD == LOW) {       
    tone(Buzz,250,500);                             // alarma sonora
    runMotor = LOW; }                               // apaga el motor si se pulsa el fin de carrera


  // lee el estado del fin de carrera de CARGA------
  botonState = digitalRead(FinLOAD);
  // el motor debe estar funcionando y cargando para comprobar el fin de carrera
  if (botonState == HIGH && runMotor == HIGH && runLOAD == HIGH) {      
    tone(Buzz,523,250);                             // alarma sonora
    runLOAD = LOW;                                  // apaga el motor si se pulsa el fin de carrera
    runMotor = LOW; }

  digitalWrite(ledONOFF, runMotor);                 // refleja el estado de marcha en el LED
  digitalWrite(ledLOAD, runLOAD);                   // refleja el estado de Carga en el LED
  digitalWrite(pinHalf, !runLOAD);                  // medio paso para vaciar y paso entero para cargar
  
  // calcula la velocidad de giro del motor         ---------------------------------------------------------
  if (remote == LOW) {
    volt = analogRead(Veloc);                       // lee el valor del potenciometro
    // comprueba el tiempo de refresco de voltaje (cada 500 ms)
    if (currentTime - previousTimeVolt >= 500000L) {
      previousTimeVolt = currentTime;
      Serial.print('v');                            // pone el valor en el puerto USB para sincronizacion
      Serial.println(volt); }}    
    
  if (Prueba == LOW) { 
    interval = (1025 - volt) * 250L;                // calcula el intervalo entre pasos de motor en microsec 
    if (runLOAD == HIGH) interval = 3500L; }        // marcha rápida para cargar 
  else {                                            // en modo demo calcula intervalos más grandes (LED)
    interval = (1024 - volt) * 1000L; 
    if (runLOAD == HIGH) interval = 25000L; }

  // comprueba el tiempo que ha pasado y si es el intervalo fijado da un paso de motor ----------------------
  if (currentTime - previousTime >= interval && runMotor == HIGH) {
    previousTime = currentTime;                     // guarda el tiempo actual
    if (Prueba == HIGH) {
      ledState = !ledState;                         // turn the LED OFF and ON every interval
      digitalWrite(ledPRUEBA, ledState); }
    else {
      digitalWrite(Paso,HIGH);                      // set a pulse on Paso (Motor goes on)
      delay(1);
      digitalWrite(Paso,LOW); }
  }
}

