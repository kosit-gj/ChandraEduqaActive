<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ page contentType="text/html; charset=utf-8" %> 
<%@ page import="javax.portlet.PortletURL" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="java.sql.*" %>

<%-- <portlet:actionURL var="formActionInsert"> 
	<portlet:param name="action" value="doInsert"/>
</portlet:actionURL> 
<portlet:actionURL var="formActionEdit">
	<portlet:param name="action" value="doEdit"/>
</portlet:actionURL> 
<portlet:actionURL var="formActionDelete">
	<portlet:param name="action" value="doDelete"/>
</portlet:actionURL> 
<portlet:actionURL var="formActionSearch">
	<portlet:param name="action" value="doSearch"/>
</portlet:actionURL> 
<portlet:actionURL var="formActionListPage">
	<portlet:param name="action" value="doListPage"/>
</portlet:actionURL>  --%>
<portlet:resourceURL var="requestOrgList" id="requestOrgList" ></portlet:resourceURL>
<portlet:resourceURL var="requestRecomndList" id="requestRecomndList" ></portlet:resourceURL>
<portlet:resourceURL var="requestRecomndInsert" id="requestRecomndInsert" ></portlet:resourceURL>
<portlet:resourceURL var="requestRecomndDelete" id="requestRecomndDelete" ></portlet:resourceURL>
<portlet:resourceURL var="requestRecomndUpdate" id="requestRecomndUpdate" ></portlet:resourceURL>

<portlet:resourceURL var="dofindOrgByUserName" id="dofindOrgByUserName" ></portlet:resourceURL>
<portlet:resourceURL var="dofindOrgByOrgId" id="dofindOrgByOrgId" ></portlet:resourceURL>
<portlet:resourceURL var="requestOrgFaculty" id="requestOrgFaculty" ></portlet:resourceURL>
<portlet:resourceURL var="requestOrgCourse" id="requestOrgCourse" ></portlet:resourceURL>
<portlet:resourceURL var="requestOrgIdByOrgDetailFilter" id="requestOrgIdByOrgDetailFilter" ></portlet:resourceURL>

