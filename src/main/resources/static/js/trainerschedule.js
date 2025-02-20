document.addEventListener('DOMContentLoaded', function () {
         var mainCalendarEl = document.getElementById('calendar');
         var miniCalendarEl = document.getElementById('mini-calendar');
         var miniCalendarWrapper = document.getElementById('mini-calendar-wrapper');
         var monthPlaceholder = document.getElementById('month-placeholder');

         // 왼쪽 사이드바의 버튼들 및 나중에 글 적을 네모박스
         var scheduleManagementButton = document.getElementById('schedule-management');
         var resetButton = document.getElementById('reset-button');
         var leftPlaceholder = document.getElementById('left-placeholder');

         // 드래그(일정 추가/수정) 활성 여부 및 임시 저장 이벤트 배열
         var editingEnabled = false;
         var pendingEvents = [];

         scheduleManagementButton.addEventListener('click', function () {
             // 토글: 편집 모드 시작(활성) <-> 편집 완료(비활성)
             editingEnabled = !editingEnabled;
             if (editingEnabled) {
                 scheduleManagementButton.textContent = "가능 일정 완료";
                 scheduleManagementButton.classList.add('active');
                 resetButton.style.display = 'block';
             } else {
                 scheduleManagementButton.textContent = "일정관리";
                 scheduleManagementButton.classList.remove('active');
                 resetButton.style.display = 'none';
                 pendingEvents.forEach(function (item) {
                     sendEventToServer(item.data, item.instance);
                 });
                 pendingEvents = [];
             }
         });

         // 초기화 버튼: pendingEvents에 저장된 이벤트들을 캘린더에서 제거
         resetButton.addEventListener('click', function () {
             pendingEvents.forEach(function (item) {
                 item.instance.remove();
             });
             pendingEvents = [];
         });

         // 미니 캘린더 설정 (week/day 뷰용)
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
             eventColor: '#3788d8', // 드래그한 이벤트의 기본 색상
             dateClick: function (info) {
                 if (mainCalendar.view.type === 'dayGridMonth') {
                     mainCalendar.changeView('timeGridWeek', info.dateStr);
                 }
             },
             select: function (info) {
                 // 편집 모드일 때만 이벤트 추가 (화면에 표시 후 임시 저장)
                 if (mainCalendar.view.type === 'timeGridWeek' && editingEnabled) {
                     var newEventData = {
                         title: "New Event",
                         start: info.startStr,
                         end: info.endStr,
                         allDay: info.allDay
                     };
                     var addedEvent = mainCalendar.addEvent(newEventData);
                     pendingEvents.push({ data: newEventData, instance: addedEvent });
                 }
                 mainCalendar.unselect();
             },
             eventClick: function (info) {
                 if (mainCalendar.view.type === 'timeGridWeek' && !editingEnabled) {
                     return;
                 }
                 if (mainCalendar.view.type === 'timeGridWeek') {
                     if (confirm('이 이벤트를 삭제하시겠습니까?')) {
                         var eventData = {
                             title: info.event.title,
                             start: info.event.startStr,
                             end: info.event.endStr,
                             allDay: info.event.allDay
                         };
                         deleteEventFromServer(eventData);
                         info.event.remove();
                         pendingEvents = pendingEvents.filter(function (item) {
                             return item.instance !== info.event;
                         });
                     }
                 }
             },
             eventDrop: function (info) {
                 if (!editingEnabled) {
                     alert('드래그 기능이 비활성화되어 있습니다.');
                     info.revert();
                     return;
                 }
             },
             eventResize: function (info) {
                 if (!editingEnabled) {
                     alert('리사이즈 기능이 비활성화되어 있습니다.');
                     info.revert();
                     return;
                 }
             },
             // week/day view에서 all-day 슬롯 제거
             allDaySlot: false,
             views: {
                 timeGridWeek: {
                     allDaySlot: false
                 },
                 timeGridDay: {
                     allDaySlot: false
                 }
             },
             events: [
                 { title: 'All Day Event', start: '2023-01-01' },
                 { title: 'Long Event', start: '2023-01-07', end: '2023-01-10' },
                 { title: '예시', start: '2023-01-07T00:00:00', end: '2023-01-07T02:00:00' },
                 { groupId: 999, title: 'Repeating Event', start: '2023-01-09T16:00:00' },
                 { groupId: 999, title: 'Repeating Event', start: '2023-01-16T16:00:00' },
                 { title: 'Conference', start: '2023-01-11', end: '2023-01-13' },
                 { title: 'Meeting', start: '2023-01-12T10:30:00', end: '2023-01-12T12:30:00' },
                 { title: 'Lunch', start: '2023-01-12T12:00:00' },
                 { title: 'Meeting', start: '2023-01-12T14:30:00' },
                 { title: 'Happy Hour', start: '2023-01-12T17:30:00' },
                 { title: 'Dinner', start: '2023-01-12T20:00:00' },
                 { title: 'Birthday Party', start: '2023-01-13T07:00:00' },
                 { title: 'Click for Google', url: 'http://google.com/', start: '2023-01-28' },
                 { start: '2025-02-13T17:30:00', end: '2025-02-13T19:30:00' }
             ],
             datesSet: function (info) {
                 var viewType = info.view.type;
                 if (viewType === 'dayGridMonth') {
                     miniCalendarWrapper.style.display = 'none';
                     monthPlaceholder.style.display = 'block';
                     scheduleManagementButton.style.display = 'none';
                     // month view에서는 네모박스 표시
                     leftPlaceholder.style.display = 'block';
                     editingEnabled = false;
                 } else if (viewType === 'timeGridWeek' || viewType === 'timeGridDay') {
                     miniCalendarWrapper.style.display = 'block';
                     monthPlaceholder.style.display = 'none';
                     // week view에서는 네모박스 숨기고 일정관리 버튼 보임
                     leftPlaceholder.style.display = 'none';
                     if (viewType === 'timeGridWeek') {
                         scheduleManagementButton.style.display = 'block';
                         editingEnabled = false;
                         scheduleManagementButton.textContent = "일정관리";
                         scheduleManagementButton.classList.remove('active');
                         resetButton.style.display = 'none';
                     } else {
                         scheduleManagementButton.style.display = 'none';
                         editingEnabled = false;
                     }
                     miniCalendar.updateSize();
                 }
             }
         });

         mainCalendar.render();
         miniCalendar.render();

         function sendEventToServer(eventData, eventInstance) {
             $.ajax({
                 url: '/temporary-url', // Spring 서버 URL
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