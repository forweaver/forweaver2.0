<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title>Forweaver : ${weaver.getId()}님의 저장소</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
</head>
<body>
	<script type="text/javascript">	
		$(function() {
			$( "#"+getSort(document.location.href) ).addClass( "active" );
			
					$('.tag-name').click(
							function() {
								var tagname = $(this).text();
								var exist = false;
								var tagNames = $("#tags-input").val();
								
								if (tagNames.length == 0 || tagNames == "")
									moveUserPage("/${weaver.getId()}/repository/",tagname,"");
									
								$.each(tagNames.split(","), function(index, value) {
									if (value == tagname)
										exist = true;
								});
								if (!exist){
									moveUserPage("/${weaver.getId()}/repository/",tagNames+ ","+ tagname+" ","");
								}
							});
					
					$('#search-button').click(
							function() {
									var tagNames = $("#tags-input").val();
									if(tagNames.length == 2){
										alert("태그가 하나도 입력되지 않았습니다. 태그를 먼저 입력해주세요!");
										return;
									}
									moveUserPage("/${weaver.getId()}/repository",tagNames,$('#post-title-input').val());							
							});
					
					var pageCount = ${postCount+1}/${number};
					pageCount = Math.ceil(pageCount);					
					var options = {
				            currentPage: ${pageIndex},
				            totalPages: pageCount,
				            pageUrl: function(type, page, current){

				                return "${pageUrl}"+page;

				            }
				        }

				        $('#page-pagination').bootstrapPaginator(options);$('a').attr('rel', 'external');
		});

		
	</script>
	<div class="container">
		<%@ include file="/WEB-INF/views/common/nav.jsp"%>
		<div class="pull-right">
			<button class="btn btn-warning">
				<b><i class="fa fa-database"></i> ${repositoryCount}</b>
			</button>
		</div>
		<div class="page-header page-header-none">
			<alert></alert>
			<h5 style="margin-left: 50px; text-align: center">
				<img style="height: 60px; width: 60px;" class="img-polaroid"
					src="${weaver.getImgSrc()}">
			</h5>
			<h5 style="text-align: center">

				<big><i class="fa fa-quote-left"></i> ${cov:htmlEscape(weaver.getSay())} <i
					class="fa fa-quote-right"></i></big> <small>- ${weaver.getId()}</small>
			</h5>
			<div class="row">
				<div class="span12">
					<ul class="nav nav-tabs pull-left" id="myTab">
						<li id="age-desc"><a
							href="/${weaver.getId()}/repository<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:age-desc/page:1">전체</a></li>
						<li id="admin"><a
							href="/${weaver.getId()}/repository<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:admin/page:1">관리중인
								저장소</a></li>
						<li id="join"><a
							href="/${weaver.getId()}/repository<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:join/page:1">참여중인
								저장소</a></li>
					</ul>

					<div class="navbar navbar-inverse">
						<ul style="border-bottom: 0px;" class="nav pull-right">
							<li class="dropdown">
								<button class="btn btn-primary dropdown-toggle"
									data-toggle="dropdown">
									<span style="font-size: 14px;"> <i
										class=" fa fa-bookmark"></i>&nbsp;저장소
									</span> <b class="caret"></b>
								</button>
								<ul class="dropdown-menu">
									<li><a href="/${weaver.getId()}/"><i
											class=" fa fa-comments"></i>&nbsp;&nbsp;커뮤니티</a></li>
									 <!--  <li><a href="/${weaver.getId()}/lecture"><i
											class=" fa fa-university"></i>&nbsp;&nbsp;강의</a></li>-->
									<li><a href="/${weaver.getId()}/code"><i
											class=" fa fa-rocket"></i>&nbsp;&nbsp;코드</a></li>
								</ul>
							</li>
						</ul>
					</div>
				</div>

				<div class="span12">
					<table id="repository-table" class="table table-hover">
						<tbody>
							<c:forEach items="${repositorys}" var="repository">
								<tr>
								<td class="td-post-writer-img" rowspan="2"><a
									href="/${repository.creatorName}"> <img src="${repository.getImgSrc()}"></a></td>
									<td colspan="2" class="post-top-title"><a
										class="a-post-title" href="/repository/${repository.name}/"> <i
											class="fa fa-bookmark"></i> &nbsp;${repository.name} ~
											&nbsp;${fn:substring(cov:htmlEscape(repository.description),0,100-fn:length(repository.name))}
									</a></td>
									<td class="td-button" rowspan="2">
								 <c:if test="${repository.authLevel == 0}">
										<span
											class="span-button"><i class="fa fa-share-alt"></i><p class="p-button">일반</p>
										</span>
									</c:if>
								<c:if test="${repository.authLevel == 1}">
										<span
											class="span-button"><i class="fa fa-lock"></i>
												<p class="p-button">비공개</p> </span>
									</c:if>
								<c:if test="${repository.authLevel == 2}">
										<span
											class="span-button"><i class="fa fa-lock"></i>
												<p class="p-button">비공개</p> </span>
									</c:if>		
									</td>
									<td class="td-button" rowspan="2"><sec:authorize
										access="isAnonymous()">
										<a href="/repository/${repository.name}/join"> <span
											class="span-button"><i class="fa fa-times"></i>
												<p class="p-button">가입</p></span>
										</a>
									</sec:authorize> <sec:authorize access="isAuthenticated()">
										<c:if
											test="${repository.isJoin() == 0}">
											<a href="/repository/${repository.name}/join"> <span
												class="span-button"><i class="fa fa-times"></i>
													<p class="p-button">미가입</p></span>
											</a>
										</c:if>
										<c:if
											test="${repository.isJoin() == 1}">
											<a href="/repository/${repository.name}"> <span
												class="span-button"><i class="fa fa-user"></i>
													<p class="p-button">회원</p></span>
											</a>
										</c:if>
										<c:if test="${repository.isJoin () == 2}">
											<a href="/repository/${repository.name}"> <span
												class='span-button'><i class="fa fa-user"></i>
													<p class='p-button'>관리자</p></span></a>
										</c:if>
									</sec:authorize></td>
								</tr>
								<tr>
								<td class="post-bottom"><a href="/${repository.creatorName}"><b>${repository.creatorName}</b></a>
									${repository.getDateFormat()}</td>
									<td class="post-bottom-tag"><c:forEach
											items="${repository.tags}" var="tag">
											<span
												class="tag-name
										<c:if test="${tag.startsWith('@')}">
										tag-private
										</c:if>
										<c:if test="${tag.startsWith('$')}">
										tag-massage
										</c:if>
										">${tag}</span>
										</c:forEach></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
					<div class="text-center">
						<div id="page-pagination"></div>
					</div>
				</div>
			</div>
			<%@ include file="/WEB-INF/views/common/footer.jsp"%>
		</div>
	</div>
</body>


</html>