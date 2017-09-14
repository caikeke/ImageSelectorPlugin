# ImageSelectorPlugin
Android图片选择插件
```
1.添加插件到项目
2.用android studio 打开项目，在res/values/string.xml中添加andorid/src/android/values/string.xml中的内容
3.按照添加依赖包.PNG，在AndroidStudio中添加包
4.使用方法
 let dataJson={
        imagePath:this.mImagePath,
      };
      cordova.plugins.ImageSelectorPlugin.selectImage(dataJson,(msg)=>{
        this.mImagePath=msg;
      },null);
