<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>${repository.name}~${repository.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
</head>
<body>
	<script>
	editorMode = true;
	function checkRepository(){
		var objPattern = /^[a-zA-Z0-9]+$/;
		var name = $('#repository-name').val();
		var description = $('#repository-description').val();
		var tags = $("#tags-input").val();
		
		if(tags.length == 0){
			alert("태그가 하나도 입력되지 않았습니다. 태그를 먼저 입력해주세요!");
			return false;
		}else{
			$("form:first").append($("input[name='tags']"));
			return true;
		}
	}
	
	function changeValue(value){
		$('#authLevel').val(value);
	}
	
	
	$(function() {
		changeValue(${repository.authLevel});		
		move = false;
		<c:forEach items='${repository.tags}' var='tag'>
		$('#tags-input').tagsinput('add',"${tag}");
		</c:forEach>
		move = true;
	});
	</script>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>
		<div class="page-header page-header-none">
			<h5>
						<big><big><i class="fa fa-bookmark"></i> ${repository.name}</big></big>
<small>${repository.description}</small>
			</h5>
		</div>
		<div class="row">
			<div class="span8">
				<ul class="nav nav-tabs">
					<li><a href="/repository/${repository.name}/">브라우져</a></li>
					<li><a href="/repository/${repository.name}/log">로그</a></li>
					<li><a href="/repository/${repository.name}/community">커뮤니티</a></li>
					
					<li><a href="/repository/${repository.name}/weaver">사용자</a></li>
					<sec:authorize ifAnyGranted="ROLE_USER, ROLE_ADMIN">
					<c:if test="${repository.getCreator().equals(currentUser) }">
					<li class="active"><a href="/repository/${repository.name}/edit">관리</a></li>
					</c:if>
					</sec:authorize>
					<li><a href="/repository/${repository.name}/info">정보</a></li>
					
					
				</ul>
			</div>
			<div class="span4">
				<div class="input-block-level input-prepend" title="http 주소로 저장소를 복제할 수 있습니다!&#13;복사하려면 ctrl+c 키를 누르세요.">
					<span class="add-on"><i class="fa fa-git"></i></span> <input
						value="http://${pageContext.request.serverName}/g/${repository.name}.git" type="text"
						class="input-block-level">
				</div>
			</div>
			
			
				<div class="span12"><h4>저장소 관리</h4></div>
			<form onsubmit="return checkRepository()" action="/repository/${repository.name}/edit" method="post">
				<div id="repository-div" class="span10">
					<input id ="repository-name" class="title span5" value="${repository.name}"
						placeholder="저장소명을 입력해주세요!" type="text" readonly="readonly"/> 
					
					
					<label  onclick="changeValue(0);"  class="radio radio-period"> 일반 
					<input type="radio" name="group"data-toggle="radio" <c:if test = "${repository.authLevel == 1}">checked="checked"</c:if>>
					</label> 
					
					<label onclick="changeValue(1);" class="radio radio-period"> 
					<input type="radio" name="group" data-toggle="radio" <c:if test = "${repository.authLevel == 2 }">checked="checked"</c:if>> 비공개
					</label> 
					
					<label onclick="changeValue(3);" class="radio radio-period"> 
					<input type="radio" name="group"  data-toggle="radio" <c:if test = "${repository.authLevel == 0}">checked="checked"</c:if>> 공개
					</label> 
						<input value="${repository.description}" name ="description"class="title span12" type="text" id="repository-description"
						placeholder="저장소에 대해 설명해주세요!"></input>
				</div>

				<div class="span2">
					<span>
					<a  href="/repository/${repository.name}/delete" onclick="return confirm('정말로 저장소를 삭제하시겠습니까?')"
						class="post-button btn btn-danger"  title="저장소를 삭제합니다!"> <i class="fa fa-remove"></i>
					</a>
						<button onclick="confirm('정말 이대로 수정하시겠습니까?')" id='repository-ok' class="post-button btn btn-primary" title="저장소 올리기">
							<i class="fa fa-check"></i>
						</button>

					</span>
				</div>
				<input value="0" id ="authLevel" name="authLevel" type="hidden"/> 	
			</form>
			
			<div class="span12">
					<hr/>
				</div>
				
			<div class="span12"><h4>저장소 관리</h4></div>
			<div class="span12" style="text-align:center;margin-bottom:40px;">
			
			<a onclick="return confirm('정말로 저장소를 초기화시킬 생각입니까? 복구 불가능 합니다!')" href="/repository/${repository.name}/reset" class="btn btn-danger">저장소 초기화</a>
			</div>
		</div>
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>


</html>