<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <!-- Bootstrap core CSS --> 
    <link rel="stylesheet" href="<c:url value="/resources/bootstrap/css/bootstrap.min.css"/>" type="text/css"/>
    <link rel="stylesheet" href="<c:url value="/resources/bootstrap/css/bootstrap-select.css"/>" type="text/css"/>
    <link rel="stylesheet" href="<c:url value="/resources/bootstrap/css/bootstrap-responsive.min.css"/>" type="text/css"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/common-element.css"/>" type="text/css"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/jquery-ui.min.css"/>"/>
    <script src="<c:url value="/resources/js/jquery-1.11.2.min.js"/>"></script> 
    <script  src="<c:url value="/resources/js/jquery-ui.min.js"/>"></script>
    <script src="<c:url value="/resources/bootstrap/js/bootstrap.min.js"/>"></script>
	<script src="<c:url value="/resources/js/confirm-master/jquery.confirm.min.js"/>"></script>

    <script type="text/javascript"> 
    	$( document ).ready(function() { 
    		ToggleEnableSelection();
    		renderParameterCtrl(getUserRoleLevelID());
    	}); 

		function ToggleEnableSelection(){
			var uni  =  $("#paramUniversity");
			var fac  =  $("#paramFaculty");
			var cou = $("#paramCourse");
			var lv = $('#paramLevel').val();
			if(lv==1){ 
				uni.prop("disabled",false); 
				fac.prop("disabled",true); 
				cou.prop("disabled",true); 
			}else if(lv==2){ 
				uni.prop("disabled",false);
				fac.prop("disabled",false); 
				cou.prop("disabled",true); 
			}else if(lv==3){ 
				uni.prop("disabled",false); 
				fac.prop("disabled",false); 
				cou.prop("disabled",false); 
			}
		}

		function getUserRoleLevelID(){
			var userRoleLevel;
			$.ajax({ 
				dataType:'json',
				url: "<%=dofindOrgByUserName%>",
				async: false,
				data: { },
				success: function(data){
					userRoleLevel = data["userRoleId"][0]["levelId"];
				}
			});
			return userRoleLevel;
		}

		function renderParameterCtrl(userLevelId){
			var paramLevel = $('select#paramLevel').val();
			var paramUniversity  = $("#paramUniversity");
			var paramFaculty  =  $("#paramFaculty");
			var paramCourse = $("#paramCourse");

			//ตรวจสอบสิทธิ์ของผู้ใชงานและทำการส้ราง Parameter ตามสิทธิ์
			if(userLevelId == 1){
				if(paramLevel == 1){
					//Do not thing
				}else if(paramLevel == 2){
					//Generate paramFaculty and set defalut value
					ParamChange(paramUniversity, 'university');
					if(${currentFaculty} != 0){
						paramFaculty.val(${currentFaculty});
					}
				}else if(paramLevel == 3){
					//Generate paramFaculty and set defalut value
					ParamChange(paramUniversity, 'university');
					if(${currentFaculty} != 0){
						paramFaculty.val(${currentFaculty});
					}

					//Generate paramCoures and set defalut value
					ParamChange(paramFaculty, 'faculty');
					if(${currentCourse} != 0){
						paramCourse.val(${currentCourse});
					}
				}
			}

			else if(userLevelId == 2){
				$("#paramLevel option[value=1]").prop("disabled", true);
				if(paramLevel == 2){
					//Generate paramFaculty and set defalut value
					ParamChange(paramUniversity, 'university');
					if(${currentFaculty} != 0){
						paramFaculty.val(${currentFaculty});
					}
				}else if(paramLevel == 3){
					//Generate paramFaculty and set defalut value
					ParamChange(paramUniversity, 'university');
					if(${currentFaculty} != 0){
						paramFaculty.val(${currentFaculty});
					}

					//Generate paramCoures and set defalut value
					ParamChange(paramFaculty, 'faculty');
					if(${currentCourse} != 0){
						paramCourse.val(${currentCourse});
					}
				}
			}

			else if(userLevelId==3){
				$('select#paramLevel option[value=1]').prop("disabled", true);
				$('select#paramLevel option[value=2]').prop("disabled", true);         
				$.ajax({ 
					dataType:'json',
					url: "<%=dofindOrgByOrgId%>",
					data: { 'orgId': $('input#paramOrg').val() },
					success: function(data){ 
						//Generate paramFaculty             
						facultyOpt = $("<option value=\""+data["facultyCourseList"][0]["facultyCode"]
						+"\"></option>").html(data["facultyCourseList"][0]["facultyName"]);
						paramFaculty.empty().append(facultyOpt);

						//Generate paramCourse
						courseOpt = $("<option value=\""+data["facultyCourseList"][0]["courseCode"]
						+"\"></option>").html(data["facultyCourseList"][0]["courseName"]);
						paramCourse.empty().append(courseOpt);
					}
				});
			}
		}

		function ParamLevelChange(el){
			ToggleEnableSelection();

			var lv = $('#paramLevel').val();
			if(lv==1){
				$("#paramFaculty").val(0);
				$("#paramCourse").val(0);
				$('select#paramFaculty option').attr('disabled', 'disabled');
				$('select#paramCourse option').attr('disabled', 'disabled');

				getOrgIdByOrgDetailFilter();
			}else if(lv==2){          
				$('select#paramCourse option').attr('disabled', 'disabled');
				ParamChange($('select#paramUniversity'), 'university');
			}else if(lv==3){ 
				ParamChange($('select#paramUniversity'), 'university');
				ParamChange($('select#paramFaculty'), 'faculty');
			}
		}

		function ParamChange(el, changeType){
			var sups = ["university","faculty","course"];
			var value = $(el).val();
			var elUniversity  = $("#paramUniversity");
			var elFaculty  =  $("#paramFaculty");
			var elCouse = $("#paramCourse");

			if(changeType == sups[0]){ // university change
				$.ajax({ 
					dataType:'json',
					url: "<%=requestOrgFaculty%>",
					async: false,
					data: { 'levelId': value , 'university':elUniversity.val()  } ,
					success: function(data){
						GenParamFacultyList(elFaculty,data["lists"]);
						var x = [];
						GenParamCourseList(elCouse,x);
					}
				});

				getOrgIdByOrgDetailFilter();
			}

			else if(changeType == sups[1]){ // faculty change
				$.ajax({ 
					dataType:'json',
					url: "<%=requestOrgCourse%>",
					data: { 'levelId': value , 'university':elUniversity.val(), 'faculty': elFaculty.val()  } ,
					async: false,            
					success: function(data){
						//  alert(JSON.stringify(data));
						if($('select#paramLevel').val() == "3"){
							GenParamCourseList(elCouse,data["lists"]);
						}

						//Remove class redBorder if not null value    
						if(elCouse.val() != null){
							elCouse.removeClass("redBorder");
						} 
					}
				});

				getOrgIdByOrgDetailFilter();
			}

			else if(changeType == sups[2]){ 
				//Remove class redBorder if not null value  
				if(elCouse.val() != null){
					elCouse.removeClass("redBorder");
				}

				getOrgIdByOrgDetailFilter();
			}
		}

 		function GenParamFacultyList(target,data){
			//  var target = $('#'+elFaculty);
			target.empty();
			var opt;
			for(var i=0; i<data.length; i++){
				opt = $("<option value=\'"+data[i]["id"]+"\'></option>").html(data[i]['name']);
				target.append(opt);
			}
		}

		function GenParamCourseList(target,data){
			// var target = $('#'+elCourse);
			target.empty();
			var opt; //= $("<option value='0'></option>").html("");
			target.append(opt);
			for(var i=0;i<data.length;i++){
				if(!(jQuery.isEmptyObject(data[i]))){
					opt = $("<option value=\""+data[i]['id']+"\"></option>").html(data[i]['name']);
					target.append(opt);
				}         
			} 
		}

		function getOrgIdByOrgDetailFilter(){
			var paramLevel = $("select#paramLevel").val() || 0;
			var paramUniversity  = $("select#paramUniversity").val() || 0;
			var paramFaculty  =  $("select#paramFaculty").val() || 0;
			var paramCourse = $("select#paramCourse").val() || 0;
			console.log("paramLevel:"+paramLevel+", paramUniversity:"+paramUniversity+", paramFaculty:"+paramFaculty+", paramCourse:"+paramCourse);

			$.ajax({ 
				dataType:'json',
				url: "<%=requestOrgIdByOrgDetailFilter%>",
				data: { 
					'paramLevel':paramLevel,
					'paramUniversity':paramUniversity.toString(), 
					'paramFaculty':paramFaculty.toString(), 
					'paramCourse': paramCourse.toString() 
				},
				async: false,            
				success: function(data){
					$("input#paramOrg").val(data["lists"][0]["orgId"]);
				}
			});
		}

    	/* bind element event */  
    	function actSubmit(){
    		//Get parameter value from element param.
    		var paramYear = $.trim($('#paramYear').val()) || '';
    		var paramGroup = $.trim($('#paramGroup').val()) || '';
    		var paramOrg = $.trim($('#paramOrg').val()) || '';
    		//console.log('paramYear:'+paramYear+', paramGroup:'+paramGroup+', paramOrg:'+paramOrg );
    		var recWFlag, recSFlag;
    		$('table#RecomdFlagS, table#RecomdFlagW').empty();
	    	$.ajax({	    		
    			dataType:'json',
    			url: '<%=requestRecomndList%>' ,
    			data: { 'paramYear':paramYear, 'paramGroup':paramGroup, 'paramOrg':paramOrg } ,
    			success: function(data){
					var dataObj = data['recomndLists'];

					//Null Value
					if(dataObj.length == 0){
						var tbodyStr = 
							"<tr> "
							+	"<td> ไม่พบข้อมูล </td"	
							+"</tr>";
						$('table#RecomdFlagS').append(tbodyStr);
		    			$('table#RecomdFlagW').append(tbodyStr);
					}

		    		//Generate data for data grid.
					for(var i=0;i<dataObj.length;i++){
						if(dataObj[i]['reComndFlag'] == 'S'){
							recSFlag = 
								"<tr> "
								+	"<td>"+dataObj[i]['reComndDesc'].replace(/\n/g, "<br/>")+"</td> "							
								+	"<td align=\"center\"> "
								+	"<img heigth=\"20\" width=\"20\" onClick=\"actEdit(this, 'S')\" src=\""+"<c:url value='/resources/images/edited.png'/>"+"\"> "
								+	"</td> "
								+	"<td align=\"center\"> "
								+	"<img heigth=\"20\" width=\"20\" onClick=\"actDelete(this)\" src=\""+"<c:url value='/resources/images/delete.png'/>"+"\"> "
								+	"</td> "
								+"<td>"+dataObj[i]['reComndId']+"</td> "
								+"<td>"+dataObj[i]['orgId']+"</td> "
								+"<td>"+dataObj[i]['groupId']+"</td> "
								+"<td>"+dataObj[i]['reComndFlag']+"</td> "
								+"<td>"+dataObj[i]['createdDate'].replace(".0", "")+"</td> "
								+"<td>"+dataObj[i]['createdBy']+"</td> "
								+"</tr> ";
							$('table#RecomdFlagS').append(recSFlag);
							
						}else if(dataObj[i]['reComndFlag'] == 'W'){
							recWFlag = 
								"<tr> "
								+	"<td>"+dataObj[i]['reComndDesc'].replace(/\n/g, "<br/>")+"</td> "							
								+	"<td align=\"center\"> "
								+	"<img heigth=\"20\" width=\"20\" onClick=\"actEdit(this, 'W')\" src=\""+"<c:url value='/resources/images/edited.png'/>"+"\"> "
								+	"</td> "
								+	"<td align=\"center\"> "
								+	"<img heigth=\"20\" width=\"20\" onClick=\"actDelete(this)\" src=\""+"<c:url value='/resources/images/delete.png'/>"+"\"> "
								+	"</td> "
								+"<td>"+dataObj[i]['reComndId']+"</td> "
								+"<td>"+dataObj[i]['orgId']+"</td> "
								+"<td>"+dataObj[i]['groupId']+"</td> "
								+"<td>"+dataObj[i]['reComndFlag']+"</td> "
								+"<td>"+dataObj[i]['createdDate'].replace(".0", "")+"</td> "
								+"<td>"+dataObj[i]['createdBy']+"</td> "
								+"</tr> ";
							$('table#RecomdFlagW').append(recWFlag);							
						}
					}									
    			}
    		});
    	}
    	
    	function actSaveInsert(){
   	 		if($.trim($('#fReComndDesc').val()) == ""){
   	 			$('label#ckInputText').css( "display", "block" ).fadeOut( 5000 );
   	 		}else{
	   	 		var valYear = $.trim($('#paramYear').val()) || '';
	    		var valGroup = $.trim($('#paramGroup').val()) || '';
	    		var valOrg = $.trim($('#paramOrg').val()) || '';
	    		var valFlag = $.trim($('#fReComndFlag').val()) || '';
	    		var valDesc = $.trim($('#fReComndDesc').val()) || '';
	    		//console.log('valYear:'+valYear+', valGroup:'+valGroup+', valOrg:'+valOrg+
	    		//		', valFlag:'+valFlag+', valDesc:'+valDesc);
   	 			$.ajax({	    		
	    			dataType:'json',
	    			url: '<%=requestRecomndInsert%>' ,
	    			data: { 'valYear':valYear, 'valGroup':valGroup, 
	    				'valOrg':valOrg, 'valFlag':valFlag, 'valDesc':valDesc } ,
	    			success: function(data){
	    				//console.log(data);
	    				//console.log(eval(data['insertStatus']));
	    				if(eval(data['insertStatus']) == '1'){
	    					$('#fReComndDesc').val("");
	    			  		$('#formActRe').slideToggle();
	    			  		$('#searchData').click();
	    			  		
	    				}else{
	    					$('span#headMsg').html(data['insertMsg']);
	    					$('div#msgAlert').css( "display", "block" ).fadeOut( 10000 );
	    				} 
	    			}
    			});
   	 		}
   	 	}
    	
    	function actSaveEdit (){
    		if($.trim($('#fReComndDesc').val()) == ""){
   	 			$('label#ckInputText').css( "display", "block" ).fadeOut( 5000 );
   	 		}else{
   	 			var valId = $.trim($('#fReComndId').val()) || '';
	   	 		var valYear = $.trim($('#paramYear').val()) || '';
	    		var valGroup = $.trim($('#paramGroup').val()) || '';
	    		var valOrg = $.trim($('#paramOrg').val()) || '';
	    		var valFlag = $.trim($('#fReComndFlag').val()) || '';
	    		var valDesc = $.trim($('#fReComndDesc').val()) || '';
	    		var valCreateDate = $.trim($('#fCreateDate').val()) || '';
	    		var valCreateBy = $.trim($('#fCreateBy').val()) || '';
	    		//console.log('valYear:'+valYear+', valGroup:'+valGroup+', valOrg:'+valOrg+
	    		//		', valFlag:'+valFlag+', valDesc:'+valDesc);
   	 			$.ajax({	    		
	    			dataType:'json',
	    			url: '<%=requestRecomndUpdate%>' ,
	    			data: { 'valId':valId, 'valYear':valYear, 'valGroup':valGroup, 
	    				'valOrg':valOrg, 'valFlag':valFlag, 'valDesc':valDesc,
	    				'valCreateDate':valCreateDate, 'valCreateBy':valCreateBy} ,
	    			success: function(data){
	    				console.log(data);
	    				//console.log(eval(data['insertStatus']));
	    				if(eval(data['updateMsgCode']) == '1'){
	    					$('#fReComndDesc').val("");
	    			  		$('#formActRe').slideToggle();
	    			  		$('#searchData').click();
	    			  		
	    				}else{
	    					$('span#headMsg').append(data['updateMsgDesc']);
	    					$('div#msgAlert').css( "display", "block" ).fadeOut( 5000 );
	    				} 
	    			}
    			});
   	 		};
    	}
    	
    	function actDelete(el){   	
   	 		var dataId = parseInt($(el).parent('td').parent('tr').children('td:nth-child(4)').html());
	    	var dataName = $(el).parent('td').parent('tr').children('td:nth-child(1)').text();
	    	//console.log('DataId:'+dataId+', DataName:'+dataName);
	   	 	$.confirm({
		   	     text: "ยืนยันการลบเนื้อหารายงานเพิ่มเติม \"".concat(dataName, "\""),
		   	     title: "ลบเนื้อหารายงานเพิ่มเติม",
		   	     confirm: function(button) {
		   	 		<%-- $('#kpiGroupTypeForm').attr("action","<%=formActionDelete%>");
			 		$('#kpiGroupTypeForm '+'#fGroupTypeId').val(dataId);
			 		$('#kpiGroupTypeForm').submit(); --%>
			 		$.ajax({	    		
		    			dataType:'json',
		    			url: '<%=requestRecomndDelete%>' ,
		    			data: { 'recomndId':dataId } ,
		    			success: function(data){
		    				//console.log(data);
		    				//console.log(eval(data['insertStatus']));
		    				if(eval(data['deleteMsgCode']) == '1'){
		    			  		$('#searchData').click();
		    			  		
		    				}else{		    					
		    					$('span#headMsg').empty().append(data['deleteMsgDesc']);
		    					$('div#msgAlert').css( "display", "block" ).fadeOut( 5000 );
		    				} 
		    			}
	    			});
		   	     },
		   	     cancel: function(button) {
		   	         // nothing to do
		   	     },
		   	     confirmButton: "ตกลง",
		   	     cancelButton: "ยกเลืก",
		   	     post: true,
		   	     confirmButtonClass: "btn-primary",
		   	     cancelButtonClass: "btn-danger",
		   	     dialogClass: "modal-dialog modal-lg" // Bootstrap classes for large modal
		   	 });
   	 	}
    	
    	function actCancel(el){
	  		//dialog.dialog( "close" );
	  		$('#fReComndDesc').val("");
	  		$('#formActRe').slideToggle('slow');
	  	}
 		
 		function actAdd(el, flag){
 			//move input div to inner content
 			if(flag == 'S'){
 				$('#formActRe').appendTo("#complainFS>.wrapContainer");
 			}
 			else{
 				$('#formActRe').appendTo("#complainFW>.wrapContainer");
 			}
   	 		renderDialog('#formActRe',1,'','',flag); 	 		
   	 	}
 		
    	function actEdit(el, flag){
    		if(flag == 'S'){
 				$('#formActRe').appendTo("#complainFS>.wrapContainer");
 			}
 			else{
 				$('#formActRe').appendTo("#complainFW>.wrapContainer");
 			}
    		var dataDesc = [];
   	 		var dataId = parseInt($(el).parent('td').parent('tr').children('td:nth-child(4)').html());
   	 		dataDesc['dataName'] = $(el).parent('td').parent('tr').children('td:nth-child(1)').html();
   	 		dataDesc['orgId'] = $(el).parent('td').parent('tr').children('td:nth-child(5)').html();
   			dataDesc['groupId'] = $(el).parent('td').parent('tr').children('td:nth-child(6)').html();
   	 		dataDesc['createDate'] = $(el).parent('td').parent('tr').children('td:nth-child(8)').html();
   	 		dataDesc['createBy'] = $(el).parent('td').parent('tr').children('td:nth-child(9)').html();
   	 		//console.log('DataId:'+dataId+', DataName:'+dataDesc['dataName']+', createDate:'+dataDesc['createDate']+
   	 		//		', createBy:'+dataDesc['createBy']+', orgId:'+dataDesc['groupId']+', groupId:'+dataDesc['groupId']);	
   	 		renderDialog('#formActRe',2,dataId,dataDesc,flag);
   	 	}
    	
 		function renderDialog(d1,mode,dataId,dataDesc,flag){
   	 		/*mode 1:insert 2:edit*/
   	 		var head,event,headDetail;
   	 		if(mode==1){
	   	 		head = 'เพิ่ม';
	 			event= 'actSaveInsert()';
	 			$(d1).find('#fReComndDesc').val("");
	 			$(d1).find('input[type=hidden]#fOrgId').val($("#paramOrg").val());
	 			$(d1).find('input[type=hidden]#fGroupId').val($("#paramGroup").val());
	 		}else if(mode==2){
	 			head = 'แก้ไข';
	 			event='actSaveEdit()';
	 			$(d1).find('#fReComndDesc').val(dataDesc["dataName"].replace(/\<br>/g, "\n"));
	 			$(d1).find('input[type=hidden]#fOrgId').val(dataDesc["orgId"]);
	 			$(d1).find('input[type=hidden]#fGroupId').val(dataDesc["groupId"]);
	 		}
   	 		/*flag S:จุดเด่น W:จุดด้อย*/
   	 		if(flag == 'S'){
				headDetail = 'จุดเด่น'
				$(d1).find('input[type=hidden]#fReComndFlag').val("S");
			}else if(flag == 'W'){
				headDetail = 'จุดที่ต้องพัฒนา'
				$(d1).find('input[type=hidden]#fReComndFlag').val("W");
			}
   	 		$(d1).find('span').html(head);
   	 		$("span.headDetail").html(headDetail);
   	 		$(d1).find('input[type=hidden]#fReComndId').val(dataId);
   	 		$(d1).find('input[type=hidden]#fCreateBy').val(dataDesc["createBy"]);
   			$(d1).find('input[type=hidden]#fCreateDate').val(dataDesc["createDate"]);	
   			$(d1).find('input[type=button].save').attr('onClick',event);

		   if ( $(d1).is(':visible')) {
		   		$(d1).slideToggle("fast");
		   		//return false ;
		   	}else{
		   		$(d1).slideToggle("slow");
		   	}
   	 	}
 		
   	</script>
  
   	<style type="text/css">
   		div.boxAct{
			padding: 20px 20px 20px 20px;
			border: thin solid #CDCDCD;
			/*border-radius: 10px;*/
			display: block; 
		}
		table.tableGridTp{
   			background-color:#FFFFFF;
    		border:1px solid #999999;
    		overflow:hidden;
    		width:100%;
   			padding-top:10px;
   			font-size:16px;
   		}
   		table.tableGridTp td:nth-child(1){ width:auto; }
   		table.tableGridTp td:nth-child(2){ width:80px; }
   		table.tableGridTp td:nth-child(3){ width:80px; }
   		table.tableGridTp td:nth-child(4){ width:0%; display:none;}
   		table.tableGridTp td:nth-child(5){ width:0%; display:none;}
   		table.tableGridTp td:nth-child(6){ width:0%; display:none;}
   		table.tableGridTp td:nth-child(7){ width:0%; display:none;}
   		table.tableGridTp td:nth-child(8){ width:0%; display:none;}
   		table.tableGridTp td:nth-child(9){ width:0%; display:none;}
   		table.tableGridTp tbody td {
			border: none;
			padding: 5px 10px 5px 10px;
		}
   		table.tableGridTp tbody tr:nth-child(2n) td{ background-color:rgba(244,244,244,1); }
		button.btnPag {
		    background-color: Transparent;
		    background-repeat:no-repeat;
		    border: none;
		    cursor:pointer;
		    overflow: hidden;
		    outline:none;
		}
		div.complainContent{
			margin-bottom:10px;
			padding:10px 15px 10px 15px;
		}
		div.complainContent{
			margin-bottom:10px;
			padding:10px 15px 10px 15px;
		}
		div.complainContent>span:nth-child(1){
			font-size:16px;
		}
   	</style>
  </head>
  
