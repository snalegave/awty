package edu.washington.snalegave.awty

import android.app.AlarmManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.PhoneNumberUtils


class MainActivity : AppCompatActivity() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getStringExtra("message")
            val number = intent.getStringExtra("number")
            val formattedNumber = PhoneNumberUtils.formatNumber(number)

            Toast.makeText(this@MainActivity, formattedNumber + ": " + message , Toast.LENGTH_SHORT).show()
        }
    }
    private lateinit var alarmIntent: PendingIntent

    private var alarmStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val message= findViewById<EditText>(R.id.message)
        val number = findViewById<EditText>(R.id.phoneNumber)
        val interval = findViewById<EditText>(R.id.minutesBetNag)
        val button = findViewById<Button>(R.id.button)

        var messageCheck: Boolean
        var numberCheck: Boolean
        var intervalCheck: Boolean


        button.setOnClickListener{
            val alarmMgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            this.registerReceiver(receiver, IntentFilter("receiveInterval"))

            messageCheck = !message.text.isNullOrEmpty()
            numberCheck = !number.text.isNullOrEmpty()
            intervalCheck = try {
                interval.text.toString().toInt() > 0
            }catch (e: NumberFormatException){
                false
            }

            if (alarmStatus){
                button.text = "Start"

                alarmIntent.cancel()
                alarmMgr.cancel(alarmIntent)
                alarmStatus= false
            }else if(messageCheck && numberCheck && intervalCheck){
                button.text = "Stop"
                alarmStatus= true

                val intent = Intent().apply {
                    putExtra("message", message.text.toString())
                    putExtra("number", number.text.toString())
                    action="receiveInterval"
                }
                alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
                alarmMgr.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + interval.text.toString().toInt()* 1000 * 60, interval.text.toString().toLong()* 1000 * 60, alarmIntent)
            }else if (!messageCheck &&numberCheck && intervalCheck ){
                Toast.makeText(this, "Message can't be empty", Toast.LENGTH_SHORT).show()
            }else if (!numberCheck &&messageCheck && intervalCheck){
                Toast.makeText(this, "Number can't be empty", Toast.LENGTH_SHORT).show()
            }else if (!intervalCheck &&numberCheck && messageCheck){
                Toast.makeText(this, "Interval should be a positive integer", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
