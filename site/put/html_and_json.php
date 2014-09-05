<?php 
// это уже HTML страница. Она открывается в новом окне в конце выполнения скрипта. Данные передаются через POST
header('Content-type: text/html; charset=utf-8');
require "../include/mysql_connect.php";

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
			//echo $j;
		}
		else{
			echo "Обработка данных прекращена из-за расхождения ключей!<br/> Ошибка возникла на паре ключей: '$idHTML' - '$idJSON'";
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