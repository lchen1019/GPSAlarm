package clqwq.press.gpsalarm;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;


public class MainActivity extends AppCompatActivity {


    public AMapLocationClient mLocationClient;
    private MyLocationStyle myLocationStyle;
    private MapView mapView;
    private AMap aMap;
    private LatLng to;
    private Double maxDistance;
    private Button start;
    private EditText address;
    private EditText max;
    private boolean isStart;


    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation.getErrorCode() == 0) {
                if (!isStart) return;
                // 解析aMapLocation，获取当前的位置信息
                double curLongitude = aMapLocation.getLongitude();
                double curLatitude = aMapLocation.getLatitude();
                LatLng cur = new LatLng(curLatitude, curLongitude);
                // 计算距离
                double distance = AMapUtils.calculateLineDistance(cur, to);
                System.out.println(distance);

                // 播放音乐
                if(isStart && distance < maxDistance) {
                    isStart = false;
                    MediaPlayer m = MediaPlayer.create(MainActivity.this, R.raw.alarm);
                    m.setLooping(true);
                    m.start();
                }

            }else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    };



    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//设置对应的XML布局文件
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // 验证隐私信息
        ServiceSettings.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyAgree(this,true);

        // 获取位置权限
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        }
                );
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        // 初始化组件
        isStart = false;
        start = findViewById(R.id.start);
        address = findViewById(R.id.address);
        max = findViewById(R.id.max);
        to = new LatLng(0, 0);

        //  蓝点设置，1s更新一次
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        mapView = (MapView) findViewById(R.id.map);
        aMap = mapView.getMap();
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
        mapView.onCreate(savedInstanceState);

        // 监听当前位置
        getCurrentLocation();

        // 注册监听
        addListener();



    }

    // 获取当前经纬度
    private void getCurrentLocation() {
        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(1000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    // 通过地址名，调用高德地图接口获取其经纬度
    private void getLocation(String addressName){
        GeocodeSearch geocodeSearch= null;
        try {
            geocodeSearch = new GeocodeSearch(this);
        } catch (AMapException e) {
            e.printStackTrace();
        }
        final boolean[] success = {true};
        assert geocodeSearch != null;
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

                if (i==1000){
                    try {

                        GeocodeAddress geocodeAddress = geocodeResult.getGeocodeAddressList().get(0);
                        double latitude = geocodeAddress.getLatLonPoint().getLatitude();//纬度
                        double longititude = geocodeAddress.getLatLonPoint().getLongitude();//经度
                        String adcode= geocodeAddress.getAdcode();//区域编码

                        Log.e("地理编码", geocodeAddress.getAdcode()+"");
                        Log.e("纬度",latitude+"");
                        Log.e("经度",longititude+"");

                        to = new LatLng(latitude, longititude);
                        aMap.clear();
                        aMap.addMarker(getMarkerOptions(to, addressName));
                        // 成功则设置标志位
                        isStart = true;
                        start.setText("暂停");
                    } catch (Exception e){
                        Toast.makeText(MainActivity.this,"目的地出错",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this,"目的地出错",Toast.LENGTH_SHORT).show();
                }
            }
        });
        GeocodeQuery geocodeQuery = new GeocodeQuery(addressName.trim(),"29");
        geocodeSearch.getFromLocationNameAsyn(geocodeQuery);
    }

    // 在图上打标记
    private MarkerOptions getMarkerOptions(LatLng point, String addressName) {
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(point.latitude, point.longitude));
        options.period(60);
        options.snippet(addressName);
        return options;
    }

    // 监听器
    private void addListener() {
        // 开始按钮
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStart) {
                    isStart = false;
                    start.setText("开始");
                    return;
                }
                // 获取阈值、地址
                String toAddress = String.valueOf(address.getText());
                String length = String.valueOf(max.getText());
                // 判断阈值格式
                try {
                    maxDistance = Double.parseDouble(length);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this,"阈值格式出错",Toast.LENGTH_SHORT).show();
                    return;
                }
                getLocation(toAddress);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(null != mLocationClient){
            mLocationClient.onDestroy();
        }
    }
}