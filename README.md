# ImageSelectorPlugin
Android图片选择插件
```
1.按照添加依赖包，在AndroidStudio中添加包
2.使用方法
 let dataJson={
        imagePath:this.mImagePath,
      };
      cordova.plugins.ImageSelectorPlugin.selectImage(dataJson,(msg)=>{
        this.mImagePath=msg;
      },null);
