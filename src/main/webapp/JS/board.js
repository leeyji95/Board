var page = 1 // 현재 페이지
var pageRows = 10 // 한 페이지에 보여지는 게시글 개수
var viewItem = undefined;  // 가장 최근에 view 한 글 데이터 

$(document).ready(function(){
	
	// 게시판 목록 1 페이지 로딩 
	loadPage(page);
	
	// 글 작성 버튼 누르면 모달 팝업 뜨기 
	$("#btnWrite").click(function(){
		setPopup("write"); 
		$("#dlg_write").show();
	});
	
	// 모달 대화상자에서 close 버튼 누르면 닫기
	$(".modal .close").click(function(){
		$(this).parents(".modal").hide(); 
	});

	// ★★★  글 작성 submit 되면 처리(여기서 백그라운드 request 보내줘야함)
	$("#frmWrite").submit(function(){
		$(this).parents(".modal").hide();
		return chkWrite();
	});
	
		// 글 삭제 버튼 누르면 
	$("#btnDel").click(function(){
		chkDelete();
	});
	
	
	// 글 읽기(view) 대화상자에서 삭제버튼 누르면 해당 글(uid) 삭제 진행 
	$('#viewDelete').click(function(){
		var uid = viewItem.uid;
		if(deleteUid(uid)){ // 해당 글 삭제 
			$(this).parents(".modal").hide(); // 삭제 성공하면 대화상자 닫기 
		}
	});
	
	
	// 글 읽기(view) 대화상자에서 수정버튼 누르면 
	$("#viewUpdate").click(function(){
		setPopup("update");
	});
	
	// 글 수정완료 버튼 누르면 
	$("#updateOk").click(function(){
		chkUpdate();
	});
});

//window.onkeyup = function(e) {
//	var key = e.keyCode ? e.keyCode : e.which;
//
//	if(key == 27) {
//		$(".modal").fadeOut(90);
//		$("body").css({overflow:'scroll'}).unbind('touchmove');
//	}
//}
	
// page 번째 페이지 로딩 
function loadPage(page){
	
	$.ajax({
		url : "list.do?page=" + page + "&pageRows=" + pageRows
		, type : "get"
		, cache : false
		, dataType : "JSON"
		, success : function(data, status){
			if(status == "success"){
				
//				alert("AJAX 성공 : request 성공"); // 최초 로딩 시 뜸 
				if(updateList(data)){
					
					// 업데이트된 list 에 필요한 이벤트 가동
					addViewEvent();
				} else{
					alert("출력할 데이터가 없습니다.");
				}
			}
		}
	});
	
} // end loadPage()

function updateList(jsonObj){
	
	result = "";  
	
	if(jsonObj.status == "OK"){
		
		var count = jsonObj.count;
		
		window.page = jsonObj.page; // 전역변수 업데이트 (서버단에서 받아온 값)
		window.pageRows = jsonObj.pagerows;
		
		var i;
		var items = jsonObj.data; // 배열
		for(i = 0; i < count; i++){
			
			result += "<tr>\n";
			result += "<td><input type='checkbox' name='wkey' value='" + items[i].wkey + "'></td>\n";
			result += "<td>" + items[i].wkey + "</td>\n";
			result += "<td><span class='subject' data-wkey='" + items[i].wkey + "'>" + items[i].subject + "</span></td>\n"; /* 어떤 엘리먼트에도 원하는 값을 꽂아 넣을 수 있다. data-(원하는 이름)*/
			result += "<td>" + items[i].name + "</td>\n";
			//result += "<td><span data-viewcnt='" + items[i].uid + "'>" + items[i].viewcnt + "</span></td>\n"; /* 해당 uid 에 대한 조회수만 증가시킬 것이기 때문에 특정 uid 값이 필요한 것이다--> 정확안 uid값이 있으므로 이 uid 값을 찾아서 조회수를 바꿔주면 된다. */
			result += "<td>" + items[i].regdate + "</td>\n";
			result += "</tr>\n";
		} // end for
	
		$("#list tbody").html(result); // 테이블 업데이트
		
		// 페이지 정보 업데이트 
		$('#pageinfo').text(jsonObj.page + "/" + jsonObj.totalpage + "페이지, " + jsonObj.totalcnt + "개의 글");
		
		// pageRows
		var txt = "<select id='rows' onchange='changePageRows()'>\n";
		txt += "<option " + ((window.pageRows == 10)?"selected":"") + " value='10'>10개씩</option>\n";
		txt += "<option " + ((window.pageRows == 20)?"selected":"") + " value='20'>20개씩</option>\n";
		txt += "<option " + ((window.pageRows == 50)?"selected":"") + " value='50'>50개씩</option>\n";
		txt += "<option " + ((window.pageRows == 100)?"selected":"") + " value='100'>100개씩</option>\n";		
		txt += "</select>\n";
		$("#pageRows").html(txt);

		// 페이징 업데이트
		var pagination = buildPagination(jsonObj.writepages, jsonObj.totalpage, jsonObj.page, jsonObj.pagerows);
		$("#pagination").html(pagination);
	
		
		return true;
	} else{
		alert(jsonObj.message);
		return false;
	}
	return false;
} // end updateList

