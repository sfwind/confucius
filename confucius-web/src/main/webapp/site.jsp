<!doctype html>
<%@ page import="com.iquanwai.confucius.biz.util.ConfigUtils" contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8" %>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>圈外</title>
    <link href="//www.iqycamp.com/images/logo.png" rel="shortcut icon">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
</head>
<body>
<!-- 腾讯点播器 -->
<link href="//imgcache.qq.com/open/qcloud/video/tcplayer/tcplayer.css" rel="stylesheet">
<script src="//imgcache.qq.com/open/qcloud/video/tcplayer/tcplayer.min.js"></script>
<script>
    window.ENV = {
        reactMountPoint: "react-app",
        isDevelopment:${isDevelopment},
        sensorsProject:"${sensorsProject}",
    }
</script>

<div id="react-app"></div>
<script src="//res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js"></script>
<!-- 业务代码-->
<script src="${resource}"></script>

<script>
    var display = '<%=ConfigUtils.domainName()%>'
    if (display === 'http://www.iquanwai.com' || display === 'https://www.iquanwai.com') {
        var _hmt = _hmt || [];
        (function () {

            var hm = document.createElement("script");
            hm.src = "https://hm.baidu.com/hm.js?64c8a6d40ec075c726072cd243d008a3";
            var s = document.getElementsByTagName("script")[0];
            s.parentNode.insertBefore(hm, s);
        })();
    }

</script>

</body>
</html>