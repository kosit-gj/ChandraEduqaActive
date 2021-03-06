<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ page contentType="text/html; charset=utf-8" %> 
<%@ page import="javax.portlet.PortletURL" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="java.sql.*" %>

<portlet:actionURL var="formActionInsert">
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
</portlet:actionURL> 
<portlet:resourceURL var="getPlan" id="getPlan" ></portlet:resourceURL>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <!-- Bootstrap core CSS --> 
    <link rel="stylesheet" href="<c:url value="/resources/bootstrap/css/bootstrap.min.css"/>" type="text/css"/>
    <link rel="stylesheet" href="<c:url value="/resources/bootstrap/css/bootstrap-responsive.min.css"/>" type="text/css"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/common-element.css"/>" type="text/css"/>
    <script src="<c:url value="/resources/js/jquery-1.11.2.min.js"/>"></script> 
    <script  src="<c:url value="/resources/js/jquery-ui.min.js"/>"></script>
    <link rel="stylesheet" href="<c:url value="/resources/css/jquery-ui.min.css"/>"/>
    <script src="<c:url value="/resources/bootstrap/js/bootstrap.min.js"/>"></script>
	<script  src="<c:url value="/resources/bootstrap/js/bootstrap-typeahead.min.js"/>"></script>
	<script src="<c:url value="/resources/js/confirm-master/jquery.confirm.min.js"/>"></script>
	
	<style>
 	 select.listStatus{
 		width:100px;
  		height:30px;
 		vertical-align: middle;
  		margin-bottom: 0px;
 		 }
  	</style>
	
	<!-- Color Picker -->
	<script src="<c:url value="/resources/colorpicker-master/js/evol.colorpicker.min.js"/>" type="text/javascript" charset="utf-8"></script>
	<link href="<c:url value="/resources/colorpicker-master/css/evol.colorpicker.css"/>" rel="stylesheet" type="text/css">
	
    <script type="text/javascript"> 
   	  	var dialog,dialog2,gobalBenginTld,gobalEndTld,gobalTldName,gobalTldColor,gobalActive;
    	$( document ).ready(function() {
    		pageMessage();
    		$('.inputNum').keypress(function (event) {                
                return isNumber(event, this)
            });
    		
    		//--Color Picker--//
    		$("input#fTldColor").colorpicker({
    			hideButton: true
    		});
    		$("input#fTldColor").change(function(){
    			$(this).css("background-color",$("#fTldColor").val());
    		});
    	});
    	function pageMessage(){
    		if($("#messageMsg").val()){
    			if($("#messageMsg").val() == 100){ //ok
    				$("#msgAlert").removeClass().addClass("alert");
    				$("span#headMsg").html("");
    				$("#msgAlert").fadeTo(10000, 500).slideUp(1000, function(){
                	    $("#msgAlert").alert('close');
                	});
    			}else{
    				$("#msgAlert").removeClass().addClass("alert alert-danger");
    				$("span#headMsg").append("<strong> ผิดพลาด! </strong>");    				
    				$("#msgAlert").fadeTo(10000, 500).slideUp(1000, function(){
                	    $("#msgAlert").alert('close');
                	});
    			}
            }
    	}
    	/* bind element event*/
    	function actSearch(el){
    		$('#thresholdForm').attr("action","<%=formActionSearch%>");
    		$('#keySearch').val($('#textSearch').val());
    		$('#keyListStatus').val($('#listStatus').val());
    		$('#thresholdForm').submit();
    	}
    	function actChangePageSize(){
    		var numPage = $('#numPage').val();
    		var sizePage = $('#pageSize').val();
    		$('#thresholdForm '+'#pageNo').val(numPage);
    		$('#thresholdForm '+'#PageSize').val(sizePage);
    		$('#thresholdForm').attr("action","<%=formActionListPage%>");
    		$('#thresholdForm').submit();
    	}
   	 	function actSelectPage(el){
   	 		var numPage = el.innerHTML;
   	 		var sizePage = $('#pageSize').val();
	   	 	$('#thresholdForm '+'#pageNo').val(numPage);	   	 	
	   	 	$('#thresholdForm '+'#PageSize').val(sizePage);
	   	 	$('#keySearch').val($('#textSearch').val());
			$('#thresholdForm').attr("action","<%=formActionListPage%>");
			$('#thresholdForm').submit();
   	 	}
   	 	function actAdd(el){
   	 		renderDialog('#formActTld',1,'','');   	 		
   	 	}
   	 	function actEdit(el){
   	 	var dataId = parseInt($(el).parent('td').parent('tr').children('tbody tr td:nth-child(9)').html());
   		 var dataDesc = [];
   	 		dataDesc["begin"] = $(el).parent('td').parent('tr').children('tbody tr td:nth-child(2)').html();
   	 		dataDesc["end"] = $(el).parent('td').parent('tr').children('tbody tr td:nth-child(3)').html();
   	 		dataDesc["name"] = $(el).parent('td').parent('tr').children('tbody tr td:nth-child(4)').html();
   	 		dataDesc["color"] = $(el).parent('td').parent('tr').children('tbody tr td:nth-child(5)').children('div').html();
   	 		dataDesc["createBy"] = $(el).parent('td').parent('tr').children('tbody tr td:nth-child(13)').html();
   	 		dataDesc["createDate"] = $(el).parent('td').parent('tr').children('tbody tr td:nth-child(12)').html();
   	 		dataDesc["active"] = $(el).parent('td').parent('tr').children('tbody tr td:nth-child(14)').html();
   	 		renderDialog('#formActTld',2,dataId,dataDesc);
   	 	}
   	 	function actDelete(el){   	
   	 	var dataId = parseInt($(el).parent('td').parent('tr').children('tbody tr td:nth-child(9)').html());
	    	var dataName = $(el).parent('td').parent('tr').children('tbody tr td:nth-child(4)').text();
	   	 	$.confirm({
		   	     text: "ยืนยันการลบระดับตัวบ่งชี้ \"".concat(dataName, "\""),
		   	     title: "ลบระดับตัวบ่งชี้",
		   	     confirm: function(button) {
		   	    	$("#keySearch").val($("#textSearch").val());
			 		$("#keyListStatus").val($("#listStatus").val());
		   	 		$('#thresholdForm').attr("action","<%=formActionDelete%>");
			 		$('#thresholdForm '+'#fTldId').val(dataId);
			 		$('#thresholdForm').submit();
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
   	 	function actSaveInsert(){
   	 		var begin = $("#fBeginTld").val();
   	 		var end = $("#fEndTld").val();
   	 		var desc = $("#fTldName").val();
   	 		var color = $("#fTldColor").val();
   	 		if(begin==""||end==""||desc==""||color==""){
   	 			$("label#chInput").html(
   	 				"<label id=\"ckInputText\" style=\"color:red;\">*กรุณากรอกข้อมูลให้ครบถ้วน</label>"
   	 			).css( "display", "block" ).fadeOut( 15000 );
   	 		}else{
   	 			if(parseFloat(begin) > parseFloat(end)){
	   	 			$("label#chInput").html(
	   	   	 				"<label id=\"ckInputTextPoint\" style=\"color:red;\">*คะแนนเริ่มต้นต้องน้อยกว่าคะแนนสูงสุด</label>"
	   	   	 		).css( "display", "block" ).fadeOut( 15000 );
   	 			}else{
   	 				$("#fTldLevel").val(parseInt($("#textSearch").val()));
	   	 			$("#keySearch").val($("#textSearch").val());
			 		$("#keyListStatus").val($("#listStatus").val());
	   	 			$('#thresholdForm').attr('action',"<%=formActionInsert%>");  
	   	 			$('#keySearch').val($("#textSearch").val()); // ??
		   	 		$('#thresholdForm').submit();
		   	 		$('.inputForm').val("");
   	 			}
   	 		}
   	 	}
   	 	function actSaveEdit(){
		 	if($.trim(gobalBenginTld) == $.trim($("input#fBeginTld").val()) //ส่วนที่เพิ่ม
		 		&& $.trim(gobalEndTld) == $.trim($("input#fEndTld").val())
		 		&& $.trim(gobalTldName) == $.trim($("input#fTldName").val())
		 		&& $.trim(gobalTldColor) == $.trim($("input#fTldColor").val())
		 		&& $.trim(gobalActive) == $.trim($("input.active:checked").val())
		 		){
		 		actCancel();
		 	
		 	}else{
		 		$("#keySearch").val($("#textSearch").val());
		 		$("#keyListStatus").val($("#listStatus").val());
	   	 		$('#thresholdForm').attr("action","<%=formActionEdit%>");
		 		$('#thresholdForm').submit();
	 		}
   	 	}
	 	
   	 	
   	 	function actCancel(el){
	  		//dialog.dialog( "close" );
	  		$('.inputForm').val("");
	  		$('#formActTld').slideToggle('slow');
	  	}
   	 	function renderDialog(d1,mode,dataId,dataDesc){
   	 		/*mode 1:insert 2:edit*/
   	 		$("#fTldLevel").val(parseInt($("#textSearch").val()));   	 		
   	 		var head,event;
   	 		if(mode==1){
	 			head = 'เพิ่ม';
	 			event= 'actSaveInsert()';
	 			$(d1).trigger( "reset" );
	 			$(d1).find('input.inputForm').val("");
	 			
	 		}else if(mode==2){
	 			head = 'แก้ไข';
	 			event='actSaveEdit()';
	 			$("input#fTldColor").css("background-color",$.trim(dataDesc["color"]));
	 			$(d1).find('input[type=text]#fBeginTld').val(dataDesc["begin"]);
	 			$(d1).find('input[type=text]#fEndTld').val(dataDesc["end"]);
	 			$(d1).find('input[type=text]#fTldName').val(dataDesc["name"]);
	 			$(d1).find('input[type=text]#fTldColor').val($.trim(dataDesc["color"]));
	 		
	 			///ส่วนที่เพิ่ม////
	 			gobalBenginTld = dataDesc["begin"];
	 			gobalEndTld = dataDesc["end"];
	 			gobalTldName = dataDesc["name"];
	 			gobalTldColor = dataDesc["color"];
	 			gobalActive = dataDesc["active"];
	 			
	 			
	 			//ส่วนที่เพิ่ม////
	 			if(dataDesc["active"]==0){
					 $(d1).find('input[type=radio]#fNotActive').prop( "checked", true );
					 $(d1).find('input[type=radio]#fActive').prop( "checked", false );
				}else if(dataDesc["active"]==1){
					 $(d1).find('input[type=radio]#fNotActive').prop( "checked", false );
					 $(d1).find('input[type=radio]#fActive').prop( "checked", true );
				}else{
					 $(d1).find('input[type=radio]#fNotActive').prop( "checked", false );
					 $(d1).find('input[type=radio]#fActive').prop( "checked", true );
				 }
	 			
	 			////////////
	 				 			
	 		}
   	 		$(d1).find('span').html(head);
   	 		$(d1).find('input[type=hidden]#fTldId').val(dataId);
   	 		$(d1).find('input[type=hidden]#fTldCreateBy').val(dataDesc["createBy"]);
   			$(d1).find('input[type=hidden]#fTldCreateDate').val(dataDesc["createDate"]);
   			
   			$(d1).find('input[type=button].save').attr('onClick',event);

		   	if ( $(d1).is(':visible')) {

		   		return false ;
		   	}else{
		   		$(d1).slideToggle("slow");
		   	} 
   	 	}        
        function isNumber(evt, element) {

            var charCode = (evt.which) ? evt.which : event.keyCode

            if (
            	(charCode != 8 || $(element).val().indexOf('.') != -1) &&
                (charCode != 45 || $(element).val().indexOf('-') != -1) &&      // “-” CHECK MINUS, AND ONLY ONE.
                (charCode != 46 || $(element).val().indexOf('.') != -1) &&      // “.” CHECK DOT, AND ONLY ONE.
                (charCode < 48 || charCode > 57) &&
                charCode != 8 && charCode != 46
            )
                return false;

            return true;
        }
   	</script>  
  
   	<style type="text/css">
   		div.boxAct{
			padding: 20px 20px 20px 20px;
			border: thin solid #CDCDCD;
			border-radius: 10px;
			display: block; 
		}
		
   		table.formActTld{
   			background-color:#FFFFFF;
    		border:1px solid #999999;
    		overflow:hidden;
    		width:100%;
   			padding-top:10px;
   			font-size:14px;
   		}   		
   		table.formActTld th:nth-child(1){ width:5%; }
   		table.formActTld th:nth-child(2){ width:10%; } table.formActTld td:nth-child(2){ text-align: center;}
   		table.formActTld th:nth-child(3){ width:10%; } table.formActTld td:nth-child(3){ text-align: center;}
   		table.formActTld th:nth-child(4){ width:30%; }
   		table.formActTld th:nth-child(5){ width:15%; } table.formActTld td:nth-child(5){ text-align: center;}
   		table.formActTld th:nth-child(6){ width:10%; }
   		table.formActTld th:nth-child(7){ width:10%; }
   		table.formActTld th:nth-child(8){ width:10%; }
   		table.formActTld th:nth-child(9), table.formActTld td:nth-child(9){ width:0%; display:none;}
   		table.formActTld th:nth-child(10), table.formActTld td:nth-child(10){ width:0%; display:none;}
   		table.formActTld th:nth-child(11), table.formActTld td:nth-child(11){ width:0%; display:none;}
   		table.formActTld th:nth-child(12), table.formActTld td:nth-child(12){ width:0%; display:none;}
     	table.formActTld th:nth-child(13), table.formActTld td:nth-child(13){ width:0%; display:none;}
     	table.formActTld th:nth-child(14), table.formActTld td:nth-child(14){ width:0%; display:none;}
   		table.formActTld tbody td:nth-child(1){
   			text-align:center;$("#success-alert").alert('close');
   			border-color:#acacac;
   			border-width:0px 0px 0px 0px;
   			border-style:inset;
   			background: linear-gradient(white, #efefef,#e2e2e2,#f7f7f7);
   		}
   		table.formActTld thead th{
   			text-align:center;
   			border-color:#acacac;
   			border-width:0px 1px 1px 1px;
   			border-style:inset;
   			background: linear-gradient(white, #efefef,#e2e2e2,#f7f7f7);
   		}
   		table.formActTld tbody td {
			border: none;
			padding: 1px 2px 2px 2px;
		}
   		table.formActTld tbody tr:nth-child(2n) td{ background-color:rgba(244,244,244,1); }				
		#numPage{width:40px; margin-bottom: 0px;}
		
		select#pageSize{
			width:50px;
			height:25px;
			vertical-align: middle;
			margin-bottom: 0px;
		}
		select#pageSize option { font-size: 18px; }
		button.btnPag {
		    background-color: Transparent;
		    background-repeat:no-repeat;
		    border: none;
		    cursor:pointer;
		    overflow: hidden;
		    outline:none;
		}
   	</style>
  </head>
  
<body> 
	<input type="hidden" id="messageMsg" value="${messageCode}"> 
	<div id="msgAlert" style="display:none">
	    <button type="button" class="close" data-dismiss="alert">x</button>
	    <span id="headMsg"> </span> ${messageDesc} 
	</div>
	
	
	<div class="box">
		<div id="formActTld" class="boxAct" style="display:none">
			<form:form id="thresholdForm" modelAttribute="thresholdForm" action="${formAction}" method="POST" enctype="multipart/form-data">
				<fieldset>
					<legend style="font:16px bold;">
						<span></span>เกณฑ์แสดงผลการประเมิน
					</legend>
					<div style="text-align: center;">
						<form:input type="hidden" id="pageNo" path="pageNo" value="${PageCur}"/>
						<form:input type="hidden" id="PageSize" path="pageSize" />
						<form:input type="hidden" id="keySearch" path="keySearch"/>
						<form:input type="hidden" id="fTldId" path="thresholdModel.thresholdId" />
						<form:input type="hidden" id="fTldLevel" path="thresholdModel.levelId"/>
						<form:input type="hidden" id="fTldCreateBy" path="thresholdModel.createdBy" />
						<form:input type="hidden" id="fTldCreateDate" path="createDate" />
						<form:input type="hidden" id="keyListStatus" path="keyListStatus" />
						
						<table style="margin:auto" > 
							<tr>
								<td style="text-align:right"> คะแนนเริ่มต้น: </td>
								<td style="text-align:left">
									<form:input type="text" id="fBeginTld" class="inputForm inputNum" path="thresholdModel.beginThreshold" 
									style="width:50px;text-align:right;" maxlength="5"/>
								</td>
								<td style="text-align:right"> คะแนนสิ้นสุด: </td>
								<td style="text-align:left">
									<form:input type="text" id="fEndTld" class="inputForm inputNum" path="thresholdModel.endThreshold" 
									style="width:50px;text-align:right;" maxlength="5"/>
								</td>
							</tr>
							<tr>
								<td style="text-align:right">คำอธิบาย: </td>
								<td colspan="3" style="text-align:left">
									<form:input id="fTldName" class="inputForm" path="thresholdModel.thresholdName" 
									maxlength="255"/>
								</td>
							</tr>
							<tr>
								<td style="text-align:right"> สี: </td>
								<td colspan="3" style="text-align:left"> 
									<form:input id="fTldColor" class="inputForm" path="thresholdModel.colorCode" 
									style="width:60px" readonly="true"/> 
								</td>						
							</tr>
							
							<tr >
			               		 <td style='text-align: right'></td>
			               		 <td style='text-align: left'>
			                			<form:radiobutton id="fActive" checked="checked" class="widt active" path="thresholdModel.active" value="1" name="active" />
			                			<div style ="margin-right:17px; display: inline;">  เปิดใช้งาน
			               				 </div>
			               				 <form:radiobutton id="fNotActive" class="widt active" path="thresholdModel.active" value="0" name="active" /> ปิดใช้งาน
			             		 </td>
			              </tr>
								
										
						</table>
	 					
						<label id="chInput"></label> <br/>								
						<input type="button" class="save" value="บันทึก" onClick="actSaveInsert()" /> 
						<input type="button" class="cancel" value="ยกเลิก" onClick="actCancel()" />
					</div>
				</fieldset>
			</form:form>
		</div><br/>
		
		<div class="row-fluid"><%--  firstLevel:${firstLevel} || keySearch:${keySearch } --%>
			<div class="span6">
				<span>เกณฑ์แสดงผลประเมินระดับ : </span>
				<select id="textSearch" > <!-- onchange="actSearch(this)" -->
					<c:forEach items="${levels}" var="level" varStatus="loop">	
						<c:choose>					
							<c:when test="${empty keySearch}"> <!-- กรณีเปิดครั้งแกรจะ Default ค่าตามที่ Controller สงมา -->														
								<c:choose>
									<c:when test="${level.levelId == firstLevel}">
										<option value="${level.levelId}" selected> ${level.desc} </option>
									</c:when>    
									<c:otherwise>
										<option value="${level.levelId}"> ${level.desc} </option>
									</c:otherwise>
								</c:choose>							
							</c:when>
							<c:otherwise>
								<c:choose>
									<c:when test="${level.levelId == keySearch}">
										<option value="${level.levelId}" selected> ${level.desc} </option>
									</c:when>    
									<c:otherwise>
										<option value="${level.levelId}"> ${level.desc} </option>
									</c:otherwise>
								</c:choose>
							</c:otherwise>
						</c:choose>						
					</c:forEach>
					
				</select>
				</div>
				
				<div>
				<span>ค้นหาเกณฑ์แสดงผลประเมินระดับ : </span>
				<select name='listStatus' id='listStatus' class="listStatus">
				<c:choose>
							 <c:when test="${keyListStatus=='0'}">
	 							 <option value='99'>ทั้งหมด</option>
								 <option value='1'>เปิดใช้งาน</option>
								 <option selected='selected' value='0'>ปิดใช้งาน</option>
				 		</c:when>
	 						<c:when test="${keyListStatus=='1'}">
	 							<option value='99'>ทั้งหมด </option>
								<option selected='selected' value='1'>เปิดใช้งาน</option>
						 		<option value='0'>ปิดใช้งาน</option>
						 </c:when>
	 					 <c:otherwise>
	 							<option selected='selected' value='99'>ทั้งหมด </option>
								<option value='1'>เปิดใช้งาน</option>
	 							<option value='0'>ปิดใช้งาน</option>
					 	</c:otherwise>
					</c:choose>
				</select>
       			 
       			 <img height="20" width="20" src="<c:url value="/resources/images/search.png"/>" onClick="actSearch(this)">
				<img style="cursor: pointer;" height="18" width="18" onClick="actAdd(this)" src="<c:url value="/resources/images/add.png"/>">
			</div>
		
		<div class="boxTable">
			<table class="formActTld">
				<thead>
					<tr>
						<th>ช่วง</th>						
						<th>คะแนนเริ่ม</th>
						<th>คะแนนสิ้นสุด</th>						
						<th>คำอธิบาย</th>
						<th>สี</th>
						<th>สถานะ</th>
						<th>แก้ไข</th>
						<th>ลบ</th>
						<th>รหัส(ซ้อน)</th>
						<th>ปี (ซ้อน)</th>
						<th>levelId (ซ้อน)</th>
						<th>createDate (ซ้อน)</th>
						<th>createBy (ซ้อน)</th>
					</tr>
				</thead>
				<tbody> 
					<c:if test="${not empty thresholds}">
						<c:forEach items="${thresholds}" var="tld" varStatus="loop">
							<tr>
								<td>${(loop.count+((PageCur-1)*pageSize))}</td>
								<td>${tld.beginThreshold} </td>
								<td>${tld.endThreshold}</td>
								<td>${tld.thresholdName}</td>
								<td> 
									<div style="background-color:${tld.colorCode};width:70%;margin:auto;" >
										${tld.colorCode} 
									</div>
								</td>
								
								<td align="center">
								<c:if test="${tld.active=='0'}">
									<img data-toggle="tooltip" data-placement="top" title="Tooltip on top" src="<c:url value="/resources/images/button-turn-off.jpg"/>" width="22" height="22" style="cursor: pointer;">
								</c:if>
								<c:if test="${tld.active=='1'}">
									<img data-toggle="tooltip" data-placement="top" title="Tooltip on top" src="<c:url value="/resources/images/button-turn-on.jpg"/>" width="22" height="22" style="cursor: pointer;">
								</c:if>
              				    </td>
								
															
								<td align="center">
									<img style="cursor: pointer;" height="18" width="18" onClick="actEdit(this)" src="<c:url value="/resources/images/edited.png"/>">
								</td>
								<td align="center">
									<img style="cursor: pointer;" height="18" width="18" onClick="actDelete(this)" src="<c:url value="/resources/images/delete.png"/>">
								</td>
								<td>${tld.thresholdId}</td>
								<td>${tld.academicYear}</td>
								<td>${tld.levelId} }</td>
								<td> <fmt:formatDate value="${tld.createdDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
								<td> ${tld.createdBy} </td>
								<td> ${tld.active} </td>
							</tr> 
						</c:forEach>
					</c:if>
				</tbody>
			</table>
		</div>
	</div>
</div>
</body>
</html>	
   