<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<span id="tag-addon"
	style="margin-top: 6px; margin-left: 0px; margin-right: -15px"
	class="span1">Tagged :</span>
<div title="태그를 입력하시고 나서 꼭 엔터키나 스페이스키를 누르시면 추가가 됩니다."
	class="span11 tag-span">
	<input placeholder="여기에 태그를 입력하고 꼭 엔터!" class="tagarea tagarea-full"
		id="tags-input" /> <input name="tags" type="hidden" id="tag-hidden" />

	<script>
					var move = true;
					function ieVersion () {
						  var myNav = navigator.userAgent.toLowerCase();
						  return (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) : false;
						}
					
					$('#tags-input').tagsinput({
						  confirmKeys: [13, 32],
						  maxTags: 6,
						  maxChars: 30,
						  trimValue: true
					});
					$("#tag-hidden").val(getTagList(document.location.href));
					
					$.each(getTagList(document.location.href).split(","), function(index, value) { 
						  $('#tags-input').tagsinput('add',value);
					});
					
					$('#tags-input').on('itemAdded', function(event) {
						 if(event.item.indexOf("?") !=-1 || event.item.indexOf("#") !=-1 || 
								 event.item.indexOf(".") !=-1){
							 $('#tags-input').tagsinput('remove',event.item);
							 return;
						 }
						
						$("#tag-hidden").val($("#tags-input").val());
						
						if(move)
							movePage($("#tags-input").val(),"");
						});
					
					$('#tags-input').on('itemRemoved', function(event) {
						$("#tag-hidden").val($("#tags-input").val());
						movePage($("#tags-input").val(),"");
						});
					
					$('#tags-input').tagsinput('focus');
					
</script>
</div>