<body>
	<%-- currentFaculty: ${currentFaculty}
	currentCourse: ${currentCourse} --%>

	<div id="msgAlert" class="alert alert-danger" style="display: none">
		<button type="button" class="close" data-dismiss="alert">x</button>
		<span id="headMsg"> </span>
	</div>

	<div class="box">
		<div id="formActRe" class="boxAct" style="display:none">
			<%-- <form:form id="reComndForm" modelAttribute="reComndForm"
			action="${formAction}" method="POST"
			enctype="multipart/form-data">
				<fieldset>
					<legend style="font:16px bold;">
						<span></span> <span class="headDetail"></span>
					</legend>
					<div style="text-align: center;">
						<form:input type="hidden" id="keySearch" path="keySearch" />
						<form:input type="hidden" id="fReComndId" path="kpiReComndModel.reComndId" />
						<form:input type="hidden" id="fOrgId" path="kpiReComndModel.orgId" />
						<form:input type="hidden" id="fGroupId" path="kpiReComndModel.groupId" />
						<form:input type="hidden" id="fReComndFlag" path="kpiReComndModel.reComndFlag" />
						<form:input type="hidden" id="fCreateBy" path="kpiReComndModel.createdBy" />
						<form:input type="hidden" id="fCreateDate" path="createDate" />
						<table style="margin:auto">
							<tr>
								<td style="text-align:right"><span class="headDetail"></span> : </td>
								<td><form:input id="fReComndDesc" path="kpiReComndModel.reComndDesc" maxlength="20"/></td>
							</tr>
							<tr>
								<td colspan=2>
									<lable id="ckInputText" style="color:red; display:none;">*กรุณากรอกข้อมูลให้ครบถ้วน</lable> <br/>				
									<input type="button" class="save" value="บันทึก" onClick="actSaveInsert()" /> 
									<input type="button" class="cancel" value="ยกเลิก" onClick="actCancel()" />
								</td>
							</tr>
						</table>
					</div>
				</fieldset>
			</form:form> --%>
			<form>
				<fieldset>
					<legend style="font:16px bold;">
						<span></span> <span class="headDetail"></span>
					</legend>
					<div style="text-align: center;">
						<input type="hidden" id="keySearch" />
						<input type="hidden" id="fReComndId" />
						<input type="hidden" id="fOrgId" />
						<input type="hidden" id="fGroupId" />
						<input type="hidden" id="fReComndFlag" />
						<input type="hidden" id="fCreateBy" />
						<input type="hidden" id="fCreateDate" />
						<table style="margin:auto">
							<tr>
								<td style="text-align:right"><span class="headDetail"></span> : </td>
								<td><textarea id="fReComndDesc" rows="3" maxlength="255" style="resize:none;width:800px"></textarea></td>
							</tr>
							<tr>
								<td colspan=2>
									<label id="ckInputText" style="color:red; display:none;">*กรุณากรอกข้อมูลให้ครบถ้วน</label> <br/>				
									<input type="button" class="save" value="บันทึก" onClick="actSaveInsert()" /> 
									<input type="button" class="cancel" value="ยกเลิก" onClick="actCancel()" />
								</td>
							</tr>
						</table>
					</div>
				</fieldset>
			</form>
		</div>
		
		<div class="row-fluid">
			<fieldset id="paramSet">
				<div style="text-align: center;">
				<form:form id="comndForm" modelAttribute="comndForm" action="" method="POST" enctype="multipart/form-data">
					<form:input type="hidden" id="paramOrg" path="model.orgId" />
					<table style="margin:auto">
						<tr>
							<td style="text-align:right"><label>ปีการศึกษา :</label></td>
							<td>
								<form:select id="paramYear" class="" path="model.academicYear" items="${acadYears}"/>
								&nbsp&nbsp&nbsp
							</td>
							<td style="text-align:right"><label>กลุ่มตัวบ่งชี้ :</label></td>
							<td>
								<form:select id="paramGroup" class="" path="model.groupId" items="${groups}"/>
								&nbsp&nbsp&nbsp
							</td>
							<td style="text-align:right"><label>ระดับตัวบ่งชี้ :</label></td>
							<td>
								<form:select id="paramLevel" class="" path="hieAuth.level" items="${levels}"
									onchange="ParamLevelChange(this)"/>
								&nbsp&nbsp&nbsp
							</td>
							<td rowspan="2"> 
								<input type="button" id="searchData" class="save" value="เรียกดูข้อมูล" onClick="actSubmit()" /> 
							</td>
						</tr>
						<tr>
							<td style="text-align:right"><label>สถาบัน :</label></td>
							<td>
								<form:select id="paramUniversity" class="" path="hieAuth.university" 
									items="${unis}" onchange="ParamChange(this,'university')" />
								&nbsp&nbsp&nbsp
							</td>
							<td style="text-align:right"><label>คณะ :</label></td>
							<td>
								<form:select id="paramFaculty" class="" path="hieAuth.faculty" 
									onchange="ParamChange(this,'faculty')"/>
								&nbsp&nbsp&nbsp
							</td>
							<td><label>หลักสูตร :</label></td>
							<td>
								 <form:select id="paramCourse" class="" path="hieAuth.course"
								 	onchange="ParamChange(this,'course')"/>
								&nbsp&nbsp&nbsp&nbsp&nbsp
							</td>
						</tr>
					</table>		
				</form:form>			
				</div>
				<hr color="gray">
			</fieldset>
		</div>

		<div id="complainFS" class="complainContent">
			<div style="border:1px solid #999; border-bottom-width:0px; padding:3px 0px 3px 10px;">
				<img style="display: inline-block" height="20" width="20" onClick="actAdd(this, 'S')" 
				src="<c:url value="/resources/images/add.png"/>"/>
    			&nbsp&nbsp<h4 style="display: inline-block"> จุดเด่น </h4> 	
			</div>
			<div class="wrapContainer"></div>
			<table id='RecomdFlagS' class='tableGridTp'>
				<tr>
					<td align="center"> <h5> เลือกฟิลเตอร์ และกด "เรียกดูข้อมูล" เพื่อเรียกดูข้อมูลที่ต้องการ </h5> <td>
				</tr>
			</table>
		</div>

		<div id="complainFW" class="complainContent" >
			<div style="border:1px solid #999; border-bottom-width:0px; padding:3px 0px 3px 10px;">
				<img style="display: inline-block" height="20" width="20" onClick="actAdd(this, 'W')" 
				src="<c:url value="/resources/images/add.png"/>"/>
				&nbsp&nbsp<h4 style="display: inline-block"> จุดที่ต้องพัฒนา </h4>		
			</div>
			<div class="wrapContainer"></div>
			<table id='RecomdFlagW' class='tableGridTp'>
				<tr>
					<td align="center"> <h5> เลือกฟิลเตอร์ และกด "เรียกดูข้อมูล" เพื่อเรียกดูข้อมูลที่ต้องการ </h5> <td>
				</tr>
			</table>
		</div>

	</div>
</body>
</html>