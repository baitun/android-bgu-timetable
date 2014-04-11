<?php 
header('Content-type: text/html; charset=utf-8');
require "../include/mysql_connect.php";

$query="CREATE TABLE IF NOT EXISTS faculties(faculty_id INTEGER, faculty_name TEXT)";
$res=mysql_query($query) or die(mysql_error()); 

//почистим все таблицы
$query="TRUNCATE TABLE faculties";
$res=mysql_query($query) or die(mysql_error()); 

$i=0;
foreach ($_GET as $id=>$name){
	if($id!="_"){
		$query="INSERT INTO faculties(faculty_id, faculty_name) VALUES($id, '$name')";
		echo "console.log(\"$query\");\n";
		$res=mysql_query($query) or die(mysql_error()); 
		$i++;
	}
}

mysql_close();

echo "console.log('добавлено $i запесей в таблицу faculties')";
 ?>