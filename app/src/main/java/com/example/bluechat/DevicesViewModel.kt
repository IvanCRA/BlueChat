package com.example.bluechat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DevicesViewModel (adapterProvider: BluetoothAdapterProvider) : ViewModel() {
    private val _devices: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    val devices: LiveData<List<BluetoothDevice>> get() = _devices

    private val adapter = adapterProvider.getAdapter()
    private var scanner : BluetoothLeScanner? = null
    private var callback : BleScanCallback? = null

    private val settings: ScanSettings
    private val filters: List<ScanFilter>

    private val foundDevices = HashMap<String, BluetoothDevice>()

    init {
        settings = buildSettings()
        filters = buildFilter()
    }

    private fun buildSettings() =
        ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

    private fun buildFilter() =
        listOf(
            ScanFilter.Builder()
                .setServiceUuid(FILTER_UUID)
                .build()
        )

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (callback == null) {
            callback = BleScanCallback()
            scanner = adapter.bluetoothLeScanner
            scanner?.startScan(filters, settings, callback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (callback != null) {
            scanner?.stopScan(callback)
            scanner = null
            callback = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }

    inner class BleScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            foundDevices[result.device.address] = result.device
            _devices.postValue(foundDevices.values.toList())
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { result ->
                foundDevices[result.device.address] = result.device
            }
            _devices.postValue(foundDevices.values.toList())
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BluetoothScanner", "onScanFailed:  scan error $errorCode")
        }
    }

    companion object {
        val FILTER_UUID: ParcelUuid = ParcelUuid.fromString("4fcfdffb-2bc8-4360-8ccd-5caef5453f79")
    }
}

class DeviceViewModelFactory(
    private val adapterProvider: BluetoothAdapterProvider
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DevicesViewModel::class.java)) {
            return DevicesViewModel(adapterProvider) as T
        }
        throw IllegalArgumentException("View Model not found")
    }
}