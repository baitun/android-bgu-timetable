<?php 
header('Content-type: application/x-javascript; charset=utf-8');
require "../include/mysql_connect.php";

$query="CREATE TABLE IF NOT EXISTS groups (group_id INTEGER, group_name TEXT, faculty_id INTEGER)";
$res=mysql_query($query) or die(mysql_error()); 

$faculty_id=$_GET["faculty_id"];
$query="DELETE FROM `groups` WHERE faculty_id=$faculty_id";
$res=mysql_query($query) or die(mysql_error()); 

$i=0;
foreach ($_GET as $id=>$name){
	if($id!="faculty_id" && $id!="_"){
		$query="INSERT INTO groups(group_id, group_name, faculty_id) VALUES($id, '$name', $faculty_id)";
		//echo "console.log(\"$query\");\n";
		$res=mysql_query($query);// or die(mysql_error()); 
		$i++;
	}
}

mysql_close();

echo "console.log('добавлено $i запесей в таблицу groups')";
 ?>