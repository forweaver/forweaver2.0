<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title>${repository.name}~${repository.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<style type="text/css">
rect.bordered {
	stroke: #E6E6E6;
	stroke-width: 2px;
}

text.mono {
	font-size: 8pt;
	fill: #000;
}

text.axis-workweek {
	fill: #000;
}

text.axis-worktime {
	fill: #000;
}

.axis path, .axis line {
	fill: none;
	stroke: #eee;
	shape-rendering: crispEdges;
}

.axis text {
	font-family: sans-serif;
	font-size: 11px;
}

.loading {
	font-family: sans-serif;
	font-size: 15px;
}

.circle {
	fill: #222;
}

.circle:hover {
	fill: #CC3333;
}

.d3-tip {
	line-height: 1;
	font-weight: bold;
	padding: 12px;
	background: rgba(0, 0, 0, 0.8);
	color: #fff;
	border-radius: 10px;
}

.d3-tip:after {
	box-sizing: border-box;
	display: inline;
	font-size: 10px;
	width: 100%;
	line-height: 0.6;
	color: rgba(0, 0, 0, 0.8);
	content: "\25BC";
	position: absolute;
	text-align: center;
}

.d3-tip.n:after {
	margin: -1px 0 0 0;
	top: 100%;
	left: 0;
}
</style>
<script src="/resources/forweaver/js/d3.v3.min.js"></script>
<script src="/resources/forweaver/js/d3-tip.min.js"></script>
</head>
<body>
<script>
$(document).ready(function() {
	move = false;
			<c:forEach items='${repository.tags}' var='tag'>
			$('#tags-input').tagsinput('add',"${tag}");
			</c:forEach>
			move = true;
});
</script>
	<div class="container">
		<%@ include file="/WEB-INF/views/common/nav.jsp"%>

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
					<li><a href="/repository/${repository.name}/log">로그</a></li>
					<li><a href="/repository/${repository.name}/community">커뮤니티</a></li>
					
					<li><a href="/repository/${repository.name}/weaver">사용자</a></li>
					<sec:authorize access="isAuthenticated()">
					<c:if test="${repository.getCreator().equals(currentUser) }">
					<li><a href="/repository/${repository.name}/edit">관리</a></li>
					</c:if>
					</sec:authorize>
					<li class="active"><a href="/repository/${repository.name}/info">정보</a></li>
					
				</ul>
			</div>
			<div class="span4">
				<div class="input-block-level input-prepend" title="http 주소로 저장소를 복제할 수 있습니다!&#13;복사하려면 ctrl+c 키를 누르세요.">
					<span class="add-on"><i class="fa fa-git"></i></span> <input
						value="http://${pageContext.request.serverName}/g/${repository.name}.git" type="text"
						class="input-block-level">
				</div>
			</div>

			<div class="span8">
				
			</div>
			<div class="carousel span4">
				<ol class="carousel-indicators">
					<a href='/repository/${repository.name}/info'>
						<li></li>
					</a>
					<a href='/repository/${repository.name}/info:stream'>
						<li></li>
					</a>
					<a href='/repository/${repository.name}/info:frequency'>
						<li class="active"></li>
					</a>
				</ol>
			</div>
			
			<div class="span12">
			<h4>펀치 카드</h4>
			<div style="margin-left: -30px;" id="punchcard"></div>
			<br>
				<h4>힛 맵</h4>

				<div style="margin-top: -30px;" id="chart"></div>
			</div>
			<script type="text/javascript">
var w = 950,
h = 300,
pad = 20,
left_pad = 80,
punchcard_data = [  // day, hour, radius 순이고 d[0], d[1], d[2]로 불러옴
                    <c:forEach var="i" begin="0" end="6">
                    <c:forEach var="j" begin="0" end="23">
                    [${i}, ${j}, ${dayAndHour[i][j]}],
                    </c:forEach>
                    </c:forEach>
                    ];

var svg = d3.select("#punchcard")
.append("svg")
.attr("width", w)
.attr("height", h);

var x = d3.scale.linear().domain([0, 23]).range([left_pad, w-pad]),
y = d3.scale.linear().domain([0, 6]).range([pad, h-pad*2]);

var xAxis = d3.svg.axis().scale(x).orient("bottom")
.ticks(24)
.tickFormat(function (d, i) {
	var m = (d > 12) ? "pm" : "am";
	return (d%12 == 0) ? 12+m :  d%12+m;
}),
yAxis = d3.svg.axis().scale(y).orient("left")
.ticks(7)
.tickFormat(function (d, i) {
	return ['일', '월', '화', '수', '목', '금', '토'][d];
});

var tooltip = d3.tip()
.attr("class", "d3-tip")
.offset([-10, 0])
.html(function(d){
	return "<span style='color:#ff3333'>" + d[2] + "</span><strong> Commits</strong>";
});

svg.append("g")
.attr("class", "axis")
.attr("transform", "translate(0, "+(h-pad)+")")
.call(xAxis);

svg.append("g")
.attr("class", "axis")
.attr("transform", "translate("+(left_pad-pad)+", 0)")
.call(yAxis);

//Loading 대비. 없애도 무방함
svg.append("text")
.attr("class", "loading")
.text("Loading ...")
.attr("x", function () { return w/2; })
.attr("y", function () { return h/2-5; });

//d3-tip.js로 따로 불러왔기 때문에 툴팁 쓰기 전에 이렇게 불러주는 것.
svg.call(tooltip);

svg.selectAll("circle")
.data(punchcard_data)
.enter()
.append("circle")
.attr("class", "circle")
.attr("cx", function (d) { return x(d[1]); })
.attr("cy", function (d) { return y(d[0]); })
.attr("r", function (d) {
	if(d[2]!=0) return 3+d[2]/3.3;
	else return d[2];
}) // 원크기 맞게 조정해 보았음
.on("mouseover", tooltip.show)
.on("mouseout", tooltip.hide);

//로딩끝나면 로딩메세지 삭제
svg.selectAll(".loading").remove();

/////////////////

var data = [
            <c:forEach var="i" begin="1" end="7">
            <c:forEach var="j" begin="1" end="24">
            {"day":${i}, "hour":${j}, "value":${dayAndHour[i-1][j-1]}},
            </c:forEach>
            </c:forEach>            
            ];

var margin = { top: 50, right: 0, bottom: 100, left: 30 },
width = 930 - margin.left - margin.right,
height = 430 - margin.top - margin.bottom,
gridSize = Math.floor(width / 24),
legendElementWidth = gridSize*2,
buckets = 9,
colors =  ["#fff7fb","#ece7f2","#d0d1e6","#a6bddb","#74a9cf","#3690c0","#0570b0","#045a8d","#023858"], // alternatively colorbrewer.YlGnBu[9]
days = [ "일","월", "화", "수", "목", "금", "토"],
times = ["12am","1am", "2am", "3am", "4am", "5am", "6am", "7am", "8am", "9am", "10am", "11am", "12am", "1pm", "2pm", "3pm", "4pm", "5pm", "6pm", "7pm", "8pm", "9pm", "10pm", "11pm"];



var colorScale = d3.scale.quantile()
.domain([0, buckets - 1, d3.max(data, function (d) { return d.value; })])
.range(colors);

var svg = d3.select("#chart").append("svg")
.attr("width", width + margin.left + margin.right)
.attr("height", height + margin.top + margin.bottom)
.append("g")
.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var dayLabels = svg.selectAll(".dayLabel")
.data(days)
.enter().append("text")
.text(function (d) { return d; })
.attr("x", 0)
.attr("y", function (d, i) { return i * gridSize; })
.style("text-anchor", "end")
.attr("transform", "translate(-6," + gridSize / 1.5 + ")")
.attr("class", function (d, i) { return ((i >= 0 && i <= 4) ? "dayLabel mono axis axis-workweek" : "dayLabel mono axis"); });

var timeLabels = svg.selectAll(".timeLabel")
.data(times)
.enter().append("text")
.text(function(d) { return d; })
.attr("x", function(d, i) { return i * gridSize; })
.attr("y", 0)
.style("text-anchor", "middle")
.attr("transform", "translate(" + gridSize / 2 + ", -6)")
.attr("class", function(d, i) { return ((i >= 7 && i <= 16) ? "timeLabel mono axis axis-worktime" : "timeLabel mono axis"); });

var heatMap = svg.selectAll(".hour")
.data(data)
.enter().append("rect")
.attr("x", function(d) { return (d.hour - 1) * gridSize; })
.attr("y", function(d) { return (d.day - 1) * gridSize; })
.attr("rx", 4)
.attr("ry", 4)
.attr("class", "hour bordered")
.attr("width", gridSize)
.attr("height", gridSize)
.style("fill", colors[0]);

heatMap.transition().duration(1000)
.style("fill", function(d) { return colorScale(d.value); });

heatMap.append("title").text(function(d) { return d.value; });

var legend = svg.selectAll(".legend")
.data([0].concat(colorScale.quantiles()), function(d) { return d; })
.enter().append("g")
.attr("class", "legend");

legend.append("rect")
.attr("x", function(d, i) { return legendElementWidth * i; })
.attr("y", height)
.attr("width", legendElementWidth)
.attr("height", gridSize / 2)
.style("fill", function(d, i) { return colors[i]; });

legend.append("text")
.attr("class", "mono")
.text(function(d) { return "≥ " + Math.round(d); })
.attr("x", function(d, i) { return legendElementWidth * i; })
.attr("y", height + gridSize);

</script>
		</div>
		<%@ include file="/WEB-INF/views/common/footer.jsp"%>
	</div>
</body>
</html>