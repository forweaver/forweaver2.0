<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>Forweaver : 소통해보세요!</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<link rel="stylesheet" type="text/css" href="/resources/forweaver/css/bootstrap-markdown.min.css"/>
<script src="/resources/forweaver/js/markdown/markdown.js"></script>
<script src="/resources/forweaver/js/markdown/bootstrap-markdown.js"></script>
<script src="/resources/forweaver/js/markdown/to-markdown.js"></script>
</head>
<body>
	<script type="text/javascript">
	var fileCount = 1;
	var fileArray = [];
	var fileHash = {};
	
	function removeFile(id,name){
		if(confirm("정말로 "+name+"을 삭제하시겠습니까?")){
			$("#file-"+id).remove();
			$("#remove").val($("#remove").val()+"@"+id);
		}
	}
	
		$(function() {
			move = false;
			<c:forEach items='${post.tags}' var='tag'>
			$('#tags-input').tagsinput('add',"${tag}");
			</c:forEach>
			move = true;
			
			<c:forEach items="${rePost.datas}" var="data">
			$(".file-div").append("<div id='file-${data.id}' class='fileinput fileinput-exists'><div class='input-group'>"+
					"<div class='form-control'><i class='icon-file '></i> <span class='fileinput-filename'>"+
					"${data.name}"+
					"</span></div><a href='#' onclick='javascript:removeFile(\"${data.id}\",\"${data.name}\")' class='input-group-addon btn btn-primary fileinput-exists'>"+
					"<i class='icon-remove icon-white'></i></a></div></div>");
			</c:forEach>
			
			$(".file-div").append("<div class='fileinput fileinput-new' data-provides='fileinput'>"+
					  "<div class='input-group'>"+
					    "<div class='form-control' data-trigger='fileinput' title='업로드할 파일을 선택하세요!'><i class='icon-file '></i> <span class='fileinput-filename'></span></div>"+
					    "<span class='input-group-addon btn btn-primary btn-file'><span class='fileinput-new'>"+
					    "<i class='fa fa-arrow-circle-o-up icon-white'></i></span><span class='fileinput-exists'><i class='icon-repeat icon-white'></i></span>"+
						"<input onchange ='fileUploadChange(this,\"#repost-content\");' type='file' id='file1' multiple='true' name='files[0]'></span>"+
					   "<a href='#' class='input-group-addon btn btn-primary fileinput-exists' data-dismiss='fileinput'><i class='icon-remove icon-white'></i></a>"+
					  "</div>"+
					"</div>");
			
				});
	</script>
	<div class="container">
		<%@ include file="/WEB-INF/common/nav.jsp"%>
		<div class="row">
			<div class=" span12">
				<table id="post-table" class="table table-hover">
					<tbody>
						<tr>
							<td class="td-post-writer-img none-top-border" rowspan="2">
								<a href="/${post.writerName}"><img src="${post.getImgSrc()}"></a>
							</td>
							<td colspan="2" class="post-top-title none-top-border">
									<c:if test="${!post.isNotice()}">
									<s:eval expression="T(com.forweaver.util.WebUtil).addLink(post.title)" /></td>
									</c:if>
									<c:if test="${post.isNotice()}">
									${post.title}
									</c:if></td>
							<td class="td-button none-top-border" rowspan="2">
							<a onclick="return confirm('정말로 추천하시겠습니까?');" href="/community/${post.postID}/push">
							<span class="span-button">${post.push}
										<p class="p-button">추천</p>
								</span></a></td>
							<td class="td-button none-top-border" rowspan="2"><span
								class="span-button">${post.rePostCount}
									<p class="p-button">답변</p>
							</span></td>
						</tr>
						<tr>
							<td class="post-bottom"><a href="/${post.writerName}"> <b>${post.writerName}</b></a>
								${post.getFormatCreated()}</td>
							<td class="post-bottom-tag"><c:forEach items="${post.tags}"
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
								</td>

						</tr>
						<c:if test="${post.datas.size() > 0}">
							<tr>
								<td class ="none-top-border" colspan="5"><c:forEach var="index" begin="0"
										end="${post.datas.size()-1}">
										<a href='/data/${post.datas.get(index).getId()}/${post.datas.get(index).getName()}'><span
											class="function-button function-file" title='파일 다운로드'><i
												class='icon-file icon-white'></i>
												${post.datas.get(index).getName()}</span></a>
									</c:forEach></td>
							</tr>
						</c:if>
						<!-- 글내용 시작 -->
						<c:if test="${post.isLong()}">
							<tr>
								<td class="post-content post-content-max"colspan="5">
								<s:eval expression="T(com.forweaver.util.WebUtil).markDownEncoder(post.getContent())" /></td>
							</tr>
						</c:if>
						<!-- 글내용 끝 -->
					</tbody>
				</table>


				<!-- 답변에 관련된 테이블 시작-->

				<form enctype="multipart/form-data" id="repost-form"
					action="/community/${post.postID}/${rePost.rePostID}/update" method="POST">

					<div style="margin-left: 0px; margin-bottom:10px" class="span11">
						<textarea data-provide="markdown"  name="content" id="repost-content"
							class="post-content span10" 
							placeholder="답변할 내용을 입력해주세요!(직접적인 html 대신 마크다운 표기법 사용가능)">${rePost.content}</textarea>
					</div>
					<div class="span1">
						<span>
						<button type="submit" class="post-button btn btn-primary" title='답변 작성하기'>
								<i class="fa fa-check"></i>
							</button>
						</span>
					</div>
					 <div class="file-div"> </div>
					 <input type="hidden" id="remove" name="remove">
				</form>
			</div>
		</div>
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>


</html>
