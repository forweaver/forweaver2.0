<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>Forweaver : 공유해보세요!</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<script src="/resources/forweaver/js/spin.min.js"></script>
<%@ include file="/WEB-INF/includes/syntaxhighlighterSrc.jsp"%>
<style>
.syntaxhighlighter{overflow:hidden;}
</style>
</head>
<body>
	<script type="text/javascript">
	
	function checkCode(){
		var objPattern = /^[a-z0-9_]+$/;
		var fileName = $("#file").val();
		var output = $("#output").val();
		var tags = $("#tags-input").val();
		
		if(fileName == ""){
			alert("파일을 업로드해 주세요!");
			return false;
		}else if(tags.length == 0){
			alert("태그를 하나라도 입력해주세요!");
			return false;
		}else if($('#code-content').val().length <5 ){
			alert("코드 설명을 5자 이상 입력하지 않았습니다!");
			return false;
		}else if(output.length > 0 && !isImage(output)){
			alert("결과화면이 이미지 파일이 아닙니다!");
			return false;
		}
		
			$("form:first").append($("input[name='tags']"));
			
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
					var spinner = new Spinner(opts).spin(document.getElementById('codeForm'));
			
			return true;
	}
	
		function showCodeContent() {
			var tags = $("#tags-input").val();
			if(tags.length == 0){
				alert("태그가 하나도 입력되지 않았습니다. 태그를 먼저 입력해주세요!");
				return;
			}
			$('#page-pagination').hide();
			$('#post-table').hide();
			$('#code-content-textarea').fadeIn('slow');
			$('#post-ok').show();
			$('#search-button').hide();
			$('#search-div').hide();
			$('#post-div').fadeIn('slow');
			$('#show-content-button').hide();
			$('#hide-content-button').show();
			$('#file-div').fadeIn('slow');
			editorMode = true;
		}

		function hideCodeContent() {
			$('#page-pagination').show();
			$('#post-table').show();
			$('#search-div').show();
			$('#post-div').hide();
			$('#code-content-textarea').hide();
			$('#post-ok').hide();
			$('#search-button').show();
			$('#show-content-button').show();
			$('#hide-content-button').hide();
			$('#file-div').hide();
			editorMode = false;
		}

		$(function() {
			
			hideCodeContent();
			
			$( "#post-search-input" ).focus(function() {
				var tags = $("#tags-input").val();
				if(tags.length == 0){
					$( "#post-search-input" ).val('');
					alert("태그가 하나도 입력되지 않았습니다. 태그를 먼저 입력해주세요!");
					return;
				}
			});
			
			
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
								if (!exist){
									movePage(tagNames+ ","+ tagname+" ","");
								}
							});
					
					$('#search-button').click(
							function() {
									var tagNames = $("#tags-input").val();
									movePage(tagNames,$('#post-search-input').val());							
							});
					
					$('#post-search-input').val(getSearchWord(document.location.href));
					
					$('#post-search-input').keyup(
							function(e) {
								if(!editorMode && e.keyCode == 13){
									var tagNames = $("#tags-input").val();
									movePage(tagNames,$('#post-search-input').val());
								}
							});
					var pageCount = ${codeCount+1}/${number};
					pageCount = Math.ceil(pageCount);					
					var options = {
				            currentPage: ${pageIndex},
				            totalPages: pageCount,
				            pageUrl: function(type, page, current){

				                return "${pageUrl}"+page;

				            }
				        }

				        $('#page-pagination').bootstrapPaginator(options);$('a').attr('rel', 'external');
				        
				        
			 <c:forEach	items="${codes}" var="code" varStatus="status">	
				 $("#code-${status.count}").addClass("brush: "+extensionSeach('${code.getFirstCodeName()}')+";");
			 </c:forEach>
		});

		SyntaxHighlighter.all();
		
		function fileUploadChange(fileUploader){
			var fileName = $(fileUploader).val();	
			if(fileName !="" && !isImage(fileName))
				alert("이미지 파일이 아닙니다!");
		}
	</script>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>
		<div class="page-header page-header-none">
			<alert></alert>
			<h5>
				<big><big><i class="fa fa-rocket"></i> 공유해보세요!</big></big> 
				 <small><a href="/intro/code">아직 코드 공유 방법을 모르신다면 사용법을 읽어주세요!</a></small>
				<div style="margin-top: -10px" class="pull-right" title='전체 코드 갯수&#13;${codeCount}개'>

					<button class="btn btn-warning">
						<b><i class="fa fa-database"></i> ${codeCount}</b>
					</button>

				</div>
			</h5>
		</div>
		<div class="row">
			<div class="span12">
				<ul class="nav nav-tabs" id="myTab">
					<li id="age-desc"><a
						href="/code<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:age-desc/page:1">최신순</a></li>
					<c:if test="${massage == null }">
						<li id="download-desc"><a
							href="/code<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:download-desc/page:1">다운로드순</a></li>
					</c:if>
					<li id="repost-desc"><a
						href="/code<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:repost-desc/page:1">최신
							답변순</a></li>
					<li id="repost-many"><a
						href="/code<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:repost-many/page:1">많은
							답변순</a></li>
					<li id="age-asc"><a
						href="/code<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:age-asc/page:1">오래된순</a></li>
					<sec:authorize access="isAuthenticated()">
					<li id="my"><a
						href="/code<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:my/page:1">내가 올린 코드</a></li>
					</sec:authorize>
				</ul>
			</div>
			<div id="search-div" class="span10">
				<input id="post-search-input" class="title span10"
					placeholder="검색어를 입력하여 코드를 찾아보세요!" type="text" />
			</div>
			<form onsubmit="return checkCode()" id="codeForm" action="/code/add"
				enctype="multipart/form-data" method="post">

				<div id="post-div" class="span10">
					<input name="content" 
						id="code-content" class="span10" maxlength="50"
						placeholder="소스 코드에 대해 소개해주세요!" type="text" />
						<input name="url"
						id="code-url" class="span10" maxlength="50"
						placeholder="만일 다른곳에서 퍼오셨다면 원본 출처를 입력해주세요!" type="text" />
				</div>
				

				<div style="margin-left:5px; width:150px" class="span2">


					<span> 
					<sec:authorize access="isAuthenticated()">
					<a id="show-content-button" title='코드 게시하기'
						href="javascript:showCodeContent();"
						class="post-button btn btn-primary"> <i class="fa fa-pencil"></i>
					</a> 
					</sec:authorize>
					<sec:authorize access="isAnonymous()">
						<button disabled="disabled" title="로그인을 하셔야 코드를 업로드 할 수 있습니다!" class="post-button btn btn-primary">
							<i class="fa fa-times"></i>
						</button>
					</sec:authorize>
					
					<a id='search-button' title='코드 검색하기' class="post-button btn btn-primary"> <i class="fa fa-search"></i>
					</a> 
					
					<a id="hide-content-button" title='작성 취소하기'
						href="javascript:hideCodeContent();"
						class="post-button btn btn-primary"> <i class="fa fa-pencil"></i>
					</a>
						
						<button id='post-ok' title='코드 올리기' class="post-button btn btn-primary">
							<i class="fa fa-check"></i>
						</button>

					</span>
				</div>
				<div id="file-div" style="padding-left: 20px;">
					<div class='fileinput fileinput-new' data-provides='fileinput'>
						<div class='input-group'>
							<div class='form-control' data-trigger='fileinput'
								title='업로드할 파일을 선택하세요!'>
								<i class='icon-file '></i> <span class='fileinput-filename'></span>
							</div>
							<span class='input-group-addon btn btn-primary btn-file'><span
								class='fileinput-new'>ZIP파일 혹은 소스파일</span> <span
								class='fileinput-exists'><i
									class='icon-repeat icon-white'></i></span><input type='file' id='file'
								multiple='true' name='file'></span> <a href='#'
								class='input-group-addon btn btn-primary fileinput-exists'
								data-dismiss='fileinput'><i class='icon-remove icon-white'></i></a>
						</div>
					</div>
					<div class='fileinput fileinput-new' data-provides='fileinput'>
						<div class='input-group'>
							<div class='form-control' data-trigger='fileinput'
								title='업로드할 파일을 선택하세요!'>
								<i class='icon-file '></i> <span class='fileinput-filename'></span>
							</div>
							<span class='input-group-addon btn btn-primary btn-file'><span
								class='fileinput-new'><i
									class='fa fa-file-photo-o'></i> 결과 화면</span> <span
								class='fileinput-exists'><i
									class='icon-repeat icon-white'></i></span><input 
									onchange ='fileUploadChange(this);'
									type='file' id='output'
								multiple='true' name='output'></span> <a href='#'
								class='input-group-addon btn btn-primary fileinput-exists'
								data-dismiss='fileinput'><i class='icon-remove icon-white'></i></a>
						</div>
					</div>
				</div>
				<!--<div class="pull-right"><a class="btn btn-inverse">
					<i class="fa fa-pencil"></i> 코드 직접 입력하기</a></div>
				</div>
				  <div class="span12">
					<textarea name="content" id="code-content-textarea"
						class="code-content span12" 
						placeholder="여기에 글을 작성하시면 파일 배포시 자동으로 readme.md 파일이 생성됩니다. 만약 코드 소개에 충분히 설명하셨다면 이부분을 비워두셔도 상관없습니다!"></textarea>
					<div class="file-div"></div>

				</div>
				
				<div class="span12">
				<input id="post-search-input" class="title span6"
						placeholder="파일명을 입력해주세요. 예시 hello.java 또는 folder/hello.java" type="text" />

					<textarea name="content" id="code-content-textarea"
						class="code-content span12" 
						placeholder="소스 코드를 입력해주세요!"></textarea>
					<div class="file-div"></div>

				</div>

