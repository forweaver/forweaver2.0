<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>${repository.name} ~ ${repository.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<link rel="stylesheet" type="text/css" href="/resources/forweaver/css/bootstrap-markdown.min.css"/>
<script src="/resources/forweaver/js/markdown/markdown.js"></script>
<script src="/resources/forweaver/js/markdown/bootstrap-markdown.js"></script>
<script src="/resources/forweaver/js/markdown/to-markdown.js"></script>
</head>
<body>
<script>
var communityOn = false;
var editorMode = false;
var fileCount = 1;
var fileArray = [];
var fileHash = {};
function checkPost(){
	for(var i=0;i<fileCount;i++){
		var fileName = $("#file"+(i+1)).val();
		fileName = replaceAll(fileName,"?","_");
		fileName = replaceAll(fileName,"#","_");
		fileName = replaceAll(fileName," ","_");
		if(fileName.indexOf("C:\\fakepath\\") != -1)
			fileName = fileName.substring(12);
		
		fileArray[i] = fileName;
	}
	
	if($('#post-title-input').val().length <5){
		alert("최소 5자 이상 입력해주세요!");
		return false;
	}else if($('#post-title-input').val().length > 200){
		alert("최대 200까지만 입력해주세요!");
		return false;
	}else{
		$("form:first").append($("#post-title-input"));
		$("form:first").append($("input[name='tags']"));
		return true;
	}
}

	function showPostContent() {
		$('#page-pagination').hide();
		$('#post-table').hide();
		$('#post-content-textarea').fadeIn('slow');

		$('#show-content-button').hide();
		$('#hide-content-button').show();
		$('.md-editor').fadeIn('slow');
		$('.file-div').fadeIn('slow');
		editorMode = true;
	}

	function hidePostContent() {
		$('#page-pagination').show();
		$('#post-table').show();
		$('#post-content-textarea').hide();

		$('#show-content-button').show();
		$('#hide-content-button').hide();
		$('.md-editor').hide();
		$('.file-div').hide();
		editorMode = false;
	}
		$(document).ready(function() {
			hidePostContent();
		$( "#"+getSort(document.location.href) ).addClass( "active" );
		
		$(".file-div").append("<div class='fileinput fileinput-new' data-provides='fileinput'>"+
				  "<div class='input-group'>"+
				    "<div class='form-control' data-trigger='fileinput' title='업로드할 파일을 선택하세요!'><i class='icon-file '></i> <span class='fileinput-filename'></span></div>"+
				    "<span class='input-group-addon btn btn-primary btn-file'><span class='fileinput-new'>"+
				    "<i class='fa fa-arrow-circle-o-up icon-white'></i></span><span class='fileinput-exists'><i class='icon-repeat icon-white'></i></span>"+
					"<input onchange ='fileUploadChange(this,\"#post-content-textarea\");' type='file' id='file1' multiple='true' name='files[0]'></span>"+
				   "<a href='#' class='input-group-addon btn btn-primary fileinput-exists' data-dismiss='fileinput'><i class='icon-remove icon-white'></i></a>"+
				  "</div>"+
				"</div>");
			
			$('#showCommunity').click(function() {
				if(communityOn){
					communityOn = false;
					$('#communityTab').hide();
					$('#myTab').show();
				}else{
					communityOn = true;
					$('#myTab').hide();
					$('#communityTab').show();
				}
			});
			
			
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
			
			var pageCount = ${postCount+1}/10;
			if(pageCount < 1 ) 
				pageCount = 1;
			
			var options = {
		            currentPage: ${pageIndex},
		            totalPages: pageCount,
		            pageUrl: function(type, page, current){

		                return "${pageUrl}"+page;

		            }
		        }
			$("#post-content-textarea").focus(function(){	
				if($("#post-content-textarea").val().length >= 0)
					$("#post-content-textarea").css('height','380px');
			});
			
			$("#post-content-textarea").focusout(function(){	
				if($("#post-content-textarea").val().length == 0)
					$("#post-content-textarea").css('height','auto');
			});

		        $('#page-pagination').bootstrapPaginator(options);$('a').attr('rel', 'external');
		});
		
		
		function fileUploadChange(fileUploader){
			var fileName = $(fileUploader).val();			
			fileName = replaceAll(fileName,"?","_");
			fileName = replaceAll(fileName,"#","_");
			fileName = replaceAll(fileName," ","_");
			$(function (){
			
			if(fileName !=""){ // 파일을 업로드하거나 수정함
				if(fileName.indexOf("C:\\fakepath\\") != -1)
					fileName = fileName.substring(12);
				fileHash[fileName] = mongoObjectId();
				$.ajax({
				    url: '/data/tmp',
	                type: "POST",
	                contentType: false,
	                processData: false,
	                data: function() {
	                    var data = new FormData();
	                    data.append("objectID", fileHash[fileName]);
	                    data.append("file", fileUploader.files[0]);
	                    return data;
	                }()
				});	
				if(isImage(fileName))
				$("#post-content-textarea").val($("#post-content-textarea").val()+'\n!['+fileName+'](/data/'+fileHash[fileName]+'/'+fileName+')');
			
				if(fileUploader.id == "file"+fileCount){ // 업로더의 마지막 부분을 수정함
			fileCount++;
			$(".file-div").append("<div class='fileinput fileinput-new' data-provides='fileinput'>"+
					  "<div class='input-group'>"+
					    "<div class='form-control' data-trigger='fileinput' title='업로드할 파일을 선택하세요!'><i class='icon-file '></i> <span class='fileinput-filename'></span></div>"+
					    "<span class='input-group-addon btn btn-primary btn-file'><span class='fileinput-new'>"+
					    "<i class='fa fa-arrow-circle-o-up icon-white'></i></span><span class='fileinput-exists'><i class='icon-repeat icon-white'></i></span>"+
						"<input onchange ='fileUploadChange(this);' type='file' multiple='true' id='file"+fileCount+"' name='files["+(fileCount-1)+"]'></span>"+
					   "<a id='remove-file' href='#' class='input-group-addon btn btn-primary fileinput-exists' data-dismiss='fileinput'><i class='icon-remove icon-white'></i></a>"+
					  "</div>"+
					"</div>");
				}
			}else{
				if(fileUploader.id == "file"+(fileCount-1)){ // 업로더의 마지막 부분을 수정함
					
				$("#file"+fileCount).parent().parent().remove();

					--fileCount;
			}}});
		}
		
	</script>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>

		<div class="page-header page-header-none">
			<h5>
								<big><big><i class="fa fa-bookmark"></i> ${repository.name}</big></big>
<small>${repository.description}</small>
				<div style="margin-top:-10px" class="pull-right">

				<button class="btn btn-warning">
								<b><i class="fa fa-database"></i> ${postCount}</b>
				</button>

				</div>
			</h5>
		</div>
		<div class="row">
		
		<div class="span7">
				<ul id="myTab" class="nav nav-tabs">
					<li><a href="/repository/${repository.name}/">브라우져</a></li>
					<li><a href="/repository/${repository.name}/log">로그</a></li>
					<li class="active"><a href="/repository/${repository.name}/community">커뮤니티</a></li>
					
					<li><a href="/repository/${repository.name}/weaver">사용자</a></li>
					<sec:authorize ifAnyGranted="ROLE_USER, ROLE_ADMIN">
					<c:if test="${repository.getCreator().equals(currentUser) }">
					<li><a href="/repository/${repository.name}/edit">관리</a></li>
					</c:if>
					</sec:authorize>
					<li><a href="/repository/${repository.name}/info">정보</a></li>
					
					
				</ul>
				
				<ul style="display:none;" class="nav nav-tabs" id="communityTab">
					<li id = "age-desc"><a href="/repository/${repository.name}/community<c:if test="${tagNames != null }">/tags:${tagNames}</c:if>/<c:if test="${search != null }">/search:${search}</c:if>/sort:age-desc/page:1">최신순</a></li>
					<li id = "push-desc"><a href="/repository/${repository.name}/community<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:push-desc/page:1">추천순</a></li>
					<li id = "repost-desc"><a href="/repository/${repository.name}/community<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:repost-desc/page:1">최신 답변순</a></li>
					<li id = "repost-many"><a href="/repository/${repository.name}/community<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:repost-many/page:1">많은 답변순</a></li>
					<li id = "age-asc"><a href="/repository/${repository.name}/community<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:age-asc/page:1">오래된순</a></li>
					<li id = "repost-null"><a href="/repository/${repository.name}/community<c:if test="${tagNames != null }">/tags:${tagNames}</c:if><c:if test="${search != null }">/search:${search}</c:if>/sort:repost-null/page:1">답변 없는 글</a></li>
				</ul>
				
			</div>
			<div class="span1">	
				<a id='showCommunity' title="커뮤니티 소식을 정렬하려면 누르세요!"
						class="post-button btn btn-inverse"> <i class="fa fa-refresh"></i>
					</a>
			</div>
			<div class="span4">
				<div class="input-block-level input-prepend" title="http 주소로 저장소를 복제할 수 있습니다!&#13;복사하려면 ctrl+c 키를 누르세요.">
					<span class="add-on"><i class="fa fa-git"></i></span> <input
						value="http://${pageContext.request.serverName}/g/${repository.name}.git" type="text"
						class="input-block-level">
				</div>
			</div>
			<div class="span10">
					<input maxlength="200"  id="post-title-input" class="title span10" name="title"
						placeholder="찾고 싶은 검색어나 쓰고 싶은 단문의 내용을 입력해주세요! (최대 200자 입력)" type="text"
						value="" />
				</div>
			
			<form id="postForm" onkeypress="return event.keyCode != 13;" onsubmit="return checkPost()"
				action="/repository/${repository.name}/community/add" enctype="multipart/form-data" METHOD="POST">
				
				<div class="span2">
					<span> 
					<sec:authorize access="isAnonymous()">
					<button disabled="disabled" title="로그인을 하셔야 글을 쓸 수 있습니다!"
						class="post-button btn btn-primary"> <i class="fa fa-pencil"></i>
					</button> 
						<button disabled="disabled" title="로그인을 하셔야 글을 쓸 수 있습니다!" class="post-button btn btn-primary">
							<i class="fa fa-times"></i>
						</button>
					</sec:authorize>
					<sec:authorize access="isAuthenticated()">
					
						<a id="show-content-button" href="javascript:showPostContent();" title="글 내용 작성하기"
						class="post-button btn btn-primary"> <i class="fa fa-pencil"></i>
					</a> <a style="display: none;" id="hide-content-button" title="작성 취소하기"
						href="javascript:hidePostContent();"
						class="post-button btn btn-primary"> <i class="fa fa-pencil"></i>
					</a>
						<button  id='post-ok' title="글 올리기" class="post-button btn btn-primary">
							<i class="fa fa-check"></i>
						</button>
					</sec:authorize>

					</span>
				</div>
				<div class="span12">
					<textarea data-provide="markdown"  name = content style="display: none;" id="post-content-textarea"
						class="post-content span12" 
						placeholder="글 내용을 입력해주세요!(직접적인 html 대신 마크다운 표기법 사용가능)"></textarea>
						<div class="file-div"></div>
				</div>
				
				</form>
				<div class="span12">
				
					<table id="post-table" class="table table-hover">
						<tbody>
							<c:forEach items="${posts}" var="post">
							<tr>
								<td class="td-post-writer-img" rowspan="2">
										<img src="${post.getImgSrc()}">
								</td>
								<td class="post-top-title-short"><a class="a-post-title"
									href="/community/${post.postID}"> <c:if
											test="${post.isLong()}">
											<i class=" icon-align-justify"></i>
										</c:if> <c:if test="${!post.isLong()}">
											<i class=" icon-comment"></i>
										</c:if> &nbsp;<c:if test="${!post.isNotice()}">${cov:htmlEscape(post.title)}</c:if>
										<c:if test="${post.isNotice()}">${post.title}</c:if>
								</a></td>
								<td class="td-button" rowspan="2">
										<span class = "span-button">${post.push}
										<p class="p-button">추천</p></span>
								</td>
								<td class="td-button" rowspan="2">
										<span class = "span-button">${post.rePostCount}
										<p class="p-button">답변</p></span>
								</td>				
							</tr>
							<tr>
								<td class="post-bottom"><b>${post.writerName}</b>
									${post.getFormatCreated()}
									&nbsp;&nbsp;
									<c:forEach items="${post.tags}" var="tag">
										<c:if test="${!fn:startsWith(tag, '@')}">
											<span class = "tag-name">${tag}</span>
										</c:if>
									</c:forEach>
								</td>
							</tr>
							</c:forEach>
						</tbody>
					</table>
					<div class = "text-center">
					<div id="page-pagination"></div>
				</div>
				</div>
			</div>

		<!-- .row-fluid -->
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>
</html>
