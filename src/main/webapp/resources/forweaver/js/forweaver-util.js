
var editorMode = false; 

function IndexOf(Val, Str, x) {
	if (x <= (Str.split(Val).length - 1)) {
		Ot = Str.indexOf(Val);
		if (x > 1) {
			for (var i = 1; i < x; i++) {
				var Ot = Str.indexOf(Val, Ot + 1)
			}
		}
		return Ot;
	} else {
		return 0
	}
}

function replaceAll(str, searchStr, replaceStr) {

    return str.split(searchStr).join(replaceStr);
}

function mongoObjectId () {
	var timestamp = (new Date().getTime() / 1000 | 0).toString(16);
	return timestamp + 'xxxxxxxxxxxxxxxx'.replace(/[x]/g, function() {
		return (Math.random() * 16 | 0).toString(16);
	}).toLowerCase();
} //https://gist.github.com/solenoid/1372386


function containsObject(list,obj ) { // 배열에 객체가 있는지 조사.
	var i;
	for (i = 0; i < list.length; i++) {
		if (list[i] === obj) {
			return true;
		}
	}

	return false;
}


function imgCheck(fileName) {
	if(!/(\.bmp|\.png|\.gif|\.jpg|\.jpeg)$/i.test(fileName)) {    
		return false;   
	}   
	return true; 
}

function deleteReply(postID,rePostID,number){
	if (confirm("정말 댓글을 삭제하시겠습니까?")){
		window.location =	"/community/"+postID+"/"+rePostID+"/"+number+"/delete";
	}else{
		return;
	}
}

function deleteCodeReply(postID,rePostID,number){
	if (confirm("정말 댓글을 삭제하시겠습니까?")){
		window.location =	"/code/"+postID+"/"+rePostID+"/"+number+"/delete";
	}else{
		return;
	}
}

function pushPost(postID){
	if (confirm("정말 추천하시겠습니까?")){
		window.location =	"/community/"+postID+"/push";
	}else{
		return;
	}
}

function deletePost(postID){
	if (confirm("정말 삭제하시겠습니까?")){
		window.location =	"/community/"+postID+"/delete";
	}else{
		return;
	}
}

function deleteCode(codeID){
	if (confirm("정말로 코드를 삭제하시겠습니까?")){
		window.location =	"/code/"+codeID+"/delete";
	}else{
		return;
	}
}

function pushRePost(postID,rePostID){
	if (confirm("정말 추천하시겠습니까?")){
		window.location =	"/community/"+postID+"/"+rePostID+"/push";
	}else{
		return;
	}
}

function deleteRePost(postID,rePostID){
	if (confirm("답글을 삭제하시겠습니까?")){
		window.location =	"/community/"+postID+"/"+rePostID+"/delete";
	}else{
		return;
	}
}

function getSearchWord(url){
	if(url.indexOf("/search:")==-1)
		return [];
	url =  decodeURI(url);
	url = url.substring(url.indexOf("search:")+7);
	if(url.indexOf("/")!=-1)
		url = url.substring(0,url.indexOf("/"));
	return url;
}


function getTagList(url){
	if(url.indexOf("/tags:")==-1)
		return "";
	url =  decodeURI(url);
	var tagList = "";
	var realURL = true;
	if(url.indexOf("/tags:") == 0)
		realURL = false;
	url = url.substring(url.indexOf("tags:")+5);
	if(realURL && url.indexOf("/")!=-1)
		url = url.substring(0,url.indexOf("/"));
	tagList = url.replace('>', '/');
	return tagList;
}

function getSort(url){
	if(url.indexOf("/sort:")==-1)
		return "age-desc";
	var sort = url.substring(url.indexOf("sort:")+5);

	if(sort.indexOf("/")==-1)
		return sort;
	return sort.substring(0,sort.indexOf("/"));
}


