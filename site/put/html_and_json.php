<?php 
header('Content-type: text/html; charset=utf-8');
require "../include/mysql_connect.php";

$CREATE_TABLE="CREATE TABLE IF NOT EXISTS schedule(group_id INTEGER PRIMARY KEY, html_format TEXT, JSON_format TEXT)";
$res=mysql_query($CREATE_TABLE) or die(mysql_error()); 

// $TRUNCATE="TRUNCATE TABLE schedule";
// $res=mysql_query($TRUNCATE) or die(mysql_error()); 

$i=0;// шаги цикла
$j=0;// количество запросов к БД
foreach ($_POST as $id=>$value){
	if(substr($id, -4)=="HTML"){ // проверка последних четырёх символов ключа
		$scheduleHTML=$value;
		$idHTML=substr($id, 0, strlen($id)-4); //хотя все номера в ключах ровно 6 символов, перестраховался и исключаю из строки последние 4 символа
	}
	else if(substr($id, -4)=="JSON"){
		$scheduleJSON=$value;
		$idJSON=substr($id, 0, strlen($id)-4);
		if($idHTML==$idJSON){ // на всякий случай, проверка, чтобы 2 последних group_id были одинаковыми
			$query="INSERT INTO schedule(group_id, HTML_format, JSON_format) VALUES($idHTML, '$scheduleHTML', '$scheduleJSON')";
			$res=mysql_query($query) or die(mysql_error()); 
			if(!$res){
				echo "</br></br>Проблема с SQL на $idHTML<br/> $query";
			}
			$j++;
		}
		else{
			echo "Обработка данных прекращена из-за отсутствия ключа!<br/> Ошибка возникла на паре ключей: '$idHTML' - '$idJSON'";
			exit;
		}
	}
	else{
		echo "Обработка дынных прекращена из-за неправильного имени ключа!<br> Ошибка в ключе $id";
		exit;
	}
	$i++;
}
mysql_close();

echo "Добавлено $j записей за $i шагов";
 ?>