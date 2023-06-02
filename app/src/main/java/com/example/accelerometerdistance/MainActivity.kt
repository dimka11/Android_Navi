package com.example.accelerometerdistance

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener {
    val xList = mutableListOf<Float>()
    val yList = mutableListOf<Float>()
    val zList = mutableListOf<Float>()

    var isMeasure = false


    private val NS2S = 1.0f / 1000000000.0f
    private var timestamp = 0L

    var prevAccelX = 0f
    var prevAccelY = 0f
    var prevAccelZ = 0f

    var prevVelX  = 0f
    var prevVelY  = 0f
    var prevVelZ  = 0f

    var distX = 0f
    var distY = 0f
    var distZ = 0f

    var velX = 0f
    var velY = 0f
    var velZ = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sensorManager =
            this.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val orientation: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isMeasure) {
            if (event!!.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                xList.add(abs(event!!.values[0]))
                yList.add(abs(event.values[1]))
                zList.add(abs(event.values[2]))

                var dT = 0f

                if (timestamp > 0) {
                    dT = (event.timestamp - timestamp) * NS2S
                }
                timestamp = event.timestamp

                val xVal = findViewById<View>(R.id.xVal) as TextView
                xVal.text = event.values[0].toString()

                val yVal = findViewById<View>(R.id.yVal) as TextView
                yVal.text = event.values[1].toString()

                val zVal = findViewById<View>(R.id.zVal) as TextView
                zVal.text = event.values[2].toString()

                var accelX = event.values[0]
                var accelY = event.values[1]
                var accelZ = event.values[2]

                val accelKFactor = .01f

                if (abs(accelX) < 0.02) {
                    accelX = 0f
                }

                if (abs(accelY) < 0.02) {
                    accelY = 0f
                }

                if (abs(accelZ) < 0.02) {
                    accelZ = 0f
                }

                accelX = (accelX * accelKFactor) + prevAccelX * (1 - accelKFactor);
                accelY = (accelY * accelKFactor) + prevAccelY * (1 - accelKFactor);
                accelZ = (accelZ * accelKFactor) + prevAccelZ * (1 - accelKFactor);

                velX += accelX * dT
                velY += accelY * dT
                velZ += accelZ * dT

                distX += prevVelX + velX * dT
                distY += prevVelY + velY * dT
                distZ += prevVelZ + velZ * dT

                prevAccelX = accelX
                prevAccelY = accelY
                prevAccelZ = accelZ

                prevVelX = velX
                prevVelY = velY
                prevVelZ = velZ

                val vVelocity = findViewById<View>(R.id.vVelocity) as TextView
                vVelocity.text = "$velX   $velY   $velZ"

                val vDistance = findViewById<View>(R.id.vDistance) as TextView
                vDistance.text = "$distX   $distY   $distZ"
            }

            if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                val vAzimuth = findViewById<View>(R.id.vAzimuth) as TextView
                vAzimuth.text = event.values[0].toString()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    fun startMeasure(view: View) {
        isMeasure = !isMeasure
        val startButton = findViewById<View>(R.id.start) as TextView
        if (isMeasure) {
            startButton.text = "Stop measure"
            xList.clear()
            yList.clear()
            zList.clear()
        }
        else {
            startButton.text = "Start measure"
            clearGravityVars()
        }

    }
    fun takeMeasure(view: View) {
        val dVal = findViewById<View>(R.id.dVal) as TextView
        val displacement = sqrt(xList.sum().pow(2) + yList.sum().pow(2) + zList.sum().pow(2)) .toString()
        dVal.text = displacement

//        val root = android.os.Environment.getExternalStorageDirectory()
//        val dir = File(root.absolutePath + "aaaaaaaaaa")
//        val files = root.listFiles()
//        dir.mkdirs()
    }

    fun clearGravityVars() {
        prevAccelX = 0f
        prevAccelY = 0f
        prevAccelZ = 0f

        prevVelX  = 0f
        prevVelY  = 0f
        prevVelZ  = 0f

        distX = 0f
        distY = 0f
        distZ = 0f

        velX = 0f
        velY = 0f
        velZ = 0f
    }
}