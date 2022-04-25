# GPS闹钟实验报告

一开始我的想法是用NASA的地图服务功能，不过画质太差了，然后就用高德地图的API做了。

高德地图的缺点在于使用起来需要认证，但是有更丰富的API，生态也很好。

<img src="file:///C:\Users\PC\Documents\Tencent Files\1966069940\Image\C2C\B7BE944FA3026C284F51B3AFCF8C1726.jpg" alt="img" style="zoom:30%;" />

## 1. 代码实现

### 1.1 获取高德API的key

1. 首先创建一个JSK文件，然后使用Toolkey(JDK)命令，解析获取正式版的SHA
2. 在.Android下有一个debug.keystore，可以获取到所有APP的debug版本SHA
3. 填入SHA获得Key

### 1.1 申请各种各样的权限

1. [请求位置权限  | Android 开发者  | Android Developers](https://developer.android.com/training/location/permissions?hl=zh-cn)
2. [入门指南-Android 定位SDK|高德地图API (amap.com)](https://lbs.amap.com/api/android-location-sdk/gettingstarted)

### 1.2 获取当前的实时位置，并显示动态更新

```java
myLocationStyle = new MyLocationStyle();
myLocationStyle.interval(2000);
aMap = mapView.getMap();
aMap.setMyLocationStyle(myLocationStyle);
aMap.setMyLocationEnabled(true);
```

### 1.3 获取当前经纬度

```java
     //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation.getErrorCode() == 0) {
				 // 解析aMapLocation，获取当前的位置信息
                double curLongitude = aMapLocation.getLongitude();
                double curLatitude = aMapLocation.getLatitude();
            }else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    };

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
```

### 1.4 标目的点

一开始我的想法是点击屏幕上的点完成标点，但是发现高德地图没有提供这样的API，我需要自己解析屏幕的位置，然后计算出其经纬度，然后通过高德的API完成标点。后来偶然间看到可以通过POI搜索出经纬度，然后用经纬度完成标点。

[(106条消息) Android 高德地图根据地址获取经纬度，计算两个坐标的距离_meixi_android的博客-CSDN博客_android 高德根据地名获取经纬度](https://blog.csdn.net/meixi_android/article/details/84971627)

```java
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
```

测试结果：这样的标点方式是很准的，可以满足我们的需求。当找不到这个位置的时候，也会返回错误信息。

### 1.5 计算经纬度

```java
// 这个是计算直线距离
double distance = AMapUtils.calculateLineDistance(cur, to);	
```

### 1.6 输入框挤乱布局

在onCreate函数种添加

```java
getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
```



## 2. 结果展示

p1：搜索不存在地址提示出错误

p2：正确标准搜索位置

p3：3D图标准中心校区（7000米时候闹钟响了，证明直线距离小于7000m）

p4：测试在科研楼附近，到图书馆的距离小于100m，闹钟响了

<img src="file:///C:\Users\PC\Documents\Tencent Files\1966069940\Image\C2C\4D5A1C4FC30448501A7AA6026BD55D69.jpg" alt="img" style="zoom:33%; float:left;	margin-left:200px;" /><img src="file:///C:\Users\PC\Documents\Tencent Files\1966069940\Image\C2C\D6BEADDE7FFEC2E646A5343AF99D7C0F.jpg" alt="img" style="zoom:33%; float:left; margin-left:200px;" />



































<img src="file:///C:\Users\PC\Documents\Tencent Files\1966069940\Image\C2C\F04CDE2E087961CE51F600FE05AEBA61.jpg" alt="img" style="zoom:33%; margin-left:200px;float:left;" /><img src="file:///C:\Users\PC\Documents\Tencent Files\1966069940\Image\C2C\D03977960CFE0858544E9B1E510407AA.jpg" alt="img" style="zoom:33%; float:left; margin-left:200px" />































