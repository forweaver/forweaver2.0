<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>Forweaver : 저장소 페이지!</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
</head>
<body>
	<script>
	
	function checkRepository(){
		var objPattern = /^[a-z0-9_]+$/;
		var name = $('#repository-name').val();
		var description = $('#repository-description').val();
		var tags = $("#tags-input").val();
		
		if(tags.length == 0){
			alert("태그가 하나도 입력되지 않았습니다. 태그를 먼저 입력해주세요!");
			return false;
		}else if(!objPattern.test(name)){
			alert("저장소명은 영문-소문자 숫자 언더바 조합이어야 합니다. 다시 입력해주세요!");
			return false;
		}
		else if(name.length <5){
			alert("저장소명은 5자 이상이어야 합니다!");
			return false;
		}else if(description.lenght <5){
			alert("저장소 소개를 입력하시지 않았습니다. 저장소 소개를 입력해주세요!");
			return false;
		}else{
			$("form:first").append($("input[name='tags']"));
			return true;
		}
		return false;
	}
	
	
	function changeValue(value){
		$('#category').val(value);
	}
		$(document).ready(function() {
			

			$( "#"+getSort(document.location.href) ).addClass( "active" );
			
					$('.tag-name').click(
							function() {
								var tagname = $(this).text();
								var exist = false;
								var tagNames = $("#tags-input").val();
								if (tagNames.length == 0 || tagNames == "")
									movePage(tagname,"");
								
								$.each(tagNames.split(","), function(index, value) {
									if (value == tagname)
										exist = true;
								});
								if (!exist)
									movePage(tagNames+ ","+ tagname+" ","");

							});					
												
						var pageCount = ${repositoryCount}/${number};
						pageCount = Math.ceil(pageCount);
						var options = {
					            currentPage: ${pageIndex},
					            totalPages: pageCount,
					            pageUrl: function(type, page, current){

					                return "${pageUrl}"+page;

					            }
					        }

					        $('#page-pagination').bootstrapPaginator(options);
				});
	</script>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>
		<div class="page-header page-header-none">
			<alert></alert>
			<h5>
				<big><big><i class=" fa fa-bookmark"></i> 진행해보세요!</big></big> <small>저장소를 생성하여 프로젝트를 진행할 수 있습니다!</small>
				<div style="margin-top: -10px" class="pull-right"  title='전체 저장소 갯수&#13;${repositoryCount}개'>

					<button class="btn btn-warning">
						<b><i class="fa fa-database"></i> ${repositoryCount}</b>
					</button>

				</div>
			</h5>
		</div>
		<div class="row">
					<sec:authorize access="isAuthenticated()">			
			<form onsubmit="return checkRepository()" action="/repository/add" method="post">

				<div id="repository-div" class="span11">
					<input maxlength="15" id ="repository-name" class="title span4"
						placeholder="저장소명 (영문-소문자 숫자 언더바 조합 최소 5자)" name="name" type="text" /> 
					
					
					<label  onclick="changeValue(0);"  class="radio radio-period"> 공개 <input type="radio"
						name="group"data-toggle="radio" checked="checked">
					</label> <label onclick="changeValue(1);" class="radio radio-period"> <input type="radio"
						name="group" data-toggle="radio"> 비공개
					</label> <label onclick="changeValue(3);" class="radio radio-period"> <input type="radio"
						name="group"  data-toggle="radio"> 과제
					</label> 
						<input maxlength="50" name ="description"class="title span12" type="text" id="repository-description"
						placeholder="저장소에 대해 설명해주세요! (최대 50자까지)"></input>
				</div>

				<div class="span1">
					<span> 
						<button id='repository-ok' title='저장소 올리기' class="post-button btn btn-primary">
							<i class="fa fa-check"></i>
						</button>
					</span>
				</div>
				<input value="0" id ="category" name="category" type="hidden"/> 	
				<input name="tags" type="hidden" id="tag-hidden"/>
			</form>
			</sec:authorize>		
			<div class="span12">
				<ul class="nav nav-tabs" id="myTab">
					<li id="age-desc"><a
						href="/repository<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:age-desc/page:1">최신순</a></li>
					<li id="public"><a
							href="/repository<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:public/page:1">공개</a></li>
					
					<li id="private"><a
						href="/repository<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:private/page:1">비공개</a></li>	
					<li id="age-asc"><a
						href="/repository<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:age-asc/page:1">오래된순</a></li>
					<sec:authorize access="isAuthenticated()">
						<li id="my"><a
							href="/repository<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:my/page:1">가입한 저장소</a></li>
					</sec:authorize>
				</ul>
			</div>

			<div class="span12">		
				<table id="repository-table" class="table table-hover">
					<tbody>
						<c:forEach items="${repositorys}" var="repository">
							<tr><td class="td-post-writer-img" rowspan="2"><a
									href="/${repository.creatorName}"> <img src="${repository.getImgSrc()}"></a></td>
								<td colspan="2" class="post-top-title"><a
									class="a-post-title" href="/repository/${repository.name}/">
										 <i class="fa fa-bookmark"></i>
									 &nbsp;${repository.name} ~
										&nbsp;${fn:substring(cov:htmlEscape(repository.description),0,100-fn:length(repository.name))}
								</a></td>
								<td class="td-button" rowspan="2">
								 <c:if test="${repository.category == 0}">
										<span
											class="span-button"><i class="fa fa-share-alt"></i><p class="p-button">공개</p>
										</span>
									</c:if>
								<c:if test="${repository.category == 1}">
										<span
											class="span-button"><i class="fa fa-lock"></i>
												<p class="p-button">비공개</p> </span>
									</c:if>
								<c:if test="${repository.category == -1}">
										<a href="/repository/${repository.name}"> <span
											class="span-button"><i class="fa fa-code-fork"></i>
												<p class="p-button">파생</p> </span>
										</a>
									</c:if>	
								<c:if test="${repository.category == 3}">
										<span
											class="span-button"><i class="fa fa-university"></i>
												<p class="p-button">과제</p> </span>
									</c:if>		
									</td>
								<td class="td-button" rowspan="2"><sec:authorize
										access="isAnonymous()">
										<a href="/login?state=null"> <span
											class="span-button"><i class="fa fa-hand-o-up"></i>
												<p class="p-button">가입</p></span>
										</a>      
									</sec:authorize> <sec:authorize access="isAuthenticated()">
										<c:if
											test="${repository.isJoin() == 0}">
											<a onclick="return confirm('정말로 가입 요청을 보내시겠습니까?')" href="/repository/${repository.name}/join"> <span
												class="span-button"><i class="fa fa-hand-o-up"></i>
													<p class="p-button">가입</p></span>
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
										<span title="태그를 클릭해보세요. 태그가 추가됩니다!"
											class="tag-name
										<c:if test="${tag.startsWith('@')}">
										tag-private
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
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>


</html>
