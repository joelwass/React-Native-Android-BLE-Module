/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  TouchableHighlight,
  Text,
  View,
  NativeModules,
} from 'react-native';

class nativeBLEModule extends Component {

  _startScanning() {
    console.log("RN start scanning tapped");
    NativeModules.BLE.startScanning();
  }

  _viewPeripherals() {
    console.log("RN Viewing peripherals");
    console.log(NativeModules.BLE.getDevices());
  }

  _stopScanning() {
    console.log("RN stop scanning");
    NativeModules.BLE.stopScanning();
  }

  render() {
    return (
      <View style={styles.container}>

        <TouchableHighlight 
        style={styles.button}
        underlayColor={'grey'}
        onPress={this._startScanning}>
          <Text>
            Start Scanning
          </Text>
        </TouchableHighlight>

        <TouchableHighlight 
        style={styles.button}
        underlayColor={'grey'}
        onPress={this._viewPeripherals}>
          <Text>
            View Peripherals
          </Text>
        </TouchableHighlight>

        <TouchableHighlight 
        style={styles.button}
        underlayColor={'grey'}
        onPress={this._stopScanning}>
          <Text>
            Stop Scanning
          </Text>
        </TouchableHighlight>

        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.android.js
        </Text>
        <Text style={styles.instructions}>
          Shake or press menu button for dev menu
        </Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  button: {
    backgroundColor: 'lavender',
    marginVertical: 10,
    borderRadius: 5,
    paddingHorizontal: 10,
    paddingVertical: 10,
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('nativeBLEModule', () => nativeBLEModule);
