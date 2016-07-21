
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.goingjesse.app.service.*" %>   
<%

String query="";
query+="delete from kpi_result where result_id=111111";


jndiService jndidd= new jndiService();

jndidd.query(query,"jndiDB");
out.print(jndidd.getData());


%>