function endsWith(str, suffix) {
	return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

function extensionSeach(url){
		
	if(endsWith(url,"java") || endsWith(url,"pde"))
		return "java";
	else if(endsWith(url,"css"))
		return "css";
	else if(endsWith(url,"xml" || endsWith(url,"html")))
		return "xml";
	else if(endsWith(url,"php"))
		return "php";
	else if(endsWith(url,"pl"))
		return "perl";
	else if(endsWith(url,"js"))
		return "jscript";
	else if(endsWith(url,"diff"))
		return "diff";
	else if(endsWith(url,"c") || endsWith(url,"h") || endsWith(url,"cpp") || endsWith(url,"ino"))
		return "cpp";
	else if(endsWith(url,"cs"))
		return "csharp";
	else if(endsWith(url,"sql"))
		return "sql";
	else if(endsWith(url,"py"))
		return "python";
	else
		return "text";
}


function movePage(tagArrayString,searchWord){
	if(editorMode)
		return;
	
	var url = document.location.href;

	if(url.indexOf("/tags:") != -1)
		url = url.substring(0,url.indexOf("/tags:"))+'/';
	else if(url.indexOf("/community") != -1)
		url = url.substring(0,url.indexOf("/community")+10)+'/';
	else if(url.indexOf("/weaver") != -1)
		url = url.substring(0,url.indexOf("/weaver")+7)+'/';
	else if(url.indexOf("/project") != -1)
		url = url.substring(0,url.indexOf("/project")+8)+'/';
	else if(url.indexOf("/code") != -1)
		url = url.substring(0,url.indexOf("/code")+5)+'/';
	else if(url.indexOf("/lecture") != -1)
		url = url.substring(0,url.indexOf("/lecture")+8)+'/';
	else
		if(url.indexOf("/m/") != -1 )
			url = url.substring(0,IndexOf("/",url,5))+"/";
		else
			url = url.substring(0,IndexOf("/",url,4))+"/";
	
	if(tagArrayString.length == 0){
		window.location = url;
		return;
	}
	if(tagArrayString.indexOf(",") === 0)
		tagArrayString = tagArrayString.substring(1,tagArrayString.length);
	
	tagArrayString = tagArrayString.replace('/', '>');
	
	url = url + "tags:"+	tagArrayString;

	if(searchWord.length != 0)
		url = url +"/search:"+ searchWord;
	window.location = url;
}

function moveUserPage(path,tagArrayString,searchWord){
	if(editorMode)
		return;
	
	var url = document.location.href;

	if(tagArrayString.length == 0 || tagArrayString==""){
		window.location = url;
		return;
	}
	
	if(tagArrayString.indexOf(",") === 0)
		tagArrayString = tagArrayString.substring(1,tagArrayString.length);
	
	tagArrayString = tagArrayString.replace('/', '>');
	
	url = path + "tags:"+	tagArrayString;

	if(searchWord.length != 0)
		url = url +"/search:"+ searchWord;

	window.location = url;
}


function readURL(input) {
	if (input.files && input.files[0]) {
		var reader = new FileReader();

		reader.onload = function (e) {
			$('#preview').attr('src', e.target.result);
		}

		reader.readAsDataURL(input.files[0]);
	}

}

function openWindow(url, width, height){
	window.open(url,'','width='+width+',height='+height+',top='+((screen.height-height)/2)+',left='+((screen.width-width)/2)+',location =no,scrollbars=no, status=no;');
}

function isImage(filename) { // 파일이 이미지 파일인지 검사
	filename = filename.substring(filename.lastIndexOf(".") + 1,filename.length).toUpperCase();
		if(filename.search("ANI")!=-1 || filename.search("BMP")!=-1 || filename.search("CAL")!=-1
			|| filename.search("CAL")!=-1 || filename.search("FAX")!=-1 || filename.search("GIF")!=-1
			|| filename.search("IMG")!=-1 || filename.search("JPE")!=-1 || filename.search("JPEG")!=-1
			|| filename.search("JPG")!=-1 || filename.search("MAC")!=-1 || filename.search("PBM")!=-1
			|| filename.search("PCD")!=-1 || filename.search("PCX")!=-1 || filename.search("PCT")!=-1
			|| filename.search("PGM")!=-1 || filename.search("PNG")!=-1 || filename.search("PPM")!=-1
			|| filename.search("PSD")!=-1 || filename.search("RAS")!=-1 || filename.search("TGA")!=-1
			|| filename.search("TIF")!=-1 || filename.search("TIFF")!=-1 || filename.search("WMF")!=-1){
		return true;
	}
 return false;

}


function fileUploadChange(fileUploader,textarea){
	var fileName = $(fileUploader).val();			

	$(function (){
	
	if( fileName.length > 70 ){
		alert("파일 이름이 너무 깁니다!");
		return;
	}
	if(fileName !=""){ // 파일을 업로드하거나 수정함
		if(fileName.indexOf("C:\\fakepath\\") != -1)
			fileName = fileName.substring(12);
		fileName = replaceAll(fileName,"?","_");
		fileName = replaceAll(fileName,"#","_");
		fileName = replaceAll(fileName," ","_");
		
		fileHash[fileName] = mongoObjectId();
		$.ajax({
		    url: '/data/tmp',
            type: "POST",
            contentType: false,
            processData: false,
            data: function() {
                var data = new FormData();
                data.append("objectID", fileHash[fileName]);
                data.append("file", fileUploader.files[0]);
                return data;
            }()
		});	
		if(isImage(fileName))
			$(textarea).val($(textarea).val()+'\n!['+fileName+'](/data/'+fileHash[fileName]+'/'+fileName+')');
	
		if(fileUploader.id == "file"+fileCount){ // 업로더의 마지막 부분을 수정함
	fileCount++;
	$(".file-div").append("<div class='fileinput fileinput-new' data-provides='fileinput'>"+
			  "<div class='input-group'>"+
			    "<div class='form-control' data-trigger='fileinput' title='업로드할 파일을 선택하세요!'><i class='icon-file '></i> <span class='fileinput-filename'></span></div>"+
			    "<span class='input-group-addon btn btn-primary btn-file'><span class='fileinput-new'>"+
			    "<i class='fa fa-arrow-circle-o-up icon-white'></i></span><span class='fileinput-exists'><i class='icon-repeat icon-white'></i></span>"+
				"<input onchange ='fileUploadChange(this,\""+textarea+"\");' type='file' multiple='true' id='file"+fileCount+"' name='files["+(fileCount-1)+"]'></span>"+
			   "<a id='remove-file' href='#' class='input-group-addon btn btn-primary fileinput-exists' data-dismiss='fileinput'><i class='icon-remove icon-white'></i></a>"+
			  "</div>"+
			"</div>");
		}
	}else{
		if(fileUploader.id == "file"+(fileCount-1)){ // 업로더의 마지막 부분을 수정함
			
		$("#file"+fileCount).parent().parent().remove();

			--fileCount;
	}}});
}
