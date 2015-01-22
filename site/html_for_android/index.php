<?php 
header('Content-type: text/html; charset=utf-8');
require "../include/mysql_connect.php";
$groupID=$_GET['group_id'];
$groupName;
if($groupID){
	$query="SELECT group_name FROM groups WHERE group_id=$groupID";
	$res_group_name = mysql_query($query) or die("Error in '$query'<br>".mysql_error());
	$assoc=mysql_fetch_assoc($res_group_name);
	$groupName=$assoc["group_name"];

	$query="SELECT HTML_format FROM schedule WHERE group_id=$groupID";
	$res = mysql_query($query) or die("Error in '$query'<br>".mysql_error()); 
}
mysql_close();
?>

<!doctype html>
<html lang="ru">
<head>
	<meta charset="UTF-8">
	<title><?php echo "Расписание ".$groupName ?></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=yes"/>
	<!-- <link href="/include/css/android.css" rel="stylesheet" type="text/css" /> -->
	<style><?php include "../include/css/android.css" ?></style>	
	<script>
		var today = new Date();
		var day_of_week=today.getDay();
		var date_start= new Date(2015, 0, 12);
		var date_end = new Date(2015, 2, 22);
		var week = Math.ceil((today-date_start)/1000/60/60/24/7);
		var all_weeks=Math.ceil((date_end-date_start)/1000/60/60/24/7);
	</script>
</head>
<body>
<div id="main">	
<?php 
if($res){
	$row=mysql_fetch_array($res); //первая строка результатов в виде ассоциативного массива
	$schedule=$row['HTML_format'];
	//Собственно, вывод самого расписания из БД:
	if($schedule) echo $schedule;
	else echo "<h2>Нет расписание для группы $groupName</h2>";
}
else {
	echo "<h1>ОШИБКА</h1>";
}
 ?>	
 	<div class="date-info">
		<b id="parity"></b>
	</div>
</div>

<script><?php include "../include/js/jquery.min.js" ?></script>

<script>
$( document ).ready(function() {
	if(week>0 && week <= all_weeks){
		if(week%2==0){
			document.getElementById("parity").innerHTML="Неделя чётная ("+week+"/"+all_weeks+")";
			$('.odd1').parent().css({'opacity': 0.5});
		}
		else {
			document.getElementById("parity").innerHTML="Неделя нечётная ("+week+"/"+all_weeks+")";
			$('.odd2').parent().addClass("this-week");
		}

		var weekdays=["воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота"];
		$("tr[id^='MainContent_LVrez_trr_']>td").each(function(){
			if($(this).text()==weekdays[day_of_week]){
				$(this).addClass('today');
				$('html, body').animate({
	                scrollTop: $(this).offset().top
	            }, 1000); 
			}
		});
	}
	else if(week<1){
		document.getElementById("parity").innerHTML="Триместр ещё не начался";	
	}
	else if(week>all_weeks){
		document.getElementById("parity").innerHTML="Учебный триместр закончился.<br>Попробуйте обновить расписание, для получения расписания сессии или расписания на следующий триместр";
	}
});
</script>
</body>
</html>