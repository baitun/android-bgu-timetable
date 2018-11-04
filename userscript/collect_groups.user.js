// ==UserScript==
// @name Time Table groups
// @description Get time table from bgu.ru
// @author Savin Yurii
// @version 0.01
// @include http://bgu.ru/help/timetable/timetable.aspx?getGroups
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
  if (w.location.href === "http://bgu.ru/help/timetable/timetable.aspx?getGroups") {
    addJQueryAndStart(mainFunction);
  }


  function addJQueryAndStart(callback) {
    // a function that loads jQuery and calls a callback function when jQuery has finished loading

    var script = document.createElement("script");
    script.setAttribute("src", "//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js");
    //script.setAttribute("src", "http://jquery.com/src/jquery-latest.js");
    script.addEventListener(
      'load',
      function () {
        var script = document.createElement("script");
        script.textContent = "(" + callback.toString() + ")();";
        document.body.appendChild(script);
      },
      false
    );
    document.body.appendChild(script);
  }

  function mainFunction() {
    // непосредственно код скрипта

    //для работы с ajax понадобится jquery
    var e = document.createElement('script');
    e.src = "//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js";
    document.getElementsByTagName('head')[0].appendChild(e);
    if (!$) {
      alert("not jquery");
      return;
    }

    var baseURL = "http://irkvuz.ru/isea/timetable/" //"http://test.savinyurii.ru/"
    var w = window;

    // select'ы в форме        
    var f = document.getElementById('MainContent_DDLfaculty');
    var g = document.getElementById("MainContent_DDLgroups");

    var currentGroupID = g.selectedOptions[0].value;
    var currentGroupName = g.selectedOptions[0].text;
    var currentFacultyID = f.selectedOptions[0].value;
    var currentFacultyName = f.selectedOptions[0].text;
    var selectedFacultyIndex = f.selectedIndex;
    var selectedGroupIndex = g.selectedIndex;

    console.log(f.selectedOptions[0].text + " " + g.selectedOptions[0].text); // факультет группа

    if (selectedGroupIndex == 0) {//для (первой группы) каждого факультета добавляем информацию о группа в ДБ

      // передача всех групп этого факультета
      var parametrsArray = ["faculty_id=" + currentFacultyID];
      for (var i = 0; i < g.options.length; i++) {
        parametrsArray.push(g.options[i].value + "=" + g.options[i].text);
      }
      $.ajax({
        url: baseURL + "put/groups.php",
        type: "GET",
        data: parametrsArray.join("&"),
        dataType: "script",
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
    }

    if (selectedFacultyIndex < f.options.length - 1) { // и не последний факультет
      f.value = f.options[selectedFacultyIndex + 1].value; // Слудуюий факультет
    }
    else return;
    document.forms["form1"].submit(); //выполнится только если !(последняя группа && последний факультет)
    // конец скрипта                
  }
})(window);