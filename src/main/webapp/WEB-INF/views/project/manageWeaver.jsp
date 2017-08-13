<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>${project.name}~ ${project.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<script src="/resources/forweaver/js/weaverBrowser.js"></script>
</head>
<body>
<script>
var weaverList = new Array();
<c:forEach items="${project.adminWeavers}" var="adminWeaver">
weaverList.push({
	"admin": true,
	"id": "${adminWeaver.id}",
	<c:if test="${project.isEducation()}">
	"massage": "${adminWeaver.studentID}",
	</c:if>
	<c:if test="${!project.isEducation()}">
	"massage": "${cov:htmlEscape(adminWeaver.say)}",
	</c:if>
	"img": "${adminWeaver.getImgSrc()}"
});
</c:forEach>
<c:forEach items="${project.joinWeavers}" var="joinWeaver">
weaverList.push({
	"admin": false,
	"id": " ${joinWeaver.id}",
	<c:if test="${project.isEducation()}">
	"massage": "${joinWeaver.studentID}",
	</c:if>
	<c:if test="${!project.isEducation()}">
	"massage": "${cov:htmlEscape(joinWeaver.say)}",
	</c:if>
	"img": "${joinWeaver.getImgSrc()}",
	"removeLink": "/project/${project.name}/weaver/${joinWeaver.id}/delete"
});
</c:forEach>
$(document).ready(function() {
	
	 var weavers = [
		<c:forEach items='${weavers}' var='weaver'>	
			"${weaver.getId()}",
		</c:forEach>
		];
	 
	$( "#weaverName" ).autocomplete({
		source: weavers
	});
	
	$('#weaverAdd').click(function(){
		if(!confirm('정말로 '+$('#weaverName').val()+'님을 초대하시겠습니까?'))
			return;
		var weaverName = $('#weaverName').val();
		
		if(weaverName.length != 0)
			window.location = "/project/${project.name}/weaver/"+weaverName+"/add-weaver";
	});
	
	move = false;
			<c:forEach items='${project.tags}' var='tag'>
			$('#tags-input').tagsinput('add',"${tag}");
			</c:forEach>
			move = true;

	makeNavigationInManageWeaver(weaverList.length,10);
	showWeaverList(1);
	$("li.page").click(function(){
		var allSize = $("li.page").size();
		var selectIndex = Number($(this).text());
		
		  $('li.page').removeClass('active'); 
		  $(this).addClass('active');
		  $("a.page-link").hide();
		  if(allSize <= 5){
			  for(var i = 0 ; i<5; i++){
			  	$("a.page-link:eq("+i+")").show();
			  }
		  }
		  else if(allSize - selectIndex < 4){
			  for(var i = 0 ; i<5; i++){
				  var j = allSize -i -1;
			  	$("a.page-link:eq("+j+")").show();
			  }
		 }
		  else{
			  for(var i = 0 ; i<5; i++){
				  var j = selectIndex + i -2;
				  if(j == -1)
					  j=4;
			  	$("a.page-link:eq("+j+")").show();
			  }
		  }
	});

	$("a.fui-arrow-left").click(function(){
		$('#pageNavigation > li,li.page').removeClass('active'); 
		$('li.page:first').addClass('active');
		$("a.page-link").hide();
		  for(var i = 0 ; i<5; i++)
		  	$("a.page-link:eq("+i+")").show();
		showWeaverList($("li.page:first").text());

	});

	$("a.fui-arrow-right").click(function(){
		var allSize = $("li.page").size();	
		$('#pageNavigation > li,li.page').removeClass('active'); 
		$('li.page:last').addClass('active');
		$("a.page-link").hide();
		for(var i = allSize-5; i < allSize ; i++)
			  	$("a.page-link:eq("+i+")").show();	
		showWeaverList($("li.page:last").text());
	
	});
	
});
</script>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>

		<div class="page-header page-header-none">
			<h5>
						<big><big>	<c:if test="${!project.isForkProject()}">
							<i class="fa fa-bookmark"></i></c:if>
							<c:if test="${project.isForkProject()}">
							<i class="fa fa-code-fork"></i></c:if> 
							${project.name}</big></big>
				<small>${project.description}</small>
			</h5>
		</div>
		<div class="row">
			<div class="span8">
				<ul class="nav nav-tabs">
					<li><a href="/project/${project.name}/">브라우져</a></li>
					<li><a href="/project/${project.name}/commitlog">커밋</a></li>
					<li><a href="/project/${project.name}/community">커뮤니티</a></li>
					
					<li  class="active"><a href="/project/${project.name}/weaver">사용자</a></li>
					<sec:authorize ifAnyGranted="ROLE_USER, ROLE_ADMIN">
					<c:if test="${project.getCreator().equals(currentUser) }">
					<li><a href="/project/${project.name}/edit">관리</a></li>
					</c:if>
					</sec:authorize>
					<li><a href="/project/${project.name}/info">정보</a></li>
					
					
				</ul>
			</div>
			<div class="span4">
				<div class="input-block-level input-prepend" title="http 주소로 저장소를 복제할 수 있습니다!&#13;복사하려면 ctrl+c 키를 누르세요.">
					<span class="add-on"><i class="fa fa-git"></i></span> <input
						value="http://${pageContext.request.serverName}/g/${project.name}.git" type="text"
						class="input-block-level">
				</div>
			</div>
			
			<div class="span12">
							
				<div class="span7">
					<h4 style="margin: 10px 0px 0px 0px"><i class="fa fa-user"></i>  사용자 목록</h4>
				</div>
				<div style="margin-left:25px;" class="span4">
					<input id="weaverName" style="width:90%;" placeholder="아이디나 이메일을 입력해주세요!" type="text">
				</div>
				<div style="margin-left:-5px;" class="span1">
					<button id="weaverAdd" class = "btn btn-primary"
					 title="Weaver 추가"><i class="fa fa-plus"></i></button>
				</div>
				
				<table id="weaverTable" class="table table-hover">
				</table>
				<div id="pageNavigation" class="text-center pagination">

				</div>
			</div>
		</div>
		<!-- .span9 -->

		<!-- .row-fluid -->
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>
</html>
