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
				<div style="padding-top:60px;padding-bottom:60px;
				text-align:center;  background-color: #fff;" class="well-white hero-unit center">
					<h1><i class="fa fa-exclamation-triangle"></i> 일반 에러.
					</h1>
					<br />
					<p>
						<b>원인 모를 에러가 발생하였습니다. 이전화면으로 돌아가주세요.</b>
					</p><br />
					<a href="/" class="btn btn-large btn-info"><i
						class="icon-home"></i> 메인 화면으로 돌아가기</a>
				</div>
				<br />
		
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>

</body>
</html>