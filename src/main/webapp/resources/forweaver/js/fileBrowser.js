function showFileBrowser(directoryPath,selectBranch,fileBrowser) {
	
	var parentDirectoryPath = makeParentDirectoryPath(directoryPath);
	
	$(document).ready(function() {
		if(directoryPath == "/"){
			$(".readme").show();
			$(".readme-header").show();
		}else{
			$(".readme").hide();
			$(".readme-header").hide();
		}
		$('#labelPath').empty();
		$('#labelPath').append(directoryPath);
		$("#fileBrowserTable").empty();
		if(!(directoryPath=="" || directoryPath=="/"))
			$("#fileBrowserTable").append("<tr><td style='border-top:0px;'>"+
					"<img src ='/resources/forweaver/img/directory.png'>"+
					"</td><td colspan = '3' style='border-top:0px;' class= 'td-filename'>" +
					"<a rel='external' href ='"+fileBrowserURL+selectBranch+ "/filepath:"+parentDirectoryPath + "'>상위 디렉토리</a>" + 
				"</td></tr>");
		
		$.each(fileBrowser, function(index, value) {

			var appendHTML = "";
			if (value.directory) {
				appendHTML = "<tr>" +  	
				"<td class='td-icon'>" +
				"<a rel='external' href ='"+
				fileBrowserURL+
				selectBranch+
				"/filepath:"+
				value.path+"'>" + 
				"<img src ='/resources/forweaver/img/directory.png'></a></td>";

			} else {
				appendHTML = "<tr>" +
				"<td class='td-icon'>" +
				"<a rel='external' href ='"+
				fileBrowserURL+
				selectBranch+
				"/filepath:"+
				value.path.replace(".jsp",",jsp")+"'>" + 
				"<img src ='/resources/forweaver/img/file.png'></a></td>";
			}
			
			
			
			appendHTML += "<td class = 'td-filename'>" +
			"<a rel='external' href ='"+fileBrowserURL+selectBranch+"/filepath:"+value.path.replace(".jsp",",jsp")+"'>" + value.name + 
			"</a></td><td class = 'td-commitlog'>";
			
			//이미지를 추가함
			appendHTML+="<a rel='external' href ='/"+value.commiterEmail+"'><img class='td-commitlog-img' src='/"
				+value.commiterEmail+"/img' title='"+value.commiterName+"<"+value.commiterEmail+">'></a>&nbsp;&nbsp;";
				
			if(commitlogHref.length == 0){
				appendHTML = appendHTML + value.commitLog + "</td>" + 
				"<td class = 'td-time'>" + value.date + "</td></tr>"; 
			}else{
				appendHTML = appendHTML + "<a rel='external' class='none-color' href ="+ commitlogHref+value.commitID+">"+
				value.commitLog + 
				"</a></td>" + 
				"<td class = 'td-time'>" + 
				"<a rel='external'  class='none-color' href ="+ commitlogHref+value.commitID+">"+
				value.date + "</a></td></tr>"; 
			}
				
			$("#fileBrowserTable").append(appendHTML);
			//화면 크기에 따라 다르게 출력
			 if ($(window).width() > 500) {
			     	$( ".td-commitlog" ).show();
			    }else{
			    	$( ".td-commitlog" ).hide();
			    }
		});
	});
}


function makeParentDirectoryPath(path) {
	if(path == "/")
		return path;
	path = path.substring(0,path.lastIndexOf("/"));
	if(path == "")
		return "/";
	return path;
}
