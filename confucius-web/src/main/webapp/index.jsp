<!doctype html>
<%@ page import="com.iquanwai.confucius.biz.util.ConfigUtils" contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
</head>
<body>
<script>
    window.ENV = {
        reactMountPoint: "react-app",
    }
</script>

<div id="react-app"></div>
<h2>Hello World!</h2>
</br>
<h3>Hello Java! I am confucius Web...</h3>
<!-- 业务代码-->
<script src="<%=ConfigUtils.staticResourceUrl()%>/bundle.js"></script>

</body>
</html>