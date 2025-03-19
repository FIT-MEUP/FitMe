document.addEventListener('DOMContentLoaded', function () {
    var mainCalendarEl = document.getElementById('calendar');
    var miniCalendarEl = document.getElementById('mini-calendar');
    var miniCalendarWrapper = document.getElementById('mini-calendar-wrapper');
    var monthPlaceholder = document.getElementById('month-placeholder');

    // 식단, 운동 게시판 버튼 클릭 리스너
    document.getElementById("diet-board").addEventListener("click", function(){
        window.location.href = "/meals";
    });
    document.getElementById("exercise-board").addEventListener("click", function(){
        window.location.href = "/work";
    });

    // 오른쪽 영역의 요소들
    var ptButton = document.getElementById('pt-button');
    var deleteButton = document.getElementById('delete-button');
    var ptInfo = document.getElementById('pt-info');
    var scheduleForm = document.getElementById('schedule-form');
    var startDateInput = document.getElementById('start-date');
    var chatBox = document.getElementById('chat-box');

    // 사용자 일정 선택 모드 관련 변수
    var userSelectionMode = false;
    var userCreatedEvents = [];

    function formatLocalDateTime(date) {
        var yyyy = date.getFullYear();
        var MM = ("0" + (date.getMonth() + 1)).slice(-2);
        var dd = ("0" + date.getDate()).slice(-2);
        var hh = ("0" + date.getHours()).slice(-2);
        var mm = ("0" + date.getMinutes()).slice(-2);
        var ss = ("0" + date.getSeconds()).slice(-2);
        return yyyy + "-" + MM + "-" + dd + "T" + hh + ":" + mm + ":" + ss;
    }

    // 기본 뷰를 주간 뷰로 설정 ("timeGridWeek")
    var mainCalendar = new FullCalendar.Calendar(mainCalendarEl, {
        timeZone: 'UTC', // 서버와 DB의 타임존이 UTC라면 그대로 사용
        initialView: 'timeGridWeek',  // 기본 뷰를 주간 뷰로 변경
        eventOverlap: true,
        customButtons: {
            logout: {
                text: '로그아웃',
                click: function () {
                    window.location.href = '/user/logout?userId=' + userId;
                }
            },
            personalInfo: {
                text: '개인정보',
                click: function () {
                    window.location.href = '/userbodyData';
                }
            }
        },
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'logout personalInfo dayGridMonth,timeGridWeek,timeGridDay'
        },
        initialDate: new Date(),
        navLinks: true,
        selectable: true,
        selectMirror: true,
        editable: true,
        dayMaxEvents: true,
        eventColor: '#3788d8',
        dateClick: function(info) {
            if (mainCalendar.view.type === 'dayGridMonth') {
                mainCalendar.changeView('timeGridWeek', info.dateStr);
            }
        },
        select: function(info) {
            mainCalendar.unselect();
        },
        eventClick: function(info) {
            // 일정관리 모드에서만 사용자 생성 이벤트 삭제 가능
            if ((mainCalendar.view.type === 'timeGridWeek' || mainCalendar.view.type === 'timeGridDay') && userSelectionMode && info.event.extendedProps.isUserCreated) {
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
        eventDidMount: function(info) {
            if (info.event.extendedProps.isUserCreated) {
                info.el.classList.add('user-created-event');
                info.el.style.cssText += "color: black !important; z-index: 9999 !important; position: relative !important;";
                var titleEl = info.el.querySelector('.fc-event-title');
                if (titleEl) {
                    titleEl.style.cssText += "color: black !important;";
                }
                var timeEl = info.el.querySelector('.fc-event-time');
                if (timeEl) {
                    timeEl.style.cssText += "color: black !important;";
                }
            } else {
                info.el.style.zIndex = '1';
            }
        },
        eventDrop: function(info) {
            if (!userSelectionMode) {
                alert('드래그 기능이 비활성화되어 있습니다.');
                info.revert();
                return;
            }
            if (!info.event.extendedProps.isUserCreated) {
                alert('기본 이벤트는 이동할 수 없습니다.');
                info.revert();
                return;
            }
        },
        eventResize: function(info) {
            if (!userSelectionMode) {
                alert('리사이즈 기능이 비활성화되어 있습니다.');
                info.revert();
                return;
            }
            if (!info.event.extendedProps.isUserCreated) {
                alert('기본 이벤트는 리사이즈할 수 없습니다.');
                info.revert();
                return;
            }
        },
        allDaySlot: false,
        views: {
            timeGridWeek: { allDaySlot: false, selectable: false },
            timeGridDay: { allDaySlot: false, selectable: false }
        },
        events: calendarEvents,
        datesSet: function(info) {
            var viewType = info.view.type;
            if (viewType === 'dayGridMonth') {
                miniCalendarWrapper.style.display = 'none';
                monthPlaceholder.style.display = 'none';
                ptButton.style.display = 'none';
                ptInfo.style.display = 'block';
                scheduleForm.style.display = 'none';
                chatBox.style.display = 'none';
                deleteButton.style.display = 'none';
            } else if (viewType === 'timeGridWeek' || viewType === 'timeGridDay') {
                miniCalendarWrapper.style.display = 'block';
                monthPlaceholder.style.display = 'none';
                ptButton.style.display = 'block';
                ptInfo.style.display = 'block';
                scheduleForm.style.display = userSelectionMode ? 'block' : 'none';
                chatBox.style.display = 'none';
                if (!userSelectionMode) {
                    ptButton.textContent = "일정 선택 및 삭제";
                    deleteButton.style.display = 'none';
                } else {
                    deleteButton.style.display = 'block';
                }
                miniCalendar.updateSize();
            }
        }
    });

    var miniCalendar = new FullCalendar.Calendar(miniCalendarEl, {
        initialView: 'dayGridMonth',
        selectable: true,
        height: '300px',
        headerToolbar: false,
        dateClick: function(info) {
            mainCalendar.changeView('timeGridWeek', info.dateStr);
        }
    });

    mainCalendar.render();
    miniCalendar.render();

    ptButton.addEventListener('click', function() {
        if (!userSelectionMode) {
            userSelectionMode = true;
            ptButton.textContent = "일정 선택 완료";
            mainCalendarEl.classList.add('selection-mode');
            scheduleForm.style.display = 'block';
            deleteButton.style.display = 'block';
        } else {
            var startStr = startDateInput.value;
            if (!startStr) {
                alert("시작 날짜를 선택해주세요.");
                return;
            }
            var startTime = new Date(startStr);
            var minutes = startTime.getMinutes();
            if (minutes !== 0 && minutes !== 30) {
                alert("분은 00분 또는 30분만 선택 가능합니다.");
                return;
            }
            var now = new Date();
            if (startTime < now) {
                alert("현재 시간 이후에만 스케줄을 생성할 수 있습니다.");
                return;
            }
            if (userCreatedEvents.length > 0) {
                userCreatedEvents[0].instance.remove();
                userCreatedEvents = [];
            }
            var endTime = new Date(startTime.getTime() + 60 * 60 * 1000);
            var newEventData = {
                title: userName, // 로그인한 사용자의 이름
                start: startTime,
                end: endTime,
                allDay: false,
                color: 'yellow',
                isUserCreated: true
            };
            // 여기서 사용자에게 확인하기 전에 이벤트를 추가하지 않고, 확인 후 추가하도록 할 수도 있지만,
            // 기존 구조를 유지하면서, 저장 전 AJAX 호출 시 eventInstance.remove()로 이벤트를 숨깁니다.
            var addedEvent = mainCalendar.addEvent(newEventData);
            userCreatedEvents.push({ data: newEventData, instance: addedEvent });
            if (confirm("선택한 이벤트를 저장하시겠습니까?")) {
                userCreatedEvents.forEach(function(item) {
                    sendUserEventToServer(item.data, item.instance);
                });
            } else {
                userCreatedEvents.forEach(function(item) {
                    item.instance.remove();
                });
            }
            userCreatedEvents = [];
            userSelectionMode = false;
            ptButton.textContent = "일정 선택";
            mainCalendarEl.classList.remove('selection-mode');
            scheduleForm.style.display = 'none';
            deleteButton.style.display = 'none';
        }
    });

    function sendUserEventToServer(eventData, eventInstance) {
        $.ajax({
            url: '/calendar',
            type: 'GET',
            data: { 
                title: eventData.title,
                start: formatLocalDateTime(eventData.start),
                end: formatLocalDateTime(eventData.end),
                allDay: eventData.allDay,
                userId: userId,
                trainerId: trainerId
            },
            dataType: 'text',
            success: function(response) {
                // 이벤트를 미리 제거하여 alert 동안 캘린더에 표시되지 않도록 합니다.
                eventInstance.remove();
                if(response === 'success'){
                    alert('저장이 완료되었습니다.');
                    window.location.href = '/firstUserCalendar?userId=' + userId;
                } else if(response === "noRange"){
                    alert('이용가능한 시간이 아닙니다.');
                } else if(response === "alreadySchedule"){
                    alert('이미 다른사람의 예약이 있습니다.');
                } else if(response === "alreadyHaveSchedule"){
                    alert('이미 예약이 있습니다.');
                }
            },
            error: function() {
                eventInstance.remove();
            }
        });
    }
    
    // 삭제 버튼 이벤트 리스너 (주간/일간 뷰에서만 활성화됨)
    deleteButton.addEventListener('click', function() {
        var now = new Date();
        var eventsToDelete = mainCalendar.getEvents().filter(function(event) {
            return event.extendedProps.userId &&
                   Number(event.extendedProps.userId) === Number(userId) &&
                   event.start >= now;
        });
    
        if (eventsToDelete.length === 0) {
            alert("삭제 가능한 일정이 없습니다.");
            return;
        }
    
        if (confirm("선택한 이벤트를 삭제하시겠습니까?")) {
            eventsToDelete.forEach(function(event) {
                var eventData = {
                    scheduleId: event.extendedProps.scheduleId, // PK 값
                    userId: event.extendedProps.userId,
                    title: event.title,
                    start: event.startStr,
                    end: event.endStr,
                    allDay: event.allDay
                };
                ajaxDeleteEvent(eventData);
                event.remove();
            });
        }
    });
    
    // 삭제 AJAX 함수 (삭제 URL: /usercalendar/delete)
    function ajaxDeleteEvent(eventData) {
        $.ajax({
            url: '/usercalendar/delete',
            type: 'GET',
            data: eventData,
            dataType: 'json',
            success: function(response) {
                console.log("삭제 성공:", response);
            },
            error: function() {
                console.error("삭제 요청 실패");
            }
        });
    }
});

function toggleDropdown() {
        var dropdown = document.getElementById("dropdownMenu");
        dropdown.classList.toggle("hidden");
    }

    // 클릭 외부 감지하여 닫기
    document.addEventListener("click", function(event) {
        var dropdown = document.getElementById("dropdownMenu");
        var button = document.getElementById("userMenu");
        if (!button.contains(event.target) && !dropdown.contains(event.target)) {
            dropdown.classList.add("hidden");
        }
    });
