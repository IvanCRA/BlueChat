#include <Arduino.h>

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLE2902.h>
#include <BLEUtils.h>

#define NAME_DEVICE "BLE_TEST_ANDROID"

#define SERVICE_CONTROL_UUID "4fcfdffb-2bc8-4360-8ccd-5caef5453f79"
#define SERVICE_WORK_TIME_UUID "db41ddc6-8acd-44bc-8085-c321b76f557e"

#define CONTROL_REQUEST_UUID "9ee87489-69b8-4aca-9d10-5f86b515b345"
#define CONTROL_RESPONSE_UUID "4c7fdd90-ac7f-4f19-b0ac-300b2960730e"

#define WORK_TIME_UUID "da19c791-3418-4ceb-8a71-9166f2e5223c"


#define PIN_LED_FIRST  4
#define PIN_LED_SECOND 33

#define CMD_ENABLE_LED  0x1

BLECharacteristic* controlRequest;
BLECharacteristic* controlResponse;
BLECharacteristic* workTime;


unsigned long timerMillis = 0;
bool isConnected = false;

void setupBluetooth();
void setupLeds();
void enableLed(int pin, bool enable);
void sendWorkedTime(int seconds);
void sendDeviceState();


class BluetoothServerEventCallback : public BLEServerCallbacks {
  void onConnect(BLEServer * server) 
  {
    isConnected = true;
    sendDeviceState(); 
  } 
  void onDisconnect(BLEServer * server) 
  {
    isConnected = false;
  }
};

class BluetoothEventCallback : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* characteristic) 
  {
    uint8_t* data = characteristic->getData();
    if (data[0] == CMD_ENABLE_LED) { 
      Serial.println("Yes, enabled");
      enableLed(data[1], data[2] > 0); 
    } 
   Serial.printf("data[0]: %d, data[1]: %d, data[2]: %d, data[3]: %d, data[4]: %d, data[5]: %d, data[6]: %d\n", data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
    sendDeviceState();
  }
}; 

void setup() {
  Serial.begin(115200);
  Serial.println("Launching...");

  Serial1.begin(115200, SERIAL_8N1, -1, -1, true, 20000UL);


  setupBluetooth();
  setupLeds(); 
}

void loop() { 
  if (millis() - timerMillis > 5000) {
    timerMillis = millis();

    sendWorkedTime(timerMillis / 1000); 
  } 
}

void setupBluetooth() 
{
  BLEDevice::init(NAME_DEVICE);

  BLEServer* server = BLEDevice::createServer();
  server->setCallbacks(new BluetoothServerEventCallback());
  BLEService* serviceControl  = server->createService(SERVICE_CONTROL_UUID);
  controlRequest = serviceControl->createCharacteristic(CONTROL_REQUEST_UUID, BLECharacteristic::PROPERTY_WRITE);
  controlRequest->setCallbacks(new BluetoothEventCallback());
  controlResponse = serviceControl->createCharacteristic(CONTROL_RESPONSE_UUID, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);
  controlResponse->addDescriptor(new BLE2902()); 

  BLEService* serviceWorkTime = server->createService(SERVICE_WORK_TIME_UUID);
  workTime = serviceWorkTime->createCharacteristic(WORK_TIME_UUID, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);
  workTime->addDescriptor(new BLE2902());

  serviceControl->start();
  serviceWorkTime->start(); 

  BLEAdvertising* advertising = BLEDevice::getAdvertising();
  advertising->addServiceUUID(SERVICE_CONTROL_UUID);
  advertising->addServiceUUID(SERVICE_WORK_TIME_UUID);
  advertising->setMinPreferred(0x06); 
  advertising->setMinPreferred(0x12);
  advertising->start();  
}

void setupLeds() 
{
  pinMode(PIN_LED_FIRST, OUTPUT);
  pinMode(PIN_LED_SECOND, OUTPUT);
}


void enableLed(int pin, bool enable) 
{
  digitalWrite(pin, (enable) ? HIGH : LOW);
}

void sendWorkedTime(int seconds)
{
  if (isConnected) {
    workTime->setValue(seconds);
    workTime->notify();
  }
}

void sendDeviceState() 
{
  if (isConnected) {
    uint8_t data[5];
    data[0] = PIN_LED_FIRST;                    
    data[1] = digitalRead(PIN_LED_FIRST);
    data[2] = PIN_LED_SECOND; 
    data[3] = digitalRead(PIN_LED_SECOND);
    controlResponse->setValue(data, 5);
    controlResponse->notify();
    Serial.printf("LED %d: %d, LED %d: %d\n", data[0], data[1], data[2], data[3]);
  }
}