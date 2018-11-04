// ==UserScript==
// @name Time Table
// @description Get time table from bgu.ru
// @author Savin Yurii
// @version 0.01
// @include http://bgu.ru/help/timetable/timetable.aspx?getTable
// ==/UserScript==

//Использовать как userscript

//Оборачиваем скрипт в замыкание, для кроссбраузерности (opera, ie)
(function (window, undefined) {
  // нормализуем window
  var w;
  if (typeof unsafeWindow != undefined) {
    w = unsafeWindow;
  } else {
    w = window;
  }

  // не запускаем скрипт во фреймах
  if (w.self != w.top) {
    return;
  }

  var modeUpdate = w.location.href === 'http://bgu.ru/help/timetable/timetable.aspx?update';
  var modeRewrite = w.location.href === 'http://bgu.ru/help/timetable/timetable.aspx?getTable';
  // дополнительная проверка наряду с @include
  if (modeRewrite || modeUpdate) {
    addJQueryAndStart(mainFunction);
  }

  // a function that loads jQuery and calls a callback function when jQuery has finished loading
  function addJQueryAndStart(callback) {
    var script = document.createElement('script');
    script.setAttribute('src', '//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js');
    script.addEventListener(
      'load',
      function () {
        var script = document.createElement('script');
        script.textContent = '(' + callback.toString() + ')();';
        document.body.appendChild(script);
      },
      false
    );
    document.body.appendChild(script);
  }

  function mainFunction() {
    // непосредственно код скрипта

    //для работы с ajax понадобится jquery
    // var e = document.createElement('script');
    // e.src = "//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js";
    // document.getElementsByTagName('head')[0].appendChild(e);
    if (typeof $ == 'undefinded') {
      alert('Нет подключена jquery!');
      return;
    }

    var baseURL = 'http://irkvuz.ru/isea/timetable/';
    var w = window;

    // select'ы в форме
    var f = document.getElementById('MainContent_DDLfaculty');
    var g = document.getElementById('MainContent_DDLgroups');

    var currentGroupID = g.selectedOptions[0].value;
    var currentGroupName = g.selectedOptions[0].text;
    var currentFacultyID = f.selectedOptions[0].value;
    var currentFacultyName = f.selectedOptions[0].text;
    var selectedFacultyIndex = f.selectedIndex;
    var selectedGroupIndex = g.selectedIndex;

    if (selectedFacultyIndex == 0 && selectedGroupIndex == 0) {// скрипт сработал первый раз (на первой странице)
      var tstWindow = w.open('', '_blank');
      if (!tstWindow) { //проверка на блокировку всплывающих окон
        alert('Для работы скрипта нужно разрешить показ всплывающих окон. /nПосле разрешения перезагрузить страницу для повторного запуска.');
        return;
      }
      tstWindow.close();

      //очистка всего от предыдущих результатов работы
      localStorage.clear();
      // это лучше делать вручную
      //if(confirm('Очистить БД?')){
      if (true) {
        $.ajax({
          async: false,
          url: baseURL + 'put/clear_all.php',
          dataType: 'script',
          jsonp: false,
          success: function (data, textStatus) {
            console.log(textStatus);
            console.log('DB cleared');
          },
          error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.error('DB not cleared: ' + textStatus);
            return;
          }
        });
      }
    }

    var tableObject = document.getElementById('main').getElementsByTagName('table')[0]; //первая таблица в #main

    var scheduleString, tableString;
    if (tableObject) {
      var trows = tableObject.getElementsByTagName('tr'); // строки таблицы
      var schedule = new Object();
      schedule.group_name = currentGroupName;
      schedule.days = new Array();
      var tmpDay, tmpLesson;
      for (var i = 1; i < trows.length; i++) { //идём по всем строком
        if (trows[i].childElementCount == 1) { // наткнулись на день недели
          if (i > 1) schedule.days.push(tmpDay); //если не первый найденный день, добавить предыдущий в расписание
          tmpDay = new Object();
          switch (trows[i].children[0].innerText) {
            case 'понедельник': tmpDay.weekday = 1; break;
            case 'вторник': tmpDay.weekday = 2; break;
            case 'среда': tmpDay.weekday = 3; break;
            case 'четверг': tmpDay.weekday = 4; break;
            case 'пятница': tmpDay.weekday = 5; break;
            case 'суббота': tmpDay.weekday = 6; break;
            case 'воскресенье': tmpDay.weekday = 7; break;
          }
          tmpDay.lessons = new Array();
        }
        else {
          if (tmpDay.lessons.length > 0 //это не первая пара текущего дня...
            && trows[i].children[0].innerText == trows[i - 1].children[0].innerText // она идёт в то же время...
            && trows[i].children[1].innerText == trows[i - 1].children[1].innerText // той же чётности...
            && trows[i].children[2].innerText == trows[i - 1].children[2].innerText) { // и она совпадает с предыдущей
            //то не создаём новую пару, а добавляем преподавателя и аудиторию к предыдущей
            tmpDay.lessons[tmpDay.lessons.length - 1].teachers.push({ teacher_name: trows[i].children[5].innerText });
            tmpDay.lessons[tmpDay.lessons.length - 1].auditories.push({ auditory_name: trows[i].children[4].innerText });
          }
          else { // а иначе создаём новую пару
            tmpLesson = new Object();
            var sbj = JSON.stringify(trows[i].children[2].innerText); //временная строка с предметом
            tmpLesson.subject = sbj.substr(1, sbj.length - 2); //stringify нужен для экранирования кавычек внутри названия предмета, но он добавляет дополнительно 2 кавычки в начале и в конце, которые надо убрать
            switch (trows[i].children[3].innerText) {
              case 'пр': tmpLesson.type = 0; break; //Практика
              case 'лаб': tmpLesson.type = 1; break; //Лабораторная
              case 'л': tmpLesson.type = 2; break; //Лекция
              case 'конс': tmpLesson.type = 4; break; //Консультация
              case 'вне': tmpLesson.type = 5; break; //Внеучебное занятие
              case 'зач': tmpLesson.type = 6; break; //Зачёт
              case 'экз': tmpLesson.type = 7; break; //Экзамен
              default:
                alert('Ошибка определения типа занятия на строке: ' + i);
                return;
            }
            tmpLesson.time_start = trows[i].children[0].innerText;
            switch (tmpLesson.time_start) {
              case '9:00': tmpLesson.time_end = '10:20'; tmpLesson.time_number = 1; break;
              case '10:35': tmpLesson.time_end = '11:55'; tmpLesson.time_number = 2; break;
              case '12:10': tmpLesson.time_end = '13:30'; tmpLesson.time_number = 3; break;
              case '14:00': tmpLesson.time_end = '15:20'; tmpLesson.time_number = 4; break;
              case '15:35': tmpLesson.time_end = '16:55'; tmpLesson.time_number = 5; break;
              case '17:10': tmpLesson.time_end = '18:30'; tmpLesson.time_number = 6; break;
              case '18:45': tmpLesson.time_end = '20:05'; tmpLesson.time_number = 7; break;
              default:
                alert('Ошибка определения времени на строке: ' + i);
                return;
            }
            switch (trows[i].children[1].className) {
              case 'odd0': tmpLesson.parity = 0; break;
              case 'odd1': tmpLesson.parity = 1; break;
              case 'odd2': tmpLesson.parity = 2; break;
              default:
                alert('Ошибка определения чётности на строке: ' + i);
                return;
            }
            tmpLesson.teachers = [{ teacher_name: trows[i].children[5].innerText }];
            tmpLesson.auditories = [{ auditory_name: trows[i].children[4].innerText }];
            tmpLesson.date_start = null;
            tmpLesson.date_end = null;
            tmpLesson.dates = null;

            tmpDay.lessons.push(tmpLesson);
          }
        }
      }
      schedule.days.push(tmpDay); //добавить последний день
      scheduleString = JSON.stringify(schedule); // перевод JS объекта в JSON строку
      tableString = tableObject.outerHTML; //содержимое таблицы

      // null элементы не записываются
      //К именам ключей дабавляются идентефикаторы
      localStorage[currentGroupID + 'HTML'] = tableString;
      localStorage[currentGroupID + 'JSON'] = scheduleString;
    }
    else { // таблицы может и не быть (нет расписания для этой группы)
      scheduleString = null;
      tableString = null;
      localStorage['_null_elements'] += ', ' + currentGroupName;//записываем группы, для которых нет расписания
      localStorage['_null_elements_count']++; // и посчитаем их
    }



    console.log(f.selectedOptions[0].text + ' ' + g.selectedOptions[0].text); // факультет группа

    if (selectedGroupIndex == 0) {//для (первой группы) каждого факультета добавляем информацию о группах в ДБ

      // передача всех групп этого факультета
      var parametrsArray = ['faculty_id=' + currentFacultyID];
      for (var i = 0; i < g.options.length; i++) {
        parametrsArray.push(g.options[i].value + '=' + g.options[i].text);
      }
      $.ajax({
        url: baseURL + 'put/groups.php',
        type: 'GET',
        data: parametrsArray.join('&'),
        dataType: 'script',
        jsonp: false,
        success: function (data, textStatus) {
          console.log('put groups success');
          console.log(textStatus);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
          console.error('put groups error: ' + textStatus);
          alert('put groups error');
          return;
        }
      });


      // передача 1 текущего факультета
      $.ajax({
        async: false,
        url: baseURL + 'put/faculties.php',
        type: 'GET',
        data: currentFacultyID + '=' + currentFacultyName,
        dataType: 'script',
        jsonp: false,
        success: function (data, textStatus) {
          console.log('put faculty success');
          console.log(textStatus);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
          console.error('put faculty error');
          console.error(textStatus);
          return;
        }
      });
      // alert("Убедимся, что всё передано для факультета "+currentFacultyName)            
    }

    if (selectedGroupIndex < g.options.length - 1) { // если не последняя группа этого факультета
      g.value = g.options[selectedGroupIndex + 1].value; // Слудующая группа
    }
    else { // последняя группа текущего факультета
      if (selectedFacultyIndex < f.options.length - 1) { // и не последний факультет
        f.value = f.options[selectedFacultyIndex + 1].value; // Слудуюий факультет
      }
      else { //последняя группа && последний факультет
        finish();
        return;
      }
    }
    document.forms['form1'].submit(); //выполнится только если !(последняя группа && последний факультет)
    // конец скрипта

    function finish() {
      //alert("Готово!");
      //if(confirm('Передать собранные данные?')){
      if (true) {
        var myForm = document.createElement('form');
        myForm.method = 'post';
        myForm.target = '_blank';
        myForm.action = baseURL + 'put/html_and_json.php';
        for (var i = 0; i < localStorage.length; i++) {
          if (localStorage.key(i)[0] != '_') {//избавляемся от посторонних записей в localStorage
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = localStorage.key(i);
            input.value = localStorage[localStorage.key(i)];
            myForm.appendChild(input);
          }
        }
        myForm.submit();
        console.log('Данные отправлены');
      }
      return;
    }
  }
})(window);