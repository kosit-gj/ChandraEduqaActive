<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.goingjesse.app.service.*" %>   
<%
String paramImageName = request.getParameter("paramImageName");
String paramScreenName = request.getParameter("paramScreenName");


//paramImageName="2016063004161914707296.jpeg";
String query="";
query+="SELECT count(*) as countFIle FROM eduqa.kpi_evidence where evidence_path='"+paramImageName+"' and updated_by='"+paramScreenName+"'";

jndiService jndidd= new jndiService();
jndidd.selectByIndex(query, "1","jdbjndiEduqaActive");
out.print(jndidd.getData());

%>
