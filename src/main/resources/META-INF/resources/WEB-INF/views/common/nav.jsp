<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<sec:authentication property="principal" var="currentUser"/>
<h1>
	<a href="/">ForWeaver</a> <small>모두를 위한 소셜 코딩!</small>
</h1>

<div id="forweaver-nav"class="navbar navbar-inverse">
	<div class="navbar-inner">
		<div class="container-fluid">
			<div class="nav-collapse collapse">
				<ul class="nav">
					<li><a href="/weaver/"><i class="fa fa-twitter"></i>&nbsp;위버</a></li>
					<li><a href="/repository/"><i class="fa fa-bookmark"></i>&nbsp;저장소</a></li>
					<li><a href="/code/"><i class="fa fa-rocket"></i>&nbsp;코드</a></li>
					<li><a href="/community/"><i class="fa fa-comments"></i>&nbsp;커뮤니티</a></li>
				</ul>

				<ul class="nav pull-right">
					<sec:authorize access="isAnonymous()">
						<li><a href="<c:url value="/login" />"><i class="fa fa-user"></i>&nbsp;로그인</a></li>
					</sec:authorize>
					<sec:authorize access="isAuthenticated()">

						<li class="dropdown"><a href="#" class="dropdown-toggle"
							data-toggle="dropdown"> 
							<img style="height:28px;width:28px;" src='<c:out value="${currentUser.getImgSrc()}" escapeXml="false"></c:out>' />&nbsp;&nbsp;${currentUser.username}
								<b class="caret"></b></a>
							<ul class="dropdown-menu">
							
								<li><a href="/"><i class="fa fa-home"></i>&nbsp;&nbsp;마이페이지</a></li>
								<li><a href='/edit' ><i class="fa fa-cog"></i>&nbsp;&nbsp;정보수정</a></li>
								<li><a href="/community/tags:$${currentUser.username}"><i
										class="fa fa-envelope"></i>&nbsp;&nbsp;메세지함</a></li>
								<li class="divider"></li>
								<li><a href="<c:url value="/logout" />">
										<i class="fa fa-power-off"></i>&nbsp;&nbsp;로그아웃
								</a></li>
							</ul></li>

					</sec:authorize>

				</ul>

				<!--/.nav-collapse -->
			</div>
			<div class="span11">
				<span id = "tag-addon" style="margin-top:-5px;margin-right:-5px"class="span1 tag-addon"><i class="fa fa-search"></i></span>
				<input id = "search-input"style="margin-left:20px; width:810px; height:16px" type="text"/>
			</div>
		</div>		
	</div>	
</div>
<script>
$(function() {
	$('#search-input').val(getSearchWord(document.location.href));
	
	$('#search-input').on('keyup', function (e) {

	    if (e.keyCode == 13) {
				var tagNames = $("#tags-input").val();
				movePage(tagNames,$('#search-input').val());				
        }
	});
});
</script>