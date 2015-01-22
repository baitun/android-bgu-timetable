<?php 
header('Content-type: application/json; charset=utf-8');
require "../include/mysql_connect.php";
require "../include/json_encode.php";

$query="SELECT faculty_id, faculty_name FROM faculties ORDER BY faculty_name ASC ";
$result = mysql_query($query) or die('{"error": "'.mysql_error().'"}'); 
if($result){
	while($row=mysql_fetch_assoc($result)){
		$output[]=$row;
	}
	echo '{"faculties":'.json_encode_utf8($output).'}';
}
else echo '{"error": "Ошибка запроса"}';

mysql_close();
?>	