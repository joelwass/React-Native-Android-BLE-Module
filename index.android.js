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
  DeviceEventEmitter,
  ScrollView,
  TextInput,
} from 'react-native';

class nativeBLEModule extends Component {

  constructor(props) {
    super(props);
    this.state = {
      deviceIndex: '0',
      peripheralsText: 'press start scan to start scanning\n',
    };
    this._startScanning = this._startScanning.bind(this);
    this._stopScanning = this._stopScanning.bind(this);
    this._connectToDevice = this._connectToDevice.bind(this);
    this._disconnectFromDevice = this._disconnectFromDevice.bind(this);
    this._discoverServices = this._discoverServices.bind(this);
    this.updateTextView = this.updateTextView.bind(this);
  }

  componentDidMount() {
    DeviceEventEmitter.addListener('DeviceDiscovered', (result) => {this.setState({peripheralsText: this.state.peripheralsText + result + "\n"})});
    DeviceEventEmitter.addListener('DeviceStateChanged', (stateChange) => {this.setState({peripheralsText: this.state.peripheralsText + "State Changed: " + stateChange + "\n"})});
    DeviceEventEmitter.addListener('Event', (eventDescription) => {this.setState({peripheralsText: this.state.peripheralsText + eventDescription + "\n"})});
    DeviceEventEmitter.addListener('WordCount', (wordCount) => {this.setState({peripheralsText: this.state.peripheralsText + "Word Count: " + wordCount + "\n"})});
    DeviceEventEmitter.addListener('ServiceDiscovered', (serviceDiscovered) => {this.setState({peripheralsText: this.state.peripheralsText + "Service Discovered: " + serviceDiscovered + "\n"})});
    DeviceEventEmitter.addListener('CharacteristicDiscovered', (character) => {this.setState({peripheralsText: this.state.peripheralsText + "Characteristic Discovered: " + CharacteristicDiscovered + "\n"})});
  }

  _startScanning() {
    console.log("RN start scanning tapped");
    this.setState({peripheralsText: "Started Scanning \n"});
    NativeModules.BLE.startScanning();
  }

  _stopScanning() {
    console.log("RN stop scanning");
    this.updateTextView("Stopped Scanning \n");
    NativeModules.BLE.stopScanning();
  }

  _connectToDevice() {
    console.log("trying to connect to device " + this.state.deviceIndex);
    this.updateTextView("Connecting to device: " + this.state.deviceIndex + "\n");
    NativeModules.BLE.connectToDeviceSelected(this.state.deviceIndex);
  }

  _disconnectFromDevice() {
    console.log("trying to disconnect from device");
    this.updateTextView("Disconnecting from device\n");
    NativeModules.BLE.disconnectDeviceSelected();
  }

  _discoverServices() {
    console.log("trying to discover services");
    this.updateTextView("Trying to discover services\n");
  }

  updateTextView(text) {
    this.setState({peripheralsText: this.state.peripheralsText + text});
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
        onPress={this._stopScanning}>
          <Text>
            Stop Scanning
          </Text>
        </TouchableHighlight>

        <TextInput
        style={styles.deviceIndexInput}
        keyboardType={'numeric'}
        onChangeText={(text) => this.setState({deviceIndex: text})}
        value={this.state.deviceIndex} 
        />

        <TouchableHighlight
        style={styles.button}
        underlayColor={'grey'}
        onPress={this._connectToDevice}>
          <Text>
            Connect To Device With Index
          </Text>
        </TouchableHighlight>

        <TouchableHighlight
        style={styles.button}
        underlayColor={'grey'}
        onPress={this._disconnectFromDevice}>
          <Text>
            Disconnect from device
          </Text>
        </TouchableHighlight>

        <Text style={styles.welcome}>
          Peripherals in this area
        </Text>

        <ScrollView 
        ref='scrollView'
        style={styles.peripheralContainer}
        onContentSizeChange={(width, height) => {this.refs.scrollView.scrollTo({y: height})}}>
          <Text
          style={styles.peripheralsText}>
          {this.state.peripheralsText}
          </Text>
        </ScrollView>

      </View>
    );
  }
}

const styles = StyleSheet.create({
  peripheralContainer: {
    flex: 1,
    width: 300,
    marginBottom: 10,
    borderWidth: 2,
    borderRadius: 5,
    borderColor: 'black',
    paddingHorizontal: 10,
    borderStyle: 'solid',
    backgroundColor: 'lavender',
  },
  peripheralsText: {
    alignItems: 'center',
    paddingBottom: 20,
  },
  deviceIndexInput: {
    width: 50,
    alignItems: 'center',
  },
  container: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  button: {
    backgroundColor: 'lavender',
    marginVertical: 5,
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
