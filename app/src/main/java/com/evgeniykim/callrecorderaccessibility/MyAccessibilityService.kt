package com.evgeniykim.callrecorderaccessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.FrameLayout
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*

class MyAccessibilityService: AccessibilityService() {

    private var TAG = "MyAccessibilityService"
    var mLayout: FrameLayout? = null

    var recorder: MediaRecorder? = null

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        val wm: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mLayout = FrameLayout(this)
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        lp.format = PixelFormat.TRANSLUCENT
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP
        val inflater: LayoutInflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.action_bar, mLayout)
        wm.addView(mLayout, lp)

        startRecording()
        stopRecording()
    }

    private fun stopRecording() {

        var buttonStop = mLayout?.findViewById<Button>(R.id.stopRecordingBtn)
        buttonStop?.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view: View?) {
                recorder?.stop()
                recorder?.release()
                recorder = null
                Log.i(TAG, "Recording stopped")

            }
        })


    }

    private fun startRecording() {
        val startRecordingBtn = mLayout?.findViewById<Button>(R.id.startRecordingBtn)
        startRecordingBtn?.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view: View?) {

                val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
//                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, 0)

                recorder = MediaRecorder()

                var audioFile: File? = null
                var out: String = SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(Date())
                var sampleDir = File(getExternalFilesDir(null), "/TestRecording")
                if (!sampleDir.exists()) {
                    sampleDir.mkdirs()
                }
                var file_name = "Record${out}"
                try {
                    audioFile = File.createTempFile(file_name, ".3gp", sampleDir)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                recorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//                if (Build.VERSION.SDK_INT >= 10) {
//                    recorder!!.setAudioSamplingRate(44100)
//                    recorder!!.setAudioEncodingBitRate(96000)
//                } else {
//                    recorder!!.setAudioSamplingRate(8000)
//                    recorder!!.setAudioEncodingBitRate(12200)
//                }
                recorder!!.setOutputFile(audioFile?.absolutePath)
                try {
                    recorder!!.prepare()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                recorder!!.start()
                Log.i(TAG, "Recording started, saving at " + audioFile?.absolutePath)

            }

        })
    }




}