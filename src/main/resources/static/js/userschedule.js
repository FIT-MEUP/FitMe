document.addEventListener('DOMContentLoaded', function () {
           // DOM 요소 가져오기
           var mainCalendarEl = document.getElementById('calendar');
           var miniCalendarEl = document.getElementById('mini-calendar');
           var miniCalendarWrapper = document.getElementById('mini-calendar-wrapper');
           var monthPlaceholder = document.getElementById('month-placeholder');
           var ptButton = document.getElementById('pt-button');
           var ptInfo = document.getElementById('pt-info');

           // 미니 캘린더 설정
           var miniCalendar = new FullCalendar.Calendar(miniCalendarEl, {
               initialView: 'dayGridMonth',
               selectable: true,
               height: '300px',
               headerToolbar: false,
               dateClick: function (info) {
                   mainCalendar.changeView('timeGridWeek', info.dateStr);
               }
           });

           // 메인 캘린더 설정
           var mainCalendar = new FullCalendar.Calendar(mainCalendarEl, {
               customButtons: {
                   logout: {
                       text: '로그아웃',
                       click: function () {
                           alert('로그아웃 버튼 클릭됨');
                       }
                   },
                   personalInfo: {
                       text: '개인정보',
                       click: function () {
                           alert('개인정보 버튼 클릭됨');
                       }
                   }
               },
               headerToolbar: {
                   left: 'prev,next today',
                   center: 'title',
                   right: 'logout personalInfo dayGridMonth,timeGridWeek,timeGridDay'
               },
               initialDate: new Date(),
               initialView: 'dayGridMonth',
               navLinks: true,
               selectable: true,
               selectMirror: true,
               editable: true,
               dayMaxEvents: true,
               eventColor: '#3788d8',
               dateClick: function (info) {
                   if (mainCalendar.view.type === 'dayGridMonth') {
                       mainCalendar.changeView('timeGridWeek', info.dateStr);
                   }
               },
               // week/day view에서 드래그로 이벤트 추가하는 기능을 아래 views 옵션에서 비활성화합니다.
               select: function (info) {
                   // month view 등 다른 view에서는 그대로 동작하도록 유지
                   if (mainCalendar.view.type !== 'timeGridWeek' && mainCalendar.view.type !== 'timeGridDay') {
                       var newEventData = {
                           title: "New Event",
                           start: info.startStr,
                           end: info.endStr,
                           allDay: info.allDay
                       };
                       var addedEvent = mainCalendar.addEvent(newEventData);
                       sendEventToServer(newEventData, addedEvent);
                   }
                   mainCalendar.unselect();
               },
               // week/day view에서 이벤트 클릭 시 삭제
               eventClick: function (info) {
                   if (mainCalendar.view.type === 'timeGridWeek' || mainCalendar.view.type === 'timeGridDay') {
                       if (confirm('이 이벤트를 삭제하시겠습니까?')) {
                           var eventData = {
                               title: info.event.title,
                               start: info.event.startStr,
                               end: info.event.endStr,
                               allDay: info.event.allDay
                           };
                           deleteEventFromServer(eventData);
                           info.event.remove();
                       }
                   }
               },
               // week/day view에서 all-day 슬롯 제거
               allDaySlot: false,
               views: {
                   timeGridWeek: {
                       allDaySlot: false,
                       selectable: false // week view에서 드래그(선택) 비활성화
                   },
                   timeGridDay: {
                       allDaySlot: false,
                       selectable: false // day view에서 드래그(선택) 비활성화
                   }
               },
               events: [
                   { title: 'All Day Event', start: '2023-01-01' },
                   { title: 'Long Event', start: '2023-01-07', end: '2023-01-10' },
                   { title: '예시', start: '2025-01-07T00:00:00', end: '2025-01-07T02:00:00' },
                   { groupId: 999, title: 'Repeating Event', start: '2023-01-09T16:00:00' },
                   { groupId: 999, title: 'Repeating Event', start: '2023-01-16T16:00:00' },
                   { title: 'Conference', start: '2023-01-11', end: '2023-01-13' },
                   { title: 'Meeting', start: '2023-01-12T10:30:00', end: '2023-01-12T12:30:00' },
                   { title: 'Lunch', start: '2023-01-12T12:00:00' },
                   { title: 'Meeting', start: '2023-01-12T14:30:00' },
                   { title: 'Happy Hour', start: '2023-01-12T17:30:00' },
                   { title: 'Dinner', start: '2023-01-12T20:00:00' },
                   { title: 'Birthday Party', start: '2023-01-13T07:00:00' },
                   { title: 'Click for Google', url: 'http://google.com/', start: '2023-01-28' }
               ],
               datesSet: function (info) {
                   var viewType = info.view.type;
                   if (viewType === 'dayGridMonth') {
                       miniCalendarWrapper.style.display = 'none';
                       monthPlaceholder.style.display = 'none';
                       // month view: pt-button은 숨기고 pt-info 표시
                       ptButton.style.display = 'none';
                       ptInfo.style.display = 'block';
                   } else if (viewType === 'timeGridWeek' || viewType === 'timeGridDay') {
                       miniCalendarWrapper.style.display = 'block';
                       monthPlaceholder.style.display = 'none';
                       ptButton.style.display = 'block';
                       ptInfo.style.display = 'none';
                       ptButton.textContent = "일정 선택";
                       miniCalendar.updateSize();
                   }
               }
           });

           mainCalendar.render();
           miniCalendar.render();

           // ajax로 이벤트 데이터를 서버에 전송하는 함수 (예제)
           function sendEventToServer(eventData, eventInstance) {
               $.ajax({
                   url: '/temporary-url', // Spring 서버 URL (임시)
                   type: 'GET',
                   data: eventData,
                   dataType: 'json',
                   success: function (response) {
                       eventInstance.setProp('title', response.title);
                       eventInstance.setStart(response.start);
                       eventInstance.setEnd(response.end);
                       eventInstance.setAllDay(response.allDay);
                   },
                   error: function () {
                       eventInstance.remove();
                   }
               });
           }

           function deleteEventFromServer(eventData) {
               // 필요시 삭제 AJAX 요청 구현
           }
       });