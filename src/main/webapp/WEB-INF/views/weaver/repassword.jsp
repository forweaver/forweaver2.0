<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<title>Forweaver : 비밀번호 변경 페이지!</title>
</head>
<body>
	<div class="container">
	                        
		<%@ include file="/WEB-INF/common/nav.jsp"%>
				<div style="padding-top:60px;padding-bottom:60px;
				text-align:center;  background-color: #fff;" class="well-white hero-unit center">
					<h1><i class="fa fa-lock"></i> 비밀번호 재발급.
					</h1>
					<br />
					<p>
						<b>비밀번호가 재발급되었습니다. 바뀐 비밀번호로 로그인해주세요.</b>
					</p><br />
					<a onclick="/" class="btn btn-large btn-info"><i
						class="icon-user"></i> 로그인하기</a>
				</div>
				<br />
		
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>
</html>