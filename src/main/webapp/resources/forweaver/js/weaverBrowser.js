
function showWeaverList(index) {
	$("#weaverTable").empty();
		if(index <=0)
			return;
		
		for(var i = (index-1)*10; i<=(index-1)*10+9;i++){

			if(weaverList.length == i)
				break;
			
			if (weaverList[i]["admin"]) {
				$("#weaverTable").append("<tr>" +
						"<td class='td-post-writer-img'>" +
						"<img src='" + 
						weaverList[i]["img"]+	
						"'></td>"+
						"<td class='vertical-middle'>" +
						weaverList[i]["id"]+
						"</td>"+
						"<td class='vertical-middle'>" +
						weaverList[i]["massage"]+
						"</td>"+
						"<td style='width: 42px;' class='td-button'>"+
						"<span class='span-button'><i class='fa fa-user'></i>"+
						"<p class='p-button'>괸리자</p>"+
						"</span></td></tr>");				
			} else {
				$("#weaverTable").append("<tr>" +
						"<td class='td-post-writer-img'>" +
						"<img src='" + 
						weaverList[i]["img"]+	
						"'></td>"+
						"<td class='vertical-middle'>" +
						weaverList[i]["id"]+
						"</td>"+
						"<td class='vertical-middle'>" +
						weaverList[i]["massage"]+
						"</td>"+
						"<td style='width: 42px;' class='td-button'> " +
						"<a onclick = 'return confirm(\"정말로 탈퇴하시겠습니까?\");' href='"+
						weaverList[i]["removeLink"]+
						"'><span class='span-button'>X" +
						"<p class='p-button'>탈퇴</p>	" +
						"</span></a></td>");
			}
		}
}


function makeNavigationInManageWeaver(size,length){ // 사이즈는 위버의 총 갯수, 랭스는 보여줄 위버 수
	if(size == 0)
		return;
	var pageLength = parseInt((size-1) / length); // 페이지 갯수
	
		$("#pageNavigation").empty();
		
		var html = "<ul>" +
		"<li class='previous'><a class='fui-arrow-left'></a></li>" +
		"<li class='page active'><a class ='page-link' href='javascript:showWeaverList(1);'>1</a></li>";
		
		for(var i =0 ; i < pageLength ; i++){
			var j = i+2;
			html+="<li class='page'><a class ='page-link' href='javascript:showWeaverList("+j+");'>"+j+"</a></li>";
		}
				
		$("#pageNavigation").append(html+
				"<li class='next'><a class='fui-arrow-right'></a></li>" +
				"</ul>");	
		
		if(pageLength>=5){
			for(var i = 5;i<pageLength+1;i++)
				$("a.page-link:eq("+i+")").hide();
		}	
}