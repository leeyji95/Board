<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>게시판</title>
</head>
<script	src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<script src="${pageContext.request.contextPath }/JS/Board.js"></script>
<title>게시판</title>
<style>
/* 글 목록 */
#list table{width: 100%;}

#list table, #list th, #list td{
	border: 1px solid olive;
	border-collapse: collapse;
}
#list th, #list td{
	padding: 10px;
}

#list .subject:hover{
	text-decoration: underline;
	color: orange;
	cursor: pointer;
}
/* 기본 버튼 */
.btn {
  border: none;
  color: white;
  padding: 14px 28px;
  font-size: 16px;
  cursor: pointer;
}

.success {background-color: #4CAF50;} /* Green */
.success:hover {background-color: #46a049;}

.info {background-color: #2196F3;} /* Blue */
.info:hover {background: #0b7dda;}

.warning {background-color: #ff9800;} /* Orange */
.warning:hover {background: #e68a00;}

.danger {background-color: #f44336;} /* Red */  
.danger:hover {background: #da190b;}

.default {background-color: #e7e7e7; color: black;} /* Gray */ 
.default:hover {background: #ddd;}



.clear { clear: both; }


.left{
	float : left;
}

.right {
	float : right;
}

/* 페이징 */
.center {
    text-align: center;
}

ul.pagination{
	list-style-type:none
}

ul.pagination li{
    display: inline-block;
}

ul.pagination a {
    color: black;
    float: left;
    padding: 4px 8px;
    text-decoration: none;
    transition: background-color .3s;
    /* border: 1px solid #ddd; */
    /* margin: 0 4px; */
    margin: 0px;
}

ul.pagination a.active {
    background-color: #4CAF50;
    color: white;
    border: 1px solid #4CAF50;
}

ul.pagination a:hover:not(.active) {background-color: #ddd;}


/* 버튼 그룹 */
.d01{
	margin: 5px 0px;
}

/* 모달 팝업 */
.modal {   /* 모달 전체 적용 */	
	background-color: rgba(0, 0, 0, 0.4);
	width: 100%;
	height: 100%;
	position : fixed;
	top: 0;
	left: 0;
	z-index: 1;
	padding-top: 40px;  /* 내부여백  */
	overflow: auto;
	
	display : none;  /* 기본적으로 안보이기 */  
}

.modal .modal-content {
	background-color: #fefefe;  /* 배경은 흰색 */
	width: 80%;   /* 화면대비 80% */
	margin: 5% auto 15% auto;  /* 위에서 5%,  아래에서 15%, 좌우 중앙정렬 */
	border: 1px solid #888;  /* 테두리 */
}

.modal .container {
	padding: 16px;
	position: relative;  /* 이래야 안에 있는 absolute 들이 동작 */
} 

.modal .close {  /* close 버튼 */
	font-size: 35px;	
	font-weight: bold;
	color: #000;
	position: absolute;
	right: 25px;
	top: 0px;
}

.modal .close:hover,
.modal .cloas:focus {
	color: red;
	cursor: pointer;
}


.modal input[type=text] {
	width: 100%;
	border: 1px solid #ccc;
	margin: 8px 0;
	padding: 12px 20px;
	display: inline-block;
	border: 1px solid #ccc;
	box-sizing: border-box;
}

.modal textarea {
	width: 100%;
	margin: 8px 0;
}

.modal .fullbtn {
	width: 100%;
	cursor: pointer;
}
</style>
</head>
<body>
	<h2>파일로 저장하는 게시판</h2>
	<%-- 글목록 --%>
	<div id="list">
		<div class="d01">
			<div class="left" id="pageinfo"></div>
			<div class="right" id="pageRows"></div>
		</div>

		<div class="clear"></div>
	
		<form id="frmList" name="frmList">
			<table>
				<thead>
					<th>선택</th>
					<th>글번호</th>
					<th>제목</th>
					<th>작성자</th>
					<th>날짜</th>
				</thead>

				<tbody>
				</tbody>
			</table>
		</form>


		<%--버튼 --%>
		<div class="d01">
			<div class="left">
				<button type="button" id="btnDel" class="btn danger">글삭제</button>
			</div>
			<div class="right">
				<button type="button" id="btnWrite" class="btn success">글작성</button>
			</div>
		</div>
	</div>

			<%-- 페이징 --%>
		<div class="center">
			<ul class="pagination" id="pagination">
			</ul>
		</div>
	
	<%--글작성 / 보기/ 수정 대화상자 --%>
	<div id="dlg_write" class="modal">

		<form class="modal-content animate" id="frmWrite" name="frmWrite" method="post">
			<div class="container">
				<h3 class="title">새글 작성</h3>

				<span class="close" title="Close Modal">&times;</span> 

				<input type='hidden' name='wkey' >
				<div class='d01 btn_group_header'>
					<div class='left'>
						<p id='viewcnt'></p>
					</div>				
					<div class='right'>
						<p id='regdate'></p>
					</div>	
					<div class='clear'></div>			
				</div>	
				
				<label for="subject"><b>글제목</b></label> 
				<input type="text" placeholder="글제목(필수)" name="subject" required> 
				
				<label for="name"><b>작성자</b></label> 
				<input type="text" placeholder="작성자(필수)" name="name" required> 
				
				<label for="content"><b>내용</b></label>
				<textarea placeholder="글내용" name="content"></textarea>
					
				<div class='d01 btn_group_write'>
					<button type="submit" class="btn success">작성</button>
				</div>

				<div class='d01 btn_group_view'>
					<div class='left'>
						<button type='button' class='btn danger' id='viewDelete'>삭제</button>
					</div>
					<div class='right'>
						<button type='button' class='btn info' id='viewUpdate'>수정</button>
					</div>
					
					<div class='clear'></div>
				</div>
				
				<div class='d01 btn_group_update'>
					<div>
						<button type='button' class='btn info fullbtn' id='updateOk'>수정완료</button>
					</div>
					
				</div>
				
			</div>
		</form>
	</div>


</body>
</html>


