package com.example.bluechat;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;

public class BleControlManager extends BleManager {
    public static final UUID SERVICE_CONTROL_UUID = UUID.fromString("4fcfdffb-2bc8-4360-8ccd-5caef5453f79");
    public static final UUID SERVICE_WORK_TIME_UUID = UUID.fromString("db41ddc6-8acd-44bc-8085-c321b76f557e");

    public static final UUID CONTROL_REQUEST_UUID = UUID.fromString("9ee87489-69b8-4aca-9d10-5f86b515b345");
    public static final UUID CONTROL_RESPONSE_UUID = UUID.fromString("4c7fdd90-ac7f-4f19-b0ac-300b2960730e");

    public static final UUID WORK_TIME_UUID = UUID.fromString("da19c791-3418-4ceb-8a71-9166f2e5223c");

    public static final int CMD_CONTROL_LED = 0x1;
    public static final int CMD_CONTROL_SERVO = 0x2;

    public static final int LED_FIRST = 25;


    private BluetoothGattCharacteristic controlRequest;
    private BluetoothGattCharacteristic controlResponse;
    private BluetoothGattCharacteristic workTime;

    public BleControlManager(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new BleControlManagerGattCallback();
    }

    public void enableLed(Led led, boolean enable) {
        writeCharacteristic(
                controlRequest,
                new byte[]{CMD_CONTROL_LED, led.getPin(), (byte) ((enable) ? 0x1 : 0x0)},
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).enqueue();
    }

    public void setAngleServo(int angle) {
        writeCharacteristic(
                controlRequest,
                new byte[]{CMD_CONTROL_SERVO, (byte) angle},
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).enqueue();
    }

    class BleControlManagerGattCallback extends BleManagerGattCallback {

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            BluetoothGattService controlService = gatt.getService(SERVICE_CONTROL_UUID);
            BluetoothGattService workTimeService = gatt.getService(SERVICE_WORK_TIME_UUID);

            if (controlService != null && workTimeService != null) {
                controlRequest = controlService.getCharacteristic(CONTROL_REQUEST_UUID);
                controlResponse = controlService.getCharacteristic(CONTROL_RESPONSE_UUID);
                workTime = workTimeService.getCharacteristic(WORK_TIME_UUID);
            }

            return controlRequest != null && controlResponse != null && workTime != null;
        }

        @Override
        protected void onServicesInvalidated() {
            controlRequest = null;
            controlResponse = null;
            workTime = null;
        }
    }
}
