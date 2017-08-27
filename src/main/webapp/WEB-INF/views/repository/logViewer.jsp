<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>${repository.name}~${repository.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<%@ include file="/WEB-INF/includes/syntaxhighlighterSrc.jsp"%>
</head>
<body>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>
<script>
var fileCount = 1;
var comment = 0;
var fileHash = {};
function fileUploadChange(fileUploader){
	var fileName = $(fileUploader).val();			
	$(function (){
	if(fileName !="" || !blank_pattern.test(fileName)  || fileName.length < 70){ // 파일을 업로드하거나 수정함
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
		$("#repost-content").val($("#repost-content").val()+'\n!['+fileName+'](/data/'+fileHash[fileName]+')');
	
		if(fileUploader.id == "file"+fileCount){ // 업로더의 마지막 부분을 수정함
	fileCount++;
	$(".file-div").append("<div class='fileinput fileinput-new' data-provides='fileinput'>"+
			  "<div class='input-group'>"+
			    "<div class='form-control' data-trigger='fileinput'><i class='icon-file '></i> <span class='fileinput-filename'></span></div>"+
			    "<span class='input-group-addon btn btn-primary btn-file'><span class='fileinput-new'>"+
			    "<i class='fa fa-arrow-circle-o-up icon-white'></i></span><span class='fileinput-exists'><i class='fa fa-repeat '></i></span>"+
				"<input onchange ='fileUploadChange(this);' type='file' multiple='true' id='file"+fileCount+"' name='files["+(fileCount-1)+"]'></span>"+
			   "<a id='remove-file' href='#' class='input-group-addon btn btn-primary fileinput-exists' data-dismiss='fileinput'><i class='fa fa-remove'></i></a>"+
			  "</div>"+
			"</div>");
		}
	}else{
		if(fileUploader.id == "file"+(fileCount-1)){ // 업로더의 마지막 부분을 수정함
			
		$("#file"+fileCount).parent().parent().remove();

			--fileCount;
	}}});
}
	function showCommentAdd(rePostID){
		$("#repost-form").hide();
		$(".comment-form").remove();
		if(comment != rePostID){
		$("#comment-form-td-"+rePostID).append("<form class='comment-form' action='/repository/${repository.name}/log-viewer/log:${gitLog.logID}/"+rePostID+"/add-reply' method='POST'>"+
		"<div style='padding-left:20px;' class='span10'>"+
		"<input id='reply-input'  type ='text' name='content' class='reply-input span10'  placeholder='답변할 내용을 입력해주세요!'></input></div>"+
		"<div class='span1'><span><button type='submit' class='post-button btn btn-primary'>"+
		"<i class='icon-ok icon-white'></i></button></span></div></form>");
		comment = rePostID;
		$("#reply-input").focus();
		
		}else{
			$("#repost-form").show();
			comment = 0;
		}
	}

	
	$(function() {
		
		$("#repost-content").focus(function(){				
				$(".file-div").fadeIn();
				$("#repost-table").hide();
				if($("#repost-content").val().length == 0)
					$("#repost-content").css('height','330px');
		});
		
		$("#repost-content").focusout(function(){	

			if( !this.value ) {
				$(".file-div").hide();
				$("#repost-table").fadeIn();
	      }
	});
		
		$(".file-div").append("<div class='fileinput fileinput-new' data-provides='fileinput'>"+
				  "<div class='input-group'>"+
				    "<div class='form-control' data-trigger='fileinput'><i class='icon-file '></i> <span class='fileinput-filename'></span></div>"+
				    "<span class='input-group-addon btn btn-primary btn-file'><span class='fileinput-new'>"+
				    "<i class='fa fa-arrow-circle-o-up icon-white'></i></span><span class='fileinput-exists'><i class='fa fa-repeat '></i></span>"+
					"<input onchange ='fileUploadChange(this);' type='file' id='file1' multiple='true' name='files[0]'></span>"+
				   "<a href='#' class='input-group-addon btn btn-primary fileinput-exists' data-dismiss='fileinput'><i class='fa fa-remove'></i></a>"+
				  "</div>"+
				"</div>");
		
		$(".file-div").hide();
	});
	
		SyntaxHighlighter.all();
		move = false;
			<c:forEach items='${repository.tags}' var='tag'>
			$('#tags-input').tagsinput('add',"${tag}");
			</c:forEach>
			move = true;

		
	</script>
		<div class="page-header page-header-none">
			<h5>
				<big><big><i class="fa fa-bookmark"></i> ${repository.name}</big></big>
<small>${repository.description}</small>
			</h5>
		</div>
		<div class="row">
			
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
			<!-- 커밋 테이블 -->
			<div class="span12">
				<table class="table table-hover">
					<tbody>
						<tr>
							<td class="none-top-border td-post-writer-img" rowspan="2"><img
								src="${gitLog.getImgSrc()}">
							</td>
							<td style="width: 710px;"
								class="none-top-border post-top-title-short">${fn:substring(gitLog.shortMassage,0,50)}</td>
								
							<td class="none-top-border" rowspan="2">
									<a	href="/repository/${repository.name}/browser/log:${fn:substring(gitLog.logID,0,8)}">
										<span class="span-button"> <i class="fa fa-eye"></i>
											<p class="p-button">전체</p></span>
									</a>
								<a	href="/repository/${repository.name}/${fn:substring(gitLog.logID,0,8)}/${repository.getRepoName()}-${fn:substring(gitLog.logID,0,8)}.zip">
										<span class="span-button"> <i class="fa fa-arrow-circle-o-down"></i>
											<p class="p-button">다운</p></span>
									</a>									
							</td>
						</tr>
						<tr>
							<td class="post-bottom"><b>${gitLog.commiterName}</b>
								${gitLog.getCommitDate()} &nbsp;&nbsp; <span
								style="cursor: text;" class="tag-commit tag-name">${gitLog.logID}</span>
							</td>

						</tr>

						<tr>
							<td style="border-top: 0px"></td>
							<td style="font-size:13px;" colspan="3">${cov:htmlEscape(gitLog.fullMassage)}</td>
						</tr>
						<c:if test="${gitLog.getNote().length() > 0}">
						<tr>
							<td style="border-top: 0px"></td>
							<td style="font-size:13px;" colspan="3">
							 <span class="label label-warning"><i class="fa fa-book"></i> 노트:</span> 
    						${cov:htmlEscape(gitLog.getNote())}</td>
						</tr>
						</c:if>
					</tbody>
				</table>
				<c:if test="${fn:length(gitLog.diff)>0}">
				<div style="padding-top:30px;" class="well-white">
					<pre id="source-code" class="span9 brush: diff">${cov:htmlEscape(gitLog.diff)}</pre>
				</div>
				</c:if>
				<!-- 답변 작성란 
				<form enctype="multipart/form-data" id="repost-form"
					action="/repository/${repository.name}/log-viewer/log:${gitLog.logID}/add-repost" method="POST">

					<div style="margin-left: 0px; margin-bottom:10px" class="span11">
						<textarea name="content" id="repost-content"
							class="post-content span10" 
							placeholder="답변할 내용을 입력해주세요!(최소 5자 이상 직접적인 html 대신 마크다운 표기법 사용가능)"></textarea>
					</div>
					<div class="span1">
						<span>
							<button type="submit" class="post-button btn btn-primary">
								<i class="fa fa-check"></i>
							</button>
						</span>
					</div>
					<div class="file-div"></div>
				</form>
			</div>
			
			<table id="repost-table" class="table table-hover">
					<tbody>
						<c:forEach items="${rePosts}" var="rePost">
							<tr>
								<td class=" td-post-writer-img "><img
									src="${rePost.getImgSrc()}"></td>

								<td class="font-middle"><a href="/${rePost.writerName}"><b>${rePost.writerName}</b></a>
									${rePost.getFormatCreated()}</td>
								<td class="function-div font-middle">
									<div class="pull-right">
										<a onClick='javascript:showCommentAdd(${rePost.rePostID})'><span
											class="function-button function-comment">댓글달기</span></a>
										<a onclick="return confirm('정말로 삭제하시겠습니까?');"
											href='/repository/${repository.name}/log-viewer/log:${gitLog.logID}/${rePost.rePostID}/delete'>
											<span class="function-button">삭제</span>
										</a>
									</div>
								</td>
								<td class="td-button"><span class="span-button">${rePost.push}
										<p class="p-button">추천</p>
								</span></td>
								<td class="td-button"><span class="span-button">${rePost.replys.size()}
										<p class="p-button">댓글</p>
								</span></td>
							</tr>
							<c:if test="${rePost.datas.size() > 0}">
								<tr>
									<td colspan="5"><c:forEach var="index" begin="0"
											end="${rePost.datas.size()-1}">
											<a href='/data/${rePost.datas.get(index).getId()}'><span
												class="function-button function-file"><i
													class='fa fa-file'></i>
													${rePost.datas.get(index).getName()}</span></a>
										</c:forEach></td>
								</tr>
							</c:if>
							<tr>
								<td class="none-top-border post-content-max" colspan="5">
								<s:eval expression="T(com.forweaver.util.WebUtil).markDownEncoder(rePost.getContent())" /></td>
							</tr>

							<tr>
								<td id="comment-form-td-${rePost.rePostID}"
									class="none-top-border" colspan="5"></td>

							</tr>
							<c:forEach items="${rePost.replys}" var="reply">
								<tr>
									<td class="none-top-border"></td>
									<td class="reply dot-top-border" colspan="4"><b>${reply.number}.</b>
										${reply.content} - <a href="/${reply.writerName}"><b>${reply.writerName}</b></a>
										${reply.getFormatCreated()}
										<div class="function-div pull-right">
											<a onclick="return confirm('정말로 삭제하시겠습니까?');"
											href="/repository/${repository.name}/log-viewer/log:${gitLog.logID}/${rePost.rePostID}/${reply.number}/delete">
												<i class='icon-remove'></i>
											</a>
										</div></td>
								</tr>
							</c:forEach>
						</c:forEach>

					</tbody>
				</table>-->
		</div>
		<!-- .row-fluid -->
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>
</html>
