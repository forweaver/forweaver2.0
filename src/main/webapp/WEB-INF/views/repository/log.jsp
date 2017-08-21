<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>${repository.name}~${repository.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<script src="/resources/forweaver/js/fileBrowser.js"></script>
</head>
<body>
	<script>
		$(document).ready(function() {
			move = false;
			<c:forEach items='${repository.tags}' var='tag'>
			$('#tags-input').tagsinput('add',"${tag}");
			</c:forEach>
			move = true;

			
			$("select").selectpicker({
				style : 'btn-primary',
				menuStyle : 'dropdown-inverse'
			});
			$("#selectBranch").change(function() {
				if ($("#selectBranch option:selected").val() != "empty_Branch")
					window.location = $("#selectBranch option:selected").val();
			});

			var pageCount = ${gitCommitListCount+1}/15;
			pageCount = Math.ceil(pageCount);
			var options = {
		            currentPage: ${pageIndex},
		            totalPages: pageCount,
		            pageUrl: function(type, page, current){

		                return "/repository/${repository.name}/log/log:${selectBranch}/page:"+page;

		            }
		        }

		        $('#page-pagination').bootstrapPaginator(options);
			$('a').attr('rel', 'external');
			
		});
	</script>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>

		<div class="page-header page-header-none">
			<h5><big><big><i class="fa fa-bookmark"></i> ${repository.name}</big></big>
				<small>${repository.description}</small>
			</h5>
		</div>
			<!-- .span3 -->
			<div class ="row">

			<div class="span8">
				<ul class="nav nav-tabs">
					<li><a href="/repository/${repository.name}/">브라우져</a></li>
					<li class="active" ><a href="/repository/${repository.name}/log">로그</a></li>
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
				<div class="span9">
					<h4 style="margin: 10px 0px 0px 0px"><i class="fa fa-info-circle"></i>  로그 목록</h4>
				</div>
				<select id="selectBranch" class="span3">
					<option value="/repository/${repository.name}/log/log:${selectBranch}">${ selectBranch}</option>
					<c:forEach items="${gitBranchList}" var="gitBranchName">
						<option value="/repository/${repository.name}/log/log:${gitBranchName}">${ gitBranchName}</option>
					</c:forEach>
				</select>
				
				<table class="table table-hover">
					<tbody>
						<c:forEach items="${gitCommitList}" var="gitCommit">
							<tr>
								<td class="td-post-writer-img" rowspan="2"><img
									src="${gitCommit.getImgSrc()}">
								</td>
								
								<td style="width: 710px;" class="post-top-title-short"><a
									class="none-color"
									href="/repository/${repository.name}/log-viewer/log:${fn:substring(gitCommit.logID,0,8)}">
										${fn:substring(gitCommit.shortMassage,0,45)}</a></td>
								
								<td class="td-log-button" rowspan="2">
								<a	href="/repository/${repository.name}/browser/log:${fn:substring(gitCommit.logID,0,8)}">
										<span class="span-button"> <i class="fa fa-eye"></i>
											<p class="p-button">전체</p></span>
									</a>
									
								<a	href="/repository/${repository.name}/${fn:substring(gitCommit.logID,0,8)}/${repository.getRepoName()}-${fn:substring(gitCommit.logID,0,8)}.zip">
										<span class="span-button"> <i class="fa fa-arrow-circle-o-down"></i>
											<p class="p-button">다운</p></span>
									</a>									
								</td>
							</tr>
							<tr>
								<td class="post-bottom"><b>${gitCommit.commiterName}</b>
									${gitCommit.getCommitDate()} &nbsp;&nbsp; 
									<span class="tag-commit tag-name">${gitCommit.logID}</span>
								</td>

							</tr>
						</c:forEach>
					</tbody>
				</table>
				<div class = "text-center">
					<div id="page-pagination"></div>
				</div>
			</div>
			<!-- .span9 -->

			<!-- .tabbable -->
		</div>
		<!-- .row-fluid -->
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>
</html>
