<?php 
header('Content-type: application/x-javascript; charset=utf-8');
require "../include/mysql_connect.php";


$res=mysql_query("CREATE TABLE IF NOT EXISTS faculties(faculty_id INTEGER PRIMARY KEY, faculty_name TEXT)") or die(mysql_error()); 
if($res) echo "console.log('создана таблица faculties')";
$res=mysql_query("TRUNCATE TABLE faculties") or die(mysql_error()); 
if($res) echo "console.log('очищена таблица faculties')";

$res=mysql_query("CREATE TABLE IF NOT EXISTS groups (group_id INTEGER PRIMARY KEY, group_name TEXT, faculty_id INTEGER)") or die(mysql_error()); 
if($res) echo "console.log('создана таблица groups')";
$res=mysql_query("TRUNCATE TABLE groups") or die(mysql_error()); 
if($res) echo "console.log('очищена таблица groups')";

$res=mysql_query("CREATE TABLE IF NOT EXISTS schedule(group_id INTEGER PRIMARY KEY, html_format TEXT, JSON_format TEXT)") or die(mysql_error()); 
if($res) echo "console.log('создана таблица schedule')";
$res=mysql_query("TRUNCATE TABLE schedule") or die(mysql_error()); 
if($res) echo "console.log('очищена таблица schedule')";


mysql_close();

echo "console.log('все старые записи зачищены, а недостающие таблицы созданы')";
 ?>