function buildPagination(writePages, totalPage, curPage, pageRows){
	
	var str = "";   // 최종적으로 페이징에 나타날 HTML 문자열 <li> 태그로 구성
	
	// 페이징에 보여질 숫자들 (시작숫자 start_page ~ 끝숫자 end_page)
    var start_page = ( (parseInt( (curPage - 1 ) / writePages ) ) * writePages ) + 1;
    var end_page = start_page + writePages - 1;

    if (end_page >= totalPage){
    	end_page = totalPage;
    }
    
  //■ << 표시 여부
	if(curPage > 1){
		str += "<li><a class='tooltip-top' title='처음'><i class='fas fa-angle-double-left'></i></a></li>\n";
	}
	
  	//■  < 표시 여부
    if (start_page > 1) 
    	str += "<li><a onclick='loadPage(" + (start_page - 1) + ")' class='tooltip-top' title='이전'><i class='fas fa-angle-left'></i></a></li>\n";
    
    //■  페이징 안의 '숫자' 표시	
	if (totalPage > 1) {
	    for (var k = start_page; k <= end_page; k++) {
	        if (curPage != k)
	            str += "<li><a onclick='loadPage(" + k + ")'>" + k + "</a></li>\n";  // k번째 페이지로 이동
	        else
	            str += "<li><a class='active tooltip-top' title='현재페이지'>" + k + "</a></li>\n";
	    }
	}
	
	//■ > 표시
    if (totalPage > end_page){
    	str += "<li><a onclick='loadPage(" + (end_page + 1) + ")' class='tooltip-top' title='다음'><i class='fas fa-angle-right'></i></a></li>\n";
    }

	//■ >> 표시
    if (curPage < totalPage) {
        str += "<li><a onclick='loadPage(" + totalPage + ")' class='tooltip-top' title='맨끝'><i class='fas fa-angle-double-right'></i></a></li>\n";
    }

    return str;


	
} // end buildPagination()


function changePageRows(){
	window.pageRows = $("#rows").val(); 
	loadPage(window.page);
}



//새 글 등록 처리
function chkWrite(){
	
	var data = $("#frmWrite").serialize(); // form 의 모든 name 들을 다 끌고 들어옴.
	
	// ajax request 
	$.ajax({
		url : "writeOk.do",
		type : "post",
		cache : false,
		data : data, 
		dataType : "JSON",
		success : function(data, status){
			if(status == "success"){ // 코드 200 
				if(data.status == "OK"){ // 정상적으로 file 저장
					alert("글 등록 성공!");
				} else{
					alert("글 등록에 실패하였습니다.");
				}
			}
		}
	});
	// request 후, form 에 입력된 것 reset()
	$('#frmWrite')[0].reset();  // 특정 object 만 reset 하는 애이다. 
	// 제이쿼리 는 여러개의 오브젝트를 반환한다. 그래서  제이쿼리 셀렉터에서 특정 애 하나 꺼내어서 reset 해줘야한다.
	
	return true;  // 페이지 리도딩은 안 할 것이다.
} // end chkWrite()


// check 된 uid 의 게시글들만 삭제하기
function chkDelete(){
	var uids = []; // 빈 배열 준비 
	$("#list tbody input[name=uid]").each(function(){  // 각각에 대해 이 함수 수행하겠다.
		// $(this) //각각 name이 uid인 checkbox 
		if($(this).is(":checked")){  // jQuery 에서 check 여부 확인방법 
			uids.push($(this).val()); // 배열에 uid 값 추가 
		}
	});
	
	//alert(uids); 확인용
	
	// 검증하기 _ 하나라도 체크 안되어 있다면
	if(uids.length == 0){
		alert('삭제할 글을 체크해주세요');
	} else{
		if(!confirm(uids.length + "개의 글을 삭제하시겠습니까?")) return false; // 아니오 선택시 false 리턴하도록.
		
		var data = $("#frmList").serialize();
		//  uid=1919&uid=1010&uid=8888
		
		$.ajax({
			url : "deleteOk.ajax",
			type : "post",
			data : data,
			cache : false,
			success : function(data, status){
				// DML 수행시 count, status 결과로 받음
				if(status == "success"){
					if(data.status == "OK"){
						alert("DELETE 성공" + data.count + "개");
						// 현재 페이지 리로딩
						loadPage(window.page);
					}else{
						alert("DELETE 실패" + data.message);
					}
				}
			}
		});
	}
	
	
} // end chkDelete()



