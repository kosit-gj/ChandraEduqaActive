
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.goingjesse.app.service.*" %>   
<%

String query="";
query+="INSERT INTO kpi_result (result_id)VALUES (111111);";


jndiService jndidd= new jndiService();

jndidd.query(query,"jndiDB");
out.print(jndidd.getData());


%>
