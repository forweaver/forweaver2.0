<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>Forweaver : 회원 정보 수정</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
</head>
<body>

<script>

editorMode = true;

function checkWeaver(){
	if($("#tags-input").val().length < 1){
		alert("태그를 하나 이상 입력해주세요!");
		return false;
	}

	$("form:first").append($("input[name='tags']"));
	return true;
}

$(document).ready(function() {
	
	move = false;
	<c:forEach items='${weaver.tags}' var='tag'>
	$('#tags-input').tagsinput('add',"${tag}");
	</c:forEach>
	move = true;

	
	$("#image").change(function(){
        readURL(this);
    });
	
});


</script>

<div class="container">
<%@ include file="/WEB-INF/common/nav.jsp"%>
<div class="row">
	<div class="span12">
				<ul class="nav nav-tabs">
					<li class="active" id="age-desc"><a href="/edit">정보수정</a></li>
					<li id="age-desc"><a href="/del">탈퇴</a></li>
				</ul>
	</div>
				<form onsubmit="return checkWeaver()"  enctype="multipart/form-data" class="form-horizontal" action="/edit" method="POST">
						<div class="span6">
						
						<div title ="비밀번호를 변경하시려면 여기에 먼저 기존 비밀번호를 입력하세요!" class="control-group">
							<label for="password" class="control-label">기존 비밀번호</label>
							<div class="controls">
								<input id="password" name="password" class="input-large"
									type="password" />

							</div>
						</div>
						<div title ="새로운 비밀번호를 입력하세요!" class="control-group">
							<label for="rePassword" class="control-label">변경할 비밀번호</label>
							<div class="controls">
								<input id="rePassword" name="newpassword" class="input-large"
									type="password" />

							</div>
						</div>

						</div>
						<div class="span4">
						
						<div class ="control-group" style="text-align:center;">
						<img id="preview" src="/${weaver.id}/img" style="height:130px;width:130px;" class="img-polaroid" src="">
						</div>
						
						
						<div class="control-group">
							<div id="file-div" style="padding-left: 20px;">
					<div class='fileinput fileinput-new' data-provides='fileinput'>
					  <div class='input-group' style="width: 340px;">
					    <div class='form-control' data-trigger='fileinput' style="width: 210px;" ><i class='icon-file '></i> <span class='fileinput-filename'></span></div>
					    <span class='input-group-addon btn btn-primary btn-file'><span class='fileinput-new'>
					    <i class='fa fa-arrow-circle-o-up icon-white'></i></span>
					    <span class='fileinput-exists'><i class='icon-repeat icon-white'></i></span>
						<input onchange ='fileUploadChange(this);' type='file' id='image' multiple='true' name='image'></span>
					   <a href='#' class='input-group-addon btn btn-primary fileinput-exists' data-dismiss='fileinput'><i class='icon-remove icon-white'></i></a>
					  </div>
					</div>
						</div>
					</div>
						</div>
						<div class="span11">
						<div class="control-group">
							<label for="say" class="control-label">자기소개</label>
							<div class="controls">
								<input name="say" value="${cov:htmlEscape(weaver.say)}" placeholder="마지막으로 자신을 나타낼 자기소개를 입력해주세요!"  id="say" style="width:90%;" type="text"/>

							</div>
						</div>
						<div class="join-form-actions-white">

							<button type="submit" class="btn btn-block btn-inverse"><i class="fa fa-pencil-square"></i>&nbsp;&nbsp;수정하기</button>
						</div>
						</div>
				</form>
		</div>
		<br><br>
		
				<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>
</body>


</html>