<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>${project.name}~${project.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
</head>
<body>
	<script>
	editorMode = true;
	function checkProject(){
		var objPattern = /^[a-zA-Z0-9]+$/;
		var name = $('#project-name').val();
		var description = $('#project-description').val();
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
		$('#category').val(value);
	}
	
	
	$(function() {
		changeValue(${project.category});
		
		move = false;
		<c:forEach items='${project.tags}' var='tag'>
		$('#tags-input').tagsinput('add',"${tag}");
		</c:forEach>
		move = true;
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
					
					<li><a href="/project/${project.name}/weaver">사용자</a></li>
					<sec:authorize ifAnyGranted="ROLE_USER, ROLE_ADMIN">
					<c:if test="${project.getCreator().equals(currentUser) }">
					<li class="active"><a href="/project/${project.name}/edit">관리</a></li>
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
			
			
				<div class="span12"><h4>프로젝트 관리</h4></div>
			<form onsubmit="return checkProject()" action="/project/${project.name}/edit" method="post">
				<div id="project-div" class="span10">
					<input id ="project-name" class="title span5" value="${project.name}"
						placeholder="프로젝트명을 입력해주세요!" type="text" readonly="readonly"/> 
					
					<c:if test="${!project.isForked()}">
					<label  onclick="changeValue(0);"  class="radio radio-period"> 공개 
					<input type="radio" name="group"data-toggle="radio" <c:if test = "${project.category == 0}">checked="checked"</c:if>>
					</label> 
					
					<label onclick="changeValue(1);" class="radio radio-period"> 
					<input type="radio" name="group" data-toggle="radio" <c:if test = "${project.category == 1 }">checked="checked"</c:if>> 비공개
					</label> 
					
					<label onclick="changeValue(3);" class="radio radio-period"> 
					<input type="radio" name="group"  data-toggle="radio" <c:if test = "${project.category == 3}">checked="checked"</c:if>> 과제
					</label> 
					</c:if>
						<input value="${project.description}" name ="description"class="title span12" type="text" id="project-description"
						placeholder="프로젝트에 대해 설명해주세요!"></input>
				</div>

				<div class="span2">
					<span>
					<a  href="/project/${project.name}/delete" onclick="return confirm('정말로 프로젝트를 삭제하시겠습니까?')"
						class="post-button btn btn-danger"  title="프로젝트를 삭제합니다!"> <i class="fa fa-remove"></i>
					</a>
						<button onclick="confirm('정말 이대로 수정하시겠습니까?')" id='project-ok' class="post-button btn btn-primary" title="프로젝트 올리기">
							<i class="fa fa-check"></i>
						</button>

					</span>
				</div>
				<input value="0" id ="category" name="category" type="hidden"/> 	
			</form>
			
			<div class="span12">
					<hr/>
				</div>
				
			<div class="span12"><h4>저장소 관리</h4></div>
			<div class="span12" style="text-align:center;margin-bottom:40px;">
			
			<a onclick="return confirm('정말로 프로젝트를 초기화시킬 생각입니까? 복구 불가능 합니다!')" href="/project/${project.name}/reset" class="btn btn-danger">저장소 초기화</a>
			</div>
		</div>
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>


</html>
