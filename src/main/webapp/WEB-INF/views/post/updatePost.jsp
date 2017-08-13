<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>Forweaver : 소통해보세요!</title>

<%@ include file="/WEB-INF/includes/src.jsp"%>
<link rel="stylesheet" type="text/css" href="/resources/forweaver/css/bootstrap-markdown.min.css"/>
<script src="/resources/forweaver/js/markdown/markdown.js"></script>
<script src="/resources/forweaver/js/markdown/bootstrap-markdown.js"></script>
<script src="/resources/forweaver/js/markdown/to-markdown.js"></script>
</head>
<body>
	<script type="text/javascript">
	var fileCount = 1;
	var fileArray = [];
	var fileHash = {};
	
	editorMode = true;

	function checkPost(){
		<c:if test="${post.isLong()}">
		for(var i=0;i<fileCount;i++){
			var fileName = $("#file"+(i+1)).val();
			
			if(fileName.indexOf("C:\\fakepath\\") != -1)
				fileName = fileName.substring(12);

			fileName = replaceAll(fileName,"?","_");
			fileName = replaceAll(fileName,"#","_");
			fileName = replaceAll(fileName," ","_");
			fileArray[i] = fileName;
		}
		</c:if>
		
		var tags = $("#tags-input").val();
		if(tags.length == 0){
			return false;
		}else if($('#post-title-input').val().length < 5){
			alert("제목을 최소 5글자 이상 입력해주세요!");
			return false;
		}
		<c:if test="${post.isLong()}">
		else if($('#post-content-textarea').val().length < 5){
			alert("내용을 최소 5글자 이상 입력해주세요!");
			return false;
		}</c:if>
		else{
			$("form:first").append($("input[name='tags']"));
			return true;
		}
	}
	
		
	function removeFile(id,name){
		if(confirm("정말로 "+name+"을 삭제하시겠습니까?")){
			$("#file-"+id).remove();
			$("#remove").val($("#remove").val()+"@"+id);
		}
	}
		$(document).ready(function() {

			$("#post-content-textarea").css('height','380px');
			<c:forEach items="${post.datas}" var="data">
			$(".file-div").append("<div id='file-${data.id}' class='fileinput fileinput-exists'><div class='input-group'>"+
					"<div class='form-control'><i class='icon-file '></i> <span class='fileinput-filename'>"+
					"${data.name}"+
					"</span></div><a href='#' onclick='javascript:removeFile(\"${data.id}\",\"${data.name}\")' class='input-group-addon btn btn-primary fileinput-exists'>"+
					"<i class='icon-remove icon-white'></i></a></div></div>");
			</c:forEach>
			
			$(".file-div").append("<div class='fileinput fileinput-new' data-provides='fileinput'>"+
					  "<div class='input-group'>"+
					    "<div class='form-control' data-trigger='fileinput' title='업로드할 파일을 선택하세요!'><i class='icon-file '></i> <span class='fileinput-filename'></span></div>"+
					    "<span class='input-group-addon btn btn-primary btn-file'><span class='fileinput-new'>"+
					    "<i class='fa fa-arrow-circle-o-up icon-white'></i></span><span class='fileinput-exists'><i class='icon-repeat icon-white'></i></span>"+
						"<input onchange ='fileUploadChange(this,\"#post-content-textarea\");' type='file' id='file1' multiple='true' name='files[0]'></span>"+
					   "<a href='#' class='input-group-addon btn btn-primary fileinput-exists' data-dismiss='fileinput'><i class='icon-remove icon-white'></i></a>"+
					  "</div>"+
					"</div>");
			
			move = false;
			<c:forEach items='${post.tags}' var='tag'>
			$('#tags-input').tagsinput('add',"${tag}");
			</c:forEach>
			move = true;
	});
	</script>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>
		<div class="page-header page-header-none">
		<alert></alert>
			<h5>
				<big><big><i class=" fa fa-comments"></i> 소통해보세요!</big></big> 
				<small>프로젝트 진행사항이나 궁금한 점들을 올려보세요!</small>
			</h5>
		</div>
		<div class="row">

			<form id="postForm" onsubmit="return checkPost()" 
					action="/community/${post.postID}/update"
				enctype="multipart/form-data" method="post">
					<div class="span11">
						<input maxlength="200" name="title" id="post-title-input" class="title span11" placeholder="쓰고 싶은 단문의 내용을 입력하거나 글의 제목을 입력해주세요!"
							type="text" value="${post.title}" />
					</div>
					<div class="span1">
						<span> 
							<button id='post-ok' class="post-button btn btn-primary">
								<i class="fa fa-check"></i>
							</button>
							
						</span>
					</div>
					<c:if test="${post.isLong()}">
					<div class="span12">
						<textarea data-provide="markdown"
							id="post-content-textarea" name ="content" class="span11 post-content"
							 placeholder="글 내용을 입력해주세요!">${post.content}</textarea>
							 <div class="file-div"> </div>
					</div>
					
					</c:if>
					<input type="hidden" id="remove" name="remove">
					</form>
			</div>
			<%@ include file="/WEB-INF/common/footer.jsp"%>
		</div>
		
		


</body>


</html>