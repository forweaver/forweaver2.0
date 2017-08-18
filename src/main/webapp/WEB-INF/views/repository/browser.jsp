<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>${repository.name}-forWeaver</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<script src="/resources/forweaver/js/fileBrowser.js"></script>
<script src="/resources/forweaver/js/spin.min.js"></script>
</head>
<body>
	<script>

function showUploadContent() {
	
	$('#show-content-button').hide();
	$('#hide-content-button').show();
	$('#upload-form').fadeIn('slow');
	$('#fileBrowserTable').fadeIn('slow');
}

function hideUploadContent() {
	$('#show-content-button').show();
	$('#hide-content-button').hide();
	$('#upload-form').hide();
	$('#fileBrowserTable').show('slow');
}

function checkUpload(){
	var fileName = $("#file").val();
	fileName = fileName.toUpperCase();

	if($("#commit-message").val().length < 5){
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
	
	hideUploadContent();
	$('#labelPath').append("/");
			move = false;
			<c:forEach items='${repository.tags}' var='tag'>
			$('#tags-input').tagsinput('add',"${tag}");
			</c:forEach>
			move = true;

	
	$("select").selectpicker({style: 'btn-primary', menuStyle: 'dropdown-inverse'});
	$("#selectBranch").change(function(){
		if($("#selectBranch option:selected").val() != "empty_Branch")
			window.location = $("#selectBranch option:selected").val();
	});
});

var logHref= "/repository/${repository.name}/log-viewer/log:";
var fileBrowser = Array();
<c:forEach items="${gitFileInfoList}" var="gitFileInfo">
fileBrowser.push({
	"name" : "${fn:substring(gitFileInfo.name,0,20)}",
	"path" : "${gitFileInfo.path}",
	"directory" : ${gitFileInfo.isDirectory},
	"log" :  "${fn:substring(gitFileInfo.simpleLog,0,35)}",
	"dateInt" :  ${gitFileInfo.commitDateInt},
	"commiterName" :  "${gitFileInfo.commiterName}",
	"commiterEmail" :  "${gitFileInfo.commiterEmail}",
	"commitID" :  "${fn:substring(gitFileInfo.commitID,0,8)}",
	"date": "${gitFileInfo.getCommitDate()}"
});
</c:forEach>
var fileBrowserURL = "/repository/${repository.name}/browser/log:";
showFileBrowser("${filePath}","${selectBranch}",fileBrowser);

</script>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>

		<div class="page-header page-header-none">
			<h5><big><big><i class="fa fa-bookmark"></i> ${repository.name}</big></big>
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

			<div class="span12 row">
				<div class="span6">
					<label id="labelPath"></label>
				</div>
				<div class="span3">
				
					<sec:authorize access="isAuthenticated()">
					
					<a id="show-content-button" class="btn btn-primary"  title="저장소 .zip파일로 업로드"
						href="javascript:showUploadContent();">파일 업로드</a> 
						
					<a
						id="hide-content-button" class="btn btn-primary" title="저장소 업로드 취소"
						href="javascript:hideUploadContent();">업로드 취소</a> 
					</sec:authorize>
					<sec:authorize access="isAnonymous()">
						<button disabled="disabled" title="로그인을 하셔야 업로드가 가능합니다!"
						class="btn btn-primary"> <i class="fa fa-times"></i>
					</button> 
					</sec:authorize>
					<a style="font-size:11px"
						class="btn btn-primary" title="저장소 .zip파일로 다운로드"
						href="/repository/${repository.name}/${selectBranch}/${repository.getRepoName()}-${selectBranch}.zip">
						ZIP
					</a>
					<a style="font-size:11px"
						class="btn btn-primary" title="저장소 .tar파일로 다운로드"
						href="/repository/${repository.name}/${selectBranch}/${repository.getRepoName()}-${selectBranch}.tar">
						TAR
					</a>

				</div>

				<select id="selectBranch" class="span3">
					<option
						value="/repository/${repository.name}/browser/log:${selectBranch}">${selectBranch}</option>
					<c:forEach items="${gitBranchList}" var="gitBranchName">
						<option
							value="/repository/${repository.name}/browser/log:${gitBranchName}">${gitBranchName}</option>
					</c:forEach>
				</select>
				<form onsubmit="return checkUpload();" id="upload-form" enctype="multipart/form-data" 
				action="/repository/${repository.name}/${selectBranch}/upload" method="post">
					<div class="span12">
					<input id="path" type="hidden" name="path" value="${filePath}"></input>
						<input maxlength="50" class="title span10" type="text" id = "commit-message" name="message"
							placeholder="저장소의 각종 변경사항을 입력해주세요! (최소 5자 이상 입력!)"></input>
						<button type="submit" class="post-button btn btn-primary" title="저장소 등록"
							style="margin-top: -10px; display: inline-block;">
							<i class="fa fa-check"></i>

						</button>
					</div>
					<div id="file-div" style="padding-left: 20px;">
						<div class='fileinput fileinput-new' data-provides='fileinput'>
							<div class='input-group'>
								<div class='form-control' data-trigger='fileinput' title="업로드할 파일을 선택하세요">
									<i class='icon-file '></i> <span class='fileinput-filename'></span>
								</div>
								
								
								<span class='input-group-addon btn btn-primary btn-file'><span
									class='fileinput-new'>ZIP파일 혹은 소스파일</span>
									<span class='fileinput-exists'><i
										class='icon-repeat icon-white'></i></span> <input type='file'
									id='file' multiple='true' name='zip'></span> 
									<a href='#'
									class='input-group-addon btn btn-primary fileinput-exists' title="업로드 취소"
									data-dismiss='fileinput'><i class='icon-remove icon-white'></i></a>
									
							</div>
						</div>
					</div>
				</form>
				<table id="fileBrowserTable" class="table table-hover">
				</table>
			</div>
			<c:if test="${readme.length() > 0}">
				<div class="span12 readme-header"><i class="fa fa-info-circle"></i> 저장소 소개</div>
				<div class="span12 readme"><s:eval expression="T(com.forweaver.util.WebUtil).markDownEncoder(readme)" /></div>
				
			</c:if>
		</div>
		<!-- .row-fluid -->
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>
</html>
