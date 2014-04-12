// ==UserScript==
// @name Time Table
// @description Get time table from isea.ru
// @author Savin Yurii
// @version 0.01
// @include http://isea.ru/help/timetable/timetable.aspx?getTable
// ==/UserScript==

//Использовать как userscript

//Оборачиваем скрипт в замыкание, для кроссбраузерности (opera, ie)
(function (window, undefined) {  // нормализуем window
    var w;
    if (typeof unsafeWindow != undefined) {
        w = unsafeWindow
    } else {
        w = window;
    }
    
    // не запускаем скрипт во фреймах
    if (w.self != w.top) {
        return;
    }
    // дополнительная проверка наряду с @include
    if (w.location.href==="http://isea.ru/help/timetable/timetable.aspx?getTable") {
        // непосредственно код скрипта

        //для работы с ajax понадобится jquery
        var e = document.createElement('script');
        e.src = "//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js";
        document.getElementsByTagName('head')[0].appendChild(e);

        // select'ы в форме        
        var f=document.getElementById('MainContent_DDLfaculty');
        var g = document.getElementById("MainContent_DDLgroups");

        var currentGroupID = g.selectedOptions[0].value;
        var currentGroupName = g.selectedOptions[0].text;
        var currentFacultyID = f.selectedOptions[0].value;
        var currentFacultyName=f.selectedOptions[0].text;
        var selectedFacultyIndex=f.selectedIndex;
        var selectedGroupIndex=g.selectedIndex;
        
        if(selectedFacultyIndex==0 && selectedGroupIndex==0){// скрипт сработал первый раз (на первой странице)
            tstWindow=w.open("", "_blank");
            if(!tstWindow){ //проверка на блокировку всплывающих окон
                alert("Для работы скрипта нужно разрешить показ всплывающих окон. /nПосле разрешения перезагрузить страницу дя повторного запуска.");
                return;
            }
            tstWindow.close();
            localStorage.clear();
        }
        
        var tableObject=document.getElementById("main").getElementsByTagName("table")[0]; //первая таблица в #main
        
        var scheduleString, tableString;
        if(tableObject){
            var trows=tableObject.getElementsByTagName("tr"); // строки таблицы
            var schedule=new Object();
            schedule.group_name=currentGroupName;
            schedule.days=new Array();
            var tmpDay, tmpLesson;
            for(var i=1; i<trows.length; i++){ //идём по всем строком                
                if(trows[i].childElementCount==1){ // наткнулись на день недели
                    if(i>1) schedule.days.push(tmpDay); //если не первый найденный день, добавить предудущий в расписание
                    tmpDay=new Object();
                    switch(trows[i].children[0].innerText){
                        case "понедельник": tmpDay.weekday=1; break;
                        case "вторник": tmpDay.weekday=2; break;
                        case "среда": tmpDay.weekday=3; break;
                        case "четверг": tmpDay.weekday=4; break;
                        case "пятница": tmpDay.weekday=5; break;
                        case "суббота": tmpDay.weekday=6; break;
                        case "воскресенье": tmpDay.weekday=7; break;
                    }
                    tmpDay.lessons=new Array();
                }
                else {
                    if(tmpDay.lessons.length>0 //это не первая пара текущего дня...
                        && trows[i].children[0].innerText==trows[i-1].children[0].innerText // она идёт в то же время...
                        && trows[i].children[1].innerText==trows[i-1].children[1].innerText // той же чётности...
                        && trows[i].children[2].innerText==trows[i-1].children[2].innerText){ // и она совпадает с предыдущей
                        //то не создаём новую пару, а добавляем преподавателя и аудиторию к предыдущей
                        tmpDay.lessons[tmpDay.lessons.length-1].teachers.push({teacher_name: trows[i].children[5].innerText});
                        tmpDay.lessons[tmpDay.lessons.length-1].auditories.push({auditory_name: trows[i].children[4].innerText});
                    }
                    else{ // а иначе создаём новую пару
                        tmpLesson=new Object();
                        tmpLesson.subject=trows[i].children[2].innerText;
                        switch(trows[i].children[3].innerText){
                            case "пр": tmpLesson.type=0; break; //Практика
                            case "лаб": tmpLesson.type=1; break; //Лабораторная
                            case "л": tmpLesson.type=2; break; //Лекция
                            default: 
                                alert("Ошибка определения типа занятия: "+i);
                                return;
                        }
                        switch(trows[i].children[0].innerText){
                            case  "9:00": tmpLesson.time_number=1; break;
                            case "10:35": tmpLesson.time_number=2; break;
                            case "12:10": tmpLesson.time_number=3; break;
                            case "14:00": tmpLesson.time_number=4; break;
                            case "15:35": tmpLesson.time_number=5; break;
                            case "17:10": tmpLesson.time_number=6; break;
                            case "18:45": tmpLesson.time_number=7; break;
                            default: 
                                alert("Ошибка определения времени: "+i);
                                return;
                        }
                        switch(trows[i].children[1].className){
                            case "odd0": tmpLesson.parity=0; break;
                            case "odd1": tmpLesson.parity=1; break;
                            case "odd2": tmpLesson.parity=2; break;
                            default:
                                alert("Ошибка определения чётности: "+i);
                                return;
                        }
                        tmpLesson.teachers=[{teacher_name: trows[i].children[5].innerText}];
                        tmpLesson.auditories=[{auditory_name:trows[i].children[4].innerText}];                        

                        tmpDay.lessons.push(tmpLesson);
                    }           
                }
            }
            schedule.days.push(tmpDay); //добавить последний день
            scheduleString=JSON.stringify(schedule); // перевод JS объекта в JSON строку
            tableString=tableObject.outerHTML; //содержимое таблицы
		}
        else{ // таблицы может и не быть (нет расписания для этой группы)
            scheduleString=null;
            tableString=null; 
        }
        //К именам ключей дабавляются идентефикаторы
        // !!! Не менять формат вывода! put/html_and_json.php пологается на правильный формат вывода
        localStorage[currentGroupID+"HTML"]=tableString;
        localStorage[currentGroupID+"JSON"]=scheduleString;


        console.log(f.selectedOptions[0].text+" "+g.selectedOptions[0].text); // факультет группа

        if(selectedGroupIndex==0){//для (первой группы) каждого факультета добавляем информацию о группа в ДБ
            var db=openDatabase("info","1.0","",1 * 1024 * 1024);
            if(!db) {
                alert("Не получилось создать БД");
                return;
            };
            db.transaction(function(tx) {                
                tx.executeSql('CREATE TABLE IF NOT EXISTS groups (group_id INTEGER, group_name TEXT, faculty_id INTEGER)');
                for(var i=0;i<g.options.length;i++){                
                    tx.executeSql("INSERT INTO groups (group_id, faculty_id, group_name) values(?, ?, ?)", [g.options[i].value, currentFacultyID, g.options[i].text], null, null);
                }
            });
            
            var parametrsArray=["faculty_id="+currentFacultyID];
            for(var i=0;i<g.options.length;i++){
                parametrsArray.push(g.options[i].value+"="+g.options[i].text);
            }
            $.ajax({
                url: "http://test.savinyurii.ru/put/groups.php",
                type: "GET",
                data: parametrsArray.join("&"),
                dataType: "script",
                jsonp: false,
                success: function(data){
                    //alert("groups success");
                }
            });


            $.ajax({
                    url: "http://test.savinyurii.ru/put/faculties.php",
                    type: "GET",
                    data: currentFacultyID+"="+currentFacultyName,
                    dataType: "script",
                    success: function(data){
                        //alert("faculty_success");
                    },
                    jsonp: false
                });

            /*if(selectedFacultyIndex==0){//для первого факультета
                var db=openDatabase("info","1.0","",1 * 1024 * 1024);
                db.transaction(function(tx) {      
                    tx.executeSql('CREATE TABLE IF NOT EXISTS faculties (faculty_id INTEGER, faculty_name TEXT)');
                    for(var i=0;i<f.options.length;i++){
                        tx.executeSql("INSERT INTO faculties(faculty_id, faculty_name) values(?, ?, ?)", [f.options[i].value, f.options[i].text], null, null);
                    }
                });

                var parametrsFacultiesArray=new Array();
                for(var i=0;i<f.options.length;i++){
                    parametrsFacultiesArray.push(f.options[i].value+"="+f.options[i].text);
                }
                $.ajax({
                    url: "http://test.savinyurii.ru/put/faculties.php",
                    type: "GET",
                    data: parametrsFacultiesArray.join("&"),
                    dataType: "script",
                    success: function(data){
                        //alert("faculty_success");
                    },
                    jsonp: false
                });
            }*/
        }

        if(selectedGroupIndex<g.options.length-1){ // если не последняя группа этого факультета
            g.value=g.options[selectedGroupIndex+1].value; // Слудующая группа
        }
        else { // последняя группа текущего факультета
            if(selectedFacultyIndex<f.options.length-1) {
                f.value=f.options[selectedFacultyIndex+1].value; // Слудуюий факультет
            }
            else{ //последняя группа && последний факультет
                finish();
                return;
            }
        }
        document.forms["form1"].submit(); //выполнится только если !(последняя группа && последний факультет)
        // конец скрипта
        
        function finish(){
            alert("Готово!");
            var myForm=document.createElement("form");
            myForm.method="post";
            myForm.target="_blank";
            myForm.action="http://test.savinyurii.ru/put/html_and_json.php";
            for(var i=0; i<localStorage.length; i++){
                var input=document.createElement("input");
                input.type="hidden";
                input.name=localStorage.key(i);
                input.value=localStorage[localStorage.key(i)];
                myForm.appendChild(input);
            }
            myForm.submit();
            console.log("Данные переданы");
            return;
        }
    }
})(window);