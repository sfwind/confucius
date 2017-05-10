<!doctype html>
<%@ page import="com.iquanwai.confucius.biz.util.ConfigUtils" contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>圈外</title>
    <link href="//www.iqycamp.com/images/logo.png" rel="shortcut icon">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
</head>
<body>
<script>
    window.ENV = {
        reactMountPoint: "react-app",
        userName: "${userName}",
        headImage:"${headImage}",
        loginSocketUrl:"${loginSocketUrl}",
        isDevelopment:${isDevelopment},
        feedBack:${feedBack},
        openFeedBack:${openFeedBack},
        openComment:${openComment},
        backend:false,
        roleId:${roleId},
        signature:${signature},
    }
</script>

<div id="react-app"></div>
<script src="//res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js"></script>
<!-- 业务代码-->
<script src="${resource}"></script>

<script>
    var display = '<%=ConfigUtils.domainName()%>'
    if(display === 'http://www.iquanwai.com') {
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