<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<jsp:useBean id="dateValue" class="java.util.Date" />

<!DOCTYPE html>
<html><head>
<title>${repository.name}~${repository.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<%@ include file="/WEB-INF/includes/syntaxhighlighterSrc.jsp"%>
</head>
<body>

	<script>

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
	
	$("#source-code").addClass("brush: "+extensionSeach(document.location.href)+";");
	SyntaxHighlighter.all();
		
});

</script>
	<div class="container">
		<%@ include file="/WEB-INF/views/common/nav.jsp"%>

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
					<sec:authorize access="isAuthenticated()">
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
							<a	href="/repository/${repository.name}/browser/log:${fn:substring(gitLog.logID,0,8)}">
									<span class="span-button"> <i class="fa fa-eye"></i>
										<p class="p-button">전체</p>
									</span>
							</a></td>
							<td class="none-top-border td-button" rowspan="2">
							<a	href="/repository/${repository.name}/data/log:${fn:substring(gitLog.logID,0,20)}/filepath:/${fn:replace(fileName,'.jsp', ',jsp')}">
									<span class="span-button"> <i class="fa fa-download"></i>
										<p class="p-button">다운</p>
									</span>
									
							</a></td>
							<c:if  test="${isCodeName}">
							<sec:authorize access="isAuthenticated()">
							<td class="none-top-border td-button" rowspan="2">
							<a	href="/repository/${repository.name}/edit/log:${fn:substring(commit,0,20)}/filepath:/${fn:replace(fileName,'.jsp', ',jsp')}">
									<span class="span-button"> <i class="fa fa-edit"></i>
										<p class="p-button">편집</p>
									</span>
									
							</a></td>
							</sec:authorize>
							<td class="none-top-border td-button" rowspan="2">
							<a	href="/repository/${repository.name}/blame/log:${fn:substring(gitLog.logID,0,20)}/filepath:/${fn:replace(fileName,'.jsp', ',jsp')}">
									<span class="span-button"> <i class="fa fa-search"></i>
										<p class="p-button">추적</p>
									</span>
									
							</a></td>
							</c:if>
						</tr>
						<tr>
							<td class="post-bottom"><b>${gitLog.commiterName}</b>
								${gitLog.getCommitDate()} &nbsp;&nbsp; <span
								style="cursor: text;" class="tag-commit tag-name">${gitLog.logID}</span>
							</td>
						</tr>
					</tbody>
				</table>
				<c:if  test="${isImageName}">
					<div style="padding-top:30px;">
						<img src="/repository/${repository.name}/data/log:${gitLog.logID}/filepath:${filePath}">
					</div>
				</c:if>
				<c:if  test="${!isImageName}">
					<div style="padding-top:30px;" class="well-white">
						<pre id="source-code" > ${cov:htmlEscape(fileContent)}</pre>
					</div>
				</c:if>
			</div>

			<!-- .span9 -->
		</div>
		<!-- .row-fluid -->
		<%@ include file="/WEB-INF/views/common/footer.jsp"%>
	</div>
</body>
</html>
