package com.cardesktop.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TirePressureData(
    val frontLeft: Float = 0f,
    val frontRight: Float = 0f,
    val rearLeft: Float = 0f,
    val rearRight: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isRealData: Boolean = false
)

data class VehicleStatus(
    val gear: String = "P",
    val speed: Float = 0f,
    val rpm: Int = 0,
    val fuelLevel: Int = 0,
    val batteryLevel: Int = 100,
    val outsideTemp: Float = 23f,
    val engineTemp: Float = 90f,
    val odometer: Float = 0f,
    val isACOn: Boolean = false,
    val acTemp: Int = 24,
    val isDoorLocked: Boolean = true,
    val isTrunkOpen: Boolean = false,
    val headlights: Boolean = false
)

object VehicleService {

    private val _tirePressure = MutableStateFlow(TirePressureData())
    val tirePressure: StateFlow<TirePressureData> = _tirePressure.asStateFlow()

    private val _vehicleStatus = MutableStateFlow(VehicleStatus())
    val vehicleStatus: StateFlow<VehicleStatus> = _vehicleStatus.asStateFlow()

    private var context: Context? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false

    private val tirePressureReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    "com.byd.vehicle.TIRE_PRESSURE_UPDATED",
                    "android.intent.action.TIRE_PRESSURE" -> {
                        val data = TirePressureData(
                            frontLeft = it.getFloatExtra("front_left", 0f),
                            frontRight = it.getFloatExtra("front_right", 0f),
                            rearLeft = it.getFloatExtra("rear_left", 0f),
                            rearRight = it.getFloatExtra("rear_right", 0f),
                            timestamp = System.currentTimeMillis(),
                            isRealData = true
                        )
                        _tirePressure.value = data
                    }
                }
            }
        }
    }

    private val vehicleStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    "com.byd.VEHICLE_STATUS",
                    "com.byd.dilink.VEHICLE_STATUS" -> {
                        val status = VehicleStatus(
                            gear = it.getStringExtra("gear") ?: _vehicleStatus.value.gear,
                            speed = it.getFloatExtra("speed", 0f),
                            batteryLevel = it.getIntExtra("battery_level", _vehicleStatus.value.batteryLevel),
                            outsideTemp = it.getFloatExtra("outside_temp", _vehicleStatus.value.outsideTemp),
                            isACOn = it.getBooleanExtra("ac_on", _vehicleStatus.value.isACOn),
                            acTemp = it.getIntExtra("ac_temp", _vehicleStatus.value.acTemp),
                            isDoorLocked = it.getBooleanExtra("door_locked", true)
                        )
                        _vehicleStatus.value = status
                    }
                }
            }
        }
    }

    fun init(context: Context) {
        this.context = context.applicationContext
        
        registerReceivers()
        
        startSimulationIfNeeded()
    }

    private fun registerReceivers() {
        context?.let { ctx ->
            try {
                val tireFilter = IntentFilter().apply {
                    addAction("com.byd.vehicle.TIRE_PRESSURE_UPDATED")
                    addAction("android.intent.action.TIRE_PRESSURE")
                }
                ctx.registerReceiver(tirePressureReceiver, tireFilter)

                val statusFilter = IntentFilter().apply {
                    addAction("com.byd.VEHICLE_STATUS")
                    addAction("com.byd.dilink.VEHICLE_STATUS")
                }
                ctx.registerReceiver(vehicleStatusReceiver, statusFilter)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startSimulationIfNeeded() {
        if (!hasRealDataSource()) {
            startSimulation()
        }
    }

    private fun hasRealDataSource(): Boolean {
        return try {
            context?.packageManager?.let { pm ->
                val intent = Intent("com.byd.vehicle.GET_STATUS")
                pm.queryIntentActivities(intent, 0).isNotEmpty()
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun startSimulation() {
        if (isMonitoring) return
        isMonitoring = true

        val runnable = object : Runnable {
            override fun run() {
                if (!isMonitoring) return
                
                simulateTirePressure()
                simulateVehicleStatus()
                
                handler.postDelayed(this, 5000)
            }
        }
        
        handler.post(runnable)
        
        simulateTirePressure()
        simulateVehicleStatus()
    }

    private fun simulateTirePressure() {
        val basePressure = 250f + (Math.random() * 30).toFloat()
        _tirePressure.value = TirePressureData(
            frontLeft = basePressure + (Math.random() * 10 - 5).toFloat(),
            frontRight = basePressure + (Math.random() * 10 - 5).toFloat(),
            rearLeft = basePressure - 5 + (Math.random() * 10 - 5).toFloat(),
            rearRight = basePressure - 5 + (Math.random() * 10 - 5).toFloat(),
            timestamp = System.currentTimeMillis(),
            isRealData = false
        )
    }

    private fun simulateVehicleStatus() {
        val current = _vehicleStatus.value
        _vehicleStatus.value = current.copy(
            batteryLevel = (95..100).random(),
            outsideTemp = 20f + (Math.random() * 8).toFloat()
        )
    }

    fun controlAirConditioner(temp: Int? = null, turnOn: Boolean? = null): Boolean {
        return try {
            context?.let { ctx ->
                val intent = Intent("com.byd.ac.CONTROL").apply {
                    putExtra("command", when {
                        turnOn == true -> "TURN_ON"
                        turnOn == false -> "TURN_OFF"
                        temp != null -> "SET_TEMP"
                        else -> "TOGGLE"
                    })
                    temp?.let { putExtra("temperature", it) }
                    `package` = "com.byd.vehiclecontroller"
                    flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                }
                
                ctx.sendBroadcast(intent)
                
                val current = _vehicleStatus.value
                _vehicleStatus.value = current.copy(
                    isACOn = turnOn ?: !current.isACOn,
                    acTemp = temp ?: current.acTemp
                )
                
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            
            fallbackACControl(temp, turnOn)
        }
    }

    private fun fallbackACControl(temp: Int? = null, turnOn: Boolean? = null): Boolean {
        return try {
            context?.let { ctx ->
                val settingsIntent = Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("ac_command", when {
                        turnOn == true -> "on"
                        turnOn == false -> "off"
                        temp != null -> "temp_$temp"
                        else -> "toggle"
                    })
                }
                
                val current = _vehicleStatus.value
                _vehicleStatus.value = current.copy(
                    isACOn = turnOn ?: !current.isACOn,
                    acTemp = temp ?: current.acTemp
                )
                
                false
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun requestTirePressureUpdate(): Boolean {
        return try {
            context?.let { ctx ->
                val intent = Intent("com.byd.vehicle.REQUEST_TIRE_PRESSURE").apply {
                    `package` = "com.byd.vehiclecontroller"
                    flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                }
                ctx.sendBroadcast(intent)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun requestVehicleStatusUpdate(): Boolean {
        return try {
            context?.let { ctx ->
                val intent = Intent("com.byd.vehicle.REQUEST_STATUS").apply {
                    `package` = "com.byd.vehiclecontroller"
                    flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                }
                ctx.sendBroadcast(intent)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun stop() {
        isMonitoring = false
        handler.removeCallbacksAndMessages(null)
        
        try {
            context?.unregisterReceiver(tirePressureReceiver)
            context?.unregisterReceiver(vehicleStatusReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateACTemperature(delta: Int): Boolean {
        val current = _vehicleStatus.value
        val newTemp = (current.acTemp + delta).coerceIn(16, 32)
        return controlAirConditioner(temp = newTemp)
    }

    fun toggleAC(): Boolean {
        return controlAirConditioner(turnOn = !_vehicleStatus.value.isACOn)
    }
}