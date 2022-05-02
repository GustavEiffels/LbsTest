package com.sing.lbstest

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat

class MyLocationService : Service() {

    lateinit var  manager : LocationManager
    lateinit var locationListener: LocationListener

    var binder = MyLocationServiceBinder()

    var myLocation : Location? =null


    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        /** SDK 버전이 Oreo 뻐전 이상일 경우 */
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
        {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "myLocationService",
                "myLocationService",
            NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)

             val builder = NotificationCompat.Builder(this, "myLocationService")
            builder.setSmallIcon(android.R.drawable.ic_menu_mylocation)
            builder.setContentTitle("현재 위치 측정")
            builder.setContentText("현재 위치를 측정 중 입니다.")

            val notification = builder.build()
            startForeground(10,notification)
        }

        manager = getSystemService(LOCATION_SERVICE) as LocationManager

        val a1 = Manifest.permission.ACCESS_FINE_LOCATION
        val a2 = Manifest.permission.ACCESS_COARSE_LOCATION

        /** 거부 상태라면 */
        if(ActivityCompat.checkSelfPermission(this, a1)== PackageManager.PERMISSION_DENIED
            || ActivityCompat.checkSelfPermission(this, a2) == PackageManager.PERMISSION_DENIED
        )
        {
            return super.onStartCommand(intent,flags,startId)
        }

        val location1 = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val location2 = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)


        locationListener = LocationListener {
            getUserLocation(it)
        }


        if(location1!=null){
            getUserLocation(location1)
        }
        else if (location2!=null)
        {
            getUserLocation(location2)
        }

        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0 ,0f, locationListener)
        }
        if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0 ,0f, locationListener)
        }

        return super.onStartCommand(intent, flags, startId)
    }


    fun getUserLocation(location:Location){
        /** 현재 사용자의 위치를 변수에 담기 */
        myLocation = location
    }

    fun returnUserLocation():Location?{
        return myLocation
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.removeUpdates(locationListener)
    }



    /** 접속하는 Activity 에서 Service 를 추축하기 위해 사용하는 객체*/
    inner class MyLocationServiceBinder : Binder()
    {
        fun getService() : MyLocationService{
            return this@MyLocationService
        }
    }


}