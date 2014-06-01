<?php 
header('Content-type: application/json; charset=utf-8'); // принудительно использовать кодировку UTF-8
require "../include/mysql_connect.php"; // Содержит инструкции подключения к БД
require "../include/json_encode.php"; // Содержит исправленную реализацию функции json_encode()

$faculty_id=$_GET['faculty_id']; // ID факультета берётся из параметра GET-запроса
if($faculty_id){ // Проверка, что ID передан
	$query="SELECT group_id, group_name FROM groups WHERE faculty_id=$faculty_id"; //SQL запрос
	$result = mysql_query($query) or die('{"error": "'.mysql_error().'"}'); 
	if($result){
		while($row=mysql_fetch_assoc($result)){
			$output[]=$row; // формирование массива
		}
		echo '{"groups":'.json_encode_utf8($output).'}';
	}
	else echo '{"error": "Ошибка при выполнении запроса к MySQL"}';
}
else echo '{"error": "Не передан ID факультета"}';

mysql_close();
?>	
