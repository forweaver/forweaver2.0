<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<jsp:useBean id="dateValue" class="java.util.Date" />

<!DOCTYPE html>
<html><head>
<title>${repository.name}~${repository.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<script src="/resources/forweaver/js/spin.min.js"></script>
</head>
<body>

	<script>
	function checkUpload(){

		if($("#message").val().length < 5){
			alert("커밋 메세지는 꼭 5자 이상 입력하셔야 합니다!");
			return false;
		}
		
		var opts = {
				  lines: 13, // The number of lines to draw
				  length: 20, // The length of each line
				  width: 10, // The line thickness
				  radius: 30, // The radius of the inner circle
				  corners: 1, // Corner roundness (0..1)
				  rotate: 0, // The rotation offset
				  direction: 1, // 1: clockwise, -1: counterclockwise
				  color: '#000', // #rgb or #rrggbb or array of colors
				  speed: 1, // Rounds per second
				  trail: 60, // Afterglow percentage
				  shadow: false, // Whether to render a shadow
				  hwaccel: false, // Whether to use hardware acceleration
				  className: 'spinner', // The CSS class to assign to the spinner
				  zIndex: 2e9, // The z-index (defaults to 2000000000)
				  top: '50%', // Top position relative to parent
				  left: '50%' // Left position relative to parent
				};
				var spinner = new Spinner(opts).spin(document.getElementById('upload-form'));
		
		return true;
	}
	
	
$(document).ready(function() {
	move = false;
			<c:forEach items='${repository.tags}' var='tag'>
			$('#tags-input').tagsinput('add',"${tag}");
			</c:forEach>
			move = true;

	
	$("#selectCommit").selectpicker({style: 'btn-primary', menuStyle: 'dropdown-inverse'});
	$('#selectCommit').selectpicker('refresh');
	
	$("#selectCommit").change(function(){
		if($("#selectCommit option:selected").val() != "empty_Branch")
			window.location = $("#selectCommit option:selected").val()+"/"+"${fileName}";
	});
	

		
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
					<li class="active"><a href="/repository/${repository.name}/">브라우져</a></li>
					<li><a href="/repository/${repository.name}/log">로그</a></li>
					<li><a href="/repository/${repository.name}/community">커뮤니티</a></li>
					
					<li><a href="/repository/${repository.name}/weaver">사용자</a></li>
					<sec:authorize ifAnyGranted="ROLE_USER, ROLE_ADMIN">
					<c:if test="${repository.getCreator().equals(currentUser) }">
					<li><a href="/repository/${repository.name}/edit">관리</a></li>
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

			<div class="span12">
				<div class="span9">
					<h4 class="file-name-title">${fileName}</h4>
				</div>
				<select id="selectCommit" class="span3">
					<c:forEach items="${gitLogList}" varStatus="status" var="gitLog">
						<option 
						<c:if test='${status.count == selectCommitIndex + 1}'>
						selected="selected"
						</c:if >
							value="/repository/${repository.name}/browser/log:${fn:substring(gitLog.getLogID(),0,20)}/filepath:">
							<jsp:setProperty name="dateValue" property="time"
								value="${gitLog.getCommitDateInt()*1000}" />
							<fmt:formatDate value="${dateValue}" pattern="yy년MM월dd일 HH시mm분" />
						</option>
					</c:forEach>
				</select>
				
				<table class="table table-hover">
					<tbody>
						<tr>
							<td class="none-top-border td-post-writer-img" rowspan="2"><img
								src="${gitLog.getImgSrc()}">
							</td>
							<td 
								class="none-top-border post-top-title-short"><a class="none-color" href="/repository/${repository.name}/log-viewer/log:${fn:substring(gitLog.logID,0,8)}">
								${fn:substring(gitLog.shortMassage,0,45)}</a></td>
							<td class="none-top-border td-button" rowspan="2">
							<a	href="/repository/${repository.name}/browser/log:${fn:substring(gitLog.logID,0,20)}/filepath:${fn:replace(fileName,'.jsp', ',jsp')}">
									<span class="span-button"> <i class="fa fa-file-code-o"></i>
										<p class="p-button">소스</p>
									</span>
									
							</a></td>
						</tr>
						<tr>
							<td class="post-bottom"><b>${gitLog.commiterName}</b>
								${gitLog.getCommitDate()} &nbsp;&nbsp; <span
								style="cursor: text;" class="tag-commit tag-name">${gitLog.logID}</span>
							</td>
						</tr>
					</tbody>
				</table>
					<div class="span12">
					<form id="upload-form" onsubmit="return checkUpload();"  action="/repository/${repository.name}/file-edit" method="post">
					
						<input maxlength="50" class="title span10" type="text" id = "message" name="message"
							placeholder="코드의 변경사항을 입력해주세요! (최소 5자 이상 입력!)"></input>
						<button type="submit" class="post-button btn btn-primary" title="소스 코드 수정"
							style="margin-top: -10px; display: inline-block;">
							<i class="fa fa-check"></i>
						</button>
						<input type="hidden" id="path" name="path" value="${fileName}">
						<input type="hidden" id="log" name="log" value="${log}">
						<textarea id="code" name ="code" style="width:94%; height:400px;">${cov:htmlEscape(fileContent)}</textarea>
					</form>
					</div>

			</div>

			<!-- .span9 -->
		</div>
		<!-- .row-fluid -->
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>
</body>
</html>
