<?php 
header('Content-type: application/x-javascript; charset=utf-8');
require "../include/mysql_connect.php";

$i=0;
foreach ($_GET as $id=>$name){
	if($id!="_"){
		$query="INSERT INTO faculties(faculty_id, faculty_name) VALUES($id, '$name')";
		//echo "console.log(\"$query\");\n";
		$res=mysql_query($query) or die(mysql_error()); 
		$i++;
	}
}

mysql_close();

echo "console.log('добавлено $i запесей в таблицу faculties')";
 ?>