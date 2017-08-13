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

	
	if($("#password").val().length < 1){
		alert("비밀번호를 입력해주세요!");
		return false;
	}

	if(!confirm("회원 탈퇴시에 그동안 업로드한 글과 자료가 모두 삭제됩니다. 정말 탈퇴하시겠습니까?"))
		return false;
	
	return true;
}

$(document).ready(function() {
	
	move = false;
	<c:forEach items='${weaver.tags}' var='tag'>
	$('#tags-input').tagsinput('add',"${tag}");
	</c:forEach>
	move = true;
	
});


</script>

<div class="container">
<%@ include file="/WEB-INF/common/nav.jsp"%>
<div class="row">

	<div class="span12">
	<div class='alert'>비밀번호를 입력하시고 탈퇴하기를 클릭하시면 모든 정보를 삭제하고 탈퇴됩니다!</div>
				<ul class="nav nav-tabs">
					<li id="age-desc"><a href="/edit">정보수정</a></li>
					<li class="active"  id="age-desc"><a href="/del">탈퇴</a></li>
				</ul>
	</div>
	
				<form onsubmit="return checkWeaver()"  class="form-horizontal" action="/del" method="POST">
						<div class="span3"></div>
						<div class="span6">
						
							<div style="margin-left:-30px"title ="회원 탈퇴를 하시려면 비밀번호를 입력해주세요!" class="control-group">
								<label for="password" class="control-label">비밀번호</label>
								<div class="controls">
									<input id="password" name="password" class="input-large"
										type="password" />
									
								</div>
								
							</div>
							
						</div>
						<div class="span4"></div>
						<div class="span4">
						<button type="submit" class="btn btn-block btn-inverse"><i class="fa fa-pencil-square"></i>&nbsp;&nbsp;탈퇴하기</button>
						</div>
				</form>
		</div>	
		<br><br>
       <%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>
</body>


</html>