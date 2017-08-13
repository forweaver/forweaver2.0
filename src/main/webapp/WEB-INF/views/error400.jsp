<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<title>Forweaver : 에러!</title>
</head>
<body>
	<div class="container">
	                        
		<h1>
			<a href="/">ForWeaver</a> <small>학생들을 위한 소셜 코딩!</small>
		</h1>
				<div style="padding-top:60px;padding-bottom:60px;text-align:center; background-color: #fff;" 
				class="well-white hero-unit center">
					<h1><i class="fa fa-lock"></i> 권한 없음.
					</h1>
					<br />
					<p>
						<b>게시글을 읽을 수 없거나, 프로젝트 및 강의에 가입하지 않으셨습니다!</b>
					</p><br />
					<a onclick="javascript:history.back(-1);" class="btn btn-large btn-info"><i
						class="icon-home"></i> 이전 화면으로 돌아가기</a>
				</div>
				<br />
		
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>
</html>