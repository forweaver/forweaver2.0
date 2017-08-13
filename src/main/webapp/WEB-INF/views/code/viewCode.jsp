<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>Forweaver! : ${code.name}</title>
<meta property="og:image" content="/resources/forweaver/img/previewCode.png" />
<meta property="og:title" content="${code.name}" />
<meta property="og:description" content="${code.content}" />


<%@ include file="/WEB-INF/includes/src.jsp"%>
<%@ include file="/WEB-INF/includes/syntaxhighlighterSrc.jsp"%>
<link rel="stylesheet" type="text/css" href="/resources/forweaver/css/bootstrap-markdown.min.css"/>
<script src="/resources/forweaver/js/markdown/markdown.js"></script>
<script src="/resources/forweaver/js/markdown/bootstrap-markdown.js"></script>
<script src="/resources/forweaver/js/markdown/to-markdown.js"></script>
</head>
<body>
	<script type="text/javascript">
	var fileCount = 1;
	var comment = 0;
	var fileHash = {};
	function hideAndShowSourceCode(number){
		$(function (){
			if($("#td-code-"+number).is(":visible")){
				$("#td-code-"+number).fadeOut();
			}else{
				$("#td-code-"+number).fadeIn();
			}
		});
	}
	
		function showCommentAdd(rePostID){
			$("#repost-form").hide();
			$(".comment-form").remove();
			if(comment != rePostID){
			$("#comment-form-td-"+rePostID).append("<form class='comment-form' action='/code/${code.codeID}/"+rePostID+"/add-reply' method='POST'>"+
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
			
			$( "#"+getSort(document.location.href) ).addClass( "active" );
			
			$("#repost-content").focus(function(){				
					$(".file-div").fadeIn();
					$("#repost-table").hide();
					$("#myTab").hide();
					if($("#repost-content").val().length == 0)
						$("#repost-content").css('height','330px');
			});
			
			$("#repost-content").focusout(function(){	

				if( !this.value ) {

					$("#repost-table").fadeIn();
					$("#myTab").fadeIn();
		      }
				
		});
			
			$(".file-div").append("<div class='fileinput fileinput-new' data-provides='fileinput'>"+
					  "<div class='input-group'>"+
					    "<div class='form-control' data-trigger='fileinput' title='업로드할 파일을 선택하세요!'><i class='icon-file '></i> <span class='fileinput-filename'></span></div>"+
					    "<span class='input-group-addon btn btn-primary btn-file'><span class='fileinput-new'>"+
					    "<i class='fa fa-arrow-circle-o-up icon-white'></i></span><span class='fileinput-exists'><i class='icon-repeat icon-white'></i></span>"+
						"<input onchange ='fileUploadChange(this,\"#repost-content\");' type='file' id='file1' multiple='true' name='files[0]'></span>"+
					   "<a href='#' class='input-group-addon btn btn-primary fileinput-exists' data-dismiss='fileinput'><i class='icon-remove icon-white'></i></a>"+
					  "</div>"+
					"</div>");
			
			$(".file-div").hide();
			
			move = false;
			<c:forEach items='${code.tags}' var='tag'>
			$('#tags-input').tagsinput('add',"${tag}");
			</c:forEach>
			move = true;

			
			<c:forEach	items="${code.codes}" var="simpleCode" varStatus="status">	
			$("#code-${status.count}").addClass("brush: "+extensionSeach('${simpleCode.fileName}')+";");
			</c:forEach>
		});
		
		SyntaxHighlighter.all();
	</script>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>
		<div class="row">
			<div class=" span12">
				<table id="post-table" class="table table-hover">
					<tbody>
						<tr>
							<td class="td-post-writer-img none-top-border" rowspan="2">
								<img src="${code.getImgSrc()}">
							</td>
							<td colspan="2" class="post-top-title none-top-border"><a
								rel="external" class="a-post-title"
								href="/code/tags:<c:forEach items='${code.tags}' var='tag'>${tag},</c:forEach>">
									<i class="fa fa-download"></i>&nbsp;${cov:htmlEscape(code.content)}
							</a></td>
							<c:if test='${code.url != null && code.url !=""}'>
							<td class="td-button none-top-border" rowspan="2"><a
								href="${code.url}"> <span
									class="span-button"> <i class=" fa fa-external-link-square"></i>
										<p class="p-button">출처</p>
								</span></a></td>
							</c:if>	
							<td class="td-button none-top-border" rowspan="2"><a
								href="/code/${code.codeID}/${cov:htmlEscape(code.name)}.zip"> <span
									class="span-button"> ${code.downCount}
										<p class="p-button">다운</p>
								</span></a></td>
							<td class="td-button none-top-border" rowspan="2"><span
								class="span-button">${rePosts.size()}
									<p class="p-button">답변</p>
							</span></td>
						</tr>
						<tr>
							<td class="post-bottom"><b>${code.writerName}</b>
								${code.getFormatCreated()}</td>
							<td class="post-bottom-tag"><c:forEach items="${code.tags}"
									var="tag">
									<span
										class="tag-name
										<c:if test="${tag.startsWith('@')}">
										tag-private
										</c:if>
										<c:if test="${tag.startsWith('$')}">
										tag-massage
										</c:if>
										">${tag}</span>
								</c:forEach>
								<c:if test="${code.writerName==currentUser}">	
								<div class="function-div pull-right">
									<a onclick="return confirm('정말로 삭제하시겠습니까?');"
										href="/code/${code.codeID}/delete"> <span
										class="function-button">삭제</span></a>
								</div>
								</c:if>
								</td>

						</tr>
						<c:forEach items="${code.codes}" var="simpleCode" varStatus="status">
							<tr>
								<td colspan="6"><span
									onclick="javascript:hideAndShowSourceCode(${status.count})"
									class="function-button function-file"> <i
										class='icon-file icon-white'></i> ${simpleCode.fileName}
								</span>
								<a href="${simpleCode.fileName}">
									<span class="function-button" title='파일 다운로드'> <i class='icon-file icon-white'></i> 다운로드
									</span>
								</a>
								</td>
							</tr>
							
							<tr>
								<td id="td-code-${status.count}" class="well-white " style="<c:if test="${status.count > 5 && !status.last}" >display:none;</c:if> padding-top: 20px; max-width: 480px;"
									colspan="6">
									
									<c:if test="${!simpleCode.fileName.endsWith('.md')}">
									
										<c:if test="${!simpleCode.isImgFile()}">
											<pre id="code-${status.count}">${cov:htmlEscape(simpleCode.getContent())}</pre>
										</c:if>
										<c:if test="${simpleCode.isImgFile()}">
											<img src="/code/${code.codeID}/${simpleCode.fileName}">
										</c:if>
										
									</c:if>
									
									<c:if test="${simpleCode.fileName.endsWith('.md')}">
										<div id="code-${status.count}"><s:eval expression="T(com.forweaver.util.WebUtil).markDownEncoder(simpleCode.getContent())" /></div>
									</c:if>
									</td>
							</tr>
							
							
							
						</c:forEach>
					</tbody>
				</table>


				<!-- 답변에 관련된 테이블 시작-->
				<sec:authorize access="isAuthenticated()">
				<form enctype="multipart/form-data" id="repost-form"
					action="/code/${code.codeID}/add-repost" method="POST">

					<div style="margin-left: 0px; margin-bottom:10px" class="span11">
						<textarea data-provide="markdown" name="content" id="repost-content"
							class="post-content span10" 
							placeholder="답변할 내용을 입력해주세요!(직접적인 html 대신 마크다운 표기법 사용가능)"></textarea>
					</div>
					<div class="span1">
						<span>
						<button type="submit" class="post-button btn btn-primary" title='답변 작성하기'>
								<i class="fa fa-check"></i>
							</button>
						</span>
					</div>
					<div class="file-div"></div>
				</form>
				</sec:authorize>
				
				<c:if test="${code.rePostCount != 0}">

					<div class="span12"></div>
					<ul class="nav nav-tabs" id="myTab">
						<li id="age-desc"><a
							href="/code/${code.codeID}/sort:age-desc">최신순</a></li>
						<li id="push-desc"><a
							href="/code/${code.codeID}/sort:push-desc">추천순</a></li>
						<li id="reply-desc"><a
							href="/code/${code.codeID}/sort:reply-desc">최신 댓글순</a></li>
						<li id="reply-many"><a
							href="/code/${code.codeID}/sort:reply-many">많은 댓글순</a></li>
						<li id="age-asc"><a href="/code/${code.codeID}/sort:age-asc">오래된순</a></li>
					</ul>

				</c:if>
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
									<sec:authorize access="isAuthenticated()">
										<a onClick='javascript:showCommentAdd(${rePost.rePostID})'><span
											class="function-button function-comment">댓글달기</span></a>
									</sec:authorize>	
									<c:if test="${rePost.writerName==currentUser}">
										<a onclick="return confirm('정말로 답변을 수정하시겠습니까?');" href='/code/${code.codeID}/${rePost.rePostID}/update#repost-content'>
											<span class="function-button">수정</span>
										</a>
										<a onclick="return confirm('정말로 삭제하시겠습니까?');"
											href='/code/${code.codeID}/${rePost.rePostID}/delete'> <span
											class="function-button">삭제</span>
										</a>
									</c:if>
									</div>
								</td>
								<td class="td-button">
								<a onclick="return confirm('정말로 추천하시겠습니까?');" href="/code/${code.codeID}/${rePost.rePostID}/push">
								<span class="span-button">${rePost.push}
										<p class="p-button">추천</p>
								</span>
								</a>
								</td>
								<td class="td-button"><span class="span-button">${rePost.replys.size()}
										<p class="p-button">댓글</p>
								</span></td>
							</tr>
							<c:if test="${rePost.datas.size() > 0}">
								<tr>
									<td class ="none-top-border"colspan="5"><c:forEach var="index" begin="0"
											end="${rePost.datas.size()-1}">
											<a href='/data/${rePost.datas.get(index).getId()}'><span
												class="function-button function-file"><i
													class='icon-file icon-white'></i>
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
										<c:if test="${reply.writerName==currentUser}">
										<div class="function-div pull-right">
											<a
												href="javascript:deleteReply(${post.postID},${rePost.rePostID},${reply.number})">
												<i class='icon-remove'></i>
											</a>
										</div></c:if></td>
								</tr>
							</c:forEach>
						</c:forEach>

					</tbody>
				</table>
				<!-- 답변에 관련된 테이블 끝-->

			</div>
		</div>
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>


</html>
