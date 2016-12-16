Blockly.Arduino.swarmy_io_led = function() {
  var dropdown_stat = this.getFieldValue('STAT');
  Blockly.Arduino.setups_['setup_output_D7'] = 'pinMode(D7, OUTPUT);';
  var code = 'digitalWrite(D7, ' + dropdown_stat + ');\n'
  return code;
};
