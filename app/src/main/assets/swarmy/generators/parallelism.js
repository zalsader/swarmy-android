Blockly.Arduino['swarmy_parallelism_my_number'] = function() {
  if (!Blockly.Arduino.definitions_['define_turns']) {
    Blockly.Arduino.definitions_['define_my_number'] = '#define MY_NUMBER ${myNumber}\n';
  }
  var code = 'MY_NUMBER';
  return [code, Blockly.Arduino.ORDER_ATOMIC];
};

Blockly.Arduino['swarmy_parallelism_swarm_size'] = function() {
  if (!Blockly.Arduino.definitions_['define_turns']) {
    Blockly.Arduino.definitions_['define_swarm_size'] = '#define SWARM_SIZE ${swarmSize}\n';
  }
  var code = 'SWARM_SIZE';
  return [code, Blockly.Arduino.ORDER_ATOMIC];
};

Blockly.Arduino['swarmy_parallelism_in_turn'] = function() {
  var branch = Blockly.Arduino.statementToCode(this, 'STACK');
  if (Blockly.Arduino.INFINITE_LOOP_TRAP) {
    branch = Blockly.Arduino.INFINITE_LOOP_TRAP.replace(/%1/g,
        '\'' + this.id + '\'') + branch;
  }
  Blockly.Arduino.definitions_['define_my_number'] = '';
  Blockly.Arduino.definitions_['define_swarm_size'] = '';
  Blockly.Arduino.definitions_['define_turns'] = '#define MY_NUMBER ${myNumber}\n'+
    '#define SWARM_SIZE ${swarmSize}\n'+
    'int swarmTurn = 0;\n'+
    'bool isMyTurn() {\n'+
    '    return swarmTurn == MY_NUMBER;\n'+
    '}\n'+
    'bool areAllTurnsDone() {\n'+
    '    return swarmTurn == SWARM_SIZE;\n'+
    '}\n'+
    'void turnHandler(const char *event, const char *data) {\n'+
    '    String action = String(event).substring(5);\n'+
    '    int senderId = String(data).toInt();\n'+
    '\n'+
    '    if (action == "next") {\n'+
    '        swarmTurn = senderId + 1;\n'+
    '    }\n'+
    '}\n';
  Blockly.Arduino.setups_["setup_turn_handler_"] = 'Particle.subscribe("turn/", turnHandler, MY_DEVICES);\n';
  var code = 'swarmTurn = 0;\n'+
     '  waitUntil(isMyTurn);\n'+
     '  ' + branch + '\n'+
     '  Particle.publish(String("turn/next"), String(MY_NUMBER), 60, PRIVATE);\n'+
     '  waitUntil(areAllTurnsDone);\n';
  return code;
};


