<?php 
header('Content-type: application/json; charset=utf-8');
require "../include/mysql_connect.php";
$groupID=$_GET['group_id'];
if($groupID){
	$query="SELECT `JSON_format` FROM `schedule` WHERE `group_id`=$groupID";
	$res = mysql_query($query) or die(mysql_error()); 
	if($res){
		$row=mysql_fetch_array($res);
		echo $row['JSON_format'];
	}
	else echo '{"error": "Ошибка запроса"}';
}
else echo '{"error": "Не передан ID группы"}';

mysql_close();
?>	