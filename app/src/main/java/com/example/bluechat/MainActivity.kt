package com.example.bluechat

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigate(DevicesFragment.newInstance())

        requestPermissions()
    }

    private fun navigate(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.containerFragment, fragment)
            .commit()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        } else {
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth()
            } else {
                Log.e("BLE", "Permissions denied")
                showEnableBluetoothMessage()
            }
        }
    }

    private fun enableBluetooth() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE", "BLUETOOTH_CONNECT permission not granted")
            return
        }
        requestEnableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private val requestEnableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_CANCELED) {
            showEnableBluetoothMessage()
        }
    }

    private fun showEnableBluetoothMessage() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.enable_bluetooth))
            .setMessage(getString(R.string.enable_bluetooth_message))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                enableBluetooth()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                finish()
            }
            .create()
            .show()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}