<?php 
header('Content-type: text/html; charset=utf-8');
require "./include/mysql_connect.php";
$groupID=$_GET['group'];
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
	<link href="/include/css/isea.css" rel="stylesheet" type="text/css" />
</head>
<body>
<div id="main">
<?php 
if($res){
	echo "<h1>$groupName</h1>";

	$row=mysql_fetch_array($res); //первая строка результатов в виде ассоциативного массива
	echo $row['HTML_format'];
	
}
else echo "<h1>ОШИБКА</h1>";
 ?>	
</div>

</body>
</html>