// 현재 글 목록 list 에 대해 이벤트 등록
// - 제목(subject) 클릭하면 view 팝업화면 뜰 수 있게 하기
function addViewEvent(){
	$("#list .subject").click(function(){
		
		// 읽어오기
		$.ajax({
			url : "view.do?wkey=" + $(this).attr('data-wkey'),
			type : "GET",
			cache : false,
			dataType : "JSON",
			success : function(data, status){
				if(status == "success"){
					if(data.status == "OK"){
						
						// 읽어온 view 데이터를 전역변수에 세팅
						viewItem = data;
						
						// 팝업에 보여주기
						setPopup("view");
						$("#dlg_write").show();
						
 					} else {
 						alert("VIEW 실패 " + data.message);
 					}
				}
			}
		});
		
		
	});
	
} // end addViewEvent()


function setPopup(mode){
	
	// 글작성
	if(mode == 'write'){
		$('#frmWrite')[0].reset(); // -> form 안의 기존 내용 reset
		$('#dlg_write .title').text("새글작성");
		$('#dlg_write .btn_group_header').hide();
		$('#dlg_write .btn_group_write').show();
		$('#dlg_write .btn_group_view').hide();
		$('#dlg_write .btn_group_update').hide();
		
		$("#dlg_write input[name='subject']").attr("readonly", false);
		$("#dlg_write input[name='subject']").css("border", "1px solid #ccc");

		$("#dlg_write input[name='name']").attr("readonly", false);
		$("#dlg_write input[name='name']").css("border", "1px solid #ccc");

		$("#dlg_write textarea[name='content']").attr("readonly", false);
		$("#dlg_write textarea[name='content']").css("border", "1px solid #ccc");
	}
	
	// 글읽기
	if(mode == 'view'){
		$('#dlg_write .title').text("글읽기");
		$('#dlg_write .btn_group_header').show();
		$('#dlg_write .btn_group_write').hide();
		$('#dlg_write .btn_group_view').show();
		$('#dlg_write .btn_group_update').hide();
		
		$("#dlg_write input[name='wkey']").val(viewItem.wkey);  // 나중에 삭제/수정을 위해 필요
		
		$("#dlg_write input[name='subject']").val(viewItem.subject);
		$("#dlg_write input[name='subject']").attr("readonly", true);
		$("#dlg_write input[name='subject']").css("border", "solid", "0.3px");

		$("#dlg_write input[name='name']").val(viewItem.name);
		$("#dlg_write input[name='name']").attr("readonly", true);
		$("#dlg_write input[name='name']").css("border", "none");

		$("#dlg_write textarea[name='content']").val(viewItem.content);
		$("#dlg_write textarea[name='content']").attr("readonly", true);
		$("#dlg_write textarea[name='content']").css("border", "none");
	}

	// 글수정
	if(mode == 'update'){
		$('#dlg_write .title').text("글 수정");
		$('#dlg_write .btn_group_header').show();
		$('#dlg_write .btn_group_write').hide();
		$('#dlg_write .btn_group_view').hide();
		$('#dlg_write .btn_group_update').show();
		
		$("#dlg_write input[name='subject']").attr("readonly", false);
		$("#dlg_write input[name='subject']").css("border", "1px solid #ccc");

		$("#dlg_write input[name='name']").attr("readonly", true);  // 이름은 수정 불가능 하도록  설정 

		$("#dlg_write textarea[name='content']").attr("readonly", false);
		$("#dlg_write textarea[name='content']").css("border", "1px solid #ccc");
	}
	

	
} // end setPopup()

// 특정 uid 의 글 삭제하기
function deleteUid(uid){
	
	if(!confirm(uid + "글을 삭제 하시겠습니까?")) return false;
	
	// POST 방식
	$.ajax({
		url: "deleteOk.ajax",
		type: "post",
		data: "uid=" + uid,
		cache: false,
		success: function(data, status){
			if(status == "success"){
				if(data.status == "OK"){
					alert("DELETE 성공!" + data.count + "개");
					loadPage(page); // 현재 페이지 리로딩   page 주거나 window.page 해도 된다. 
				} else{
					alert("DELETE 실패 " + data.message);
					return false;
				}
			}
		}
	});
	
	return true;  
} // end deleteUid()


// 글 수정 
function chkUpdate(){
	
	var data = $("#frmWrite").serialize();
	$.ajax({
		url: "updateOk.ajax",
		type: "post",
		cache: false,
		data : data,
		success: function(data, status){
			if(status == "success"){
				if(data.status == "OK"){
					alert("UPDATE 성공" + data.count + "개:" + data.status);
					// 성공한 경우 현재 페이지 리로딩
					loadPage(window.page);  
				} else{
					alert("UPDATE 실패" + data.status + " : " + data.message);
					
				}
				$("#dlg_write").hide();  //현재 팝업 닫기.
			}
		}
	});
	
} // end chkUpdate




