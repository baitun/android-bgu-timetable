<?php 
header('Content-type: text/html; charset=utf-8');
require "./include/mysql_connect.php";
$groupID=$_GET['group'];
if($groupID){
	$query="SELECT `HTML_format` FROM `schedule` WHERE `group_id`=$groupID";
	$res = mysql_query($query) or die(mysql_error()); 
}
mysql_close();
?>

<!doctype html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title><?php echo "Расписание ".$groupName ?></title>
</head>
<body>

<?php 
if($res){
	//TODO тут всего одно запись. Надо найти как её без while вывести
	while ($row=mysql_fetch_array($res)) {
		echo $row['HTML_format'];
	}
}
else echo "<h1>ОШИБКА</h1>";
 ?>	

</body>
</html>