package com.sing.lbstest

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.sing.lbstest.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    /** 허용 받을 권한 목록 */
    val permission_list = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /** 현재 위치를 위한 변수 선언 */
    lateinit var manager:LocationManager
    lateinit var locationListener: LocationListener


    /** 구글 map 제어를 위한 method */
    lateinit var googleMap: GoogleMap


    /** 바인딩 객체 */
    lateinit var binding : ActivityMainBinding



    /** Service Intent 를 받을 변수 */
    lateinit var serviceIntent : Intent


    var ipcService:MyLocationService? = null
    var serviceRunning = false
    var myLocation: Location? = null


    /** Service 접속을 관리하는 객체 */
    var connection = object:ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            // Service 를 추출
            val binder = p1 as MyLocationService.MyLocationServiceBinder
            ipcService = binder.getService()

        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            ipcService = null
        }
    }

    /** Setting*/
    val dialogData = arrayOf(
        "accounting", "airport", "amusement_park",
        "aquarium", "art_gallery", "atm", "bakery",
        "bank", "bar", "beauty_salon", "bicycle_store",
        "book_store", "bowling_alley", "bus_station",
        "cafe", "campground", "car_dealer", "car_rental",
        "car_repair", "car_wash", "casino", "cemetery",
        "church", "city_hall", "clothing_store", "convenience_store",
        "courthouse", "dentist", "department_store", "doctor",
        "drugstore", "electrician", "electronics_store", "embassy",
        "fire_station", "florist", "funeral_home", "furniture_store",
        "gas_station", "gym", "hair_care", "hardware_store", "hindu_temple",
        "home_goods_store", "hospital", "insurance_agency",
        "jewelry_store", "laundry", "lawyer", "library", "light_rail_station",
        "liquor_store", "local_government_office", "locksmith", "lodging",
        "meal_delivery", "meal_takeaway", "mosque", "movie_rental", "movie_theater",
        "moving_company", "museum", "night_club", "painter", "park", "parking",
        "pet_store", "pharmacy", "physiotherapist", "plumber", "police", "post_office",
        "primary_school", "real_estate_agency", "restaurant", "roofing_contractor",
        "rv_park", "school", "secondary_school", "shoe_store", "shopping_mall",
        "spa", "stadium", "storage", "store", "subway_station", "supermarket",
        "synagogue", "taxi_stand", "tourist_attraction", "train_station",
        "transit_station", "travel_agency", "university", "eterinary_care","zoo"
    )


    var lat = ArrayList<Double>()
    var lng = ArrayList<Double>()
    var name = ArrayList<String>()
    var vicinity = ArrayList<String>()
    var markerList = ArrayList<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SystemClock.sleep(1000)
        setTheme(R.style.Theme_LbsTest)


        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)


        /** 권한 요청 */
        requestPermissions(permission_list,0)

        /** 현재 위치 측정 */
        // Map 의 상태가 변경되면 호출될 method가 구현되어 있는 곳을 등록
        // 1. Map Fragment 받아오기
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_Fragment) as SupportMapFragment

        // 2. 호출
        mapFragment.getMapAsync(this)

        /** Service 가 가동중이 아니라면 Service 를 가동 시킨다  */
        val chk = isServiceRunning("com.sing.lbstest.MyLocationService")

        serviceIntent = Intent(this, MyLocationService::class.java)

        if(!chk)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                startForegroundService(serviceIntent)
            }
            else
            {
                startService(serviceIntent)
            }
        }

        /** Service에 접속 */
        bindService(serviceIntent, connection, BIND_AUTO_CREATE)


    }

    /** Service 가동 여부를 확인하는 method */
    fun isServiceRunning(name:String):Boolean{
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager

        /** 현재 실행중인 Service 를 가져온다. */
        val serviceList = manager.getRunningServices(Int.MAX_VALUE)

        /** 실행중인 Service를 조회 */
        for(serviceInfo in serviceList)
        {
            // 같은 이름이 존재한다면 true 를 리턴
            if(serviceInfo.service.className.equals(name))
            {
                return true
            }
        }
        return false
    }

    /** Activity 가 중지되면 Service 를 중지 */
    override fun onDestroy() {
        super.onDestroy()
        /** Service 접속 해제 */
        unbindService(connection)
        stopService(serviceIntent)
    }


    /** 지도 준비가 완료되면 호출하는 method */
    override fun onMapReady(p0: GoogleMap) {
            googleMap = p0


        // 구글지도의 옵션설정을 위한 권한 확인
        val option1 = Manifest.permission.ACCESS_FINE_LOCATION
        val option2 = Manifest.permission.ACCESS_COARSE_LOCATION



        if(ActivityCompat.checkSelfPermission(this, option1) == PackageManager.PERMISSION_GRANTED
            &&
                    ActivityCompat.checkSelfPermission(this, option2) == PackageManager.PERMISSION_GRANTED
        )
        {

        }

        // 확대 축소 버튼
        googleMap.uiSettings.isZoomControlsEnabled = true


        // 현재 위치 표시
        googleMap.isMyLocationEnabled = true



        serviceRunning = true

        /** 현재 위치를 가져오는 Thread 가동 */
        // thread 를 사용해서 지도와 현재 정보를 따로 가져오도록 설정

        thread{
            while(serviceRunning)
            {
                SystemClock.sleep(1000)
                myLocation = ipcService?.returnUserLocation()

                runOnUiThread {
                    if(myLocation!=null)
                    {
                        setUserLocation(myLocation!!, true)
                        serviceRunning = false
                    }
                }
            }
        }



    }



    /** 위치 값을 받아 지도를 이동시키는 method */
    fun setUserLocation(location: Location, zoom:Boolean)
    {

        // 위도와 경도값을 관리하는 객체
        val loc1 = LatLng(location.latitude, location.longitude)


        if(zoom)
        {
            // 지도를 이동시키기 위한 객체를 생성
            // 15 배 확대
            val loc2 = CameraUpdateFactory.newLatLngZoom(loc1,15f)
            googleMap.animateCamera(loc2)
        }
        else
        {

            val loc2= CameraUpdateFactory.newLatLng(loc1)
            // camera 이동
            googleMap.animateCamera(loc2)
        }


        // Marker





    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.main_menu_location->
            {
                myLocation = ipcService?.returnUserLocation()
                setUserLocation(myLocation!!, false)
            }
            R.id.my_place->
            {
                val placeListBuilder = AlertDialog.Builder(this)
                placeListBuilder.setTitle("set Place List")
                placeListBuilder.setNegativeButton("Cancel", null)
                placeListBuilder.setNeutralButton("initialize"){
                        dialogInterface, i ->
                    /** list 초기화*/
                    lat.clear()
                    lng.clear()
                    name.clear()
                    vicinity.clear()

                    for(m in markerList)
                    {
                        m.remove()
                    }
                    markerList.clear()
                }
                placeListBuilder.setItems(dialogData)
                {
                   dialogInterface, i ->
                    /** list 초기화*/
                    lat.clear()
                    lng.clear()
                    name.clear()
                    vicinity.clear()

                    for(m in markerList)
                    {
                        m.remove()
                    }
                    markerList.clear()


                   getNearbyPlaceData(dialogData[i])
                }
                placeListBuilder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** 메뉴를 터치하면 해당타입의 건물 정보 불러오기 */
    fun getNearbyPlaceData(type:String)
    {

        thread {
            var site = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
            site += "?location=${myLocation?.latitude},${myLocation?.longitude}"
            site += "&radius=1000&type=${type}&language=ko"
            site += "&key=AIzaSyCo3_S9qlhi42OHN6eaIWSjU0BjZhhmrNY"

            val url = URL(site)
            val conn = url.openConnection() as HttpURLConnection

            // 데이터 읽어 오기
            val isr = InputStreamReader(conn.inputStream, "UTF-8")
            val br = BufferedReader(isr)

            var str: String? = null
            val buf = StringBuffer()

            do {
                str = br.readLine()
                if (str != null) {
                    buf.append(str)
                }
            } while (str != null)

            val data = buf.toString()


            /** JSON 객체 생성 */
            val root= JSONObject(data)

            if( root.getString("status").equals("OK") )
            {
                val results = root.getJSONArray("results")

                for(i in 0 until results.length() )
                {
                    val results_item = results.getJSONObject(i)

                    val geometry = results_item.getJSONObject("geometry")
                    val location = geometry.getJSONObject("location")
                    val getLat = location.getDouble("lat")
                    val getLng = location.getDouble("lng")
                    val getName = results_item.getString("name")
                    val getVicinity = results_item.getString("vicinity")

                    lat.add(getLat)
                    lng.add(getLng)
                    name.add(getName)
                    vicinity.add(getVicinity)

                    runOnUiThread {
                        for(i in 0 until lat.size)
                        {
                            val loc = LatLng(lat[i],lng[i])

                            var plcaeMarkerOptions = MarkerOptions()
                            plcaeMarkerOptions.position(loc)
                            plcaeMarkerOptions.title(name[i])
                            plcaeMarkerOptions.snippet(vicinity[i])

                            val placeMarker = googleMap.addMarker(plcaeMarkerOptions)
                            markerList.add(placeMarker!!)
                        }
                    }
                }
            }
    }


    // 현재 위치를 측정하는 메서드
    fun getMyLocation(){
        // 위치 정보를 관리하는 매니저를 추출한다.
        manager = getSystemService(LOCATION_SERVICE) as LocationManager

        // 저장되어 있는 위치값이 있으면 가져온다.
        val a1 = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        val a2 = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

        if (a1 && a2) {
            return
        }

        val location1 = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val location2 = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        // 새로운 위치 측정을 요청
        locationListener = LocationListener {
            setUserLocation(it, false)
        }

        if(location1 != null){
            setUserLocation(location1, false)
        } else if(location2 != null){
            setUserLocation(location2, false)
        }



        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER) == true){
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0f, locationListener)
        } else if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true){
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
                0f, locationListener)
        }
    }

}
}