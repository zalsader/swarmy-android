Blockly.Arduino.swarmy_events_wait = function() {
  Blockly.Arduino.definitions_['define_continue_flag'] = 'bool continueFlag = true;\n' +
    'bool getContinueFlag() {\n' +
    '  return continueFlag;\n'+
    '}\n';
  var code = 'continueFlag = false;\nwaitUntil(getContinueFlag);\n'
  return code;
};

Blockly.Arduino.swarmy_events_go_on = function() {
  Blockly.Arduino.definitions_['define_continue_flag'] = 'bool continueFlag = true;\n' +
    'bool getContinueFlag() {\n' +
    '  return continueFlag;\n'+
    '}\n';
  var code = 'continueFlag = true;\n'
  return code;
};

Blockly.Arduino.swarmy_events_say = function() {
  Blockly.Arduino.definitions_['define_my_name'] = 'String myName = "${myName}";\n';
  var value_sentence = Blockly.Arduino.valueToCode(this, 'SENTENCE', Blockly.Arduino.ORDER_ATOMIC) || '""';
  var data = Blockly.Arduino.valueToCode(this, 'DATA', Blockly.Arduino.ORDER_ATOMIC) || '""';
  var code = 'Particle.publish(String("say/") + ' + value_sentence + ', myName + ";" + ' + data + ', 60, PRIVATE);\n';
  return code;
};

Blockly.Arduino.swarmy_events_said_sentence = function() {
  Blockly.Arduino.definitions_['define_said_sentence'] = 'String saidSentence = "";\n';
  var code = 'saidSentence';
  return [code, Blockly.Arduino.ORDER_ATOMIC];
};

Blockly.Arduino.swarmy_events_carried_data = function() {
  Blockly.Arduino.definitions_['define_carried_data'] = 'String carriedData = "";\n';
  var code = 'carriedData'
  return [code, Blockly.Arduino.ORDER_ATOMIC];
};

Blockly.Arduino.swarmy_events_name = function() {
  var subject = this.getFieldValue('SUBJECT');
  var code;
  switch (subject) {
    case 'MY':
      Blockly.Arduino.definitions_['define_my_name'] = 'String myName = "${myName}";\n';
      code = 'myName';
    break;
    case 'SPEAKER':
      Blockly.Arduino.definitions_['define_speaker_name'] = 'String speakerName = "";\n';
      code = 'speakerName'
    break;
    default:
      code = 'String("")'
  }
  return [code, Blockly.Arduino.ORDER_ATOMIC];
};

Blockly.Arduino.swarmy_events_listen = function() {
  var branch = Blockly.Arduino.statementToCode(this, 'STACK');
  var value_name = Blockly.Arduino.valueToCode(this, 'NAME', Blockly.Arduino.ORDER_ATOMIC);
  var fnname = value_name.replace(/\W/g, '_');
  if (Blockly.Arduino.INFINITE_LOOP_TRAP) {
    branch = Blockly.Arduino.INFINITE_LOOP_TRAP.replace(/%1/g,
        '\'' + this.id + '\'') + branch;
  }
  Blockly.Arduino.definitions_['define_my_name'] = 'String myName = "${myName}";\n';
  var code =  'void sayHandler'+ fnname +'(const char *event, const char *data) {\n' +
      '  String saidSentence = String(event).substring(4);\n' +
      '  int endName = String(data).indexOf(\';\')' +
      '  String speakerName = String(data).substring(0, endName);\n' +
      '  String carriedData = String(data).substring(endName + 1);\n' +
      '  if (speakerName != myName) {\n'
      branch + '}\n}\n';
  code = Blockly.Arduino.scrub_(this, code);
  Blockly.Arduino.definitions_['say_handler_' + fnname] = code;
  Blockly.Arduino.setups_["setup_say_handler_" + fnname] = 'Particle.subscribe(String("say/") + ' + value_name + ', sayHandler ' + fnname + ', MY_DEVICES);\n';
  return null;
};
