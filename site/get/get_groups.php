<?php 
header('Content-type: application/json; charset=utf-8');
require "../include/mysql_connect.php";
require "../include/json_encode.php";

$faculty_id=$_GET['faculty_id'];
if($faculty_id){
	$query="SELECT group_id, group_name FROM groups WHERE faculty_id=$faculty_id";
	$result = mysql_query($query) or die('{"error": "'.mysql_error().'"}'); 
	if($result){
		while($row=mysql_fetch_assoc($result)){
			$output[]=$row;
		}
		echo '{"groups":'.json_encode_utf8($output).'}';
	}
	else echo '{"error": "Ошибка при выполнении запроса к MySQL"}';
}
else echo '{"error": "Не передан ID факультета"}';

mysql_close();
?>	