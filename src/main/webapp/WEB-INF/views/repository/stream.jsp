<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/includes/taglibs.jsp"%>
<!DOCTYPE html>
<html><head>
<title>${repository.name}~${repository.description}</title>
<%@ include file="/WEB-INF/includes/src.jsp"%>
<script src="/resources/forweaver/js/d3.v3.min.js"></script>
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
		<%@ include file="/WEB-INF/common/nav.jsp"%>

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
					<sec:authorize ifAnyGranted="ROLE_USER, ROLE_ADMIN">
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
						<li class="active"></li>
					</a>
					<a href='/repository/${repository.name}/info:frequency'>
						<li></li>
					</a>
				</ol>
			</div>

			<div class="span12">
				<h4>라인 차트</h4>
			<div class="chart1"></div>
				<h4>파일 차트</h4>
				<div class="chart2"></div>
			</div>
			<script>
				var infoArray =[];
				<c:forEach items="${gps.getUserHashMap().keySet()}" var="email">
				infoArray["${email}"] = [];
				<c:forEach items="${gps.getDates()}" var="day">
				infoArray["${email}"]["${day}"] = 0;
				</c:forEach>
				</c:forEach>
				
				<c:forEach items="${gps.getGitChildStatistics()}" var="gcs">
				infoArray["${gcs.getUserEmail()}"]["${gcs.getDate()}"] = ${gcs.getTotal()};
				</c:forEach>
				
				var data = [];
				<c:forEach items="${gps.getUserHashMap().keySet()}" var="email">
				<c:forEach items="${gps.getDates()}" var="day">
				data.push({"key":"${email}","value":infoArray["${email}"]["${day}"],"date":"${day}"});
				</c:forEach>
				</c:forEach>
				chart(".chart1",data,"red",300,"Line");
				data = [];
				<c:forEach items="${gps.getGitChildStatistics()}" var="gcs">
				infoArray["${gcs.getUserEmail()}"]["${gcs.getDate()}"] = ${gcs.getTotalFile()};
				</c:forEach>
				<c:forEach items="${gps.getUserHashMap().keySet()}" var="email">
				<c:forEach items="${gps.getDates()}" var="day">
				data.push({"key":"${email}","value":infoArray["${email}"]["${day}"],"date":"${day}"});
				</c:forEach>
				</c:forEach>
				chart(".chart2",data,"green",740,"File");
				
				function chart(chart,data,color,top,say) {
					var datearray = [];
					var colorrange = [];
					if (color == "blue") {
						colorrange = ["#eff3ff","#c6dbef","#9ecae1","#6baed6","#3182bd","#08519c"];
					}
					else if (color == "green") {
						colorrange = ["#c7e9c0","#a1d99b","#74c476","#41ab5d","#238b45","#005a32"];
					}
					else if (color == "red") {
						colorrange = ["#fdbb84","#fc8d59","#ef6548","#d7301f","#b30000","#7f0000"];
					}
					strokecolor = colorrange[0];
				
					var format = d3.time.format("%Y/%m/%d");
				
					var margin = {top: 20, right: 50, bottom: 30, left: 50};
					var width = 860;
					var height = 400 - margin.top - margin.bottom;
				
					var tooltip = d3.select("body")
					.append("div")
					.attr("class", "remove")
					.style("position", "absolute")
					.style("z-index", "20")
					.style("visibility", "hidden")
					.style("top", top+50+"px")
					.style("left", "280px");
				
					var x = d3.time.scale()
					.range([0, width]);
				
					var y = d3.scale.linear()
					.range([height-10, 0]);
				
					var z = d3.scale.ordinal()
					.range(colorrange);
				
					var xAxis = d3.svg.axis()
					.scale(x)
					.orient("bottom");
				
					var yAxis = d3.svg.axis()
					.scale(y);
				
					var yAxisr = d3.svg.axis()
					.scale(y);
				
					var stack = d3.layout.stack()
					.offset("silhouette")
					.values(function(d) { return d.values; })
					.x(function(d) { return d.date; })
					.y(function(d) { return d.value; });
				
					var nest = d3.nest()
					.key(function(d) { return d.key; });
				
					var area = d3.svg.area()
					.interpolate("cardinal")
					.x(function(d) { return x(d.date); })
					.y0(function(d) { return y(d.y0); })
					.y1(function(d) { return y(d.y0 + d.y); });
				
					var svg = d3.select(chart).append("svg")
					.attr("width", width + margin.left + margin.right)
					.attr("height", height + margin.top + margin.bottom)
					.append("g")
					.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
				
					data.forEach(function(d) {
						d.date = format.parse(d.date);
						d.value = +d.value;
					});
				
					var layers = stack(nest.entries(data));
				
					x.domain(d3.extent(data, function(d) { return d.date; }));
					y.domain([0, d3.max(data, function(d) { return d.y0 + d.y; })]);
				
					svg.selectAll(".layer")
					.data(layers)
					.enter().append("path")
					.attr("class", "layer")
					.attr("d", function(d) { return area(d.values); })
					.style("fill", function(d, i) { return z(i); });
				
				
					svg.append("g")
					.attr("class", "x axis")
					.attr("transform", "translate(0," + height + ")")
					.call(xAxis);
				
					svg.append("g")
					.attr("class", "y axis")
					.attr("transform", "translate(" + width + ", 0)")
					.call(yAxis.orient("right"));
				
					svg.append("g")
					.attr("class", "y axis")
					.call(yAxis.orient("left"));
				
					svg.selectAll(".layer")
					.attr("opacity", 1)
					.on("mouseover", function(d, i) {
						svg.selectAll(".layer").transition()
						.duration(250)
						.attr("opacity", function(d, j) {
							return j != i ? 0.6 : 1;
						})})
				
						.on("mousemove", function(d, i) {
							mousex = d3.mouse(this);
							mousex = mousex[0];
							var invertedx = x.invert(mousex);
							invertedx = invertedx.getMonth() + invertedx.getDate();
							var selected = (d.values);
							for (var k = 0; k < selected.length; k++) {
								datearray[k] = selected[k].date
								datearray[k] = datearray[k].getMonth() + datearray[k].getDate();
							}
				
							mousedate = datearray.indexOf(invertedx);
							pro = d.values[mousedate].value;
				
							d3.select(this)
							.classed("hover", true)
							.attr("stroke", strokecolor)
							.attr("stroke-width", "1px"), 
							tooltip.html( "<p> <img style='width:32px;' src = '/"+d.key+"/img'> " + d.key + " " + pro + " "+say+"</p>" ).style("visibility", "visible");
				
						})
						.on("mouseout", function(d, i) {
							svg.selectAll(".layer")
							.transition()
							.duration(250)
							.attr("opacity", "1");
							d3.select(this)
							.classed("hover", false)
							.attr("stroke-width", "0px"), tooltip.html( "<p> <img style='width:32px;' src = '/"+(d.key).replace(".",",")+"/img'> " + d.key + " " + pro + " "+say+"</p>" ).style("visibility", "hidden");
						})
				
						var vertical = d3.select(chart)
						.append("div")
						.attr("class", "remove")
						.style("position", "absolute")
						.style("z-index", "19")
						.style("width", "1px")
						.style("height", "380px")
						.style("top", top+40+"px")
						.style("bottom", "30px")
						.style("left", "0px")
						.style("background", "#000");
				
					d3.select(chart)
					.on("mousemove", function(){  
						mousex = d3.mouse(this);
						mousex = mousex[0] + 5;
						vertical.style("left", (mousex+200) + "px" )})
						.on("mouseover", function(){  
							mousex = d3.mouse(this);
							mousex = mousex[0] + 5;
							vertical.style("left", (mousex+200) + "px")});
				
				}
				
			</script>
		</div>
		<%@ include file="/WEB-INF/common/footer.jsp"%>
	</div>
</body>
</html>