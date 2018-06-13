# 拍题SDK V0.0.9

## Mavent库集成

```gradle

   repositories {
        maven { url "http://nexus.abcpen.com/repository/release/" }
        ...
        
    }
    //相机库
    implementation 'com.abcpen:open_camera:0.0.9'
    //识别库
    implementation 'com.abcpen:recognition:0.0.9'

```

## 初始化SDK

> appkey appSecret 请联系笔声申请

```
        ABCPaiTiManager.getInstance().init(this, appkey, appSecret);

```

## 资格认证(Auth)

```
 public void authToSDK(View view) {
        ABCPaiTiManager.getInstance().authToSDK(new ABCCallBack() {
            @Override
            public void onSuccess(Object o) {
                //认证成功
            }

            @Override
            public void onFail(Exception e) {
              
            }
        });
    }
```


## 相机(CameraView)
>-  CameraView 使用方式和普通View一直 Xml布局中直接使用

```
  <com.abcpen.camera.sdk.CameraView
        android:id="@+id/camera"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

```

>- 初始化CameraView

```java
 void initCamera(Activity activity, Bundle savedInstanceState, OnPreviewStatusChangeListen onSurfaceCreatedListen)
```

>- 设置拍照回调

```java
//设置拍照回调
void setCameraResultListener(CameraTakePhotoLister lister)


interface CameraTakePhotoLister {
    //拍照成功
    void onTakePhotoCompile(Uri uri);
    //拍照失败
    void onTakePhotoFail();

}

```
>- 设置照片地址

```java 

 void setSavePhotoUri(Uri savePhotoUri)
 
```

>- 拍照

**拍照后会通过回调 返回拍照 成功 / 失败**

``` java
void takePhoto()
```

>- 后台操作

```java

// 切换 后台时候 释放相机
void onPause()

// 恢复相机
void onResume()

```

>- 其他Api

```java
  
  //相机旋转角度
  void setUIRotation(int uiRotation)
  
  //设置闪光灯 0关闭 1开启 2自动模式
  setFlash(int flash)
  
```

>- 添加网格

**SDK默认是不带网格的 如果需要使用 覆盖在Cameraview上 透明即可**


## 图片处理(ABCImageProcessingUtil)

>- SDK提供了 文字矫正 以及 图片质量识别 灰度图等

**文字矫正功能 会剪切图片 自动模式有剪切最大角度限制 文字倾斜度不可超过±15°   所有方法都可以在其他线程中操作**



 ```java
    //文字矫正  自动模式 SDK内部默认矫正最
    Bitmap changeAngleFont(Bitmap bitmap)
    
    //获取文字倾斜度 可自行处理
    float getAngleForBitMap(Bitmap bitmap)
    
    //获取图片质量是否合格
    boolean getBlurStatus(Bitmap bitmap)
    
    //转成灰度图
    Bitmap toGrayscale(Bitmap bmpOriginal)

 ```



## 图片上传 答案识别(ABCPaiTiManager)
```
//路径
uploadFile(String path, ABCFileCallBack<String> callBack)

//图片
uploadFile(String path, ABCFileCallBack<String> callBack)

```
>- CallBack介绍

```java
   /**
     * 上传成功
     * @param t
     */
    void onSuccess(T t);

    /**
     * 上传失败
     * @param e
     */
    void onFail(Exception e);

    /**
     * 上传进度
     * @param progress
     */
    void onUploadProgress(int progress);
````
**上传成功后会返回图片id 后面答案识别成功后会对应此图片ID**

## 答案识别
>- 图片上传完成后等待服务端识别结果

``` java
// 注册图片识别 监听
registerOnReceiveListener(ABCPaiTiAnswerListener listener)

// 销毁监听 
unRegisterOnReceiveListener(ABCPaiTiAnswerListener listener)   

```
**注册监听后 要记得销毁监听 不然会引起内存泄漏**

>- 监听说明

```java

  /**
     * 答案识别成功
     * @param imgId
     * @param paSingleModel
     */
    void onAnswerData(String imgId, PASingleModel paSingleModel);


    /**
     * 没有找到对应答案
     * @param imgId
     */
    void noGetAnswerData(String imgId);

```

>- Model说明

Model | 说明
---|---
AnswerModel | 识别详情 识别答案 问题详情等
QuestionResult | 图片详情 包含图片地址等
PASingleModel | 服务端返回的Json 对象 （包含 QuestionResult,AnswerModel）

**一般情况 服务端会返回最接近三条识别数据 也就是对应AnswerModel集合**


```java

class QuestionResult  {
	 public QuestionModel question;
	 public ArrayList<AnswerModel> answers;
}

public class AnswerModel implements Parcelable {


   
    //题目ID
    public String question_id;
    
    //普通文本
    public String question_body;
    
    //Html 样式文本
    public String question_html;
    
    // 题目标签
    public String question_tag;
    
    //答案详情 Html样式
    public String quesiton_answer;


   

```



### 样式补充说明：HTML的方式展示。

步骤： 1，页面HTML，拷贝 main/assets 文件夹下www目录到app的对应目录中


2，拷贝demo 中的jsplugin目录下所有java文件（MyPlugin，Answer，ClassModel）和 res 的config.xml目录到app的对应文件夹。

注意：这里Myplugin如果修改包名，请在config.xml中也修改成相应的包名

	//xml file in res
	<feature name="MyPlugin">
        	<param name="android-package" value="com.abcpen.simple.jsplugin.MyPlugin" />
    </feature>

3，参考ResultActivity ，使用cordovawebview加载页面
	
	        loadUrl("file:///android_asset/www/index.html");
	        
4，参考ResultActivity 实现showquestion等的调用装载数据的逻辑

	 public JSONObject showQuestion() {
        	if (TextUtils.isEmpty(mImageId) || mContent == null || mContent.size() == 0) {
       	   	  return showEmptyQuestion();
       		 }
       		 return genQuestionObj(2);
 	   }


--------------------------------------------------------------------------------------

####UpdateLog v0.0.9

1,更新aar 到v0.0.9 

    //相机库
    implementation 'com.abcpen:open_camera:0.0.9'
    //识别库
    implementation 'com.abcpen:recognition:0.0.9'

2,update h5 css ，统一引用all.css .


