Blockly.Arduino.swarmy_io_led = function() {
  var dropdown_stat = this.getFieldValue('STAT');
  Blockly.Arduino.setups_['setup_output_D7'] = 'pinMode(D7, OUTPUT);';
  var code = 'digitalWrite(D7, ' + dropdown_stat + ');\n'
  return code;
};

var PWM1 = "D0";
var PWM2 = "D5";
var FOR1 = "D1";
var BAC1 = "D2";
var FOR2 = "D3";
var BAC2 = "D4";

Blockly.Arduino.swarmy_io_move_all = function() {
  var dropdown_direction = this.getFieldValue('DIRECTION') || "STOP";
//  var speed = 127; // TODO use speed
  Blockly.Arduino.setups_["setup_motor"] = "pinMode("+FOR1+",OUTPUT);\n"+
  "  pinMode("+FOR2+",OUTPUT);\n"+
  "  pinMode("+BAC1+",OUTPUT);\n"+
  "  pinMode("+BAC2+",OUTPUT);\n"+
  "  pinMode("+PWM1+",OUTPUT);\n"+
  "  pinMode("+PWM2+",OUTPUT);\n"+
  "  digitalWrite("+PWM1+",HIGH);\n"+ // TODO use PWM
  "  digitalWrite("+PWM2+",HIGH);\n";
  var code = "";
  switch (dropdown_direction) {
    case "FORWARD":
      Blockly.Arduino.definitions_['define_forward'] = "void forward()\n"+
        "{\n"+
        "  digitalWrite("+FOR1+",HIGH);\n"+
        "  digitalWrite("+BAC1+",LOW);\n"+
        "  digitalWrite("+FOR2+",HIGH);\n"+
        "  digitalWrite("+BAC2+",LOW);\n"+
        "}\n";
      code="forward();\n";
      break;
    case "BACKWARD":
      Blockly.Arduino.definitions_['define_backward'] = "void backward()\n"+
        "{\n"+
        "  digitalWrite("+FOR1+",LOW);\n"+
        "  digitalWrite("+BAC1+",HIGH);\n"+
        "  digitalWrite("+FOR2+",LOW);\n"+
        "  digitalWrite("+BAC2+",HIGH);\n"+
        "}\n";
      code="backward();\n";
      break;
    case "RIGHT":
      Blockly.Arduino.definitions_['define_right'] = "void right()\n"+
        "{\n"+
        "  digitalWrite("+FOR1+",HIGH);\n"+
        "  digitalWrite("+BAC1+",LOW);\n"+
        "  digitalWrite("+FOR2+",LOW);\n"+
        "  digitalWrite("+BAC2+",HIGH);\n"+
        "}\n";
      code="right();\n";
      break;
    case "LEFT":
      Blockly.Arduino.definitions_['define_left'] = "void left()\n"+
        "{\n"+
        "  digitalWrite("+FOR1+",LOW);\n"+
        "  digitalWrite("+BAC1+",HIGH);\n"+
        "  digitalWrite("+FOR2+",HIGH);\n"+
        "  digitalWrite("+BAC2+",LOW);\n"+
        "}\n";
      code="left();\n";
      break;
    case "stop":
    default:
      Blockly.Arduino.definitions_['define_stop'] = "void stop()\n"+
        "{\n"+
        "  digitalWrite("+FOR1+",LOW);\n"+
        "  digitalWrite("+BAC1+",LOW);\n"+
        "  digitalWrite("+FOR2+",LOW);\n"+
        "  digitalWrite("+BAC2+",LOW);\n"+
        "}\n";
      code="stop();\n";
  }
  return code;
};

Blockly.Arduino.swarmy_io_move = Blockly.Arduino.swarmy_io_move_all;
Blockly.Arduino.swarmy_io_turn = Blockly.Arduino.swarmy_io_move_all;
Blockly.Arduino.swarmy_io_stop = Blockly.Arduino.swarmy_io_move_all;

Blockly.Arduino['swarmy_io_sensor_event'] = function() {
  var dropdown_action = this.getFieldValue('ACTION');
  var stack = Blockly.Arduino.statementToCode(this, 'STACK');
  switch(dropdown_action) {
    case "SOUND":
    case "MOVEMENT":
    case "OBSTACLE":
    case "FIRE":
    case "LIGHT":
    case "DARKNESS":
      Blockly.Arduino.setups_["setup_sensor"] = "pinMode(A1, INPUT_PULLUP);\n"+
          "  pinMode(A0, OUTPUT);\n"+
          "  pinMode(A2, OUTPUT);\n"+
          "  digitalWrite(A0, LOW);\n"+
          "  digitalWrite(A2, HIGH);\n"+
          "  attachInterrupt(A1, sensorEventISR, CHANGE);\n";
      Blockly.Arduino.setups_["setup_sensor_no_int"] = undefined;
      Blockly.Arduino.definitions_['define_sensor'] = "Timer sensorTimer(50, sensorEventCallback, true);\n"+
      "void sensorEventISR() {\n"+
      "  sensorTimer.startFromISR();\n"+
      "  detachInterrupt(A1);\n"+
      "}\n"+
      "void sensorEventCallback() {\n"+
      "  " + stack + "\n"+
      "  attachInterrupt(A1, sensorEventISR, CHANGE);\n"+
      "}\n";
    break;
    default:
  } 
  return null;
};

Blockly.Arduino['swarmy_io_sensor_read'] = function() {
  var dropdown_sensor = this.getFieldValue('SENSOR');
  if (!Blockly.Arduino.setups_["setup_sensor"]) {
    Blockly.Arduino.setups_["setup_sensor_no_int"] = "pinMode(A1, INPUT_PULLUP);\n"+
        "  pinMode(A0, OUTPUT);\n"+
        "  pinMode(A2, OUTPUT);\n"+
        "  digitalWrite(A0, LOW);\n"+
        "  digitalWrite(A2, HIGH);\n";
  }
  switch(dropdown_sensor) {
    case "SOUND":
    case "MOVEMENT":
    case "OBSTACLE":
    case "FIRE":
    case "LIGHT":
    case "DARKNESS":
      return ['digitalRead(A1)', Blockly.Arduino.ORDER_NONE]
    break;
    default:
  }
};

Blockly.Arduino.swarmy_io_laser = function() {
  var dropdown_stat = this.getFieldValue('STAT');
  Blockly.Arduino.setups_['setup_laser'] = "pinMode(A3, OUTPUT);\n"+
   "  pinMode(A4, OUTPUT);\n"+
   "  pinMode(A5, OUTPUT);\n"+
   "  digitalWrite(A5, LOW);\n"+
   "  digitalWrite(A4, HIGH);\n";
  var code = 'digitalWrite(A3, ' + dropdown_stat + ');\n'
  return code;
};
