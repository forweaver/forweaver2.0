<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<jsp:useBean id="dateValue" class="java.util.Date" />
<!DOCTYPE html>
<html><head>
<title>${project.name}~${project.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<%@ include file="/WEB-INF/includes/syntaxhighlighterSrc.jsp"%>
</head>

<body>
	<script>
	var blameArray = [];
	blameArray.push([
	         	    '1111',
	         		'@@@@',
	         		'3333',
	         		'4444'
	         		]);//쓰레기 값
	<c:forEach items="${gitBlameList}" var="gitBlame">
	blameArray.push([
	    '${gitBlame.getUserName()}',
		'${gitBlame.getCommitID()}',
		'${gitBlame.getCommitTime()}',
		'${gitBlame.getUserEmail()}'
		]);
	</c:forEach>
	$(function() {
	
	
	
	move = false;
			<c:forEach items='${project.tags}' var='tag'>
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
	
	 setTimeout(function(){ // 에러가 있어서 0.3초 뒤에 실행
		 for(var i=1;i<=blameArray.length;i++)
			 if(blameArray[i-1][1] != blameArray[i][1])
		 	$('td.gutter > div.line.number'+i).html(
		 			"<span><a href='/"+blameArray[i][3].replace('.',',')+"'>"+blameArray[i][0]+
		 			"</a>  <a href='/project/${project.name}/commitlog-viewer/commit:"+blameArray[i][1]+"'>"+
		 			blameArray[i][1]+"</a>  "+blameArray[i][2]+"</span>  "+i
		 	);
	}, 300);
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
					<li class="active"><a href="/project/${project.name}/">브라우져</a></li>
					<li><a href="/project/${project.name}/commitlog">커밋</a></li>
					<li><a href="/project/${project.name}/community">커뮤니티</a></li>	
					<li><a href="/project/${project.name}/weaver">사용자</a></li>
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
				<div class="span9">
					<h4 class="file-name-title">${fileName}</h4>
				</div>
				<select id="selectCommit" class="span3">
					<c:forEach items="${gitLogList}" varStatus="status" var="gitLog">
						<option 
						<c:if test='${status.count == selectCommitIndex + 1}'>
						selected="selected"
						</c:if >
							value="/project/${project.name}/blame/commit:${fn:substring(gitLog.getCommitLogID(),0,20)}/filepath:">
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
								src="${gitCommitLog.getImgSrc()}">
							</td>
							<td style="width: 800px;"
								class="none-top-border post-top-title-short"><a class="none-color" href="/project/${project.name}/commitlog-viewer/commit:${fn:substring(gitCommitLog.commitLogID,0,8)}">
								${fn:substring(gitCommitLog.shortMassage,0,45)}</a></td>
							<td class="none-top-border td-button" rowspan="2">
							<a	href="/project/${project.name}/browser/commit:${fn:substring(gitCommitLog.commitLogID,0,8)}">
									<span class="span-button"> <i class="fa fa-eye"></i>
										<p class="p-button">전체</p>
									</span>
									
							</a></td>
							<td class="none-top-border td-button" rowspan="2">
							<a	href="/project/${project.name}/data/commit:${fn:substring(gitCommitLog.commitLogID,0,20)}/filepath:/${fn:replace(fileName,'.jsp', ',jsp')}">
									<span class="span-button"> <i class="fa fa-download"></i>
										<p class="p-button">다운</p>
									</span>
									
							</a></td>
							<td class="none-top-border td-button" rowspan="2">
							<a	href="/project/${project.name}/browser/commit:${fn:substring(gitCommitLog.commitLogID,0,20)}/filepath:${fn:replace(fileName,'.jsp', ',jsp')}">
									<span class="span-button"> <i class="fa fa-file-code-o"></i>
										<p class="p-button">소스</p>
									</span>
									
							</a></td>
						</tr>
						<tr>
							<td class="post-bottom"><b>${gitCommitLog.commiterName}</b>
								${gitCommitLog.getCommitDate()} &nbsp;&nbsp; <span
								style="cursor: text;" class="tag-commit tag-name">${gitCommitLog.commitLogID}</span>
							</td>
						</tr>
					</tbody>
				</table>
				<div style="padding-top:30px;" class="well-white">
					<pre id="source-code" >${cov:htmlEscape(fileContent)}</pre>
				</div>
			</div>

			<!-- .span9 -->
		</div>
		<!-- .row-fluid -->
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>
</body>
</html>
