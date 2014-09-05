<?php 
header('Content-type: application/x-javascript; charset=utf-8');
require "../include/mysql_connect.php";

$faculty_id=$_GET["faculty_id"];

$i=0;
foreach ($_GET as $id=>$name){
	if($id!="faculty_id" && $id!="_"){
		$query="INSERT INTO groups(group_id, group_name, faculty_id) VALUES($id, '$name', $faculty_id)";
		//echo 'console.log("$query");\n';
		$res=mysql_query($query) or die(mysql_error()); 
		if(!$res) echo "console.log('Error in group_id=$id')";
		$i++;
	}
}

mysql_close();

echo "console.log('добавлено $i запесей в таблицу groups')";
 ?>