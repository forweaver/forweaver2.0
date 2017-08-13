<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title>${project.name}~${project.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
</head>
<body>
	<script>
		$(document)
				.ready(
						function() {
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
				<big><big> <c:if test="${!project.isForkProject()}">
							<i class="fa fa-bookmark"></i>
						</c:if> <c:if test="${project.isForkProject()}">
							<i class="fa fa-code-fork"></i>
						</c:if> ${project.name}
				</big></big> <small>${project.description}</small>
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
					<li><a href="/project/${project.name}/edit">관리</a></li>
					</c:if>
					</sec:authorize>
					<li class="active"><a href="/project/${project.name}/info">정보</a></li>
					
					
				</ul>
			</div>
			<div class="span4">
				<div class="input-block-level input-prepend" title="http 주소로 저장소를 복제할 수 있습니다!&#13;복사하려면 ctrl+c 키를 누르세요.">
					<span class="add-on"><i class="fa fa-git"></i></span> <input
						value="http://${pageContext.request.serverName}/g/${project.name}.git"
						type="text" class="input-block-level">
				</div>
			</div>
			<div class="span8"></div>
			<div class="carousel span4">
				<ol class="carousel-indicators">
					<a href='/project/${project.name}/info'>
						<li class="active"></li>
					</a>
					<a href='/project/${project.name}/info:stream'>
						<li></li>
					</a>
					<a href='/project/${project.name}/info:frequency'>
						<li></li>
					</a>
				</ol>
			</div>
		</div>

		<div class="row">
			<div class="span12">
				<h4>프로젝트 정보 <small>${gitInfo.getEnd().getAuthorIdent().getWhen().toLocaleString()} ~ ${gitInfo.getStart().getAuthorIdent().getWhen().toLocaleString()}</small></h4>
				<div class="span4">
					<ul>
						<li><span class="label label-info" title="라인">${gitInfo.getCommits()}</span>
							커밋들</li>
						<li><span class="label label-info" title="라인">${gitInfo.getMerges()}</span>
							병합 커밋들</li>
						<li><span class="label label-info" title="라인">${gitInfo.getAuthors().size()}</span>
							저자들</li>
						<li><span class="label label-info" title="라인">${gitInfo.getCommitters().size()}</span>
							커미터들</li>

					</ul>
				</div>
				<div class="span4">
					<ul>
						
						<li><span class="label label-info" title="라인">${gitInfo.getLinesAdded()}</span>
							라인 추가</li>
						<li><span class="label label-info" title="라인">${gitInfo.getLinesEdited()}</span>
							라인 수정</li>
						<li><span class="label label-info" title="라인">${gitInfo.getLinesDeleted()}</span>
							라인 삭제</li>
					</ul>
				</div>
				<div class="span3">
					<ul>
						<li><span class="label label-info" title="라인">${gitInfo.getAdded()}</span>
							파일 추가</li>
						<li><span class="label label-info" title="라인">${gitInfo.getModified()}</span>
							파일 수정</li>
						<li><span class="label label-info" title="라인">${gitInfo.getDeleted()}</span>
							파일 삭제</li>
					</ul>
				</div>
			</div>
		</div>
		<hr>
		<h4>
			기여자들 <small>소스 코드를 수정한 사람들</small>
		</h4>
		<div class="row">
			<div class="span6">
				<h5>
					저자 <small>${gitInfo.getAuthors().size()}</small>
				</h5>
				<ul>
					<c:forEach items="${gitInfo.getAuthors()}" var="author">
						<li>${author}&nbsp;&nbsp;<span class="label label-success" title="라인">${gitInfo.getAuthoredCommits(author)}</span></li>
					</c:forEach>
				</ul>
			</div>
			<div class="span6">
				<h5>
					커미터 <small>${gitInfo.getCommitters().size()}</small>
				</h5>
				<ul>
					<c:forEach items="${gitInfo.getCommitters()}" var="committer">
						<li>${committer}&nbsp;&nbsp;<span class="label label-success" title="라인">${gitInfo.getCommittedCommits(committer)}</span></li>
					</c:forEach>
				</ul>
			</div>
		</div>
		<div class="row">
			<div class="span6">
				<h5>
					라인 수정 <small>라인 수정 내역</small>
				</h5>
				<ul>
					<c:forEach items="${gitInfo.getAuthorLineImpacts()}" var="author">
						<li>${author}&nbsp;&nbsp;<span class="label label-success" title="라인">+${gitInfo.getAuthorLineImpact(author).getAdd()}</span>
							<span class="label label-warning" title="라인">${gitInfo.getAuthorLineImpact(author).getEdit()}</span>
							<span class="label label-important" title="라인">-${gitInfo.getAuthorLineImpact(author).getDelete()}</span>
						</li>
					</c:forEach>
				</ul>
			</div>
			<div class="span6">
				<h5>
					파일 수정 <small>파일 수정 내역</small>
				</h5>
				<ul>
					<c:forEach items="${gitInfo.getAuthorFileImpacts()}" var="author">
						<li>${author}&nbsp;&nbsp;<span class="label label-success" title="라인">+${gitInfo.getAuthorFileImpact(author).getAdd()}</span>
							<span class="label label-warning" title="라인">${gitInfo.getAuthorFileImpact(author).getEdit()}</span>
							<span class="label label-important" title="라인">-${gitInfo.getAuthorFileImpact(author).getDelete()}</span>
						</li>
					</c:forEach>
				</ul>
			</div>
		</div>
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>
</body>
</html>