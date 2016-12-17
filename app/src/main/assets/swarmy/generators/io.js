Blockly.Arduino.swarmy_io_led = function() {
  var dropdown_stat = this.getFieldValue('STAT');
  Blockly.Arduino.setups_['setup_output_D7'] = 'pinMode(D7, OUTPUT);';
  var code = 'digitalWrite(D7, ' + dropdown_stat + ');\n'
  return code;
};

var PWM1 = "D5";
var PWM2 = "D0";
var FOR1 = "D4";
var BAC1 = "D3";
var FOR2 = "D2";
var BAC2 = "D1";

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
        "  digitalWrite("+FOR1+",LOW);\n"+
        "  digitalWrite("+BAC1+",HIGH);\n"+
        "  digitalWrite("+FOR2+",HIGH);\n"+
        "  digitalWrite("+BAC2+",LOW);\n"+
        "}\n";
      code="right();\n";
      break;
    case "LEFT":
      Blockly.Arduino.definitions_['define_left'] = "void left()\n"+
        "{\n"+
        "  digitalWrite("+FOR1+",HIGH);\n"+
        "  digitalWrite("+BAC1+",LOW);\n"+
        "  digitalWrite("+FOR2+",LOW);\n"+
        "  digitalWrite("+BAC2+",HIGH);\n"+
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
