# 短时间倒计时工具

> 主要用于短信验证码倒计时，相应超时倒计时等短时间倒计时场景。
>
> 短信验证码倒计时即使退出App，再次回到页面倒计时依旧同步。

## 特色

1. 可以轻松的实现返回后倒计时依然同步的功能。
2. 可以同时开启多个倒计时且并不损耗性能。

## 工作方式

多个倒计时任务根据**tag**来区分。
当你不再关注某个倒计时回调的时候（调用了unregist方法），后台的timer类也会随之停止，同时会将当前的倒计时任务记录到文件中，当你再次注册回调的时候，他会告诉还剩多少秒，或者已经倒计时完毕了。

开启倒计时后离开页面，倒计时任务并不会在后台继续执行，只会在下次注册进来的时候进行时间同步，所以不必担心性能消耗问题。

## 使用

我们用获取短信验证码为例：

```java
// 在onCreate或页面初始化的地方调用
CountDownUtil.getInstance().register("getMsgCode", new CountDownCallback(){
      @Override
        public void onTimerTick(int sec) {
          	// 每秒调用一次该方法
            textView.setText("倒计时:" + sec);
        }

        @Override
        public void onTimerPause() {
						// 当倒计时暂停时候调用，基本用不到
        }

        @Override
        public void onTimerStateChange(int state, int sec) {
          	// 第一次注册/倒计时结束 的时候调用
            if (sec == -1) {
              	// 如果是倒计时 完毕/未开始 的状态，sec为-1
          			// 当然你可以使用state来获取更详细的状态
                textView.setText("获取验证码");
            } else {
              	// 如果这个倒计时任务还有剩余时间
               	// 直接调用开始任务即可继续上次的倒计时，此时第二个参数不生效
                CountDownUtil.getInstance().startCountDown("getMsgCode", 60);
            }
        }
});

// 在获取验证码的点击事件上开始60秒倒计时
CountDownUtil.getInstance().startCountDown("getMsgCode", 60);

// 在onPause()方法中取消注册
CountDownUtil.getInstance().unregister("getMsgCode");


```

## 其他

直接下载源码拷到项目里用即可，工具类还未完善，本意是想封装一整套工具类，未完待续。