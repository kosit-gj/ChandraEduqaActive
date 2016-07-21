
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.goingjesse.app.service.*" %>   
<%
String paramXXX = request.getParameter("paramXXX");
String query="";
query+=" select * from kpi_result LIMIT 5";
jndiService jndidd= new jndiService();
jndidd.selectByIndex(query, "1,2,4","jndiKMU");
out.print(jndidd.getData());

%>