-->
			</form>

			<div class="span12">

				<table id="post-table" class="table table-hover">
					<tbody>
						<c:forEach items="${codes}" var="code" varStatus="status">
							<tr>
								<td class="td-post-writer-img" rowspan="2"><a href="/${code.writerName}"><img
									src="${code.getImgSrc()}"></a></td>
								<td colspan="2" class="post-top-title"><a
									class="a-post-title" href="/code/${code.codeID}"> <i
										class="fa fa-download"></i>&nbsp;${cov:htmlEscape(code.content)}
								</a></td>
								<td class="td-button" rowspan="2"><a
									href="/code/${code.codeID}/${cov:htmlEscape(code.name)}.zip"> <span
										class="span-button"> ${code.downCount}
											<p class="p-button">다운</p>
									</span>
								</a></td>
								<td class="td-button" rowspan="2"><a
									href="/code/${code.codeID}"> <span class="span-button">${code.rePostCount}
											<p class="p-button">답변</p>
									</span></a></td>
							</tr>
							<tr>
								<td class="post-bottom"><a href="/${code.writerName}"><b>${code.writerName}</b></a>
									${code.getFormatCreated()}</td>
								<td class="post-bottom-tag"><c:forEach items="${code.tags}"
										var="tag">
										<span class="tag-name">${tag}</span>
									</c:forEach></td>
							</tr>
							<tr><td style="padding-top: 20px; max-width: 480px;" class="none-top-border"colspan="5">
							<a href="/code/${code.codeID}">
							<pre id="code-${status.count}">${cov:htmlEscape(code.getFirstCode())}</pre>
							</a>
							 </td>
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
