<!doctype html>
<%@ page import="com.iquanwai.confucius.biz.util.ConfigUtils" contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>圈外训练营</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
</head>
<body>
<script>
    window.ENV = {
        reactMountPoint: "react-app",
        userName: "${userName}",
        headImage:"${headImage}",
        // TODO 改到配置文件
        openPromo:${openPromo}
    }
</script>

<div id="react-app"></div>
<!-- 业务代码-->
<script src="${resource}"></script>
<script src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script>
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

    ga('create', '<%=ConfigUtils.gaId()%>', 'auto');
    ga('send', 'pageview');

</script>
<script>
    (function(window, mta) {
        window.MeituanAnalyticsObject = mta;
        window[mta] = window[mta] || function() {
            (window[mta].q = window[mta].q || []).push(arguments);
        };
    }(window, 'mta'));
    window.onload = function () {
        //页面名称
        mta('create', 'appPageName');
        //上报接口
        mta('config', 'beaconImage', '/performance/report');
        (function sendTime(){
            var timing = performance.timing;
            var loadTime = timing.loadEventEnd - timing.navigationStart;//过早获取时,loadEventEnd有时会是0
            if(loadTime <= 0) {
                // 未加载完，延迟200ms后继续times方法，直到成功
                console.warn('200 ms')
                setTimeout(function(){
                    sendTime();
                }, 200);
                return;
            } else {
                mta('send', 'page');
            }
        })()
    };
</script>
<script src="//www.iqycamp.com/script/mta.min.js"></script>
</body>
</html>