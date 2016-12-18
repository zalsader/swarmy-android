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
  Blockly.Arduino.definitions_['define_my_name'] = 'String myName = "${myName}";';
  var value_sentence = Blockly.Arduino.valueToCode(this, 'SENTENCE', Blockly.Arduino.ORDER_ATOMIC);
  var code = 'Particle.publish(String("say/") + myName, ' + value_sentence + ', 60, PRIVATE);\n';
  return code;
};

Blockly.Arduino.swarmy_events_said_sentence = function() {
  Blockly.Arduino.definitions_['define_said_sentence'] = 'String saidSentence = "";';
  var code = 'saidSentence';
  return [code, Blockly.Arduino.ORDER_ATOMIC];
};

Blockly.Arduino.swarmy_events_name = function() {
  var subject = this.getFieldValue('SUBJECT');
  var code;
  switch (subject) {
    case 'MY':
      Blockly.Arduino.definitions_['define_my_name'] = 'String myName = "${myName}";';
      code = 'myName';
    break;
    case 'SPEAKER':
      Blockly.Arduino.definitions_['define_speaker_name'] = 'String speakerName = "";';
      code = 'speakerName'
    break;
    default:
      code = 'String("")'
  }
  return [code, Blockly.Arduino.ORDER_ATOMIC];
};

Blockly.Arduino.swarmy_events_listen = function() {
  var branch = Blockly.Arduino.statementToCode(this, 'STACK');
  if (Blockly.Arduino.INFINITE_LOOP_TRAP) {
    branch = Blockly.Arduino.INFINITE_LOOP_TRAP.replace(/%1/g,
        '\'' + this.id + '\'') + branch;
  }
  var code =  'void sayHandler(const char *event, const char *saidSentence) {\n' +
      '  String speakerName = String(event).substring(4);\n' +
      branch + '}\n';
  code = Blockly.Arduino.scrub_(this, code);
  Blockly.Arduino.definitions_['say_handler'] = code;
  Blockly.Arduino.setups_["setup_say_handler"] = 'Particle.subscribe("say/", sayHandler, MY_DEVICES);\n';
  return null;
};
