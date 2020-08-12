# 调用系统相机  相册
  调用系统相机拍照大致过程\n
        1.创建File对象 用于存放摄像头拍下的图片 参数为关联缓存目录，图片取名output.jpg\n\n


        2.对系统版本进行判断，低于7.0 将File对象转换成Uri对象(标识着图片地本地真实地址)\n
        否则转换为封装过的Uri对象（可以选择性地将封装过地Uri共享给外部，\n
        使用了和ContentProvider类似地机制来对数据进行保护）\n\n


        3.构建Intent对象 action指定为android.media.action.IMAG_CAPTURE\n
        再调用putExtra()方法指定图片输出地址\n
        最后调用startActivityForResult()启动Activity 打开相机程序 拍下地照片将会输出到output.jpg\n\n


        4.拍完照后有结果返回到onActivityResult()中，\n
        如果拍照成功 就将照片解析为Bitmap对象  设置到ImageView中\n\n

        
        5.调用相机拍照，照片储存在其他SD卡目录 点击确定的时候，基本会马上有反应。\n\n